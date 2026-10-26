package nekiplay.meteorplus.utils;

public class NumeralUtils {
   public static String FormatNumber(long count) {
      if (count < 1000L) {
         return count + "";
      } else {
         int exp = (int)(Math.log(count) / Math.log(1000.0));
         return String.format("%.1f %c", count / Math.pow(1000.0, exp), "kMGTPE".charAt(exp - 1));
      }
   }
}
