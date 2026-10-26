package nekiplay.meteorplus.features.modules.movement.nofall.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.mixin.ServerboundMovePlayerPacketAccessor;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallMode;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallModes;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class Verus extends NoFallMode {
   public Verus() {
      super(NoFallModes.Verus);
   }

   @Override
   public void onSendPacket(Send event) {
      if (event.packet instanceof ServerboundMovePlayerPacket packet) {
         ServerboundMovePlayerPacketAccessor accessor = (ServerboundMovePlayerPacketAccessor)packet;
         if (this.mc.player.fallDistance > 3.35) {
            accessor.meteor$setOnGround(true);
            this.mc.player.fallDistance = 0.0;
            Vec3 vel = this.mc.player.getDeltaMovement();
            this.mc.player.setDeltaMovement(vel.x, 0.0, vel.z);
         }
      }
   }
}
