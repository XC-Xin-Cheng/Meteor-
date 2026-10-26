package nekiplay.meteorplus.features.modules.world.customblocks;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

public class CustomBlockData {
   public int block_id;
   public List<PosData> positions = new ArrayList<>();
   public String world;
   public String dimension;

   public CustomBlockData(BlockPos pos, int block_id) {
      this.positions.add(new PosData(pos));
      this.block_id = block_id;
   }
}
