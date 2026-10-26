package anticope.rejects.settings;

import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory.Factory;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

public class RejectsSettings {
   private final Map<Class<?>, Factory> factories;
   private final GuiTheme theme;

   public RejectsSettings(Map<Class<?>, Factory> factories, GuiTheme theme) {
      this.factories = factories;
      this.theme = theme;
   }

   public void addSettings() {
      this.factories.put(StringMapSetting.class, (table, setting) -> this.stringMapW(table, (StringMapSetting)setting));
      this.factories.put(GameModeListSetting.class, (table, setting) -> this.gameModeListW(table, (GameModeListSetting)setting));
   }

   private void stringMapW(WTable table, StringMapSetting setting) {
      WTable wtable = (WTable)table.add(this.theme.table()).expandX().widget();
      StringMapSetting.fillTable(this.theme, wtable, setting);
   }

   private void gameModeListW(WTable table, GameModeListSetting setting) {
      WButton button = (WButton)table.add(this.theme.button("Select")).expandCellX().widget();
      button.action = () -> MeteorClient.mc.setScreen(new GameModeListSettingScreen(this.theme, setting));
      WButton reset = (WButton)table.add(this.theme.button(GuiRenderer.RESET)).widget();
      reset.action = setting::reset;
   }
}
