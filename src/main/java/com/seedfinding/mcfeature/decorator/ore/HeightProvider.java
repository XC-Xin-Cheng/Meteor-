package com.seedfinding.mcfeature.decorator.ore;

import com.seedfinding.mcseed.rand.JRand;
import java.util.function.Function;

public interface HeightProvider {
   static HeightProvider range(int bottomOffset, int topOffset, int maximumY) {
      return rand -> rand.nextInt(maximumY - topOffset) + bottomOffset;
   }

   static HeightProvider depthAverage(int baseline, int spread) {
      return rand -> rand.nextInt(spread) + rand.nextInt(spread) - spread + baseline;
   }

   static HeightProvider uniformRange(int minOffset, int maxOffset) {
      return rand -> minOffset > maxOffset ? minOffset : HeightProvider.MathHelper.nextBetween(rand, minOffset, maxOffset);
   }

   static HeightProvider triangleRange(int minOffset, int maxOffset) {
      return rand -> {
         if (minOffset > maxOffset) {
            return minOffset;
         } else {
            int range = maxOffset - minOffset;
            if (range <= 0) {
               return HeightProvider.MathHelper.nextBetween(rand, minOffset, maxOffset);
            } else {
               int midPoint = range / 2;
               int midPoint2 = range - midPoint;
               return minOffset + HeightProvider.MathHelper.nextBetween(rand, 0, midPoint2) + HeightProvider.MathHelper.nextBetween(rand, 0, midPoint);
            }
         }
      };
   }

   static HeightProvider spreadRange(int baseValue, int spread) {
      return rand -> spread == 0 ? baseValue : baseValue + rand.nextInt(spread + 1);
   }

   static HeightProvider custom(Function<JRand, Integer> func) {
      return func::apply;
   }

   int getY(JRand var1);

   public static class MathHelper {
      static int nextBetween(JRand rand, int min, int max) {
         return rand.nextInt(max - min + 1) + min;
      }
   }
}
