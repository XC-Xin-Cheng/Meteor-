package com.seedfinding.mcfeature.decorator.ore;

import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mccore.block.Blocks;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcnoise.utils.MathHelper;
import com.seedfinding.mcseed.rand.JRand;
import com.seedfinding.mcterrain.TerrainGenerator;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public abstract class RegularOreDecorator<C extends OreDecorator.Config, D extends OreDecorator.Data<?>> extends OreDecorator<C, D> {
   public RegularOreDecorator(C config, MCVersion version) {
      super(config, version);
   }

   @Override
   public Dimension getValidDimension() {
      return Dimension.OVERWORLD;
   }

   @Override
   protected List<BPos> generateOrePositions(BPos bPos, Biome biome, TerrainGenerator generator, JRand rand) {
      float angle = rand.nextFloat() * (float) Math.PI;
      float size = this.getSize(biome) / 8.0F;
      int amortizedSize = ceil((this.getSize(biome) / 16.0F * 2.0F + 1.0F) / 2.0F);
      double offsetXPos = bPos.getX() + Math.sin(angle) * size;
      double offsetXNeg = bPos.getX() - Math.sin(angle) * size;
      double offsetZPos = bPos.getZ() + Math.cos(angle) * size;
      double offsetZNeg = bPos.getZ() - Math.cos(angle) * size;
      double offsetYPos = bPos.getY() + rand.nextInt(3) - 2;
      double offsetYNeg = bPos.getY() + rand.nextInt(3) - 2;
      int startX = bPos.getX() - ceil(size) - amortizedSize;
      int startY = bPos.getY() - 2 - amortizedSize;
      int startZ = bPos.getZ() - ceil(size) - amortizedSize;
      int oreSize = 2 * (ceil(size) + amortizedSize);
      int radius = 2 * (2 + amortizedSize);

      for (int x = startX; x <= startX + oreSize; x++) {
         for (int z = startZ; z <= startZ + oreSize; z++) {
            if (startY <= generator.getFirstHeightInColumn(x, z, TerrainGenerator.OCEAN_FLOOR_WG)) {
               return this.generateVeinPart(
                  generator, biome, rand, offsetXPos, offsetXNeg, offsetZPos, offsetZNeg, offsetYPos, offsetYNeg, startX, startY, startZ, oreSize, radius
               );
            }
         }
      }

      return Collections.emptyList();
   }

   private List<BPos> generateVeinPart(
      TerrainGenerator generator,
      Biome biome,
      JRand rand,
      double offsetXPos,
      double offsetXNeg,
      double offsetZPos,
      double offsetZNeg,
      double offsetYPos,
      double offsetYNeg,
      int startX,
      int startY,
      int startZ,
      int oreSize,
      int radius
   ) {
      List<BPos> poses = new ArrayList<>();
      BitSet bitSet = new BitSet(oreSize * radius * oreSize);
      int size = this.getSize(biome);
      double[] store = new double[size * 4];

      for (int i = 0; i < size; i++) {
         float percent = (float)i / size;
         double x = MathHelper.lerp(percent, offsetXPos, offsetXNeg);
         double y = MathHelper.lerp(percent, offsetYPos, offsetYNeg);
         double z = MathHelper.lerp(percent, offsetZPos, offsetZNeg);
         double length = rand.nextDouble() * size / 16.0;
         double offset = ((Math.sin((float) Math.PI * percent) + 1.0) * length + 1.0) / 2.0;
         store[i * 4] = x;
         store[i * 4 + 1] = y;
         store[i * 4 + 2] = z;
         store[i * 4 + 3] = offset;
      }

      for (int i = 0; i < size - 1; i++) {
         if (store[i * 4 + 3] > 0.0) {
            for (int j = i + 1; j < size; j++) {
               if (store[j * 4 + 3] > 0.0) {
                  double diffX = store[i * 4] - store[j * 4];
                  double diffY = store[i * 4 + 1] - store[j * 4 + 1];
                  double diffZ = store[i * 4 + 2] - store[j * 4 + 2];
                  double offset = store[i * 4 + 3] - store[j * 4 + 3];
                  if (offset * offset > diffX * diffX + diffY * diffY + diffZ * diffZ) {
                     if (offset > 0.0) {
                        store[j * 4 + 3] = -1.0;
                     } else {
                        store[i * 4 + 3] = -1.0;
                     }
                  }
               }
            }
         }
      }

      for (int ix = 0; ix < size; ix++) {
         double offset = store[ix * 4 + 3];
         if (offset >= 0.0) {
            double x = store[ix * 4];
            double y = store[ix * 4 + 1];
            double z = store[ix * 4 + 2];
            int minX = Math.max(MathHelper.floor(x - offset), startX);
            int minY = Math.max(MathHelper.floor(y - offset), startY);
            int minZ = Math.max(MathHelper.floor(z - offset), startZ);
            int maxX = Math.max(MathHelper.floor(x + offset), minX);
            int maxY = Math.max(MathHelper.floor(y + offset), minY);
            int maxZ = Math.max(MathHelper.floor(z + offset), minZ);

            for (int X = minX; X <= maxX; X++) {
               double xSlide = (X + 0.5 - x) / offset;
               if (xSlide * xSlide < 1.0) {
                  for (int Y = minY; Y <= maxY; Y++) {
                     double ySlide = (Y + 0.5 - y) / offset;
                     if (xSlide * xSlide + ySlide * ySlide < 1.0) {
                        for (int Z = minZ; Z <= maxZ; Z++) {
                           double zSlide = (Z + 0.5 - z) / offset;
                           if (xSlide * xSlide + ySlide * ySlide + zSlide * zSlide < 1.0) {
                              int area = X - startX + (Y - startY) * oreSize + (Z - startZ) * oreSize * radius;
                              if (!bitSet.get(area)) {
                                 bitSet.set(area);
                                 BPos pos = new BPos(X, Y, Z);
                                 if (this.getReplaceBlocks(biome).contains(generator.getBlockAt(pos).orElse(Blocks.AIR))) {
                                    poses.add(pos);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return poses;
   }

   private static int ceil(float f) {
      int i = (int)f;
      return f > i ? i + 1 : i;
   }
}
