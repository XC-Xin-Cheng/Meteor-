package anticope.rejects.gui.themes.rounded.widgets;

import anticope.rejects.gui.themes.rounded.MeteorWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WTooltip;

public class WMeteorTooltip extends WTooltip implements MeteorWidget {
   public WMeteorTooltip(String text) {
      super(text);
   }

   protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
      renderer.quad(this, this.theme().backgroundColor.get());
   }
}
