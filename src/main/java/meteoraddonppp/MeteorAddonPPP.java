package meteoraddonppp;

import java.lang.reflect.Field;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MeteorAddonPPP extends MeteorAddon {
   public static final String MOD_ID = "meteor-addon-ppp";
   public static final String DISPLAY_NAME = "Meteor Addon+++";
   public static final String CATEGORY_NAME = "Meteor+++";
   public static final Category CATEGORY = new Category("Meteor+++", () -> new ItemStack(Items.NETHER_STAR));
   public static final String[] BUNDLED_PACKAGES = new String[]{
      "anticope.rejects",
      "com.kkllffaa.meteor_litematica_printer",
      "nekiplay.meteorplus",
      "fr.crazycat256.satellite",
      "zgoly.meteorist",
      "cn.blockforge.meteorzhcn",
      "com.stash.hunt"
   };
   private static final String[] MOVED_PACKAGES = new String[]{
      "anticope.rejects", "com.kkllffaa.meteor_litematica_printer", "fr.crazycat256.satellite", "zgoly.meteorist", "cn.blockforge.meteorzhcn", "com.stash.hunt"
   };
   private static final String[][] OLD_CATEGORY_FIELDS = new String[][]{
      {"anticope.rejects.MeteorRejectsAddon", "CATEGORY"},
      {"com.kkllffaa.meteor_litematica_printer.Addon", "CATEGORY"},
      {"fr.crazycat256.satellite.Addon", "CATEGORY"},
      {"zgoly.meteorist.Meteorist", "CATEGORY"},
      {"zgoly.meteorist.Meteorist", "DEV_CATEGORY"},
      {"cn.blockforge.meteorzhcn.MeteorZhCn", "CATEGORY"},
      {"com.stash.hunt.Addon", "CATEGORY"}
   };
   private static final String[][] ORIGINAL_NAMES = new String[][]{
      {"anticope.rejects.MeteorRejectsAddon", "Meteor Rejects"},
      {"com.kkllffaa.meteor_litematica_printer.Addon", "Meteor Litematica Printer"},
      {"nekiplay.meteorplus.MeteorPlusAddon", "Meteor+"},
      {"fr.crazycat256.satellite.Addon", "Meteor Satellite Addon"},
      {"zgoly.meteorist.Meteorist", "Meteorist"},
      {"cn.blockforge.meteorzhcn.MeteorZhCn", "Meteor-I18n-XC"},
      {"com.stash.hunt.Addon", "Jefff's Mods"}
   };

   public void onInitialize() {
      int var1 = -1;
      int var2 = -1;

      try {
         var1 = Modules.get().getAll().size();
         var2 = 0;

         for (Module var4 : Modules.get().getAll()) {
            if (var4.category == CATEGORY) {
               var2++;
            }
         }
      } catch (Throwable var5) {
      }

      MeteorClient.LOG.info("[{}] {} modules loaded, {} of them in category '{}'.", new Object[]{"Meteor Addon+++", var1, var2, "Meteor+++"});
   }

   public void onRegisterCategories() {
      restoreOriginalAddonNames();
      Modules.registerCategory(CATEGORY);
      List var1 = getCategories();
      if (var1 != null) {
         var1.remove(CATEGORY);
         var1.add(0, CATEGORY);

         for (String[] var5 : OLD_CATEGORY_FIELDS) {
            Category var6 = readCategory(var5[0], var5[1]);
            if (var6 != null) {
               var1.remove(var6);
            }
         }
      }
   }

   private static List<Category> getCategories() {
      try {
         Field var0 = Modules.class.getDeclaredField("CATEGORIES");
         var0.setAccessible(true);
         return (List<Category>)var0.get(null);
      } catch (Throwable var1) {
         return null;
      }
   }

   private static Category readCategory(String var0, String var1) {
      try {
         Class var2 = Class.forName(var0);
         if (var2.getField(var1).get(null) instanceof Category var4) {
            return var4;
         }
      } catch (Throwable var5) {
      }

      return null;
   }

   private static void restoreOriginalAddonNames() {
      for (MeteorAddon var1 : AddonManager.ADDONS) {
         String var2 = var1.getClass().getName();

         for (String[] var6 : ORIGINAL_NAMES) {
            if (var6[0].equals(var2)) {
               var1.name = var6[1];
               break;
            }
         }
      }
   }

   public static boolean movesToMainCategory(String var0) {
      for (String var4 : MOVED_PACKAGES) {
         if (var0.startsWith(var4)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isBundledComponent(MeteorAddon var0) {
      if (var0 == null) {
         return false;
      } else {
         String var1 = var0.getPackage();
         if (var1 == null) {
            return false;
         } else {
            for (String var5 : BUNDLED_PACKAGES) {
               if (var5.equals(var1)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public String getPackage() {
      return "meteoraddonppp";
   }

   public String getWebsite() {
      return null;
   }

   public GithubRepo getRepo() {
      return null;
   }

   public String getCommit() {
      return null;
   }
}
