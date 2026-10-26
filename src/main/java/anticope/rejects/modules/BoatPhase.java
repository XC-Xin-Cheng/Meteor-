package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.entity.BoatMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

public class BoatPhase extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgSpeeds = this.settings.createGroup("Speeds");
   private final Setting<Boolean> lockYaw = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("lock-boat-yaw")).description("Locks boat yaw to the direction you're facing.")).defaultValue(true))
            .build()
      );
   private final Setting<Boolean> verticalControl = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("vertical-control")).description("Whether or not space/ctrl can be used to move vertically."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> adjustHorizontalSpeed = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("adjust-horizontal-speed")).description("Whether or not horizontal speed is modified."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> fall = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("fall")).description("Toggles vertical glide.")).defaultValue(false)).build());
   private final Setting<Double> horizontalSpeed = this.sgSpeeds
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("horizontal-speed"))
               .description("Horizontal speed in blocks per second."))
            .defaultValue(10.0)
            .min(0.0)
            .sliderMax(50.0)
            .build()
      );
   private final Setting<Double> verticalSpeed = this.sgSpeeds
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("vertical-speed"))
               .description("Vertical speed in blocks per second."))
            .defaultValue(5.0)
            .min(0.0)
            .sliderMax(20.0)
            .build()
      );
   private final Setting<Double> fallSpeed = this.sgSpeeds
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("fall-speed"))
               .description("How fast you fall in blocks per second."))
            .defaultValue(0.625)
            .min(0.0)
            .sliderMax(10.0)
            .build()
      );
   private AbstractBoat boat = null;

   public BoatPhase() {
      super(MeteorRejectsAddon.CATEGORY, "boat-phase", "Phase through blocks using a boat.");
   }

   public void onActivate() {
      this.boat = null;
      if (Modules.get().isActive(BoatGlitch.class)) {
         ((BoatGlitch)Modules.get().get(BoatGlitch.class)).toggle();
      }
   }

   public void onDeactivate() {
      if (this.boat != null) {
         this.boat.noPhysics = false;
      }
   }

   @EventHandler
   private void onBoatMove(BoatMoveEvent event) {
      if (this.mc.player.getVehicle() instanceof AbstractBoat) {
         if (this.boat != this.mc.player.getVehicle()) {
            if (this.boat != null) {
               this.boat.noPhysics = false;
            }

            this.boat = (AbstractBoat)this.mc.player.getVehicle();
         }
      } else {
         this.boat = null;
      }

      if (this.boat != null) {
         this.boat.noPhysics = true;
         if ((Boolean)this.lockYaw.get()) {
            this.boat.setYRot(this.mc.player.getYRot());
         }

         Vec3 vel;
         if ((Boolean)this.adjustHorizontalSpeed.get()) {
            vel = PlayerUtils.getHorizontalVelocity((Double)this.horizontalSpeed.get());
         } else {
            vel = this.boat.getDeltaMovement();
         }

         double velX = vel.x;
         double velY = 0.0;
         double velZ = vel.z;
         if ((Boolean)this.verticalControl.get()) {
            if (this.mc.options.keyJump.isDown()) {
               velY += this.verticalSpeed.get() / 20.0;
            }

            if (this.mc.options.keySprint.isDown()) {
               velY -= this.verticalSpeed.get() / 20.0;
            } else if ((Boolean)this.fall.get()) {
               velY -= this.fallSpeed.get() / 20.0;
            }
         } else if ((Boolean)this.fall.get()) {
            velY -= this.fallSpeed.get() / 20.0;
         }

         ((IVec3)this.boat.getDeltaMovement()).meteor$set(velX, velY, velZ);
      }
   }
}
