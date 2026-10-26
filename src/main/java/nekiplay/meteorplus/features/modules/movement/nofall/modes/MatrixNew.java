package nekiplay.meteorplus.features.modules.movement.nofall.modes;

import java.util.Iterator;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.mixin.ServerboundMovePlayerPacketAccessor;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallMode;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallModes;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MatrixNew extends NoFallMode {
   private Timer timer;

   public MatrixNew() {
      super(NoFallModes.Matrix_New);
   }

   @Override
   public void onDeactivate() {
      this.timer = (Timer)Modules.get().get(Timer.class);
      this.timer.setOverride(1.0);
   }

   @Override
   public void onSendPacket(Send event) {
      if (event.packet instanceof ServerboundMovePlayerPacket packet) {
         ServerboundMovePlayerPacketAccessor accessor = (ServerboundMovePlayerPacketAccessor)packet;
         this.timer = (Timer)Modules.get().get(Timer.class);
         if (!this.mc.player.onGround()) {
            if (this.mc.player.fallDistance > 2.69) {
               this.timer.setOverride(0.3);
               accessor.meteor$setOnGround(true);
               this.mc.player.fallDistance = 0.0;
            }

            if (this.mc.player.fallDistance > 3.5) {
               this.timer.setOverride(0.3);
            } else {
               this.timer.setOverride(1.0);
            }
         }

         Iterator<VoxelShape> voxelShapeIterator = this.mc
            .level
            .getCollisions(this.mc.player, this.mc.player.getBoundingBox().move(0.0, this.mc.player.getDeltaMovement().y, 0.0))
            .iterator();
         boolean isEmpty = true;

         while (voxelShapeIterator.hasNext()) {
            VoxelShape shape = voxelShapeIterator.next();
            isEmpty = shape.isEmpty();
         }

         if (!isEmpty && !((ServerboundMovePlayerPacket)event.packet).isOnGround() && this.mc.player.getDeltaMovement().y < -0.6) {
            accessor.meteor$setOnGround(true);
         }
      }
   }
}
