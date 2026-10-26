package zgoly.meteorist.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import zgoly.meteorist.modules.autotrade.offers.BaseOffer;

public class OfferScreen extends WindowScreen {
   private final BaseOffer autoTrade;
   private WContainer settingsContainer;

   public OfferScreen(GuiTheme theme, BaseOffer autoTrade) {
      super(theme, autoTrade.getTypeName());
      this.autoTrade = autoTrade;
   }

   public void initWidgets() {
      if (!this.autoTrade.settings.groups.isEmpty()) {
         this.settingsContainer = (WContainer)this.add(this.theme.verticalList()).expandX().widget();
         this.settingsContainer.add(this.theme.settings(this.autoTrade.settings)).expandX();
      }
   }

   public void tick() {
      super.tick();
      this.autoTrade.settings.tick(this.settingsContainer, this.theme);
   }
}
