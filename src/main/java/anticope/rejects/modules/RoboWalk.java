package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixin.PlayerMoveC2SPacketAccessor;
import anticope.rejects.mixin.VehicleMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.phys.Vec3;

public class RoboWalk extends Module {
   public RoboWalk() {
      super(MeteorRejectsAddon.CATEGORY, "robo-walk", "Bypasses LiveOverflow movement check.");
   }

   private double smooth(double d) {
      double temp = Math.round(d * 100.0) / 100.0;
      return Math.nextAfter(temp, temp + Math.signum(d));
   }

   @EventHandler
   private void onPacketSend(Send event) {
      if (event.packet instanceof ServerboundMovePlayerPacket packet) {
         if (!packet.hasPosition()) {
            return;
         }

         double x = this.smooth(packet.getX(0.0));
         double z = this.smooth(packet.getZ(0.0));
         ((PlayerMoveC2SPacketAccessor)packet).setX(x);
         ((PlayerMoveC2SPacketAccessor)packet).setZ(z);
      } else if (event.packet instanceof ServerboundMoveVehiclePacket packet) {
         Vec3 pos = ((VehicleMoveC2SPacketAccessor)packet).getPosition();
         double x = this.smooth(pos.x());
         double z = this.smooth(pos.z());
         event.packet = VehicleMoveC2SPacketAccessor.create(new Vec3(x, pos.y(), z), packet.yRot(), packet.xRot(), packet.onGround());
      }
   }
}
