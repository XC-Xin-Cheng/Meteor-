package zgoly.meteorist.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import zgoly.meteorist.Meteorist;

public class BoatControl extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<Boolean> autoForward = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("auto-forward")).description("Automatically moves forward when you get into the boat."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> stopWhenTurning = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("stop-when-turning")).description("Do not move forward when turning.")).defaultValue(false))
               .visible(this.autoForward::get))
            .build()
      );
   private final Setting<Boolean> smartTurning = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("smart-turning")).description("Automatically turns the boat.")).defaultValue(true)).build());
   private final Setting<Boolean> turnToYaw = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("turn-to-yaw"))
                     .description("Always try (if possible) to turn the boat back to the yaw axis. Not effective but may be useful in some cases."))
                  .defaultValue(false))
               .visible(this.smartTurning::get))
            .build()
      );
   private final Setting<Double> yaw = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("yaw"))
                  .description("The yaw to turn to."))
               .defaultValue(0.0)
               .min(-180.0)
               .max(180.0)
               .visible(() -> (Boolean)this.smartTurning.get() && (Boolean)this.turnToYaw.get()))
            .build()
      );
   private final Setting<Boolean> autoYaw = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("auto-yaw")).description("Automatically capture boat's yaw upon boarding."))
                  .defaultValue(false))
               .visible(() -> (Boolean)this.smartTurning.get() && (Boolean)this.turnToYaw.get()))
            .build()
      );
   private final Setting<Double> accuracy = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("accuracy"))
                  .description("Accuracy of turning to yaw. The higher the value, the lower the accuracy."))
               .defaultValue(10.0)
               .min(0.0)
               .visible(() -> (Boolean)this.smartTurning.get() && (Boolean)this.turnToYaw.get()))
            .build()
      );
   private final Setting<Vector3d> leftCollisionSize = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)new meteordevelopment.meteorclient.settings.Vector3dSetting.Builder()
                     .name("left-collision-size"))
                  .description("The size of the left collision."))
               .defaultValue(new Vector3d(1.0, 1.0, 1.0))
               .visible(this.smartTurning::get))
            .build()
      );
   private final Setting<Vector3d> leftCollisionOffset = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)new meteordevelopment.meteorclient.settings.Vector3dSetting.Builder()
                     .name("left-collision-offset"))
                  .description("The offset of the left collision."))
               .defaultValue(new Vector3d(1.0, 0.0, -1.0))
               .visible(this.smartTurning::get))
            .build()
      );
   private final Setting<Vector3d> rightCollisionSize = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)new meteordevelopment.meteorclient.settings.Vector3dSetting.Builder()
                     .name("right-collision-size"))
                  .description("The size of the right collision."))
               .defaultValue(new Vector3d(1.0, 1.0, 1.0))
               .visible(this.smartTurning::get))
            .build()
      );
   private final Setting<Vector3d> rightCollisionOffset = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)new meteordevelopment.meteorclient.settings.Vector3dSetting.Builder()
                     .name("right-collision-offset"))
                  .description("The offset of the right collision."))
               .defaultValue(new Vector3d(1.0, 0.0, 1.0))
               .visible(this.smartTurning::get))
            .build()
      );
   private final Setting<BoatControl.State> leftCollisionAction = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("left-collision-action"))
                  .description("The action to do when left collision is detected."))
               .defaultValue(BoatControl.State.TURNING_RIGHT))
            .build()
      );
   private final Setting<BoatControl.State> rightCollisionAction = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("right-collision-action"))
                  .description("The action to do when right collision is detected."))
               .defaultValue(BoatControl.State.TURNING_LEFT))
            .build()
      );
   private final Setting<BoatControl.State> bothCollisionAction = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("both-collision-action"))
                  .description("The action to do when both left and right collisions are detected."))
               .defaultValue(BoatControl.State.TURNING_LEFT))
            .build()
      );
   private final Setting<Boolean> useLerpedPos = this.sgRender
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("use-lerped-position")).description("Uses the lerped position for the rendering collisions."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> renderSideColor1 = this.sgRender
      .add(((Builder)((Builder)((Builder)new Builder().name("render-left-collision")).description("Renders the left collision.")).defaultValue(true)).build());
   private final Setting<SettingColor> sideColor1 = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("left-collision-side-color"))
                  .description("The color of the sides of the collision being rendered."))
               .defaultValue(new SettingColor(0, 0, 255, 40))
               .visible(this.renderSideColor1::get))
            .build()
      );
   private final Setting<SettingColor> lineColor1 = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("left-collision-line-color"))
                  .description("The color of the lines of the collision being rendered."))
               .defaultValue(new SettingColor(0, 0, 255, 100))
               .visible(this.renderSideColor1::get))
            .build()
      );
   private final Setting<Boolean> renderSideColor2 = this.sgRender
      .add(((Builder)((Builder)((Builder)new Builder().name("render-right-collision")).description("Renders the right collision.")).defaultValue(true)).build());
   private final Setting<SettingColor> sideColor2 = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("right-collision-side-color"))
                  .description("The color of the sides of the collision being rendered."))
               .defaultValue(new SettingColor(255, 0, 0, 40))
               .visible(this.renderSideColor2::get))
            .build()
      );
   private final Setting<SettingColor> lineColor2 = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("right-collision-line-color"))
                  .description("The color of the lines of the collision being rendered."))
               .defaultValue(new SettingColor(255, 0, 0, 100))
               .visible(this.renderSideColor2::get))
            .build()
      );
   private final List<KeyMapping> toRelease = new ArrayList<>();
   private BoatControl.State currentState = BoatControl.State.NOTHING;
   private boolean wasInBoat = false;

   public BoatControl() {
      super(Meteorist.CATEGORY, "boat-control", "Automatically controls the boat for you.");
   }

   public String getInfoString() {
      return this.currentState.toString();
   }

   public void onDeactivate() {
      this.wasInBoat = false;
      this.toRelease.clear();
      this.mc.options.keyUp.setDown(false);
      this.mc.options.keyLeft.setDown(false);
      this.mc.options.keyRight.setDown(false);
   }

   @EventHandler
   private void onTick(Pre event) {
      this.currentState = BoatControl.State.NOTHING;
      if (!(this.mc.player.getVehicle() instanceof AbstractBoat boat)) {
         this.wasInBoat = false;
      } else if (boat.isInLiquid() && boat.getControllingPassenger() == this.mc.player) {
         if (!this.wasInBoat && (Boolean)this.turnToYaw.get() && (Boolean)this.autoYaw.get()) {
            this.yaw.set((double)Mth.wrapDegrees(boat.getYRot()));
            this.wasInBoat = true;
         }

         if ((Boolean)this.autoForward.get()) {
            this.currentState = BoatControl.State.MOVING_FORWARD;
            this.hold(this.mc.options.keyUp);
         }

         if ((Boolean)this.smartTurning.get()) {
            Map<BoatControl.State, KeyMapping> stateToKeyBinding = Map.of(
               BoatControl.State.TURNING_LEFT, this.mc.options.keyLeft, BoatControl.State.TURNING_RIGHT, this.mc.options.keyRight
            );
            BoatControl.State leftAction = (BoatControl.State)this.leftCollisionAction.get();
            BoatControl.State rightAction = (BoatControl.State)this.rightCollisionAction.get();
            List<BlockPos> leftList = this.getBlockPos(this.getBox(boat, (Vector3d)this.leftCollisionOffset.get(), (Vector3d)this.leftCollisionSize.get()));
            List<BlockPos> rightList = this.getBlockPos(this.getBox(boat, (Vector3d)this.rightCollisionOffset.get(), (Vector3d)this.rightCollisionSize.get()));
            if (!leftList.isEmpty()) {
               this.currentState = leftAction;
               this.hold(stateToKeyBinding.get(leftAction));
            } else if (!rightList.isEmpty()) {
               this.currentState = rightAction;
               this.hold(stateToKeyBinding.get(rightAction));
            }

            if (!leftList.isEmpty() && !rightList.isEmpty()) {
               this.currentState = (BoatControl.State)this.bothCollisionAction.get();
            }

            double yawDifference = Mth.wrapDegrees((Double)this.yaw.get() - boat.getYRot());
            if ((this.currentState == BoatControl.State.MOVING_FORWARD || this.currentState == BoatControl.State.NOTHING)
               && (Boolean)this.turnToYaw.get()
               && Math.abs(yawDifference) > (Double)this.accuracy.get()) {
               this.currentState = yawDifference < 0.0 ? BoatControl.State.TURNING_LEFT : BoatControl.State.TURNING_RIGHT;
               this.hold(stateToKeyBinding.get(this.currentState));
            }

            if ((this.currentState == BoatControl.State.TURNING_LEFT || this.currentState == BoatControl.State.TURNING_RIGHT)
               && (Boolean)this.autoForward.get()
               && (Boolean)this.stopWhenTurning.get()) {
               this.mc.options.keyUp.setDown(false);
            }
         }
      } else {
         this.wasInBoat = false;
      }
   }

   private void hold(KeyMapping keyBinding) {
      this.toRelease.add(keyBinding);
      keyBinding.setDown(true);
   }

   @EventHandler
   private void onTick(Post event) {
      if (!this.toRelease.isEmpty()) {
         this.toRelease.forEach(keyBinding -> keyBinding.setDown(false));
         this.toRelease.clear();
      }
   }

   private Vec3 vectorToVec(Vector3d vector3d) {
      return new Vec3(vector3d.x, vector3d.y, vector3d.z);
   }

   private AABB getBox(AbstractBoat boat, Vector3d collisionOffset, Vector3d collisionSize) {
      return this.getBoxInternal(boat, collisionOffset, collisionSize, boat.position());
   }

   private AABB getBoxWithLerp(AbstractBoat boat, Vector3d collisionOffset, Vector3d collisionSize, float deltaTime) {
      return this.getBoxInternal(boat, collisionOffset, collisionSize, boat.getPosition(deltaTime));
   }

   private AABB getBoxInternal(AbstractBoat boat, Vector3d collisionOffset, Vector3d collisionSize, Vec3 boatPos) {
      Vec3 offsetPos = boatPos.add(this.vectorToVec(collisionOffset).yRot((float)(-Math.toRadians(boat.getYRot() + 90.0F))));
      Vec3 size = this.vectorToVec(collisionSize);
      return AABB.ofSize(offsetPos, size.x, size.y, size.z);
   }

   private List<BlockPos> getBlockPos(AABB box) {
      return BlockPos.betweenClosedStream(box)
         .<BlockPos>map(BlockPos::new)
         .filter(blockPos -> !this.mc.level.getBlockState(blockPos).getCollisionShape(this.mc.level, blockPos).isEmpty())
         .filter(blockPos -> !(this.mc.level.getBlockState(blockPos).getBlock() instanceof LilyPadBlock))
         .toList();
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.mc.player.getVehicle() instanceof AbstractBoat boat && boat.isInLiquid() && boat.getControllingPassenger() == this.mc.player) {
         if ((Boolean)this.renderSideColor1.get()) {
            AABB box = this.useLerpedPos.get()
               ? this.getBoxWithLerp(boat, (Vector3d)this.leftCollisionOffset.get(), (Vector3d)this.leftCollisionSize.get(), event.tickDelta)
               : this.getBox(boat, (Vector3d)this.leftCollisionOffset.get(), (Vector3d)this.leftCollisionSize.get());
            event.renderer.box(box, (Color)this.sideColor1.get(), (Color)this.lineColor1.get(), ShapeMode.Both, 0);
         }

         if ((Boolean)this.renderSideColor2.get()) {
            AABB box = this.useLerpedPos.get()
               ? this.getBoxWithLerp(boat, (Vector3d)this.rightCollisionOffset.get(), (Vector3d)this.rightCollisionSize.get(), event.tickDelta)
               : this.getBox(boat, (Vector3d)this.rightCollisionOffset.get(), (Vector3d)this.rightCollisionSize.get());
            event.renderer.box(box, (Color)this.sideColor2.get(), (Color)this.lineColor2.get(), ShapeMode.Both, 0);
         }
      }
   }

   private static enum State {
      NOTHING,
      MOVING_FORWARD,
      TURNING_LEFT,
      TURNING_RIGHT;
   }
}
