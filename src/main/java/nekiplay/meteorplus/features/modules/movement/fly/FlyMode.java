package nekiplay.meteorplus.features.modules.movement.fly;

import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent.Pre;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.Minecraft;

public class FlyMode {
   protected final Minecraft mc;
   protected final FlyPlus settings = (FlyPlus)Modules.get().get(FlyPlus.class);
   private final FlyModes type;

   public FlyMode(FlyModes type) {
      this.mc = Minecraft.getInstance();
      this.type = type;
   }

   public void onSendPacket(Send event) {
   }

   public void onSentPacket(Sent event) {
   }

   public void onReceivePacket(Receive event) {
   }

   public void onPlayerMoveEvent(PlayerMoveEvent event) {
   }

   public void onPlayerMoveSendPre(Pre event) {
   }

   public void onCanWalkOnFluid(CanWalkOnFluidEvent event) {
   }

   public void onCollisionShape(CollisionShapeEvent event) {
   }

   public void onTickEventPre(meteordevelopment.meteorclient.events.world.TickEvent.Pre event) {
   }

   public void onTickEventPost(Post event) {
   }

   public void onActivate() {
   }

   public void onDeactivate() {
   }
}
