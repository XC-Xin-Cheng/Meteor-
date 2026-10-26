package fr.crazycat256.satellite.modules;

import fr.crazycat256.satellite.Addon;
import fr.crazycat256.satellite.utils.TPUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class Phase extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Phase.Mode> mode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("The mode of the phase.")).defaultValue(Phase.Mode.TP))
               .onChanged(v -> {
                  if (this.isActive()) {
                     this.startPos = this.mc.player.position();
                  }
               }))
            .build()
      );
   private final Setting<Double> horizontalSpeed = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("horizontal-speed"))
               .description("The horizontal speed."))
            .defaultValue(1.0)
            .min(0.0)
            .sliderMax(10.0)
            .build()
      );
   private final Setting<Double> verticalSpeed = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("vertical-speed"))
               .description("The vertical speed."))
            .defaultValue(1.0)
            .min(0.0)
            .sliderMax(10.0)
            .build()
      );
   private Vec3 startPos;

   public Phase() {
      super(Addon.CATEGORY, "Phase", "Allows you to fly trough blocks.");
   }

   public void onActivate() {
      this.startPos = this.mc.player.position();
   }

   public void onDeactivate() {
      if (this.mc.player != null && this.mode.get() == Phase.Mode.TP) {
         TPUtils.PaperTP(this.startPos, this.mc.player.position());
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null && this.mc.getCameraEntity() != null) {
         double forwardX = this.mc.player.getLookAngle().x;
         double forwardZ = this.mc.player.getLookAngle().z;
         Vec3 forward = new Vec3(forwardX, 0.0, forwardZ).normalize();
         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         double hs = (Double)this.horizontalSpeed.get() / 10.0;
         double vs = (Double)this.verticalSpeed.get() / 10.0;
         if (this.mc.options.keySprint.isDown()) {
            hs *= 3.0;
            vs *= 3.0;
         }

         if (this.mc.options.keyUp.isDown()) {
            x += forward.x * hs;
            z += forward.z * hs;
         }

         if (this.mc.options.keyDown.isDown()) {
            x -= forward.x * hs;
            z -= forward.z * hs;
         }

         if (this.mc.options.keyLeft.isDown()) {
            x += forward.z * hs;
            z -= forward.x * hs;
         }

         if (this.mc.options.keyRight.isDown()) {
            x -= forward.z * hs;
            z += forward.x * hs;
         }

         if (this.mc.options.keyJump.isDown()) {
            y += vs;
         }

         if (this.mc.options.keyShift.isDown()) {
            y -= vs;
            this.mc.player.setShiftKeyDown(false);
         }

         this.mc.player.setDeltaMovement(x, y, z);
      }
   }

   @EventHandler
   private void onSendPacket(Send event) {
      if (this.mc.player != null) {
         if (event.packet instanceof ServerboundMovePlayerPacket && this.mode.get() == Phase.Mode.TP) {
            event.cancel();
         }
      }
   }

   @EventHandler
   private void onCollisionShape(CollisionShapeEvent event) {
      event.cancel();
   }

   public static enum Mode {
      NoClip,
      TP;
   }
}
