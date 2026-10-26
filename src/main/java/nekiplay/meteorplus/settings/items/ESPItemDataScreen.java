package nekiplay.meteorplus.settings.items;

import java.util.Map;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class ESPItemDataScreen extends WindowScreen {
   private final ESPItemData blockData;
   private final Setting<?> setting;
   @Nullable
   private final Runnable firstChangeConsumer;

   public ESPItemDataScreen(GuiTheme theme, ESPItemData blockData, Item block, ItemDataSetting<ESPItemData> setting) {
      this(theme, blockData, setting, () -> ((Map)setting.get()).put(block, blockData));
   }

   public ESPItemDataScreen(GuiTheme theme, ESPItemData blockData, GenericSetting<ESPItemData> setting) {
      this(theme, blockData, setting, null);
   }

   private ESPItemDataScreen(GuiTheme theme, ESPItemData blockData, Setting<?> setting, @Nullable Runnable firstChangeConsumer) {
      super(theme, "Configure Items");
      this.blockData = blockData;
      this.setting = setting;
      this.firstChangeConsumer = firstChangeConsumer;
   }

   public void initWidgets() {
      Settings settings = new Settings();
      SettingGroup sgGeneral = settings.getDefaultGroup();
      SettingGroup sgTracer = settings.createGroup("Tracer");
      sgGeneral.add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("shape-mode")).description("How the shape is rendered."))
                     .defaultValue(ShapeMode.Lines))
                  .onModuleActivated(shapeModeSetting -> shapeModeSetting.set(this.blockData.shapeMode)))
               .onChanged(shapeMode -> {
                  if (this.blockData.shapeMode != shapeMode) {
                     this.blockData.shapeMode = shapeMode;
                     this.onChanged();
                  }
               }))
            .build()
      );
      sgGeneral.add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                        .name("line-color"))
                     .description("Color of lines."))
                  .defaultValue(new SettingColor(0, 255, 200))
                  .onModuleActivated(settingColorSetting -> ((SettingColor)settingColorSetting.get()).set(this.blockData.lineColor)))
               .onChanged(settingColor -> {
                  if (!this.blockData.lineColor.equals(settingColor)) {
                     this.blockData.lineColor.set(settingColor);
                     this.onChanged();
                  }
               }))
            .build()
      );
      sgGeneral.add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                        .name("side-color"))
                     .description("Color of sides."))
                  .defaultValue(new SettingColor(0, 255, 200, 25))
                  .onModuleActivated(settingColorSetting -> ((SettingColor)settingColorSetting.get()).set(this.blockData.sideColor)))
               .onChanged(settingColor -> {
                  if (!this.blockData.sideColor.equals(settingColor)) {
                     this.blockData.sideColor.set(settingColor);
                     this.onChanged();
                  }
               }))
            .build()
      );
      sgTracer.add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                           .name("tracer"))
                        .description("If tracer line is allowed to this block."))
                     .defaultValue(true))
                  .onModuleActivated(booleanSetting -> booleanSetting.set(this.blockData.tracer)))
               .onChanged(aBoolean -> {
                  if (this.blockData.tracer != aBoolean) {
                     this.blockData.tracer = aBoolean;
                     this.onChanged();
                  }
               }))
            .build()
      );
      sgTracer.add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                        .name("tracer-color"))
                     .description("Color of tracer line."))
                  .defaultValue(new SettingColor(0, 255, 200, 125))
                  .onModuleActivated(settingColorSetting -> settingColorSetting.set(this.blockData.tracerColor)))
               .onChanged(settingColor -> {
                  if (!this.blockData.tracerColor.equals(settingColor)) {
                     this.blockData.tracerColor.set(settingColor);
                     this.onChanged();
                  }
               }))
            .build()
      );
      settings.onActivated();
      this.add(this.theme.settings(settings)).expandX();
   }

   private void onChanged() {
      if (!this.blockData.isChanged() && this.firstChangeConsumer != null) {
         this.firstChangeConsumer.run();
      }

      this.setting.onChanged();
      this.blockData.changed();
   }
}
