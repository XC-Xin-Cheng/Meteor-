package zgoly.meteorist.modules.rangeactions.rangeactions;

import java.util.List;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting.Builder;

public class CommandsRangeAction extends BaseRangeAction {
   public static final String type = "Commands";
   protected final SettingGroup sgCommandsRangeAction = this.settings.createGroup("Commands Range Action");
   public final Setting<List<String>> commands = this.sgCommandsRangeAction
      .add(((Builder)((Builder)new Builder().name("commands")).description("Commands to send.")).defaultValue(new String[]{"/spawn"}).build());
   public final Setting<Integer> delay = this.sgCommandsRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay"))
                  .description("Delay after sending a commands in ticks (20 ticks = 1 sec)."))
               .defaultValue(20))
            .min(1)
            .sliderRange(1, 40)
            .build()
      );
   public final Setting<Integer> commandsPerTick = this.sgCommandsRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("commands-per-tick"))
                  .description("Number of commands to send per tick."))
               .defaultValue(1))
            .min(1)
            .sliderRange(1, 10)
            .build()
      );

   @Override
   public String getTypeName() {
      return "Commands";
   }

   public CommandsRangeAction copy() {
      return (CommandsRangeAction)new CommandsRangeAction().fromTag(this.toTag());
   }
}
