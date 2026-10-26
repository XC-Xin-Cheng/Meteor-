package nekiplay.meteorplus.features.modules.movement.noslow.modes;

import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import nekiplay.meteorplus.features.modules.movement.noslow.NoSlowMode;
import nekiplay.meteorplus.features.modules.movement.noslow.NoSlowModes;
import net.minecraft.world.phys.Vec3;

public class Matrix extends NoSlowMode {
   private int ticks = 0;

   public Matrix() {
      super(NoSlowModes.Matrix);
   }

   @Override
   public void onActivate() {
      this.ticks = 0;
   }

   @Override
   public void onTickEventPre(Pre event) {
      if (this.mc.player.isUsingItem() && this.mc.player.onGround() && this.ticks % 2 == 0) {
         float speed = 0.4F;
         Vec3 vel = this.mc.player.getDeltaMovement();
         double x = vel.x() * speed;
         double z = vel.z() * speed;
         this.mc.player.setDeltaMovement(x, vel.y(), z);
      }

      this.ticks++;
   }
}
