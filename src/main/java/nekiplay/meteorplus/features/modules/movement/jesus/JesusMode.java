package nekiplay.meteorplus.features.modules.movement.jesus;

import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.Minecraft;

public class JesusMode {
   protected final Minecraft mc;
   protected final JesusPlus settings = (JesusPlus)Modules.get().get(JesusPlus.class);
   private final JesusModes type;

   public JesusMode(JesusModes type) {
      this.mc = Minecraft.getInstance();
      this.type = type;
   }

   public void onSendPacket(Send event) {
   }

   public void onSentPacket(Sent event) {
   }

   public void onPlayerMoveEvent(PlayerMoveEvent event) {
   }

   public void onCanWalkOnFluid(CanWalkOnFluidEvent event) {
   }

   public void onCollisionShape(CollisionShapeEvent event) {
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
