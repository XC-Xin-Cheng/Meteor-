package nekiplay.meteorplus.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class RotationUtils {
   public static Vec3 getEyesPos() {
      LocalPlayer player = MeteorClient.mc.player;
      return new Vec3(player.getX(), player.getY() + player.getEyeHeight(player.getPose()), player.getZ());
   }

   public static float getAngleDifference(float a, float b) {
      return ((a - b) % 360.0F + 540.0F) % 360.0F - 180.0F;
   }

   public static double getRotationDifference(RotationUtils.Rotation a, RotationUtils.Rotation b) {
      return Math.hypot(getAngleDifference(a.yaw, b.yaw), a.getPitch() - b.getPitch());
   }

   public static RotationUtils.Rotation toRotation(Vec3 vec, boolean predict) {
      Vec3 eyesPos = new Vec3(
         MeteorClient.mc.player.getX(),
         MeteorClient.mc.player.getBoundingBox().minY + MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()),
         MeteorClient.mc.player.getZ()
      );
      if (predict) {
         if (MeteorClient.mc.player.onGround()) {
            eyesPos.add(MeteorClient.mc.player.getDeltaMovement().x, 0.0, MeteorClient.mc.player.getDeltaMovement().z);
         } else {
            eyesPos.add(MeteorClient.mc.player.getDeltaMovement().x, MeteorClient.mc.player.getDeltaMovement().y, MeteorClient.mc.player.getDeltaMovement().z);
         }
      }

      double diffX = vec.x - eyesPos.x;
      double diffY = vec.y - eyesPos.y;
      double diffZ = vec.z - eyesPos.z;
      return new RotationUtils.Rotation(
         (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F, (float)(-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))
      );
   }

   public static Vec3 getCenter(AABB bb) {
      return new Vec3(bb.minX + (bb.maxX - bb.minX) * 0.5, bb.minY + (bb.maxY - bb.minY) * 0.5, bb.minZ + (bb.maxZ - bb.minZ) * 0.5);
   }

   public static RotationUtils.Rotation limitAngleChange(RotationUtils.Rotation currentRotation, RotationUtils.Rotation targetRotation, float turnSpeed) {
      float yawDifference = getAngleDifference(targetRotation.getYaw(), currentRotation.getYaw());
      float pitchDifference = getAngleDifference(targetRotation.getPitch(), currentRotation.getPitch());
      return new RotationUtils.Rotation(
         currentRotation.getYaw() + (yawDifference > turnSpeed ? turnSpeed : Math.max(yawDifference, -turnSpeed)),
         currentRotation.getPitch() + (pitchDifference > turnSpeed ? turnSpeed : Math.max(pitchDifference, -turnSpeed))
      );
   }

   public static RotationUtils.Rotation getNeededRotations(Vec3 vec) {
      Vec3 eyesPos = getEyesPos();
      double diffX = vec.x - eyesPos.x;
      double diffY = vec.y - eyesPos.y;
      double diffZ = vec.z - eyesPos.z;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      return new RotationUtils.Rotation(yaw, pitch);
   }

   public static double getAngleToLookVec(Vec3 vec) {
      RotationUtils.Rotation needed = getNeededRotations(vec);
      LocalPlayer player = MeteorClient.mc.player;
      float currentYaw = Mth.wrapDegrees(player.getYRot());
      float currentPitch = Mth.wrapDegrees(player.getXRot());
      float diffYaw = currentYaw - needed.yaw;
      float diffPitch = currentPitch - needed.pitch;
      return Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
   }

   public static Vector3d getVector3d(LivingEntity me, LivingEntity to, float range) {
      double wHalf = to.getBbWidth() / 2.0F;
      double yExpand = org.joml.Math.clamp(me.getEyeHeight(me.getPose()) - to.getY(), 0.0, to.getBbHeight() * (MeteorClient.mc.player.distanceTo(to) / range));
      double xExpand = org.joml.Math.clamp(MeteorClient.mc.player.position().x - to.position().x, -wHalf, wHalf);
      double zExpand = org.joml.Math.clamp(MeteorClient.mc.player.position().z - to.position().z, -wHalf, wHalf);
      return new Vector3d(to.getX() - me.getX() + xExpand, to.getY() - me.getEyeHeight(me.getPose()) + yExpand, to.getZ() - me.getZ() + zExpand);
   }

   public static double getDistance(LivingEntity entity, float range) {
      return getVector3d(MeteorClient.mc.player, entity, range).length();
   }

   public static double getDegree(LivingEntity entity, float range) {
      Vector3d vec = getVector3d(MeteorClient.mc.player, entity, range);
      double yaw = Mth.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
      double delta = Mth.wrapDegrees(yaw - MeteorClient.mc.player.getYRot());
      if (Math.abs(delta) > 180.0) {
         delta -= Math.signum(delta) * 360.0;
      }

      return Math.abs(delta);
   }

   public static boolean isLookingAtMe(LivingEntity target) {
      double x = target.getX() - MeteorClient.mc.player.getX();
      double z = target.getZ() - MeteorClient.mc.player.getZ();
      float entityYaw = Mth.wrapDegrees(target.getYRot());
      double yaw = Math.toDegrees(Math.atan2(z, x)) + 90.0;
      return Math.abs(Mth.wrapDegrees(yaw - entityYaw)) <= 90.0;
   }

   public static boolean isInHitBox(LivingEntity me, LivingEntity to) {
      double wHalf = to.getBbWidth() / 2.0F;
      double yExpand = org.joml.Math.clamp(me.getEyeHeight(me.getPose()) - to.getY(), 0.0, to.getBbHeight());
      double xExpand = org.joml.Math.clamp(MeteorClient.mc.player.getX() - to.getX(), -wHalf, wHalf);
      double zExpand = org.joml.Math.clamp(MeteorClient.mc.player.getZ() - to.getZ(), -wHalf, wHalf);
      return new Vector3d(to.getX() - me.getX() + xExpand, to.getY() - me.getEyeHeight(me.getPose()) + yExpand, to.getZ() - me.getZ() + zExpand).length()
         == 0.0;
   }

   public static final class Rotation {
      private float yaw;
      private float pitch;

      public Rotation(float yaw, float pitch) {
         this.yaw = Mth.wrapDegrees(yaw);
         this.pitch = Mth.wrapDegrees(pitch);
      }

      public Rotation(double yaw, double pitch) {
         this.yaw = Mth.wrapDegrees((float)yaw);
         this.pitch = Mth.wrapDegrees((float)pitch);
      }

      public float getYaw() {
         return this.yaw;
      }

      public float getPitch() {
         return this.pitch;
      }

      public void setYaw(float yaw) {
         this.yaw = yaw;
      }

      public void setPitch(float pitch) {
         this.pitch = pitch;
      }
   }
}
