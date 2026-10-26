package nekiplay.meteorplus.mixinclasses;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.EditSystemScreen;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint.Builder;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

public class EditWaypointScreen extends EditSystemScreen<Waypoint> {
   private final Runnable reload;

   public EditWaypointScreen(GuiTheme theme, Waypoint value, Runnable reload) {
      super(theme, value, reload);
      this.reload = reload;
   }

   public Waypoint create() {
      return new Builder().pos(MeteorClient.mc.player.blockPosition().above(2)).dimension(PlayerUtils.getDimension()).build();
   }

   public boolean save() {
      return !this.isNew || Waypoints.get().add((Waypoint)this.value);
   }

   public Settings getSettings() {
      return ((Waypoint)this.value).settings;
   }
}
