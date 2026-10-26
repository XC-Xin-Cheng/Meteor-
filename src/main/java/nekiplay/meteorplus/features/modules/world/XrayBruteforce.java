package nekiplay.meteorplus.features.modules.world;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPChunk;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.UnorderedArrayList;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.RainbowColors;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.utils.GenerationBlock;
import nekiplay.meteorplus.utils.xraybruteforce.XBlock;
import nekiplay.meteorplus.utils.xraybruteforce.XChunk;
import nekiplay.meteorplus.utils.xraybruteforce.XGroup;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class XrayBruteforce extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgSVisual = this.settings.createGroup("Scanner");
   private final SettingGroup sgRVisual = this.settings.createGroup("Scanner Render Visuals");
   private final SettingGroup sgSRenderer = this.settings.createGroup("Scanned Renderer");
   private final SettingGroup sgSaver = this.settings.createGroup("Scanned Saver");
   private final SettingGroup sgDelayer = this.settings.createGroup("Scanned Delayer");
   public final Setting<Boolean> autoSave = this.sgSaver
      .add(((Builder)((Builder)((Builder)new Builder().name("Auto save")).description("Save rendered ores.")).defaultValue(true)).build());
   public final Setting<Boolean> save = this.sgSaver
      .add(((Builder)((Builder)((Builder)((Builder)new Builder().name("Save")).description("Save rendered ores.")).defaultValue(false)).onChanged(c -> {
         if (this.isActive()) {
            Thread saveth = new Thread(() -> {
               for (XrayBruteforce.RenderOre ore : ores.toArray(new XrayBruteforce.RenderOre[0])) {
                  this.saveRenderOre(ore);
               }

               this.info("Saving complete", new Object[0]);
            });
            saveth.start();
         }
      })).build());
   public final Setting<Boolean> load = this.sgSaver
      .add(((Builder)((Builder)((Builder)((Builder)new Builder().name("Load")).description("Load rendered ores.")).defaultValue(false)).onChanged(c -> {
         if (this.isActive()) {
            this.loadSaveOres();
            this.info("Loaded", new Object[0]);
         }
      })).build());
   public final Setting<Boolean> new_render = this.sgSRenderer
      .add(((Builder)((Builder)((Builder)new Builder().name("New Render")).description("New Render.")).defaultValue(true)).build());
   private final Setting<List<Block>> whblocks = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                     .name("whitelist"))
                  .description("Which blocks to show x-rayed."))
               .defaultValue(new Block[]{Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.ANCIENT_DEBRIS})
               .onChanged(v -> scanned.clear()))
            .build()
      );
   private final Setting<ESPBlockData> defaultBlockConfig = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.GenericSetting.Builder)((meteordevelopment.meteorclient.settings.GenericSetting.Builder)((meteordevelopment.meteorclient.settings.GenericSetting.Builder)new meteordevelopment.meteorclient.settings.GenericSetting.Builder()
                     .name("whitelist-default-config"))
                  .description("Default block config."))
               .defaultValue(
                  new ESPBlockData(ShapeMode.Lines, new SettingColor(0, 255, 200), new SettingColor(0, 255, 200, 25), false, new SettingColor(0, 255, 200, 125))
               ))
            .build()
      );
   private final Setting<Map<Block, ESPBlockData>> blockConfigs = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockDataSetting.Builder)((meteordevelopment.meteorclient.settings.BlockDataSetting.Builder)((meteordevelopment.meteorclient.settings.BlockDataSetting.Builder)new meteordevelopment.meteorclient.settings.BlockDataSetting.Builder()
                     .name("whitelist-block-configs"))
                  .description("Config for each block."))
               .defaultData(this.defaultBlockConfig)
               .onChanged(v -> {
                  synchronized (ores) {
                     for (XrayBruteforce.RenderOre ore : ores) {
                        ESPBlockData data = this.getBlockData(ore.block);
                        ore.linecolor = data.lineColor;
                        ore.sidecolor = data.sideColor;
                        ore.tracercolor = data.tracerColor;
                        ore.shapeMode = data.shapeMode;
                     }
                  }
               }))
            .build()
      );
   private final Setting<XrayBruteforce.PacketMode> packet_first = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("packet-#1"))
                  .description("First packet."))
               .defaultValue(XrayBruteforce.PacketMode.Start))
            .build()
      );
   private final Setting<XrayBruteforce.PacketMode> packet_two = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("packet-#2"))
                  .description("Two packet."))
               .defaultValue(XrayBruteforce.PacketMode.Abort))
            .build()
      );
   private final Setting<XrayBruteforce.GenerationType> generationType = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("Generation-type"))
                  .description("Ores generation type."))
               .defaultValue(XrayBruteforce.GenerationType.Old))
            .build()
      );
   public final Setting<Boolean> clear_cache_blocks = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("Clear-cache")).description("Clear saved cache.")).defaultValue(false)).build());
   public final Setting<Boolean> clear_cache_ores = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("Clear-rendered-ores-cache")).description("Clear saved ores cache.")).defaultValue(false)).build());
   public final Setting<Integer> rescannerDelay = this.sgDelayer
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("rescanner-delay"))
                  .description("Delay for rechecking block."))
               .defaultValue(2000))
            .build()
      );
   public final Setting<Boolean> tps_sync = this.sgDelayer
      .add(((Builder)((Builder)((Builder)new Builder().name("TPS-sync")).description("TPS sync scanning.")).defaultValue(true)).build());
   public final Setting<Boolean> fps_sync = this.sgDelayer
      .add(((Builder)((Builder)((Builder)new Builder().name("FPS-sync")).description("FPS sync scanning.")).defaultValue(false)).build());
   public final Setting<Boolean> auto_height = this.sgSVisual
      .add(((Builder)((Builder)((Builder)new Builder().name("Auto-height")).description("Auto detect height.")).defaultValue(false)).build());
   public final Setting<Boolean> auto_dimension = this.sgSVisual
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("Auto-dimension")).description("Auto detect dimension.")).defaultValue(true))
               .visible(this.auto_height::get))
            .build()
      );
   public final Setting<Boolean> expanded = this.sgSVisual
      .add(((Builder)((Builder)((Builder)new Builder().name("exposed")).description("Scan exposed blocks.")).defaultValue(true)).build());
   private final Setting<XrayBruteforce.ScanPriority> scanPriority = this.sgSVisual
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("Scan priority"))
                  .description("Ores generation type."))
               .defaultValue(XrayBruteforce.ScanPriority.Normal))
            .build()
      );
   private final Setting<Integer> cavesRange = this.sgSVisual
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("Caves range"))
                     .description("Caves range."))
                  .defaultValue(2))
               .visible(() -> this.scanPriority.get() == XrayBruteforce.ScanPriority.Caves))
            .build()
      );
   private final Setting<Integer> cavesRangeY = this.sgSVisual
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("Caves Y range"))
                     .description("Caves y range."))
                  .defaultValue(2))
               .visible(() -> this.scanPriority.get() == XrayBruteforce.ScanPriority.Caves))
            .build()
      );
   private final Setting<Integer> range = this.sgSVisual
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("range"))
                  .description("Bruteforce range."))
               .defaultValue(40))
            .min(3)
            .sliderRange(3, 512)
            .build()
      );
   private final Setting<Integer> y_range = this.sgSVisual
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("y-range"))
                  .description("Bruteforce range."))
               .defaultValue(13))
            .min(3)
            .sliderRange(3, 255)
            .build()
      );
   private final Setting<Integer> delaymin = this.sgDelayer
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("Scan delay min"))
                  .description("Bruteforce delay min ."))
               .defaultValue(30))
            .min(0)
            .sliderRange(0, 150)
            .build()
      );
   private final Setting<Integer> delaymax = this.sgDelayer
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("Scan delay max"))
                  .description("Bruteforce delay max ."))
               .defaultValue(35))
            .min(0)
            .sliderRange(0, 150)
            .build()
      );
   private final Setting<Boolean> pauseBind = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("pause-bind")).description("Allow use pause bind.")).defaultValue(true)).build());
   private final Setting<Keybind> pausekeybind = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)new meteordevelopment.meteorclient.settings.KeybindSetting.Builder()
                        .name("pause-keybind"))
                     .description("The bind for pause."))
                  .defaultValue(Keybind.fromKey(88)))
               .visible(this.pauseBind::get))
            .build()
      );
   private final Setting<Boolean> outline = this.sgRVisual
      .add(((Builder)((Builder)((Builder)new Builder().name("outline")).description("Outline to block.")).defaultValue(true)).build());
   private final Setting<SettingColor> outlineColor = this.sgRVisual
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("outline-color"))
                  .description("Outline color to block."))
               .defaultValue(new SettingColor(255, 255, 0, 255))
               .visible(this.outline::get))
            .build()
      );
   private final Setting<Boolean> tracer = this.sgRVisual
      .add(((Builder)((Builder)((Builder)new Builder().name("tracer")).description("Tracer to block.")).defaultValue(true)).build());
   private final Setting<SettingColor> tracerColor = this.sgRVisual
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("tracer-color"))
                  .description("Tracer color to block."))
               .defaultValue(new SettingColor(255, 255, 0, 255))
               .visible(this.tracer::get))
            .build()
      );
   private final Setting<Integer> clusterRange = this.sgSVisual
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("Cluster range"))
                  .description("Cluster range."))
               .defaultValue(2))
            .min(1)
            .sliderRange(1, 2)
            .build()
      );
   private BlockPos currentScanBlock;
   private boolean pause_toggle = true;
   private final List<XGroup> groups = new UnorderedArrayList();
   public HashMap<BlockPos, XBlock> oresV3 = new HashMap<>();
   private final Long2ObjectMap<XChunk> chunks = new Long2ObjectOpenHashMap();
   public static final List<XrayBruteforce.RenderOre> ores = new ArrayList<>();
   public ArrayList<XrayBruteforce.BlockScanned> need_rescan = new ArrayList<>();
   private int renderedBlocks = 0;
   private boolean lagging = false;
   private final List<BlockPos> blocks = new ArrayList<>();
   long timescan = 0L;
   long millis = 0L;
   private Thread clickerThread;
   private Thread exposedthread = null;
   private boolean scan = false;
   private static final List<BlockPos> scanned = new ArrayList<>();

   public XrayBruteforce() {
      super(Categories.World, "xray-bruteForce", "Bypasses anti-xray.");
      RainbowColors.register(this::onTickRainbow);
   }

   private void loadSaveOres() {
      Thread loadth = new Thread(() -> {
         File dir = new File(MeteorClient.FOLDER, "xray-bruteforce");
         if (dir.exists()) {
            File dir2 = new File(dir, Utils.getWorldName());
            if (dir2.exists()) {
               File[] arrFiles = dir2.listFiles();

               for (File file : arrFiles) {
                  FileReader fr = null;

                  try {
                     fr = new FileReader(file);
                     BufferedReader reader = new BufferedReader(fr);

                     try {
                        String json = reader.readLine();
                        Gson gson = new Gson();
                        Type type = (new TypeToken<Map<String, Integer>>() {
                           {
                              Objects.requireNonNull(XrayBruteforce.this);
                           }
                        }).getType();
                        Map<String, Integer> read = (Map<String, Integer>)gson.fromJson(json, type);
                        BlockPos pos = new BlockPos(read.get("X"), read.get("Y"), read.get("Z"));
                        if (EntityUtils.isInRenderDistance(pos)) {
                           this.addBlock(pos, true);
                        }
                     } catch (IOException var16) {
                        var16.printStackTrace();
                     }
                  } catch (FileNotFoundException var17) {
                     var17.printStackTrace();
                  }
               }
            }
         }
      });
      loadth.start();
   }

   private void saveRenderOre(XrayBruteforce.RenderOre ore) {
      File dir = new File(MeteorClient.FOLDER, "xray-bruteforce");
      if (!dir.exists()) {
         dir.mkdir();
      }

      File dir2 = new File(dir, Utils.getWorldName());
      if (!dir2.exists()) {
         dir2.mkdir();
      }

      Gson gson = new Gson();
      Map<String, Integer> map = new LinkedHashMap<>();
      map.put("X", ore.blockPos.getX());
      map.put("Y", ore.blockPos.getY());
      map.put("Z", ore.blockPos.getZ());
      String json = gson.toJson(map);
      File file = new File(dir2.getPath(), "x={" + ore.blockPos.getX() + "}, y={" + ore.blockPos.getY() + "}, {z={" + ore.blockPos.getZ() + "}");
      if (!file.exists()) {
         try {
            file.createNewFile();
         } catch (IOException var11) {
            var11.printStackTrace();
         }
      }

      try {
         FileWriter fileWriter = new FileWriter(file);
         PrintWriter printWriter = new PrintWriter(fileWriter);
         printWriter.print(json);
         printWriter.close();
      } catch (IOException var10) {
         var10.printStackTrace();
      }
   }

   private boolean isPressed() {
      return ((Keybind)this.pausekeybind.get()).isPressed() && (Boolean)this.pauseBind.get();
   }

   private void onTickRainbow() {
      if (this.isActive()) {
         try {
            if (this.defaultBlockConfig != null && this.blockConfigs != null && this.defaultBlockConfig.get() != null) {
               ((ESPBlockData)this.defaultBlockConfig.get()).tickRainbow();
               if (this.blockConfigs.get() != null) {
                  Collection<ESPBlockData> datas = ((Map)this.blockConfigs.get()).values();
                  if (!datas.isEmpty()) {
                     for (ESPBlockData blockData : datas) {
                        if (blockData != null) {
                           blockData.tickRainbow();
                        }
                     }
                  }
               }
            }
         } catch (NullPointerException var4) {
         }
      }
   }

   @EventHandler
   private void onKeyInputEvent(KeyInputEvent event) {
      if (event.action == KeyAction.Press && this.isPressed()) {
         this.pause_toggle = !this.pause_toggle;
         if (this.pause_toggle) {
            this.currentScanBlock = null;
            this.info("§cPaused", new Object[0]);
         } else {
            this.info("§aUn paused", new Object[0]);
         }
      }
   }

   @EventHandler
   private void onMouseClickEvent(MouseClickEvent event) {
      if (event.action == KeyAction.Press && this.isPressed()) {
         this.pause_toggle = !this.pause_toggle;
         if (this.pause_toggle) {
            this.currentScanBlock = null;
            this.info("§cPaused", new Object[0]);
         } else {
            this.info("§aUn paused", new Object[0]);
         }
      }
   }

   public XBlock getSBlockData(BlockPos pos) {
      return this.oresV3.containsKey(pos) ? this.oresV3.get(pos) : null;
   }

   public XGroup newGroup(Block block) {
      synchronized (this.chunks) {
         XGroup group = new XGroup(block);
         this.groups.add(group);
         return group;
      }
   }

   public void removeGroup(XGroup group) {
      synchronized (this.chunks) {
         this.groups.remove(group);
      }
   }

   public XBlock getBlock(int x, int y, int z) {
      XChunk chunk = (XChunk)this.chunks.get(ChunkPos.pack(x >> 4, z >> 4));
      return chunk == null ? null : chunk.get(x, y, z);
   }

   private boolean setColors(XrayBruteforce.RenderOre ore) {
      if (ore == null
         || ore.block == null
         || ore.linecolor != null
         || ore.sidecolor != null
         || ore.tracercolor != null
         || ore.shapeMode != null
         || ore.sBlock != null) {
         return false;
      } else {
         assert this.mc.level != null;

         BlockState state = this.mc.level.getBlockState(ore.blockPos);
         if (ore.block == Blocks.AIR) {
            ore.block = state.getBlock();
         }

         ESPBlockData blockdata = this.getBlockData(state.getBlock());
         XBlock sbp = new XBlock(ore.blockPos.getX(), ore.blockPos.getY(), ore.blockPos.getZ());
         ore.sBlock = sbp;
         ore.sBlock.update();
         ore.linecolor = blockdata.lineColor;
         ore.sidecolor = blockdata.sideColor;
         ore.tracercolor = blockdata.tracerColor;
         ore.shapeMode = blockdata.shapeMode;
         return true;
      }
   }

   private XrayBruteforce.RenderOre getRenderOre(BlockPos pos) {
      synchronized (ores) {
         for (XrayBruteforce.RenderOre cur : ores) {
            if (cur != null && cur.block != null && cur.blockPos != null && cur.blockPos.equals(pos)) {
               return cur;
            }
         }

         return null;
      }
   }

   private void removeRenderOre(BlockPos pos) {
      synchronized (ores) {
         ores.removeIf(cur -> cur != null && cur.block != null && cur.blockPos != null && cur.blockPos.equals(pos));
      }
   }

   private void addRenderBlock(BlockPos blockPos) {
      synchronized (ores) {
         XrayBruteforce.RenderOre ore = this.getRenderOre(blockPos);
         if (ore == null) {
            XrayBruteforce.RenderOre ne = new XrayBruteforce.RenderOre(blockPos);
            ne.blockPos = blockPos;
            ores.add(ne);
         } else if (ore.sBlock != null) {
            ore.sBlock.update();
         }
      }
   }

   private void addNeedRescan(BlockPos pos, int delay) {
      synchronized (this.need_rescan) {
         XrayBruteforce.BlockScanned scanned = new XrayBruteforce.BlockScanned();
         scanned.pos = pos;
         scanned.rescanTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + delay;
         this.need_rescan.add(scanned);
      }
   }

   @EventHandler
   public void blockUpdateEvent(BlockUpdateEvent event) {
      if (!event.oldState.isAir() && scanned.contains(event.pos) && ((List)this.whblocks.get()).contains(event.newState.getBlock()) && !this.pause_toggle) {
         this.addBlock(event.pos, true);

         for (BlockPos pos2 : this.getBlocks(event.pos, (Integer)this.cavesRange.get(), (Integer)this.cavesRangeY.get())) {
            this.addBlock(pos2, true);
         }
      }

      if (event.newState.isAir()) {
         XrayBruteforce.RenderOre render = this.getRenderOre(event.pos);
         if (render != null) {
            synchronized (ores) {
               ores.remove(render);
            }
         }
      }
   }

   @EventHandler
   private void minedBlock(BreakBlockEvent event) {
      new Thread(() -> {
         XrayBruteforce.RenderOre ore = this.getRenderOre(event.blockPos);
         if (ore != null) {
            ore.block = Blocks.AIR;
            this.updateRenderedOres();
            synchronized (ores) {
               ores.remove(ore);
            }
         }
      }).start();
   }

   private void addBlock(BlockPos pos, Boolean ignore) {
      if (!scanned.contains(pos) && !ignore) {
         this.blocks.add(pos);
         scanned.add(pos);
      } else if (ignore) {
         this.blocks.add(pos);
         if (!scanned.contains(pos)) {
            scanned.add(pos);
         }
      }
   }

   public ESPBlockData getBlockData(Block block) {
      ESPBlockData blockData = (ESPBlockData)((Map)this.blockConfigs.get()).get(block);
      return blockData == null ? (ESPBlockData)this.defaultBlockConfig.get() : blockData;
   }

   private void updateRenderedOres() {
      if (!ores.isEmpty()) {
         for (XrayBruteforce.RenderOre pos : ores.toArray(new XrayBruteforce.RenderOre[0])) {
            BlockState state = this.mc.level.getBlockState(pos.blockPos);
            if (state.getBlock() == pos.block) {
               pos.sBlock.update();
            }
         }
      }
   }

   private void renderOres(Render3DEvent event) {
      int renderBlocks = 0;
      if (!ores.isEmpty()) {
         for (XrayBruteforce.RenderOre pos : ores.toArray(new XrayBruteforce.RenderOre[0])) {
            if (this.setColors(pos) && (Boolean)this.autoSave.get()) {
               Thread saveth = new Thread(() -> {
                  for (XrayBruteforce.RenderOre ore : ores.toArray(new XrayBruteforce.RenderOre[0])) {
                     this.saveRenderOre(ore);
                  }
               });
               saveth.start();
            }

            if (!(Boolean)this.new_render.get()) {
               if (EntityUtils.isInRenderDistance(pos.blockPos) && pos.block != null && ((List)this.whblocks.get()).contains(pos.block)) {
                  this.renderOreBlock(event, pos);
                  renderBlocks++;
               }
            } else if (pos.sBlock != null
               && EntityUtils.isInRenderDistance(pos.blockPos)
               && pos.block != null
               && ((List)this.whblocks.get()).contains(pos.block)) {
               pos.sBlock.render(event, pos);
               if (pos.sBlock.group != null) {
                  pos.sBlock.group.render(event);
               }

               renderBlocks++;
            }
         }
      }

      this.renderedBlocks = renderBlocks;
   }

   private void renderOreBlock(Render3DEvent event, XrayBruteforce.RenderOre ore) {
      if (ore.block != null && this.mc.level != null && ore.tracercolor != null && ore.sidecolor != null && ore.linecolor != null) {
         BlockState state = this.mc.level.getBlockState(ore.blockPos);
         VoxelShape shape = state.getShape(this.mc.level, ore.blockPos);
         ESPBlockData blockdata = this.getBlockData(ore.block);
         if (shape.isEmpty()) {
            return;
         }

         for (AABB b : shape.toAabbs()) {
            event.renderer
               .box(
                  ore.blockPos.getX() + b.minX,
                  ore.blockPos.getY() + b.minY,
                  ore.blockPos.getZ() + b.minZ,
                  ore.blockPos.getX() + b.maxX,
                  ore.blockPos.getY() + b.maxY,
                  ore.blockPos.getZ() + b.maxZ,
                  ore.sidecolor,
                  ore.linecolor,
                  blockdata.shapeMode,
                  0
               );
         }

         if (blockdata.tracer) {
            event.renderer
               .line(
                  RenderUtils.center.x,
                  RenderUtils.center.y,
                  RenderUtils.center.z,
                  ore.blockPos.getX(),
                  ore.blockPos.getY(),
                  ore.blockPos.getZ(),
                  ore.tracercolor
               );
         }
      }
   }

   @EventHandler
   private void render2d(Render2DEvent event) {
      this.lagging = false;
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if ((Boolean)this.clear_cache_blocks.get()) {
         scanned.clear();
         this.need_rescan.clear();
         this.clear_cache_blocks.set(false);
         this.info("Cache checked blocks cleared", new Object[0]);
      }

      if ((Boolean)this.clear_cache_ores.get()) {
         ores.clear();
         this.clear_cache_ores.set(false);
         this.info("Cache render ores cleared", new Object[0]);
      }

      if (this.currentScanBlock != null) {
         BlockPos bp = this.currentScanBlock;

         assert this.mc.level != null;

         BlockState state = this.mc.level.getBlockState(bp);
         VoxelShape shape = state.getShape(this.mc.level, bp);
         if (shape.isEmpty()) {
            return;
         }

         if ((Boolean)this.outline.get()) {
            for (AABB b : shape.toAabbs()) {
               event.renderer
                  .box(
                     bp.getX() + b.minX,
                     bp.getY() + b.minY,
                     bp.getZ() + b.minZ,
                     bp.getX() + b.maxX,
                     bp.getY() + b.maxY,
                     bp.getZ() + b.maxZ,
                     new SettingColor(255, 255, 255, 255),
                     (Color)this.outlineColor.get(),
                     ShapeMode.Lines,
                     0
                  );
            }
         }

         if ((Boolean)this.tracer.get()) {
            event.renderer
               .line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, bp.getX(), bp.getY(), bp.getZ(), (Color)this.tracerColor.get());
         }
      }
   }

   @EventHandler
   private void onRenderOres(Render3DEvent event) {
      this.renderOres(event);
   }

   private void addCaves(ChunkAccess chunk) {
      if (this.scanPriority.get() == XrayBruteforce.ScanPriority.Caves) {
         ArrayList<Block> caf = new ArrayList<>();
         caf.add(Blocks.AIR);
         ESPChunk s = ESPChunk.searchChunk(chunk, caf);
         if (s.blocks != null) {
            ObjectIterator var4 = s.blocks.values().iterator();

            while (var4.hasNext()) {
               ESPBlock sBlock = (ESPBlock)var4.next();

               for (Block bp : (List)this.whblocks.get()) {
                  boolean newGeneration = this.generationType.get() == XrayBruteforce.GenerationType.New;
                  GenerationBlock generationBlock = GenerationBlock.getGenerationBlock(bp, newGeneration);
                  if (generationBlock != null) {
                     BlockPos pos = new BlockPos(sBlock.x, sBlock.y, sBlock.z);
                     if (sBlock.y >= generationBlock.min_height && sBlock.y <= generationBlock.max_height) {
                        for (BlockPos pos2 : this.getBlocks(pos, (Integer)this.cavesRange.get(), (Integer)this.cavesRangeY.get())) {
                           if (!this.isExposedOre(pos2) && pos2.getY() >= generationBlock.min_height && pos2.getY() <= generationBlock.max_height) {
                              this.addBlock(pos2, false);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void addExposedBlocks() {
      if (this.mc.level != null) {
         for (ChunkAccess chunk : Utils.chunks()) {
            if ((Boolean)this.expanded.get()) {
               ESPChunk s = ESPChunk.searchChunk(chunk, (List)this.whblocks.get());
               if (s.blocks != null) {
                  ObjectIterator var5 = s.blocks.values().iterator();

                  while (var5.hasNext()) {
                     ESPBlock sBlock = (ESPBlock)var5.next();
                     BlockPos pos = new BlockPos(sBlock.x, sBlock.y, sBlock.z);
                     if (((List)this.whblocks.get()).contains(this.mc.level.getBlockState(pos).getBlock()) && this.isExposedOre(pos)) {
                        if ((Boolean)this.auto_dimension.get()) {
                           GenerationBlock generationBlock = GenerationBlock.getGenerationBlock(this.mc.level.getBlockState(pos).getBlock(), false);
                           if (generationBlock != null && generationBlock.dimension == PlayerUtils.getDimension()) {
                              this.addBlock(pos, false);

                              for (BlockPos pos2 : this.getBlocks(pos, (Integer)this.clusterRange.get(), (Integer)this.clusterRange.get())) {
                                 this.addBlock(pos2, false);
                              }
                           }
                        } else {
                           this.addBlock(pos, false);

                           for (BlockPos pos2 : this.getBlocks(pos, (Integer)this.clusterRange.get(), (Integer)this.clusterRange.get())) {
                              this.addBlock(pos2, false);
                           }
                        }
                     }
                  }
               }
            }

            this.addCaves(chunk);

            try {
               Thread.sleep(5L);
            } catch (InterruptedException var12) {
            }
         }
      }
   }

   private boolean isExposedBlock(Block block) {
      return block == Blocks.AIR || block == Blocks.WATER || block == Blocks.LAVA;
   }

   private boolean isExposedBlock(BlockState state) {
      Block block = state.getBlock();
      return this.isExposedBlock(block);
   }

   private boolean isExposedOre(BlockPos pos) {
      if (this.mc.level != null) {
         BlockState def = this.mc.level.getBlockState(pos);
         if (((List)this.whblocks.get()).contains(def.getBlock())) {
            if (this.isExposedBlock(this.mc.level.getBlockState(pos.offset(0, 1, 0)))) {
               return true;
            }

            if (this.isExposedBlock(this.mc.level.getBlockState(pos.offset(0, -1, 0)))) {
               return true;
            }

            if (this.isExposedBlock(this.mc.level.getBlockState(pos.offset(1, 0, 0)))) {
               return true;
            }

            if (this.isExposedBlock(this.mc.level.getBlockState(pos.offset(-1, 0, 0)))) {
               return true;
            }

            if (this.isExposedBlock(this.mc.level.getBlockState(pos.offset(0, 0, 1)))) {
               return true;
            }

            return this.isExposedBlock(this.mc.level.getBlockState(pos.offset(0, 0, -1)));
         }
      }

      return false;
   }

   public String getInfoString() {
      return this.pause_toggle ? "paused" : this.renderedBlocks + "b, " + this.timescan + "t";
   }

   private boolean send(BlockPos blockpos) {
      boolean success = blockpos != null;
      ClientPacketListener conn = this.mc.getConnection();
      if (conn == null) {
         success = false;
      }

      if (this.mc.level == null) {
         success = false;
      } else {
         BlockState state = this.mc.level.getBlockState(blockpos);
         if (state.getBlock() == Blocks.WALL_TORCH
            || state.getBlock() == Blocks.TORCH
            || state.getBlock() == Blocks.AIR
            || state.getBlock() == Blocks.LAVA
            || state.getBlock() == Blocks.WATER) {
            success = false;
         }
      }

      if (!EntityUtils.isInRenderDistance(blockpos)) {
         success = false;
      }

      if (success) {
         this.currentScanBlock = blockpos;
         ServerboundPlayerActionPacket packet_one = this.getPacket(blockpos, this.packet_first);
         if (packet_one != null) {
            conn.send(packet_one);
         }

         ServerboundPlayerActionPacket packet_tw = this.getPacket(blockpos, this.packet_two);
         if (packet_tw != null) {
            conn.send(packet_tw);
         }

         this.addNeedRescan(blockpos, (Integer)this.rescannerDelay.get());
      } else {
         this.currentScanBlock = null;
      }

      if (success && !scanned.contains(blockpos)) {
         scanned.add(blockpos);
      }

      return success;
   }

   private ServerboundPlayerActionPacket getPacket(BlockPos blockpos, Setting<XrayBruteforce.PacketMode> setting) {
      if (setting.get() == XrayBruteforce.PacketMode.Abort) {
         ServerboundPlayerActionPacket abort = new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, new BlockPos(blockpos), Direction.UP, 0);
         return abort;
      } else if (setting.get() == XrayBruteforce.PacketMode.Start) {
         ServerboundPlayerActionPacket start = new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, new BlockPos(blockpos), Direction.UP, 0);
         return start;
      } else {
         return setting.get() == XrayBruteforce.PacketMode.Stop
            ? new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, new BlockPos(blockpos), Direction.UP, 0)
            : null;
      }
   }

   private void checker() {
      float timeSinceLastTick = TickRate.INSTANCE.getTimeSinceLastTick();
      if (LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() >= this.millis && !this.pause_toggle) {
         if (this.blocks != null && !this.blocks.isEmpty()) {
            if ((Boolean)this.tps_sync.get() && timeSinceLastTick <= 1.0F) {
               this.work();
            } else if (!(Boolean)this.tps_sync.get()) {
               this.work();
            }
         } else if ((Boolean)this.tps_sync.get() && timeSinceLastTick <= 1.0F) {
            this.addRandomBlock();
            this.work();
         } else if (!(Boolean)this.tps_sync.get()) {
            this.addRandomBlock();
            this.work();
         }
      }
   }

   private void work() {
      try {
         long start = System.currentTimeMillis();
         Iterator<BlockPos> blocksIterator = this.blocks.iterator();
         if (blocksIterator.hasNext()) {
            if ((Boolean)this.fps_sync.get()) {
               if (!this.lagging) {
                  BlockPos block = blocksIterator.next();
                  if (this.send(block)) {
                     this.millis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        + Utils.random((Integer)this.delaymin.get(), (Integer)this.delaymax.get());
                     this.lagging = true;
                  }

                  blocksIterator.remove();
               }
            } else {
               BlockPos block = blocksIterator.next();
               blocksIterator.remove();
               if (this.send(block)) {
                  this.millis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                     + Utils.random((Integer)this.delaymin.get(), (Integer)this.delaymax.get());
                  this.lagging = true;
               }
            }
         }

         long finish = System.currentTimeMillis();
         this.timescan = finish - start;
      } catch (Exception var6) {
      }
   }

   private void addRandomBlock() {
      assert this.mc.player != null;

      int x = Utils.random(this.mc.player.blockPosition().getX() - (Integer)this.range.get(), this.mc.player.blockPosition().getX() + (Integer)this.range.get());
      int z = Utils.random(this.mc.player.blockPosition().getZ() - (Integer)this.range.get(), this.mc.player.blockPosition().getZ() + (Integer)this.range.get());
      if ((Boolean)this.auto_height.get()) {
         List<Block> findBlocks = (List<Block>)this.whblocks.get();
         boolean newGeneration = this.generationType.get() == XrayBruteforce.GenerationType.New;

         for (Block block : findBlocks) {
            GenerationBlock b = GenerationBlock.getGenerationBlock(block, newGeneration);
            if (b != null && b.block == block) {
               int y = Utils.random(b.min_height, b.max_height);
               if ((Boolean)this.auto_dimension.get() && PlayerUtils.getDimension() == b.dimension) {
                  this.addBlock(new BlockPos(x, y, z), false);
               } else if (!(Boolean)this.auto_dimension.get()) {
                  this.addBlock(new BlockPos(x, y, z), false);
               }
            } else if (b == null) {
               int y = Utils.random(
                  this.mc.player.blockPosition().getY() - (Integer)this.y_range.get(), this.mc.player.blockPosition().getY() + (Integer)this.y_range.get()
               );
               if (!scanned.contains(new BlockPos(x, y, z))) {
                  this.addBlock(new BlockPos(x, y, z), false);
               }
            }
         }
      } else {
         int y = Utils.random(
            this.mc.player.blockPosition().getY() - (Integer)this.y_range.get(), this.mc.player.blockPosition().getY() + (Integer)this.y_range.get()
         );
         if (!scanned.contains(new BlockPos(x, y, z))) {
            if ((Boolean)this.auto_dimension.get() && PlayerUtils.getDimension() == Dimension.Overworld) {
               this.addBlock(new BlockPos(x, y, z), false);
            } else if (!(Boolean)this.auto_dimension.get()) {
               this.addBlock(new BlockPos(x, y, z), false);
            }
         }
      }
   }

   @EventHandler
   private void onGameLeft(GameLeftEvent event) {
      this.need_rescan.clear();
   }

   private void reScanner() {
      synchronized (this.need_rescan) {
         float timeSinceLastTick = TickRate.INSTANCE.getTimeSinceLastTick();
         Iterator<XrayBruteforce.BlockScanned> iterator = this.need_rescan.iterator();

         while (iterator.hasNext()) {
            XrayBruteforce.BlockScanned blockscanned = iterator.next();
            if (timeSinceLastTick <= 1.0F) {
               if (LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() >= blockscanned.rescanTime && this.mc.level != null) {
                  BlockState state = this.mc.level.getBlockState(blockscanned.pos);
                  if (((List)this.whblocks.get()).contains(state.getBlock())) {
                     this.addRenderBlock(blockscanned.pos);

                     for (BlockPos pos : this.getBlocks(blockscanned.pos, (Integer)this.clusterRange.get(), (Integer)this.clusterRange.get())) {
                        this.addBlock(pos, true);
                        if (!scanned.contains(pos)) {
                           this.addNeedRescan(pos, (Integer)this.rescannerDelay.get());
                        }
                     }
                  } else {
                     XrayBruteforce.RenderOre ore = this.getRenderOre(blockscanned.pos);
                     if (ore != null) {
                        this.removeRenderOre(blockscanned.pos);
                     }
                  }

                  iterator.remove();
               }
            } else if (timeSinceLastTick > 1.0F) {
               int[] piArray = String.valueOf(1.0F / timeSinceLastTick).replaceAll("\\D", "").chars().map(Character::getNumericValue).toArray();
               blockscanned.rescanTime = blockscanned.rescanTime + Arrays.stream(piArray).skip(1L).max().getAsInt();
            }
         }
      }
   }

   public void onActivate() {
      this.scan = true;
      this.millis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
      this.clickerThread = new Thread(() -> {
         this.addRandomBlock();

         while (this.scan) {
            this.checker();
            this.reScanner();
         }
      });
      this.clickerThread.start();
      this.exposedthread = new Thread(() -> {
         while (this.scan) {
            this.addExposedBlocks();
            this.updateRenderedOres();

            try {
               Thread.sleep(25L);
            } catch (InterruptedException var2) {
            }
         }
      });
      this.exposedthread.start();
   }

   public void onDeactivate() {
      this.scan = false;
      this.blocks.clear();
      this.currentScanBlock = null;
      if (this.clickerThread != null) {
         this.clickerThread = null;
      }

      if (this.exposedthread != null) {
         this.exposedthread = null;
      }
   }

   private List<BlockPos> getBlocks(BlockPos startPos, int y_radius, int radius) {
      List<BlockPos> temp = new ArrayList<>();

      for (int dy = -y_radius; dy <= y_radius; dy++) {
         if (startPos.getY() + dy >= -60 && startPos.getY() + dy <= 360) {
            for (int dz = -radius; dz <= radius; dz++) {
               for (int dx = -radius; dx <= radius; dx++) {
                  BlockPos blockPos = startPos.offset(dx, dy, dz);
                  BlockState state = this.mc.level.getBlockState(blockPos);
                  boolean isInRenderDistance = EntityUtils.isInRenderDistance(blockPos);
                  boolean isBlockPosNotInList = !scanned.contains(blockPos);
                  if (isInRenderDistance && isBlockPosNotInList) {
                     temp.add(blockPos);
                  }
               }
            }
         }
      }

      return temp;
   }

   public class BlockScanned {
      public BlockPos pos;
      public long rescanTime;

      public BlockScanned() {
         Objects.requireNonNull(XrayBruteforce.this);
         super();
      }
   }

   public static enum GenerationType {
      Old,
      New;
   }

   public static enum PacketMode {
      None,
      Start,
      Abort,
      Stop;
   }

   public class RenderOre {
      public Block block;
      public BlockPos blockPos;
      public XBlock sBlock;
      public SettingColor linecolor;
      public SettingColor sidecolor;
      public SettingColor tracercolor;
      public ShapeMode shapeMode;

      public RenderOre(BlockPos pos) {
         Objects.requireNonNull(XrayBruteforce.this);
         super();
         this.block = Blocks.AIR;
         this.blockPos = null;
         this.sBlock = null;
         this.linecolor = null;
         this.sidecolor = null;
         this.tracercolor = null;
         this.shapeMode = null;
         this.blockPos = pos;
      }
   }

   public static enum ScanPriority {
      Caves,
      Normal;
   }
}
