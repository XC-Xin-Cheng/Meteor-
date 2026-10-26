package zgoly.meteorist.modules;

import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BlockListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.mixin.TrapdoorBlockAccessor;

public class AutoInteract extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<List<Block>> blocks = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("blocks")).description("The block to interact with."))
               .filter(
                  block -> block instanceof DoorBlock
                     || block instanceof FenceGateBlock
                     || block instanceof TrapDoorBlock
                     || block instanceof ButtonBlock
                     || block instanceof LeverBlock
               )
               .defaultValue(BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof DoorBlock || block instanceof FenceGateBlock).toList()))
            .build()
      );
   private final Setting<Double> innerRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("inner-range"))
               .description("The range to interact with blocks."))
            .defaultValue(3.0)
            .min(0.0)
            .build()
      );
   private final Setting<Integer> outerRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("outer-range"))
                  .description("The range to stop interacting with blocks."))
               .defaultValue(4))
            .min(1)
            .build()
      );
   private final Setting<BlockPos> rangePosOffset = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                     .name("range-pos-offset"))
                  .description("The offset of the range position."))
               .defaultValue(new BlockPos(0, 1, 0)))
            .build()
      );
   private final Setting<Boolean> swingHand = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("swing-hand"))
                  .description("Swing hand client-side."))
               .defaultValue(true))
            .build()
      );

   public AutoInteract() {
      super(Meteorist.CATEGORY, "auto-interact", "Automatically interacts with interactable blocks like doors, trapdoors, etc.");
   }

   @EventHandler
   private void onTick(Post event) {
      for (BlockPos blockPos : BlockPos.withinManhattan(
         this.mc.player.blockPosition().offset((Vec3i)this.rangePosOffset.get()),
         (Integer)this.outerRange.get(),
         (Integer)this.outerRange.get(),
         (Integer)this.outerRange.get()
      )) {
         BlockState blockState = this.mc.level.getBlockState(blockPos);
         if ((!(blockState.getBlock() instanceof DoorBlock) || blockState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER)
            && !(blockState.getBlock() instanceof DoorBlock doorBlock && !doorBlock.type().canOpenByHand())
            && !(
               blockState.getBlock() instanceof TrapDoorBlock trapdoorBlock && !((TrapdoorBlockAccessor)trapdoorBlock).invokeGetBlockSetType().canOpenByHand()
            )
            && ((List)this.blocks.get()).contains(blockState.getBlock())) {
            boolean shouldOpen = PlayerUtils.distanceTo(blockPos.getCenter()) <= (Double)this.innerRange.get();

            boolean isOpen = switch (blockState.getBlock()) {
               case DoorBlock ignored -> blockState.getValue(DoorBlock.OPEN);
               case FenceGateBlock ignoredx -> blockState.getValue(FenceGateBlock.OPEN);
               case TrapDoorBlock ignoredxx -> blockState.getValue(TrapDoorBlock.OPEN);
               case ButtonBlock ignoredxxx -> blockState.getValue(ButtonBlock.POWERED);
               case LeverBlock ignoredxxxx -> blockState.getValue(LeverBlock.POWERED);
               default -> false;
            };
            if (shouldOpen != isOpen) {
               BlockUtils.interact(
                  new BlockHitResult(Utils.vec3(blockPos), Direction.UP, blockPos, false), InteractionHand.MAIN_HAND, (Boolean)this.swingHand.get()
               );
               break;
            }
         }
      }
   }
}
