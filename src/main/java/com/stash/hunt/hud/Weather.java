package com.stash.hunt.hud;

import com.stash.hunt.Addon;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.Minecraft;

public class Weather extends HudElement {
   public static final HudElementInfo<Weather> INFO = new HudElementInfo(Addon.HUD_GROUP, "Weather", "Displays current weather", Weather::new);
   private Minecraft mc = null;
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private boolean recalculateSize;
   public final Setting<Double> scale = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("scale")).description("Custom scale.")).defaultValue(1.0).min(0.5).sliderRange(0.5, 3.0).build());

   public Weather() {
      super(INFO);
      this.mc = Minecraft.getInstance();
   }

   public void render(HudRenderer renderer) {
      String textWidth = "Weather:            ";
      this.setSize(renderer.textWidth(textWidth, true, (Double)this.scale.get()), renderer.textHeight(true, (Double)this.scale.get()));
      String weather = "None";
      if (this.mc.level != null && this.mc.level.canHaveWeather()) {
         if (this.mc.level.isThundering()) {
            weather = "Thundering";
         } else if (this.mc.level.isRaining()) {
            weather = "Raining";
         } else {
            weather = "Clear";
         }
      }

      renderer.text("Weather: ", this.x, this.y, Color.WHITE, true, (Double)this.scale.get());
      renderer.text(weather, renderer.textWidth("Weather: ", (Double)this.scale.get()) + this.x, this.y, Color.LIGHT_GRAY, true, (Double)this.scale.get());
   }
}
