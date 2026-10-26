package nekiplay.meteorplus.features.modules.world.autoobsidianmine.modes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import nekiplay.meteorplus.features.modules.world.autoobsidianmine.AutoObsidianFarmMode;
import nekiplay.meteorplus.features.modules.world.autoobsidianmine.AutoObsidianFarmModes;
import nekiplay.meteorplus.utils.RaycastUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.Shapes;

public class Cauldrons extends AutoObsidianFarmMode {
   private final List<MutableBlockPos> blocks = new ArrayList<>();
   private boolean firstBlock;
   private int noBlockTimer;
   private final MutableBlockPos lastBlockPos = new MutableBlockPos();
   private int timer;
   private final Pool<MutableBlockPos> blockPosPool = new Pool(MutableBlockPos::new);
   private int collectTimer = 0;
   private int lavaPlaceTimer = 0;
   private final Portals.SortMode sortMode = Portals.SortMode.Closest;
   private int placed = 0;

   public Cauldrons() {
      super(AutoObsidianFarmModes.Cauldrons);
   }

   @Override
   public void onActivate() {
      this.firstBlock = true;
      this.timer = 0;
      this.noBlockTimer = 0;
      this.collectTimer = 0;
      this.lavaPlaceTimer = 0;
      this.placed = 0;
   }

   @Override
   public void onCollisionShape(CollisionShapeEvent event) {
      if ((Boolean)this.settings.solidCauldrons.get()) {
         if (event.state.getBlock() == Blocks.CAULDRON || event.state.getBlock() == Blocks.LAVA_CAULDRON || event.state.getBlock() == Blocks.WATER_CAULDRON) {
            event.shape = Shapes.block();
         }
      }
   }

   @Override
   public void onDeactivate() {
      if (this.placed > 0) {
         ChatUtils.info("Farmed obsidian: " + this.placed / 64 + " stacks", new Object[0]);
         if (this.placed >= 1728) {
            ChatUtils.info("Farmed obsidian: " + this.placed / 64 / 27 + " chests", new Object[0]);
         }
      }
   }

   @Override
   public String getInfoString() {
      return Integer.toString(this.placed);
   }

   @Override
   public void onTickEventPost(Post event) {
      if (this.mc.player != null && this.mc.level != null && this.mc.gameMode != null) {
         if (!this.mc.player.isUsingItem() && (!((AutoEat)Modules.get().get(AutoEat.class)).isActive() || !((AutoEat)Modules.get().get(AutoEat.class)).eating)
            || !(Boolean)this.settings.pauseOnEat.get()) {
            if (!(TickRate.INSTANCE.getTimeSinceLastTick() >= 1.7) || !(Boolean)this.settings.tpsCheck.get()) {
               BlockPos placing = (BlockPos)this.settings.lavaPlaceLocation.get();
               if (!(this.mc.player.distanceToSqr(placing.getCenter()) >= (Integer)this.settings.range.get() + (Integer)this.settings.range.get() + 1)) {
                  BlockIterator.register((Integer)this.settings.range.get(), (Integer)this.settings.range.get(), (blockPos, blockState) -> {
                     Block block = blockState.getBlock();
                     if (block == Blocks.LAVA_CAULDRON || block == Blocks.OBSIDIAN || block == Blocks.CAULDRON) {
                        this.blocks.add(((MutableBlockPos)this.blockPosPool.get()).set(blockPos));
                     }
                  });
                  BlockIterator.after(
                     () -> {
                        if (this.mc.player != null && this.mc.player.getInventory() != null) {
                           if (this.blocks.isEmpty()) {
                              if (this.noBlockTimer++ >= (Integer)this.settings.delay.get()) {
                                 this.firstBlock = true;
                              }
                           } else {
                              this.noBlockTimer = 0;
                              double pX = this.mc.player.getX();
                              double pY = this.mc.player.getY();
                              double pZ = this.mc.player.getZ();
                              this.blocks
                                 .sort(
                                    Comparator.comparingDouble(
                                       value -> Utils.squaredDistance(pX, pY, pZ, value.getX() + 0.5, value.getY() + 0.5, value.getZ() + 0.5)
                                          * (this.sortMode == Portals.SortMode.Closest ? 1 : -1)
                                    )
                                 );
                              if (!this.firstBlock && !this.lastBlockPos.equals(this.blocks.get(0))) {
                                 this.timer = (Integer)this.settings.delay.get();
                                 this.firstBlock = false;
                                 this.lastBlockPos.set((Vec3i)this.blocks.get(0));
                                 if (this.timer > 0) {
                                    return;
                                 }
                              }

                              BlockState state = this.mc.level.getBlockState(placing);
                              if (state.getBlock() == Blocks.OBSIDIAN) {
                                 if (BlockUtils.canBreak(placing)) {
                                    this.rotate(placing, null);
                                    this.lavaPlaceTimer = 0;
                                    BlockUtils.breakBlock(placing, true);
                                 }
                              } else {
                                 FindItemResult bucket = InvUtils.findInHotbar(new Item[]{Items.BUCKET});
                                 FindItemResult lavaBucket = InvUtils.findInHotbar(new Item[]{Items.LAVA_BUCKET});
                                 if (lavaBucket.found() && this.mc.player.position().distanceTo(placing.getCenter()) <= (Integer)this.settings.range.get() + 1) {
                                    if (state.getBlock() != Blocks.LAVA) {
                                       if (this.lavaPlaceTimer >= (Integer)this.settings.lavaPlaceDelay.get()) {
                                          double yaw = Rotations.getYaw(placing);
                                          double pitch = Rotations.getPitch(placing);
                                          Vec3 pos = this.mc.player.getEyePosition();
                                          HitResult result = RaycastUtils.bucketRaycast(pos, (float)pitch, (float)yaw, Fluid.NONE);
                                          if (result.getType() == Type.BLOCK) {
                                             BlockHitResult blockHitResult = (BlockHitResult)result;
                                             BlockPos blockPos = blockHitResult.getBlockPos();
                                             Direction direction = blockHitResult.getDirection();
                                             BlockPos blockPos2 = blockPos.relative(direction);
                                             if (blockPos2.equals(placing)) {
                                                Rotations.rotate(yaw, pitch, 10, true, () -> {
                                                   InvUtils.swap(lavaBucket.slot(), true);
                                                   this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND);
                                                   this.placed++;
                                                   InvUtils.swapBack();
                                                   this.lavaPlaceTimer = 0;
                                                });
                                             }
                                          }
                                       } else {
                                          this.lavaPlaceTimer++;
                                       }
                                    }
                                 } else if (bucket.found()) {
                                    for (BlockPos block : this.blocks) {
                                       BlockState state2 = this.mc.level.getBlockState(block);
                                       if (state2.getBlock() == Blocks.LAVA_CAULDRON
                                          && this.mc.player.position().distanceTo(block.getCenter()) <= (Integer)this.settings.range.get() + 1) {
                                          if (this.collectTimer >= (Integer)this.settings.collectDelay.get()) {
                                             this.mc.player.getInventory().setSelectedSlot(bucket.slot());
                                             this.mc.player.connection.send(new ServerboundSetCarriedItemPacket(bucket.slot()));
                                             this.rotate(block, () -> {
                                                Vec3 hitPos = Vec3.atCenterOf(block);
                                                Direction side = Direction.DOWN;
                                                BlockHitResult bhr = new BlockHitResult(hitPos, Direction.UP, block, false);
                                                boolean isSneaking = false;
                                                if ((Boolean)this.settings.bypassSneak.get()) {
                                                   this.mc.player.setShiftKeyDown(false);
                                                   isSneaking = true;
                                                }

                                                BlockUtils.interact(bhr, InteractionHand.MAIN_HAND, true);
                                                if ((Boolean)this.settings.bypassSneak.get() && isSneaking) {
                                                   this.mc.player.setShiftKeyDown(true);
                                                }

                                                this.collectTimer = 0;
                                             });
                                          } else {
                                             this.collectTimer++;
                                          }
                                          break;
                                       }
                                    }

                                    this.firstBlock = false;

                                    for (MutableBlockPos blockPos : this.blocks) {
                                       this.blockPosPool.free(blockPos);
                                    }

                                    this.blocks.clear();
                                 }
                              }
                           }
                        }
                     }
                  );
               }
            }
         }
      }
   }

   private void rotate(BlockPos target, Runnable action) {
      Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target), action);
   }
}
