package nekiplay.meteorplus.features.modules.movement.fly;

import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.features.modules.movement.fly.modes.MatrixExploit;
import nekiplay.meteorplus.features.modules.movement.fly.modes.MatrixExploit2;
import nekiplay.meteorplus.features.modules.movement.fly.modes.VulcanClip;

public class FlyPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<FlyModes> flyMode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("The method of applying fly."))
                     .defaultValue(FlyModes.Matrix_Exploit))
                  .onModuleActivated(spiderModesSetting -> this.onFlyModeChanged((FlyModes)spiderModesSetting.get())))
               .onChanged(this::onFlyModeChanged))
            .build()
      );
   public final Setting<Double> speed_1 = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("speed-№1"))
                  .description("Fly speed."))
               .defaultValue(1.25)
               .max(2500.0)
               .sliderRange(0.0, 2500.0)
               .visible(() -> this.flyMode.get() == FlyModes.Matrix_Exploit))
            .build()
      );
   public final Setting<Double> speed2 = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("speed-№2"))
                  .description("Fly speed."))
               .defaultValue(0.3)
               .max(5.0)
               .sliderRange(0.0, 5.0)
               .visible(() -> this.flyMode.get() == FlyModes.Matrix_Exploit_2))
            .build()
      );
   public final Setting<Boolean> canClip = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("can-clip"))
                  .description("Max fly ticks."))
               .visible(() -> this.flyMode.get() == FlyModes.Vulcan_Clip))
            .build()
      );
   public final Setting<Boolean> showInfo = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("show-info"))
                  .description("Displays information about whether this mode is running on the server."))
               .visible(() -> this.flyMode.get() == FlyModes.Vulcan_Clip))
            .build()
      );
   private FlyMode currentMode;

   public FlyPlus() {
      super(Categories.Movement, "flight+", "Bypass fly");
      this.onFlyModeChanged((FlyModes)this.flyMode.get());
   }

   public WWidget getWidget(GuiTheme theme) {
      WWidget widget = super.getWidget(theme);
      return (WWidget)(this.flyMode.get() == FlyModes.Vulcan_Clip ? theme.label("This mode work only on 1.89 servers") : widget);
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
   public void onReceivePacket(Receive event) {
      this.currentMode.onReceivePacket(event);
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

   @EventHandler
   private void onPlayerMoveSendPre(meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent.Pre event) {
      this.currentMode.onPlayerMoveSendPre(event);
   }

   private void onFlyModeChanged(FlyModes mode) {
      switch (mode) {
         case Matrix_Exploit_2:
            this.currentMode = new MatrixExploit2();
            break;
         case Matrix_Exploit:
            this.currentMode = new MatrixExploit();
            break;
         case Vulcan_Clip:
            if ((Boolean)this.showInfo.get()) {
               this.info("Vulcan fly work on 1.8.9 servers", new Object[0]);
            }

            this.currentMode = new VulcanClip();
      }
   }
}
