package zgoly.meteorist.modules.instructions.instructions;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;

public class DelayInstruction extends BaseInstruction {
   public static final String type = "Delay";
   protected final SettingGroup sgDelayInstruction = this.settings.createGroup("Delay Instruction");
   public final Setting<Integer> delay = this.sgDelayInstruction
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("delay")).description("The number of ticks to wait.")).defaultValue(20))
            .sliderRange(1, 20)
            .min(1)
            .build()
      );

   @Override
   public String getTypeName() {
      return "Delay";
   }

   public DelayInstruction copy() {
      return (DelayInstruction)new DelayInstruction().fromTag(this.toTag());
   }
}
