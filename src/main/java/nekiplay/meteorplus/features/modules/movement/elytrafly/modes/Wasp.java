package nekiplay.meteorplus.features.modules.movement.elytrafly.modes;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import nekiplay.meteorplus.features.modules.movement.elytrafly.ElytraFlyMode;
import nekiplay.meteorplus.features.modules.movement.elytrafly.ElytraFlyModes;

public class Wasp extends ElytraFlyMode {
   private boolean moving;
   private float yaw;
   private float pitch;

   public Wasp() {
      super(ElytraFlyModes.Wasp);
   }

   @Override
   public void onPlayerMove(PlayerMoveEvent event) {
      if (this.mc.player.isFallFlying()) {
         this.updateWaspMovement();
         this.pitch = this.mc.player.getXRot();
         double cos = Math.cos(Math.toRadians(this.yaw + 90.0F));
         double sin = Math.sin(Math.toRadians(this.yaw + 90.0F));
         double x = this.moving ? cos * (Double)this.elytraFly.horizontal_wasp.get() : 0.0;
         double y = -(Double)this.elytraFly.fallSpeed_wasp.get();
         double z = this.moving ? sin * (Double)this.elytraFly.horizontal_wasp.get() : 0.0;
         if ((Boolean)this.elytraFly.smartFall_wasp.get()) {
            y *= Math.abs(Math.sin(Math.toRadians(this.pitch)));
         }

         if (this.mc.options.keyShift.isDown() && !this.mc.options.keyJump.isDown()) {
            y = -(Double)this.elytraFly.down_wasp.get();
         }

         if (!this.mc.options.keyShift.isDown() && this.mc.options.keyJump.isDown()) {
            y = (Double)this.elytraFly.up_wasp.get();
         }

         ((IVec3)event.movement).meteor$set(x, y, z);
         if ((Boolean)this.elytraFly.resetSpeed.get()) {
            this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         }
      }
   }

   private void updateWaspMovement() {
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
