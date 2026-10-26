package fr.crazycat256.satellite.modules;

import fr.crazycat256.satellite.Addon;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.phys.Vec3;

public class KnockbackTweaks extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<KnockbackTweaks.Mode> mode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("mode")).description("The mode of the knockback direction."))
               .defaultValue(KnockbackTweaks.Mode.RelativeToPlayer))
            .build()
      );
   private final Setting<Integer> direction = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("direction"))
                  .description("The direction of the knockback."))
               .defaultValue(0))
            .range(-180, 180)
            .sliderRange(-180, 180)
            .build()
      );
   private final Setting<Integer> randomRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("random"))
                  .description("The random range of the knockback."))
               .defaultValue(0))
            .min(0)
            .sliderMax(90)
            .max(180)
            .build()
      );
   private final Setting<Boolean> fakeSprint = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("fake-sprint"))
                  .description("Make you sprint when you hit an entity."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> renderDirection = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("render-direction"))
                  .description("Display the direction range."))
               .defaultValue(false))
            .build()
      );
   private final Setting<SettingColor> lineColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("line-color"))
               .description("The color of the line."))
            .defaultValue(new SettingColor(255, 128, 0, 255))
            .build()
      );
   private final Setting<SettingColor> sideColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("side-color"))
               .description("The color of the side."))
            .defaultValue(new SettingColor(255, 128, 0, 64))
            .build()
      );
   private final Setting<Double> distance = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("distance"))
               .description("The distance of the direction range."))
            .defaultValue(3.0)
            .min(0.0)
            .sliderMax(5.0)
            .build()
      );
   private final Setting<Double> radius = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("radius"))
               .description("The radius of the direction range."))
            .defaultValue(3.0)
            .min(0.0)
            .sliderMax(5.0)
            .build()
      );
   private boolean shouldResendLookPacket = false;
   private boolean shouldResendSprintPacket = false;

   public KnockbackTweaks() {
      super(
         Addon.CATEGORY,
         "knockback-tweaks",
         "Allows you to change the direction of the knockback received by the entities you hit. (doesn't work with criticals)"
      );
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.player != null && this.mc.level != null) {
         if (this.shouldResendLookPacket) {
            this.shouldResendLookPacket = false;
            this.mc
               .player
               .connection
               .send(new Rot(this.mc.player.getYRot(), this.mc.player.getXRot(), this.mc.player.onGround(), this.mc.player.horizontalCollision));
         }

         if (this.shouldResendSprintPacket) {
            this.shouldResendSprintPacket = false;
            this.mc.player.connection.send(new ServerboundPlayerCommandPacket(this.mc.player, Action.START_SPRINTING));
         }
      }
   }

   @EventHandler
   private void onEntityAttack(AttackEntityEvent event) {
      if (this.mc.player != null && this.mc.level != null) {
         if ((Boolean)this.fakeSprint.get()) {
            if (this.mc.player.isSprinting()) {
               this.mc.player.connection.send(new ServerboundPlayerCommandPacket(this.mc.player, Action.STOP_SPRINTING));
            }

            this.mc.player.connection.send(new ServerboundPlayerCommandPacket(this.mc.player, Action.START_SPRINTING));
            if (this.mc.player.isSprinting()) {
               this.shouldResendSprintPacket = true;
            }
         }

         float yaw;
         if (this.mode.get() == KnockbackTweaks.Mode.Absolute) {
            yaw = ((Integer)this.direction.get()).intValue();
         } else if (this.mode.get() == KnockbackTweaks.Mode.RelativeToView) {
            yaw = ((Integer)this.direction.get()).intValue() + this.mc.player.getYRot();
         } else {
            if (this.mode.get() != KnockbackTweaks.Mode.RelativeToPlayer) {
               return;
            }

            Vec3 playerPos = this.mc.player.position();
            Vec3 entityPos = event.entity.position();
            Vec3 diff = entityPos.subtract(playerPos);
            yaw = (float)Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F + ((Integer)this.direction.get()).intValue();
         }

         if ((Integer)this.randomRange.get() != 0) {
            int range = (Integer)this.randomRange.get() * 2 + 1;
            yaw += (float)(Math.random() * range - ((Integer)this.randomRange.get()).intValue());
         }

         this.mc.player.connection.send(new Rot(yaw, this.mc.player.getXRot(), this.mc.player.onGround(), this.mc.player.horizontalCollision));
         this.shouldResendLookPacket = true;
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.mc.level != null && this.mc.getCameraEntity() != null && (Boolean)this.renderDirection.get()) {
         float yawMin = (
               this.mode.get() == KnockbackTweaks.Mode.Absolute
                  ? this.mc.player.getYRot() - ((Integer)this.direction.get()).intValue()
                  : -(Integer)this.direction.get()
            )
            - ((Integer)this.randomRange.get()).intValue();
         float yawMax = yawMin + 2 * (Integer)this.randomRange.get();
         Vec3 rotation = this.mc.player.getLookAngle().multiply(1.0, 0.0, 1.0);
         Vec3 lookPos = this.mc
            .player
            .position()
            .add(
               new Vec3(
                  (this.mc.player.getX() - this.mc.player.xo) * event.tickDelta + this.mc.player.xo - this.mc.player.getX(),
                  (this.mc.player.getY() - this.mc.player.yo) * event.tickDelta + this.mc.player.yo - this.mc.player.getY(),
                  (this.mc.player.getZ() - this.mc.player.zo) * event.tickDelta + this.mc.player.zo - this.mc.player.getZ()
               )
            )
            .add(rotation.scale((Double)this.distance.get()));
         Vec3 kbRangeMin = lookPos.add(
            rotation.normalize().multiply((Double)this.radius.get(), 0.0, (Double)this.radius.get()).yRot((float)Math.toRadians(yawMin))
         );
         Vec3 kbRangeMax = lookPos.add(
            rotation.normalize().multiply((Double)this.radius.get(), 0.0, (Double)this.radius.get()).yRot((float)Math.toRadians(yawMax))
         );
         event.renderer
            .sideHorizontal(
               lookPos.x - 0.25,
               lookPos.y,
               lookPos.z - 0.25,
               lookPos.x + 0.25,
               lookPos.z + 0.25,
               (Color)this.sideColor.get(),
               (Color)this.lineColor.get(),
               ShapeMode.Both
            );
         event.renderer.line(lookPos.x, lookPos.y, lookPos.z, kbRangeMin.x, kbRangeMin.y, kbRangeMin.z, (Color)this.lineColor.get());
         event.renderer.line(lookPos.x, lookPos.y, lookPos.z, kbRangeMax.x, kbRangeMax.y, kbRangeMax.z, (Color)this.lineColor.get());
         if ((Integer)this.randomRange.get() != 0) {
            Vec3 lastPoint = null;

            for (float yaw = yawMin; yaw < yawMax; yaw++) {
               Vec3 currentPoint = lookPos.add(
                  rotation.normalize().multiply((Double)this.radius.get(), 0.0, (Double)this.radius.get()).yRot((float)Math.toRadians(yaw))
               );
               if (lastPoint != null) {
                  event.renderer.line(lastPoint.x, lastPoint.y, lastPoint.z, currentPoint.x, currentPoint.y, currentPoint.z, (Color)this.lineColor.get());
               }

               lastPoint = currentPoint;
            }

            event.renderer.line(lastPoint.x, lastPoint.y, lastPoint.z, kbRangeMax.x, kbRangeMax.y, kbRangeMax.z, (Color)this.lineColor.get());
         }
      }
   }

   public static enum Mode {
      RelativeToPlayer,
      RelativeToView,
      Absolute;
   }
}
