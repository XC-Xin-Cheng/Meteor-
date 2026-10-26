package nekiplay.meteorplus.features.modules.movement.speed.modes;

import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedMode;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedModes;

public class NCPHop extends SpeedMode {
   public NCPHop() {
      super(SpeedModes.NCP_Hop);
   }

   @Override
   public void onActivate() {
      ((Timer)Modules.get().get(Timer.class)).setOverride(1.0865F);
   }

   @Override
   public void onDeactivate() {
      ((Timer)Modules.get().get(Timer.class)).setOverride(1.0);
      this.mc.player.getAbilities().setFlyingSpeed(0.02F);
   }

   @Override
   public void onTickEventPre(Pre event) {
      if (!this.mc.player.isInWater() && !this.mc.player.isInLava() && !this.mc.player.onClimbable() && !this.mc.player.isHandsBusy()) {
         Timer timer = (Timer)Modules.get().get(Timer.class);
         if (PlayerUtils.isMoving() && this.mc.player.onGround()) {
            this.mc.player.jumpFromGround();
            this.mc.player.getAbilities().setFlyingSpeed(0.0223F);
         } else {
            timer.setOverride(1.0);
         }
      }
   }
}
