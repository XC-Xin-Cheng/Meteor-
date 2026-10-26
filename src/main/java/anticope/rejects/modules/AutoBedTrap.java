package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class AutoBedTrap extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> bpt = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("blocks-per-tick")).description("How many blocks to place per tick")).defaultValue(2))
            .min(1)
            .sliderMax(8)
            .build()
      );
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate"))
                  .description("Rotates when placing"))
               .defaultValue(true))
            .build()
      );
   private final Setting<Double> range = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("range"))
               .description("The break range."))
            .defaultValue(4.0)
            .min(0.0)
            .build()
      );
   private final Setting<List<Block>> blockTypes = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                     .name("blocks"))
                  .description("The blocks you bedtrap with."))
               .defaultValue(Arrays.asList(Blocks.OBSIDIAN)))
            .build()
      );
   private final Pool<MutableBlockPos> blockPosPool = new Pool(MutableBlockPos::new);
   private final List<MutableBlockPos> blocks = new ArrayList<>();
   int cap = 0;

   public AutoBedTrap() {
      super(MeteorRejectsAddon.CATEGORY, "auto-bed-trap", "Automatically places obsidian around beds");
   }

   public void onDeactivate() {
      for (MutableBlockPos blockPos : this.blocks) {
         this.blockPosPool.free(blockPos);
      }

      this.blocks.clear();
   }

   @EventHandler
   private void onTick(Pre event) {
      BlockIterator.register((int)Math.ceil((Double)this.range.get()), (int)Math.ceil((Double)this.range.get()), (blockPos, blockState) -> {
         if (BlockUtils.canBreak(blockPos, blockState)) {
            if (blockState.getBlock() instanceof BedBlock) {
               this.blocks.add(((MutableBlockPos)this.blockPosPool.get()).set(blockPos));
            }
         }
      });
   }

   @EventHandler
   private void onTickPost(Post event) {
      boolean noBlocks = false;

      for (BlockPos blockPos : this.blocks) {
         if (!this.placeTickAround(blockPos)) {
            noBlocks = true;
            break;
         }
      }

      if (noBlocks && this.isActive()) {
         this.toggle();
      }
   }

   public boolean placeTickAround(BlockPos block) {
      for (BlockPos b : new BlockPos[]{block.above(), block.west(), block.north(), block.south(), block.east(), block.below()}) {
         if (this.cap >= (Integer)this.bpt.get()) {
            this.cap = 0;
            return true;
         }

         if (((List)this.blockTypes.get()).contains(this.mc.level.getBlockState(b).getBlock())) {
            return true;
         }

         FindItemResult findBlock = InvUtils.findInHotbar(item -> {
            if (!(item.getItem() instanceof BlockItem)) {
               return false;
            } else {
               BlockItem bitem = (BlockItem)item.getItem();
               return ((List)this.blockTypes.get()).contains(bitem.getBlock());
            }
         });
         if (!findBlock.found()) {
            this.error("No specified blocks found. Disabling.", new Object[0]);
            return false;
         }

         if (BlockUtils.place(b, findBlock, (Boolean)this.rotate.get(), 10, false)) {
            this.cap++;
            if (this.cap >= (Integer)this.bpt.get()) {
               return true;
            }
         }
      }

      this.cap = 0;
      return true;
   }
}
