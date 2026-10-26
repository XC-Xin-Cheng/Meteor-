package zgoly.meteorist.modules.zaimbot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura.EntityAge;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.utils.MeteoristUtils;
import zgoly.meteorist.utils.config.MeteoristConfigManager;

public class ZAimbot extends Module {
   private final SettingGroup sgFilter = this.settings.createGroup("Filter");
   private final SettingGroup sgAim = this.settings.createGroup("Aim");
   private final Setting<Set<EntityType<?>>> entities = this.sgFilter
      .add(
         ((Builder)((Builder)new Builder().name("entities")).description("Entities to aim at."))
            .onlyAttackable()
            .defaultValue(new EntityType[]{EntityType.PLAYER})
            .build()
      );
   private final Setting<Double> range = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("range"))
               .description("The maximum range the entity can be to aim at it."))
            .defaultValue(4.5)
            .min(0.0)
            .sliderMax(6.0)
            .build()
      );
   private final Setting<EntityAge> mobAgeFilter = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("mob-age-filter"))
                  .description("Determines the age of the mobs to target (baby, adult, or both)."))
               .defaultValue(EntityAge.Adult))
            .build()
      );
   private final Setting<Boolean> ignoreNamed = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-named"))
                  .description("Whether or not to aim at mobs with a name."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> ignorePassive = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-passive"))
                  .description("Will only aim at sometimes passive mobs if they are targeting you."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> ignoreTamed = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-tamed"))
                  .description("Will avoid aiming at mobs you tamed."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> ignoreCreative = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-creative"))
                  .description("Will avoid aiming at players in creative mode."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> ignoreFriends = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-friends"))
                  .description("Will avoid aiming at players on your friends list."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> ignoreShield = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-shield"))
                  .description("Will avoid aiming at players who are using a shield."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> useFovRange = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("use-fov-range"))
                  .description("Restrict aiming to entities within the specified FOV."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Double> fovRange = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("fov-range"))
                  .description("Maximum Field of View (FOV) range for targeting entities."))
               .sliderRange(0.0, 180.0)
               .defaultValue(90.0)
               .visible(this.useFovRange::get))
            .build()
      );
   private final Setting<Boolean> ignoreWalls = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-walls"))
                  .description("Allow aiming through walls."))
               .defaultValue(false))
            .build()
      );
   private final Setting<SortPriority> priority = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("priority"))
                  .description("Sorting method to prioritize targets within range."))
               .defaultValue(SortPriority.ClosestAngle))
            .build()
      );
   public final Setting<Double> targetMovementPrediction = this.sgAim
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("target-movement-prediction"))
               .description("Amount to predict the target's movement when aiming."))
            .min(0.0)
            .sliderMax(20.0)
            .defaultValue(0.0)
            .build()
      );
   private final Setting<ZAimbot.AimAxes> aimAxes = this.sgAim
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("aim-axes"))
                  .description("Which rotational axes to adjust when aiming."))
               .defaultValue(ZAimbot.AimAxes.Both))
            .build()
      );
   private final Setting<Target> bodyTarget = this.sgAim
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("aim-target"))
                  .description("Part of the target entity's body to aim at."))
               .defaultValue(Target.Head))
            .build()
      );
   private final Setting<Boolean> instantYaw = this.sgAim
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("instant-yaw"))
                  .description("Aim horizontally (yaw) at the target entity instantly."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> instantPitch = this.sgAim
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("instant-pitch"))
                  .description("Aim vertically (pitch) at the target entity instantly."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> syncYawWithCooldown = this.sgAim
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("sync-yaw-with-cooldown"))
                     .description("Synchronize yaw aim speed with attack cooldown progress."))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.instantYaw.get()))
            .build()
      );
   private final Setting<Boolean> syncPitchWithCooldown = this.sgAim
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("sync-pitch-with-cooldown"))
                     .description("Synchronize pitch aim speed with attack cooldown progress."))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.instantPitch.get()))
            .build()
      );
   private final Setting<Double> speedYaw = this.sgAim
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("speed-yaw"))
                  .description("Speed at which to adjust horizontal (yaw) aim."))
               .min(0.0)
               .defaultValue(1.0)
               .sliderRange(0.1, 10.0)
               .visible(() -> !(Boolean)this.instantYaw.get()))
            .build()
      );
   private final Setting<Double> speedPitch = this.sgAim
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("speed-pitch"))
                  .description("Speed at which to adjust vertical (pitch) aim."))
               .min(0.0)
               .defaultValue(1.0)
               .sliderRange(0.1, 10.0)
               .visible(() -> !(Boolean)this.instantPitch.get()))
            .build()
      );
   private final List<AimTrajectory> trajectories = new ArrayList<>();

   public ZAimbot() {
      super(Meteorist.CATEGORY, "z-aimbot", "Smart aimbot that takes many settings into account when targeting.");
   }

   public CompoundTag toTag() {
      CompoundTag tag = super.toTag();
      ListTag list = new ListTag();

      for (AimTrajectory trajectory : this.trajectories) {
         CompoundTag tTag = new CompoundTag();
         tTag.put("trajectory", trajectory.toTag());
         list.add(tTag);
      }

      tag.put("trajectories", list);
      return tag;
   }

   public Module fromTag(CompoundTag tag) {
      super.fromTag(tag);
      this.trajectories.clear();

      for (Tag tagII : tag.getListOrEmpty("trajectories")) {
         CompoundTag tagI = (CompoundTag)tagII;
         AimTrajectory trajectory = new AimTrajectory();
         CompoundTag trajectoryTag = (CompoundTag)tagI.get("trajectory");
         if (trajectoryTag != null) {
            trajectory.fromTag(trajectoryTag);
         }

         this.trajectories.add(trajectory);
      }

      return this;
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      this.fillWidget(theme, list);
      return list;
   }

   private void fillWidget(GuiTheme theme, WVerticalList list) {
      list.clear();

      for (AimTrajectory trajectory : this.trajectories) {
         list.add(theme.settings(trajectory.settings)).expandX();
         WContainer controls = (WContainer)list.add(theme.horizontalList()).widget();
         if (this.trajectories.size() > 1) {
            WContainer moveContainer = (WContainer)controls.add(theme.horizontalList()).expandX().widget();
            int index = this.trajectories.indexOf(trajectory);
            if (index > 0) {
               WButton moveUp = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_UP)).expandX().widget();
               moveUp.tooltip = "Move trajectory up.";
               moveUp.action = () -> {
                  this.trajectories.remove(index);
                  this.trajectories.add(index - 1, trajectory);
                  this.fillWidget(theme, list);
               };
            }

            if (index < this.trajectories.size() - 1) {
               WButton moveDown = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_DOWN)).expandX().widget();
               moveDown.tooltip = "Move trajectory down.";
               moveDown.action = () -> {
                  this.trajectories.remove(index);
                  this.trajectories.add(index + 1, trajectory);
                  this.fillWidget(theme, list);
               };
            }
         }

         WButton copy = (WButton)controls.add(theme.button(GuiRenderer.COPY)).widget();
         copy.tooltip = "Duplicate trajectory.";
         copy.action = () -> {
            this.trajectories.add(this.trajectories.indexOf(trajectory), trajectory.copy());
            this.fillWidget(theme, list);
         };
         WMinus remove = (WMinus)controls.add(theme.minus()).widget();
         remove.tooltip = "Remove trajectory.";
         remove.action = () -> {
            this.trajectories.remove(trajectory);
            this.fillWidget(theme, list);
         };
      }

      if (!this.trajectories.isEmpty()) {
         list.add(theme.horizontalSeparator()).expandX();
      }

      WContainer controls = (WContainer)list.add(theme.horizontalList()).expandX().widget();
      WButton add = (WButton)controls.add(theme.button("Add Trajectory")).expandX().widget();
      add.action = () -> {
         this.trajectories.add(new AimTrajectory());
         this.fillWidget(theme, list);
      };
      WButton removeAll = (WButton)controls.add(theme.button("Remove All Trajectories")).expandX().widget();
      removeAll.action = () -> {
         this.trajectories.clear();
         this.fillWidget(theme, list);
      };
      MeteoristConfigManager.configManager(theme, list, this);
   }

   @EventHandler
   private void onRender3D(Render3DEvent event) {
      if (this.mc.player != null && this.mc.level != null) {
         Entity target = TargetUtils.get(this::entityCheck, (SortPriority)this.priority.get());
         if (target != null) {
            this.aim(this.mc.player, target);
         }
      }
   }

   private boolean entityCheck(Entity entity) {
      if (entity == this.mc.player || entity == this.mc.getCameraEntity()) {
         return false;
      } else if (!entity.isAlive()) {
         return false;
      } else if (entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) {
         return false;
      } else if (!PlayerUtils.isWithin(entity, (Double)this.range.get())) {
         return false;
      } else if (!((Set)this.entities.get()).contains(entity.getType())) {
         return false;
      } else if ((Boolean)this.ignoreNamed.get() && entity.hasCustomName()) {
         return false;
      } else if ((Boolean)this.ignoreTamed.get() && entity instanceof OwnableEntity ownable && Objects.equals(ownable.getOwner(), this.mc.player)) {
         return false;
      } else if ((Boolean)this.ignorePassive.get() && this.isPassive(entity)) {
         return false;
      } else {
         if (entity instanceof Player player) {
            if ((Boolean)this.ignoreCreative.get() && player.isCreative()) {
               return false;
            }

            if ((Boolean)this.ignoreFriends.get() && !Friends.get().shouldAttack(player)) {
               return false;
            }

            if ((Boolean)this.ignoreShield.get() && player.isBlocking()) {
               return false;
            }
         }

         if (entity instanceof Animal animal) {
            return switch ((EntityAge)this.mobAgeFilter.get()) {
               case Baby -> animal.isBaby();
               case Adult -> !animal.isBaby();
               case Both -> true;
               default -> throw new MatchException(null, null);
            };
         } else {
            return this.useFovRange.get() && MeteoristUtils.calculateFov(this.mc.player, entity) > this.fovRange.get()
               ? false
               : (Boolean)this.ignoreWalls.get() || PlayerUtils.canSeeEntity(entity);
         }
      }
   }

   private boolean isPassive(Entity entity) {
      return switch (entity) {
         case EnderMan enderman -> !enderman.isCreepy();
         case ZombifiedPiglin piglin -> !piglin.isAggressive();
         case Wolf wolf -> !wolf.isAggressive();
         default -> false;
      };
   }

   private void aim(LivingEntity player, Entity target) {
      Vec3 predictedPos = target.position().add(target.getDeltaMovement().scale((Double)this.targetMovementPrediction.get()));
      float yaw = (float)Rotations.getYaw(predictedPos);
      Item usedItem = this.mc.player.getMainHandItem().getItem();
      AimTrajectory matchingTrajectory = this.trajectories.stream().filter(t -> ((List)t.items.get()).contains(usedItem)).findFirst().orElse(null);
      float pitch;
      if (matchingTrajectory != null) {
         pitch = this.calculatePitchWithTrajectory(player, target, matchingTrajectory);
      } else {
         pitch = (float)Rotations.getPitch(target, (Target)this.bodyTarget.get());
      }

      float yawDifference = Mth.wrapDegrees(yaw - player.getYRot());
      float pitchDifference = Mth.wrapDegrees(pitch - player.getXRot());
      if ((Boolean)this.instantYaw.get()) {
         if (this.aimAxes.get() == ZAimbot.AimAxes.Both || this.aimAxes.get() == ZAimbot.AimAxes.OnlyYaw) {
            player.setYRot(yaw);
         }
      } else if (this.aimAxes.get() == ZAimbot.AimAxes.Both || this.aimAxes.get() == ZAimbot.AimAxes.OnlyYaw) {
         float cooldownProgress = this.syncYawWithCooldown.get() ? this.mc.player.getAttackStrengthScale(0.0F) : 1.0F;
         float yawSpeedFactor = ((Double)this.speedYaw.get()).floatValue() / 10.0F;
         player.setYRot(player.getYRot() + yawDifference * cooldownProgress * yawSpeedFactor);
      }

      if ((Boolean)this.instantPitch.get()) {
         if (this.aimAxes.get() == ZAimbot.AimAxes.Both || this.aimAxes.get() == ZAimbot.AimAxes.OnlyPitch) {
            player.setXRot(pitch);
         }
      } else if (this.aimAxes.get() == ZAimbot.AimAxes.Both || this.aimAxes.get() == ZAimbot.AimAxes.OnlyPitch) {
         float cooldownProgress = this.syncPitchWithCooldown.get() ? this.mc.player.getAttackStrengthScale(0.0F) : 1.0F;
         float pitchSpeedFactor = ((Double)this.speedPitch.get()).floatValue() / 10.0F;
         player.setXRot(player.getXRot() + pitchDifference * cooldownProgress * pitchSpeedFactor);
      }
   }

   private float calculatePitchWithTrajectory(LivingEntity player, Entity target, AimTrajectory traj) {
      Vec3 playerEye = player.getEyePosition();
      Vec3 targetPos = target.position().add(0.0, target.getBoundingBox().getYsize() / 2.0, 0.0);
      double dx = targetPos.x - playerEye.x;
      double dy = targetPos.y - playerEye.y;
      double dz = targetPos.z - playerEye.z;
      double hDist = Math.sqrt(dx * dx + dz * dz);
      if (hDist == 0.0) {
         return 0.0F;
      } else {
         double g = 0.006 * (Double)traj.gravity.get();
         double v = (Double)traj.velocityScale.get();
         double vSq = v * v;
         double sqrtPart = vSq * vSq - g * (g * hDist * hDist + 2.0 * dy * vSq);
         if (sqrtPart < 0.0) {
            return (float)Rotations.getPitch(target, (Target)this.bodyTarget.get());
         } else {
            double pitchRad = Math.atan((vSq - Math.sqrt(sqrtPart)) / (g * hDist));
            return (float)(-Math.toDegrees(pitchRad));
         }
      }
   }

   public static enum AimAxes {
      Both,
      OnlyYaw,
      OnlyPitch;
   }
}
