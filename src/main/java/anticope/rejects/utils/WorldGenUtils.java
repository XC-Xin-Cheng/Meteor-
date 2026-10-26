package anticope.rejects.utils;

import anticope.rejects.utils.seeds.Seed;
import anticope.rejects.utils.seeds.Seeds;
import baritone.api.BaritoneAPI;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.data.SpiralIterator;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.misc.SlimeChunk;
import com.seedfinding.mcfeature.structure.BastionRemnant;
import com.seedfinding.mcfeature.structure.BuriedTreasure;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.EndCity;
import com.seedfinding.mcfeature.structure.Fortress;
import com.seedfinding.mcfeature.structure.Mansion;
import com.seedfinding.mcfeature.structure.Mineshaft;
import com.seedfinding.mcfeature.structure.Monument;
import com.seedfinding.mcfeature.structure.RegionStructure;
import com.seedfinding.mcfeature.structure.Stronghold;
import com.seedfinding.mcfeature.structure.Structure;
import com.seedfinding.mcfeature.structure.Village;
import com.seedfinding.mcterrain.TerrainGenerator;
import cubitect.Cubiomes;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapDecorations.Entry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenUtils {
   private static final Logger LOG = LogManager.getLogger();
   private static final HashMap<WorldGenUtils.Feature, List<Block>> FEATURE_BLOCKS = new HashMap<WorldGenUtils.Feature, List<Block>>() {
      {
         this.put(WorldGenUtils.Feature.nether_fortress, Arrays.asList(Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_WART));
         this.put(WorldGenUtils.Feature.ocean_monument, Arrays.asList(Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE));
         this.put(WorldGenUtils.Feature.stronghold, Arrays.asList(Blocks.END_PORTAL_FRAME, Blocks.END_PORTAL));
         this.put(WorldGenUtils.Feature.end_city, Arrays.asList(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.PURPUR_STAIRS, Blocks.END_ROD));
         this.put(
            WorldGenUtils.Feature.village,
            Arrays.asList(
               Blocks.BELL,
               Blocks.BREWING_STAND,
               Blocks.SMOKER,
               Blocks.BLAST_FURNACE,
               Blocks.FLETCHING_TABLE,
               Blocks.STONECUTTER,
               Blocks.LOOM,
               Blocks.GRINDSTONE,
               Blocks.LECTERN
            )
         );
         this.put(WorldGenUtils.Feature.mineshaft, Collections.singletonList(Blocks.RAIL));
         this.put(WorldGenUtils.Feature.desert_pyramid, Arrays.asList(Blocks.TNT, Blocks.CHISELED_SANDSTONE, Blocks.STONE_PRESSURE_PLATE));
      }
   };
   private static final HashMap<WorldGenUtils.Feature, List<Class<? extends Entity>>> FEATURE_ENTITIES = new HashMap<WorldGenUtils.Feature, List<Class<? extends Entity>>>() {
      {
         this.put(WorldGenUtils.Feature.ocean_monument, Arrays.asList(ElderGuardian.class, Guardian.class));
         this.put(WorldGenUtils.Feature.nether_fortress, Arrays.asList(Blaze.class, WitherSkeleton.class));
         this.put(WorldGenUtils.Feature.mansion, Collections.singletonList(Evoker.class));
         this.put(WorldGenUtils.Feature.slime_chunk, Collections.singletonList(Slime.class));
         this.put(WorldGenUtils.Feature.bastion_remnant, Collections.singletonList(PiglinBrute.class));
         this.put(WorldGenUtils.Feature.end_city, Collections.singletonList(Shulker.class));
         this.put(WorldGenUtils.Feature.village, Arrays.asList(Villager.class, IronGolem.class));
         this.put(WorldGenUtils.Feature.mineshaft, Collections.singletonList(MinecartChest.class));
      }
   };

   public static BlockPos locateFeature(Cubiomes.StructureType cfeature, BlockPos center) {
      WorldGenUtils.Feature feature = switch (cfeature) {
         case Treasure -> WorldGenUtils.Feature.buried_treasure;
         case Mansion -> WorldGenUtils.Feature.mansion;
         case Stronghold -> WorldGenUtils.Feature.stronghold;
         case Fortress -> WorldGenUtils.Feature.nether_fortress;
         case Monument -> WorldGenUtils.Feature.ocean_monument;
         case Bastion -> WorldGenUtils.Feature.bastion_remnant;
         case End_City -> WorldGenUtils.Feature.end_city;
         case Village -> WorldGenUtils.Feature.village;
         case Mineshaft -> WorldGenUtils.Feature.mineshaft;
         case Desert_Pyramid -> WorldGenUtils.Feature.desert_pyramid;
         default -> null;
      };
      Seed seed = Seeds.get().getSeed();
      BlockPos pos = null;
      if (!checkIfInDimension(getDimension(feature))) {
         return null;
      } else {
         if (seed != null) {
            try {
               pos = locateFeature(seed, feature, center);
            } catch (Error | Exception var10) {
               LOG.error(var10);
            }

            if (pos != null) {
               return pos;
            }
         }

         if (MeteorClient.mc.player != null) {
            ItemStack stack = MeteorClient.mc.player.getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.getItem() != Items.FILLED_MAP) {
               stack = MeteorClient.mc.player.getItemInHand(InteractionHand.OFF_HAND);
            }

            if (stack.getItem() == Items.FILLED_MAP) {
               try {
                  pos = locateFeatureMap(feature, stack);
               } catch (Error | Exception var9) {
                  LOG.error(var9);
               }

               if (pos != null) {
                  return pos;
               }
            }
         }

         try {
            pos = locateFeatureEntities(feature);
         } catch (Error | Exception var8) {
            LOG.error(var8);
         }

         if (pos != null) {
            return pos;
         } else {
            try {
               pos = locateFeatureBlocks(feature);
            } catch (Error | Exception var7) {
               LOG.error(var7);
            }

            return pos;
         }
      }
   }

   private static BlockPos locateFeatureMap(WorldGenUtils.Feature feature, ItemStack stack) {
      return !isValidMap(feature, stack) ? null : getMapMarker(stack);
   }

   private static BlockPos locateFeatureBlocks(WorldGenUtils.Feature feature) {
      List<Block> blocks = FEATURE_BLOCKS.get(feature);
      if (blocks == null) {
         return null;
      } else {
         List<BlockPos> posList = BaritoneAPI.getProvider()
            .getWorldScanner()
            .scanChunkRadius(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext(), blocks, 64, 10, 32);
         if (posList.isEmpty()) {
            return null;
         } else {
            if (posList.size() < 5) {
               ChatUtils.warningPrefix("Locate", "Only %d block(s) found. This search might be a false positive.", new Object[]{posList.size()});
            }

            return posList.get(0);
         }
      }
   }

   private static BlockPos locateFeatureEntities(WorldGenUtils.Feature feature) {
      List<Class<? extends Entity>> entities = FEATURE_ENTITIES.get(feature);
      if (entities == null) {
         return null;
      } else if (MeteorClient.mc.level == null) {
         return null;
      } else {
         for (Entity e : MeteorClient.mc.level.entitiesForRendering()) {
            for (Class<? extends Entity> clazz : entities) {
               if (clazz.isInstance(e)) {
                  return e.blockPosition();
               }
            }
         }

         return null;
      }
   }

   private static BlockPos locateFeature(Seed seed, WorldGenUtils.Feature feature, BlockPos center) {
      return feature == WorldGenUtils.Feature.slime_chunk ? locateSlimeChunk(seed, center) : locateStructure(seed, feature, center);
   }

   private static BlockPos locateSlimeChunk(Seed seed, BlockPos center) {
      Dimension dimension = getDimension(WorldGenUtils.Feature.slime_chunk);
      MCVersion mcVersion = seed.version;
      CPos centerChunk = new CPos(center.getX() >> 4, center.getZ() >> 4);
      CPos slimeChunkPos = locateSlimeChunk(new SlimeChunk(mcVersion), centerChunk, 6400, seed.seed, new ChunkRand(), dimension);
      return slimeChunkPos == null ? null : toBlockPos(slimeChunkPos.toBlockPos());
   }

   private static CPos locateSlimeChunk(SlimeChunk slimeChunk, CPos centerChunk, int radius, long seed, ChunkRand rand, Dimension dimension) {
      if (!slimeChunk.isValidDimension(dimension)) {
         return null;
      } else {
         for (CPos next : new SpiralIterator<>(centerChunk, new CPos(radius, radius), (x, y, z) -> new CPos(x, z))) {
            SlimeChunk.Data data = slimeChunk.at(next.getX(), next.getZ(), true);
            if (data.testStart(seed, rand)) {
               return next;
            }
         }

         return null;
      }
   }

   private static BlockPos locateStructure(Seed seed, WorldGenUtils.Feature feature, BlockPos center) {
      Dimension dimension = getDimension(feature);
      if (dimension == Dimension.OVERWORLD && seed.version.isNewerThan(MCVersion.v1_18)) {
         return null;
      } else {
         MCVersion mcVersion = seed.version;
         Structure<?, ?> structure = getStructure(feature, mcVersion);
         if (structure == null) {
            return null;
         } else {
            BiomeSource biomeSource = BiomeSource.of(dimension, mcVersion, seed.seed);
            if (!structure.isValidDimension(biomeSource.getDimension())) {
               return null;
            } else {
               BPos structurePos = locateStructure(
                  structure, new BPos(center.getX(), center.getY(), center.getZ()), 6400, new ChunkRand(), biomeSource, TerrainGenerator.of(biomeSource)
               );
               return structurePos == null ? null : toBlockPos(structurePos);
            }
         }
      }
   }

   private static BPos locateStructure(
      Structure<?, ?> structure, BPos center, int radius, ChunkRand chunkRand, BiomeSource source, TerrainGenerator terrainGenerator
   ) {
      if (structure instanceof RegionStructure<?, ?> regionStructure) {
         int chunkInRegion = regionStructure.getSpacing();
         int regionSize = chunkInRegion * 16;
         int border = 30000000;
         SpiralIterator<RPos> spiralIterator = new SpiralIterator<>(
            center.toRegionPos(regionSize),
            new BPos(-30000000, 0, -30000000).toRegionPos(regionSize),
            new BPos(30000000, 0, 30000000).toRegionPos(regionSize),
            1,
            (x, y, z) -> new RPos(x, z, regionSize)
         );
         return StreamSupport.stream(spiralIterator.spliterator(), false)
            .map(rPos -> regionStructure.getInRegion(source.getWorldSeed(), rPos.getX(), rPos.getZ(), chunkRand))
            .filter(Objects::nonNull)
            .filter(cPos -> regionStructure.canSpawn(cPos, source) && (terrainGenerator == null || regionStructure.canGenerate(cPos, terrainGenerator)))
            .findAny()
            .map(cPos -> cPos.toBlockPos().add(9, 0, 9))
            .orElse(null);
      } else if (structure instanceof Stronghold strongholdStructure) {
         CPos currentChunkPos = center.toChunkPos();
         int squaredDistance = Integer.MAX_VALUE;
         CPos closest = new CPos(0, 0);

         for (CPos stronghold : strongholdStructure.getAllStarts(source, chunkRand)) {
            int newSquaredDistance = (currentChunkPos.getX() - stronghold.getX()) * (currentChunkPos.getX() - stronghold.getX())
               + (currentChunkPos.getZ() - stronghold.getZ()) * (currentChunkPos.getZ() - stronghold.getZ());
            if (newSquaredDistance < squaredDistance) {
               squaredDistance = newSquaredDistance;
               closest = stronghold;
            }
         }

         BPos dimPos = closest.toBlockPos().add(9, 0, 9);
         return new BPos(dimPos.getX(), 0, dimPos.getZ());
      } else if (structure instanceof Mineshaft mineshaft) {
         SpiralIterator<CPos> spiralIterator = new SpiralIterator<>(
            new CPos(center.getX() >> 4, center.getZ() >> 4), new CPos(radius, radius), (x, y, z) -> new CPos(x, z)
         );
         return StreamSupport.stream(spiralIterator.spliterator(), false).filter(cPos -> {
            com.seedfinding.mcfeature.Feature.Data<Mineshaft> data = mineshaft.at(cPos.getX(), cPos.getZ());
            return data.testStart(source.getWorldSeed(), chunkRand) && data.testBiome(source) && data.testGenerate(terrainGenerator);
         }).findAny().map(cPos -> cPos.toBlockPos().add(9, 0, 9)).orElse(null);
      } else {
         return null;
      }
   }

   private static Dimension getDimension(WorldGenUtils.Feature feature) {
      return switch (feature) {
         case buried_treasure -> Dimension.OVERWORLD;
         case mansion -> Dimension.OVERWORLD;
         case stronghold -> Dimension.OVERWORLD;
         case nether_fortress -> Dimension.NETHER;
         case ocean_monument -> Dimension.OVERWORLD;
         case bastion_remnant -> Dimension.NETHER;
         case end_city -> Dimension.END;
         case village -> Dimension.OVERWORLD;
         case mineshaft -> Dimension.OVERWORLD;
         case slime_chunk -> Dimension.OVERWORLD;
         case desert_pyramid -> Dimension.OVERWORLD;
         default -> Dimension.OVERWORLD;
      };
   }

   private static boolean checkIfInDimension(Dimension dimension) {
      return switch (dimension) {
         case OVERWORLD -> PlayerUtils.getDimension() == meteordevelopment.meteorclient.utils.world.Dimension.Overworld;
         case NETHER -> PlayerUtils.getDimension() == meteordevelopment.meteorclient.utils.world.Dimension.Nether;
         case END -> PlayerUtils.getDimension() == meteordevelopment.meteorclient.utils.world.Dimension.End;
      };
   }

   private static Structure<?, ?> getStructure(WorldGenUtils.Feature feature, MCVersion version) {
      return (Structure<?, ?>)(switch (feature) {
         case buried_treasure -> new BuriedTreasure(version);
         case mansion -> new Mansion(version);
         case stronghold -> new Stronghold(version);
         case nether_fortress -> new Fortress(version);
         case ocean_monument -> new Monument(version);
         case bastion_remnant -> new BastionRemnant(version);
         case end_city -> new EndCity(version);
         case village -> new Village(version);
         case mineshaft -> new Mineshaft(version);
         default -> null;
         case desert_pyramid -> new DesertPyramid(version);
      });
   }

   private static BlockPos toBlockPos(BPos pos) {
      return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
   }

   private static boolean isValidMap(WorldGenUtils.Feature feature, ItemStack stack) {
      if (stack.getComponents().isEmpty()) {
         return false;
      } else if (!stack.getComponents().has(DataComponents.MAP_DECORATIONS)) {
         return false;
      } else {
         MapDecorations displayTag = (MapDecorations)stack.getPrototype().get(DataComponents.MAP_DECORATIONS);
         if (!displayTag.toString().contains("Name")) {
            return false;
         } else {
            String nameTag = String.valueOf(displayTag.decorations().get("Name"));
            if (!nameTag.contains("translate")) {
               return false;
            } else if (feature == WorldGenUtils.Feature.buried_treasure) {
               return nameTag.contains("filled_map.buried_treasure");
            } else if (feature == WorldGenUtils.Feature.ocean_monument) {
               return nameTag.contains("filled_map.monument");
            } else {
               return feature == WorldGenUtils.Feature.mansion ? nameTag.contains("filled_map.mansion") : false;
            }
         }
      }
   }

   private static BlockPos getMapMarker(ItemStack stack) {
      if (stack.getComponents().isEmpty()) {
         return null;
      } else if (!stack.getPrototype().has(DataComponents.MAP_DECORATIONS)) {
         return null;
      } else {
         MapDecorations decorationsTag = (MapDecorations)stack.get(DataComponents.MAP_DECORATIONS);
         if (decorationsTag.decorations().isEmpty()) {
            return null;
         } else {
            Entry iconTag = (Entry)decorationsTag.decorations().get(0);
            return new BlockPos((int)iconTag.x(), 0, (int)iconTag.z());
         }
      }
   }

   public static enum Feature {
      buried_treasure,
      mansion,
      stronghold,
      nether_fortress,
      ocean_monument,
      bastion_remnant,
      end_city,
      village,
      mineshaft,
      slime_chunk,
      desert_pyramid;
   }
}
