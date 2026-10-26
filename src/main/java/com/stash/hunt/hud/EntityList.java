package com.stash.hunt.hud;

import com.stash.hunt.Addon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class EntityList extends HudElement {
   public static final HudElementInfo INFO = new HudElementInfo(
      Addon.HUD_GROUP, "EntityList", "Shows nearby entities in the HUD like in RusherHack", EntityList::new
   );
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> scale = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("scale")).description("Text scale.")).defaultValue(1.0).min(0.5).sliderMax(3.0).build());
   private final Setting<Boolean> gradient = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("gradient"))
                  .description("Enable vertical gradient color."))
               .defaultValue(false))
            .build()
      );
   private final Setting<SettingColor> solidColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("color"))
                  .description("Text color (only used when gradient is off)."))
               .defaultValue(new Color(255, 255, 255, 255))
               .visible(() -> !(Boolean)this.gradient.get()))
            .build()
      );
   private final Setting<SettingColor> gradientStart = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("gradient-start"))
                  .description("Top color of the gradient."))
               .defaultValue(new Color(255, 255, 255, 255))
               .visible(this.gradient::get))
            .build()
      );
   private final Setting<SettingColor> gradientEnd = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("gradient-end"))
                  .description("Bottom color of the gradient."))
               .defaultValue(new Color(150, 150, 150, 255))
               .visible(this.gradient::get))
            .build()
      );
   private final Map<String, Integer> entityCounts = new HashMap<>();

   public EntityList() {
      super(INFO);
   }

   public void render(HudRenderer renderer) {
      this.entityCounts.clear();
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         for (Entity e : mc.level.entitiesForRendering()) {
            if (e != mc.player && !e.isRemoved()) {
               String name = e.getName().getString();
               this.entityCounts.merge(name, 1, Integer::sum);
            }
         }

         if (!this.entityCounts.isEmpty()) {
            List<Entry<String, Integer>> entries = new ArrayList<>(this.entityCounts.entrySet());
            entries.sort(Entry.<String, Integer>comparingByValue().reversed());
            double lineHeight = renderer.textHeight(true, (Double)this.scale.get());
            double maxWidth = 0.0;

            for (Entry<String, Integer> entry : entries) {
               String line = entry.getKey();
               if (entry.getValue() > 1) {
                  line = line + " (" + entry.getValue() + ")";
               }

               maxWidth = Math.max(maxWidth, renderer.textWidth(line, true, (Double)this.scale.get()));
            }

            double padding = 4.0 * (Double)this.scale.get();
            this.setSize(maxWidth + padding * 2.0, lineHeight * entries.size() + padding * 2.0);
            Color start = (Color)this.gradientStart.get();
            Color end = (Color)this.gradientEnd.get();
            double total = Math.max(1, entries.size() - 1);
            double currentY = this.y + padding;

            for (int i = 0; i < entries.size(); i++) {
               Entry<String, Integer> entry = entries.get(i);
               String line = entry.getKey();
               if (entry.getValue() > 1) {
                  line = line + " (" + entry.getValue() + ")";
               }

               Color textColor;
               if ((Boolean)this.gradient.get()) {
                  double t = i / total;
                  textColor = new Color(
                     (int)(start.r + (end.r - start.r) * t),
                     (int)(start.g + (end.g - start.g) * t),
                     (int)(start.b + (end.b - start.b) * t),
                     (int)(start.a + (end.a - start.a) * t)
                  );
               } else {
                  textColor = (Color)this.solidColor.get();
               }

               renderer.text(line, this.x + padding, currentY, textColor, true, (Double)this.scale.get());
               currentY += lineHeight;
            }
         }
      }
   }
}
