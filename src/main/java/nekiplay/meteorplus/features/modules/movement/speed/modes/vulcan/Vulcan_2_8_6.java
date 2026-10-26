package nekiplay.meteorplus.features.modules.movement.speed.modes.vulcan;

import meteordevelopment.meteorclient.events.entity.player.JumpVelocityMultiplierEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedMode;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedModes;
import nekiplay.meteorplus.utils.MovementUtils;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public class Vulcan_2_8_6 extends SpeedMode {
   private int ticks = 0;
   private int speedLevel = 0;
   private boolean jumped = false;

   public Vulcan_2_8_6() {
      super(SpeedModes.Vulcan_2dot8dot6);
   }

   @Override
   public void onJump(JumpVelocityMultiplierEvent event) {
      this.ticks = 0;
      this.speedLevel = 0;
      this.jumped = true;
      if (this.mc.player.hasEffect(MobEffects.SPEED)) {
         this.speedLevel = this.mc.player.getEffect(MobEffects.SPEED).getAmplifier();
      }
   }

   @Override
   public void onTickEventPre(Pre event) {
      if (this.jumped) {
         this.ticks++;
         if (this.ticks == 1) {
            MovementUtils.strafe(0.3355 * (1.0 + this.speedLevel * 0.3819));
         }

         if (this.ticks == 2 && this.mc.player.isSprinting()) {
            MovementUtils.strafe(0.3284 * (1.0 + this.speedLevel * 0.355));
         }

         if (this.ticks == 4) {
            Vec3 vel = this.mc.player.position();
            this.mc.player.setPosRaw(vel.x, vel.y - 0.376, vel.z);
         }

         if (this.ticks == 6) {
            if (this.mc.player.flyDist > 0.298) {
               MovementUtils.strafe(0.298);
            }

            this.jumped = false;
         }
      }
   }
}
