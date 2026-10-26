package zgoly.meteorist.modules.minescript;

import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;

public class MinescriptIntegration extends Module {
   private final MinescriptService service = MinescriptServiceFactory.create(this);

   public MinescriptIntegration() {
      super(Meteorist.CATEGORY, "minescript-integration", "Control your Minescript custom scripts.");
   }

   public WWidget getWidget(GuiTheme theme) {
      return this.service.createWidget(theme);
   }

   @EventHandler
   private void onTick(Post event) {
      this.service.onTick();
   }

   public void onActivate() {
      this.service.onActivate();
   }

   public void onDeactivate() {
      this.service.onDeactivate();
   }
}
