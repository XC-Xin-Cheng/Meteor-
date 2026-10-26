package com.stash.hunt.modules;

import com.stash.hunt.Addon;
import com.stash.hunt.Utils;
import java.util.ArrayDeque;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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

public class OldChunkNotifier extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> notifyAnyChunks = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("notify-any-chunks")).description("Whether to notify you of any old chunks.")).defaultValue(false))
            .build()
      );
   private final Setting<Boolean> notifyOffHighway = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("notify-trails-off-highway")).description("Whether to notify you of old chunks off the highway."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Double> directionOfTravel = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("direction-of-travel"))
                  .description("The direction of travel (yaw) in degrees."))
               .defaultValue(0.0)
               .min(-180.0)
               .max(180.0)
               .visible(this.notifyOffHighway::get))
            .build()
      );
   private final Setting<Double> distanceOffAxis = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("distance-off-axis"))
                  .description("The distance in chunks off the axis of movement from the player to check for old chunks."))
               .defaultValue(13.0)
               .sliderRange(0.0, 15.0)
               .visible(this.notifyOffHighway::get))
            .build()
      );
   private final Setting<OldChunkNotifier.ChunkTypeMode> chunkTypeMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("chunk-type"))
                  .description("Which type of old chunks to detect."))
               .defaultValue(OldChunkNotifier.ChunkTypeMode.BOTH))
            .build()
      );
   private final Setting<OldChunkNotifier.LogType> logType = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("log-type"))
                  .description("What to do when an old chunk is detected."))
               .defaultValue(OldChunkNotifier.LogType.Marker))
            .build()
      );
   private final Setting<String> webhookLink = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("webhook-link"))
                     .description("A discord webhook link. Looks like this: https://discord.com/api/webhooks/webhookUserId/webHookTokenOrSomething"))
                  .defaultValue(""))
               .visible(() -> this.logType.get() == OldChunkNotifier.LogType.Webhook || this.logType.get() == OldChunkNotifier.LogType.Both))
            .build()
      );
   private final Setting<Boolean> ping = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("ping")).description("Whether to ping you or not.")).defaultValue(false))
               .visible(() -> this.logType.get() == OldChunkNotifier.LogType.Webhook || this.logType.get() == OldChunkNotifier.LogType.Both))
            .build()
      );
   private final Setting<String> discordId = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("discord-ID"))
                     .description("Your discord ID"))
                  .defaultValue(""))
               .visible(
                  () -> (Boolean)this.ping.get()
                     && (this.logType.get() == OldChunkNotifier.LogType.Webhook || this.logType.get() == OldChunkNotifier.LogType.Both)
               ))
            .build()
      );
   public final Setting<OldChunkNotifier.DimensionMode> dimensionMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("dimension-mode"))
                  .description("Choose where the module will detect old chunks."))
               .defaultValue(OldChunkNotifier.DimensionMode.All))
            .build()
      );
   private final ArrayDeque<ChunkPos> oldChunks = new ArrayDeque<>();

   public OldChunkNotifier() {
      super(Addon.CATEGORY, "old-chunk-notifier", "Sends a webhook message and optionally pings you when an old chunk is detected.");
   }

   public void onActivate() {
      XaeroPlus.EVENT_BUS.register(this);
      this.oldChunks.clear();
   }

   public void onDeactivate() {
      XaeroPlus.EVENT_BUS.unregister(this);
   }

   @EventHandler(
      priority = -1
   )
   public void onChunkData(ChunkDataEvent event) {
      if (!event.seenChunk()) {
         if (!this.mc.player.getAbilities().mayfly) {
            if ((this.dimensionMode.get() != OldChunkNotifier.DimensionMode.Nether || this.mc.level.dimension() == Level.NETHER)
               && (this.dimensionMode.get() != OldChunkNotifier.DimensionMode.End || this.mc.level.dimension() == Level.END)
               && (this.dimensionMode.get() != OldChunkNotifier.DimensionMode.Overworld || this.mc.level.dimension() == Level.OVERWORLD)) {
               if (this.oldChunks.size() > 1000) {
                  this.oldChunks.removeFirst();
               }

               if (!this.oldChunks.contains(event.chunk().getPos())) {
                  this.oldChunks.add(event.chunk().getPos());
                  boolean is119NewChunk = ((PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class))
                     .isNewChunk(event.chunk().getPos().x(), event.chunk().getPos().z(), event.chunk().getLevel().dimension());
                  boolean is112OldChunk = ((OldChunks)ModuleManager.getModule(OldChunks.class))
                     .isOldChunk(event.chunk().getPos().x(), event.chunk().getPos().z(), event.chunk().getLevel().dimension());
                  OldChunkNotifier.ChunkTypeMode typeMode = (OldChunkNotifier.ChunkTypeMode)this.chunkTypeMode.get();
                  boolean shouldNotify = false;
                  switch (typeMode) {
                     case ONLY_112:
                        shouldNotify = is112OldChunk;
                        break;
                     case ONLY_119:
                        shouldNotify = !is119NewChunk && !is112OldChunk;
                        break;
                     case BOTH:
                        shouldNotify = !is119NewChunk || is112OldChunk;
                  }

                  if (shouldNotify) {
                     if ((Boolean)this.notifyAnyChunks.get()) {
                        if (this.logType.get() == OldChunkNotifier.LogType.Both || this.logType.get() == OldChunkNotifier.LogType.Marker) {
                           this.createMapMarker(event.chunk().getPos().x(), event.chunk().getPos().z());
                        }

                        if (this.logType.get() == OldChunkNotifier.LogType.Both || this.logType.get() == OldChunkNotifier.LogType.Webhook) {
                           String message = "";
                           if (is112OldChunk && !is119NewChunk) {
                              message = "1.12 Followed in 1.19+ Old Chunk Detected";
                           } else if (is112OldChunk && is119NewChunk) {
                              message = "1.12 Unfollowed in 1.19+ Old Chunk Detected";
                           } else {
                              message = "1.19+ Old Chunk Detected";
                           }

                           String finalMessage = message;
                           String discordID = this.ping.get() && !((String)this.discordId.get()).isBlank() ? (String)this.discordId.get() : null;
                           new Thread(
                                 () -> Utils.sendWebhook(
                                    (String)this.webhookLink.get(),
                                    "Old Chunk Detected",
                                    finalMessage + " at " + this.mc.player.position().toString(),
                                    discordID,
                                    this.mc.player.getGameProfile().name()
                                 )
                              )
                              .start();
                        }
                     }

                     if ((Boolean)this.notifyOffHighway.get()) {
                        ChunkPos chunkPos = event.chunk().getPos();
                        Vec3 direction = Utils.yawToDirection((Double)this.directionOfTravel.get());
                        ChunkPos playerChunkPos = this.mc.player.chunkPosition();
                        double distance = Utils.distancePointToDirection(
                           new Vec3(chunkPos.x(), 0.0, chunkPos.z()), direction, new Vec3(playerChunkPos.x(), 0.0, playerChunkPos.z())
                        );
                        if (distance > (Double)this.distanceOffAxis.get()) {
                           if (this.logType.get() == OldChunkNotifier.LogType.Both || this.logType.get() == OldChunkNotifier.LogType.Marker) {
                              this.createMapMarker(chunkPos.x(), chunkPos.z());
                           }

                           if (this.logType.get() == OldChunkNotifier.LogType.Both || this.logType.get() == OldChunkNotifier.LogType.Webhook) {
                              String discordID = this.ping.get() && !((String)this.discordId.get()).isBlank() ? (String)this.discordId.get() : null;
                              new Thread(
                                    () -> Utils.sendWebhook(
                                       (String)this.webhookLink.get(),
                                       "Old Chunk Detected",
                                       "Old chunk detected off the highway at " + chunkPos.x() * 16 + " " + chunkPos.z() * 16,
                                       discordID,
                                       this.mc.player.getGameProfile().name()
                                    )
                                 )
                                 .start();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void createMapMarker(int x, int z) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         MinimapWorld currentWorld = minimapSession.getWorldManager().getCurrentWorld();
         if (currentWorld != null) {
            WaypointSet waypointSet = currentWorld.getCurrentWaypointSet();
            if (waypointSet != null) {
               Waypoint waypoint = new Waypoint(x * 16, 70, z * 16, "Old Chunk", "O", 5, 0, false);
               waypointSet.add(waypoint);
               SupportMods.xaeroMinimap.requestWaypointsRefresh();
            }
         }
      }
   }

   public static enum ChunkTypeMode {
      ONLY_112("1.12 Only"),
      ONLY_119("1.19+ Only"),
      BOTH("Both");

      private final String displayName;

      private ChunkTypeMode(String displayName) {
         this.displayName = displayName;
      }

      @Override
      public String toString() {
         return this.displayName;
      }
   }

   public static enum DimensionMode {
      Overworld,
      Nether,
      End,
      All;
   }

   private static enum LogType {
      Webhook,
      Marker,
      Both;
   }
}
