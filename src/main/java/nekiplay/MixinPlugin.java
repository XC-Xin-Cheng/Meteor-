package nekiplay;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinPlugin implements IMixinConfigPlugin {
   public static final Logger LOG = LoggerFactory.getLogger(MixinPlugin.class);
   public static final String METEOR_LOGPREFIX_MIXIN = "[Meteor+ Mixins]";
   private static final String mixinPackageMeteorPlus = "nekiplay.meteorplus.mixin";
   public static boolean isMeteorClient = false;
   public static boolean isMeteorRejects = false;
   public static boolean isNumbyHack = false;
   public static boolean isZewo2 = false;
   public static boolean isBozeAPI = false;
   public static boolean isFutureClient = false;
   public static boolean isBaritonePresent = false;
   public static boolean isXaeroWorldMapPresent = false;
   public static boolean isXaeroMiniMapPresent = false;
   public static boolean isXaeroPlusMapPresent = false;
   public static boolean isWhereIsIt = false;

   public void onLoad(String mixinPackage) {
      FabricLoader loader = FabricLoader.getInstance();
      isMeteorClient = loader.isModLoaded("meteor-client");
      isBozeAPI = loader.isModLoaded("boze-api");
      isFutureClient = loader.isModLoaded("future");
      isMeteorRejects = loader.isModLoaded("meteor-rejects");
      isNumbyHack = loader.isModLoaded("numbyhack");
      isZewo2 = loader.isModLoaded("zewo2");
      isBaritonePresent = loader.isModLoaded("baritone-meteor") || loader.isModLoaded("baritone");
      isXaeroWorldMapPresent = loader.isModLoaded("xaeroworldmap");
      isXaeroMiniMapPresent = loader.isModLoaded("xaerominimap");
      isXaeroPlusMapPresent = loader.isModLoaded("xaeroplus");
      isWhereIsIt = loader.isModLoaded("whereisit");
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      if (!mixinClassName.startsWith("nekiplay.meteorplus.mixin")) {
         throw new RuntimeException("[Meteor+ Mixins] " + mixinClassName + " is not in the mixin package");
      } else if (mixinClassName.startsWith("nekiplay.meteorplus.mixin.meteorclient")) {
         return !mixinClassName.contains("FreecamMixin") && !mixinClassName.contains("WaypointsModuleMixin")
            ? isMeteorClient
            : isBaritonePresent && isMeteorClient;
      } else if (mixinClassName.startsWith("nekiplay.meteorplus.mixin.xaero.worldmap")) {
         return isBaritonePresent && isXaeroWorldMapPresent && isMeteorClient;
      } else if (!mixinClassName.startsWith("nekiplay.meteorplus.mixin.whereisit")) {
         return mixinClassName.startsWith("nekiplay.meteorplus.mixin.minecraft") ? isMeteorClient : false;
      } else {
         return isWhereIsIt && isMeteorClient;
      }
   }

   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
