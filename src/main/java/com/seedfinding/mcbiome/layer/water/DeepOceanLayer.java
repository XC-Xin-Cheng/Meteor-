package com.seedfinding.mcbiome.layer.water;

import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.layer.IntBiomeLayer;
import com.seedfinding.mcbiome.layer.composite.CrossLayer;
import com.seedfinding.mccore.version.MCVersion;

public class DeepOceanLayer extends CrossLayer {
   public DeepOceanLayer(MCVersion version, long worldSeed, long salt, IntBiomeLayer parent) {
      super(version, worldSeed, salt, parent);
   }

   @Override
   public int sample(int n, int e, int s, int w, int center) {
      if (!Biome.isShallowOcean(center, this.getVersion())) {
         return center;
      } else {
         int i = 0;
         if (Biome.isShallowOcean(n, this.getVersion())) {
            i++;
         }

         if (Biome.isShallowOcean(e, this.getVersion())) {
            i++;
         }

         if (Biome.isShallowOcean(w, this.getVersion())) {
            i++;
         }

         if (Biome.isShallowOcean(s, this.getVersion())) {
            i++;
         }

         if (i > 3) {
            if (center == Biomes.WARM_OCEAN.getId()) {
               return Biomes.DEEP_WARM_OCEAN.getId();
            } else if (center == Biomes.LUKEWARM_OCEAN.getId()) {
               return Biomes.DEEP_LUKEWARM_OCEAN.getId();
            } else if (center == Biomes.OCEAN.getId()) {
               return Biomes.DEEP_OCEAN.getId();
            } else if (center == Biomes.COLD_OCEAN.getId()) {
               return Biomes.DEEP_COLD_OCEAN.getId();
            } else {
               return center == Biomes.FROZEN_OCEAN.getId() ? Biomes.DEEP_FROZEN_OCEAN.getId() : Biomes.DEEP_OCEAN.getId();
            }
         } else {
            return center;
         }
      }
   }
}
