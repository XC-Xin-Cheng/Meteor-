package nekiplay.meteorplus.features.modules.world.autoobsidianmine.modes;

import baritone.api.BaritoneAPI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import nekiplay.meteorplus.features.modules.world.autoobsidianmine.AutoObsidianFarmMode;
import nekiplay.meteorplus.features.modules.world.autoobsidianmine.AutoObsidianFarmModes;
import nekiplay.meteorplus.utils.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Portals extends AutoObsidianFarmMode {
   private final Portals.Shape shape = Portals.Shape.Cube;
   private final Portals.Mode mode = Portals.Mode.All;
   private final Double range = 5.5;
   private final Integer maxBlocksPerTick = 1;
   private final Portals.SortMode sortMode = Portals.SortMode.Closest;
   private final Pool<MutableBlockPos> blockPosPool = new Pool(MutableBlockPos::new);
   private final List<MutableBlockPos> blocks = new ArrayList<>();
   private boolean firstBlock;
   private final MutableBlockPos lastBlockPos = new MutableBlockPos();
   private int timer;
   private int noBlockTimer;
   private final MutableBlockPos pos1 = new MutableBlockPos();
   private final MutableBlockPos pos2 = new MutableBlockPos();
   private AABB box;
   private int maxh = 0;
   private int maxv = 0;
   private boolean baritoneBreakSaved = false;
   private boolean baritonePlaceSaved = false;
   private int commandDelay = 0;
   private boolean isMine = false;

   public Portals() {
      super(AutoObsidianFarmModes.Portals_Vanilla);
   }

   @Override
   public void onActivate() {
      this.commandDelay = (Integer)this.settings.delayCommand.get();
      this.firstBlock = true;
      this.timer = 0;
      this.noBlockTimer = 0;
      if ((Boolean)this.settings.noBaritoneBreaking.get()) {
         this.baritoneBreakSaved = (Boolean)BaritoneAPI.getSettings().allowBreak.value;
      }

      if ((Boolean)this.settings.noBaritonePlacing.get()) {
         this.baritonePlaceSaved = (Boolean)BaritoneAPI.getSettings().allowPlace.value;
      }
   }

   @Override
   public void onDeactivate() {
      if (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().hasPath()
         || BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
         BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("stop");
      }

      if ((Boolean)this.settings.noBaritoneBreaking.get()) {
         BaritoneAPI.getSettings().allowBreak.value = this.baritoneBreakSaved;
      }

      if ((Boolean)this.settings.noBaritonePlacing.get()) {
         BaritoneAPI.getSettings().allowPlace.value = this.baritonePlaceSaved;
      }
   }

   @Override
   public void onTickEventPre(Pre event) {
      if (this.commandDelay <= (Integer)this.settings.delayCommand.get()) {
         this.commandDelay++;
      }

      if (this.mc.player != null && this.mc.level != null) {
         if (!this.mc.player.isUsingItem() && (!((AutoEat)Modules.get().get(AutoEat.class)).isActive() || !((AutoEat)Modules.get().get(AutoEat.class)).eating)
            || !(Boolean)this.settings.pauseOnEat.get()) {
            if (this.settings.workingMode.get() == AutoObsidianFarmModes.Portal_Homes) {
               if (PlayerUtils.getDimension() == Dimension.Overworld) {
                  this.commandDelay = 0;
                  this.isMine = false;
                  if (!BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().hasPath()
                     && !BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
                     BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("goto nether_portal");
                  }
               } else if (PlayerUtils.getDimension() == Dimension.Nether) {
                  List<BlockPos> obsidians = this.getPortalBlocks();
                  this.isMine = true;
                  if ((obsidians.isEmpty() || this.blocks.isEmpty())
                     && this.mc.player != null
                     && this.commandDelay >= (Integer)this.settings.delayCommand.get()) {
                     ChatUtils.sendPlayerMsg((String)this.settings.command.get());
                     this.commandDelay = 0;
                  }
               }
            } else if (this.settings.workingMode.get() == AutoObsidianFarmModes.Portals_Vanilla) {
               if (PlayerUtils.getDimension() == Dimension.Overworld) {
                  BlockPos to = (BlockPos)this.settings.twoPortalPosition.get();
                  BlockPos dis = BlockHelper.opposite(to, Dimension.Nether);
                  double distance = Math.sqrt(PlayerUtils.squaredDistanceTo(dis.getX(), this.mc.player.getY(), dis.getZ()));
                  if (distance <= 20.0) {
                     List<BlockPos> obsidians = this.getPortalBlocks();
                     Block down = this.mc.level.getBlockState(this.mc.player.blockPosition().offset(0, -1, 0)).getBlock();
                     if (obsidians.isEmpty()) {
                        this.isMine = false;
                     } else if (down == Blocks.OBSIDIAN) {
                        this.isMine = true;
                     }
                  } else {
                     this.isMine = false;
                  }

                  if (!this.isMine) {
                     if ((Boolean)this.settings.noBaritoneBreaking.get()) {
                        BaritoneAPI.getSettings().allowBreak.value = false;
                     }

                     if (!BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().hasPath()
                        && !BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
                        to = (BlockPos)this.settings.mainPortalPosition.get();
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("goto " + to.getX() + " " + to.getY() + " " + to.getZ());
                     }
                  } else if (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().hasPath()
                     || BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()
                     || BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().getPath().isPresent()) {
                     BaritoneAPI.getSettings().allowBreak.value = true;
                     BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("stop");
                  }
               } else if (PlayerUtils.getDimension() == Dimension.Nether) {
                  this.isMine = false;
                  if ((Boolean)this.settings.noBaritoneBreaking.get()) {
                     BaritoneAPI.getSettings().allowBreak.value = false;
                  }

                  if (!BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().hasPath()
                     && !BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
                     BlockPos tox = (BlockPos)this.settings.twoPortalPosition.get();
                     BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("goto " + tox.getX() + " " + tox.getY() + " " + tox.getZ());
                  }
               }
            }

            if (this.timer > 0) {
               this.timer--;
            } else {
               assert this.mc.player != null;

               double pX = this.mc.player.getX();
               double pY = this.mc.player.getY();
               double pZ = this.mc.player.getZ();
               double rangeSq = Math.pow(this.range, 2.0);
               if (this.shape == Portals.Shape.UniformCube) {
                  Math.round(this.range);
               }

               int r = (int)Math.round(this.range);
               if (this.shape == Portals.Shape.UniformCube) {
                  double pX_ = pX + 1.0;
                  this.pos1.set(pX_ - r, pY - r + 1.0, pZ - r + 1.0);
                  this.pos2.set(pX_ + r - 1.0, pY + r, pZ + r);
               } else {
                  int direction = Math.round(this.mc.player.getRotationVector().y % 360.0F / 90.0F);
                  direction = direction != 4 && direction != -4 ? direction : 0;
                  direction = direction == -2 ? 2 : (direction == -1 ? 3 : (direction == -3 ? 1 : direction));
                  int range_down = 0;
                  int range_right = 2;
                  int range_forward = 2;
                  this.pos1.set(pX - range_forward, Math.ceil(pY) - range_down, pZ - range_right);
                  int range_up = 6;
                  int range_back = 2;
                  int range_left = 2;
                  this.pos2.set(pX + range_back + 1.0, Math.ceil(pY + range_up + 1.0), pZ + range_left + 1.0);
                  if (direction == 2) {
                     double var28 = pX + 1.0;
                     double pZ_ = pZ + 1.0;
                     this.pos1.set(var28 - (range_left + 1), Math.ceil(pY) - range_down, pZ_ - (range_forward + 1));
                     this.pos2.set(var28 + range_right, Math.ceil(pY + range_up + 1.0), pZ_ + range_back);
                  } else if (direction == 3) {
                     double var29 = pX + 1.0;
                     this.pos1.set(var29 - (range_back + 1), Math.ceil(pY) - range_down, pZ - range_left);
                     this.pos2.set(var29 + range_forward, Math.ceil(pY + range_up + 1.0), pZ + range_right + 1.0);
                  } else if (direction == 0) {
                     double var31 = pZ + 1.0;
                     double var30 = pX + 1.0;
                     this.pos1.set(var30 - (range_right + 1), Math.ceil(pY) - range_down, var31 - (range_back + 1));
                     this.pos2.set(var30 + range_left, Math.ceil(pY + range_up + 1.0), var31 + range_forward);
                  }

                  this.maxh = 1 + Math.max(Math.max(Math.max(range_back, range_right), range_forward), range_left);
                  this.maxv = 1 + Math.max(range_up, range_down);
               }

               if (this.mode == Portals.Mode.Flatten) {
                  this.pos1.setY((int)Math.floor(pY));
               }

               this.box = new AABB(this.pos1.getCenter(), this.pos2.getCenter());
               BlockIterator.register(
                  Math.max((int)Math.ceil(this.range + 1.0), this.maxh),
                  Math.max((int)Math.ceil(this.range), this.maxv),
                  (blockPos, blockState) -> {
                     boolean toofarSphere = Utils.squaredDistance(pX, pY, pZ, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) > rangeSq;
                     boolean toofarUniformCube = maxDist(Math.floor(pX), Math.floor(pY), Math.floor(pZ), blockPos.getX(), blockPos.getY(), blockPos.getZ())
                        >= this.range;
                     boolean toofarCube = !this.box.contains(Vec3.atCenterOf(blockPos));
                     if (BlockUtils.canBreak(blockPos, blockState)
                        && (!toofarSphere || this.shape != Portals.Shape.Sphere)
                        && (!toofarUniformCube || this.shape != Portals.Shape.UniformCube)
                        && (!toofarCube || this.shape != Portals.Shape.Cube)) {
                        if (blockState.getBlock() == Blocks.OBSIDIAN || blockState.getBlock() == Blocks.FIRE) {
                           this.blocks.add(((MutableBlockPos)this.blockPosPool.get()).set(blockPos));
                        }
                     }
                  }
               );
               BlockIterator.after(
                  () -> {
                     if (this.isMine) {
                        if (this.sortMode != Portals.SortMode.None) {
                           if (this.sortMode == Portals.SortMode.Closest || this.sortMode == Portals.SortMode.Furthest) {
                              this.blocks
                                 .sort(
                                    Comparator.comparingDouble(
                                       value -> Utils.squaredDistance(pX, pY, pZ, value.getX() + 0.5, value.getY() + 0.5, value.getZ() + 0.5)
                                          * (this.sortMode == Portals.SortMode.Closest ? 1 : -1)
                                    )
                                 );
                           } else if (this.sortMode == Portals.SortMode.TopDown) {
                              this.blocks.sort(Comparator.comparingDouble(value -> -1 * value.getY()));
                           }
                        }

                        if (this.blocks.isEmpty()) {
                           if (this.noBlockTimer++ >= (Integer)this.settings.delay.get()) {
                              this.firstBlock = true;
                           }

                           return;
                        }

                        this.noBlockTimer = 0;
                        if (!this.firstBlock && !this.lastBlockPos.equals(this.blocks.get(0))) {
                           this.timer = (Integer)this.settings.delay.get();
                           this.firstBlock = false;
                           this.lastBlockPos.set((Vec3i)this.blocks.get(0));
                           if (this.timer > 0) {
                              return;
                           }
                        }

                        int count = 0;

                        for (BlockPos block : this.blocks) {
                           if (count >= this.maxBlocksPerTick) {
                              break;
                           }

                           if ((Boolean)this.settings.rotate.get()) {
                              this.rotate(block);
                           }

                           boolean canInstaMine = BlockUtils.canInstaBreak(block);
                           BlockUtils.breakBlock(block, (Boolean)this.settings.swingHand.get());
                           this.lastBlockPos.set(block);
                           count++;
                           if (!canInstaMine) {
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
               );
            }
         }
      }
   }

   private void rotate(BlockPos target) {
      Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target), null);
   }

   private List<BlockPos> getPortalBlocks() {
      List<BlockPos> temp = new ArrayList<>();

      for (int i = 0; i < 6; i++) {
         for (int i2 = -4; i2 < 4; i2++) {
            for (int i3 = -4; i3 < 4; i3++) {
               assert this.mc.player != null;

               assert this.mc.level != null;

               BlockPos pos = this.mc.player.blockPosition().offset(i2, i, i3);
               BlockState state = this.mc.level.getBlockState(pos);
               if (state.getBlock() == Blocks.OBSIDIAN) {
                  temp.add(pos);
               }
            }
         }
      }

      return temp;
   }

   public static double maxDist(double x1, double y1, double z1, double x2, double y2, double z2) {
      double dX = Math.ceil(Math.abs(x2 - x1));
      double dY = Math.ceil(Math.abs(y2 - y1));
      double dZ = Math.ceil(Math.abs(z2 - z1));
      return Math.max(Math.max(dX, dY), dZ);
   }

   public static enum Mode {
      All,
      Flatten,
      Smash;
   }

   public static enum Shape {
      Cube,
      UniformCube,
      Sphere;
   }

   public static enum SortMode {
      None,
      Closest,
      Furthest,
      TopDown;
   }
}
