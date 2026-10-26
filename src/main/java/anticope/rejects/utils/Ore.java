package anticope.rejects.utils;

import anticope.rejects.mixin.CountPlacementModifierAccessor;
import anticope.rejects.mixin.HeightRangePlacementModifierAccessor;
import anticope.rejects.mixin.RarityFilterPlacementModifierAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.FeatureSorter.StepFeatureData;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ScatteredOreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class Ore {
   private static final Setting<Boolean> coal = ((Builder)new Builder().name("Coal")).build();
   private static final Setting<Boolean> iron = ((Builder)new Builder().name("Iron")).build();
   private static final Setting<Boolean> gold = ((Builder)new Builder().name("Gold")).build();
   private static final Setting<Boolean> redstone = ((Builder)new Builder().name("Redstone")).build();
   private static final Setting<Boolean> diamond = ((Builder)new Builder().name("Diamond")).build();
   private static final Setting<Boolean> lapis = ((Builder)new Builder().name("Lapis")).build();
   private static final Setting<Boolean> copper = ((Builder)new Builder().name("Copper")).build();
   private static final Setting<Boolean> emerald = ((Builder)new Builder().name("Emerald")).build();
   private static final Setting<Boolean> quartz = ((Builder)new Builder().name("Quartz")).build();
   private static final Setting<Boolean> debris = ((Builder)new Builder().name("Ancient Debris")).build();
   public static final List<Setting<Boolean>> oreSettings = new ArrayList<>(
      Arrays.asList(coal, iron, gold, redstone, diamond, lapis, copper, emerald, quartz, debris)
   );
   public int step;
   public int index;
   public Setting<Boolean> active;
   public IntProvider count = ConstantInt.of(1);
   public HeightProvider heightProvider;
   public WorldGenerationContext heightContext;
   public float rarity = 1.0F;
   public float discardOnAirChance;
   public int size;
   public Color color;
   public boolean scattered;

   public static Map<ResourceKey<Biome>, List<Ore>> getRegistry(Dimension dimension) {
      Provider registry = VanillaRegistries.createLookup();
      RegistryLookup<PlacedFeature> features = registry.lookupOrThrow(Registries.PLACED_FEATURE);
      Map<ResourceKey<LevelStem>, LevelStem> reg = ((WorldPreset)registry.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL).value())
         .createWorldDimensions()
         .dimensions();

      LevelStem dim = switch (dimension) {
         case Overworld -> (LevelStem)reg.get(LevelStem.OVERWORLD);
         case Nether -> (LevelStem)reg.get(LevelStem.NETHER);
         case End -> (LevelStem)reg.get(LevelStem.END);
         default -> throw new MatchException(null, null);
      };
      Set<Holder<Biome>> biomes = dim.generator().getBiomeSource().possibleBiomes();
      List<Holder<Biome>> biomes1 = biomes.stream().toList();
      List<StepFeatureData> indexer = FeatureSorter.buildFeaturesPerStep(
         biomes1, biomeEntry -> ((Biome)biomeEntry.value()).getGenerationSettings().features(), true
      );
      Map<PlacedFeature, Ore> featureToOre = new HashMap<>();
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_COAL_LOWER, 6, coal, new Color(47, 44, 54));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_COAL_UPPER, 6, coal, new Color(47, 44, 54));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_IRON_MIDDLE, 6, iron, new Color(236, 173, 119));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_IRON_SMALL, 6, iron, new Color(236, 173, 119));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_IRON_UPPER, 6, iron, new Color(236, 173, 119));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_GOLD, 6, gold, new Color(247, 229, 30));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_GOLD_LOWER, 6, gold, new Color(247, 229, 30));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_GOLD_EXTRA, 6, gold, new Color(247, 229, 30));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_GOLD_NETHER, 7, gold, new Color(247, 229, 30));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_GOLD_DELTAS, 7, gold, new Color(247, 229, 30));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_REDSTONE, 6, redstone, new Color(245, 7, 23));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_REDSTONE_LOWER, 6, redstone, new Color(245, 7, 23));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_DIAMOND, 6, diamond, new Color(33, 244, 255));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_DIAMOND_BURIED, 6, diamond, new Color(33, 244, 255));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_DIAMOND_LARGE, 6, diamond, new Color(33, 244, 255));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_DIAMOND_MEDIUM, 6, diamond, new Color(33, 244, 255));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_LAPIS, 6, lapis, new Color(8, 26, 189));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_LAPIS_BURIED, 6, lapis, new Color(8, 26, 189));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_COPPER, 6, copper, new Color(239, 151, 0));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_COPPER_LARGE, 6, copper, new Color(239, 151, 0));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_EMERALD, 6, emerald, new Color(27, 209, 45));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_QUARTZ_NETHER, 7, quartz, new Color(205, 205, 205));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_QUARTZ_DELTAS, 7, quartz, new Color(205, 205, 205));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_ANCIENT_DEBRIS_SMALL, 7, debris, new Color(209, 27, 245));
      registerOre(featureToOre, indexer, features, OrePlacements.ORE_ANCIENT_DEBRIS_LARGE, 7, debris, new Color(209, 27, 245));
      Map<ResourceKey<Biome>, List<Ore>> biomeOreMap = new HashMap<>();
      biomes1.forEach(
         biome -> {
            biomeOreMap.put((ResourceKey<Biome>)biome.unwrapKey().get(), new ArrayList<>());
            ((Biome)biome.value())
               .getGenerationSettings()
               .features()
               .stream()
               .flatMap(HolderSet::stream)
               .<PlacedFeature>map(Holder::value)
               .filter(featureToOre::containsKey)
               .forEach(feature -> biomeOreMap.get(biome.unwrapKey().get()).add(featureToOre.get(feature)));
         }
      );
      return biomeOreMap;
   }

   private static void registerOre(
      Map<PlacedFeature, Ore> map,
      List<StepFeatureData> indexer,
      RegistryLookup<PlacedFeature> oreRegistry,
      ResourceKey<PlacedFeature> oreKey,
      int genStep,
      Setting<Boolean> active,
      Color color
   ) {
      PlacedFeature orePlacement = (PlacedFeature)oreRegistry.getOrThrow(oreKey).value();
      int index = indexer.get(genStep).indexMapping().applyAsInt(orePlacement);
      Ore ore = new Ore(orePlacement, genStep, index, active, color);
      map.put(orePlacement, ore);
   }

   private Ore(PlacedFeature feature, int step, int index, Setting<Boolean> active, Color color) {
      this.step = step;
      this.index = index;
      this.active = active;
      this.color = color;
      int bottom = Minecraft.getInstance().level.getMinY();
      int height = Minecraft.getInstance().level.dimensionType().logicalHeight();
      this.heightContext = new WorldGenerationContext(null, LevelHeightAccessor.create(bottom, height));

      for (PlacementModifier modifier : feature.placement()) {
         if (modifier instanceof CountPlacement) {
            this.count = ((CountPlacementModifierAccessor)modifier).getCount();
         } else if (modifier instanceof HeightRangePlacement) {
            this.heightProvider = ((HeightRangePlacementModifierAccessor)modifier).getHeight();
         } else if (modifier instanceof RarityFilter) {
            this.rarity = ((RarityFilterPlacementModifierAccessor)modifier).getChance();
         }
      }

      if (((ConfiguredFeature)feature.feature().value()).config() instanceof OreConfiguration oreFeatureConfig) {
         this.discardOnAirChance = oreFeatureConfig.discardChanceOnAirExposure;
         this.size = oreFeatureConfig.size;
         if (((ConfiguredFeature)feature.feature().value()).feature() instanceof ScatteredOreFeature) {
            this.scattered = true;
         }
      } else {
         throw new IllegalStateException("config for " + feature + "is not OreFeatureConfig.class");
      }
   }
}
