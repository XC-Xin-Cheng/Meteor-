package com.seedfinding.mcbiome.layer.scale;

import com.seedfinding.mcbiome.layer.IntBiomeLayer;
import com.seedfinding.mccore.version.MCVersion;

public class ScaleLayer extends IntBiomeLayer {
   private final ScaleLayer.Type type;

   public ScaleLayer(MCVersion version, long worldSeed, long salt, ScaleLayer.Type type, IntBiomeLayer parent) {
      super(version, worldSeed, salt, parent);
      this.type = type;
   }

   public ScaleLayer.Type getType() {
      return this.type;
   }

   @Override
   public int sample(int x, int y, int z) {
      IntBiomeLayer parent = this.getParent(IntBiomeLayer.class);
      int center = parent.get(x >> 1, y, z >> 1);
      this.setSeed(x & -2, z & -2);
      int xb = x & 1;
      int zb = z & 1;
      if (xb == 0 && zb == 0) {
         return center;
      } else {
         int s = parent.get(x >> 1, y, z + 1 >> 1);
         int zPlus = this.choose(center, s);
         if (xb == 0) {
            return zPlus;
         } else {
            int e = parent.get(x + 1 >> 1, y, z >> 1);
            int xPlus = this.choose(center, e);
            if (zb == 0) {
               return xPlus;
            } else {
               int se = parent.get(x + 1 >> 1, y, z + 1 >> 1);
               return this.sample(center, e, s, se);
            }
         }
      }
   }

   public int sample(int center, int e, int s, int se) {
      int ret = this.choose(center, e, s, se);
      if (this.type == ScaleLayer.Type.FUZZY) {
         return ret;
      } else if (e == s && e == se) {
         return e;
      } else if (center == e && s != se) {
         return center;
      } else if (center == s && e != se) {
         return center;
      } else if (center == se && e != s) {
         return center;
      } else if (e == s && center != se) {
         return e;
      } else if (e == se && center != s) {
         return e;
      } else {
         return s == se && center != e ? s : ret;
      }
   }

   public static enum Type {
      NORMAL,
      FUZZY;
   }
}
