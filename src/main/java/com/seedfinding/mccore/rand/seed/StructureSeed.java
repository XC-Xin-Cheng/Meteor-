package com.seedfinding.mccore.rand.seed;

import com.seedfinding.mccore.util.data.SeedIterator;
import com.seedfinding.mccore.util.math.NextLongReverser;
import java.util.List;

public final class StructureSeed {
   public static long toPillarSeed(long structureSeed) {
      return PillarSeed.fromStructureSeed(structureSeed);
   }

   public static long toWorldSeed(long structureSeed, long upperBits) {
      return upperBits << 48 | WorldSeed.toStructureSeed(structureSeed);
   }

   public static List<Long> toRandomWorldSeeds(long structureSeed) {
      return NextLongReverser.getNextLongEquivalents(structureSeed);
   }

   public static SeedIterator getWorldSeeds(long structureSeed) {
      return new SeedIterator(0L, 65536L, upperBits -> toWorldSeed(structureSeed, upperBits));
   }

   public static SeedIterator iterator() {
      return new SeedIterator(0L, 281474976710656L);
   }
}
