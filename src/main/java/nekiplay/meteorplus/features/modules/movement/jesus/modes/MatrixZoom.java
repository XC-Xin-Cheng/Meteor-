package nekiplay.meteorplus.features.modules.movement.jesus.modes;

import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import nekiplay.meteorplus.features.modules.movement.jesus.JesusMode;
import nekiplay.meteorplus.features.modules.movement.jesus.JesusModes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class MatrixZoom extends JesusMode {
   private final float range = 0.005F;

   public MatrixZoom() {
      super(JesusModes.Matrix_Zoom);
   }

   @Override
   public void onTickEventPre(Pre event) {
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

      if (this.mc
               .level
               .getBlockState(new BlockPos(this.mc.player.getBlockX(), (int)(this.mc.player.getBlockY() + 0.005F), this.mc.player.getBlockZ()))
               .getBlock()
            == Blocks.WATER
         && !this.mc.player.horizontalCollision) {
         ((IVec3)this.mc.player.getDeltaMovement()).meteor$set(velX, 0.0, velZ);
      }
   }
}
