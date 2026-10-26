package nekiplay.meteorplus.utils.algoritms;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
   public static float nextFloat(float min, float max) {
      return max <= min ? max : ThreadLocalRandom.current().nextFloat(min, max);
   }

   public static int nextInt(int min, int max) {
      return max <= min ? max : ThreadLocalRandom.current().nextInt(min, max);
   }

   public static double nextDouble(double min, double max) {
      return max <= min ? max : ThreadLocalRandom.current().nextDouble(min, max);
   }
}
