package nekiplay.meteorplus.features.modules.combat.velocity;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.Minecraft;

public class VelocityMode {
   protected final Minecraft mc;
   protected final VelocityPlus settings = (VelocityPlus)Modules.get().get(VelocityPlus.class);
   private final VelocityModes type;

   public VelocityMode(VelocityModes type) {
      this.mc = Minecraft.getInstance();
      this.type = type;
   }

   public void onSendPacket(Send event) {
   }

   public void onSentPacket(Sent event) {
   }

   public void onReceivePacket(Receive event) {
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
