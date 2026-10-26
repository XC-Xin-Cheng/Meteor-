package zgoly.meteorist.gui.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import net.minecraft.util.Mth;
import zgoly.meteorist.Meteorist;

public class WVisibilityCheckbox extends WCheckbox implements MeteorWidget {
   public boolean checked;
   private double animProgress;

   public WVisibilityCheckbox(boolean checked) {
      super(checked);
      this.checked = checked;
      this.animProgress = checked ? 0.0 : 1.0;
   }

   protected void onPressed(int button) {
      this.checked = !this.checked;
   }

   protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
      double pad = this.pad();
      double ts = this.theme.textHeight();
      renderer.quad(this.x + this.width / 2.0 - ts / 2.0, this.y + pad, ts, ts, Meteorist.EYE, this.theme.textColor());
      this.animProgress = this.animProgress + (this.checked ? -1 : 1) * delta * 14.0;
      this.animProgress = Mth.clamp(this.animProgress, 0.0, 1.0);
      this.renderBackground(renderer, this, this.pressed, this.mouseOver);
      if (this.animProgress > 0.0) {
         renderer.rotatedQuad(
            this.x + this.width / 2.0 - this.animProgress * this.width / 2.0,
            this.y + this.height / 2.0 - this.theme.scale(1.0) / 2.0,
            this.width * this.animProgress,
            this.theme.scale(1.0),
            45.0,
            GuiRenderer.CIRCLE,
            this.theme.textColor()
         );
      }
   }
}
