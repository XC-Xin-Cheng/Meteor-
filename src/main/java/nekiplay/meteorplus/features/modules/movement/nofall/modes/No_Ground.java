package nekiplay.meteorplus.features.modules.movement.nofall.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.mixininterface.IServerboundMovePlayerPacket;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallMode;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallModes;
import nekiplay.meteorplus.mixin.minecraft.entity.ServerboundMovePlayerPacketAccessor;

public class No_Ground extends NoFallMode {
   public No_Ground() {
      super(NoFallModes.No_Ground);
   }

   @Override
   public void onSendPacket(Send event) {
      if (event.packet instanceof IServerboundMovePlayerPacket move) {
         ServerboundMovePlayerPacketAccessor move2 = (ServerboundMovePlayerPacketAccessor)move;
         if (move2.getOnGround()) {
            move2.setOnGround(false);
         }
      }
   }
}
