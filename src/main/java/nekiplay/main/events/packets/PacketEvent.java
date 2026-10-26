package nekiplay.main.events.packets;

import nekiplay.main.events.Cancellable;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;

public class PacketEvent {
   public static class Receive extends Cancellable {
      private static final PacketEvent.Receive INSTANCE = new PacketEvent.Receive();
      public Packet<?> packet;
      public PacketListener packetListener;

      public static PacketEvent.Receive get(Packet<?> packet, PacketListener listener) {
         INSTANCE.setCancelled(false);
         INSTANCE.packet = packet;
         INSTANCE.packetListener = listener;
         return INSTANCE;
      }
   }

   public static class Send extends Cancellable {
      private static final PacketEvent.Send INSTANCE = new PacketEvent.Send();
      public Packet<?> packet;

      public static PacketEvent.Send get(Packet<?> packet) {
         INSTANCE.setCancelled(false);
         INSTANCE.packet = packet;
         return INSTANCE;
      }
   }

   public static class Sent {
      private static final PacketEvent.Sent INSTANCE = new PacketEvent.Sent();
      public Packet<?> packet;

      public static PacketEvent.Sent get(Packet<?> packet) {
         INSTANCE.packet = packet;
         return INSTANCE;
      }
   }
}
