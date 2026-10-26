package nekiplay.meteorplus.features.modules.combat.velocity.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import nekiplay.meteorplus.features.modules.combat.velocity.VelocityMode;
import nekiplay.meteorplus.features.modules.combat.velocity.VelocityModes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;

public class GrimCancel extends VelocityMode {
   private boolean canCancel = false;

   public GrimCancel() {
      super(VelocityModes.Grim_Cancel);
   }

   @Override
   public void onActivate() {
      this.canCancel = false;
   }

   @Override
   public void onDeactivate() {
      this.canCancel = false;
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
         event.cancel();
         MeteorExecutor.execute(
            () -> {
               try {
                  Thread.sleep(20L);
               } catch (Exception var2x) {
               }

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
               this.mc
                  .getConnection()
                  .send(
                     new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, this.mc.player.blockPosition(), this.mc.player.getDirection().getOpposite())
                  );
               this.canCancel = false;
            }
         );
      }
   }
}
