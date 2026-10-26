package zgoly.meteorist.modules.slotclick.selections;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import net.minecraft.world.inventory.ContainerInput;

public class SlotRangeSelection extends DefaultSlotSelection {
   public static final String type = "Range";
   public final SettingGroup sgMultipleSlotSelection = this.settings.createGroup("Slot Range Selection");
   public final Setting<Integer> fromSlot = this.sgMultipleSlotSelection
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("from-slot")).description("The first slot to select.")).defaultValue(0))
               .min(0)
               .onChanged(var0 -> reloadParent()))
            .build()
      );
   public final Setting<Integer> toSlot = this.sgMultipleSlotSelection
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("to-slot")).description("The last slot to select.")).defaultValue(8))
               .min(0)
               .onChanged(var0 -> reloadParent()))
            .build()
      );
   public final Setting<ContainerInput> action = this.sgMultipleSlotSelection
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("action"))
                  .description("Action to perform."))
               .defaultValue(ContainerInput.PICKUP))
            .build()
      );
   public final Setting<Integer> button = this.sgMultipleSlotSelection
      .add(((Builder)((Builder)new Builder().name("button")).description("Button to press.")).build());
   public final Setting<Integer> delay = this.sgMultipleSlotSelection
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("delay")).description("Delay between each slot selection.")).defaultValue(10))
            .sliderRange(0, 20)
            .min(0)
            .build()
      );
   public int calculatedSlot = 0;

   @Override
   public String getTypeName() {
      return "Range";
   }

   public SlotRangeSelection copy() {
      return (SlotRangeSelection)new SlotRangeSelection().fromTag(this.toTag());
   }
}
