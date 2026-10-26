package fr.crazycat256.satellite.utils;

public class MathUtils {
   public static double mod(double x, double y) {
      return (x % y + y) % y;
   }
}
