package com.seedfinding.mcbiome.layer.cache;

import com.seedfinding.mcmath.util.Mth;
import java.util.Arrays;

public class IntLayerCache {
   private final long[] keys;
   private final int[] values;
   private final int mask;

   public IntLayerCache(int capacity) {
      if (capacity >= 1 && Mth.isPowerOf2(capacity)) {
         this.keys = new long[capacity];
         Arrays.fill(this.keys, -1L);
         this.values = new int[capacity];
         this.mask = (int)Mth.getMask(Long.numberOfTrailingZeros(capacity));
      } else {
         throw new UnsupportedOperationException("capacity must be a power of 2");
      }
   }

   public int get(int x, int y, int z, IntLayerCache.Sampler sampler) {
      long key = this.uniqueHash(x, y, z);
      int id = this.murmur64(key) & this.mask;
      if (this.keys[id] == key) {
         return this.values[id];
      } else {
         int value = sampler.sample(x, y, z);
         this.keys[id] = key;
         this.values[id] = value;
         return value;
      }
   }

   public int getWithoutStore(int x, int y, int z, IntLayerCache.Sampler sampler) {
      long key = this.uniqueHash(x, y, z);
      int id = this.murmur64(key) & this.mask;
      return this.keys[id] == key ? this.values[id] : sampler.sample(x, y, z);
   }

   public int forceStoreAndGet(int x, int y, int z, IntLayerCache.Sampler sampler) {
      long key = this.uniqueHash(x, y, z);
      int id = this.murmur64(key) & this.mask;
      int value = sampler.sample(x, y, z);
      this.keys[id] = key;
      this.values[id] = value;
      return value;
   }

   public long uniqueHash(int x, int y, int z) {
      long hash = x & Mth.getMask(26);
      hash |= (z & Mth.getMask(26)) << 26;
      return hash | (y & Mth.getMask(8)) << 52;
   }

   public int murmur64(long value) {
      value ^= value >>> 33;
      value *= -49064778989728563L;
      value ^= value >>> 33;
      value *= -4265267296055464877L;
      value ^= value >>> 33;
      return (int)value;
   }

   @FunctionalInterface
   public interface Sampler {
      int sample(int var1, int var2, int var3);
   }
}
