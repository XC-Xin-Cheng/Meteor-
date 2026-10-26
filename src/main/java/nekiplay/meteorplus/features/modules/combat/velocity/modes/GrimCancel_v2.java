package nekiplay.meteorplus.features.modules.combat.velocity.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import nekiplay.meteorplus.features.modules.combat.velocity.VelocityMode;
import nekiplay.meteorplus.features.modules.combat.velocity.VelocityModes;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;

public class GrimCancel_v2 extends VelocityMode {
   private int skip = 0;
   private boolean canCancel = false;

   public GrimCancel_v2() {
      super(VelocityModes.Grim_Cancel_v2);
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
      if ((
            packet instanceof ClientboundSetEntityMotionPacket motionPacket && motionPacket.id() == this.mc.player.getId()
               || packet instanceof ClientboundExplodePacket
         )
         && this.canCancel) {
         event.cancel();
         this.canCancel = true;
      } else if (packet instanceof ClientboundPlayerPositionPacket) {
         this.skip = 3;
      }
   }

   @Override
   public void onSendPacket(Send event) {
      Packet<?> packet = event.packet;
      if (packet instanceof ServerboundMovePlayerPacket) {
         this.skip--;
         if (this.canCancel && this.skip <= 0) {
            this.mc
               .getConnection()
               .send(
                  new PosRot(
                     this.mc.player.getX(),
                     this.mc.player.getY(),
                     this.mc.player.getZ(),
                     this.mc.player.getYRot(),
                     this.mc.player.getXRot(),
                     this.mc.player.onGround(),
                     this.mc.player.horizontalCollision
                  )
               );
            this.mc.getConnection().send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, this.mc.player.blockPosition(), Direction.UP));
         }
      }
   }
}
