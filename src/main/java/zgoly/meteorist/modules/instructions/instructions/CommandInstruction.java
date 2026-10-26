package zgoly.meteorist.modules.instructions.instructions;

import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;

public class CommandInstruction extends BaseInstruction {
   public static final String type = "Command";
   protected final SettingGroup sgCommandInstruction = this.settings.createGroup("Command Instruction");
   public final Setting<String> command = this.sgCommandInstruction
      .add(((Builder)((Builder)new Builder().name("command")).description("The command to run.")).renderer(StarscriptTextBoxRenderer.class).build());
   public final Setting<Integer> runCount = this.sgCommandInstruction
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("run-count"))
                  .description("The number of times to run the command."))
               .defaultValue(1))
            .sliderRange(1, 10)
            .min(1)
            .build()
      );
   public final Setting<Integer> delayBetweenRuns = this.sgCommandInstruction
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay-between-runs"))
                  .description("The delay between runs in ticks."))
               .defaultValue(10))
            .min(0)
            .build()
      );

   @Override
   public String getTypeName() {
      return "Command";
   }

   public CommandInstruction copy() {
      return (CommandInstruction)new CommandInstruction().fromTag(this.toTag());
   }
}
