package anticope.rejects.utils;

import java.util.List;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;

public class ConfigModifier {
   private static ConfigModifier INSTANCE;
   public final SettingGroup sgRejects = Config.get().settings.createGroup("Rejects");
   public final Setting<RejectsConfig.HttpAllowed> httpAllowed = this.sgRejects
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("http-allowed")).description("Changes what api endpoints can be reached."))
                  .defaultValue(RejectsConfig.get().httpAllowed))
               .onChanged(v -> RejectsConfig.get().httpAllowed = v))
            .build()
      );
   public final Setting<String> httpUserAgent = this.sgRejects
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("http-user-agent"))
                     .description("Changes the HTTP user agent. Empty for none."))
                  .defaultValue(RejectsConfig.get().httpUserAgent))
               .onChanged(v -> RejectsConfig.get().httpUserAgent = v))
            .build()
      );
   public final Setting<List<Module>> hiddenModules = this.sgRejects
      .add(
         ((meteordevelopment.meteorclient.settings.ModuleListSetting.Builder)((meteordevelopment.meteorclient.settings.ModuleListSetting.Builder)((meteordevelopment.meteorclient.settings.ModuleListSetting.Builder)((meteordevelopment.meteorclient.settings.ModuleListSetting.Builder)((meteordevelopment.meteorclient.settings.ModuleListSetting.Builder)new meteordevelopment.meteorclient.settings.ModuleListSetting.Builder()
                           .name("hidden-modules"))
                        .description("Which modules to hide."))
                     .defaultValue(List.of()))
                  .defaultValue(RejectsConfig.get().getHiddenModules()))
               .onChanged(v -> RejectsConfig.get().setHiddenModules(v)))
            .build()
      );
   public final Setting<Boolean> loadSystemFonts = this.sgRejects
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                           .name("load-system-fonts"))
                        .description("Disabling this for faster launch. You can put font into meteor-client/fonts folder. Restart to take effect."))
                     .defaultValue(true))
                  .defaultValue(RejectsConfig.get().loadSystemFonts))
               .onChanged(v -> RejectsConfig.get().loadSystemFonts = v))
            .build()
      );
   public final Setting<Boolean> duplicateModuleNames = this.sgRejects
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                           .name("duplicate-module-names"))
                        .description("Allow duplicate module names. Best for addon compatibility."))
                     .defaultValue(false))
                  .defaultValue(RejectsConfig.get().duplicateModuleNames))
               .onChanged(v -> RejectsConfig.get().duplicateModuleNames = v))
            .build()
      );

   public static ConfigModifier get() {
      if (INSTANCE == null) {
         INSTANCE = new ConfigModifier();
      }

      return INSTANCE;
   }
}
