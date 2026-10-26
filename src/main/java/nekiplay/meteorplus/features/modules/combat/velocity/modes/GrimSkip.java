package nekiplay.meteorplus.features.modules.combat.velocity.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import nekiplay.meteorplus.features.modules.combat.velocity.VelocityMode;
import nekiplay.meteorplus.features.modules.combat.velocity.VelocityModes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class GrimSkip extends VelocityMode {
   private int skip = 0;
   private boolean canCancel = false;

   public GrimSkip() {
      super(VelocityModes.Grim_Skip);
   }

   @Override
   public void onActivate() {
      this.canCancel = false;
      this.skip = 0;
   }

   @Override
   public void onDeactivate() {
      this.canCancel = false;
      this.skip = 0;
   }

   @Override
   public void onReceivePacket(Receive event) {
      Packet<?> packet = event.packet;
      if (packet instanceof ClientboundDamageEventPacket && ((ClientboundDamageEventPacket)packet).entityId() == this.mc.player.getId()) {
         this.canCancel = true;
      }

      if ((
            packet instanceof ClientboundSetEntityMotionPacket motionPacket && motionPacket.id() == this.mc.player.getId()
               || packet instanceof ClientboundExplodePacket
         )
         && this.canCancel) {
         this.skip = 6;
         event.cancel();
      }
   }

   @Override
   public void onSendPacket(Send event) {
      Packet<?> packet = event.packet;
      if (packet instanceof ServerboundMovePlayerPacket && this.skip > 0) {
         this.skip--;
         event.cancel();
      }
   }
}
