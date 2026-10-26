package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import baritone.api.BaritoneAPI;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public class AutoPot extends Module {
   private static final Class<? extends Module>[] AURAS = new Class[]{KillAura.class, CrystalAura.class, AnchorAura.class, BedAura.class};
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<List<MobEffect>> usablePotions = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("potions-to-use")).description("The potions to use."))
            .defaultValue(
               new MobEffect[]{(MobEffect)MobEffects.INSTANT_HEALTH.value(), (MobEffect)MobEffects.STRENGTH.value(), (MobEffect)MobEffects.BAD_OMEN.value()}
            )
            .build()
      );
   private final Setting<Boolean> useSplashPots = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("splash-potions"))
                  .description("Allow the use of splash potions"))
               .defaultValue(true))
            .build()
      );
   private final Setting<Integer> health = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("health"))
                  .description("If health goes below this point, Healing potions will trigger."))
               .defaultValue(15))
            .min(0)
            .sliderMax(20)
            .build()
      );
   private final Setting<Boolean> pauseAuras = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("pause-auras"))
                  .description("Pauses all auras when eating."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> pauseBaritone = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("pause-baritone"))
                  .description("Pause baritone when eating."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> lookDown = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate"))
                  .description("Forces you to rotate downwards when throwing splash potions."))
               .defaultValue(true))
            .build()
      );
   private int slot;
   private int prevSlot;
   private boolean drinking;
   private boolean splashing;
   private final List<Class<? extends Module>> wasAura = new ArrayList<>();
   private boolean wasBaritone;

   public AutoPot() {
      super(MeteorRejectsAddon.CATEGORY, "auto-pot", "Automatically Drinks Potions");
   }

   public void onDeactivate() {
      this.stopPotionUsage();
   }

   @EventHandler
   private void onTick(Pre event) {
      if (!this.mc.player.isUsingItem()) {
         for (MobEffect statusEffect : (List)this.usablePotions.get()) {
            Holder<MobEffect> registryEntry = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(statusEffect);
            if (!this.mc.player.hasEffect(registryEntry)) {
               this.slot = this.potionSlot(statusEffect);
               if (this.slot != -1) {
                  if (registryEntry == MobEffects.INSTANT_HEALTH && this.ShouldDrinkHealth()) {
                     this.startPotionUse();
                     return;
                  }

                  if (registryEntry == MobEffects.INSTANT_HEALTH) {
                     return;
                  }

                  this.startPotionUse();
               }
            }
         }
      }
   }

   @EventHandler
   private void onItemUseCrosshairTarget(ItemUseCrosshairTargetEvent event) {
      if (this.drinking) {
         event.target = null;
      }
   }

   private void setPressed(boolean pressed) {
      this.mc.options.keyUse.setDown(pressed);
   }

   private void drink() {
      this.changeSlot(this.slot);
      this.setPressed(true);
      if (!this.mc.player.isUsingItem()) {
         Utils.rightClick();
      }

      this.drinking = true;
   }

   private void splash() {
      this.changeSlot(this.slot);
      this.setPressed(true);
      this.splashing = true;
   }

   private void stopPotionUsage() {
      this.changeSlot(this.prevSlot);
      this.setPressed(false);
      this.drinking = false;
      this.splashing = false;
      if ((Boolean)this.pauseAuras.get()) {
         for (Class<? extends Module> klass : AURAS) {
            Module module = Modules.get().get(klass);
            if (this.wasAura.contains(klass) && !module.isActive()) {
               module.toggle();
            }
         }
      }

      if ((Boolean)this.pauseBaritone.get() && this.wasBaritone) {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
      }
   }

   private double trueHealth() {
      assert this.mc.player != null;

      return this.mc.player.getHealth();
   }

   private void changeSlot(int slot) {
      this.mc.player.getInventory().setSelectedSlot(slot);
      this.slot = slot;
   }

   private int potionSlot(MobEffect statusEffect) {
      int slot = -1;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = this.mc.player.getInventory().getItem(i);
         if (!stack.isEmpty()) {
            if (this.isOminousBottle(stack)) {
               if (statusEffect == MobEffects.BAD_OMEN.value()) {
                  slot = i;
                  break;
               }
            } else {
               boolean isPotion = stack.getItem() == Items.POTION;
               boolean isSplashPotion = stack.getItem() == Items.SPLASH_POTION;
               if (isPotion || isSplashPotion && (Boolean)this.useSplashPots.get()) {
                  PotionContents effects = (PotionContents)stack.getComponents().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

                  for (MobEffectInstance effectInstance : effects.getAllEffects()) {
                     if (effectInstance.getDescriptionId().equals(statusEffect.getDescriptionId())) {
                        slot = i;
                        break;
                     }
                  }

                  if (slot != -1) {
                     break;
                  }
               }
            }
         }
      }

      return slot;
   }

   private void startPotionUse() {
      this.prevSlot = this.mc.player.getInventory().getSelectedSlot();
      ItemStack stack = this.mc.player.getInventory().getItem(this.slot);
      boolean isSplashPotion = stack.getItem() == Items.SPLASH_POTION;
      if (!isSplashPotion || !(Boolean)this.useSplashPots.get()) {
         this.drink();
      } else if ((Boolean)this.lookDown.get()) {
         Rotations.rotate(this.mc.player.getYRot(), 90.0);
         this.splash();
      } else {
         this.splash();
      }

      this.wasAura.clear();
      if ((Boolean)this.pauseAuras.get()) {
         for (Class<? extends Module> klass : AURAS) {
            Module module = Modules.get().get(klass);
            if (module.isActive()) {
               this.wasAura.add(klass);
               module.toggle();
            }
         }
      }

      this.wasBaritone = false;
      if (BaritoneUtils.IS_AVAILABLE && (Boolean)this.pauseBaritone.get() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
         this.wasBaritone = true;
         BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
      }
   }

   private boolean isOminousBottle(ItemStack stack) {
      return stack.getItem() == Items.OMINOUS_BOTTLE;
   }

   private boolean ShouldDrinkHealth() {
      return this.trueHealth() < ((Integer)this.health.get()).intValue();
   }
}
