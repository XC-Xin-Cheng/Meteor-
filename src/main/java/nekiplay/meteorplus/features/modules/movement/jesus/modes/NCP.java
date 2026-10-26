package nekiplay.meteorplus.features.modules.movement.jesus.modes;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import nekiplay.meteorplus.features.modules.movement.jesus.JesusMode;
import nekiplay.meteorplus.features.modules.movement.jesus.JesusModes;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.phys.Vec3;

public class NCP extends JesusMode {
   float newSpeed = 0.0F;

   public NCP() {
      super(JesusModes.NCP);
   }

   @Override
   public void onPlayerMoveEvent(PlayerMoveEvent event) {
      this.mc.player.setSprinting(false);
      if (this.mc.player.isInLiquid()) {
         Vec3 velocity = this.mc.player.getDeltaMovement();
         if (this.mc.options.keyJump.isDown()
            && !this.mc.player.isShiftKeyDown()
            && !(this.mc.level.getBlockState(this.mc.player.blockPosition().offset(0, 1, 0)).getBlock() instanceof AirBlock)) {
            this.mc.player.setDeltaMovement(velocity.x, 0.12, velocity.z);
         }

         velocity = this.mc.player.getDeltaMovement();
         if (this.mc.options.keyShift.isDown()) {
            this.mc.player.setDeltaMovement(velocity.x, -0.12, velocity.z);
         }

         velocity = this.mc.player.getDeltaMovement();
         if (this.mc.level.getBlockState(this.mc.player.blockPosition().offset(0, 1, 0)).getBlock() instanceof AirBlock && this.mc.options.keyJump.isDown()) {
            this.mc.player.setShiftKeyDown(true);
            this.mc.player.setDeltaMovement(velocity.x, 0.12, velocity.z);
         }

         float yaw = this.mc.player.getYRot();
         Vec3 forward = Vec3.directionFromRotation(0.0F, yaw);
         Vec3 right = Vec3.directionFromRotation(0.0F, yaw + 90.0F);
         double velX = 0.0;
         double velZ = 0.0;
         double s = 0.5;
         double speedValue = (Double)this.settings.speed.get();
         if (this.mc.options.keyUp.isDown()) {
            velX += forward.x * s * speedValue;
            velZ += forward.z * s * speedValue;
         }

         if (this.mc.options.keyDown.isDown()) {
            velX -= forward.x * s * speedValue;
            velZ -= forward.z * s * speedValue;
         }

         if (this.mc.options.keyRight.isDown()) {
            velX += right.x * s * speedValue;
            velZ += right.z * s * speedValue;
         }

         if (this.mc.options.keyLeft.isDown()) {
            velX -= right.x * s * speedValue;
            velZ -= right.z * s * speedValue;
         }

         if (velX >= ((Double)this.settings.limit_speed.get()).floatValue()) {
            velX = ((Double)this.settings.limit_speed.get()).floatValue();
         }

         if (velZ >= ((Double)this.settings.limit_speed.get()).floatValue()) {
            velZ = ((Double)this.settings.limit_speed.get()).floatValue();
         }

         ((IVec3)this.mc.player.getDeltaMovement()).meteor$set(velX, 0.0, velZ);
         this.mc.player.setShiftKeyDown(true);
      }
   }

   @Override
   public void onDeactivate() {
      this.newSpeed = 0.6F;
      super.onDeactivate();
   }

   @Override
   public void onActivate() {
      this.newSpeed = 0.6F;
      this.newSpeed = this.newSpeed + ((Double)this.settings.speed.get()).floatValue();
      super.onActivate();
   }

   public void setMotion(double motion) {
      float forward = this.mc.player.zza;
      float yaw = this.mc.player.getYRot();
      if (forward == 0.0F) {
         ((IVec3)this.mc.player.getDeltaMovement()).meteor$set(0.0, this.mc.player.getDeltaMovement().y, 0.0);
      } else {
         double x = forward * motion * Math.cos(Math.toRadians(yaw + 90.0F)) * motion * Math.sin(Math.toRadians(yaw + 90.0F));
         double z = forward * motion * Math.sin(Math.toRadians(yaw + 90.0F)) * motion * Math.cos(Math.toRadians(yaw + 90.0F));
         ((IVec3)this.mc.player.getDeltaMovement()).meteor$set(x, this.mc.player.getDeltaMovement().y, z);
      }
   }
}
