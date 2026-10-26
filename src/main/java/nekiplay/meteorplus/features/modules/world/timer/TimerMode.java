package nekiplay.meteorplus.features.modules.world.timer;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import net.minecraft.client.Minecraft;

public class TimerMode {
   protected final Minecraft mc;
   protected final TimerPlus settings;
   private final TimerModes type;
   protected Timer timer = (Timer)Modules.get().get(Timer.class);

   public TimerMode(TimerModes type) {
      this.settings = (TimerPlus)Modules.get().get(TimerPlus.class);
      this.mc = Minecraft.getInstance();
      this.type = type;
   }

   public void onSendPacket(Send event) {
   }

   public void onSentPacket(Sent event) {
   }

   public void onTickEventPre(Pre event) {
   }

   public void onTickEventPost(Post event) {
   }

   public void onActivate() {
   }

   public void onDeactivate() {
   }
}
