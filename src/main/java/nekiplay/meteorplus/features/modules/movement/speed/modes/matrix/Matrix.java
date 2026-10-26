package nekiplay.meteorplus.features.modules.movement.speed.modes.matrix;

import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedMode;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedModes;

public class Matrix extends SpeedMode {
   public Matrix() {
      super(SpeedModes.Matrix);
   }

   @Override
   public void onDeactivate() {
      ((Timer)Modules.get().get(Timer.class)).setOverride(1.0);
      if (this.mc.player != null) {
         this.mc.player.getAbilities().setFlyingSpeed(0.02F);
      }
   }

   @Override
   public void onTickEventPre(Pre event) {
      Timer timer = (Timer)Modules.get().get(Timer.class);
      timer.setOverride(1.0);
      if (!this.mc.player.isInWater() && !this.mc.player.isInLava() && !this.mc.player.onClimbable() && !this.mc.player.isHandsBusy()) {
         if (PlayerUtils.isMoving()) {
            if (this.mc.player.onGround()) {
               this.mc.player.jumpFromGround();
               this.mc.player.getAbilities().setFlyingSpeed(0.02098F);
               timer.setOverride(1.055F);
            }
         } else {
            timer.setOverride(1.0);
         }
      }
   }
}
