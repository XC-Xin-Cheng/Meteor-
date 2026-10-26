package nekiplay.meteorplus.features.modules.movement.speed.modes;

import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedMode;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedModes;

public class AACHop438 extends SpeedMode {
   public AACHop438() {
      super(SpeedModes.AAC_Hop_4dot3dot8);
   }

   @Override
   public void onDeactivate() {
      ((Timer)Modules.get().get(Timer.class)).setOverride(1.0);
   }

   @Override
   public void onTickEventPre(Pre event) {
      Timer timer = (Timer)Modules.get().get(Timer.class);
      timer.setOverride(1.0);
      if (PlayerUtils.isMoving() && !this.mc.player.isInWater() && !this.mc.player.isInLava() && !this.mc.player.onClimbable() && !this.mc.player.isHandsBusy()
         )
       {
         if (this.mc.player.onGround()) {
            this.mc.player.jumpFromGround();
         } else if (this.mc.player.fallDistance <= 0.1) {
            timer.setOverride(1.5);
         } else if (this.mc.player.fallDistance < 1.3) {
            timer.setOverride(0.7);
         } else {
            timer.setOverride(1.0);
         }
      }
   }
}
