package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.BlockHitResult;
import zgoly.meteorist.Meteorist;

public class DoubleDoorsInteract extends Module {
   boolean isInteracting = false;

   public DoubleDoorsInteract() {
      super(Meteorist.CATEGORY, "double-doors-interact", "Open both doors with one interaction.");
   }

   @EventHandler
   private void onInteract(InteractBlockEvent event) {
      if (!this.isInteracting) {
         this.isInteracting = true;
         BlockPos doorPos = event.result.getBlockPos();
         BlockState blockState = this.mc.level.getBlockState(doorPos);
         if (blockState.getBlock() instanceof DoorBlock) {
            Direction doorFacing = (Direction)blockState.getValue(DoorBlock.FACING);
            DoorHingeSide doorHinge = (DoorHingeSide)blockState.getValue(DoorBlock.HINGE);
            BlockPos otherDoorPos;
            if (doorHinge == DoorHingeSide.LEFT) {
               otherDoorPos = doorPos.relative(doorFacing.getClockWise());
            } else {
               otherDoorPos = doorPos.relative(doorFacing.getCounterClockWise());
            }

            BlockState otherBlockState = this.mc.level.getBlockState(otherDoorPos);
            if (otherBlockState.getBlock() instanceof DoorBlock
               && blockState.getValue(DoorBlock.HALF) == otherBlockState.getValue(DoorBlock.HALF)
               && blockState.getValue(DoorBlock.HINGE) != otherBlockState.getValue(DoorBlock.HINGE)
               && blockState.getValue(DoorBlock.OPEN) == otherBlockState.getValue(DoorBlock.OPEN)) {
               BlockUtils.interact(new BlockHitResult(Utils.vec3(otherDoorPos), Direction.UP, otherDoorPos, false), InteractionHand.MAIN_HAND, false);
            }
         }

         this.isInteracting = false;
      }
   }
}
