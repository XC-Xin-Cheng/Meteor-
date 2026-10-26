package anticope.rejects.gui.themes.rounded.widgets.pressable;

import anticope.rejects.gui.themes.rounded.MeteorWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WMeteorMinus extends WMinus implements MeteorWidget {
   protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
      double pad = this.pad();
      double s = this.theme.scale(3.0);
      this.renderBackground(renderer, this, this.pressed, this.mouseOver);
      renderer.quad(this.x + pad, this.y + this.height / 2.0 - s / 2.0, this.width - pad * 2.0, s, (Color)this.theme().minusColor.get());
   }
}
