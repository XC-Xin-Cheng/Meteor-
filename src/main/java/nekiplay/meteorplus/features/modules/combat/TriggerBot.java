package nekiplay.meteorplus.features.modules.combat;

import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.features.modules.combat.criticals.CriticalsPlus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Unique;

public class TriggerBot extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgTiming = this.settings.createGroup("Timing");
   private final Setting<Set<EntityType<?>>> entities = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("entities")).description("Entities to attack.")).onlyAttackable().build());
   private final Setting<Boolean> babies = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("babies"))
                  .description("Whether or not to attack baby variants of the entity."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> smartDelay = this.sgTiming
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("smart-delay"))
                  .description("Uses the vanilla cooldown to attack entities."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Integer> hitDelay = this.sgTiming
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("hit-delay"))
                     .description("How fast you hit the entity in ticks."))
                  .defaultValue(0))
               .min(0)
               .sliderMax(60)
               .visible(() -> !(Boolean)this.smartDelay.get()))
            .build()
      );
   private final Setting<Boolean> randomDelayEnabled = this.sgTiming
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("random-delay-enabled"))
                     .description("Adds a random delay between hits to attempt to bypass anti-cheats."))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.smartDelay.get()))
            .build()
      );
   private final Setting<Integer> randomDelayMax = this.sgTiming
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("random-delay-max"))
                     .description("The maximum value for random delay."))
                  .defaultValue(4))
               .min(0)
               .sliderMax(20)
               .visible(() -> (Boolean)this.randomDelayEnabled.get() && !(Boolean)this.smartDelay.get()))
            .build()
      );
   private final Setting<Boolean> onlyCrits = this.sgTiming
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("only-crits"))
                  .description("Attack enemy only if this attack crit after jump."))
               .defaultValue(true))
            .build()
      );
   @Unique
   private final Setting<Boolean> ignoreOnlyCritsOnLevitation = this.sgTiming
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-only-crits-on-levetation"))
                  .defaultValue(true))
               .visible(() -> (Boolean)this.onlyCrits.get()))
            .build()
      );
   private int hitDelayTimer;

   public TriggerBot() {
      super(Categories.Combat, "Trigger-bot", "Attacks specified entities around you.");
   }

   public void onDeactivate() {
      this.hitDelayTimer = 0;
   }

   private boolean entityCheck(Entity entity) {
      if (!entity.equals(this.mc.player) && !entity.equals(this.mc.getCameraEntity())) {
         if ((!(entity instanceof LivingEntity) || !((LivingEntity)entity).isDeadOrDying()) && entity.isAlive()) {
            if (!((Set)this.entities.get()).contains(entity.getType())) {
               return false;
            } else if (entity instanceof OwnableEntity tameable
               && tameable.getOwner() != null
               && tameable.getOwner().getUUID().equals(this.mc.player.getUUID())) {
               return false;
            } else {
               if (entity instanceof Player player) {
                  if (player.isCreative()) {
                     return false;
                  }

                  if (!Friends.get().shouldAttack(player)) {
                     return false;
                  }

                  AntiBotPlus antiBotPlus = (AntiBotPlus)Modules.get().get(AntiBotPlus.class);
                  Teams teams = (Teams)Modules.get().get(Teams.class);
                  if (antiBotPlus != null && antiBotPlus.isBot(player)) {
                     return false;
                  }

                  if (teams != null && teams.isInYourTeam(player)) {
                     return false;
                  }
               }

               return !(entity instanceof Animal) || (Boolean)this.babies.get() || !((Animal)entity).isBaby();
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean delayCheck() {
      if ((Boolean)this.onlyCrits.get() && !CriticalsPlus.allowCrit() && CriticalsPlus.needCrit(this.mc.crosshairPickEntity)) {
         if ((Boolean)this.ignoreOnlyCritsOnLevitation.get() && !Objects.requireNonNull(this.mc.player).hasEffect(MobEffects.LEVITATION)) {
            return false;
         }

         if (!(Boolean)this.ignoreOnlyCritsOnLevitation.get()) {
            return false;
         }
      }

      if ((Boolean)this.smartDelay.get()) {
         return this.mc.player.getAttackStrengthScale(0.5F) >= 1.0F;
      } else if (this.hitDelayTimer > 0) {
         this.hitDelayTimer--;
         return false;
      } else {
         this.hitDelayTimer = (Integer)this.hitDelay.get();
         if ((Boolean)this.randomDelayEnabled.get()) {
            this.hitDelayTimer = (int)(this.hitDelayTimer + Math.round(Math.random() * ((Integer)this.randomDelayMax.get()).intValue()));
         }

         return true;
      }
   }

   @EventHandler
   private void onTick(Render3DEvent event) {
      if (this.mc.player.isAlive() && PlayerUtils.getGameMode() != GameType.SPECTATOR) {
         if (this.mc.crosshairPickEntity != null) {
            if (this.delayCheck() && this.entityCheck(this.mc.crosshairPickEntity)) {
               this.hitEntity(this.mc.crosshairPickEntity);
            }
         }
      }
   }

   private void hitEntity(Entity target) {
      this.mc.gameMode.attack(this.mc.player, target);
      this.mc.player.swing(InteractionHand.MAIN_HAND);
   }
}
