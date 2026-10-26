package anticope.rejects.gui.themes.rounded.widgets;

import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.gui.themes.rounded.MeteorWidget;
import anticope.rejects.utils.gui.GuiUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow.WHeader;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WMeteorWindow extends WWindow implements MeteorWidget {
   public WMeteorWindow(WWidget icon, String title) {
      super(icon, title);
   }

   protected WHeader header(WWidget icon) {
      return new WMeteorWindow.WMeteorHeader(icon);
   }

   protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
      if (this.expanded || this.animProgress > 0.0) {
         GuiUtils.quadRounded(
            renderer,
            this.x,
            this.y + this.header.height / 2.0,
            this.width,
            this.height - this.header.height / 2.0,
            this.theme().backgroundColor.get(),
            ((MeteorRoundedGuiTheme)this.theme).roundAmount(),
            false
         );
      }
   }

   private class WMeteorHeader extends WHeader {
      public WMeteorHeader(WWidget icon) {
         Objects.requireNonNull(WMeteorWindow.this);
         super(WMeteorWindow.this, icon);
      }

      protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
         GuiUtils.quadRounded(renderer, this, (Color)WMeteorWindow.this.theme().accentColor.get(), ((MeteorRoundedGuiTheme)this.theme).roundAmount());
      }
   }
}
