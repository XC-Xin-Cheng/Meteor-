package nekiplay.meteorplus.mixin.meteorclient.modules;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
   value = {BlockESP.class},
   remap = false
)
public class BlockESPMixin extends Module {
   @Final
   @Shadow
   private final Long2ObjectMap<ESPChunk> chunks = new Long2ObjectOpenHashMap();
   @Unique
   private long renders = 0L;

   public BlockESPMixin(Category category, String name, String description) {
      super(category, name, description);
   }

   public String getInfoString() {
      this.renders = 0L;
      synchronized (this.chunks) {
         ObjectIterator var2 = this.chunks.values().iterator();

         while (var2.hasNext()) {
            ESPChunk chunk = (ESPChunk)var2.next();
            if (!chunk.shouldBeDeleted()) {
               ObjectIterator var1 = chunk.blocks.values().iterator();

               while (var1.hasNext()) {
                  ESPBlock block = (ESPBlock)var1.next();
                  this.renders++;
                  block.loaded = false;
               }
            }
         }
      }

      return this.renders + "";
   }
}
