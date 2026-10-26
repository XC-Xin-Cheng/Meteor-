package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoulSandBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoFarm extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgTill = this.settings.createGroup("Till");
   private final SettingGroup sgHarvest = this.settings.createGroup("Harvest");
   private final SettingGroup sgPlant = this.settings.createGroup("Plant");
   private final SettingGroup sgBonemeal = this.settings.createGroup("Bonemeal");
   private final Map<BlockPos, Item> replantMap = new HashMap<>();
   private final Setting<Integer> range = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("range")).description("Auto farm range.")).defaultValue(4)).min(1).build());
   private final Setting<Integer> bpt = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("blocks-per-tick")).description("Amount of operations that can be applied in one tick."))
               .min(1)
               .defaultValue(1))
            .build()
      );
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate"))
                  .description("Whether or not to rotate towards block."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> till = this.sgTill
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("till"))
                  .description("Turn nearby dirt into farmland."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> moist = this.sgTill
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("moist"))
                  .description("Only till moist blocks."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> harvest = this.sgHarvest
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("harvest"))
                  .description("Harvest crops."))
               .defaultValue(true))
            .build()
      );
   private final Setting<List<Block>> harvestBlocks = this.sgHarvest
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                  .name("harvest-blocks"))
               .description("Which crops to harvest."))
            .defaultValue(new Block[0])
            .filter(this::harvestFilter)
            .build()
      );
   private final Setting<Boolean> plant = this.sgPlant
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("plant"))
                  .description("Plant crops."))
               .defaultValue(true))
            .build()
      );
   private final Setting<List<Item>> plantItems = this.sgPlant
      .add(
         ((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)new meteordevelopment.meteorclient.settings.ItemListSetting.Builder()
                  .name("plant-items"))
               .description("Which crops to plant."))
            .defaultValue(new Item[0])
            .filter(this::plantFilter)
            .build()
      );
   private final Setting<Boolean> onlyReplant = this.sgPlant
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("only-replant"))
                     .description("Only replant planted crops."))
                  .defaultValue(true))
               .onChanged(b -> this.replantMap.clear()))
            .build()
      );
   private final Setting<Boolean> bonemeal = this.sgBonemeal
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("bonemeal"))
                  .description("Bonemeal crops."))
               .defaultValue(true))
            .build()
      );
   private final Setting<List<Block>> bonemealBlocks = this.sgBonemeal
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                  .name("bonemeal-blocks"))
               .description("Which crops to bonemeal."))
            .defaultValue(new Block[0])
            .filter(this::bonemealFilter)
            .build()
      );
   private final Pool<MutableBlockPos> blockPosPool = new Pool(MutableBlockPos::new);
   private final List<MutableBlockPos> blocks = new ArrayList<>();
   int actions = 0;

   public AutoFarm() {
      super(MeteorRejectsAddon.CATEGORY, "auto-farm", "All-in-one farm utility.");
   }

   public void onDeactivate() {
      this.replantMap.clear();
   }

   @EventHandler
   private void onBreakBlock(BreakBlockEvent event) {
      BlockState state = this.mc.level.getBlockState(event.blockPos);
      Block block = state.getBlock();
      if ((Boolean)this.onlyReplant.get()) {
         Item item = null;
         if (block == Blocks.WHEAT) {
            item = Items.WHEAT_SEEDS;
         } else if (block == Blocks.CARROTS) {
            item = Items.CARROT;
         } else if (block == Blocks.POTATOES) {
            item = Items.POTATO;
         } else if (block == Blocks.BEETROOTS) {
            item = Items.BEETROOT_SEEDS;
         } else if (block == Blocks.NETHER_WART) {
            item = Items.NETHER_WART;
         } else if (block == Blocks.PITCHER_CROP) {
            item = Items.PITCHER_POD;
         } else if (block == Blocks.TORCHFLOWER) {
            item = Items.TORCHFLOWER_SEEDS;
         }

         if (item != null) {
            this.replantMap.put(event.blockPos, item);
         }
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      this.actions = 0;
      BlockIterator.register((Integer)this.range.get(), (Integer)this.range.get(), (pos, state) -> {
         if (this.mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(pos)) <= ((Integer)this.range.get()).intValue()) {
            this.blocks.add(((MutableBlockPos)this.blockPosPool.get()).set(pos));
         }
      });
      BlockIterator.after(() -> {
         this.blocks.sort(Comparator.comparingDouble(value -> this.mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(value))));

         for (BlockPos pos : this.blocks) {
            BlockState state = this.mc.level.getBlockState(pos);
            Block block = state.getBlock();
            if (this.till(pos, block) || this.harvest(pos, state, block) || this.plant(pos, block) || this.bonemeal(pos, state, block)) {
               this.actions++;
            }

            if (this.actions >= (Integer)this.bpt.get()) {
               break;
            }
         }

         for (MutableBlockPos blockPos : this.blocks) {
            this.blockPosPool.free(blockPos);
         }

         this.blocks.clear();
      });
   }

   private boolean till(BlockPos pos, Block block) {
      if (!(Boolean)this.till.get()) {
         return false;
      } else {
         boolean moist = !(Boolean)this.moist.get() || this.isWaterNearby(this.mc.level, pos);
         boolean tillable = block == Blocks.GRASS_BLOCK
            || block == Blocks.DIRT_PATH
            || block == Blocks.DIRT
            || block == Blocks.COARSE_DIRT
            || block == Blocks.ROOTED_DIRT;
         if (moist && tillable && this.mc.level.getBlockState(pos.above()).isAir()) {
            FindItemResult hoe = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof HoeItem);
            return WorldUtils.interact(pos, hoe, (Boolean)this.rotate.get());
         } else {
            return false;
         }
      }
   }

   private boolean harvest(BlockPos pos, BlockState state, Block block) {
      if (!(Boolean)this.harvest.get()) {
         return false;
      } else if (!((List)this.harvestBlocks.get()).contains(block)) {
         return false;
      } else if (!this.isMature(state, block)) {
         return false;
      } else {
         if (block instanceof SweetBerryBushBlock) {
            this.mc.gameMode.useItemOn(this.mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Utils.vec3(pos), Direction.UP, pos, false));
         } else {
            BlockUtils.breakBlock(pos, true);
         }

         return true;
      }
   }

   private boolean plant(BlockPos pos, Block block) {
      if (!(Boolean)this.plant.get()) {
         return false;
      } else if (!this.mc.level.isEmptyBlock(pos.above())) {
         return false;
      } else {
         FindItemResult findItemResult = null;
         if ((Boolean)this.onlyReplant.get()) {
            for (BlockPos replantPos : this.replantMap.keySet()) {
               if (replantPos.equals(pos.above())) {
                  findItemResult = InvUtils.find(new Item[]{this.replantMap.get(replantPos)});
                  this.replantMap.remove(replantPos);
                  break;
               }
            }
         } else if (block instanceof FarmlandBlock) {
            findItemResult = InvUtils.find(itemStack -> {
               Item item = itemStack.getItem();
               return item != Items.NETHER_WART && ((List)this.plantItems.get()).contains(item);
            });
         } else if (block instanceof SoulSandBlock) {
            findItemResult = InvUtils.find(itemStack -> {
               Item item = itemStack.getItem();
               return item == Items.NETHER_WART && ((List)this.plantItems.get()).contains(Items.NETHER_WART);
            });
         }

         if (findItemResult != null && findItemResult.found()) {
            BlockUtils.place(pos.above(), findItemResult, (Boolean)this.rotate.get(), -100, false);
            return true;
         } else {
            return false;
         }
      }
   }

   private boolean bonemeal(BlockPos pos, BlockState state, Block block) {
      if (!(Boolean)this.bonemeal.get()) {
         return false;
      } else if (!((List)this.bonemealBlocks.get()).contains(block)) {
         return false;
      } else if (this.isMature(state, block)) {
         return false;
      } else {
         FindItemResult bonemeal = InvUtils.findInHotbar(new Item[]{Items.BONE_MEAL});
         return WorldUtils.interact(pos, bonemeal, (Boolean)this.rotate.get());
      }
   }

   private boolean isWaterNearby(LevelReader world, BlockPos pos) {
      for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
         if (world.getFluidState(blockPos).is(FluidTags.WATER)) {
            return true;
         }
      }

      return false;
   }

   private boolean isMature(BlockState state, Block block) {
      if (block instanceof CropBlock cropBlock) {
         return cropBlock.isMaxAge(state);
      } else if (block instanceof CocoaBlock cocoaBlock) {
         return (Integer)state.getValue(CocoaBlock.AGE) >= 2;
      } else if (block instanceof StemBlock) {
         return (Integer)state.getValue(StemBlock.AGE) == 7;
      } else if (block instanceof SweetBerryBushBlock sweetBerryBushBlock) {
         return (Integer)state.getValue(SweetBerryBushBlock.AGE) >= 2;
      } else if (block instanceof NetherWartBlock netherWartBlock) {
         return (Integer)state.getValue(NetherWartBlock.AGE) >= 3;
      } else {
         return block instanceof PitcherCropBlock pitcherCropBlock ? (Integer)state.getValue(PitcherCropBlock.AGE) >= 4 : !(block instanceof SaplingBlock);
      }
   }

   private boolean bonemealFilter(Block block) {
      return block instanceof CropBlock
         || block instanceof StemBlock
         || block instanceof MushroomBlock
         || block instanceof AzaleaBlock
         || block instanceof SaplingBlock
         || block == Blocks.COCOA
         || block == Blocks.SWEET_BERRY_BUSH
         || block == Blocks.PITCHER_CROP
         || block == Blocks.TORCHFLOWER;
   }

   private boolean harvestFilter(Block block) {
      return block instanceof CropBlock
         || block == Blocks.PUMPKIN
         || block == Blocks.MELON
         || block == Blocks.NETHER_WART
         || block == Blocks.SWEET_BERRY_BUSH
         || block == Blocks.COCOA
         || block == Blocks.PITCHER_CROP
         || block == Blocks.TORCHFLOWER;
   }

   private boolean plantFilter(Item item) {
      return item == Items.WHEAT_SEEDS
         || item == Items.CARROT
         || item == Items.POTATO
         || item == Items.BEETROOT_SEEDS
         || item == Items.PUMPKIN_SEEDS
         || item == Items.MELON_SEEDS
         || item == Items.NETHER_WART
         || item == Items.PITCHER_POD
         || item == Items.TORCHFLOWER_SEEDS;
   }
}
