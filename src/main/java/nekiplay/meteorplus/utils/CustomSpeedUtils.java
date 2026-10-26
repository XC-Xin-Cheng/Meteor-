package nekiplay.meteorplus.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Anchor;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public class CustomSpeedUtils {
   public static void applySpeed(PlayerMoveEvent event, double speed) {
      Vec3 vel = PlayerUtils.getHorizontalVelocity(speed);
      double velX = vel.x();
      double velZ = vel.z();
      if (MeteorClient.mc.player.hasEffect(MobEffects.SPEED)) {
         double value = (MeteorClient.mc.player.getEffect(MobEffects.SPEED).getAmplifier() + 1) * 0.205;
         velX += velX * value;
         velZ += velZ * value;
      }

      Anchor anchor = (Anchor)Modules.get().get(Anchor.class);
      if (anchor.isActive() && anchor.controlMovement) {
         velX = anchor.deltaX;
         velZ = anchor.deltaZ;
      }

      ((IVec3)event.movement).meteor$set(velX, event.movement.y, velZ);
   }
}
