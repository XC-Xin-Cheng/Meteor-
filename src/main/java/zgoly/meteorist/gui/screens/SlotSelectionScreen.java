package zgoly.meteorist.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import zgoly.meteorist.modules.slotclick.selections.BaseSlotSelection;

public class SlotSelectionScreen extends WindowScreen {
   private final BaseSlotSelection slotSelection;
   private WContainer settingsContainer;

   public SlotSelectionScreen(GuiTheme theme, BaseSlotSelection slotSelection) {
      super(theme, slotSelection.getTypeName());
      this.slotSelection = slotSelection;
   }

   public void initWidgets() {
      if (!this.slotSelection.settings.groups.isEmpty()) {
         this.settingsContainer = (WContainer)this.add(this.theme.verticalList()).expandX().widget();
         this.settingsContainer.add(this.theme.settings(this.slotSelection.settings)).expandX();
      }
   }

   public void tick() {
      super.tick();
      this.slotSelection.settings.tick(this.settingsContainer, this.theme);
   }
}
