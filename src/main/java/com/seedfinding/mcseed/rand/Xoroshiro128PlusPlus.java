package com.seedfinding.mcseed.rand;

import com.seedfinding.mcmath.util.Mth;
import java.util.concurrent.atomic.AtomicLong;

public class Xoroshiro128PlusPlus implements IRand {
   public Xoroshiro128PlusPlus.Seed128 seed;
   public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
   public static final long SILVER_RATIO_64 = 7640891576956012809L;
   private static final AtomicLong SEED_UNIQUIFIER = new AtomicLong(8682522807148012L);
   private double nextNextGaussian;
   private boolean haveNextNextGaussian;

   public static long mixStafford13(long l) {
      l = (l ^ l >>> 30) * -4658895280553007687L;
      l = (l ^ l >>> 27) * -7723592293110705685L;
      return l ^ l >>> 31;
   }

   public static Xoroshiro128PlusPlus.Seed128 upgradeSeedTo128bit(long seed) {
      long silverSeed = seed ^ 7640891576956012809L;
      long goldenSeed = silverSeed + -7046029254386353131L;
      return new Xoroshiro128PlusPlus.Seed128(mixStafford13(silverSeed), mixStafford13(goldenSeed));
   }

   public static long seedUniquifier() {
      return SEED_UNIQUIFIER.updateAndGet(l -> l * 1181783497276652981L) ^ System.nanoTime();
   }

   public Xoroshiro128PlusPlus(long loSeed, long hiSeed) {
      this.seed = new Xoroshiro128PlusPlus.Seed128(loSeed, hiSeed);
      if ((loSeed | hiSeed) == 0L) {
         this.seed = new Xoroshiro128PlusPlus.Seed128(-7046029254386353131L, 7640891576956012809L);
      }
   }

   public Xoroshiro128PlusPlus(long seed) {
      this.seed = upgradeSeedTo128bit(seed);
   }

   @Override
   public int nextInt() {
      return (int)this.nextLong();
   }

   @Override
   public int nextInt(int n) {
      if (n <= 0) {
         throw new IllegalArgumentException("Bound must be positive");
      } else {
         long r = this.nextLong() & Mth.MASK_32;
         r *= n;
         long lowerBits = r & Mth.MASK_32;
         if (lowerBits < n) {
            for (int bound = Integer.remainderUnsigned(~n + 1, n); lowerBits < bound; lowerBits = r & Mth.MASK_32) {
               r = this.nextLong() & Mth.MASK_32;
               r *= n;
            }
         }

         return (int)(r >> 32);
      }
   }

   @Override
   public boolean nextBoolean() {
      return (this.nextLong() & 1L) != 0L;
   }

   @Override
   public float nextFloat() {
      return (float)this.nextBits(24) * 5.9604645E-8F;
   }

   @Override
   public double nextDouble() {
      return this.nextBits(53) * 1.110223E-16F;
   }

   public void consumeCount(int n) {
      for (int i = 0; i < n; i++) {
         this.nextLong();
      }
   }

   @Override
   public double nextGaussian() {
      if (this.haveNextNextGaussian) {
         this.haveNextNextGaussian = false;
         return this.nextNextGaussian;
      } else {
         double s;
         double v1;
         double v2;
         do {
            v1 = 2.0 * this.nextDouble() - 1.0;
            v2 = 2.0 * this.nextDouble() - 1.0;
            s = v1 * v1 + v2 * v2;
         } while (s >= 1.0 || s == 0.0);

         double multiplier = StrictMath.sqrt(-2.0 * StrictMath.log(s) / s);
         this.nextNextGaussian = v2 * multiplier;
         this.haveNextNextGaussian = true;
         return v1 * multiplier;
      }
   }

   private long nextBits(int n) {
      return this.nextLong() >>> 64 - n;
   }

   @Override
   public long nextLong() {
      long lowSeed = this.seed.loSeed;
      long hiSeed = this.seed.hiSeed;
      long res = Long.rotateLeft(lowSeed + hiSeed, 17) + lowSeed;
      long var7;
      this.seed.loSeed = Long.rotateLeft(lowSeed, 49) ^ (var7 = hiSeed ^ lowSeed) ^ var7 << 21;
      this.seed.hiSeed = Long.rotateLeft(var7, 28);
      return res;
   }

   public static class Seed128 {
      public long hiSeed;
      public long loSeed;

      public Seed128(long lo, long hi) {
         this.loSeed = lo;
         this.hiSeed = hi;
      }

      @Override
      public String toString() {
         return "Seed128{hiSeed=" + this.hiSeed + ", loSeed=" + this.loSeed + '}';
      }
   }
}
