package nekiplay.meteorplus.features.modules.world.customblocks;

import java.util.Objects;
import net.minecraft.core.BlockPos;

public class PosData {
   public double x;
   public double y;
   public double z;

   public PosData(BlockPos pos) {
      this.x = pos.getX();
      this.y = pos.getY();
      this.z = pos.getZ();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         PosData otherPos = (PosData)obj;
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
