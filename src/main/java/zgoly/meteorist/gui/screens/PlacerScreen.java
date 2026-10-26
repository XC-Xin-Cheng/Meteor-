package zgoly.meteorist.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import zgoly.meteorist.modules.placer.BasePlacer;

public class PlacerScreen extends WindowScreen {
   private final BasePlacer placer;
   private WContainer settingsContainer;

   public PlacerScreen(GuiTheme theme, BasePlacer placer) {
      super(theme, (String)placer.name.get());
      this.placer = placer;
   }

   public void initWidgets() {
      if (!this.placer.settings.groups.isEmpty()) {
         this.settingsContainer = (WContainer)this.add(this.theme.verticalList()).expandX().widget();
         this.settingsContainer.add(this.theme.settings(this.placer.settings)).expandX();
      }
   }

   public void tick() {
      super.tick();
      this.placer.settings.tick(this.settingsContainer, this.theme);
   }
}
