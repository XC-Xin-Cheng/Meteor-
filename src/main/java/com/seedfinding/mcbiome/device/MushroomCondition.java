package com.seedfinding.mcbiome.device;

public class MushroomCondition {
   static final long SHROOM_LAYER_SEED = -7479281634960481323L;
   static final long EPSILON = 4294967296L;
   static final long MASK = 4294967295L;
   static final int ARG = 299999;
   static final int TARGET = 0;
   private final long upperSeedMultiplier;
   private final long upperSeedAddend;
   private final long numValidSeeds;
   private final long lower32Bits;
   private final int x;
   private final int z;

   public MushroomCondition(long lower32Bits, int x, int z) {
      this.x = x;
      this.z = z;
      this.lower32Bits = lower32Bits & 4294967295L;
      long mushAddend = getShroomSeed(lower32Bits, x, z);
      long mushMultiplier = getShroomSeed(this.lower32Bits + 4294967296L, x, z) - mushAddend;
      int argNoEven = 299999 >> Long.numberOfTrailingZeros(299999L);
      this.upperSeedMultiplier = argNoEven * this.inverse32Bit(mushMultiplier >>> 32);
      long inverse = this.modInverse(256L, argNoEven);
      long needed = (0L - ((mushAddend & 4294967295L) >>> 24) % argNoEven + argNoEven) % argNoEven;
      long lowestValidMushSeed = inverse * needed % argNoEven * 4294967296L + (mushAddend & 4294967295L);
      this.upperSeedAddend = (lowestValidMushSeed - mushAddend >>> 32) * this.inverse32Bit(mushMultiplier >>> 32) & 4294967295L;
      this.numValidSeeds = (4294967296L - (lowestValidMushSeed >>> 32)) / argNoEven;
   }

   private long inverse32Bit(long a) {
      if (a % 2L == 0L) {
         System.err.println("a is not invertible");
      }

      long x = (a << 2 ^ a << 1) & 8L ^ a;
      x += x - a * x * x;
      x += x - a * x * x;
      x += x - a * x * x;
      return x & 4294967295L;
   }

   private long modInverse(long a, long m) {
      long m0 = m;
      long y = 0L;
      long x = 1L;
      if (m == 1L) {
         return 0L;
      } else {
         while (a > 1L) {
            long q = a / m;
            long t = m;
            m = a % m;
            a = t;
            t = y;
            y = x - q * y;
            x = t;
         }

         if (x < 0L) {
            x += m0;
         }

         return x;
      }
   }

   static long getShroomSeed(long ws, int x, int z) {
      long shroom_layer_seed = -7479281634960481323L;
      ws *= ws * 6364136223846793005L + 1442695040888963407L;
      ws += shroom_layer_seed;
      ws *= ws * 6364136223846793005L + 1442695040888963407L;
      ws += shroom_layer_seed;
      ws *= ws * 6364136223846793005L + 1442695040888963407L;
      ws += shroom_layer_seed;
      ws *= ws * 6364136223846793005L + 1442695040888963407L;
      long ss = ws + x;
      ss *= ss * 6364136223846793005L + 1442695040888963407L;
      ss += z;
      ss *= ss * 6364136223846793005L + 1442695040888963407L;
      ss += x;
      ss *= ss * 6364136223846793005L + 1442695040888963407L;
      return ss + z;
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public long getNumValidSeeds() {
      return this.numValidSeeds;
   }

   public long getUpperSeedMultiplier() {
      return this.upperSeedMultiplier;
   }

   public long getUpperSeedAddend() {
      return this.upperSeedAddend;
   }

   public long getLower32Bits() {
      return this.lower32Bits;
   }

   public static void main(String[] args) {
      MushroomCondition c = new MushroomCondition(10L, 0, 0);
      System.out.println(c.getUpperSeedAddend());
      System.out.println(c.getUpperSeedMultiplier());
      System.out.println(c.getNumValidSeeds());

      for (long lowerBits = 0L; lowerBits < 4294967296L; lowerBits++) {
         c = new MushroomCondition(lowerBits, 0, 0);

         for (long i = 0L; i < c.getNumValidSeeds(); i++) {
            long var6 = getShroomSeed((c.getUpperSeedAddend() + i * c.getUpperSeedMultiplier() << 32) + 10L, 0, 0);
         }
      }
   }
}
