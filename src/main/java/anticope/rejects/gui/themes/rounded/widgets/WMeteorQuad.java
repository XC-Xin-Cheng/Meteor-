package anticope.rejects.gui.themes.rounded.widgets;

import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.utils.gui.GuiUtils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WQuad;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WMeteorQuad extends WQuad {
   public WMeteorQuad(Color color) {
      super(color);
   }

   protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
      GuiUtils.quadRounded(renderer, this.x, this.y, this.width, this.height, this.color, ((MeteorRoundedGuiTheme)this.theme).roundAmount());
   }
}
