package nekiplay.meteorplus.utils.algoritms;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.utils.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ShadyRotation {
   private static float pitchDifference;
   public static float yawDifference;
   private static int ticks = -1;
   private static int tickCounter = 0;
   private static Runnable callback = null;
   public static boolean running = false;
   private static boolean client = false;

   private static double wrapAngleTo180(double angle) {
      return angle - Math.floor(angle / 360.0 + 0.5) * 360.0;
   }

   private static float wrapAngleTo180(float angle) {
      return (float)(angle - Math.floor(angle / 360.0F + 0.5) * 360.0);
   }

   public static ShadyRotation.Rotation getRotationToBlock(BlockPos block) {
      double diffX = block.getX() - MeteorClient.mc.player.position().x + 0.5;
      double diffY = block.getY() - MeteorClient.mc.player.position().y + 0.5 - MeteorClient.mc.player.getEyeY();
      double diffZ = block.getZ() - MeteorClient.mc.player.position().z + 0.5;
      double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float pitch = (float)(-Math.atan2(dist, diffY));
      float yaw = (float)Math.atan2(diffZ, diffX);
      pitch = (float)wrapAngleTo180((pitch * 180.0F / Math.PI + 90.0) * -1.0);
      yaw = (float)wrapAngleTo180(yaw * 180.0F / Math.PI - 90.0);
      return new ShadyRotation.Rotation(pitch, yaw);
   }

   public static ShadyRotation.Rotation getRotationToEntity(Entity entity) {
      double diffX = entity.position().x - MeteorClient.mc.player.position().x;
      double diffY = entity.position().y + entity.getEyePosition().y - MeteorClient.mc.player.position().y - MeteorClient.mc.player.getEyeY();
      double diffZ = entity.position().z - MeteorClient.mc.player.position().z;
      double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float pitch = (float)(-Math.atan2(dist, diffY));
      float yaw = (float)Math.atan2(diffZ, diffX);
      pitch = (float)wrapAngleTo180((pitch * 180.0F / Math.PI + 90.0) * -1.0);
      yaw = (float)wrapAngleTo180(yaw * 180.0F / Math.PI - 90.0);
      return new ShadyRotation.Rotation(pitch, yaw);
   }

   public static ShadyRotation.Rotation vec3ToRotation(Vec3 vec) {
      double diffX = vec.x - MeteorClient.mc.player.position().x;
      double diffY = vec.y - MeteorClient.mc.player.position().y - MeteorClient.mc.player.getEyeY();
      double diffZ = vec.z - MeteorClient.mc.player.position().z;
      double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float pitch = (float)(-Math.atan2(dist, diffY));
      float yaw = (float)Math.atan2(diffZ, diffX);
      pitch = (float)wrapAngleTo180((pitch * 180.0F / Math.PI + 90.0) * -1.0);
      yaw = (float)wrapAngleTo180(yaw * 180.0F / Math.PI - 90.0);
      return new ShadyRotation.Rotation(pitch, yaw);
   }

   public static void smoothLook(ShadyRotation.Rotation rotation, int ticks, boolean client, Runnable callback) {
      ShadyRotation.client = client;
      if (ticks == 0) {
         look(rotation, client);
         callback.run();
      } else {
         ShadyRotation.callback = callback;
         pitchDifference = wrapAngleTo180(rotation.pitch - MeteorClient.mc.player.getXRot());
         yawDifference = wrapAngleTo180(rotation.yaw - MeteorClient.mc.player.getYRot());
         ShadyRotation.ticks = ticks * 20;
         tickCounter = 0;
      }
   }

   public static void smoothLook(RotationUtils.Rotation rotation, int ticks, boolean client, Runnable callback) {
      ShadyRotation.client = client;
      if (ticks == 0) {
         look(new ShadyRotation.Rotation(rotation.getPitch(), rotation.getYaw()), client);
         callback.run();
      } else {
         ShadyRotation.callback = callback;
         if (client) {
            pitchDifference = wrapAngleTo180(rotation.getPitch() - MeteorClient.mc.player.getXRot());
            yawDifference = wrapAngleTo180(rotation.getYaw() - MeteorClient.mc.player.getYRot());
         } else {
            pitchDifference = wrapAngleTo180(rotation.getPitch() - Rotations.serverPitch);
            yawDifference = wrapAngleTo180(rotation.getYaw() - Rotations.serverYaw);
         }

         ShadyRotation.ticks = ticks * 20;
         tickCounter = 0;
      }
   }

   public static void smartLook(ShadyRotation.Rotation rotation, int ticksPer180, boolean client, Runnable callback) {
      ShadyRotation.client = client;
      float rotationDifference = Math.max(
         Math.abs(rotation.pitch - MeteorClient.mc.player.getXRot()), Math.abs(rotation.yaw - MeteorClient.mc.player.getYRot())
      );
      smoothLook(rotation, (int)(rotationDifference / 180.0F * ticksPer180), client, callback);
   }

   public static void look(ShadyRotation.Rotation rotation, boolean client) {
      Rotations.serverYaw = rotation.yaw;
      Rotations.serverPitch = rotation.pitch;
      if (client) {
         MeteorClient.mc.player.setXRot(rotation.pitch);
         MeteorClient.mc.player.setYRot(rotation.yaw);
      }
   }

   public void Init() {
      MeteorClient.EVENT_BUS.subscribe(this);
   }

   private void rotatorWorker() {
      if (MeteorClient.mc.player != null) {
         if (tickCounter < ticks) {
            running = true;
            if (client) {
               MeteorClient.mc.player.setXRot(MeteorClient.mc.player.getXRot() + pitchDifference / ticks);
               MeteorClient.mc.player.setYRot(MeteorClient.mc.player.getYRot() + yawDifference / ticks);
            } else {
               Rotations.setCamRotation(Rotations.serverYaw + yawDifference / ticks, Rotations.serverPitch + pitchDifference / ticks);
            }

            tickCounter++;
         } else {
            running = false;
         }
      }
   }

   @EventHandler
   public void onTick(Pre event) {
      this.rotatorWorker();
   }

   @EventHandler
   public void onTick2(Post event) {
      this.rotatorWorker();
   }

   public static class Rotation {
      public float pitch;
      public float yaw;

      public Rotation(float pitch, float yaw) {
         this.pitch = pitch;
         this.yaw = yaw;
      }
   }
}
