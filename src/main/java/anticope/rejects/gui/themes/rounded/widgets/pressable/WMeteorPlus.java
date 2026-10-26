package anticope.rejects.gui.themes.rounded.widgets.pressable;

import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.gui.themes.rounded.MeteorWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WMeteorPlus extends WPlus implements MeteorWidget {
   protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
      MeteorRoundedGuiTheme theme = this.theme();
      double pad = this.pad();
      double s = theme.scale(3.0);
      this.renderBackground(renderer, this, this.pressed, this.mouseOver);
      renderer.quad(this.x + pad, this.y + this.height / 2.0 - s / 2.0, this.width - pad * 2.0, s, (Color)theme.plusColor.get());
      renderer.quad(this.x + this.width / 2.0 - s / 2.0, this.y + pad, s, this.height - pad * 2.0, (Color)theme.plusColor.get());
   }
}
