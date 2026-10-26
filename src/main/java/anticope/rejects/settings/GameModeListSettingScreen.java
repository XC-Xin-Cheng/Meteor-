package anticope.rejects.settings;

import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.world.level.GameType;

public class GameModeListSettingScreen extends WindowScreen {
   private final GameModeListSetting setting;
   private final WTable table;

   public GameModeListSettingScreen(GuiTheme theme, GameModeListSetting setting) {
      super(theme, "Select Gamemodes");
      this.setting = setting;
      this.table = (WTable)super.add(theme.table()).expandX().widget();
   }

   public void initWidgets() {
      List<GameType> gms = (List<GameType>)this.setting.get();

      for (GameType gameMode : GameType.values()) {
         this.table.add(this.theme.label(Utils.nameToTitle(gameMode.getName()))).expandCellX();
         boolean contains = ((List)this.setting.get()).contains(gameMode);
         WCheckbox checkbox = (WCheckbox)this.table.add(this.theme.checkbox(contains)).widget();
         checkbox.action = () -> {
            if (contains) {
               gms.remove(gameMode);
            } else {
               gms.add(gameMode);
            }
         };
         this.table.row();
      }
   }
}
