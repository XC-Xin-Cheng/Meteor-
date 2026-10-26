package com.seedfinding.mcnoise.noise;

import com.seedfinding.mcseed.lcg.LCG;

public interface NoiseSampler {
   LCG SKIP_262 = LCG.JAVA.combine(262L);

   double sample(double var1, double var3, double var5, double var7);
}
