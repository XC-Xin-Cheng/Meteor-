package fr.crazycat256.satellite.modules;

import fr.crazycat256.satellite.Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixin.ServerboundMovePlayerPacketAccessor;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class VecFly extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> flySpeed = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("fly-speed")).description("The speed to fly at.")).defaultValue(0.35).min(0.0).sliderMax(1.0).build());
   private final Setting<Double> yWeight = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("y-weight")).description("The weight of the y axis.")).defaultValue(0.5).min(0.0).sliderMax(1.0).build());
   private final Setting<VecFly.SpeedBypassMode> speedBypassMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("speed-bypass-mode"))
                  .description("The mode to use when speed bypass is enabled."))
               .defaultValue(VecFly.SpeedBypassMode.OnSprint))
            .build()
      );
   private final Setting<Double> speedBypassMultiplier = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("speed-bypass-multiplier")).description("The speed multiplier when speed bypass is enabled."))
               .defaultValue(10.0)
               .min(0.0)
               .sliderMax(100.0)
               .visible(() -> this.speedBypassMode.get() != VecFly.SpeedBypassMode.None))
            .build()
      );
   private final Setting<Integer> stepDownDelay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("step-down-delay"))
                     .description("The delay in ticks between each step down."))
                  .defaultValue(20))
               .min(0)
               .sliderMax(20)
               .visible(() -> this.speedBypassMode.get() != VecFly.SpeedBypassMode.None))
            .build()
      );
   private int ticks = 0;
   private boolean bypassLastTick = false;
   private Vec3 lastForward = Vec3.ZERO;

   public VecFly() {
      super(Addon.CATEGORY, "vec-fly", "Allows you to fly at a precise speed.");
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null && this.mc.level != null) {
         Vec3 dir = Vec3.directionFromRotation(0.0F, this.mc.player.getYRot()).normalize();
         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         if (this.mc.options.keyUp.isDown()) {
            x += dir.x;
            z += dir.z;
         }

         if (this.mc.options.keyDown.isDown()) {
            x += -dir.x;
            z += -dir.z;
         }

         if (this.mc.options.keyRight.isDown()) {
            x += -dir.z;
            z += dir.x;
         }

         if (this.mc.options.keyLeft.isDown()) {
            x += dir.z;
            z += -dir.x;
         }

         if (this.mc.options.keyJump.isDown()) {
            y += this.yWeight.get();
         }

         if (this.mc.options.keyShift.isDown()) {
            y -= this.yWeight.get();
         }

         Vec3 forward;
         if (x == 0.0 && y == 0.0 && z == 0.0) {
            forward = new Vec3(0.0, 0.0, 0.0);
         } else {
            forward = new Vec3(x, y, z).normalize().scale((Double)this.flySpeed.get());
         }

         BlockPos stepDownPos = this.mc.player.blockPosition().below();
         boolean sprinting = this.mc.options.keySprint.isDown()
            && !this.mc.options.keyJump.isDown()
            && this.speedBypassMode.get() != VecFly.SpeedBypassMode.None;
         if (sprinting != this.bypassLastTick && !this.mc.player.onGround() && !this.mc.level.getBlockState(stepDownPos).isSolidRender()) {
            this.mc.player.setPos(this.mc.player.getX(), stepDownPos.getY(), this.mc.player.getZ());
         }

         this.bypassLastTick = sprinting;
         this.mc.player.setDeltaMovement(forward);
         Vec3 tempLastForward = this.lastForward;
         this.lastForward = forward;
         if (this.speedBypassMode.get() != VecFly.SpeedBypassMode.None) {
            if (this.speedBypassMode.get() != VecFly.SpeedBypassMode.OnSprint || this.mc.options.keySprint.isDown()) {
               if (!forward.equals(Vec3.ZERO) || !tempLastForward.equals(Vec3.ZERO)) {
                  if (!this.mc.options.keyJump.isDown()) {
                     if (!this.mc.level.getBlockState(stepDownPos).isSolidRender()) {
                        forward = forward.scale((Double)this.speedBypassMultiplier.get());
                        if (++this.ticks >= (Integer)this.stepDownDelay.get()) {
                           this.mc.player.setPos(this.mc.player.getX(), stepDownPos.getY(), this.mc.player.getZ());
                           this.ticks = 0;
                        }
                     }

                     this.mc.player.setDeltaMovement(forward);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   private void onPacketSend(Send event) {
      if (event.packet instanceof ServerboundMovePlayerPacket packet && !packet.isOnGround()) {
         ((ServerboundMovePlayerPacketAccessor)packet).meteor$setOnGround(true);
      }
   }

   public static enum SpeedBypassMode {
      OnSprint,
      OnMove,
      None;
   }
}
