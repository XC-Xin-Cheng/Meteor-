package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

public class MossBot extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> range = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("range")).description("The bonemeal range.")).defaultValue(4)).min(0).build());
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate"))
                  .description("Whether or not to rotate towards block while bonemealing."))
               .defaultValue(true))
            .build()
      );
   private final Map<BlockPos, Integer> mossMap = new HashMap<>();

   public MossBot() {
      super(MeteorRejectsAddon.CATEGORY, "moss-bot", "Use bonemeal on moss blocks with maximized efficiency.");
   }

   @EventHandler
   private void onTick(Pre event) {
      this.mossMap.entrySet().removeIf(e -> e.setValue(e.getValue() - 1) == 0);
      FindItemResult findItemResult = InvUtils.findInHotbar(new Item[]{Items.BONE_MEAL});
      if (findItemResult.found()) {
         BlockPos bestBlock = BlockPos.withinManhattanStream(
               BlockPos.containing(this.mc.player.getEyePosition()), (Integer)this.range.get(), (Integer)this.range.get(), (Integer)this.range.get()
            )
            .filter(
               b -> this.mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(b)) <= ((Integer)this.range.get()).intValue() && !this.mossMap.containsKey(b)
            )
            .map(b -> Pair.of(b.immutable(), this.getMossSpots(b)))
            .filter(p -> (Integer)p.getRight() > 10)
            .<BlockPos>map(Pair::getLeft)
            .max(Comparator.naturalOrder())
            .orElse(null);
         if (bestBlock != null) {
            if (!this.mc.level.isEmptyBlock(bestBlock.above())) {
               this.mc.gameMode.continueDestroyBlock(bestBlock.above(), Direction.UP);
            }

            WorldUtils.interact(bestBlock, findItemResult, (Boolean)this.rotate.get());
            this.mossMap.put(bestBlock, 100);
         }
      }
   }

   private int getMossSpots(BlockPos pos) {
      Block block = this.mc.level.getBlockState(pos).getBlock();
      return (block == Blocks.MOSS_BLOCK || block == Blocks.PALE_MOSS_BLOCK)
            && this.mc.level.getBlockState(pos.above()).getDestroySpeed(this.mc.level, pos) == 0.0F
         ? (int)BlockPos.withinManhattanStream(pos, 3, 4, 3)
            .filter(b -> this.isMossGrowableOn(this.mc.level.getBlockState(b)) && this.mc.level.isEmptyBlock(b.above()))
            .count()
         : 0;
   }

   private boolean isMossGrowableOn(BlockState state) {
      return state.is(BlockTags.MOSS_REPLACEABLE);
   }
}
