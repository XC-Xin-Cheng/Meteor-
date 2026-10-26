package com.seedfinding.mccore.rand.seed;

import com.seedfinding.mccore.rand.ChunkRand;

public class PositionSeed {
   private static final ChunkRand INTERNAL = new ChunkRand();

   public static long getPositionSeed(int x, int y, int z) {
      long i = x * 3129871 ^ z * 116129781L ^ y;
      i = i * i * 42317861L + i * 11L;
      return i >> 16;
   }

   public static long getBaseStoneSeed(long worldSeed, int x, int y, int z) {
      return INTERNAL.setBaseStoneSeed(worldSeed, x, y, z);
   }
}
