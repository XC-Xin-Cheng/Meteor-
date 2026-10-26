package zgoly.meteorist.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import zgoly.meteorist.Meteorist;

public class AutoRepair extends Module {
   private final List<AutoRepair.SlotConfig> slotConfigs = new ArrayList<>();

   public AutoRepair() {
      super(Meteorist.CATEGORY, "auto-repair", "Sends repair commands for equipped items when durability is low.");
      EquipmentSlot[] slotTypes = new EquipmentSlot[]{
         EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
      };

      for (EquipmentSlot slotType : slotTypes) {
         AutoRepair.SlotConfig slotConfig = new AutoRepair.SlotConfig(slotType);
         this.slotConfigs.add(slotConfig);
      }
   }

   @EventHandler
   private void onTick(Post event) {
      for (AutoRepair.SlotConfig slotConfig : this.slotConfigs) {
         ItemStack item = this.mc.player.getItemBySlot(slotConfig.slot);
         if ((Boolean)slotConfig.enabled.get() && !item.isEmpty() && item.getMaxDamage() > 0) {
            if (slotConfig.timer < (Integer)slotConfig.delay.get()) {
               slotConfig.timer++;
            } else {
               int currentDurability = item.getMaxDamage() - item.getDamageValue();
               boolean shouldRepair = false;
               if (slotConfig.mode.get() == AutoRepair.Mode.Total && currentDurability <= (Integer)slotConfig.minDurability.get()) {
                  shouldRepair = true;
               } else if (slotConfig.mode.get() == AutoRepair.Mode.Percentage
                  && currentDurability * 100 / item.getMaxDamage() <= (Integer)slotConfig.minDurabilityPercentage.get()) {
                  shouldRepair = true;
               }

               if (shouldRepair) {
                  ChatUtils.sendPlayerMsg((String)slotConfig.repairCommand.get());
                  slotConfig.timer = 0;
               }
            }
         }
      }
   }

   public static enum Mode {
      Total,
      Percentage;
   }

   private class SlotConfig {
      public final EquipmentSlot slot;
      public final Setting<Boolean> enabled;
      public final Setting<String> repairCommand;
      public final Setting<AutoRepair.Mode> mode;
      public final Setting<Integer> minDurability;
      public final Setting<Integer> minDurabilityPercentage;
      public final Setting<Integer> delay;
      public int timer;

      public SlotConfig(EquipmentSlot slot) {
         Objects.requireNonNull(AutoRepair.this);
         super();
         this.slot = slot;
         this.timer = 0;
         SettingGroup group = AutoRepair.this.settings.createGroup(slot.getName());
         this.enabled = group.add(
            ((Builder)((Builder)((Builder)new Builder().name("enabled")).description("Enable durability check for the " + slot.getName() + " slot."))
                  .defaultValue(true))
               .build()
         );
         this.repairCommand = group.add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("repair-command"))
                     .description("Command to repair the " + slot.getName() + " slot."))
                  .defaultValue("/repair " + slot.getName().toLowerCase()))
               .build()
         );
         this.mode = group.add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("mode"))
                     .description("Total - calculate item durability in numbers; Percentage - calculate item durability in percentage."))
                  .defaultValue(AutoRepair.Mode.Percentage))
               .build()
         );
         this.minDurability = group.add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("min-durability"))
                        .description("Minimum durability for the " + slot.getName() + " slot."))
                     .defaultValue(10))
                  .min(1)
                  .sliderRange(1, 1000)
                  .visible(() -> this.mode.get() == AutoRepair.Mode.Total))
               .build()
         );
         this.minDurabilityPercentage = group.add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("min-durability-percentage"))
                        .description("Minimum durability percentage for the " + slot.getName() + " slot."))
                     .defaultValue(10))
                  .min(1)
                  .sliderRange(1, 100)
                  .visible(() -> this.mode.get() == AutoRepair.Mode.Percentage))
               .build()
         );
         this.delay = group.add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("delay"))
                     .description("Delay after sending a command in ticks (20 ticks = 1 sec)."))
                  .defaultValue(20))
               .min(1)
               .sliderRange(1, 40)
               .build()
         );
      }
   }
}
