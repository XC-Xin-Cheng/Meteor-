package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BlockSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Painter extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Block> block = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("block")).description("Block to use for painting")).defaultValue(Blocks.STONE_BUTTON)).build());
   private final Setting<Integer> range = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("range"))
                  .description("Range of placement"))
               .min(0)
               .defaultValue(0))
            .build()
      );
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay"))
                  .description("Delay between block placement (in ticks)"))
               .min(0)
               .defaultValue(0))
            .build()
      );
   private final Setting<Integer> bpt = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("blocks-per-tick"))
                  .description("Amount of blocks that can be placed in one tick"))
               .min(1)
               .defaultValue(1))
            .build()
      );
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate"))
                  .description("Whether or not to rotate towards block while placing"))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> topSurfaces = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("top-surface"))
                  .description("Whether or not to cover top surfaces"))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> sideSurfaces = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("side-surface"))
                  .description("Whether or not to cover side surfaces"))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> bottomSurfaces = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("bottom-surface"))
                  .description("Whether or not to cover bottom surfaces"))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> oneBlockHeight = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("one-block-height"))
                  .description("Whether or not to cover in one block high gaps"))
               .defaultValue(true))
            .build()
      );
   private int ticksWaited;

   public Painter() {
      super(MeteorRejectsAddon.CATEGORY, "painter", "Automatically paints/covers surfaces (good for trolling)");
   }

   @EventHandler
   private void onTick(Post event) {
      if ((Integer)this.delay.get() != 0 && this.ticksWaited < (Integer)this.delay.get() - 1) {
         this.ticksWaited++;
      } else {
         this.ticksWaited = 0;
         FindItemResult findItemResult = InvUtils.findInHotbar(itemStack -> this.block.get() == Block.byItem(itemStack.getItem()));
         if (!findItemResult.found()) {
            this.error("No selected blocks in hotbar", new Object[0]);
            this.toggle();
         } else {
            int placed = 0;

            for (BlockPos blockPos : WorldUtils.getSphere(this.mc.player.blockPosition(), (Integer)this.range.get(), (Integer)this.range.get())) {
               if (this.shouldPlace(blockPos, (Block)this.block.get())) {
                  BlockUtils.place(blockPos, findItemResult, (Boolean)this.rotate.get(), -100, false);
                  if ((Integer)this.delay.get() != 0 && ++placed >= (Integer)this.bpt.get()) {
                     break;
                  }
               }
            }
         }
      }
   }

   private boolean shouldPlace(BlockPos blockPos, Block useBlock) {
      if (!this.mc.level.getBlockState(blockPos).canBeReplaced()) {
         return false;
      } else if (!(Boolean)this.oneBlockHeight.get()
         && !this.mc.level.getBlockState(blockPos.above()).canBeReplaced()
         && !this.mc.level.getBlockState(blockPos.below()).canBeReplaced()) {
         return false;
      } else {
         boolean north = true;
         boolean south = true;
         boolean east = true;
         boolean west = true;
         boolean up = true;
         boolean bottom = true;
         BlockState northState = this.mc.level.getBlockState(blockPos.north());
         BlockState southState = this.mc.level.getBlockState(blockPos.south());
         BlockState eastState = this.mc.level.getBlockState(blockPos.east());
         BlockState westState = this.mc.level.getBlockState(blockPos.west());
         BlockState upState = this.mc.level.getBlockState(blockPos.above());
         BlockState bottomState = this.mc.level.getBlockState(blockPos.below());
         if ((Boolean)this.topSurfaces.get() && (upState.canBeReplaced() || upState.getBlock() == useBlock)) {
            up = false;
         }

         if ((Boolean)this.sideSurfaces.get()) {
            if (northState.canBeReplaced() || northState.getBlock() == useBlock) {
               north = false;
            }

            if (southState.canBeReplaced() || southState.getBlock() == useBlock) {
               south = false;
            }

            if (eastState.canBeReplaced() || eastState.getBlock() == useBlock) {
               east = false;
            }

            if (westState.canBeReplaced() || westState.getBlock() == useBlock) {
               west = false;
            }
         }

         if ((Boolean)this.bottomSurfaces.get() && (bottomState.canBeReplaced() || bottomState.getBlock() == useBlock)) {
            bottom = false;
         }

         return north || south || east || west || up || bottom;
      }
   }
}
