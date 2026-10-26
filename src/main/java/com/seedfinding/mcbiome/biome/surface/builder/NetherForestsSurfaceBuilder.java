package com.seedfinding.mcbiome.biome.surface.builder;

import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.biome.surface.SurfaceConfig;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.rand.ChunkRand;

public class NetherForestsSurfaceBuilder extends SurfaceBuilder {
   public NetherForestsSurfaceBuilder(SurfaceConfig surfaceConfig) {
      super(surfaceConfig);
   }

   @Override
   public Block[] applyToColumn(
      BiomeSource source,
      ChunkRand rand,
      Block[] column,
      Biome biome,
      int x,
      int z,
      int maxY,
      int minY,
      double noise,
      int seaLevel,
      Block defaultBlock,
      Block defaultFluid
   ) {
      double seaHeight = source.getStaticNoiseSource().getNetherForestsNoise().sample(x * 0.1, seaLevel, z * 0.1);
      boolean shouldExpose = seaHeight > 0.15 + rand.nextDouble() * 0.35;
      double topHeight = source.getStaticNoiseSource().getNetherForestsNoise().sample(x * 0.1, 109.0, z * 0.1);
      boolean shouldUnderwater = topHeight > 0.25 + rand.nextDouble() * 0.9;
      int elevation = (int)(noise / 3.0 + 3.0 + rand.nextDouble() * 0.25);
      int state = -1;
      Block underBlock = this.getSurfaceConfig().getUnderBlock();

      for (int y = Math.max(127, maxY); y >= minY; y--) {
         Block topBlock = this.getSurfaceConfig().getTopBlock();
         Block block = column[y];
         if (Block.IS_AIR.test(source.getVersion(), block)) {
            state = -1;
         } else if (block == defaultBlock) {
            if (state == -1) {
               boolean flag2 = false;
               if (elevation <= 0) {
                  flag2 = true;
                  underBlock = this.getSurfaceConfig().getUnderBlock();
               }

               if (shouldExpose) {
                  topBlock = this.getSurfaceConfig().getUnderBlock();
               } else if (shouldUnderwater) {
                  topBlock = this.getSurfaceConfig().getUnderwaterBlock();
               }

               if (y < seaLevel && flag2) {
                  topBlock = defaultFluid;
               }

               state = elevation;
               if (y >= seaLevel - 1) {
                  block = topBlock;
               } else {
                  block = underBlock;
               }
            } else if (state > 0) {
               state--;
               block = underBlock;
            }
         }

         column[y] = block;
      }

      return column;
   }
}
