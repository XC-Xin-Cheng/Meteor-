package nekiplay.meteorplus.utils.xraybruteforce;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.utils.misc.UnorderedArrayList;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import nekiplay.meteorplus.features.modules.world.XrayBruteforce;
import net.minecraft.world.level.block.Block;

public class XGroup {
   private static final XrayBruteforce search = (XrayBruteforce)Modules.get().get(XrayBruteforce.class);
   private final Block block;
   public final UnorderedArrayList<XBlock> blocks = new UnorderedArrayList();
   private double sumX;
   private double sumY;
   private double sumZ;

   public XGroup(Block block) {
      this.block = block;
   }

   public void add(XBlock block, boolean removeFromOld, boolean splitGroup) {
      this.blocks.add(block);
      this.sumX = this.sumX + block.x;
      this.sumY = this.sumY + block.y;
      this.sumZ = this.sumZ + block.z;
      if (block.group != null && removeFromOld) {
         block.group.remove(block, splitGroup);
      }

      block.group = this;
   }

   public void add(XBlock block) {
      this.add(block, true, true);
   }

   public void remove(XBlock block, boolean splitGroup) {
      this.blocks.remove(block);
      this.sumX = this.sumX - block.x;
      this.sumY = this.sumY - block.y;
      this.sumZ = this.sumZ - block.z;
      if (this.blocks.isEmpty()) {
         search.removeGroup(block.group);
      } else if (splitGroup) {
         this.trySplit(block);
      }
   }

   public void remove(XBlock block) {
      this.remove(block, true);
   }

   private void trySplit(XBlock block) {
      Set<XBlock> neighbours = new ObjectOpenHashSet(6);

      for (int side : XBlock.SIDES) {
         if ((block.neighbours & side) == side) {
            XBlock neighbour = block.getSideBlock(side);
            if (neighbour != null) {
               neighbours.add(neighbour);
            }
         }
      }

      if (neighbours.size() > 1) {
         Set<XBlock> remainingBlocks = new ObjectOpenHashSet(this.blocks);
         Queue<XBlock> blocksToCheck = new ArrayDeque<>();
         blocksToCheck.offer((XBlock)this.blocks.get(0));
         remainingBlocks.remove(this.blocks.get(0));
         neighbours.remove(this.blocks.get(0));

         label86:
         while (!blocksToCheck.isEmpty()) {
            XBlock b = blocksToCheck.poll();

            for (int sidex : XBlock.SIDES) {
               if ((b.neighbours & sidex) == sidex) {
                  XBlock neighbour = b.getSideBlock(sidex);
                  if (neighbour != null && remainingBlocks.contains(neighbour)) {
                     blocksToCheck.offer(neighbour);
                     remainingBlocks.remove(neighbour);
                     neighbours.remove(neighbour);
                     if (neighbours.isEmpty()) {
                        break label86;
                     }
                  }
               }
            }
         }

         if (!neighbours.isEmpty()) {
            XGroup group = search.newGroup(this.block);
            group.blocks.ensureCapacity(remainingBlocks.size());
            this.blocks.removeIf(remainingBlocks::contains);

            for (XBlock b : remainingBlocks) {
               group.add(b, false, false);
               this.sumX = this.sumX - b.x;
               this.sumY = this.sumY - b.y;
               this.sumZ = this.sumZ - b.z;
            }

            if (neighbours.size() > 1) {
               block.neighbours = 0;

               for (XBlock b : neighbours) {
                  int x = b.x - block.x;
                  if (x == 1) {
                     block.neighbours |= 8;
                  } else if (x == -1) {
                     block.neighbours |= 128;
                  }

                  int y = b.y - block.y;
                  if (y == 1) {
                     block.neighbours |= 512;
                  } else if (y == -1) {
                     block.neighbours |= 16384;
                  }

                  int z = b.z - block.z;
                  if (z == 1) {
                     block.neighbours |= 2;
                  } else if (z == -1) {
                     block.neighbours |= 32;
                  }
               }

               group.trySplit(block);
            }
         }
      }
   }

   public void merge(XGroup group) {
      this.blocks.ensureCapacity(this.blocks.size() + group.blocks.size());

      for (XBlock block : group.blocks) {
         this.add(block, false, false);
      }

      search.removeGroup(group);
   }

   public void render(Render3DEvent event) {
      ESPBlockData blockData = search.getBlockData(this.block);
      if (blockData.tracer) {
         event.renderer
            .line(
               RenderUtils.center.x,
               RenderUtils.center.y,
               RenderUtils.center.z,
               this.sumX / this.blocks.size() + 0.5,
               this.sumY / this.blocks.size() + 0.5,
               this.sumZ / this.blocks.size() + 0.5,
               blockData.tracerColor
            );
      }
   }
}
