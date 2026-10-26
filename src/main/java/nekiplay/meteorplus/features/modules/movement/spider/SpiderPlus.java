package nekiplay.meteorplus.features.modules.movement.spider;

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
import nekiplay.meteorplus.features.modules.movement.spider.modes.Eclip;
import nekiplay.meteorplus.features.modules.movement.spider.modes.Matrix;
import nekiplay.meteorplus.features.modules.movement.spider.modes.Vulcan;

public class SpiderPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<SpiderModes> spiderMode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("The method of applying spider."))
                     .defaultValue(SpiderModes.Matrix))
                  .onModuleActivated(spiderModesSetting -> this.onSpiderModeChanged((SpiderModes)spiderModesSetting.get())))
               .onChanged(this::onSpiderModeChanged))
            .build()
      );
   public final Setting<Integer> Blocks = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("blocks"))
                     .defaultValue(3))
                  .description("Don't touch if you don't know what it does."))
               .visible(() -> this.spiderMode.get() == SpiderModes.Elytra_clip))
            .range(0, 10)
            .build()
      );
   private SpiderMode currentMode;

   public SpiderPlus() {
      super(Categories.Movement, "spider+", "Bypass spider");
      this.onSpiderModeChanged((SpiderModes)this.spiderMode.get());
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

   private void onSpiderModeChanged(SpiderModes mode) {
      switch (mode) {
         case Matrix:
            this.currentMode = new Matrix();
            break;
         case Vulcan:
            this.currentMode = new Vulcan();
            break;
         case Elytra_clip:
            this.currentMode = new Eclip();
      }
   }
}
