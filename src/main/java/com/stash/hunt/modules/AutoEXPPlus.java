package com.stash.hunt.modules;

import com.stash.hunt.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class AutoEXPPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<AutoEXPPlus.Mode> mode = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("mode")).description("Which items to repair.")).defaultValue(AutoEXPPlus.Mode.Both)).build());
   private final Setting<Boolean> replenish = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("replenish"))
                  .description("Automatically replenishes exp into a selected hotbar slot."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Integer> slot = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("exp-slot"))
                     .description("The slot to replenish exp into."))
                  .visible(this.replenish::get))
               .defaultValue(6))
            .range(1, 9)
            .sliderRange(1, 9)
            .build()
      );
   private final Setting<Integer> minThreshold = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("min-threshold"))
                  .description("The minimum durability percentage that an item needs to fall to, to be repaired."))
               .defaultValue(30))
            .range(1, 100)
            .sliderRange(1, 100)
            .build()
      );
   private final Setting<Integer> maxThreshold = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("max-threshold"))
                  .description("The maximum durability percentage to repair items to."))
               .defaultValue(80))
            .range(1, 100)
            .sliderRange(1, 100)
            .build()
      );
   private final Setting<Boolean> ignoreElytra = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-elytra"))
                  .description("Ignore elytra when repairing."))
               .defaultValue(false))
            .build()
      );
   private int repairingI;

   public AutoEXPPlus() {
      super(Addon.CATEGORY, "auto-exp-plus", "Automatically repairs your armor and tools in pvp.");
   }

   public void onActivate() {
      this.repairingI = -1;
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.repairingI == -1) {
         if (this.mode.get() != AutoEXPPlus.Mode.Hands) {
            for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
               ItemStack stack = this.mc.player.getItemBySlot(slot);
               if (this.needsRepair(stack, ((Integer)this.minThreshold.get()).intValue())) {
                  this.repairingI = 36 + slot.getIndex();
                  break;
               }
            }
         }

         if (this.mode.get() != AutoEXPPlus.Mode.Armor && this.repairingI == -1) {
            for (InteractionHand hand : InteractionHand.values()) {
               if (this.needsRepair(this.mc.player.getItemInHand(hand), ((Integer)this.minThreshold.get()).intValue())) {
                  this.repairingI = hand == InteractionHand.MAIN_HAND ? this.mc.player.getInventory().getSelectedSlot() : 40;
                  break;
               }
            }
         }
      }

      if (this.repairingI != -1) {
         if (!this.needsRepair(this.mc.player.getInventory().getItem(this.repairingI), ((Integer)this.maxThreshold.get()).intValue())) {
            this.repairingI = -1;
            return;
         }

         FindItemResult exp = InvUtils.find(new Item[]{Items.EXPERIENCE_BOTTLE});
         if (exp.found()) {
            if (!exp.isHotbar() && !exp.isOffhand()) {
               if (!(Boolean)this.replenish.get()) {
                  return;
               }

               InvUtils.move().from(exp.slot()).toHotbar((Integer)this.slot.get() - 1);
            }

            Rotations.rotate(this.mc.player.getYRot(), 90.0, () -> {
               if (exp.getHand() != null) {
                  this.mc.gameMode.useItem(this.mc.player, exp.getHand());
               } else {
                  InvUtils.swap(exp.slot(), true);
                  this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND);
                  InvUtils.swapBack();
               }
            });
         }
      }
   }

   private boolean needsRepair(ItemStack itemStack, double threshold) {
      return !itemStack.isEmpty() && Utils.hasEnchantments(itemStack, new ResourceKey[]{Enchantments.MENDING})
         ? (double)(itemStack.getMaxDamage() - itemStack.getDamageValue()) / itemStack.getMaxDamage() * 100.0 <= threshold
         : false;
   }

   public static enum Mode {
      Armor,
      Hands,
      Both;
   }
}
