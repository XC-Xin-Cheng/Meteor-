package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.RejectsUtils;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;

public class AimAssist extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgSpeed = this.settings.createGroup("Aim Speed");
   private final Setting<Set<EntityType<?>>> entities = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("entities")).description("Entities to aim at.")).defaultValue(new EntityType[]{EntityType.PLAYER}).build());
   private final Setting<Double> range = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("range"))
               .description("The range at which an entity can be targeted."))
            .defaultValue(5.0)
            .min(0.0)
            .build()
      );
   private final Setting<Double> fov = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("fov"))
               .description("Will only aim entities in the fov."))
            .defaultValue(360.0)
            .min(0.0)
            .max(360.0)
            .build()
      );
   private final Setting<Boolean> ignoreWalls = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-walls"))
                  .description("Whether or not to ignore aiming through walls."))
               .defaultValue(false))
            .build()
      );
   private final Setting<SortPriority> priority = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("priority"))
                  .description("How to filter targets within range."))
               .defaultValue(SortPriority.LowestHealth))
            .build()
      );
   private final Setting<Target> bodyTarget = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("aim-target"))
                  .description("Which part of the entities body to aim at."))
               .defaultValue(Target.Body))
            .build()
      );
   private final Setting<Boolean> instant = this.sgSpeed
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("instant-look"))
                  .description("Instantly looks at the entity."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Double> speed = this.sgSpeed
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("speed"))
                  .description("How fast to aim at the entity."))
               .defaultValue(5.0)
               .min(0.0)
               .visible(() -> !(Boolean)this.instant.get()))
            .build()
      );
   private final Vector3d vec3d1 = new Vector3d();
   private Entity target;

   public AimAssist() {
      super(MeteorRejectsAddon.CATEGORY, "aim-assist", "Automatically aims at entities.");
   }

   @EventHandler
   private void onTick(Post event) {
      this.target = TargetUtils.get(entity -> {
         if (!entity.isAlive()) {
            return false;
         } else if (!PlayerUtils.isWithin(entity, (Double)this.range.get())) {
            return false;
         } else if (!(Boolean)this.ignoreWalls.get() && !PlayerUtils.canSeeEntity(entity)) {
            return false;
         } else if (entity == this.mc.player || !((Set)this.entities.get()).contains(entity.getType())) {
            return false;
         } else {
            return entity instanceof Player && !Friends.get().shouldAttack((Player)entity) ? false : RejectsUtils.inFov(entity, (Double)this.fov.get());
         }
      }, (SortPriority)this.priority.get());
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.target != null) {
         this.aim(this.target, event.tickDelta, (Boolean)this.instant.get());
      }
   }

   private void aim(Entity target, double delta, boolean instant) {
      Utils.set(this.vec3d1, target, delta);
      switch ((Target)this.bodyTarget.get()) {
         case Head:
            this.vec3d1.add(0.0, target.getEyeHeight(target.getPose()), 0.0);
            break;
         case Body:
            this.vec3d1.add(0.0, target.getEyeHeight(target.getPose()) / 2.0F, 0.0);
      }

      double deltaX = this.vec3d1.x - this.mc.player.getX();
      double deltaZ = this.vec3d1.z - this.mc.player.getZ();
      double deltaY = this.vec3d1.y - (this.mc.player.getY() + this.mc.player.getEyeHeight(this.mc.player.getPose()));
      double angle = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0;
      if (instant) {
         this.mc.player.setYRot((float)angle);
      } else {
         double deltaAngle = Mth.wrapDegrees(angle - this.mc.player.getYRot());
         double toRotate = (Double)this.speed.get() * (deltaAngle >= 0.0 ? 1 : -1) * delta;
         if (toRotate >= 0.0 && toRotate > deltaAngle || toRotate < 0.0 && toRotate < deltaAngle) {
            toRotate = deltaAngle;
         }

         this.mc.player.setYRot(this.mc.player.getYRot() + (float)toRotate);
      }

      double idk = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      angle = -Math.toDegrees(Math.atan2(deltaY, idk));
      if (instant) {
         this.mc.player.setXRot((float)angle);
      } else {
         double deltaAngle = Mth.wrapDegrees(angle - this.mc.player.getXRot());
         double toRotate = (Double)this.speed.get() * (deltaAngle >= 0.0 ? 1 : -1) * delta;
         if (toRotate >= 0.0 && toRotate > deltaAngle || toRotate < 0.0 && toRotate < deltaAngle) {
            toRotate = deltaAngle;
         }

         this.mc.player.setXRot(this.mc.player.getXRot() + (float)toRotate);
      }
   }

   public String getInfoString() {
      return EntityUtils.getName(this.target);
   }
}
