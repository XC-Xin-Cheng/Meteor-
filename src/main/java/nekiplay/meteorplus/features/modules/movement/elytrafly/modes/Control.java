package nekiplay.meteorplus.features.modules.movement.elytrafly.modes;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import nekiplay.meteorplus.features.modules.movement.elytrafly.ElytraFlyMode;
import nekiplay.meteorplus.features.modules.movement.elytrafly.ElytraFlyModes;

public class Control extends ElytraFlyMode {
   private boolean moving;
   private float yaw;
   private float pitch;
   private float p;
   private double velocity;

   public Control() {
      super(ElytraFlyModes.Control);
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event) {
      if (this.mc.player.isFallFlying()) {
         this.updateControlMovement();
         this.pitch = 0.0F;
         boolean movingUp = false;
         if (!this.mc.options.keyShift.isDown() && this.mc.options.keyJump.isDown() && this.velocity > (Double)this.elytraFly.speed_control.get() * 0.4) {
            this.p = (float)Math.min(this.p + 0.1 * (1.0F - this.p) * (1.0F - this.p) * (1.0F - this.p), 1.0);
            this.pitch = Math.max(Math.max(this.p, 0.0F) * -90.0F, -90.0F);
            movingUp = true;
            this.moving = false;
         } else {
            this.velocity = (Double)this.elytraFly.speed_control.get();
            this.p = -0.2F;
         }

         this.velocity = this.moving
            ? (Double)this.elytraFly.speed_control.get()
            : Math.min(this.velocity + Math.sin(Math.toRadians(this.pitch)) * 0.08, (Double)this.elytraFly.speed_control.get());
         double cos = Math.cos(Math.toRadians(this.yaw + 90.0F));
         double sin = Math.sin(Math.toRadians(this.yaw + 90.0F));
         double x = this.moving && !movingUp
            ? cos * (Double)this.elytraFly.speed_control.get()
            : (movingUp ? this.velocity * Math.cos(Math.toRadians(this.pitch)) * cos : 0.0);
         double y = this.pitch < 0.0F
            ? this.velocity * (Double)this.elytraFly.upMultiplier_control.get() * -Math.sin(Math.toRadians(this.pitch)) * this.velocity
            : -(Double)this.elytraFly.fallSpeed_control.get();
         double z = this.moving && !movingUp
            ? sin * (Double)this.elytraFly.speed_control.get()
            : (movingUp ? this.velocity * Math.cos(Math.toRadians(this.pitch)) * sin : 0.0);
         y *= Math.abs(Math.sin(Math.toRadians(movingUp ? this.pitch : this.mc.player.getXRot())));
         if (this.mc.options.keyShift.isDown() && !this.mc.options.keyJump.isDown()) {
            y = -(Double)this.elytraFly.downSpeed_control.get();
         }

         ((IVec3)event.movement).meteor$set(x, y, z);
         if ((Boolean)this.elytraFly.resetSpeed.get()) {
            this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         }
      }
   }

   private void updateControlMovement() {
      float yaw = this.mc.player.getYRot();
      float forward = this.mc.player.input.getMoveVector().y;
      float sideways = this.mc.player.input.getMoveVector().x;
      if (forward > 0.0F) {
         this.moving = true;
         yaw += sideways > 0.0F ? -45.0F : (sideways < 0.0F ? 45.0F : 0.0F);
      } else if (forward < 0.0F) {
         this.moving = true;
         yaw += sideways > 0.0F ? -135.0F : (sideways < 0.0F ? 135.0F : 180.0F);
      } else {
         this.moving = sideways != 0.0F;
         yaw += sideways > 0.0F ? -90.0F : (sideways < 0.0F ? 90.0F : 0.0F);
      }

      this.yaw = yaw;
   }
}
