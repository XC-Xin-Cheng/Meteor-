package nekiplay.meteorplus.utils.algoritms;

public class Smooth {
   public static double getDouble(Smooth.SmoothType type, double diffAngle, double minRotation, double maxRotation) {
      double speeds = 180.0;
      if (type == Smooth.SmoothType.Linear) {
         speeds = diffAngle / 180.0 * maxRotation + (1.0 - diffAngle / 180.0) * minRotation;
      } else if (type == Smooth.SmoothType.Quad) {
         speeds = Math.pow(diffAngle / 180.0, 2.0) * maxRotation + (1.0 - Math.pow(diffAngle / 180.0, 2.0)) * minRotation;
      } else if (type == Smooth.SmoothType.Sine || type == Smooth.SmoothType.QuadSine) {
         double v = -Math.cos(diffAngle / 180.0 * Math.PI) * 0.5 + 0.5;
         if (type == Smooth.SmoothType.Sine) {
            speeds = v * maxRotation + (Math.cos(diffAngle / 180.0 * Math.PI * 0.5 + 0.5) * 0.5 + 0.5) * minRotation;
         } else {
            speeds = Math.pow(v, 2.0) * maxRotation + (1.0 - Math.pow(v, 2.0)) * minRotation;
         }
      } else if (type == Smooth.SmoothType.Perlin) {
         int noice = PerlinNoice.PerlinNoice(maxRotation);
         if (noice > 0) {
            speeds = noice;
         } else {
            speeds = maxRotation;
         }
      } else if (type == Smooth.SmoothType.PerlinRandom) {
         double random = RandomUtils.nextDouble(minRotation, maxRotation);
         int noice = PerlinNoice.PerlinNoice(random);
         if (noice > 0) {
            speeds = noice;
         } else {
            speeds = random;
         }
      }

      return speeds;
   }

   public static enum SmoothType {
      None,
      Linear,
      Quad,
      Sine,
      QuadSine,
      Perlin,
      PerlinRandom;
   }
}
