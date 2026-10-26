package zgoly.meteorist.utils.misc;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.events.TweenEndEvent;

public class TweenHandler {
   private static double currentTime;
   private final List<TweenHandler.Tween> tweens = new ArrayList<>();

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      currentTime = currentTime + event.frameTime;
      Iterator<TweenHandler.Tween> iterator = this.tweens.iterator();

      while (iterator.hasNext()) {
         TweenHandler.Tween tween = iterator.next();
         tween.update();
         if (tween.completed) {
            iterator.remove();
         }
      }
   }

   public void play(
      boolean instant,
      AtomicDouble reference,
      double endValue,
      double duration,
      TweenHandler.EasingStyle easingStyle,
      TweenHandler.EasingDirection easingDirection
   ) {
      if (instant) {
         this.stopPreviousTween(reference);
         reference.set(endValue);
      } else {
         this.play(reference, endValue, duration, easingStyle, easingDirection);
      }
   }

   public void play(
      AtomicDouble reference, double endValue, double duration, TweenHandler.EasingStyle easingStyle, TweenHandler.EasingDirection easingDirection
   ) {
      this.stopPreviousTween(reference);
      TweenHandler.Tween tween = new TweenHandler.Tween(reference, endValue, duration, easingStyle, easingDirection);
      this.tweens.add(tween);
   }

   private void stopPreviousTween(AtomicDouble reference) {
      this.tweens.removeIf(tween -> Objects.equals(tween.reference, reference));
   }

   public static enum EasingDirection {
      In,
      Out,
      InOut,
      OutIn;
   }

   public static class EasingFunctions {
      public static double ease(TweenHandler.EasingStyle easingStyle, TweenHandler.EasingDirection easingDirection, double x) {
         return switch (easingDirection) {
            case In -> easeIn(easingStyle, x);
            case Out -> 1.0 - easeIn(easingStyle, 1.0 - x);
            case InOut -> x < 0.5 ? easeIn(easingStyle, 2.0 * x) / 2.0 : 1.0 - easeIn(easingStyle, 2.0 * (1.0 - x)) / 2.0;
            case OutIn -> x < 0.5 ? (1.0 - easeIn(easingStyle, 1.0 - 2.0 * x)) / 2.0 : easeIn(easingStyle, 2.0 * x - 1.0) / 2.0 + 0.5;
         };
      }

      private static double easeIn(TweenHandler.EasingStyle easingStyle, double x) {
         switch (easingStyle) {
            case Sine:
               return 1.0 - Math.cos(x * Math.PI / 2.0);
            case Quad:
               return x * x;
            case Cubic:
               return x * x * x;
            case Quart:
               return x * x * x * x;
            case Quint:
               return x * x * x * x * x;
            case Expo:
               return x == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * x - 10.0);
            case Circ:
               return 1.0 - Math.sqrt(1.0 - Math.pow(x, 2.0));
            case Back:
               double c1 = 1.70158;
               double c3 = c1 + 1.0;
               return c3 * x * x * x - c1 * x * x;
            case Elastic:
               double c4 = Math.PI * 2.0 / 3.0;
               return x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : -Math.pow(2.0, 10.0 * x - 10.0) * Math.sin((x * 10.0 - 10.75) * c4));
            case Bounce:
               double n1 = 7.5625;
               double d1 = 2.75;
               if (x < 1.0 / d1) {
                  return n1 * x * x;
               } else if (x < 2.0 / d1) {
                  double var9;
                  return n1 * (var9 = x - 1.5 / d1) * var9 + 0.75;
               } else {
                  if (x < 2.5 / d1) {
                     double var8;
                     return n1 * (var8 = x - 2.25 / d1) * var8 + 0.9375;
                  }

                  double var7;
                  return n1 * (var7 = x - 2.625 / d1) * var7 + 0.984375;
               }
            default:
               return x;
         }
      }
   }

   public static enum EasingStyle {
      Linear,
      Sine,
      Quad,
      Cubic,
      Quart,
      Quint,
      Expo,
      Circ,
      Back,
      Elastic,
      Bounce;
   }

   public static class Tween {
      private final AtomicDouble reference;
      private final double startValue;
      private final double endValue;
      private final double duration;
      private final TweenHandler.EasingStyle easingStyle;
      private final TweenHandler.EasingDirection easingDirection;
      private final double startTime;
      private boolean completed = false;

      public Tween(AtomicDouble reference, double endValue, double duration, TweenHandler.EasingStyle easingStyle, TweenHandler.EasingDirection easingDirection) {
         this.reference = reference;
         this.startValue = reference.get();
         this.endValue = endValue;
         this.duration = duration;
         this.easingStyle = easingStyle;
         this.easingDirection = easingDirection;
         this.startTime = TweenHandler.currentTime;
      }

      public void update() {
         double elapsedTime = TweenHandler.currentTime - this.startTime;
         if (elapsedTime < this.duration) {
            double t = elapsedTime / this.duration;
            double easedValue = TweenHandler.EasingFunctions.ease(this.easingStyle, this.easingDirection, t);
            this.reference.set(this.startValue + (this.endValue - this.startValue) * easedValue);
         } else {
            this.reference.set(this.endValue);
            MeteorClient.EVENT_BUS.post(new TweenEndEvent());
            this.completed = true;
         }
      }
   }
}
