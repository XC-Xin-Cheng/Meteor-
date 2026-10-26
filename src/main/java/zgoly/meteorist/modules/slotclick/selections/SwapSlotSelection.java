package zgoly.meteorist.modules.slotclick.selections;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;

public class SwapSlotSelection extends DefaultSlotSelection {
   public static final String type = "Swap";
   public final SettingGroup sgSwapSlotSelection = this.settings.createGroup("Swap Slot Selection");
   public final Setting<Integer> fromSlot = this.sgSwapSlotSelection
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("from-slot")).description("Slot to swap from.")).defaultValue(0))
               .min(0)
               .onChanged(value -> reloadParent()))
            .build()
      );
   public final Setting<Integer> toSlot = this.sgSwapSlotSelection
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("to-slot")).description("Slot to swap to.")).defaultValue(1))
               .min(0)
               .onChanged(value -> reloadParent()))
            .build()
      );

   @Override
   public String getTypeName() {
      return "Swap";
   }

   public SwapSlotSelection copy() {
      return (SwapSlotSelection)new SwapSlotSelection().fromTag(this.toTag());
   }
}
