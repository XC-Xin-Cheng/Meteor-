package nekiplay.meteorplus.features.modules.combat.killaura.modes;

import java.util.ArrayList;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura.ShieldMode;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import nekiplay.meteorplus.features.modules.combat.criticals.CriticalsPlus;
import nekiplay.meteorplus.features.modules.combat.killaura.KillAuraPlusMode;
import nekiplay.meteorplus.features.modules.combat.killaura.KillAuraPlusModes;
import nekiplay.meteorplus.utils.GameSensitivityUtils;
import nekiplay.meteorplus.utils.RaycastUtils;
import nekiplay.meteorplus.utils.math.StopWatch;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class Matrix extends KillAuraPlusMode {
   private final ArrayList<Entity> targets = new ArrayList<>();
   private final StopWatch stopWatch = new StopWatch();
   private UVPair rotateVector = new UVPair(0.0F, 0.0F);
   private LivingEntity target;
   private Entity selected;
   float lastYaw;
   float lastPitch;
   int ticks = 0;
   boolean isRotated;

   public Matrix() {
      super(KillAuraPlusModes.Matrix);
   }

   @Override
   public void onTickPre(Pre event) {
      if (this.target == null || !this.entityCheck(this.target)) {
         TargetUtils.getList(this.targets, this::entityCheck, (SortPriority)this.settings.priority.get(), 15);
         if (!this.targets.isEmpty() && this.targets.get(0) instanceof LivingEntity livingEntity) {
            this.target = livingEntity;
         }
      }

      if (this.target != null && this.target.isAlive()) {
         this.isRotated = false;
         EntityHitResult result = RaycastUtils.raycastEntity((Double)this.settings.range.get(), this.rotateVector.u(), this.rotateVector.v(), 0.0);
         if (result != null) {
            ChatUtils.info(result.getType().name(), new Object[0]);
         }

         if ((!(Boolean)this.settings.onlyCrits.get() || CriticalsPlus.allowCrit() || !CriticalsPlus.needCrit(this.target))
            && this.delayCheck()
            && result != null
            && result.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
            this.attack(this.target);
            this.ticks = 2;
         }

         if (this.settings.rotationType.get() == Matrix.Type.Fast) {
            if (this.ticks > 0) {
               this.updateRotation(true, 180.0F, 90.0F);
               Rotations.rotate(this.rotateVector.u(), this.rotateVector.v());
               this.ticks--;
            } else {
               this.reset();
            }
         } else if (!this.isRotated) {
            this.updateRotation(false, 80.0F, 35.0F);
            Rotations.rotate(this.rotateVector.u(), this.rotateVector.v());
         }
      } else {
         this.reset();
      }
   }

   private boolean delayCheck() {
      return this.mc.player.getAttackStrengthScale(0.5F) >= 1.0F;
   }

   private void attack(Entity target) {
      this.mc.gameMode.attack(this.mc.player, target);
      this.mc.player.swing(InteractionHand.MAIN_HAND);
   }

   private boolean entityCheck(Entity entity) {
      if (!entity.equals(this.mc.player) && !entity.equals(this.mc.getCameraEntity())) {
         if (!(entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) && entity.isAlive()) {
            AABB hitbox = entity.getBoundingBox();
            if (!PlayerUtils.isWithin(
               Mth.clamp(this.mc.player.getX(), hitbox.minX, hitbox.maxX),
               Mth.clamp(this.mc.player.getY(), hitbox.minY, hitbox.maxY),
               Mth.clamp(this.mc.player.getZ(), hitbox.minZ, hitbox.maxZ),
               (Double)this.settings.range.get()
            )) {
               return false;
            } else if (!((Set)this.settings.entities.get()).contains(entity.getType())) {
               return false;
            } else if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, (Double)this.settings.wallsRange.get())) {
               return false;
            } else if ((Boolean)this.settings.ignoreTamed.get()
               && entity instanceof OwnableEntity tameable
               && tameable.getOwner().getUUID() != null
               && tameable.getOwner().getUUID().equals(this.mc.player.getUUID())) {
               return false;
            } else {
               if ((Boolean)this.settings.ignorePassive.get()) {
                  if (entity instanceof EnderMan enderman && !enderman.isCreepy()) {
                     return false;
                  }

                  if (entity instanceof ZombifiedPiglin piglin && !piglin.isAggressive()) {
                     return false;
                  }

                  if (entity instanceof Wolf wolf && !wolf.isAggressive()) {
                     return false;
                  }
               }

               if (!(entity instanceof Player player)) {
                  return true;
               } else if (player.isCreative()) {
                  return false;
               } else {
                  return !Friends.get().shouldAttack(player) ? false : this.settings.shieldMode.get() != ShieldMode.Ignore || !player.isBlocking();
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void updateRotation(boolean attack, float rotationYawSpeed, float rotationPitchSpeed) {
      Vec3 vec = this.target
         .position()
         .add(
            0.0,
            Mth.clamp(
               this.mc.player.getEyeHeight(this.mc.player.getPose()) - this.target.getY(),
               0.0,
               this.target.getBbHeight() * (this.mc.player.distanceTo(this.target) / (Double)this.settings.range.get())
            ),
            0.0
         )
         .subtract(this.mc.player.getEyePosition());
      this.isRotated = true;
      float yawToTarget = (float)Mth.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
      float pitchToTarget = (float)(-Math.toDegrees(Math.atan2(vec.y, Mth.length(vec.x, vec.z))));
      float yawDelta = Mth.wrapDegrees(yawToTarget - this.rotateVector.u());
      float pitchDelta = Mth.wrapDegrees(pitchToTarget - this.rotateVector.v());
      int roundedYaw = (int)yawDelta;
      switch ((Matrix.Type)this.settings.rotationType.get()) {
         case Smooth: {
            float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1.0F), rotationYawSpeed);
            float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 1.0F), rotationPitchSpeed);
            if (attack && this.selected != this.target && (Boolean)this.settings.speedUpRotationWhenAttacking.get()) {
               clampedPitch = Math.max(Math.abs(pitchDelta), 1.0F);
            } else {
               clampedPitch /= 3.0F;
            }

            if (Math.abs(clampedYaw - this.lastYaw) <= 3.0F) {
               clampedYaw = this.lastYaw + 3.1F;
            }

            float yaw = this.rotateVector.u() + (yawDelta > 0.0F ? clampedYaw : -clampedYaw);
            float pitch = Mth.clamp(this.rotateVector.v() + (pitchDelta > 0.0F ? clampedPitch : -clampedPitch), -89.0F, 89.0F);
            float gcd = GameSensitivityUtils.getGCDValue();
            yaw -= (yaw - this.rotateVector.u()) % gcd;
            pitch -= (pitch - this.rotateVector.v()) % gcd;
            this.rotateVector = new UVPair(yaw, pitch);
            this.lastYaw = clampedYaw;
            this.lastPitch = clampedPitch;
            break;
         }
         case Fast: {
            float yaw = this.rotateVector.u() + roundedYaw;
            float pitch = Mth.clamp(this.rotateVector.v() + pitchDelta, -90.0F, 90.0F);
            float gcd = GameSensitivityUtils.getGCDValue();
            yaw -= (yaw - this.rotateVector.u()) % gcd;
            pitch -= (pitch - this.rotateVector.v()) % gcd;
            this.rotateVector = new UVPair(yaw, pitch);
         }
      }
   }

   private void reset() {
      this.rotateVector = new UVPair(this.mc.player.getYRot(), this.mc.player.getXRot());
   }

   public static enum Type {
      Smooth,
      Fast;
   }
}
