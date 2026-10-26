package com.stash.hunt.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stash.hunt.Addon;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public class HighlightOldLava extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> searchAbove = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("search-above")).description("Y value to look above")).sliderRange(0, 120).build());
   private final Setting<Integer> lavaHeight = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("lava-height")).description("The height of the lava to count as already loaded")).sliderRange(1, 30).build());
   private final Setting<Integer> renderDistance = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("render-distance")).description("How far away to render the blocks.")).sliderRange(0, 512).build());
   private final Setting<Boolean> disconnectOnFound = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("disconnect-on-found"))
                  .description("Will auto disconnect you if old lava is found. (Good for afking)"))
               .defaultValue(false))
            .build()
      );
   public final Setting<HighlightOldLava.Mode> logMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("log-mode"))
                  .description("How results are shown."))
               .defaultValue(HighlightOldLava.Mode.Highlight))
            .build()
      );
   public final Setting<String> webhookLink = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("webhook-link"))
                     .description("A discord webhook link. Looks like this: https://discord.com/api/webhooks/webhookUserId/webHookTokenOrSomething"))
                  .defaultValue(""))
               .visible(() -> this.logMode.get() == HighlightOldLava.Mode.LogWebhook || this.logMode.get() == HighlightOldLava.Mode.Both))
            .build()
      );
   public final Setting<Boolean> ping = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("ping-on-lava-found"))
                     .description("Pings you when lava that matches your search is found."))
                  .defaultValue(false))
               .visible(() -> this.logMode.get() == HighlightOldLava.Mode.LogWebhook || this.logMode.get() == HighlightOldLava.Mode.Both))
            .build()
      );
   public final Setting<String> discordId = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("discord-ID"))
                     .description("Your discord ID"))
                  .defaultValue(""))
               .visible(() -> this.logMode.get() == HighlightOldLava.Mode.LogWebhook || this.logMode.get() == HighlightOldLava.Mode.Both))
            .build()
      );
   private final Setting<ShapeMode> shapeMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("box-render-mode"))
                  .description("How the shape for the bounding box is rendered."))
               .defaultValue(ShapeMode.Both))
            .build()
      );
   private final Setting<SettingColor> sideColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("side-color"))
               .description("The side color of the bounding box."))
            .defaultValue(new SettingColor(16, 106, 144, 100))
            .build()
      );
   private final Setting<SettingColor> lineColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("line-color"))
               .description("The line color of the bounding box."))
            .defaultValue(new SettingColor(16, 106, 144, 255))
            .build()
      );
   protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private HashSet<BlockPos> oldLava = new HashSet<>();
   private HashSet<Vec3> loadedChunks = new HashSet<>();

   public HighlightOldLava() {
      super(Addon.CATEGORY, "highlight-old-lava", "Highlights lava that has already flowed down");
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      WButton clearLog = (WButton)list.add(theme.button("Clear saved data.")).widget();
      clearLog.action = () -> {
         File lavaPosFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "lavaPos.json");
         File loadedChunksFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "loadedChunks.json");
         lavaPosFile.delete();
         loadedChunksFile.delete();
      };
      return list;
   }

   public void onActivate() {
      this.oldLava = new HashSet<>();
      this.loadedChunks = new HashSet<>();

      try {
         File lavaPosFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "lavaPos.json");
         FileReader reader = new FileReader(lavaPosFile);
         this.oldLava = (HashSet<BlockPos>)GSON.fromJson(reader, (new TypeToken<HashSet<BlockPos>>() {
            {
               Objects.requireNonNull(HighlightOldLava.this);
            }
         }).getType());
         reader.close();
         File loadedChunksFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "loadedChunks.json");
         FileReader reader2 = new FileReader(loadedChunksFile);
         this.loadedChunks = (HashSet<Vec3>)GSON.fromJson(reader2, (new TypeToken<HashSet<Vec3>>() {
            {
               Objects.requireNonNull(HighlightOldLava.this);
            }
         }).getType());
         reader2.close();
      } catch (Exception var5) {
      }
   }

   public void onDeactivate() {
      try {
         File lavaPosFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "lavaPos.json");
         lavaPosFile.getParentFile().mkdirs();
         Writer writer = new FileWriter(lavaPosFile);
         GSON.toJson(this.oldLava, writer);
         writer.close();
         File loadedChunksFile = new File(new File(new File(MeteorClient.FOLDER, "HighlightOldLava"), Utils.getFileWorldName()), "loadedChunks.json");
         loadedChunksFile.getParentFile().mkdirs();
         Writer writer2 = new FileWriter(loadedChunksFile);
         GSON.toJson(this.loadedChunks, writer2);
         writer2.close();
      } catch (IOException | NullPointerException var5) {
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.logMode.get() == HighlightOldLava.Mode.Highlight || this.logMode.get() == HighlightOldLava.Mode.Both) {
         for (BlockPos blockPos : this.oldLava) {
            if (Math.sqrt(this.mc.player.distanceToSqr(blockPos.getCenter())) <= ((Integer)this.renderDistance.get()).intValue()) {
               RenderUtils.renderTickingBlock(
                  blockPos.immutable(), (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0, 8, true, false
               );
            }
         }
      }
   }

   @EventHandler
   private void onChunkData(ChunkDataEvent event) {
      ChunkAccess chunk = event.chunk();
      Vec3 chunkPos = chunk.getPos().getWorldPosition().getCenter();
      if (!this.loadedChunks.contains(chunkPos)) {
         this.loadedChunks.add(chunkPos);
         new HashSet();
         boolean chunkLogged = false;

         for (int x = chunk.getPos().getMinBlockX(); x <= chunk.getPos().getMaxBlockX(); x++) {
            for (int z = chunk.getPos().getMinBlockZ(); z <= chunk.getPos().getMaxBlockZ(); z++) {
               int height = chunk.getOrCreateHeightmapUnprimed(Types.WORLD_SURFACE)
                  .getFirstAvailable(x - chunk.getPos().getMinBlockX(), z - chunk.getPos().getMinBlockZ());

               for (int y = height; y > this.searchAbove.get(); y--) {
                  BlockPos blockPos = new BlockPos(x, y, z);
                  BlockState blockState = chunk.getBlockState(blockPos);
                  if (blockState.getBlock() == Blocks.POLISHED_BLACKSTONE_BRICKS || blockState.getBlock() == Blocks.NETHER_BRICKS) {
                     return;
                  }

                  if (blockState.getBlock() == Blocks.LAVA) {
                     boolean heightFound = true;

                     for (int i = 1; i < this.lavaHeight.get(); i++) {
                        if (chunk.getBlockState(blockPos.offset(0, i, 0)).getBlock() != Blocks.LAVA) {
                           heightFound = false;
                           break;
                        }
                     }

                     if (heightFound) {
                        if ((this.logMode.get() == HighlightOldLava.Mode.LogWebhook || this.logMode.get() == HighlightOldLava.Mode.Both)
                           && !((String)this.webhookLink.get()).isEmpty()) {
                           new Thread(
                                 () -> com.stash.hunt.Utils.sendWebhook(
                                    (String)this.webhookLink.get(),
                                    "Old Chunk Found",
                                    "At: " + blockPos.getX() + " " + blockPos.getZ(),
                                    this.ping.get() ? (String)this.discordId.get() : null,
                                    this.mc.player.getGameProfile().name()
                                 )
                              )
                              .start();
                        }

                        if ((Boolean)this.disconnectOnFound.get()) {
                           this.mc
                              .player
                              .connection
                              .handleDisconnect(new ClientboundDisconnectPacket(Component.literal("[HighlightOldLava] Old lava was found.")));
                        }

                        this.oldLava.add(blockPos);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   public static enum Mode {
      Highlight,
      LogWebhook,
      Both;
   }
}
