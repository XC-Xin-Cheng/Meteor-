package com.seedfinding.mcterrain.utils;

public class NoiseSettings {
   public NoiseSettings.NoiseSamplingSettings samplingSettings;
   public NoiseSettings.NoiseSlideSettings topSlideSettings;
   public NoiseSettings.NoiseSlideSettings bottomSlideSettings;
   public static final double COORDINATE_SCALE = 684.412;
   public static final double HEIGHT_SCALE = 684.412;
   public static final double NOISE_SCALE = 512.0;

   public NoiseSettings(
      NoiseSettings.NoiseSamplingSettings samplingSettings,
      NoiseSettings.NoiseSlideSettings topSlideSettings,
      NoiseSettings.NoiseSlideSettings bottomSlideSettings
   ) {
      this.samplingSettings = samplingSettings;
      this.topSlideSettings = topSlideSettings;
      this.bottomSlideSettings = bottomSlideSettings;
   }

   public NoiseSettings(NoiseSettings.NoiseSamplingSettings samplingSettings) {
      this(samplingSettings, null, null);
   }

   public static NoiseSettings create(double xzScale, double yScale, double xzFactor, double yFactor) {
      return new NoiseSettings(new NoiseSettings.NoiseSamplingSettings(xzScale, yScale, xzFactor, yFactor));
   }

   public NoiseSettings addTopSlide(int target, int size, int offset) {
      this.topSlideSettings = new NoiseSettings.NoiseSlideSettings(target, size, offset);
      return this;
   }

   public NoiseSettings addBottomSlide(int target, int size, int offset) {
      this.bottomSlideSettings = new NoiseSettings.NoiseSlideSettings(target, size, offset);
      return this;
   }

   public static class NoiseSamplingSettings {
      public double xzScale;
      public double yScale;
      public double xzFactor;
      public double yFactor;

      public NoiseSamplingSettings(double xzScale, double yScale, double xzFactor, double yFactor) {
         this.xzScale = xzScale;
         this.yScale = yScale;
         this.xzFactor = xzFactor;
         this.yFactor = yFactor;
      }
   }

   public static class NoiseSlideSettings {
      public int target;
      public int size;
      public int offset;

      public NoiseSlideSettings(int target, int size, int offset) {
         this.target = target;
         this.size = size;
         this.offset = offset;
      }
   }
}
