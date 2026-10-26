package nekiplay.meteorplus.mixinclasses;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;

public class WIcon extends WWidget {
   private final Waypoint waypoint;

   public WIcon(Waypoint waypoint) {
      this.waypoint = waypoint;
   }

   protected void onCalculateSize() {
      double s = this.theme.scale(32.0);
      this.width = s;
      this.height = s;
   }

   protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
      renderer.post(() -> this.waypoint.renderIcon(this.x, this.y, 1.0, this.width));
   }
}
