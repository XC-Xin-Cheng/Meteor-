package nekiplay.meteorplus.utils;

import meteordevelopment.meteorclient.MeteorClient;
import nekiplay.meteorplus.mixin.minecraft.ConnectionAccessor;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class TickTimer {
   private int ticksBegin = ticks();
   private double delay;

   public TickTimer(double delay) {
      this.delay = delay;
   }

   public void reset(double delay) {
      this.delay = delay;
      this.ticksBegin = ticks();
   }

   public boolean elapsed(double newDelay) {
      int currentTicks = ticks();
      if (currentTicks != -1 && this.ticksBegin != -1 && !(50.0 * (currentTicks - this.ticksBegin) >= this.delay)) {
         return false;
      } else {
         this.reset(newDelay);
         return true;
      }
   }

   public boolean elapsed() {
      return this.elapsed(this.delay);
   }

   private static int ticks() {
      ClientPacketListener handler = MeteorClient.mc.getConnection();
      return handler == null ? -1 : ((ConnectionAccessor)handler.getConnection()).getTickCount();
   }
}
