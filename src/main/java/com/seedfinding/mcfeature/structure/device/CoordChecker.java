package com.seedfinding.mcfeature.structure.device;

@FunctionalInterface
public interface CoordChecker {
   boolean test(int var1, int var2, long var3, ParentInfo var5);

   @FunctionalInterface
   public interface Head extends CoordChecker {
      boolean test(int var1, int var2, long var3);

      @Override
      default boolean test(int x, int z, long mask, ParentInfo parent) {
         return this.test(x, z, mask);
      }
   }
}
