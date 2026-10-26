package anticope.rejects.gui.themes.rounded;

import anticope.rejects.utils.gui.GuiUtils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.BaseWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;

public interface MeteorWidget extends BaseWidget {
   default MeteorRoundedGuiTheme theme() {
      return (MeteorRoundedGuiTheme)this.getTheme();
   }

   default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
      MeteorRoundedGuiTheme theme = this.theme();
      int r = theme.roundAmount();
      double s = theme.scale(2.0);
      Color outlineColor = theme.outlineColor.get(pressed, mouseOver);
      GuiUtils.quadRounded(
         renderer, widget.x + s, widget.y + s, widget.width - s * 2.0, widget.height - s * 2.0, theme.backgroundColor.get(pressed, mouseOver), r - s
      );
      GuiUtils.quadOutlineRounded(renderer, widget, outlineColor, r, s);
   }
}
