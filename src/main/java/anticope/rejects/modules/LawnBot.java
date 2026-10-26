package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BlockListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LawnBot extends Module {
   private final ArrayList<BlockPos> myceliumSpots = new ArrayList<>();
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<List<Block>> blockWhitelist = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("block-whitelist")).description("Which blocks to replace with grass."))
            .defaultValue(new Block[0])
            .filter(this::grassFilter)
            .build()
      );
   private final Setting<Integer> range = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("range"))
                  .description("The range to search for blocks to replace."))
               .defaultValue(5))
            .min(1)
            .sliderMax(10)
            .build()
      );
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay"))
                  .description("The delay between block placements in ticks."))
               .defaultValue(0))
            .min(0)
            .sliderMax(20)
            .build()
      );
   private final Setting<Boolean> useShovel = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("use-shovel"))
                  .description("Automatically switch to shovel when breaking blocks."))
               .defaultValue(true))
            .build()
      );
   private int tickCounter = 0;

   public LawnBot() {
      super(MeteorRejectsAddon.CATEGORY, "lawnbot", "Replace a variety of dirt-type blocks with grass");
   }

   @EventHandler
   private void onTick(Post event) {
      if ((Integer)this.delay.get() > 0) {
         if (this.tickCounter < (Integer)this.delay.get()) {
            this.tickCounter++;
            return;
         }

         this.tickCounter = 0;
      }

      Item grassBlockItem = Items.GRASS_BLOCK;
      int grassCount = InvUtils.find(new Item[]{grassBlockItem}).count();
      if (grassCount != 0) {
         int grassHotbarSlot = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == grassBlockItem).slot();
         if (grassHotbarSlot == -1) {
            int grassInvSlot = InvUtils.find(itemStack -> itemStack.getItem() == grassBlockItem).slot();
            if (grassInvSlot != -1) {
               this.mc
                  .gameMode
                  .handleContainerInput(
                     this.mc.player.containerMenu.containerId, grassInvSlot < 9 ? grassInvSlot + 36 : grassInvSlot, 8, ContainerInput.SWAP, this.mc.player
                  );
            }
         } else {
            int rangeValue = (Integer)this.range.get();

            for (int i = 0; i < this.myceliumSpots.size(); i++) {
               BlockPos pos = this.myceliumSpots.get(i);
               Block block = this.mc.level.getBlockState(pos).getBlock();
               double distance = new Vec3(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ())
                  .distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
               if (block == Blocks.AIR && distance <= rangeValue) {
                  this.mc.player.getInventory().setSelectedSlot(grassHotbarSlot);
                  this.mc
                     .gameMode
                     .useItemOn(
                        this.mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false)
                     );
                  return;
               }

               if (!((List)this.blockWhitelist.get()).contains(block)) {
                  this.myceliumSpots.remove(i);
               }
            }

            for (int i = 0; i < this.myceliumSpots.size(); i++) {
               BlockPos posx = this.myceliumSpots.get(i);
               Block blockx = this.mc.level.getBlockState(posx).getBlock();
               double distancex = new Vec3(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ())
                  .distanceTo(new Vec3(posx.getX(), posx.getY(), posx.getZ()));
               if (((List)this.blockWhitelist.get()).contains(blockx) && distancex <= rangeValue) {
                  if ((Boolean)this.useShovel.get()) {
                     int shovelSlot = InvUtils.findInHotbar(
                           itemStack -> itemStack.getItem() == Items.NETHERITE_SHOVEL
                              || itemStack.getItem() == Items.DIAMOND_SHOVEL
                              || itemStack.getItem() == Items.IRON_SHOVEL
                              || itemStack.getItem() == Items.GOLDEN_SHOVEL
                              || itemStack.getItem() == Items.STONE_SHOVEL
                              || itemStack.getItem() == Items.WOODEN_SHOVEL
                        )
                        .slot();
                     if (shovelSlot != -1) {
                        this.mc.player.getInventory().setSelectedSlot(shovelSlot);
                     }
                  }

                  this.mc.gameMode.continueDestroyBlock(posx, Direction.UP);
                  return;
               }
            }

            this.myceliumSpots.clear();

            for (int x = -rangeValue; x < rangeValue; x++) {
               for (int y = -3; y < 3; y++) {
                  for (int z = -rangeValue; z < rangeValue; z++) {
                     BlockPos posx = this.mc.player.blockPosition().offset(x, y, z);
                     if (((List)this.blockWhitelist.get()).contains(this.mc.level.getBlockState(posx).getBlock())) {
                        this.myceliumSpots.add(posx);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean grassFilter(Block block) {
      return block == Blocks.MYCELIUM || block == Blocks.PODZOL || block == Blocks.DIRT_PATH || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT;
   }
}
