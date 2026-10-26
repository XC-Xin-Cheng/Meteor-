package nekiplay.meteorplus.features.modules.world.customblocks;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.MeteorPlusAddon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class CustomBlocksModule extends Module {
   public Gson gson = new Gson();
   public HashMap<PosData, CustomBlockData> allBlocks = new HashMap<>();
   private final SettingGroup settingsGroup = this.settings.getDefaultGroup();
   public final Setting<Boolean> noSetIfAir = this.settingsGroup
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("no-set-if-air")).description("No set custom block if original block equals air."))
               .defaultValue(true))
            .build()
      );

   public CustomBlocksModule() {
      super(Categories.World, "Custom Blocks", "Place custom blocks in world, extension for Holograms");
   }

   public void onActivate() {
      super.onActivate();
      this.createDefault();
      this.load();

      for (ChunkAccess chunk : Utils.chunks()) {
         this.updateChunkData(chunk);
      }
   }

   private void updateChunkData(ChunkAccess chunk) {
      String dimension = PlayerUtils.getDimension().name();

      for (int x = chunk.getPos().getMinBlockX(); x <= chunk.getPos().getMaxBlockX(); x++) {
         for (int z = chunk.getPos().getMinBlockZ(); z <= chunk.getPos().getMaxBlockZ(); z++) {
            int height = chunk.getOrCreateHeightmapUnprimed(Types.WORLD_SURFACE)
               .getFirstAvailable(x - chunk.getPos().getMinBlockX(), z - chunk.getPos().getMinBlockZ());

            for (int y = this.mc.level.getMinY(); y < height; y++) {
               BlockPos pos = new BlockPos(x, y, z);
               BlockState bs = chunk.getBlockState(pos);
               Block original = bs.getBlock();
               CustomBlockData customBlockData = this.allBlocks.get(new PosData(pos));
               if (customBlockData != null && customBlockData.dimension.equals(dimension) && this.isValidBlockForReplace(original)) {
                  this.setBlock(pos, customBlockData);
               }
            }
         }
      }
   }

   @EventHandler
   private void onChunkData(ChunkDataEvent event) {
      LevelChunk chunk = event.chunk();
      this.updateChunkData(chunk);
   }

   private boolean isValidBlockForReplace(Block block) {
      return block != Blocks.AIR && this.noSetIfAir.get() ? true : !(Boolean)this.noSetIfAir.get();
   }

   private void setBlock(BlockPos pos, CustomBlockData data) {
      BlockState block = Block.stateById(data.block_id);
      this.mc.level.setBlockAndUpdate(pos, block);
   }

   @EventHandler
   private void onBlockUpdate(BlockUpdateEvent event) {
      String dimension = PlayerUtils.getDimension().name();
      CustomBlockData customBlockData = this.allBlocks.get(new PosData(event.pos));
      if (customBlockData != null && customBlockData.dimension.equals(dimension)) {
         Block original = event.newState.getBlock();
         if (this.isValidBlockForReplace(original)) {
            this.setBlock(event.pos, customBlockData);
         }
      }
   }

   private void load() {
      File dir = new File(MeteorClient.FOLDER, "customblocks");
      if (dir.exists()) {
         String world_name = Utils.getWorldName();
         File dir2 = new File(dir, world_name);
         if (dir2.exists()) {
            this.allBlocks.clear();
            File[] files = dir2.listFiles();
            if (files != null) {
               for (File file : files) {
                  if (file.exists()) {
                     MeteorPlusAddon.LOG.info("[Meteor+] Loading hologram: " + file.getName());

                     try {
                        BufferedReader reader = Files.newBufferedReader(Path.of(file.toURI()), StandardCharsets.UTF_8);

                        try {
                           String json = reader.lines().collect(Collectors.joining());
                           CustomBlockData hologramData = (CustomBlockData)this.gson.fromJson(json, CustomBlockData.class);
                           if (hologramData != null) {
                              for (PosData posData : hologramData.positions) {
                                 this.allBlocks.put(posData, hologramData);
                              }

                              MeteorPlusAddon.LOG.info("[Meteor+] Success loaded custom block: " + file.getName());
                           }
                        } catch (JsonSyntaxException var14) {
                           MeteorPlusAddon.LOG.error("[Meteor+] Error in custom block: " + var14);
                        }
                     } catch (IOException var15) {
                        MeteorPlusAddon.LOG.error("[Meteor+] Error in custom block: " + var15);
                     }
                  }
               }
            }
         }
      }
   }

   private void createDefault() {
      File dir = new File(MeteorClient.FOLDER, "customblocks");
      if (!dir.exists()) {
         dir.mkdir();
      }

      String world_name = Utils.getWorldName();
      File dir2 = new File(dir, world_name);
      if (!dir2.exists()) {
         dir2.mkdir();
         CustomBlockData hologramData = new CustomBlockData(new BlockPos(0, 64, 0), 1);
         hologramData.dimension = PlayerUtils.getDimension().name();
         hologramData.world = world_name;
         String json = this.gson.toJson(hologramData);
         File file = new File(dir2.getPath(), "0.json");
         if (!file.exists()) {
            try {
               file.createNewFile();
            } catch (IOException var10) {
               var10.printStackTrace();
            }
         }

         try {
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(json);
            printWriter.close();
         } catch (IOException var9) {
            var9.printStackTrace();
         }
      }
   }
}
