package nekiplay.meteorplus.utils;

import meteordevelopment.meteorclient.MeteorClient;

public class MovementUtils {
   public static double getSpeed() {
      return Math.sqrt(
         MeteorClient.mc.player.getDeltaMovement().x * MeteorClient.mc.player.getDeltaMovement().x
            + MeteorClient.mc.player.getDeltaMovement().z * MeteorClient.mc.player.getDeltaMovement().z
      );
   }

   public static void strafe(float speed) {
      strafe((double)speed);
   }

   public static void strafe(double speed) {
      double yaw = direction();
      double sin = -Math.sin(yaw) * speed;
      double cos = Math.cos(yaw) * speed;
      MeteorClient.mc.player.getDeltaMovement().add(cos, 0.0, sin);
   }

   public static double direction() {
      float yaw = MeteorClient.mc.player.getYRot();
      if (MeteorClient.mc.player.input.getMoveVector().x < 0.0F) {
         yaw += 180.0F;
      }

      float forward = 1.0F;
      if (MeteorClient.mc.player.input.getMoveVector().x < 0.0F) {
         forward = (float)(forward - 0.5);
      } else if (MeteorClient.mc.player.input.getMoveVector().x > 0.0F) {
         forward = (float)(forward + 0.5);
      }

      if (MeteorClient.mc.player.input.getMoveVector().y > 0.0F) {
         yaw -= 90.0F * forward;
      }

      if (MeteorClient.mc.player.input.getMoveVector().y < 0.0F) {
         yaw += 90.0F * forward;
      }

      return Math.toRadians(yaw);
   }
}
