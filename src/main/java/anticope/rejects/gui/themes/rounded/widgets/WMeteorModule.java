package anticope.rejects.gui.themes.rounded.widgets;

import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.gui.themes.rounded.MeteorWidget;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.Mth;

public class WMeteorModule extends WPressable implements MeteorWidget {
   private final Module module;
   private final String title;
   private double titleWidth;
   private double animationProgress1;
   private double animationProgress2;

   public WMeteorModule(Module module, String title) {
      this.module = module;
      this.title = title;
      this.tooltip = module.description;
      if (module.isActive()) {
         this.animationProgress1 = 1.0;
         this.animationProgress2 = 1.0;
      } else {
         this.animationProgress1 = 0.0;
         this.animationProgress2 = 0.0;
      }
   }

   public double pad() {
      return this.theme.scale(4.0);
   }

   protected void onCalculateSize() {
      double pad = this.pad();
      if (this.titleWidth == 0.0) {
         this.titleWidth = this.theme.textWidth(this.title);
      }

      this.width = pad + this.titleWidth + pad;
      this.height = pad + this.theme.textHeight() + pad;
   }

   protected void onPressed(int button) {
      if (button == 0) {
         this.module.toggle();
      } else if (button == 1) {
         MeteorClient.mc.setScreen(this.theme.moduleScreen(this.module));
      }
   }

   protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
      MeteorRoundedGuiTheme theme = this.theme();
      double pad = this.pad();
      this.animationProgress1 = this.animationProgress1 + delta * 4.0 * (!this.module.isActive() && !this.mouseOver ? -1 : 1);
      this.animationProgress1 = Mth.clamp(this.animationProgress1, 0.0, 1.0);
      this.animationProgress2 = this.animationProgress2 + delta * 6.0 * (this.module.isActive() ? 1 : -1);
      this.animationProgress2 = Mth.clamp(this.animationProgress2, 0.0, 1.0);
      if (this.animationProgress1 > 0.0) {
         renderer.quad(this.x, this.y, this.width * this.animationProgress1, this.height, (Color)theme.moduleBackground.get());
      }

      if (this.animationProgress2 > 0.0) {
         renderer.quad(
            this.x,
            this.y + this.height * (1.0 - this.animationProgress2),
            theme.scale(2.0),
            this.height * this.animationProgress2,
            (Color)theme.accentColor.get()
         );
      }

      double x = this.x + pad;
      double w = this.width - pad * 2.0;
      if (theme.moduleAlignment.get() == AlignmentX.Center) {
         x += w / 2.0 - this.titleWidth / 2.0;
      } else if (theme.moduleAlignment.get() == AlignmentX.Right) {
         x += w - this.titleWidth;
      }

      renderer.text(this.title, x, this.y + pad, (Color)theme.textColor.get(), false);
   }
}
