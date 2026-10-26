package nekiplay.meteorplus.features.modules.integrations;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import nekiplay.meteorplus.MeteorPlusAddon;

public class WhereIsIt extends Module {
   public SettingGroup defaultGroup = this.settings.getDefaultGroup();
   public final Setting<Boolean> background = this.defaultGroup.add(((Builder)((Builder)new Builder().name("draw-background")).defaultValue(true)).build());
   public final Setting<Boolean> support_color_symbols = this.defaultGroup
      .add(((Builder)((Builder)new Builder().name("use-color-symbols")).defaultValue(true)).build());
   public final Setting<SettingColor> text_color = this.defaultGroup
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("text-color"))
               .visible(() -> !(Boolean)this.support_color_symbols.get()))
            .build()
      );
   public final Setting<Double> y_offset = this.defaultGroup
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("y-offset"))
               .description("change-y-offset."))
            .defaultValue(-1.0)
            .max(15.0)
            .min(-15.0)
            .sliderRange(-15.0, 15.0)
            .build()
      );

   public WhereIsIt() {
      super(MeteorPlusAddon.CATEGORYMODS, "where-is-it", "ChestTracker modification.");
   }
}
