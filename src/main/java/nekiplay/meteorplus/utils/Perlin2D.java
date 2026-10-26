package nekiplay.meteorplus.utils;

import java.util.Random;

public class Perlin2D {
   byte[] permutationTable;

   public Perlin2D(int seed) {
      Random rand = new Random(seed);
      this.permutationTable = new byte[1024];
      rand.nextBytes(this.permutationTable);
   }

   public Perlin2D() {
      Random rand = new Random(0L);
      this.permutationTable = new byte[1024];
      rand.nextBytes(this.permutationTable);
   }

   private float[] GetPseudoRandomGradientVector(int x, int y) {
      int v = (int)((x * 1836311903L ^ y * 2971215073L + 4807526976L) & 1023L);
      v = this.permutationTable[v] & 3;

      return switch (v) {
         case 0 -> new float[]{1.0F, 0.0F};
         case 1 -> new float[]{-1.0F, 0.0F};
         case 2 -> new float[]{0.0F, 1.0F};
         default -> new float[]{0.0F, -1.0F};
      };
   }

   static float QunticCurve(float t) {
      return t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
   }

   static float Lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   static float Dot(float[] a, float[] b) {
      return a[0] * b[0] + a[1] * b[1];
   }

   public float Noise(float fx, float fy) {
      int left = (int)Math.floor(fx);
      int top = (int)Math.floor(fy);
      float pointInQuadX = fx - left;
      float pointInQuadY = fy - top;
      float[] topLeftGradient = this.GetPseudoRandomGradientVector(left, top);
      float[] topRightGradient = this.GetPseudoRandomGradientVector(left + 1, top);
      float[] bottomLeftGradient = this.GetPseudoRandomGradientVector(left, top + 1);
      float[] bottomRightGradient = this.GetPseudoRandomGradientVector(left + 1, top + 1);
      float[] distanceToTopLeft = new float[]{pointInQuadX, pointInQuadY};
      float[] distanceToTopRight = new float[]{pointInQuadX - 1.0F, pointInQuadY};
      float[] distanceToBottomLeft = new float[]{pointInQuadX, pointInQuadY - 1.0F};
      float[] distanceToBottomRight = new float[]{pointInQuadX - 1.0F, pointInQuadY - 1.0F};
      float tx1 = Dot(distanceToTopLeft, topLeftGradient);
      float tx2 = Dot(distanceToTopRight, topRightGradient);
      float bx1 = Dot(distanceToBottomLeft, bottomLeftGradient);
      float bx2 = Dot(distanceToBottomRight, bottomRightGradient);
      pointInQuadX = QunticCurve(pointInQuadX);
      pointInQuadY = QunticCurve(pointInQuadY);
      float tx = Lerp(tx1, tx2, pointInQuadX);
      float bx = Lerp(bx1, bx2, pointInQuadX);
      return Lerp(tx, bx, pointInQuadY);
   }

   public float Noise(float fx, float fy, int octaves, float persistence) {
      float amplitude = 1.0F;
      float max = 0.0F;
      float result = 0.0F;

      while (octaves-- > 0) {
         max += amplitude;
         result += this.Noise(fx, fy) * amplitude;
         amplitude *= persistence;
         fx *= 2.0F;
         fy *= 2.0F;
      }

      return result / max;
   }
}
