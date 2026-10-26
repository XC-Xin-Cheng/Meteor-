package nekiplay.meteorplus.settings.items;

import java.util.Map;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.ColorSetting.Builder;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class HighlightItemDataScreen extends WindowScreen {
   private final HighlightItemData blockData;
   private final Setting<?> setting;
   @Nullable
   private final Runnable firstChangeConsumer;

   public HighlightItemDataScreen(GuiTheme theme, HighlightItemData blockData, Item block, ItemDataSetting<HighlightItemData> setting) {
      this(theme, blockData, setting, () -> ((Map)setting.get()).put(block, blockData));
   }

   public HighlightItemDataScreen(GuiTheme theme, HighlightItemData blockData, GenericSetting<HighlightItemData> setting) {
      this(theme, blockData, setting, null);
   }

   private HighlightItemDataScreen(GuiTheme theme, HighlightItemData blockData, Setting<?> setting, @Nullable Runnable firstChangeConsumer) {
      super(theme, "Configure Items");
      this.blockData = blockData;
      this.setting = setting;
      this.firstChangeConsumer = firstChangeConsumer;
   }

   public void initWidgets() {
      Settings settings = new Settings();
      SettingGroup sgGeneral = settings.getDefaultGroup();
      sgGeneral.add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("color")).description("Color of item."))
                  .defaultValue(new SettingColor(0, 255, 200))
                  .onModuleActivated(settingColorSetting -> ((SettingColor)settingColorSetting.get()).set(this.blockData.Color)))
               .onChanged(settingColor -> {
                  if (!this.blockData.Color.equals(settingColor)) {
                     this.blockData.Color.set(settingColor);
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
