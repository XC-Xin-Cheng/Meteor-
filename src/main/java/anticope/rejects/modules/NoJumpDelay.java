package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixin.LivingEntityAccessor;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class NoJumpDelay extends Module {
   public NoJumpDelay() {
      super(MeteorRejectsAddon.CATEGORY, "no-jump-delay", "NoJumpDelay.");
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.player != null) {
         ((LivingEntityAccessor)this.mc.player).setJumpingCooldown(0);
      }
   }
}
