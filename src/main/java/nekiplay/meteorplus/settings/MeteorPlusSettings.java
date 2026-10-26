package nekiplay.meteorplus.settings;

import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory.Factory;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import nekiplay.meteorplus.settings.items.ItemDataSetting;
import nekiplay.meteorplus.settings.items.ItemDataSettingScreen;

public class MeteorPlusSettings {
   private final Map<Class<?>, Factory> factories;
   private final GuiTheme theme;

   public MeteorPlusSettings(Map<Class<?>, Factory> factories, GuiTheme theme) {
      this.factories = factories;
      this.theme = theme;
   }

   public void addSettings() {
      this.factories.put(ItemDataSetting.class, (table, setting) -> this.stringMapW(table, (ItemDataSetting<?>)setting));
   }

   private void stringMapW(WTable table, ItemDataSetting<?> setting) {
      WButton button = (WButton)table.add(this.theme.button(GuiRenderer.EDIT)).expandCellX().widget();
      button.action = () -> MeteorClient.mc.setScreen(new ItemDataSettingScreen(this.theme, setting));
   }
}
