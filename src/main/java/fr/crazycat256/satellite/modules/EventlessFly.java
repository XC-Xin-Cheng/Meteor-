package fr.crazycat256.satellite.modules;

import fr.crazycat256.satellite.Addon;
import fr.crazycat256.satellite.utils.ServerUtils;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.world.phys.Vec3;

public class EventlessFly extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgAntiKick = this.settings.createGroup("Anti Kick");
   private final Setting<Double> speed = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Speed")).description("Your speed when flying.")).defaultValue(0.062).min(0.0).sliderMax(1.0).build());
   private final Setting<Boolean> cancelRotations = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("cancel-rotations"))
                  .description("Cancels rotations when flying."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> resetRotationOnDisable = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("reset-rotation-on-disable"))
                     .description("Resets your rotation when you disable eventless-fly."))
                  .defaultValue(false))
               .visible(this.cancelRotations::get))
            .build()
      );
   private final Setting<Boolean> antiKick = this.sgAntiKick
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("anti-kick"))
                  .description("Tries to prevent you from getting kicked for flying."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Integer> delay = this.sgAntiKick
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("delay"))
                     .description("The amount of delay, in ticks, between flying down a bit and return to original position."))
                  .defaultValue(20))
               .min(1)
               .sliderMax(200)
               .visible(this.antiKick::get))
            .build()
      );
   private final Setting<Integer> offTime = this.sgAntiKick
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("off-time"))
                     .description("The amount of delay, in milliseconds, to fly down a bit to reset floating ticks."))
                  .defaultValue(3))
               .min(1)
               .sliderRange(1, 20)
               .visible(this.antiKick::get))
            .build()
      );
   private final Setting<Boolean> flyUp = this.sgAntiKick
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("fly-up"))
                     .description("Fly up a bit after flying down."))
                  .defaultValue(true))
               .visible(this.antiKick::get))
            .build()
      );
   public float cameraYaw;
   public float cameraPitch;
   private float startPitch;
   private float startYaw;
   private int ticks = 0;
   private boolean shouldFlyDown = false;
   private boolean shouldFlyUp = false;

   public EventlessFly() {
      super(Addon.CATEGORY, "eventless-fly", "Allows you to fly without triggering events.");
   }

   public boolean noRotate() {
      return this.isActive() && (Boolean)this.cancelRotations.get();
   }

   public void onActivate() {
      this.startYaw = this.mc.player.getYRot();
      this.startPitch = this.mc.player.getXRot();
      this.cameraYaw = this.mc.player.getYRot();
      this.cameraPitch = this.mc.player.getXRot();
   }

   public void onDeactivate() {
      if ((Boolean)this.cancelRotations.get() && (Boolean)this.resetRotationOnDisable.get()) {
         this.mc.player.setYRot(this.startYaw);
         this.mc.player.setXRot(this.startPitch);
      } else if (!(Boolean)this.cancelRotations.get()) {
         this.mc.player.setYRot(this.cameraYaw);
         this.mc.player.setXRot(this.cameraPitch);
         this.mc.player.connection.send(new Rot(this.cameraYaw, this.cameraPitch, this.mc.player.onGround(), this.mc.player.horizontalCollision));
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null && this.mc.level != null) {
         this.ticks++;
         if ((Boolean)this.cancelRotations.get()) {
            Rotations.rotate(this.startYaw, this.startPitch);
            this.mc.player.setYRot(this.cameraYaw);
            this.mc.player.setXRot(this.cameraPitch);
         }

         if (!ServerUtils.noBlocksAround(this.mc.player)) {
            this.ticks = 0;
            this.shouldFlyDown = false;
            this.shouldFlyUp = false;
         }

         if (this.ticks >= (Integer)this.delay.get()) {
            this.shouldFlyDown = true;
         }

         if (this.ticks > (Integer)this.delay.get() + (Integer)this.offTime.get()) {
            this.ticks = 0;
            this.shouldFlyDown = false;
            this.shouldFlyUp = (Boolean)this.flyUp.get();
         }

         if (this.ticks >= (Integer)this.offTime.get() + 1) {
            this.shouldFlyUp = false;
         }

         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         if (this.shouldFlyDown && (Boolean)this.antiKick.get()) {
            y = -1.0;
         } else if (this.shouldFlyUp && (Boolean)this.antiKick.get() && (Boolean)this.flyUp.get()) {
            y = 1.0;
         } else {
            Vec3 dir = Vec3.directionFromRotation(0.0F, this.mc.player.getYRot()).normalize();
            if (this.mc.options.keyUp.isDown()) {
               x += dir.x;
               z += dir.z;
            }

            if (this.mc.options.keyDown.isDown()) {
               x -= dir.x;
               z -= dir.z;
            }

            if (this.mc.options.keyRight.isDown()) {
               x -= dir.z;
               z += dir.x;
            }

            if (this.mc.options.keyLeft.isDown()) {
               x += dir.z;
               z -= dir.x;
            }

            if (this.mc.options.keyJump.isDown()) {
               y++;
            }

            if (this.mc.options.keyShift.isDown()) {
               y--;
               this.mc.player.setShiftKeyDown(false);
               this.ticks = 0;
            }

            if (this.mc.options.keySprint.isDown()) {
               this.mc.player.setSprinting(false);
            }
         }

         Vec3 velocity = new Vec3(x, y, z);
         if (velocity.equals(Vec3.ZERO)) {
            this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         } else {
            velocity = velocity.normalize().scale((Double)this.speed.get());
            this.mc.player.setDeltaMovement(velocity);
            Vec3 endPos = this.mc.player.position().add(velocity);
            this.mc.player.connection.send(new Pos(endPos.x, endPos.y, endPos.z, this.mc.player.onGround(), this.mc.player.horizontalCollision));
            this.mc.player.connection.send(new Pos(endPos.x, -1000000.0, endPos.z, false, this.mc.player.horizontalCollision));
         }
      }
   }
}
