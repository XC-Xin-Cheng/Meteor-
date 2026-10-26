package com.stash.hunt.modules;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stash.hunt.Addon;
import com.stash.hunt.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StorageBlockListSetting;
import meteordevelopment.meteorclient.settings.StorageBlockListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.map.mods.SupportMods;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.module.ModuleManager;
import xaeroplus.module.impl.OldChunks;
import xaeroplus.module.impl.PaletteNewChunks;

public class BetterStashFinder extends Module {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public List<BetterStashFinder.Chunk> chunks = new ArrayList<>();
   private final Setting<List<BlockEntityType<?>>> storageBlocks = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("storage-blocks")).description("Select the storage blocks to search for."))
            .defaultValue(StorageBlockListSetting.STORAGE_BLOCKS)
            .build()
      );
   private final Setting<Integer> minimumStorageCount = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("minimum-storage-count"))
                  .description("The minimum amount of storage blocks in a chunk to record the chunk."))
               .defaultValue(4))
            .min(1)
            .sliderMin(1)
            .build()
      );
   private final Setting<Boolean> shulkerInstantHit = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("shulker-instant-hit"))
                  .description("If a single shulker counts as a stash."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> crafterInstantHit = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("crafter-instant-hit"))
                  .description("If a single auto crafter counts as a stash."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> disableOnTeleport = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("disable-on-teleport-or-death"))
                  .description("If on, will disable this module when respawning or teleporting to try to prevent coord leaks."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> ignoreTrialChambers = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-trial-chambers"))
                  .description("Attempts to ignore trial chambers, but may cause false negatives if someone made their base to look like a trial chamber."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Integer> minimumDistance = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("minimum-distance"))
                  .description("The minimum distance you must be from spawn to record a certain chunk."))
               .defaultValue(0))
            .min(0)
            .sliderMax(10000)
            .build()
      );
   private final Setting<Boolean> onlyOldchunks = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("only-old-chunks"))
                  .description("Checks that the chunks it scans have already been loaded."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> saveToWaypoints = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("save-to-waypoints"))
                     .description("Creates xaeros minimap waypoints for stash finds."))
                  .defaultValue(false))
               .onChanged(this::waypointSettingChanged))
            .build()
      );
   private final Setting<Boolean> sendNotifications = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("notifications"))
                  .description("Sends Minecraft notifications when new stashes are found."))
               .defaultValue(true))
            .build()
      );
   private final Setting<BetterStashFinder.Mode> notificationMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("notification-mode"))
                     .description("The mode to use for notifications."))
                  .defaultValue(BetterStashFinder.Mode.Both))
               .visible(this.sendNotifications::get))
            .build()
      );
   private final Setting<Boolean> sendWebhook = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("send-webhook"))
                  .description("Sends a webhook when a stash is found."))
               .defaultValue(false))
            .build()
      );
   public final Setting<String> webhookLink = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("webhook-link"))
                     .description("A discord webhook link. Looks like this: https://discord.com/api/webhooks/webhookUserId/webHookTokenOrSomething"))
                  .defaultValue(""))
               .visible(this.sendWebhook::get))
            .build()
      );
   public final Setting<Boolean> advancedLogging = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("advanced-logging"))
                  .description("Will log more information, including the amount of each container found."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> ping = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("ping-for-stash-finder"))
                     .description("Pings you for stash finder and base finder messages"))
                  .defaultValue(false))
               .visible(this.sendWebhook::get))
            .build()
      );
   public final Setting<String> discordId = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("discord-ID"))
                     .description("Your discord ID"))
                  .defaultValue(""))
               .visible(() -> (Boolean)this.sendWebhook.get() && (Boolean)this.ping.get()))
            .build()
      );
   private Vec3 lastPosition = null;

   public BetterStashFinder() {
      super(Addon.CATEGORY, "better-stash-finder", "Meteors StashFinder but with more features.");
   }

   public void onActivate() {
      this.lastPosition = null;
      XaeroPlus.EVENT_BUS.register(this);
      this.load();
   }

   public void onDeactivate() {
      XaeroPlus.EVENT_BUS.unregister(this);
   }

   @EventHandler(
      priority = -1
   )
   public void onChunkData(ChunkDataEvent event) {
      if (!event.seenChunk()) {
         double chunkXAbs = Math.abs(event.chunk().getPos().x() * 16);
         double chunkZAbs = Math.abs(event.chunk().getPos().z() * 16);
         if (!(Math.sqrt(chunkXAbs * chunkXAbs + chunkZAbs * chunkZAbs) < ((Integer)this.minimumDistance.get()).intValue())) {
            BetterStashFinder.Chunk chunk = new BetterStashFinder.Chunk(event.chunk().getPos());
            ResourceKey<Level> currentDimension = this.mc.level.dimension();
            ChunkPos chunkPos = chunk.chunkPos;
            PaletteNewChunks paletteNewChunks = (PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class);
            boolean is119NewChunk = paletteNewChunks.isNewChunk(chunkPos.x(), chunkPos.z(), currentDimension);
            boolean is112OldChunk = ((OldChunks)ModuleManager.getModule(OldChunks.class)).isOldChunk(chunkPos.x(), chunkPos.z(), currentDimension);
            if (!(Boolean)this.onlyOldchunks.get() || !is119NewChunk || is112OldChunk) {
               for (BlockEntity blockEntity : event.chunk().getBlockEntities().values()) {
                  if (((List)this.storageBlocks.get()).contains(blockEntity.getType())) {
                     Block blockUnder = this.mc.level.getBlockState(blockEntity.getBlockPos().below()).getBlock();
                     if ((!(Boolean)this.ignoreTrialChambers.get() || !blockUnder.equals(Blocks.WAXED_OXIDIZED_CUT_COPPER))
                        && !blockUnder.equals(Blocks.TUFF_BRICKS)
                        && !blockUnder.equals(Blocks.WAXED_COPPER_BLOCK)
                        && !blockUnder.equals(Blocks.WAXED_OXIDIZED_COPPER)) {
                        if (blockEntity instanceof ChestBlockEntity) {
                           chunk.chests++;
                        } else if (blockEntity instanceof BarrelBlockEntity) {
                           chunk.barrels++;
                        } else if (blockEntity instanceof ShulkerBoxBlockEntity) {
                           chunk.shulkers++;
                        } else if (blockEntity instanceof EnderChestBlockEntity) {
                           chunk.enderChests++;
                        } else if (blockEntity instanceof AbstractFurnaceBlockEntity) {
                           chunk.furnaces++;
                        } else if (blockEntity instanceof DispenserBlockEntity) {
                           chunk.dispensersDroppers++;
                        } else if (blockEntity instanceof HopperBlockEntity) {
                           chunk.hoppers++;
                        } else if (blockEntity instanceof CrafterBlockEntity) {
                           chunk.crafters++;
                        }
                     }
                  }
               }

               if (chunk.getTotal() >= (Integer)this.minimumStorageCount.get()
                  || (Boolean)this.shulkerInstantHit.get() && chunk.shulkers > 0
                  || (Boolean)this.crafterInstantHit.get() && chunk.crafters > 0) {
                  BetterStashFinder.Chunk prevChunk = null;
                  int i = this.chunks.indexOf(chunk);
                  if (i < 0) {
                     this.chunks.add(chunk);
                  } else {
                     prevChunk = this.chunks.set(i, chunk);
                  }

                  this.saveJson();
                  this.saveCsv();
                  if (!chunk.equals(prevChunk) || !chunk.countsEqual(prevChunk)) {
                     if ((Boolean)this.sendNotifications.get()) {
                        switch ((BetterStashFinder.Mode)this.notificationMode.get()) {
                           case Chat:
                              this.info("Found stash at (highlight)%s(default), (highlight)%s(default).", new Object[]{chunk.x, chunk.z});
                              break;
                           case Toast: {
                              MeteorToast toast = new meteordevelopment.meteorclient.utils.render.MeteorToast.Builder(this.title)
                                 .icon(Items.CHEST)
                                 .text("Found Stash!")
                                 .build();
                              this.mc.getToastManager().addToast(toast);
                              break;
                           }
                           case Both: {
                              this.info("Found stash at (highlight)%s(default), (highlight)%s(default).", new Object[]{chunk.x, chunk.z});
                              MeteorToast toast = new meteordevelopment.meteorclient.utils.render.MeteorToast.Builder(this.title)
                                 .icon(Items.CHEST)
                                 .text("Found Stash!")
                                 .build();
                              this.mc.getToastManager().addToast(toast);
                           }
                        }
                     }

                     if ((Boolean)this.sendWebhook.get() && !((String)this.webhookLink.get()).isEmpty()) {
                        if (!(Boolean)this.advancedLogging.get()) {
                           String message = "Found stash at " + chunk.x + ", " + chunk.z + ".";
                           new Thread(
                                 () -> Utils.sendWebhook(
                                    (String)this.webhookLink.get(),
                                    this.title,
                                    message,
                                    this.ping.get() ? (String)this.discordId.get() : null,
                                    this.mc.player.getGameProfile().name()
                                 )
                              )
                              .start();
                        } else {
                           String chunkType = "";
                           if (is119NewChunk && !is112OldChunk) {
                              chunkType = "new";
                           } else if (is119NewChunk && is112OldChunk) {
                              chunkType = "unfollowed 1.12";
                           } else if (!is119NewChunk && !is112OldChunk) {
                              chunkType = "1.19";
                           } else if (!is119NewChunk && is112OldChunk) {
                              chunkType = "followed 1.12";
                           }

                           String json = "{\"embeds\": [{\"title\": \"Stash Found!\",\"color\": 2154012,\"description\": \"Coordinates: || X: "
                              + chunk.x
                              + " Z: "
                              + chunk.z
                              + "|| in "
                              + chunkType
                              + " chunks\",\"fields\": [{\"name\": \"Chests\",\"value\": "
                              + chunk.chests
                              + ",\"inline\": true},{\"name\": \"Barrels\",\"value\": "
                              + chunk.barrels
                              + ",\"inline\": true},{\"name\": \"Shulkers\",\"value\": "
                              + chunk.shulkers
                              + ",\"inline\": true},{\"name\": \"Ender Chests\",\"value\": "
                              + chunk.enderChests
                              + ",\"inline\": true},{\"name\": \"Hoppers\",\"value\": "
                              + chunk.hoppers
                              + ",\"inline\": true},{\"name\": \"Dispensers/Droppers\",\"value\": "
                              + chunk.dispensersDroppers
                              + ",\"inline\": true},{\"name\": \"Furnaces\",\"value\": "
                              + chunk.furnaces
                              + ",\"inline\": true},{\"name\": \"Crafters\",\"value\": "
                              + chunk.crafters
                              + ",\"inline\": true}]}]}";
                           new Thread(() -> Utils.sendWebhook((String)this.webhookLink.get(), json, this.ping.get() ? (String)this.discordId.get() : null))
                              .start();
                        }
                     }

                     if ((Boolean)this.saveToWaypoints.get()) {
                        WaypointSet waypointSet = this.getWaypointSet();
                        if (waypointSet == null) {
                           return;
                        }

                        this.addToWaypoints(waypointSet, chunk);
                        SupportMods.xaeroMinimap.requestWaypointsRefresh();
                     }
                  }
               }
            }
         }
      }
   }

   public WWidget getWidget(GuiTheme theme) {
      this.chunks.sort(Comparator.comparingInt(value -> -value.getTotal()));
      WVerticalList list = theme.verticalList();
      WButton clear = (WButton)list.add(theme.button("Clear")).widget();
      WTable table = new WTable();
      if (!this.chunks.isEmpty()) {
         list.add(table);
      }

      clear.action = () -> {
         this.removeAllStashWaypoints(this.chunks);
         this.chunks.clear();
         table.clear();
      };
      this.fillTable(theme, table);
      return list;
   }

   private void fillTable(GuiTheme theme, WTable table) {
      for (BetterStashFinder.Chunk chunk : this.chunks) {
         table.add(theme.label("Pos: " + chunk.x + ", " + chunk.z));
         table.add(theme.label("Total: " + chunk.getTotal()));
         WButton open = (WButton)table.add(theme.button("Open")).widget();
         open.action = () -> this.mc.setScreen(new BetterStashFinder.ChunkScreen(theme, chunk));
         WButton gotoBtn = (WButton)table.add(theme.button("Goto")).widget();
         gotoBtn.action = () -> PathManagers.get().moveTo(new BlockPos(chunk.x, 0, chunk.z), true);
         WMinus delete = (WMinus)table.add(theme.minus()).widget();
         delete.action = () -> {
            if (this.chunks.remove(chunk)) {
               table.clear();
               this.fillTable(theme, table);
               this.saveJson();
               this.saveCsv();
               Waypoint waypoint = this.getWaypointByCoordinate(chunk.x, chunk.z);
               if (waypoint != null) {
                  WaypointSet waypointSet = this.getWaypointSet();
                  if (waypointSet != null) {
                     waypointSet.remove(waypoint);
                     SupportMods.xaeroMinimap.requestWaypointsRefresh();
                  }
               }
            }
         };
         table.row();
      }
   }

   private void load() {
      boolean loaded = false;
      File file = this.getJsonFile();
      if (file.exists()) {
         try {
            FileReader reader = new FileReader(file);
            this.chunks = (List<BetterStashFinder.Chunk>)GSON.fromJson(reader, (new TypeToken<List<BetterStashFinder.Chunk>>() {
               {
                  Objects.requireNonNull(BetterStashFinder.this);
               }
            }).getType());
            reader.close();

            for (BetterStashFinder.Chunk chunk : this.chunks) {
               chunk.calculatePos();
            }

            loaded = true;
         } catch (Exception var8) {
            if (this.chunks == null) {
               this.chunks = new ArrayList<>();
            }
         }
      }

      file = this.getCsvFile();
      if (!loaded && file.exists()) {
         try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
               String[] values = line.split(" ");
               BetterStashFinder.Chunk chunk = new BetterStashFinder.Chunk(new ChunkPos(Integer.parseInt(values[0]), Integer.parseInt(values[1])));
               chunk.chests = Integer.parseInt(values[2]);
               chunk.shulkers = Integer.parseInt(values[3]);
               chunk.enderChests = Integer.parseInt(values[4]);
               chunk.furnaces = Integer.parseInt(values[5]);
               chunk.dispensersDroppers = Integer.parseInt(values[6]);
               chunk.hoppers = Integer.parseInt(values[7]);
               this.chunks.add(chunk);
            }

            reader.close();
         } catch (Exception var7) {
            if (this.chunks == null) {
               this.chunks = new ArrayList<>();
            }
         }
      }
   }

   private void saveCsv() {
      try {
         File file = this.getCsvFile();
         file.getParentFile().mkdirs();
         Writer writer = new FileWriter(file);
         writer.write("X,Z,Chests,Barrels,Shulkers,EnderChests,Furnaces,DispensersDroppers,Hoppers\n");

         for (BetterStashFinder.Chunk chunk : this.chunks) {
            chunk.write(writer);
         }

         writer.close();
      } catch (IOException var5) {
         var5.printStackTrace();
      }
   }

   private void saveJson() {
      try {
         File file = this.getJsonFile();
         file.getParentFile().mkdirs();
         Writer writer = new FileWriter(file);
         GSON.toJson(this.chunks, writer);
         writer.close();
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   private File getJsonFile() {
      return new File(
         new File(new File(MeteorClient.FOLDER, "better-stash-finder"), meteordevelopment.meteorclient.utils.Utils.getFileWorldName()), "stashes.json"
      );
   }

   private File getCsvFile() {
      return new File(
         new File(new File(MeteorClient.FOLDER, "better-stash-finder"), meteordevelopment.meteorclient.utils.Utils.getFileWorldName()), "stashes.csv"
      );
   }

   public String getInfoString() {
      return String.valueOf(this.chunks.size());
   }

   private Waypoint getWaypointByCoordinate(int x, int z) {
      WaypointSet waypointSet = this.getWaypointSet();
      if (waypointSet == null) {
         return null;
      } else {
         for (Waypoint waypoint : waypointSet.getWaypoints()) {
            if (waypoint.getX() == x && waypoint.getZ() == z) {
               return waypoint;
            }
         }

         return null;
      }
   }

   private void removeAllStashWaypoints(List<BetterStashFinder.Chunk> chunks) {
      WaypointSet waypointSet = this.getWaypointSet();
      if (waypointSet != null) {
         for (BetterStashFinder.Chunk chunk : chunks) {
            Waypoint waypoint = this.getWaypointByCoordinate(chunk.x, chunk.z);
            if (waypoint != null) {
               waypointSet.remove(waypoint);
            }
         }

         SupportMods.xaeroMinimap.requestWaypointsRefresh();
      }
   }

   private WaypointSet getWaypointSet() {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession == null) {
         return null;
      } else {
         MinimapWorld currentWorld = minimapSession.getWorldManager().getCurrentWorld();
         return currentWorld == null ? null : currentWorld.getCurrentWaypointSet();
      }
   }

   private void addToWaypoints(WaypointSet waypointSet, BetterStashFinder.Chunk chunk) {
      int x = chunk.x;
      int z = chunk.z;
      if (this.getWaypointByCoordinate(x, z) == null) {
         String waypointName = getWaypointName(chunk);
         int color = 0;
         if (chunk.getTotal() < 15) {
            color = 10;
         } else if (chunk.getTotal() < 50) {
            color = 14;
         } else if (chunk.getTotal() < 100) {
            color = 12;
         } else if (chunk.getTotal() >= 100) {
            color = 4;
         }

         Waypoint waypoint = new Waypoint(x, 70, z, waypointName, "S", color, 0, false);
         waypointSet.add(waypoint);
      }
   }

   private static String getWaypointName(BetterStashFinder.Chunk chunk) {
      String waypointName = "";
      if (chunk.chests > 0) {
         waypointName = waypointName + "C:" + chunk.chests;
      }

      if (chunk.barrels > 0) {
         waypointName = waypointName + "B:" + chunk.barrels;
      }

      if (chunk.shulkers > 0) {
         waypointName = waypointName + "S:" + chunk.shulkers;
      }

      if (chunk.enderChests > 0) {
         waypointName = waypointName + "E:" + chunk.enderChests;
      }

      if (chunk.hoppers > 0) {
         waypointName = waypointName + "H:" + chunk.hoppers;
      }

      if (chunk.dispensersDroppers > 0) {
         waypointName = waypointName + "D:" + chunk.dispensersDroppers;
      }

      if (chunk.furnaces > 0) {
         waypointName = waypointName + "F:" + chunk.furnaces;
      }

      if (chunk.crafters > 0) {
         waypointName = waypointName + "A:" + chunk.crafters;
      }

      return waypointName;
   }

   private void waypointSettingChanged(boolean enabled) {
      if (!enabled) {
         this.removeAllStashWaypoints(this.chunks);
      } else {
         WaypointSet waypointSet = this.getWaypointSet();
         if (waypointSet == null) {
            return;
         }

         for (BetterStashFinder.Chunk chunk : this.chunks) {
            this.addToWaypoints(waypointSet, chunk);
         }

         SupportMods.xaeroMinimap.requestWaypointsRefresh();
      }
   }

   @meteordevelopment.orbit.EventHandler(
      priority = 100
   )
   private void onOpenScreenEvent(OpenScreenEvent event) {
      if (event.screen instanceof DeathScreen) {
         if ((Boolean)this.disableOnTeleport.get()) {
            this.toggle();
         }
      }
   }

   @meteordevelopment.orbit.EventHandler(
      priority = 100
   )
   private void onPlayerMove(PlayerMoveEvent event) {
      if (this.lastPosition != null && (Boolean)this.disableOnTeleport.get() && this.mc.player.distanceToSqr(this.lastPosition) > 256.0) {
         this.toggle();
      }

      this.lastPosition = this.mc.player.position();
   }

   public static class Chunk {
      private static final StringBuilder sb = new StringBuilder();
      public ChunkPos chunkPos;
      public transient int x;
      public transient int z;
      public int chests;
      public int barrels;
      public int shulkers;
      public int enderChests;
      public int furnaces;
      public int dispensersDroppers;
      public int hoppers;
      public int crafters;

      public Chunk(ChunkPos chunkPos) {
         this.chunkPos = chunkPos;
         this.calculatePos();
      }

      public void calculatePos() {
         this.x = this.chunkPos.x() * 16 + 8;
         this.z = this.chunkPos.z() * 16 + 8;
      }

      public int getTotal() {
         return this.chests + this.barrels + this.shulkers + this.enderChests + this.furnaces + this.dispensersDroppers + this.hoppers + this.crafters;
      }

      public void write(Writer writer) throws IOException {
         sb.setLength(0);
         sb.append(this.x).append(',').append(this.z).append(',');
         sb.append(this.chests)
            .append(',')
            .append(this.barrels)
            .append(',')
            .append(this.shulkers)
            .append(',')
            .append(this.enderChests)
            .append(',')
            .append(this.furnaces)
            .append(',')
            .append(this.dispensersDroppers)
            .append(',')
            .append(this.hoppers)
            .append(',')
            .append(this.crafters)
            .append('\n');
         writer.write(sb.toString());
      }

      public boolean countsEqual(BetterStashFinder.Chunk c) {
         return c == null
            ? false
            : this.chests != c.chests
               || this.barrels != c.barrels
               || this.shulkers != c.shulkers
               || this.enderChests != c.enderChests
               || this.furnaces != c.furnaces
               || this.dispensersDroppers != c.dispensersDroppers
               || this.hoppers != c.hoppers
               || this.crafters != c.crafters;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BetterStashFinder.Chunk chunk = (BetterStashFinder.Chunk)o;
            return Objects.equals(this.chunkPos, chunk.chunkPos);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.chunkPos);
      }
   }

   private static class ChunkScreen extends WindowScreen {
      private final BetterStashFinder.Chunk chunk;

      public ChunkScreen(GuiTheme theme, BetterStashFinder.Chunk chunk) {
         super(theme, "Chunk at " + chunk.x + ", " + chunk.z);
         this.chunk = chunk;
      }

      public void initWidgets() {
         WTable t = (WTable)this.add(this.theme.table()).expandX().widget();
         t.add(this.theme.label("Total:"));
         t.add(this.theme.label(this.chunk.getTotal() + ""));
         t.row();
         t.add(this.theme.horizontalSeparator()).expandX();
         t.row();
         t.add(this.theme.label("Chests:"));
         t.add(this.theme.label(this.chunk.chests + ""));
         t.row();
         t.add(this.theme.label("Barrels:"));
         t.add(this.theme.label(this.chunk.barrels + ""));
         t.row();
         t.add(this.theme.label("Shulkers:"));
         t.add(this.theme.label(this.chunk.shulkers + ""));
         t.row();
         t.add(this.theme.label("Ender Chests:"));
         t.add(this.theme.label(this.chunk.enderChests + ""));
         t.row();
         t.add(this.theme.label("Furnaces:"));
         t.add(this.theme.label(this.chunk.furnaces + ""));
         t.row();
         t.add(this.theme.label("Dispensers and droppers:"));
         t.add(this.theme.label(this.chunk.dispensersDroppers + ""));
         t.row();
         t.add(this.theme.label("Hoppers:"));
         t.add(this.theme.label(this.chunk.hoppers + ""));
         t.row();
         t.add(this.theme.label("Crafters:"));
         t.add(this.theme.label(this.chunk.crafters + ""));
      }
   }

   public static enum Mode {
      Chat,
      Toast,
      Both;
   }
}
