package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.CardinalDirection;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.phys.BlockHitResult;

public class TreeAura extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> rotation = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("rotate")).description("rotate for block interactions")).defaultValue(false)).build());
   private final Setting<Integer> plantDelay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("plant-delay"))
                  .description("delay between planting trees"))
               .defaultValue(6))
            .min(0)
            .sliderMax(25)
            .build()
      );
   private final Setting<Integer> bonemealDelay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("bonemeal-delay"))
                  .description("delay between placing bonemeal on trees"))
               .defaultValue(3))
            .min(0)
            .sliderMax(25)
            .build()
      );
   private final Setting<Integer> rRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("radius"))
                  .description("how far you can place horizontally"))
               .defaultValue(4))
            .min(1)
            .sliderMax(5)
            .build()
      );
   private final Setting<Integer> yRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("y-range"))
                  .description("how far you can place vertically"))
               .defaultValue(3))
            .min(1)
            .sliderMax(5)
            .build()
      );
   private final Setting<TreeAura.SortMode> sortMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("sort-mode"))
                  .description("how to sort nearby trees/placements."))
               .defaultValue(TreeAura.SortMode.Farthest))
            .build()
      );
   private int bonemealTimer;
   private int plantTimer;

   public TreeAura() {
      super(MeteorRejectsAddon.CATEGORY, "tree-aura", "Plants trees around you");
   }

   public void onActivate() {
      this.bonemealTimer = 0;
      this.plantTimer = 0;
   }

   @EventHandler
   private void onTick(Post event) {
      this.plantTimer--;
      this.bonemealTimer--;
      if (this.plantTimer <= 0) {
         BlockPos plantPos = this.findPlantLocation();
         if (plantPos == null) {
            return;
         }

         this.doPlant(plantPos);
         this.plantTimer = (Integer)this.plantDelay.get();
      }

      if (this.bonemealTimer <= 0) {
         BlockPos p = this.findPlantedSapling();
         if (p == null) {
            return;
         }

         this.doBonemeal(p);
         this.bonemealTimer = (Integer)this.bonemealDelay.get();
      }
   }

   private FindItemResult findBonemeal() {
      return InvUtils.findInHotbar(new Item[]{Items.BONE_MEAL});
   }

   private FindItemResult findSapling() {
      return InvUtils.findInHotbar(itemStack -> Block.byItem(itemStack.getItem()) instanceof SaplingBlock);
   }

   private boolean isSapling(BlockPos pos) {
      return this.mc.level.getBlockState(pos).getBlock() instanceof SaplingBlock;
   }

   private void doPlant(BlockPos plantPos) {
      FindItemResult sapling = this.findSapling();
      if (!sapling.found()) {
         this.error("No saplings in hotbar", new Object[0]);
         this.toggle();
      } else {
         InvUtils.swap(sapling.slot(), false);
         if ((Boolean)this.rotation.get()) {
            Rotations.rotate(
               Rotations.getYaw(plantPos),
               Rotations.getPitch(plantPos),
               () -> this.mc
                  .player
                  .connection
                  .send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Utils.vec3(plantPos), Direction.UP, plantPos, false), 0))
            );
         } else {
            this.mc
               .player
               .connection
               .send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Utils.vec3(plantPos), Direction.UP, plantPos, false), 0));
         }
      }
   }

   private void doBonemeal(BlockPos sapling) {
      FindItemResult bonemeal = this.findBonemeal();
      if (!bonemeal.found()) {
         this.error("No bonemeal in hotbar", new Object[0]);
         this.toggle();
      } else {
         InvUtils.swap(bonemeal.slot(), false);
         if ((Boolean)this.rotation.get()) {
            Rotations.rotate(
               Rotations.getYaw(sapling),
               Rotations.getPitch(sapling),
               () -> this.mc
                  .player
                  .connection
                  .send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Utils.vec3(sapling), Direction.UP, sapling, false), 0))
            );
         } else {
            this.mc
               .player
               .connection
               .send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Utils.vec3(sapling), Direction.UP, sapling, false), 0));
         }
      }
   }

   private boolean canPlant(BlockPos pos) {
      Block b = this.mc.level.getBlockState(pos).getBlock();
      if (!b.equals(Blocks.SHORT_GRASS) && !b.equals(Blocks.GRASS_BLOCK) && !b.equals(Blocks.DIRT) && !b.equals(Blocks.COARSE_DIRT)) {
         return false;
      } else {
         AtomicBoolean plant = new AtomicBoolean(true);
         IntStream.rangeClosed(1, 5).forEach(i -> {
            BlockPos check = pos.above(i);
            if (!this.mc.level.getBlockState(check).getBlock().equals(Blocks.AIR)) {
               plant.set(false);
            } else {
               for (CardinalDirection dir : CardinalDirection.values()) {
                  if (!this.mc.level.getBlockState(check.relative(dir.toDirection(), i)).getBlock().equals(Blocks.AIR)) {
                     plant.set(false);
                     return;
                  }
               }
            }
         });
         return plant.get();
      }
   }

   private List<BlockPos> findSaplings(BlockPos centerPos, int radius, int height) {
      ArrayList<BlockPos> blocc = new ArrayList<>();

      for (BlockPos b : WorldUtils.getSphere(centerPos, radius, height)) {
         if (this.isSapling(b)) {
            blocc.add(b);
         }
      }

      return blocc;
   }

   private BlockPos findPlantedSapling() {
      List<BlockPos> saplings = this.findSaplings(this.mc.player.blockPosition(), (Integer)this.rRange.get(), (Integer)this.yRange.get());
      if (saplings.isEmpty()) {
         return null;
      } else {
         saplings.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
         if (((TreeAura.SortMode)this.sortMode.get()).equals(TreeAura.SortMode.Farthest)) {
            Collections.reverse(saplings);
         }

         return saplings.get(0);
      }
   }

   private List<BlockPos> getPlantLocations(BlockPos centerPos, int radius, int height) {
      ArrayList<BlockPos> blocc = new ArrayList<>();

      for (BlockPos b : WorldUtils.getSphere(centerPos, radius, height)) {
         if (this.canPlant(b)) {
            blocc.add(b);
         }
      }

      return blocc;
   }

   private BlockPos findPlantLocation() {
      List<BlockPos> nearby = this.getPlantLocations(this.mc.player.blockPosition(), (Integer)this.rRange.get(), (Integer)this.yRange.get());
      if (nearby.isEmpty()) {
         return null;
      } else {
         nearby.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
         if (((TreeAura.SortMode)this.sortMode.get()).equals(TreeAura.SortMode.Farthest)) {
            Collections.reverse(nearby);
         }

         return nearby.get(0);
      }
   }

   public static enum SortMode {
      Closest,
      Farthest;
   }
}
