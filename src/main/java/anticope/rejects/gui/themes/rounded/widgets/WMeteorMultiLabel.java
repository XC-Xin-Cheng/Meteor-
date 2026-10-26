package anticope.rejects.gui.themes.rounded.widgets;

import anticope.rejects.gui.themes.rounded.MeteorWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WMeteorMultiLabel extends WMultiLabel implements MeteorWidget {
   public WMeteorMultiLabel(String text, boolean title, double maxWidth) {
      super(text, title, maxWidth);
   }

   protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
      double h = this.theme.textHeight(this.title);
      Color color = (Color)this.theme().textColor.get();

      for (int i = 0; i < this.lines.size(); i++) {
         renderer.text((String)this.lines.get(i), this.x, this.y + h * i, color, false);
      }
   }
}
