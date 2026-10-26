package com.seedfinding.mccore.rand.seed;

import com.seedfinding.mccore.util.data.SeedIterator;
import com.seedfinding.mccore.util.data.StringUnhasher;
import com.seedfinding.mcmath.util.Mth;
import com.seedfinding.mcseed.rand.JRand;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class WorldSeed {
   public static long toPillarSeed(long worldSeed) {
      return StructureSeed.toPillarSeed(worldSeed);
   }

   public static boolean isStructureSeed(long worldSeed) {
      return toStructureSeed(worldSeed) == worldSeed;
   }

   public static long toStructureSeed(long worldSeed) {
      return worldSeed & Mth.MASK_48;
   }

   public static long getShadowSeed(long worldSeed) {
      return SeedMixer.getOtherSolution(worldSeed);
   }

   public static SeedIterator getSisterSeeds(long worldSeed) {
      return StructureSeed.getWorldSeeds(toStructureSeed(worldSeed));
   }

   public static boolean isString(long worldSeed) {
      return (int)worldSeed == worldSeed;
   }

   public static void toString(long worldSeed, StringUnhasher.Config config, Predicate<String> continueSearching) {
      if (isString(worldSeed)) {
         StringUnhasher.unhash((int)worldSeed, config, continueSearching);
      }
   }

   public static boolean isRandom(long worldSeed) {
      long upperBits = worldSeed >>> 32;
      long lowerBits = worldSeed & Mth.MASK_32;
      long a = 24667315L * upperBits + 18218081L * lowerBits + 67552711L >> 32;
      long b = -4824621L * upperBits + 7847617L * lowerBits + 7847617L >> 32;
      long seed = 7847617L * a - 18218081L * b;
      return JRand.nextLong(seed) == worldSeed;
   }

   public static List<Long> fromHash(long structureSeed, long hashedWorldSeed) {
      List<Long> worldSeeds = new ArrayList<>();
      StructureSeed.getWorldSeeds(structureSeed).forEachRemaining(worldSeed -> {
         if (toHash(worldSeed) == hashedWorldSeed) {
            worldSeeds.add(worldSeed);
         }
      });
      if (worldSeeds.size() > 1) {
         System.err
            .format(
               "This should never happen. Open a github issue right now and provide the structure seed [%d] as well as the hashed seed [%d], you have stumbled upon an incredibly rare collision.\n",
               structureSeed,
               hashedWorldSeed
            );
      }

      return worldSeeds;
   }

   public static long toHash(long worldSeed) {
      MessageDigest digest;
      try {
         digest = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException var7) {
         var7.printStackTrace();
         return worldSeed;
      }

      byte[] bytes = new byte[8];

      for (int i = 0; i < 8; i++) {
         bytes[i] = (byte)(worldSeed & 255L);
         worldSeed >>>= 8;
      }

      bytes = digest.digest(bytes);
      long hashedWorldSeed = bytes[0] & 255L;

      for (int i = 1; i < 8; i++) {
         hashedWorldSeed |= (bytes[i] & 255L) << (i << 3);
      }

      return hashedWorldSeed;
   }

   public static SeedIterator randomSeedsIterator() {
      return new SeedIterator(0L, 281474976710656L, JRand::nextLong);
   }
}
