package fr.crazycat256.satellite.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TPUtils {
   public static final double movedWronglyThreshold = 0.0625;

   public static void PaperTP(Vec3 startPos, Vec3 pos) {
      if (MeteorClient.mc.player.isShiftKeyDown()) {
         Input lastInput = MeteorClient.mc.player.getLastSentInput();
         Input input = new Input(lastInput.forward(), lastInput.backward(), lastInput.left(), lastInput.right(), lastInput.jump(), false, lastInput.sprint());
         MeteorClient.mc.player.connection.send(new ServerboundPlayerInputPacket(input));
      }

      double distance = startPos.distanceTo(pos);
      int packetsRequired = (int)Math.ceil(Math.abs(distance / 10.0));

      for (int packetNumber = 0; packetNumber < packetsRequired - 1; packetNumber++) {
         MeteorClient.mc.player.connection.send(new StatusOnly(true, MeteorClient.mc.player.horizontalCollision));
      }

      MeteorClient.mc.player.connection.send(new Pos(pos.x, pos.y, pos.z, true, MeteorClient.mc.player.horizontalCollision));
   }

   public static void PaperTP(Vec3 pos) {
      PaperTP(MeteorClient.mc.player.position(), pos);
   }

   public static BlockPos vec3ToBlockPos(Vec3 pos) {
      return new BlockPos((int)Math.floor(pos.x), (int)Math.floor(pos.y), (int)Math.floor(pos.z));
   }

   public static ArrayList<Vec3> findTPPath(Vec3 pos, double maxDistance) {
      return findTPPath(MeteorClient.mc.player.position(), pos, 10, maxDistance, 8.0);
   }

   public static ArrayList<Vec3> findTPPath(Vec3 startPos, Vec3 newPos, int maxSteps, double maxDistance, double accuracy) {
      double maxSquaredDistance = maxDistance * maxDistance;
      List<Direction> directions = new ArrayList<>(Arrays.stream(Direction.values()).toList());
      directions.sort((d1, d2) -> {
         double d1Dist = newPos.distanceTo(startPos.relative(d1, 10.0));
         double d2Dist = newPos.distanceTo(startPos.relative(d2, 10.0));
         return Double.compare(d1Dist, d2Dist);
      });
      ArrayList<Vec3> positions = new ArrayList<>();

      for (int h = 0; h < maxSteps; h++) {
         double closestPosSquaredDistance = positions.isEmpty() ? 2.147483647E9 : positions.getLast().distanceToSqr(newPos) + 100.0;
         Vec3 currentPos = positions.isEmpty() ? startPos : positions.getLast();
         double currentPosDistance = currentPos.distanceTo(newPos);
         Vec3 roundCurrentPos = new Vec3(Math.floor(currentPos.x) + 0.5, Math.floor(currentPos.y), Math.floor(currentPos.z) + 0.5);
         Vec3 closestPos = null;
         Vec3 potentialPos;
         if (currentPos.distanceToSqr(newPos) <= 100.0) {
            potentialPos = newPos;
         } else {
            potentialPos = currentPos.add(newPos.subtract(currentPos).normalize().scale(10.0));
            if (positions.contains(potentialPos)) {
               return null;
            }
         }

         if (isTPValid(currentPos, potentialPos)) {
            positions.add(potentialPos);
            if (potentialPos.distanceToSqr(newPos) <= maxSquaredDistance) {
               return positions;
            }
         } else {
            label78:
            for (int i = 10; i >= -10; i--) {
               int maxJ = (int)Math.sqrt(100 - i * i);

               for (int j = maxJ; j >= -maxJ; j--) {
                  int maxK = (int)Math.sqrt(100 - i * i - j * j);

                  for (int k = maxK; k >= -maxK; k--) {
                     potentialPos = roundCurrentPos.relative(directions.get(0), i).relative(directions.get(1), j).relative(directions.get(2), k);
                     double potentialPosSquaredDistance = potentialPos.distanceToSqr(newPos);
                     if (potentialPosSquaredDistance < closestPosSquaredDistance && isTPValid(currentPos, potentialPos)) {
                        if (potentialPosSquaredDistance <= maxSquaredDistance) {
                           positions.add(potentialPos);
                           return positions;
                        }

                        closestPos = potentialPos;
                        closestPosSquaredDistance = potentialPosSquaredDistance;
                        if (currentPosDistance - Math.sqrt(potentialPosSquaredDistance) > accuracy) {
                           break label78;
                        }
                     }
                  }
               }
            }

            if (closestPos == null || positions.contains(closestPos) || closestPosSquaredDistance > 10.0 * Math.pow(maxSteps - h, 2.0 + maxDistance)) {
               return null;
            }

            positions.add(closestPos);
         }
      }

      return positions;
   }

   public static boolean isObstructed(Vec3 pos) {
      AABB box = MeteorClient.mc.player.getBoundingBox().move(MeteorClient.mc.player.position().reverse()).move(pos);
      box = box.inflate(-1.0E-4, -1.0E-4, -1.0E-4);
      Iterator var2 = MeteorClient.mc.level.getBlockCollisions(MeteorClient.mc.player, box).iterator();
      if (var2.hasNext()) {
         VoxelShape v = (VoxelShape)var2.next();
         return true;
      } else {
         return false;
      }
   }

   public static boolean isTPValid(Vec3 startPos, Vec3 endPos) {
      return startPos.distanceToSqr(endPos) < 100.0000000000001 && !isObstructed(endPos) && !isWrongMove(startPos, endPos);
   }

   public static boolean isWrongMove(Vec3 startPos, Vec3 endPos) {
      return ServerUtils.getSquaredMovementDelta(startPos, endPos) > 0.0625;
   }
}
