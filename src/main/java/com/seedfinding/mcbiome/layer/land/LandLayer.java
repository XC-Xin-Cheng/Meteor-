package com.seedfinding.mcbiome.layer.land;

import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.Biomes;
import com.seedfinding.mcbiome.layer.IntBiomeLayer;
import com.seedfinding.mcbiome.layer.composite.XCrossLayer;
import com.seedfinding.mccore.version.MCVersion;

public class LandLayer extends XCrossLayer {
   public LandLayer(MCVersion version, long worldSeed, long salt, IntBiomeLayer parent) {
      super(version, worldSeed, salt, parent);
   }

   @Override
   public int sample(int sw, int se, int ne, int nw, int center) {
      if (this.getVersion().isOlderOrEqualTo(MCVersion.vb1_8_1)) {
         return this.sample_beta(sw, se, ne, nw, center);
      } else if (Biome.isShallowOcean(center, this.getVersion()) && !Biome.applyAll(v -> Biome.isShallowOcean(v, this.getVersion()), sw, se, ne, nw)) {
         int i = 1;
         int j = 1;
         if (!Biome.isShallowOcean(nw, this.getVersion()) && this.nextInt(i++) == 0) {
            j = nw;
         }

         if (!Biome.isShallowOcean(ne, this.getVersion()) && this.nextInt(i++) == 0) {
            j = ne;
         }

         if (!Biome.isShallowOcean(sw, this.getVersion()) && this.nextInt(i++) == 0) {
            j = sw;
         }

         if (!Biome.isShallowOcean(se, this.getVersion()) && this.nextInt(i) == 0) {
            j = se;
         }

         if (this.nextInt(3) == 0) {
            return j;
         } else if (this.getVersion().isOlderOrEqualTo(MCVersion.v1_6_4)) {
            return j == Biomes.SNOWY_TUNDRA.getId() ? Biomes.FROZEN_OCEAN.getId() : Biomes.OCEAN.getId();
         } else {
            return j == Biomes.FOREST.getId() ? Biomes.FOREST.getId() : center;
         }
      } else if (Biome.isShallowOcean(center, this.getVersion())
         || Biome.applyAll(v -> !Biome.isShallowOcean(v, this.getVersion()), sw, se, ne, nw)
         || this.nextInt(5) != 0) {
         return center;
      } else if (this.getVersion().isOlderOrEqualTo(MCVersion.v1_6_4)) {
         return center == Biomes.SNOWY_TUNDRA.getId() ? Biomes.FROZEN_OCEAN.getId() : Biomes.OCEAN.getId();
      } else if (Biome.isShallowOcean(nw, this.getVersion())) {
         return Biome.equalsOrDefault(center, Biomes.FOREST.getId(), nw);
      } else if (Biome.isShallowOcean(sw, this.getVersion())) {
         return Biome.equalsOrDefault(center, Biomes.FOREST.getId(), sw);
      } else if (Biome.isShallowOcean(ne, this.getVersion())) {
         return Biome.equalsOrDefault(center, Biomes.FOREST.getId(), ne);
      } else {
         return Biome.isShallowOcean(se, this.getVersion()) ? Biome.equalsOrDefault(center, Biomes.FOREST.getId(), se) : center;
      }
   }

   public int sample_beta(int sw, int se, int ne, int nw, int center) {
      if (Biome.isShallowOcean(center, this.getVersion()) && !Biome.applyAll(v -> Biome.isShallowOcean(v, this.getVersion()), sw, se, ne, nw)) {
         return this.nextInt(3) == 2 ? Biomes.PLAINS.getId() : Biomes.OCEAN.getId();
      } else if (center == Biomes.PLAINS.getId() && !Biome.applyAll(v -> v == Biomes.PLAINS.getId(), sw, se, ne, nw)) {
         return this.nextInt(5) == 4 ? Biomes.OCEAN.getId() : Biomes.PLAINS.getId();
      } else {
         return center;
      }
   }
}
