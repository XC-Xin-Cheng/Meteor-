package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.PlayerRespawnEvent;
import anticope.rejects.events.SeedChangedEvent;
import anticope.rejects.utils.Ore;
import anticope.rejects.utils.seeds.Seed;
import anticope.rejects.utils.seeds.Seeds;
import baritone.api.BaritoneAPI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.WorldgenRandom.Algorithm;
import net.minecraft.world.phys.Vec3;

public class OreSim extends Module {
   private final Map<Long, Map<Ore, Set<Vec3>>> chunkRenderers = new ConcurrentHashMap<>();
   private Seed worldSeed = null;
   private Map<ResourceKey<Biome>, List<Ore>> oreConfig;
   public List<BlockPos> oreGoals = new ArrayList<>();
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> horizontalRadius = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("chunk-range")).description("Taxi cap distance of chunks being shown.")).defaultValue(5))
            .min(1)
            .sliderMax(10)
            .build()
      );
   private final Setting<OreSim.AirCheck> airCheck = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("air-check-mode"))
                  .description("Checks if there is air at a calculated ore pos."))
               .defaultValue(OreSim.AirCheck.RECHECK))
            .build()
      );
   private final Setting<Boolean> baritone = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("baritone"))
                  .description("Set baritone ore positions to the simulated ones."))
               .defaultValue(false))
            .build()
      );

   public OreSim() {
      super(MeteorRejectsAddon.CATEGORY, "ore-sim", "Xray on crack.");
      SettingGroup sgOres = this.settings.createGroup("Ores");
      Ore.oreSettings.forEach(sgOres::add);
   }

   public boolean baritone() {
      return this.isActive() && (Boolean)this.baritone.get() && BaritoneUtils.IS_AVAILABLE;
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.oreConfig != null) {
         if (Seeds.get().getSeed() != null) {
            int chunkX = this.mc.player.chunkPosition().x();
            int chunkZ = this.mc.player.chunkPosition().z();
            int rangeVal = (Integer)this.horizontalRadius.get();

            for (int range = 0; range <= rangeVal; range++) {
               for (int x = -range + chunkX; x <= range + chunkX; x++) {
                  this.renderChunk(x, chunkZ + range - rangeVal, event);
               }

               for (int x = -range + 1 + chunkX; x < range + chunkX; x++) {
                  this.renderChunk(x, chunkZ - range + rangeVal + 1, event);
               }
            }
         }
      }
   }

   private void renderChunk(int x, int z, Render3DEvent event) {
      long chunkKey = ChunkPos.pack(x, z);
      if (this.chunkRenderers.containsKey(chunkKey)) {
         Map<Ore, Set<Vec3>> chunk = this.chunkRenderers.get(chunkKey);

         for (Entry<Ore, Set<Vec3>> oreRenders : chunk.entrySet()) {
            if ((Boolean)oreRenders.getKey().active.get()) {
               for (Vec3 pos : oreRenders.getValue()) {
                  event.renderer.boxLines(pos.x, pos.y, pos.z, pos.x + 1.0, pos.y + 1.0, pos.z + 1.0, oreRenders.getKey().color, 0);
               }
            }
         }
      }
   }

   @EventHandler
   private void onBlockUpdate(BlockUpdateEvent event) {
      if (this.airCheck.get() == OreSim.AirCheck.RECHECK && !event.newState.canOcclude()) {
         long chunkKey = ChunkPos.pack(event.pos);
         if (this.chunkRenderers.containsKey(chunkKey)) {
            Vec3 pos = Vec3.atLowerCornerOf(event.pos);

            for (Set<Vec3> ore : this.chunkRenderers.get(chunkKey).values()) {
               ore.remove(pos);
            }
         }
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null && this.mc.level != null && this.oreConfig != null) {
         if (this.baritone() && BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive()) {
            this.oreGoals.clear();
            ChunkPos chunkPos = this.mc.player.chunkPosition();
            int rangeVal = 4;

            for (int range = 0; range <= rangeVal; range++) {
               for (int x = -range + chunkPos.x(); x <= range + chunkPos.x(); x++) {
                  this.oreGoals.addAll(this.addToBaritone(x, chunkPos.z() + range - rangeVal));
               }

               for (int x = -range + 1 + chunkPos.x(); x < range + chunkPos.x(); x++) {
                  this.oreGoals.addAll(this.addToBaritone(x, chunkPos.z() - range + rangeVal + 1));
               }
            }
         }
      }
   }

   private ArrayList<BlockPos> addToBaritone(int chunkX, int chunkZ) {
      ArrayList<BlockPos> baritoneGoals = new ArrayList<>();
      long chunkKey = ChunkPos.pack(chunkX, chunkZ);
      if (this.chunkRenderers.containsKey(chunkKey)) {
         this.chunkRenderers
            .get(chunkKey)
            .entrySet()
            .stream()
            .filter(entry -> (Boolean)entry.getKey().active.get())
            .flatMap(entry -> entry.getValue().stream())
            .map(BlockPos::containing)
            .forEach(baritoneGoals::add);
      }

      return baritoneGoals;
   }

   public void onActivate() {
      if (Seeds.get().getSeed() == null) {
         this.error("No seed found. To set a seed do .seed <seed>", new Object[0]);
         this.toggle();
      }

      this.reload();
   }

   public void onDeactivate() {
      this.chunkRenderers.clear();
      this.oreConfig = null;
   }

   @EventHandler
   private void onSeedChanged(SeedChangedEvent event) {
      this.reload();
   }

   @EventHandler
   private void onPlayerRespawn(PlayerRespawnEvent event) {
      this.reload();
   }

   private void loadVisibleChunks() {
      if (this.mc.player != null) {
         for (ChunkAccess chunk : Utils.chunks(false)) {
            this.doMathOnChunk(chunk);
         }
      }
   }

   private void reload() {
      Seed seed = Seeds.get().getSeed();
      if (seed != null) {
         this.worldSeed = seed;
         this.oreConfig = Ore.getRegistry(PlayerUtils.getDimension());
         this.chunkRenderers.clear();
         if (this.mc.level != null && this.worldSeed != null) {
            this.loadVisibleChunks();
         }
      }
   }

   @EventHandler
   public void onChunkData(ChunkDataEvent event) {
      this.doMathOnChunk(event.chunk());
   }

   private void doMathOnChunk(ChunkAccess chunk) {
      ChunkPos chunkPos = chunk.getPos();
      long chunkKey = chunkPos.pack();
      ClientLevel world = this.mc.level;
      if (!this.chunkRenderers.containsKey(chunkKey) && world != null) {
         Set<ResourceKey<Biome>> biomes = new HashSet<>();
         ChunkPos.rangeClosed(chunkPos, 1).forEach(chunkPosx -> {
            ChunkAccess chunkxx = world.getChunk(chunkPosx.x(), chunkPosx.z(), ChunkStatus.BIOMES, false);
            if (chunkxx != null) {
               for (LevelChunkSection chunkSection : chunkxx.getSections()) {
                  chunkSection.getBiomes().getAll(entry -> biomes.add((ResourceKey)entry.unwrapKey().get()));
               }
            }
         });
         Set<Ore> oreSet = biomes.stream().flatMap(b -> this.getDefaultOres((ResourceKey<Biome>)b).stream()).collect(Collectors.toSet());
         int chunkX = chunkPos.x() << 4;
         int chunkZ = chunkPos.z() << 4;
         WorldgenRandom random = new WorldgenRandom(Algorithm.XOROSHIRO.newInstance(0L));
         long populationSeed = random.setDecorationSeed(this.worldSeed.seed, chunkX, chunkZ);
         HashMap<Ore, Set<Vec3>> h = new HashMap<>();

         for (Ore ore : oreSet) {
            HashSet<Vec3> ores = new HashSet<>();
            random.setFeatureSeed(populationSeed, ore.index, ore.step);
            int repeat = ore.count.sample(random);

            for (int i = 0; i < repeat; i++) {
               if (ore.rarity == 1.0F || !(random.nextFloat() >= 1.0F / ore.rarity)) {
                  int x = random.nextInt(16) + chunkX;
                  int z = random.nextInt(16) + chunkZ;
                  int y = ore.heightProvider.sample(random, ore.heightContext);
                  BlockPos origin = new BlockPos(x, y, z);
                  ResourceKey<Biome> biome = (ResourceKey<Biome>)chunk.getNoiseBiome(x, y, z).unwrapKey().get();
                  if (this.getDefaultOres(biome).contains(ore)) {
                     if (ore.scattered) {
                        ores.addAll(this.generateHidden(world, random, origin, ore.size));
                     } else {
                        ores.addAll(this.generateNormal(world, random, origin, ore.size, ore.discardOnAirChance));
                     }
                  }
               }
            }

            if (!ores.isEmpty()) {
               h.put(ore, ores);
            }
         }

         this.chunkRenderers.put(chunkKey, h);
      }
   }

   private List<Ore> getDefaultOres(ResourceKey<Biome> biomeRegistryKey) {
      return this.oreConfig.containsKey(biomeRegistryKey) ? this.oreConfig.get(biomeRegistryKey) : this.oreConfig.values().stream().findAny().get();
   }

   private ArrayList<Vec3> generateNormal(ClientLevel world, WorldgenRandom random, BlockPos blockPos, int veinSize, float discardOnAir) {
      float f = random.nextFloat() * (float) Math.PI;
      float g = veinSize / 8.0F;
      int i = Mth.ceil((veinSize / 16.0F * 2.0F + 1.0F) / 2.0F);
      double d = blockPos.getX() + Math.sin(f) * g;
      double e = blockPos.getX() - Math.sin(f) * g;
      double h = blockPos.getZ() + Math.cos(f) * g;
      double j = blockPos.getZ() - Math.cos(f) * g;
      double l = blockPos.getY() + random.nextInt(3) - 2;
      double m = blockPos.getY() + random.nextInt(3) - 2;
      int n = blockPos.getX() - Mth.ceil(g) - i;
      int o = blockPos.getY() - 2 - i;
      int p = blockPos.getZ() - Mth.ceil(g) - i;
      int q = 2 * (Mth.ceil(g) + i);
      int r = 2 * (2 + i);

      for (int s = n; s <= n + q; s++) {
         for (int t = p; t <= p + q; t++) {
            if (o <= world.getHeight(Types.MOTION_BLOCKING, s, t)) {
               return this.generateVeinPart(world, random, veinSize, d, e, h, j, l, m, n, o, p, q, r, discardOnAir);
            }
         }
      }

      return new ArrayList<>();
   }

   private ArrayList<Vec3> generateVeinPart(
      ClientLevel world,
      WorldgenRandom random,
      int veinSize,
      double startX,
      double endX,
      double startZ,
      double endZ,
      double startY,
      double endY,
      int x,
      int y,
      int z,
      int size,
      int i,
      float discardOnAir
   ) {
      BitSet bitSet = new BitSet(size * i * size);
      MutableBlockPos mutable = new MutableBlockPos();
      double[] ds = new double[veinSize * 4];
      ArrayList<Vec3> poses = new ArrayList<>();

      for (int n = 0; n < veinSize; n++) {
         float f = (float)n / veinSize;
         double p = Mth.lerp(f, startX, endX);
         double q = Mth.lerp(f, startY, endY);
         double r = Mth.lerp(f, startZ, endZ);
         double s = random.nextDouble() * veinSize / 16.0;
         double m = ((Mth.sin((float) Math.PI * f) + 1.0F) * s + 1.0) / 2.0;
         ds[n * 4] = p;
         ds[n * 4 + 1] = q;
         ds[n * 4 + 2] = r;
         ds[n * 4 + 3] = m;
      }

      for (int var59 = 0; var59 < veinSize - 1; var59++) {
         if (!(ds[var59 * 4 + 3] <= 0.0)) {
            for (int o = var59 + 1; o < veinSize; o++) {
               if (!(ds[o * 4 + 3] <= 0.0)) {
                  double p = ds[var59 * 4] - ds[o * 4];
                  double q = ds[var59 * 4 + 1] - ds[o * 4 + 1];
                  double r = ds[var59 * 4 + 2] - ds[o * 4 + 2];
                  double s = ds[var59 * 4 + 3] - ds[o * 4 + 3];
                  if (s * s > p * p + q * q + r * r) {
                     if (s > 0.0) {
                        ds[o * 4 + 3] = -1.0;
                     } else {
                        ds[var59 * 4 + 3] = -1.0;
                     }
                  }
               }
            }
         }
      }

      for (int var60 = 0; var60 < veinSize; var60++) {
         double u = ds[var60 * 4 + 3];
         if (!(u < 0.0)) {
            double v = ds[var60 * 4];
            double w = ds[var60 * 4 + 1];
            double aa = ds[var60 * 4 + 2];
            int ab = Math.max(Mth.floor(v - u), x);
            int ac = Math.max(Mth.floor(w - u), y);
            int ad = Math.max(Mth.floor(aa - u), z);
            int ae = Math.max(Mth.floor(v + u), ab);
            int af = Math.max(Mth.floor(w + u), ac);
            int ag = Math.max(Mth.floor(aa + u), ad);

            for (int ah = ab; ah <= ae; ah++) {
               double ai = (ah + 0.5 - v) / u;
               if (ai * ai < 1.0) {
                  for (int aj = ac; aj <= af; aj++) {
                     double ak = (aj + 0.5 - w) / u;
                     if (ai * ai + ak * ak < 1.0) {
                        for (int al = ad; al <= ag; al++) {
                           double am = (al + 0.5 - aa) / u;
                           if (ai * ai + ak * ak + am * am < 1.0) {
                              int an = ah - x + (aj - y) * size + (al - z) * size * i;
                              if (!bitSet.get(an)) {
                                 bitSet.set(an);
                                 mutable.set(ah, aj, al);
                                 if (aj >= -64
                                    && aj < 320
                                    && (this.airCheck.get() == OreSim.AirCheck.OFF || world.getBlockState(mutable).canOcclude())
                                    && this.shouldPlace(world, mutable, discardOnAir, random)) {
                                    poses.add(new Vec3(ah, aj, al));
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

   private boolean shouldPlace(ClientLevel world, BlockPos orePos, float discardOnAir, WorldgenRandom random) {
      if (discardOnAir != 0.0F && (discardOnAir == 1.0F || !(random.nextFloat() >= discardOnAir))) {
         for (Direction direction : Direction.values()) {
            if (!world.getBlockState(orePos.offset(direction.getUnitVec3i())).canOcclude() && discardOnAir != 1.0F) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   private ArrayList<Vec3> generateHidden(ClientLevel world, WorldgenRandom random, BlockPos blockPos, int size) {
      ArrayList<Vec3> poses = new ArrayList<>();
      int i = random.nextInt(size + 1);

      for (int j = 0; j < i; j++) {
         size = Math.min(j, 7);
         int x = this.randomCoord(random, size) + blockPos.getX();
         int y = this.randomCoord(random, size) + blockPos.getY();
         int z = this.randomCoord(random, size) + blockPos.getZ();
         if ((this.airCheck.get() == OreSim.AirCheck.OFF || world.getBlockState(new BlockPos(x, y, z)).canOcclude())
            && this.shouldPlace(world, new BlockPos(x, y, z), 1.0F, random)) {
            poses.add(new Vec3(x, y, z));
         }
      }

      return poses;
   }

   private int randomCoord(WorldgenRandom random, int size) {
      return Math.round((random.nextFloat() - random.nextFloat()) * size);
   }

   public static enum AirCheck {
      ON_LOAD,
      RECHECK,
      OFF;
   }
}
