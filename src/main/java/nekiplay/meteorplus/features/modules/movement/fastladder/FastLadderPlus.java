package nekiplay.meteorplus.features.modules.movement.fastladder;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.features.modules.movement.fastladder.modes.Spartan;

public class FastLadderPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<FastLadderModes> spiderMode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("The method of applying spider."))
                     .defaultValue(FastLadderModes.Spartan))
                  .onModuleActivated(spiderModesSetting -> this.onSpiderModeChanged((FastLadderModes)spiderModesSetting.get())))
               .onChanged(this::onSpiderModeChanged))
            .build()
      );
   private FastLadderMode currentMode;

   public FastLadderPlus() {
      super(Categories.Movement, "fast-climb+", "Bypass fast-climb");
      this.onSpiderModeChanged((FastLadderModes)this.spiderMode.get());
   }

   public void onActivate() {
      this.currentMode.onActivate();
   }

   public void onDeactivate() {
      this.currentMode.onDeactivate();
   }

   @EventHandler
   private void onPreTick(Pre event) {
      this.currentMode.onTickEventPre(event);
   }

   @EventHandler
   private void onPostTick(Post event) {
      this.currentMode.onTickEventPost(event);
   }

   @EventHandler
   public void onSendPacket(Send event) {
      this.currentMode.onSendPacket(event);
   }

   @EventHandler
   public void onSentPacket(Sent event) {
      this.currentMode.onSentPacket(event);
   }

   private void onSpiderModeChanged(FastLadderModes mode) {
      switch (mode) {
         case Spartan:
            this.currentMode = new Spartan();
      }
   }
}
