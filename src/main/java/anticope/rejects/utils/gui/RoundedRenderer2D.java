package anticope.rejects.utils.gui;

import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class RoundedRenderer2D {
   private static final double circleNone = 0.0;
   private static final double circleQuarter = Math.PI / 2;
   private static final double circleHalf = Math.PI;
   private static final double circleThreeQuarter = Math.PI * 3.0 / 2.0;

   public static void quadRoundedOutline(Renderer2D mb, double x, double y, double width, double height, Color color, double r, double s) {
      r = getR(r, width, height);
      if (r <= 0.0) {
         mb.quad(x, y, width, s, color);
         mb.quad(x, y + height - s, width, s, color);
         mb.quad(x, y + s, s, height - s * 2.0, color);
         mb.quad(x + width - s, y + s, s, height - s * 2.0, color);
      } else {
         circlePartOutline(mb, x + r, y + r, r, Math.PI * 3.0 / 2.0, Math.PI / 2, color, s);
         mb.quad(x + r, y, width - r * 2.0, s, color);
         circlePartOutline(mb, x + width - r, y + r, r, 0.0, Math.PI / 2, color, s);
         mb.quad(x, y + r, s, height - r * 2.0, color);
         mb.quad(x + width - s, y + r, s, height - r * 2.0, color);
         circlePartOutline(mb, x + width - r, y + height - r, r, Math.PI / 2, Math.PI / 2, color, s);
         mb.quad(x + r, y + height - s, width - r * 2.0, s, color);
         circlePartOutline(mb, x + r, y + height - r, r, Math.PI, Math.PI / 2, color, s);
      }
   }

   public static void quadRounded(Renderer2D mb, double x, double y, double width, double height, Color color, double r, boolean roundTop) {
      r = getR(r, width, height);
      if (r <= 0.0) {
         mb.quad(x, y, width, height, color);
      } else {
         if (roundTop) {
            circlePart(mb, x + r, y + r, r, Math.PI * 3.0 / 2.0, Math.PI / 2, color);
            mb.quad(x + r, y, width - 2.0 * r, r, color);
            circlePart(mb, x + width - r, y + r, r, 0.0, Math.PI / 2, color);
            mb.quad(x, y + r, width, height - 2.0 * r, color);
         } else {
            mb.quad(x, y, width, height - r, color);
         }

         circlePart(mb, x + width - r, y + height - r, r, Math.PI / 2, Math.PI / 2, color);
         mb.quad(x + r, y + height - r, width - 2.0 * r, r, color);
         circlePart(mb, x + r, y + height - r, r, Math.PI, Math.PI / 2, color);
      }
   }

   public static void quadRoundedSide(Renderer2D mb, double x, double y, double width, double height, Color color, double r, boolean right) {
      r = getR(r, width, height);
      if (r <= 0.0) {
         mb.quad(x, y, width, height, color);
      } else if (right) {
         circlePart(mb, x + width - r, y + r, r, 0.0, Math.PI / 2, color);
         circlePart(mb, x + width - r, y + height - r, r, Math.PI / 2, Math.PI / 2, color);
         mb.quad(x, y, width - r, height, color);
         mb.quad(x + width - r, y + r, r, height - r * 2.0, color);
      } else {
         circlePart(mb, x + r, y + r, r, Math.PI * 3.0 / 2.0, Math.PI / 2, color);
         circlePart(mb, x + r, y + height - r, r, Math.PI, Math.PI / 2, color);
         mb.quad(x + r, y, width - r, height, color);
         mb.quad(x, y + r, r, height - r * 2.0, color);
      }
   }

   private static double getR(double r, double w, double h) {
      if (r * 2.0 > h) {
         r = h / 2.0;
      }

      if (r * 2.0 > w) {
         r = w / 2.0;
      }

      return r;
   }

   private static int getCirDepth(double r, double angle) {
      return Math.max(1, (int)(angle * r / (Math.PI / 2)));
   }

   public static void circlePart(Renderer2D mb, double x, double y, double r, double startAngle, double angle, Color color) {
      int cirDepth = getCirDepth(r, angle);
      double cirPart = angle / cirDepth;
      int center = mb.triangles.vec2(x, y).color(color).next();
      int prev = vecOnCircle(mb, x, y, r, startAngle, color);

      for (int i = 1; i < cirDepth + 1; i++) {
         int next = vecOnCircle(mb, x, y, r, startAngle + cirPart * i, color);
         mb.triangles.quad(prev, center, next, next);
         prev = next;
      }
   }

   public static void circlePartOutline(Renderer2D mb, double x, double y, double r, double startAngle, double angle, Color color, double outlineWidth) {
      if (outlineWidth >= r) {
         circlePart(mb, x, y, r, startAngle, angle, color);
      } else {
         int cirDepth = getCirDepth(r, angle);
         double cirPart = angle / cirDepth;
         int innerPrev = vecOnCircle(mb, x, y, r - outlineWidth, startAngle, color);
         int outerPrev = vecOnCircle(mb, x, y, r, startAngle, color);

         for (int i = 1; i < cirDepth + 1; i++) {
            int inner = vecOnCircle(mb, x, y, r - outlineWidth, startAngle + cirPart * i, color);
            int outer = vecOnCircle(mb, x, y, r, startAngle + cirPart * i, color);
            mb.triangles.quad(inner, innerPrev, outerPrev, outer);
            innerPrev = inner;
            outerPrev = outer;
         }
      }
   }

   private static int vecOnCircle(Renderer2D mb, double x, double y, double r, double angle, Color color) {
      return mb.triangles.vec2(x + Math.sin(angle) * r, y - Math.cos(angle) * r).color(color).next();
   }
}
