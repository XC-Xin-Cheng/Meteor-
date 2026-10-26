package nekiplay.meteorplus.features.modules.render.holograms;

import java.util.ArrayList;
import java.util.Objects;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.core.BlockPos;

public class HologramDataListed {
   public double x;
   public double y;
   public double z;
   public String text;
   public String dimension;
   public Color color;
   public double max_render_distance = 16.0;
   public double scale = 1.0;
   public boolean distanceScaling = false;
   public int item_id = 0;
   public int item_scale = 2;
   public ArrayList<HologramData> other_holograms = new ArrayList<>();

   public HologramDataListed() {
   }

   public HologramDataListed(double x, double y, double z, String text, String dimension, Color color, double max_render_distance) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.color = color;
      this.text = text;
      this.dimension = dimension;
      this.max_render_distance = max_render_distance;
   }

   public HologramDataListed(BlockPos pos, String text, Dimension dimension, Color color, double max_render_distance) {
      this.x = pos.getX();
      this.y = pos.getY();
      this.z = pos.getZ();
      this.color = color;
      this.text = text;
      this.dimension = dimension.name();
      this.max_render_distance = max_render_distance;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         HologramDataListed otherPos = (HologramDataListed)obj;
         return this.x == otherPos.x && this.y == otherPos.y && this.z == otherPos.z ? true : super.equals(obj);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
