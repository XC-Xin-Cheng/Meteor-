package cn.blockforge.meteorzhcn.util;

import java.util.Set;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.chat.Component;

public final class TransUtil {
   private TransUtil() {
   }

   private static String trans(String s) {
      return Component.translatable(s).getString();
   }

   public static String trans(String key, String alternative) {
      String trans = trans(key);
      return trans.equals(key) ? alternative : trans;
   }

   public static boolean hasKey(String key) {
      return !trans(key).equals(key);
   }

   public static Set<String> getAddonNames() {
      return AddonManager.ADDONS.stream().map(addon -> addon.name).map(TransUtil::baseFormat).collect(Collectors.toSet());
   }

   public static String getAddonName(Module m) {
      return baseFormat(m.addon == null ? "unknow_addon" : m.addon.name);
   }

   public static String baseFormat(String s) {
      String lower = s.toLowerCase();
      int length = lower.length();

      for (int i = 0; i < length; i++) {
         char c = lower.charAt(i);
         if (c == ' ' || c == '-' || c == '.' || c == '"') {
            StringBuilder sb = new StringBuilder(length);

            for (int j = 0; j < length; j++) {
               char current = lower.charAt(j);
               sb.append(current != ' ' && current != '-' && current != '.' && current != '"' ? current : '_');
            }

            return sb.toString();
         }
      }

      return lower;
   }

   public static String formatValue(String s) {
      return s.replace("\"", "\\\"");
   }
}
