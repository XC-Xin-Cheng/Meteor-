package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MyUtils {
   public static boolean place(
      BlockPos blockPos,
      Direction direction,
      SlabType slabType,
      Half blockHalf,
      Direction blockHorizontalOrientation,
      Axis wantedAxies,
      boolean airPlace,
      boolean swingHand,
      boolean rotate,
      boolean clientSide,
      int range,
      InteractionHand hand
   ) {
      if (MeteorClient.mc.player == null) {
         return false;
      } else if (!BlockUtils.canPlace(blockPos)) {
         return false;
      } else {
         Vec3 hitPos = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
         BlockPos neighbour;
         if (direction == null) {
            if ((slabType != null && slabType != SlabType.DOUBLE || blockHalf != null || blockHorizontalOrientation != null || wantedAxies != null)
               && !MeteorClient.mc.player.isCreative()) {
               return false;
            }

            direction = Direction.UP;
            neighbour = blockPos;
         } else if (airPlace) {
            neighbour = blockPos;
         } else {
            neighbour = blockPos.relative(direction.getOpposite());
            hitPos.add(direction.getStepX() * 0.5, direction.getStepY() * 0.5, direction.getStepZ() * 0.5);
         }

         Direction s = direction;
         if (rotate) {
            VoxelShape collisionShape = MeteorClient.mc.level.getBlockState(neighbour).getCollisionShape(MeteorClient.mc.level, neighbour);
            if (collisionShape.isEmpty()) {
               Rotations.rotate(
                  Rotations.getYaw(hitPos),
                  Rotations.getPitch(hitPos),
                  50,
                  clientSide,
                  () -> place(new BlockHitResult(hitPos, s, neighbour, false), swingHand, hand)
               );
               return true;
            }

            AABB aabb = collisionShape.bounds();

            for (double z = 0.1; z < 0.9; z += 0.2) {
               for (double x = 0.1; x < 0.9; x += 0.2) {
                  for (Vec3 placementMultiplier : aabbSideMultipliers(direction.getOpposite())) {
                     double placeX = neighbour.getX() + aabb.minX * x + aabb.maxX * (1.0 - x);
                     if ((slabType == null || slabType == SlabType.DOUBLE) && (blockHalf == null || direction == Direction.UP || direction == Direction.DOWN)
                        || MeteorClient.mc.player.isCreative()
                        || (slabType != SlabType.BOTTOM && blockHalf != Half.BOTTOM ? !(placementMultiplier.y > 0.5) : !(placementMultiplier.y <= 0.5))) {
                        double placeY = neighbour.getY() + aabb.minY * placementMultiplier.y + aabb.maxY * (1.0 - placementMultiplier.y);
                        double placeZ = neighbour.getZ() + aabb.minZ * z + aabb.maxZ * (1.0 - z);
                        Vec3 testHitPos = new Vec3(placeX, placeY, placeZ);
                        Vec3 playerHead = new Vec3(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getEyeY(), MeteorClient.mc.player.getZ());
                        Rotation rot = RotationStuff.calcRotationFromVec3d(
                           playerHead, testHitPos, new Rotation(MeteorClient.mc.player.getYRot(), MeteorClient.mc.player.getXRot())
                        );
                        Direction testHorizontalDirection = getHorizontalDirectionFromYaw(rot.normalize().getYaw());
                        if (blockHorizontalOrientation == null || testHorizontalDirection.getAxis() == blockHorizontalOrientation.getAxis()) {
                           HitResult res = RotationStuff.rayTraceTowards(MeteorClient.mc.player, rot, range);
                           BlockHitResult blockHitRes = (BlockHitResult)res;
                           if (res != null
                              && res.getType() == Type.BLOCK
                              && blockHitRes.getBlockPos().equals(neighbour)
                              && blockHitRes.getDirection() == direction) {
                              Rotations.rotate(
                                 Rotations.getYaw(testHitPos),
                                 Rotations.getPitch(testHitPos),
                                 50,
                                 clientSide,
                                 () -> place(new BlockHitResult(testHitPos, s, neighbour, false), swingHand, hand)
                              );
                              return true;
                           }
                        }
                     }
                  }
               }
            }
         } else {
            place(new BlockHitResult(hitPos, s, neighbour, false), swingHand, hand);
         }

         return true;
      }
   }

   private static void place(BlockHitResult blockHitResult, boolean swing, InteractionHand hand) {
      if (MeteorClient.mc.player != null && MeteorClient.mc.gameMode != null && MeteorClient.mc.getConnection() != null) {
         Input old_input = MeteorClient.mc.player.input.keyPresses;
         MeteorClient.mc.player.input.keyPresses = new Input(
            old_input.forward(), old_input.backward(), old_input.left(), old_input.right(), old_input.jump(), true, old_input.sprint()
         );
         InteractionResult result = MeteorClient.mc.gameMode.useItemOn(MeteorClient.mc.player, hand, blockHitResult);
         if (result == InteractionResult.SUCCESS) {
            if (swing) {
               MeteorClient.mc.player.swing(hand);
            } else {
               MeteorClient.mc.getConnection().send(new ServerboundSwingPacket(hand));
            }
         }

         MeteorClient.mc.player.input.keyPresses = old_input;
      }
   }

   public static boolean isBlockNormalCube(BlockState state) {
      Block block = state.getBlock();
      if (!(block instanceof ScaffoldingBlock)
         && !(block instanceof ShulkerBoxBlock)
         && !(block instanceof PointedDripstoneBlock)
         && !(block instanceof AmethystClusterBlock)) {
         try {
            return Block.isShapeFullBlock(state.getCollisionShape(null, null)) || state.getBlock() instanceof StairBlock;
         } catch (Exception var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean canPlaceAgainst(BlockState placeAtState, BlockState placeAgainstState, Direction against) {
      return isBlockNormalCube(placeAgainstState)
         || placeAgainstState.getBlock() == Blocks.GLASS
         || placeAgainstState.getBlock() instanceof StainedGlassBlock
         || placeAgainstState.getBlock() instanceof StairBlock
         || placeAgainstState.getBlock() instanceof SlabBlock
            && (
               placeAgainstState.getValue(SlabBlock.TYPE) != SlabType.BOTTOM
                     && placeAtState.getBlock() == placeAgainstState.getBlock()
                     && against != Direction.DOWN
                  || placeAtState.getBlock() != placeAgainstState.getBlock()
            );
   }

   public static boolean isBlockInLineOfSight(BlockPos placeAt, BlockState placeAtState) {
      Vec3 playerHead = new Vec3(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getEyeY(), MeteorClient.mc.player.getZ());
      Vec3 placeAtVec = new Vec3(placeAt.getX() + 0.5, placeAt.getY() + 0.5, placeAt.getZ() + 0.5);
      net.minecraft.world.level.ClipContext.Block type = net.minecraft.world.level.ClipContext.Block.COLLIDER;
      Fluid fluid = Fluid.NONE;
      ClipContext context = new ClipContext(playerHead, placeAtVec, type, fluid, MeteorClient.mc.player);
      BlockHitResult bhr = MeteorClient.mc.level.clip(context);
      return bhr.getType() == Type.MISS;
   }

   public static boolean isBlockSameAsPlaceDir(Block block) {
      return block instanceof HopperBlock;
   }

   public static boolean isBlockPlacementOppositeToPlacePos(Block block) {
      return block instanceof AmethystClusterBlock
         || block instanceof EndRodBlock
         || block instanceof LightningRodBlock
         || block instanceof TrapDoorBlock
         || block instanceof ChainBlock
         || block == Blocks.OAK_LOG
         || block == Blocks.SPRUCE_LOG
         || block == Blocks.BIRCH_LOG
         || block == Blocks.JUNGLE_LOG
         || block == Blocks.ACACIA_LOG
         || block == Blocks.DARK_OAK_LOG
         || block == Blocks.STRIPPED_SPRUCE_LOG
         || block == Blocks.STRIPPED_BIRCH_LOG
         || block == Blocks.STRIPPED_JUNGLE_LOG
         || block == Blocks.STRIPPED_ACACIA_LOG
         || block == Blocks.STRIPPED_DARK_OAK_LOG;
   }

   public static boolean isBlockSpecialCase(Block block) {
      return block instanceof ObserverBlock || block instanceof AnvilBlock || block instanceof GrindstoneBlock || block instanceof ButtonBlock;
   }

   public static boolean isBlockLikeButton(Block block) {
      return block instanceof ButtonBlock || block instanceof BellBlock || block instanceof GrindstoneBlock || block instanceof TrapDoorBlock;
   }

   public static boolean isBlockCheckingPitchForVerticalDir(Block block) {
      return block instanceof ObserverBlock || block instanceof PistonBaseBlock;
   }

   public static boolean isFaceDesired(Block block, Direction blockHorizontalOrientation, Direction against) {
      return blockHorizontalOrientation == null
         || !isBlockSameAsPlaceDir(block) && !isBlockPlacementOppositeToPlacePos(block)
         || isBlockSameAsPlaceDir(block) && blockHorizontalOrientation == against
         || block instanceof TrapDoorBlock && against.getOpposite() == blockHorizontalOrientation
         || !(block instanceof TrapDoorBlock)
            && (
               isBlockPlacementOppositeToPlacePos(block) && blockHorizontalOrientation == against.getOpposite()
                  || isBlockLikeButton(block) && against != Direction.UP && against != Direction.DOWN && blockHorizontalOrientation == against
            );
   }

   public static boolean isPlayerOrientationDesired(Block block, Direction blockHorizontalOrientation, Direction playerOrientation) {
      return blockHorizontalOrientation == null
         || block instanceof StairBlock && playerOrientation == blockHorizontalOrientation
         || !(block instanceof StairBlock)
            && !isBlockPlacementOppositeToPlacePos(block)
            && !isBlockSameAsPlaceDir(block)
            && playerOrientation == blockHorizontalOrientation.getOpposite();
   }

   public static Direction getVisiblePlaceSide(
      BlockPos placeAt,
      BlockState placeAtState,
      SlabType slabType,
      Half blockHalf,
      Direction blockHorizontalOrientation,
      Axis wantedAxies,
      int range,
      Direction requiredDir
   ) {
      if (MeteorClient.mc.level == null) {
         return null;
      } else {
         for (Direction against : Direction.values()) {
            if ((wantedAxies == null || against.getAxis() == wantedAxies)
               && (blockHalf == null || (against != Direction.UP || blockHalf != Half.BOTTOM) && (against != Direction.DOWN || blockHalf != Half.TOP))
               && (
                  slabType == null
                     || slabType == SlabType.DOUBLE
                     || MeteorClient.mc.player.isCreative()
                     || (slabType == SlabType.BOTTOM ? against != Direction.DOWN : against != Direction.UP)
               )
               && (wantedAxies != null || isFaceDesired(placeAtState.getBlock(), blockHorizontalOrientation, against))
               && (wantedAxies == null || wantedAxies == against.getAxis())
               && canPlaceAgainst(placeAtState, MeteorClient.mc.level.getBlockState(placeAt), against)
               && !BlockUtils.isClickable(MeteorClient.mc.level.getBlockState(placeAt).getBlock())) {
               AABB aabb = MeteorClient.mc.level.getBlockState(placeAt).getCollisionShape(MeteorClient.mc.level, placeAt).bounds();

               for (double z = 0.1; z < 0.9; z += 0.2) {
                  for (double x = 0.1; x < 0.9; x += 0.2) {
                     for (Vec3 placementMultiplier : aabbSideMultipliers(against)) {
                        double placeX = placeAt.getX() + aabb.minX * x + aabb.maxX * (1.0 - x);
                        if ((slabType == null || slabType == SlabType.DOUBLE) && (blockHalf == null || against == Direction.DOWN || against == Direction.UP)
                           || MeteorClient.mc.player.isCreative()
                           || (slabType != SlabType.BOTTOM && blockHalf != Half.BOTTOM ? !(placementMultiplier.y > 0.5) : !(placementMultiplier.y <= 0.5))) {
                           double placeY = placeAt.getY() + aabb.minY * placementMultiplier.y + aabb.maxY * (1.0 - placementMultiplier.y);
                           double placeZ = placeAt.getZ() + aabb.minZ * z + aabb.maxZ * (1.0 - z);
                           Vec3 hitPos = new Vec3(placeX, placeY, placeZ);
                           Vec3 playerHead = new Vec3(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getEyeY(), MeteorClient.mc.player.getZ());
                           Rotation rot = RotationStuff.calcRotationFromVec3d(
                              playerHead, hitPos, new Rotation(MeteorClient.mc.player.getYRot(), MeteorClient.mc.player.getXRot())
                           );
                           Direction testHorizontalDirection = getHorizontalDirectionFromYaw(rot.normalize().getYaw());
                           if ((
                                 !(placeAtState.getBlock() instanceof TrapDoorBlock)
                                    || against != Direction.DOWN && against != Direction.UP
                                    || isPlayerOrientationDesired(placeAtState.getBlock(), blockHorizontalOrientation, testHorizontalDirection)
                              )
                              && (
                                 placeAtState.getBlock() instanceof TrapDoorBlock
                                    || isPlayerOrientationDesired(placeAtState.getBlock(), blockHorizontalOrientation, testHorizontalDirection)
                              )) {
                              HitResult res = RotationStuff.rayTraceTowards(MeteorClient.mc.player, rot, range);
                              BlockHitResult blockHitRes = (BlockHitResult)res;
                              if (res != null
                                 && res.getType() == Type.BLOCK
                                 && blockHitRes.getBlockPos().equals(placeAt)
                                 && blockHitRes.getDirection() == against.getOpposite()) {
                                 return against.getOpposite();
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   public static Direction getPlaceSide(
      BlockPos blockPos,
      BlockState placeAtState,
      SlabType slabType,
      Half blockHalf,
      Direction blockHorizontalOrientation,
      Axis wantedAxies,
      Direction requiredDir
   ) {
      for (Direction side : Direction.values()) {
         BlockPos neighbor = blockPos.relative(side);
         Direction side2 = side.getOpposite();
         if ((wantedAxies == null || side.getAxis() == wantedAxies)
            && (blockHalf == null || (side != Direction.UP || blockHalf != Half.BOTTOM) && (side != Direction.DOWN || blockHalf != Half.TOP))
            && (
               (slabType == null || slabType == SlabType.DOUBLE) && blockHalf == null
                  || MeteorClient.mc.player.isCreative()
                  || (slabType != SlabType.BOTTOM && blockHalf != Half.BOTTOM ? side2 != Direction.UP : side2 != Direction.DOWN)
            )) {
            BlockState state = MeteorClient.mc.level.getBlockState(neighbor);
            if ((wantedAxies != null || isFaceDesired(placeAtState.getBlock(), blockHorizontalOrientation, side))
               && (wantedAxies == null || wantedAxies == side.getAxis())
               && !state.isAir()
               && !BlockUtils.isClickable(state.getBlock())
               && (
                  !state.hasProperty(BlockStateProperties.SLAB_TYPE)
                     || state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE
                        && (side != Direction.UP || state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.TOP)
                        && (side != Direction.DOWN || state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.BOTTOM)
               )
               && state.getFluidState().isEmpty()) {
               Vec3 hitPos = new Vec3(neighbor.getX(), neighbor.getY(), neighbor.getZ());
               Vec3 playerHead = new Vec3(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getEyeY(), MeteorClient.mc.player.getZ());
               Rotation rot = RotationStuff.calcRotationFromVec3d(
                  playerHead, hitPos, new Rotation(MeteorClient.mc.player.getYRot(), MeteorClient.mc.player.getXRot())
               );
               Direction testHorizontalDirection = getHorizontalDirectionFromYaw(rot.normalize().getYaw());
               if ((
                     !(placeAtState.getBlock() instanceof TrapDoorBlock)
                        || side != Direction.DOWN && side != Direction.UP
                        || isPlayerOrientationDesired(placeAtState.getBlock(), blockHorizontalOrientation, testHorizontalDirection)
                  )
                  && (
                     placeAtState.getBlock() instanceof TrapDoorBlock
                        || isPlayerOrientationDesired(placeAtState.getBlock(), blockHorizontalOrientation, testHorizontalDirection)
                  )) {
                  return side2;
               }
            }
         }
      }

      return null;
   }

   private static Vec3[] aabbSideMultipliers(Direction side) {
      switch (side) {
         case UP:
            return new Vec3[]{new Vec3(0.5, 1.0, 0.5), new Vec3(0.1, 1.0, 0.5), new Vec3(0.9, 1.0, 0.5), new Vec3(0.5, 1.0, 0.1), new Vec3(0.5, 1.0, 0.9)};
         case DOWN:
            return new Vec3[]{new Vec3(0.5, 0.0, 0.5), new Vec3(0.1, 0.0, 0.5), new Vec3(0.9, 0.0, 0.5), new Vec3(0.5, 0.0, 0.1), new Vec3(0.5, 0.0, 0.9)};
         case NORTH:
         case SOUTH:
         case EAST:
         case WEST:
            double x = side.getStepX() == 0 ? 0.5 : (1 + side.getStepX()) / 2.0;
            double z = side.getStepZ() == 0 ? 0.5 : (1 + side.getStepZ()) / 2.0;
            return new Vec3[]{new Vec3(x, 0.25, z), new Vec3(x, 0.75, z)};
         default:
            throw new IllegalStateException();
      }
   }

   public static Direction getHorizontalDirectionFromYaw(float yaw) {
      yaw %= 360.0F;
      if (yaw < 0.0F) {
         yaw += 360.0F;
      }

      if ((!(yaw >= 45.0F) || !(yaw < 135.0F)) && (!(yaw >= -315.0F) || !(yaw < -225.0F))) {
         if ((!(yaw >= 135.0F) || !(yaw < 225.0F)) && (!(yaw >= -225.0F) || !(yaw < -135.0F))) {
            return (!(yaw >= 225.0F) || !(yaw < 315.0F)) && (!(yaw >= -135.0F) || !(yaw < -45.0F)) ? Direction.SOUTH : Direction.EAST;
         } else {
            return Direction.NORTH;
         }
      } else {
         return Direction.WEST;
      }
   }

   public static Direction getVerticalDirectionFromPitch(float pitch) {
      return pitch >= 0.0F ? Direction.UP : Direction.DOWN;
   }
}
