package zgoly.meteorist.modules.slotclick.selections;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;

public class DelaySelection extends BaseSlotSelection {
   public static final String type = "Delay";
   public final SettingGroup sgDelaySelection = this.settings.createGroup("Delay Selection");
   public final Setting<Integer> delay = this.sgDelaySelection
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("delay")).description("Selection delay.")).defaultValue(10))
               .sliderRange(0, 20)
               .min(0)
               .onChanged(value -> reloadParent()))
            .build()
      );

   @Override
   public String getTypeName() {
      return "Delay";
   }

   public DelaySelection copy() {
      return (DelaySelection)new DelaySelection().fromTag(this.toTag());
   }
}
