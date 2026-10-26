package nekiplay.meteorplus.features.modules.movement.jesus;

import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.features.modules.movement.jesus.modes.MatrixZoom;
import nekiplay.meteorplus.features.modules.movement.jesus.modes.MatrixZoom2;
import nekiplay.meteorplus.features.modules.movement.jesus.modes.NCP;
import nekiplay.meteorplus.features.modules.movement.jesus.modes.VulcanExploit;

public class JesusPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<JesusModes> jesusMode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("The method of applying jesus."))
                     .defaultValue(JesusModes.Matrix_Zoom))
                  .onModuleActivated(spiderModesSetting -> this.onJesusModeChanged((JesusModes)spiderModesSetting.get())))
               .onChanged(this::onJesusModeChanged))
            .build()
      );
   public final Setting<Double> speed = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("speed"))
               .description("Jesus speed."))
            .defaultValue(1.25)
            .max(2500.0)
            .sliderRange(0.0, 2500.0)
            .build()
      );
   public final Setting<Double> limit_speed = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("limit-speed"))
                  .description("Jesus speed."))
               .visible(() -> this.jesusMode.get() == JesusModes.NCP))
            .build()
      );
   public final Setting<Boolean> autoSwapVulcan = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("auto-swap"))
                     .description("Auto swap."))
                  .defaultValue(true))
               .visible(() -> this.jesusMode.get() == JesusModes.Vulcan_Exploit))
            .build()
      );
   private JesusMode currentMode;

   public JesusPlus() {
      super(Categories.Movement, "jesus+", "Bypass-jesus");
      this.onJesusModeChanged((JesusModes)this.jesusMode.get());
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

   @EventHandler
   public void onCanWalkOnFluid(CanWalkOnFluidEvent event) {
      this.currentMode.onCanWalkOnFluid(event);
   }

   @EventHandler
   public void onCollisionShape(CollisionShapeEvent event) {
      this.currentMode.onCollisionShape(event);
   }

   @EventHandler
   private void onPlayerMoveEvent(PlayerMoveEvent event) {
      this.currentMode.onPlayerMoveEvent(event);
   }

   private void onJesusModeChanged(JesusModes mode) {
      switch (mode) {
         case Matrix_Zoom:
            this.currentMode = new MatrixZoom();
            break;
         case Matrix_Zoom_2:
            this.currentMode = new MatrixZoom2();
            break;
         case Vulcan_Exploit:
            this.currentMode = new VulcanExploit();
            break;
         case NCP:
            this.currentMode = new NCP();
      }
   }
}
