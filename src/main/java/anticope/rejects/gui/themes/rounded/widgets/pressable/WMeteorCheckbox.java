package anticope.rejects.gui.themes.rounded.widgets.pressable;

import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.gui.themes.rounded.MeteorWidget;
import anticope.rejects.utils.gui.GuiUtils;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.Mth;

public class WMeteorCheckbox extends WCheckbox implements MeteorWidget {
   private double animProgress;

   public WMeteorCheckbox(boolean checked) {
      super(checked);
      this.animProgress = checked ? 1.0 : 0.0;
   }

   protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
      MeteorRoundedGuiTheme theme = this.theme();
      this.animProgress = this.animProgress + (this.checked ? 1 : -1) * delta * 14.0;
      this.animProgress = Mth.clamp(this.animProgress, 0.0, 1.0);
      this.renderBackground(renderer, this, this.pressed, this.mouseOver);
      if (this.animProgress > 0.0) {
         double cs = (this.width - theme.scale(2.0)) / 1.75 * this.animProgress;
         GuiUtils.quadRounded(
            renderer, this.x + (this.width - cs) / 2.0, this.y + (this.height - cs) / 2.0, cs, cs, (Color)theme.checkboxColor.get(), theme.roundAmount()
         );
      }
   }
}
