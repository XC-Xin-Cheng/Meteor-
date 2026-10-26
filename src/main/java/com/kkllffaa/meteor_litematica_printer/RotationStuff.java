package com.kkllffaa.meteor_litematica_printer;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RotationStuff {
   public static final double DEG_TO_RAD = Math.PI / 180.0;
   public static final float DEG_TO_RAD_F = (float) (Math.PI / 180.0);
   public static final double RAD_TO_DEG = 180.0 / Math.PI;
   public static final float RAD_TO_DEG_F = (float) (180.0 / Math.PI);

   public static Rotation calcRotationFromVec3d(Vec3 orig, Vec3 dest, Rotation current) {
      return wrapAnglesToRelative(current, calcRotationFromVec3d(orig, dest));
   }

   private static Rotation calcRotationFromVec3d(Vec3 orig, Vec3 dest) {
      double[] delta = new double[]{orig.x - dest.x, orig.y - dest.y, orig.z - dest.z};
      double yaw = Mth.atan2(delta[0], -delta[2]);
      double dist = Math.sqrt(delta[0] * delta[0] + delta[2] * delta[2]);
      double pitch = Mth.atan2(delta[1], dist);
      return new Rotation((float)(yaw * (180.0 / Math.PI)), (float)(pitch * (180.0 / Math.PI)));
   }

   public static Rotation wrapAnglesToRelative(Rotation current, Rotation target) {
      return current.yawIsReallyClose(target) ? new Rotation(current.getYaw(), target.getPitch()) : target.subtract(current).normalize().add(current);
   }

   public static Vec3 calcLookDirectionFromRotation(Rotation rotation) {
      float flatZ = Mth.cos(-rotation.getYaw() * (float) (Math.PI / 180.0) - (float) Math.PI);
      float flatX = Mth.sin(-rotation.getYaw() * (float) (Math.PI / 180.0) - (float) Math.PI);
      float pitchBase = -Mth.cos(-rotation.getPitch() * (float) (Math.PI / 180.0));
      float pitchHeight = Mth.sin(-rotation.getPitch() * (float) (Math.PI / 180.0));
      return new Vec3(flatX * pitchBase, pitchHeight, flatZ * pitchBase);
   }

   public static HitResult rayTraceTowards(LocalPlayer entity, Rotation rotation, double blockReachDistance) {
      Vec3 start = entity.getEyePosition(1.0F);
      Vec3 direction = calcLookDirectionFromRotation(rotation);
      Vec3 end = start.add(direction.x * blockReachDistance, direction.y * blockReachDistance, direction.z * blockReachDistance);
      return entity.level().clip(new ClipContext(start, end, Block.OUTLINE, Fluid.NONE, entity));
   }
}
