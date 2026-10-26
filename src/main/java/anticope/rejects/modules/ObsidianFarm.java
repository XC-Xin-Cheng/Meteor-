package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class ObsidianFarm extends Module {
   private boolean allowBreakAgain;

   public ObsidianFarm() {
      super(MeteorRejectsAddon.CATEGORY, "obsidian-farm", "Auto obsidian farm(portals).");
   }

   public void onActivate() {
      this.allowBreakAgain = true;
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.player != null) {
         if (this.mc.level != null) {
            if (this.mc.gameMode != null) {
               if (this.mc.level.dimension() == Level.NETHER) {
                  this.allowBreakAgain = true;
               } else if (this.allowBreakAgain) {
                  if (!this.mc.player.isUsingItem() && !((AutoEat)Modules.get().get(AutoEat.class)).isActive()
                     || !this.mc.player.getOffhandItem().has(DataComponents.FOOD) && !this.mc.player.getMainHandItem().has(DataComponents.FOOD)) {
                     if (this.mc.player.getMainHandItem().getItem() != Items.NETHERITE_PICKAXE
                        && this.mc.player.getMainHandItem().getItem() != Items.DIAMOND_PICKAXE) {
                        int pickAxe = this.findPickAxe();
                        if (pickAxe == -1 && this.isActive()) {
                           this.toggle();
                           return;
                        }

                        this.mc.player.getInventory().setSelectedSlot(pickAxe);
                     }

                     BlockPos obsidian = this.findObsidian();
                     if (obsidian != null) {
                        this.mc.gameMode.continueDestroyBlock(obsidian, Direction.UP);
                        this.mc.player.swing(InteractionHand.MAIN_HAND);
                        if (this.mc.player.blockPosition().below().equals(obsidian) && this.mc.level.getBlockState(obsidian).getBlock() != Blocks.OBSIDIAN) {
                           this.allowBreakAgain = false;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private BlockPos findObsidian() {
      List<BlockPos> blocksList = new ArrayList<>();

      for (int x = -2; x < 3; x++) {
         for (int z = -2; z < 3; z++) {
            int y = 2;
            BlockPos block = new BlockPos(
               this.mc.player.blockPosition().getX() + x, this.mc.player.blockPosition().getY() + y, this.mc.player.blockPosition().getZ() + z
            );
            blocksList.add(block);
         }
      }

      Optional<BlockPos> result = blocksList.stream()
         .parallel()
         .filter(blockPos -> this.mc.level.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)
         .min(Comparator.comparingDouble(blockPos -> this.mc.player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
      if (result.isPresent()) {
         return result.get();
      } else {
         blocksList.clear();

         for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
               for (int y = 3; y > -2; y--) {
                  BlockPos block = new BlockPos(
                     this.mc.player.blockPosition().getX() + x, this.mc.player.blockPosition().getY() + y, this.mc.player.blockPosition().getZ() + z
                  );
                  blocksList.add(block);
               }
            }
         }

         Optional<BlockPos> result2 = blocksList.stream()
            .parallel()
            .filter(blockPos -> !this.mc.player.blockPosition().below().equals(blockPos))
            .filter(blockPos -> this.mc.level.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)
            .min(Comparator.comparingDouble(blockPos -> this.mc.player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
         if (result2.isPresent()) {
            return result2.get();
         } else {
            return this.mc.level.getBlockState(this.mc.player.blockPosition().below()).getBlock() == Blocks.OBSIDIAN
               ? this.mc.player.blockPosition().below()
               : null;
         }
      }
   }

   private int findPickAxe() {
      for (int i = 0; i < 9; i++) {
         if (this.mc.player.getInventory().getItem(i).getItem() == Items.NETHERITE_PICKAXE) {
            return i;
         }

         if (this.mc.player.getInventory().getItem(i).getItem() == Items.DIAMOND_PICKAXE) {
            return i;
         }
      }

      return -1;
   }
}
