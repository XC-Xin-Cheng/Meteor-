package nekiplay.meteorplus.utils.algoritms;

import java.util.Random;
import nekiplay.meteorplus.utils.Perlin2D;

public class PerlinNoice {
   public static int PerlinNoice(int multiply) {
      Perlin2D perlin = new Perlin2D(new Random().nextInt());
      float Phi = 0.70710677F;
      float noice = perlin.Noise(5.0F, 5.0F) + perlin.Noise(0.0F * Phi, 50.0F * Phi) * -1.0F;
      return (int)(noice * multiply);
   }

   public static int PerlinNoice(double multiply) {
      Perlin2D perlin = new Perlin2D(new Random().nextInt());
      float Phi = 0.70710677F;
      float noice = perlin.Noise(5.0F, 5.0F) + perlin.Noise(0.0F * Phi, 50.0F * Phi) * -1.0F;
      return (int)(noice * multiply);
   }
}
