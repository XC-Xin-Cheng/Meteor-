package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;

public class AutoFeed extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<String> feedCommand = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("feed-command")).description("Command to refill hunger bar.")).defaultValue("/feed")).build());
   private final Setting<Integer> hungerLevel = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("hunger-level"))
                  .description("Hunger level at which to send the command."))
               .defaultValue(12))
            .min(1)
            .sliderRange(1, 20)
            .build()
      );
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay"))
                  .description("Delay after sending a command in ticks (20 ticks = 1 sec)."))
               .defaultValue(20))
            .min(1)
            .sliderRange(1, 40)
            .build()
      );
   private int timer;

   public AutoFeed() {
      super(Meteorist.CATEGORY, "auto-feed", "Writes command in chat when hunger level is low.");
   }

   public void onActivate() {
      this.timer = 0;
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.timer >= (Integer)this.delay.get() && this.mc.player.getFoodData().getFoodLevel() <= (Integer)this.hungerLevel.get()) {
         ChatUtils.sendPlayerMsg((String)this.feedCommand.get());
         this.timer = 0;
      } else {
         this.timer++;
      }
   }
}
