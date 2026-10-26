package cn.blockforge.meteorzhcn.util.trans_engine;

import cn.blockforge.meteorzhcn.util.TransUtil;
import java.util.function.Function;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class TransEngineAuto extends AbstractTransEngine {
   private static final IKeyGenerate[] CHAIN = new IKeyGenerate[]{new TransEngineNew(), new TransEngineOld(), new TransEngineLegacy()};

   private static String firstHit(Function<IKeyGenerate, String> keyOf) {
      String fallback = null;

      for (IKeyGenerate engine : CHAIN) {
         String key = keyOf.apply(engine);
         if (fallback == null) {
            fallback = key;
         }

         if (TransUtil.hasKey(key)) {
            return key;
         }
      }

      return fallback;
   }

   @Override
   public String getModuleNameKey(Module module) {
      return firstHit(engine -> engine.getModuleNameKey(module));
   }

   @Override
   public String getModuleDescriptionKey(Module module) {
      return firstHit(engine -> engine.getModuleDescriptionKey(module));
   }

   @Override
   public String getSettingNameKey(Module module, SettingGroup group, Setting<?> setting) {
      return firstHit(engine -> engine.getSettingNameKey(module, group, setting));
   }

   @Override
   public String getSettingDesKey(Module module, SettingGroup group, Setting<?> setting) {
      return firstHit(engine -> engine.getSettingDesKey(module, group, setting));
   }
}
