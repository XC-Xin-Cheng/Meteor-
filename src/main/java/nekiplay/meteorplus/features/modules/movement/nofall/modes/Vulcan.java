package nekiplay.meteorplus.features.modules.movement.nofall.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixin.ServerboundMovePlayerPacketAccessor;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallMode;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallModes;
import nekiplay.meteorplus.utils.MovementUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class Vulcan extends NoFallMode {
   private boolean vulCanNoFall = false;
   private boolean vulCantNoFall = false;
   private boolean nextSpoof = false;
   private boolean doSpoof = false;

   public Vulcan() {
      super(NoFallModes.Vulcan);
   }

   @Override
   public void onActivate() {
      this.vulCanNoFall = false;
      this.vulCantNoFall = false;
      this.nextSpoof = false;
      this.doSpoof = false;
   }

   @Override
   public void onTickEventPre(Pre event) {
      if (!this.vulCanNoFall && this.mc.player.fallDistance > 3.25) {
         this.vulCanNoFall = true;
      }

      if (this.vulCanNoFall && this.mc.player.onGround() && this.vulCantNoFall) {
         this.vulCantNoFall = false;
      }

      if (!this.vulCantNoFall) {
         if (this.nextSpoof) {
            this.mc.player.getDeltaMovement().add(0.0, -0.1, 0.0);
            this.mc.player.fallDistance = -0.1F;
            MovementUtils.strafe(0.3F);
            this.nextSpoof = false;
         }

         if (this.mc.player.fallDistance > 3.5625) {
            this.mc.player.fallDistance = 0.0;
            this.doSpoof = true;
            this.nextSpoof = true;
         }
      }
   }

   @Override
   public void onSendPacket(Send event) {
      if (event.packet instanceof ServerboundMovePlayerPacket packet) {
         ServerboundMovePlayerPacketAccessor accessor = (ServerboundMovePlayerPacketAccessor)packet;
         accessor.meteor$setOnGround(true);
         this.doSpoof = false;
         accessor.meteor$setY(Math.round(this.mc.player.position().y * 2.0) / 2.0);
         this.mc
            .player
            .setPos(this.mc.player.position().x, ((ServerboundMovePlayerPacket)event.packet).getY(this.mc.player.position().y), this.mc.player.position().z);
      }
   }
}
