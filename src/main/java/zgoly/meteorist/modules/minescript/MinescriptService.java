package zgoly.meteorist.modules.minescript;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;

public interface MinescriptService {
   WWidget createWidget(GuiTheme var1);

   void onActivate();

   void onDeactivate();

   void onTick();

   void refreshScripts();

   void refreshJobsIfNeeded();
}
