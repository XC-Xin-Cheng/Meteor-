package nekiplay.meteorplus.utils;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RaycastUtils {
   public static EntityHitResult raycastEntity(double range, float yaw, float pitch, double boxexpand) {
      Entity camera = MeteorClient.mc.getCameraEntity();
      Vec3 cameraVec = MeteorClient.mc.player.getEyePosition();
      float yawCos = Mth.cos(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
      float yawSin = Mth.sin(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
      float pitchCos = -Mth.cos(-pitch * (float) (Math.PI / 180.0));
      float pitchSin = Mth.sin(-pitch * (float) (Math.PI / 180.0));
      Vec3 rotation = new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
      Vec3 vec3d3 = cameraVec.add(rotation.x * range, rotation.y * range, rotation.z * range);
      AABB box = camera.getBoundingBox().expandTowards(rotation.scale(range)).inflate(boxexpand, boxexpand, boxexpand);
      return ProjectileUtil.getEntityHitResult(camera, cameraVec, vec3d3, box, new Predicate<Entity>() {
         public boolean test(Entity entity) {
            return !entity.isSpectator() && entity.canBeCollidedWith(MeteorClient.mc.player);
         }
      }, 0.0);
   }

   public static Vec3 getRotationVector(float pitch, float yaw) {
      float f = pitch * (float) (Math.PI / 180.0);
      float g = -yaw * (float) (Math.PI / 180.0);
      float h = Mth.cos(g);
      float i = Mth.sin(g);
      float j = Mth.cos(f);
      float k = Mth.sin(f);
      return new Vec3(i * j, -k, h * j);
   }

   public static HitResult raycast(Vec3 camera, Vec3 rotation, double maxDistance, float tickDelta, boolean includeFluids) {
      Vec3 vec3d3 = camera.add(rotation.x * maxDistance, rotation.y * maxDistance, rotation.z * maxDistance);
      return MeteorClient.mc.level.clip(new ClipContext(camera, vec3d3, Block.OUTLINE, includeFluids ? Fluid.ANY : Fluid.NONE, MeteorClient.mc.player));
   }

   public static BlockHitResult bucketRaycast(Vec3 camera, float pitch, float yaw, Fluid fluidHandling) {
      float h = Mth.cos(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
      float i = Mth.sin(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
      float j = -Mth.cos(-pitch * (float) (Math.PI / 180.0));
      float k = Mth.sin(-pitch * (float) (Math.PI / 180.0));
      float l = i * j;
      float n = h * j;
      double d = 5.0;
      Vec3 vec3d2 = camera.add(l * 5.0, k * 5.0, n * 5.0);
      return MeteorClient.mc.level.clip(new ClipContext(camera, vec3d2, Block.OUTLINE, fluidHandling, MeteorClient.mc.player));
   }
}
