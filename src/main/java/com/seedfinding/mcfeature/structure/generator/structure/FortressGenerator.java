package com.seedfinding.mcfeature.structure.generator.structure;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mccore.version.UnsupportedVersion;
import com.seedfinding.mcfeature.structure.generator.Generator;
import com.seedfinding.mcterrain.TerrainGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class FortressGenerator extends Generator {
   private static final int NORTH = 0;
   private static final int EAST = 1;
   private static final int SOUTH = 2;
   private static final int WEST = 3;
   private static final int START = 0;
   private static final int BRIDGE_FIRST = 1;
   private static final int BRIDGE_STRAIGHT = 1;
   private static final int BRIDGE_CROSSING = 2;
   private static final int BRIDGE_FORTIFIED_CROSSING = 3;
   private static final int BRIDGE_STAIRS = 4;
   private static final int BRIDGE_SPAWNER = 5;
   private static final int BRIDGE_CORRIDOR_ENTRANCE = 6;
   private static final int BRIDGE_PIECES_COUNT = 6;
   private static final int CORRIDOR_FIRST = 7;
   private static final int CORRIDOR_STRAIGHT = 7;
   private static final int CORRIDOR_CROSSING = 8;
   private static final int CORRIDOR_TURN_RIGHT = 9;
   private static final int CORRIDOR_TURN_LEFT = 10;
   private static final int CORRIDOR_STAIRS = 11;
   private static final int CORRIDOR_T_CROSSING = 12;
   private static final int CORRIDOR_NETHER_WART = 13;
   private static final int CORRIDOR_PIECES_COUNT = 7;
   private static final int END = 14;
   private static final int PIECES_COUNT = 15;
   private static final int[] BRIDGE_WEIGHTS = new int[]{30, 10, 10, 10, 5, 5};
   private static final int[] BRIDGE_MAXS = new int[]{0, 4, 4, 3, 2, 1};
   private static final boolean[] BRIDGE_ALLOW_CONSECUTIVE = new boolean[]{true, false, false, false, false, false};
   private static final int[] CORRIDOR_WEIGHTS = new int[]{25, 15, 5, 5, 10, 7, 5};
   private static final int[] CORRIDOR_MAXS = new int[]{0, 5, 10, 10, 3, 2, 2};
   private static final boolean[] CORRIDOR_ALLOW_CONSECUTIVE = new boolean[]{true, false, false, false, true, false, false};
   private static final FortressGenerator.Creator[] CREATORS = new FortressGenerator.Creator[]{
      null,
      FortressGenerator::createBridgeStraight,
      FortressGenerator::createBridgeCrossing,
      FortressGenerator::createBridgeFortifiedCrossing,
      FortressGenerator::createBridgeStairs,
      FortressGenerator::createBridgeSpawner,
      FortressGenerator::createBridgeCorridorEntrance,
      FortressGenerator::createCorridorStraight,
      FortressGenerator::createCorridorCrossing,
      FortressGenerator::createCorridorTurnRight,
      FortressGenerator::createCorridorTurnLeft,
      FortressGenerator::createCorridorStairs,
      FortressGenerator::createCorridorTCrossing,
      FortressGenerator::createCorridorNetherWart
   };
   private static final ArrayList<Consumer<ChunkRand>> POST_CREATORS = new ArrayList<Consumer<ChunkRand>>() {
      {
         this.add(rand -> {});
         this.add(rand -> {});
         this.add(rand -> {});
         this.add(rand -> {});
         this.add(rand -> {});
         this.add(rand -> {});
         this.add(rand -> {});
         this.add(rand -> {});
         this.add(rand -> {});
         this.add(rand -> rand.nextInt(3));
         this.add(rand -> rand.nextInt(3));
         this.add(rand -> {});
         this.add(rand -> {});
         this.add(rand -> {});
      }
   };
   private final List<FortressGenerator.PieceInfo>[] placements = new List[15];
   private final List<FortressGenerator.PieceInfo> pieceQueue = new ArrayList<>();
   private FortressGenerator.PieceInfo start;
   private int lastPlaced;
   private final ArrayList<Function<FortressGenerator, FortressGenerator.Extender>> EXTENDERS = new ArrayList<Function<FortressGenerator, FortressGenerator.Extender>>(
      
   ) {
      {
         this.add(generator -> null);
         this.add(generator -> (x$0, x$1) -> generator.extendBridgeStraight(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendBridgeCrossing(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendBridgeFortifiedCrossing(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendBridgeStairs(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendBridgeSpawner(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendBridgeCorridorEntrance(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendCorridorStraight(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendCorridorCrossing(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendCorridorTurnRight(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendCorridorTurnLeft(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendCorridorStairs(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendCorridorTCrossing(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendCorridorNetherWart(x$0, x$1));
         this.add(generator -> (x$0, x$1) -> generator.extendEnd(x$0, x$1));
      }
   };

   public FortressGenerator(MCVersion version) {
      super(version);
      Arrays.setAll(this.placements, i -> new ArrayList<>());
   }

   private void genFortress(int chunkX, int chunkZ, ChunkRand rand) {
      if (this.version.isOlderThan(MCVersion.v1_12)) {
         throw new UnsupportedVersion(this.version, "fortress generator.");
      } else {
         this.start = createStart((chunkX << 4) + 2, 64, (chunkZ << 4) + 2, rand);
         this.placements[0].add(this.start);
         this.extendBridgeCrossing(this.start, rand);

         while (!this.pieceQueue.isEmpty()) {
            int i = rand.nextInt(this.pieceQueue.size());
            FortressGenerator.PieceInfo piece = this.pieceQueue.remove(i);

            assert this.EXTENDERS.get(piece.type) != null;

            this.EXTENDERS.get(piece.type).apply(this).extend(piece, rand);
         }
      }
   }

   public List<FortressGenerator.PieceInfo>[] getPlacements() {
      return this.placements;
   }

   private static FortressGenerator.PieceInfo createStart(int x, int y, int z, ChunkRand rand) {
      return new FortressGenerator.PieceInfo(0, 0, x, y, z, x + 19 - 1, 73, z + 19 - 1, rand.nextInt(4));
   }

   private static FortressGenerator.PieceInfo createBridgeStraight(int x, int y, int z, int depth, int facing) {
      return createRotated(1, depth, x, y, z, -1, -3, 0, 5, 10, 19, facing);
   }

   private static FortressGenerator.PieceInfo createBridgeCrossing(int x, int y, int z, int depth, int facing) {
      return createRotated(2, depth, x, y, z, -8, -3, 0, 19, 10, 19, facing);
   }

   private static FortressGenerator.PieceInfo createBridgeFortifiedCrossing(int x, int y, int z, int depth, int facing) {
      return createRotated(3, depth, x, y, z, -2, 0, 0, 7, 9, 7, facing);
   }

   private static FortressGenerator.PieceInfo createBridgeStairs(int x, int y, int z, int depth, int facing) {
      return createRotated(4, depth, x, y, z, -2, 0, 0, 7, 11, 7, facing);
   }

   private static FortressGenerator.PieceInfo createBridgeSpawner(int x, int y, int z, int depth, int facing) {
      return createRotated(5, depth, x, y, z, -2, 0, 0, 7, 8, 9, facing);
   }

   private static FortressGenerator.PieceInfo createBridgeCorridorEntrance(int x, int y, int z, int depth, int facing) {
      return createRotated(6, depth, x, y, z, -5, -3, 0, 13, 14, 13, facing);
   }

   private static FortressGenerator.PieceInfo createCorridorStraight(int x, int y, int z, int depth, int facing) {
      return createRotated(7, depth, x, y, z, -1, 0, 0, 5, 7, 5, facing);
   }

   private static FortressGenerator.PieceInfo createCorridorCrossing(int x, int y, int z, int depth, int facing) {
      return createRotated(8, depth, x, y, z, -1, 0, 0, 5, 7, 5, facing);
   }

   private static FortressGenerator.PieceInfo createCorridorTurnRight(int x, int y, int z, int depth, int facing) {
      return createRotated(9, depth, x, y, z, -1, 0, 0, 5, 7, 5, facing);
   }

   private static FortressGenerator.PieceInfo createCorridorTurnLeft(int x, int y, int z, int depth, int facing) {
      return createRotated(10, depth, x, y, z, -1, 0, 0, 5, 7, 5, facing);
   }

   private static FortressGenerator.PieceInfo createCorridorStairs(int x, int y, int z, int depth, int facing) {
      return createRotated(11, depth, x, y, z, -1, -7, 0, 5, 14, 10, facing);
   }

   private static FortressGenerator.PieceInfo createCorridorTCrossing(int x, int y, int z, int depth, int facing) {
      return createRotated(12, depth, x, y, z, -3, 0, 0, 9, 7, 9, facing);
   }

   private static FortressGenerator.PieceInfo createCorridorNetherWart(int x, int y, int z, int depth, int facing) {
      return createRotated(13, depth, x, y, z, -5, -3, 0, 13, 14, 13, facing);
   }

   private static FortressGenerator.PieceInfo createEnd(int x, int y, int z, int depth, int facing) {
      return createRotated(14, depth, x, y, z, -1, -3, 0, 5, 10, 8, facing);
   }

   private static FortressGenerator.PieceInfo createRotated(
      int type, int depth, int x, int y, int z, int relXMin, int relYMin, int relZMin, int relXMax, int relYMax, int relZMax, int facing
   ) {
      int xMin;
      int xMax;
      switch (facing) {
         case 0:
         case 2:
            xMin = x + relXMin;
            xMax = x + relXMax - 1 + relXMin;
            break;
         case 1:
            xMin = x + relZMin;
            xMax = x + relZMax - 1 + relZMin;
            break;
         case 3:
            xMin = x - relZMax + 1 + relZMin;
            xMax = x + relZMin;
            break;
         default:
            throw new AssertionError();
      }

      int yMin = y + relYMin;
      int yMax = y + relYMax - 1 + relYMin;
      int zMin;
      int zMax;
      switch (facing) {
         case 0:
            zMin = z - relZMax + 1 + relZMin;
            zMax = z + relZMin;
            break;
         case 1:
         case 3:
            zMin = z + relXMin;
            zMax = z + relXMax - 1 + relXMin;
            break;
         case 2:
            zMin = z + relZMin;
            zMax = z + relZMax - 1 + relZMin;
            break;
         default:
            throw new AssertionError();
      }

      return new FortressGenerator.PieceInfo(type, depth, xMin, yMin, zMin, xMax, yMax, zMax, facing);
   }

   private void extendBridgeStraight(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendForwards(pieceInfo, rand, 1, 3, false);
   }

   private void extendBridgeCrossing(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendForwards(pieceInfo, rand, 8, 3, false);
      this.extendLeft(pieceInfo, rand, 8, 3, false);
      this.extendRight(pieceInfo, rand, 8, 3, false);
   }

   private void extendBridgeFortifiedCrossing(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendForwards(pieceInfo, rand, 2, 0, false);
      this.extendLeft(pieceInfo, rand, 2, 0, false);
      this.extendRight(pieceInfo, rand, 2, 0, false);
   }

   private void extendBridgeStairs(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendRight(pieceInfo, rand, 2, 6, false);
   }

   private void extendBridgeSpawner(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
   }

   private void extendEnd(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
   }

   private void extendBridgeCorridorEntrance(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendForwards(pieceInfo, rand, 5, 3, true);
   }

   private void extendCorridorStraight(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendForwards(pieceInfo, rand, 1, 0, true);
   }

   private void extendCorridorCrossing(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendForwards(pieceInfo, rand, 1, 0, true);
      this.extendLeft(pieceInfo, rand, 1, 0, true);
      this.extendRight(pieceInfo, rand, 1, 0, true);
   }

   private void extendCorridorTurnRight(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendRight(pieceInfo, rand, 1, 0, true);
   }

   private void extendCorridorTurnLeft(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendLeft(pieceInfo, rand, 1, 0, true);
   }

   private void extendCorridorStairs(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendForwards(pieceInfo, rand, 1, 0, true);
   }

   private void extendCorridorTCrossing(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      int horOffset;
      if (pieceInfo.facing != 3 && pieceInfo.facing != 0) {
         horOffset = 1;
      } else {
         horOffset = 5;
      }

      this.extendLeft(pieceInfo, rand, horOffset, 0, rand.nextInt(8) > 0);
      this.extendRight(pieceInfo, rand, horOffset, 0, rand.nextInt(8) > 0);
   }

   private void extendCorridorNetherWart(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand) {
      this.extendForwards(pieceInfo, rand, 5, 3, true);
      this.extendForwards(pieceInfo, rand, 5, 11, true);
   }

   private void extendForwards(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand, int horOffset, int vertOffset, boolean inCorridor) {
      switch (pieceInfo.facing) {
         case 0:
            this.extend(rand, pieceInfo.xMin + horOffset, pieceInfo.yMin + vertOffset, pieceInfo.zMin - 1, pieceInfo.facing, pieceInfo.depth + 1, inCorridor);
            break;
         case 1:
            this.extend(rand, pieceInfo.xMax + 1, pieceInfo.yMin + vertOffset, pieceInfo.zMin + horOffset, pieceInfo.facing, pieceInfo.depth + 1, inCorridor);
            break;
         case 2:
            this.extend(rand, pieceInfo.xMin + horOffset, pieceInfo.yMin + vertOffset, pieceInfo.zMax + 1, pieceInfo.facing, pieceInfo.depth + 1, inCorridor);
            break;
         case 3:
            this.extend(rand, pieceInfo.xMin - 1, pieceInfo.yMin + vertOffset, pieceInfo.zMin + horOffset, pieceInfo.facing, pieceInfo.depth + 1, inCorridor);
      }
   }

   private void extendLeft(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand, int horOffset, int vertOffset, boolean inCorridor) {
      switch (pieceInfo.facing) {
         case 0:
         case 2:
            this.extend(rand, pieceInfo.xMin - 1, pieceInfo.yMin + vertOffset, pieceInfo.zMin + horOffset, 3, pieceInfo.depth + 1, inCorridor);
            break;
         case 1:
         case 3:
            this.extend(rand, pieceInfo.xMin + horOffset, pieceInfo.yMin + vertOffset, pieceInfo.zMin - 1, 0, pieceInfo.depth + 1, inCorridor);
      }
   }

   private void extendRight(FortressGenerator.PieceInfo pieceInfo, ChunkRand rand, int horOffset, int vertOffset, boolean inCorridor) {
      switch (pieceInfo.facing) {
         case 0:
         case 2:
            this.extend(rand, pieceInfo.xMax + 1, pieceInfo.yMin + vertOffset, pieceInfo.zMin + horOffset, 1, pieceInfo.depth + 1, inCorridor);
            break;
         case 1:
         case 3:
            this.extend(rand, pieceInfo.xMin + horOffset, pieceInfo.yMin + vertOffset, pieceInfo.zMax + 1, 2, pieceInfo.depth + 1, inCorridor);
      }
   }

   private void extend(ChunkRand rand, int x, int y, int z, int facing, int depth, boolean inCorridor) {
      if (Math.abs(x - this.start.xMin) <= 112 && Math.abs(z - this.start.zMin) <= 112) {
         int first;
         int pieceCount;
         int[] weights;
         int[] maxs;
         boolean[] allowConsecutives;
         if (inCorridor) {
            first = 7;
            pieceCount = 7;
            weights = CORRIDOR_WEIGHTS;
            maxs = CORRIDOR_MAXS;
            allowConsecutives = CORRIDOR_ALLOW_CONSECUTIVE;
         } else {
            first = 1;
            pieceCount = 6;
            weights = BRIDGE_WEIGHTS;
            maxs = BRIDGE_MAXS;
            allowConsecutives = BRIDGE_ALLOW_CONSECUTIVE;
         }

         boolean anyValid = false;
         int totalWeight = 0;

         for (int i = 0; i < pieceCount; i++) {
            if (maxs[i] <= 0 || this.placements[first + i].size() < maxs[i]) {
               if (maxs[i] > 0) {
                  anyValid = true;
               }

               totalWeight += weights[i];
            }
         }

         if (anyValid && totalWeight > 0 && depth <= 30) {
            int tries = 0;

            while (tries < 5) {
               tries++;
               int n = rand.nextInt(totalWeight);

               for (int ix = 0; ix < pieceCount; ix++) {
                  if (maxs[ix] <= 0 || this.placements[first + ix].size() < maxs[ix]) {
                     n -= weights[ix];
                     if (n < 0) {
                        if (this.lastPlaced == first + ix && !allowConsecutives[ix]) {
                           break;
                        }

                        FortressGenerator.Creator creator = CREATORS[first + ix];
                        FortressGenerator.PieceInfo pieceInfo = creator.create(x, y, z, depth, facing);
                        if (!this.intersectsAny(pieceInfo.xMin, pieceInfo.yMin, pieceInfo.zMin, pieceInfo.xMax, pieceInfo.yMax, pieceInfo.zMax)) {
                           POST_CREATORS.get(first + ix).accept(rand);
                           this.lastPlaced = first + ix;
                           this.placements[first + ix].add(pieceInfo);
                           this.pieceQueue.add(pieceInfo);
                           return;
                        }
                     }
                  }
               }
            }
         }
      }

      FortressGenerator.PieceInfo pieceInfo = createEnd(x, y, z, depth, facing);
      if (!this.intersectsAny(pieceInfo.xMin, pieceInfo.yMin, pieceInfo.zMin, pieceInfo.xMax, pieceInfo.yMax, pieceInfo.zMax)) {
         rand.nextInt();
         this.placements[14].add(pieceInfo);
         this.pieceQueue.add(pieceInfo);
      }
   }

   private boolean intersectsAny(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
      List[] var7 = this.placements;
      int var8 = var7.length;

      for (int var9 = 0; var9 < var8; var9++) {
         for (FortressGenerator.PieceInfo pieceInfo : var7[var9]) {
            if (pieceInfo.xMin <= xMax
               && pieceInfo.xMax >= xMin
               && pieceInfo.zMin <= zMax
               && pieceInfo.zMax >= zMin
               && pieceInfo.yMin <= yMax
               && pieceInfo.yMax >= yMin) {
               return true;
            }
         }
      }

      return false;
   }

   private void setSeed(ChunkRand rand, long worldSeed, int chunkX, int chunkZ) {
      rand.setSeed(chunkX >> 4 ^ chunkZ >> 4 << 4 ^ worldSeed);
      rand.nextInt();
      rand.nextInt(3);
      rand.nextInt(8);
      rand.nextInt(8);
   }

   public static void main(String[] args) {
      ChunkRand rand = new ChunkRand();
      FortressGenerator fortressGenerator = new FortressGenerator(MCVersion.v1_12);
      fortressGenerator.setSeed(rand, 5896870166552931055L, -21, -5);
      fortressGenerator.genFortress(-21, -5, rand);
   }

   @Override
   public boolean generate(TerrainGenerator generator, int chunkX, int chunkZ, ChunkRand rand) {
      this.setSeed(rand, generator.getWorldSeed(), chunkX, chunkZ);
      this.genFortress(chunkX, chunkZ, rand);
      return true;
   }

   @Override
   public List<Pair<Generator.ILootType, BPos>> getChestsPos() {
      return null;
   }

   @Override
   public List<Pair<Generator.ILootType, BPos>> getLootPos() {
      return this.getChestsPos();
   }

   @Override
   public Generator.ILootType[] getLootTypes() {
      return new Generator.ILootType[0];
   }

   @FunctionalInterface
   private interface Creator {
      FortressGenerator.PieceInfo create(int var1, int var2, int var3, int var4, int var5);
   }

   @FunctionalInterface
   private interface Extender {
      void extend(FortressGenerator.PieceInfo var1, ChunkRand var2);
   }

   private static class PieceInfo {
      private final int type;
      private final int depth;
      private final int xMin;
      private final int yMin;
      private final int zMin;
      private final int xMax;
      private final int yMax;
      private final int zMax;
      private final int facing;

      public PieceInfo(int type, int depth, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, int facing) {
         this.type = type;
         this.depth = depth;
         this.xMin = xMin;
         this.yMin = yMin;
         this.zMin = zMin;
         this.xMax = xMax;
         this.yMax = yMax;
         this.zMax = zMax;
         this.facing = facing;
      }
   }
}
