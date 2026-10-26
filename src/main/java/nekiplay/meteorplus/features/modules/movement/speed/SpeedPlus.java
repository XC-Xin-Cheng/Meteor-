package nekiplay.meteorplus.features.modules.movement.speed;

import meteordevelopment.meteorclient.events.entity.player.JumpVelocityMultiplierEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
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
import nekiplay.meteorplus.features.modules.movement.speed.modes.AACHop438;
import nekiplay.meteorplus.features.modules.movement.speed.modes.NCPHop;
import nekiplay.meteorplus.features.modules.movement.speed.modes.matrix.Matrix;
import nekiplay.meteorplus.features.modules.movement.speed.modes.matrix.Matrix6_7_0;
import nekiplay.meteorplus.features.modules.movement.speed.modes.matrix.MatrixExploit;
import nekiplay.meteorplus.features.modules.movement.speed.modes.matrix.MatrixExploit2;
import nekiplay.meteorplus.features.modules.movement.speed.modes.vulcan.Vulcan;
import nekiplay.meteorplus.features.modules.movement.speed.modes.vulcan.Vulcan_2_8_6;

public class SpeedPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<SpeedModes> speedMode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("The method of applying speed."))
                     .defaultValue(SpeedModes.Matrix_Exploit))
                  .onModuleActivated(spiderModesSetting -> this.onSpeedModeChanged((SpeedModes)spiderModesSetting.get())))
               .onChanged(this::onSpeedModeChanged))
            .build()
      );
   public final Setting<Double> speedMatrix = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("Speed"))
                  .description("Speed."))
               .defaultValue(4.0)
               .visible(() -> this.speedMode.get() == SpeedModes.Matrix_Exploit || this.speedMode.get() == SpeedModes.Matrix_Exploit_2))
            .build()
      );
   public final Setting<Double> speedVulcanef2 = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("Speed-effect-2"))
                  .description("Speed 2 effect."))
               .defaultValue(45.0)
               .max(75.0)
               .sliderRange(0.0, 75.0)
               .visible(() -> this.speedMode.get() == SpeedModes.Vulcan))
            .build()
      );
   public final Setting<Double> speedVulcanef1 = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("Speed-effect-1"))
                  .description("Speed 1 effect."))
               .defaultValue(45.0)
               .max(75.0)
               .sliderRange(0.0, 75.0)
               .visible(() -> this.speedMode.get() == SpeedModes.Vulcan))
            .build()
      );
   public final Setting<Double> speedVulcanef0 = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("Speed-effect-0"))
                  .description("Speed 0 effect."))
               .defaultValue(35.0)
               .max(75.0)
               .sliderRange(0.0, 75.0)
               .visible(() -> this.speedMode.get() == SpeedModes.Vulcan))
            .build()
      );
   public final Setting<Boolean> autoSwapVulcan = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("auto-swap"))
                     .description("Auto swap."))
                  .defaultValue(true))
               .visible(() -> this.speedMode.get() == SpeedModes.Vulcan))
            .build()
      );
   private SpeedMode currentMode;

   public SpeedPlus() {
      super(Categories.Movement, "speed+", "Bypass speed");
      this.onSpeedModeChanged((SpeedModes)this.speedMode.get());
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
   private void onPlayerMoveEvent(PlayerMoveEvent event) {
      this.currentMode.onPlayerMoveEvent(event);
   }

   @EventHandler
   public void onJump(JumpVelocityMultiplierEvent event) {
      this.currentMode.onJump(event);
   }

   private void onSpeedModeChanged(SpeedModes mode) {
      switch (mode) {
         case Matrix_Exploit_2:
            this.currentMode = new MatrixExploit2();
            break;
         case Matrix_Exploit:
            this.currentMode = new MatrixExploit();
            break;
         case Matrix_6dot7dot0:
            this.currentMode = new Matrix6_7_0();
            break;
         case Matrix:
            this.currentMode = new Matrix();
            break;
         case AAC_Hop_4dot3dot8:
            this.currentMode = new AACHop438();
            break;
         case Vulcan:
            this.currentMode = new Vulcan();
            break;
         case Vulcan_2dot8dot6:
            this.currentMode = new Vulcan_2_8_6();
            break;
         case NCP_Hop:
            this.currentMode = new NCPHop();
      }
   }
}
