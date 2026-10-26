package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.ColorSetting.Builder;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SkeletonESP extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<SettingColor> skeletonColorSetting = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("players-color")).description("The other player's color."))
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
      );
   public final Setting<Boolean> distance = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("distance-colors"))
                  .description("Changes the color of skeletons depending on distance."))
               .defaultValue(false))
            .build()
      );
   private final Freecam freecam;
   private final Color color = new Color();

   public SkeletonESP() {
      super(MeteorRejectsAddon.CATEGORY, "skeleton-esp", "Looks cool as fuck");
      this.freecam = (Freecam)Modules.get().get(Freecam.class);
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      float tickDelta = event.tickDelta;
      int rotationHoldTicks = (Integer)Config.get().rotationHoldTicks.get();
      this.mc.level.entitiesForRendering().forEach(entity -> {
         if (entity instanceof Player) {
            if (this.mc.options.getCameraType() != CameraType.FIRST_PERSON || this.freecam.isActive() || this.mc.player != entity) {
               Color skeletonColor = PlayerUtils.getPlayerColor((Player)entity, (Color)this.skeletonColorSetting.get());
               if ((Boolean)this.distance.get()) {
                  skeletonColor = this.getColorFromDistance(entity);
               }

               Player player = (Player)entity;
               Vec3 footPos = this.getEntityRenderPosition(player, tickDelta);
               AvatarRenderer livingEntityRenderer = (AvatarRenderer)this.mc.getEntityRenderDispatcher().getRenderer(player);
               PlayerModel playerModel = (PlayerModel)livingEntityRenderer.getModel();
               float bodyYaw = Mth.rotLerp(tickDelta, player.yBodyRotO, player.yBodyRot);
               if (this.mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) {
                  bodyYaw = Rotations.serverYaw;
               }

               float headYaw = Mth.rotLerp(tickDelta, player.yHeadRotO, player.yHeadRot);
               if (this.mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) {
                  headYaw = Rotations.serverYaw;
               }

               float ageInTicks = player.tickCount + tickDelta;
               float relativeHeadYaw = headYaw - bodyYaw;
               float pitch = player.getViewXRot(tickDelta);
               if (this.mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) {
                  pitch = Rotations.serverPitch;
               }

               boolean swimming = player.isVisuallySwimming();
               boolean sneaking = player.isCrouching();
               boolean flying = player.isFallFlying();
               AvatarRenderState renderState = new AvatarRenderState();
               renderState.walkAnimationPos = player.walkAnimation.position(tickDelta);
               renderState.walkAnimationSpeed = player.walkAnimation.speed(tickDelta);
               renderState.ageInTicks = ageInTicks;
               renderState.yRot = relativeHeadYaw;
               renderState.xRot = pitch;
               playerModel.setupAnim(renderState);
               ModelPart head = playerModel.head;
               ModelPart leftArm = playerModel.leftArm;
               ModelPart rightArm = playerModel.rightArm;
               ModelPart leftLeg = playerModel.leftLeg;
               ModelPart rightLeg = playerModel.rightLeg;
               Matrix4f outerMat = new Matrix4f();
               if (swimming) {
                  outerMat.translate(0.0F, 0.35F, 0.0F);
               }

               outerMat.rotateY((float)Math.toRadians(-(bodyYaw + 180.0F)));
               if (swimming || flying) {
                  outerMat.rotateX((float)Math.toRadians(-(90.0F + pitch)));
               }

               if (swimming) {
                  outerMat.translate(0.0F, -0.95F, 0.0F);
               }

               Matrix4f tiltedMat = new Matrix4f(outerMat).translate(0.0F, 0.75F, 0.0F);
               if (sneaking && !swimming && !flying) {
                  tiltedMat.rotate(0.5F, -1.0F, 0.0F, 0.0F);
               }

               tiltedMat.translate(0.0F, -0.75F, 0.0F);
               float hipY = sneaking ? 0.6F : 0.7F;
               float hipZ = sneaking ? 0.23F : 0.0F;
               float shoulderY = sneaking ? 1.05F : 1.35F;
               float spineTopY = sneaking ? 1.05F : 1.4F;
               this.drawBone(event, footPos, tiltedMat, 0.0F, hipY, hipZ, 0.0F, spineTopY, 0.0F, skeletonColor);
               this.drawBone(event, footPos, tiltedMat, -0.37F, shoulderY, 0.0F, 0.37F, shoulderY, 0.0F, skeletonColor);
               this.drawBone(event, footPos, outerMat, -0.15F, hipY, hipZ, 0.15F, hipY, hipZ, skeletonColor);
               Matrix4f headMat = new Matrix4f(tiltedMat).translate(0.0F, spineTopY, 0.0F);
               this.applyModelRot(headMat, head);
               this.drawBone(event, footPos, headMat, 0.0F, 0.0F, 0.0F, 0.0F, 0.15F, 0.0F, skeletonColor);
               Matrix4f rightLegMat = new Matrix4f(outerMat).translate(0.15F, hipY, hipZ);
               this.applyModelRot(rightLegMat, rightLeg);
               this.drawBone(event, footPos, rightLegMat, 0.0F, 0.0F, 0.0F, 0.0F, -0.6F, 0.0F, skeletonColor);
               Matrix4f leftLegMat = new Matrix4f(outerMat).translate(-0.15F, hipY, hipZ);
               this.applyModelRot(leftLegMat, leftLeg);
               this.drawBone(event, footPos, leftLegMat, 0.0F, 0.0F, 0.0F, 0.0F, -0.6F, 0.0F, skeletonColor);
               Matrix4f rightArmMat = new Matrix4f(tiltedMat).translate(0.37F, shoulderY, 0.0F);
               this.applyModelRot(rightArmMat, rightArm);
               this.drawBone(event, footPos, rightArmMat, 0.0F, 0.0F, 0.0F, 0.0F, -0.55F, 0.0F, skeletonColor);
               Matrix4f leftArmMat = new Matrix4f(tiltedMat).translate(-0.37F, shoulderY, 0.0F);
               this.applyModelRot(leftArmMat, leftArm);
               this.drawBone(event, footPos, leftArmMat, 0.0F, 0.0F, 0.0F, 0.0F, -0.55F, 0.0F, skeletonColor);
            }
         }
      });
   }

   private void drawBone(Render3DEvent event, Vec3 origin, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, Color color) {
      Vector3f p1 = mat.transformPosition(new Vector3f(x1, y1, z1));
      Vector3f p2 = mat.transformPosition(new Vector3f(x2, y2, z2));
      event.renderer.line(origin.x + p1.x, origin.y + p1.y, origin.z + p1.z, origin.x + p2.x, origin.y + p2.y, origin.z + p2.z, color);
   }

   private void applyModelRot(Matrix4f mat, ModelPart part) {
      if (part.zRot != 0.0F) {
         mat.rotate(part.zRot, 0.0F, 0.0F, 1.0F);
      }

      if (part.yRot != 0.0F) {
         mat.rotate(part.yRot, 0.0F, -1.0F, 0.0F);
      }

      if (part.xRot != 0.0F) {
         mat.rotate(part.xRot, -1.0F, 0.0F, 0.0F);
      }
   }

   private Vec3 getEntityRenderPosition(Entity entity, double partial) {
      double x = entity.xo + (entity.getX() - entity.xo) * partial;
      double y = entity.yo + (entity.getY() - entity.yo) * partial;
      double z = entity.zo + (entity.getZ() - entity.zo) * partial;
      return new Vec3(x, y, z);
   }

   private Color getColorFromDistance(Entity entity) {
      Vec3 entityPos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
      double distance = this.mc.gameRenderer.getMainCamera().position().distanceTo(entityPos);
      double percent = distance / 60.0;
      if (!(percent < 0.0) && !(percent > 1.0)) {
         int r;
         int g;
         if (percent < 0.5) {
            r = 255;
            g = (int)(255.0 * percent / 0.5);
         } else {
            g = 255;
            r = 255 - (int)(255.0 * (percent - 0.5) / 0.5);
         }

         this.color.set(r, g, 0, 255);
         return this.color;
      } else {
         this.color.set(0, 255, 0, 255);
         return this.color;
      }
   }
}
