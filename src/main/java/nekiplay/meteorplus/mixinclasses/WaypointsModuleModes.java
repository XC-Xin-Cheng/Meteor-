package nekiplay.meteorplus.mixinclasses;

import java.util.Comparator;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;

public class WaypointsModuleModes {
   public static class DistanceComparator implements Comparator<String> {
      Map<String, Waypoint> base;

      public DistanceComparator(Map<String, Waypoint> base) {
         this.base = base;
      }

      public int compare(String a, String b) {
         long distance1 = 0L;
         long distance2 = 0L;
         if (MeteorClient.mc.player != null) {
            Waypoint awp = this.base.get(a);
            Waypoint bwp = this.base.get(b);
            if (awp != null && bwp != null && awp.getPos() != null && bwp.getPos() != null) {
               distance1 = Math.round(MeteorClient.mc.player.position().distanceTo(awp.getPos().getCenter()));
               distance2 = Math.round(MeteorClient.mc.player.position().distanceTo(bwp.getPos().getCenter()));
            }
         }

         return distance1 >= distance2 ? 1 : -1;
      }
   }

   public static class NameComparator implements Comparator<String> {
      Map<String, Waypoint> base;

      public NameComparator(Map<String, Waypoint> base) {
         this.base = base;
      }

      public int compare(String a, String b) {
         if (this.base.containsKey(a) && this.base.containsKey(b)) {
            Waypoint awp = this.base.get(a);
            Waypoint bwp = this.base.get(b);
            if (awp != null && bwp != null) {
               return ((String)awp.name.get()).length() >= ((String)bwp.name.get()).length() ? 1 : -1;
            } else {
               return 0;
            }
         } else {
            return 0;
         }
      }
   }

   public static enum SortMode {
      None,
      Distance,
      Name;
   }
}
