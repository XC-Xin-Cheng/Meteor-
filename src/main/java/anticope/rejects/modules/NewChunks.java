package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NewChunks extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<Boolean> remove = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("remove")).description("Removes the cached chunks when disabling the module.")).defaultValue(true))
            .build()
      );
   public final Setting<Integer> renderHeight = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("render-height"))
                  .description("The height at which new chunks will be rendered"))
               .defaultValue(0))
            .min(-64)
            .sliderRange(-64, 319)
            .build()
      );
   private final Setting<ShapeMode> shapeMode = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("shape-mode"))
                  .description("How the shapes are rendered."))
               .defaultValue(ShapeMode.Both))
            .build()
      );
   private final Setting<SettingColor> newChunksSideColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("new-chunks-side-color"))
                  .description("Color of the chunks that are (most likely) completely new."))
               .defaultValue(new SettingColor(255, 0, 0, 75))
               .visible(() -> this.shapeMode.get() == ShapeMode.Sides || this.shapeMode.get() == ShapeMode.Both))
            .build()
      );
   private final Setting<SettingColor> oldChunksSideColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("old-chunks-side-color"))
                  .description("Color of the chunks that have (most likely) been loaded before."))
               .defaultValue(new SettingColor(0, 255, 0, 75))
               .visible(() -> this.shapeMode.get() == ShapeMode.Sides || this.shapeMode.get() == ShapeMode.Both))
            .build()
      );
   private final Setting<SettingColor> newChunksLineColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("new-chunks-line-color"))
                  .description("Color of the chunks that are (most likely) completely new."))
               .defaultValue(new SettingColor(255, 0, 0, 255))
               .visible(() -> this.shapeMode.get() == ShapeMode.Lines || this.shapeMode.get() == ShapeMode.Both))
            .build()
      );
   private final Setting<SettingColor> oldChunksLineColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("old-chunks-line-color"))
                  .description("Color of the chunks that have (most likely) been loaded before."))
               .defaultValue(new SettingColor(0, 255, 0, 255))
               .visible(() -> this.shapeMode.get() == ShapeMode.Lines || this.shapeMode.get() == ShapeMode.Both))
            .build()
      );
   private final Set<ChunkPos> newChunks = Collections.synchronizedSet(new HashSet<>());
   private final Set<ChunkPos> oldChunks = Collections.synchronizedSet(new HashSet<>());
   private static final Direction[] searchDirs = new Direction[]{Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP};
   private final Executor taskExecutor = Executors.newSingleThreadExecutor();

   public NewChunks() {
      super(MeteorRejectsAddon.CATEGORY, "new-chunks", "Detects completely new chunks using certain traits of them");
   }

   public void onDeactivate() {
      if ((Boolean)this.remove.get()) {
         this.newChunks.clear();
         this.oldChunks.clear();
      }

      super.onDeactivate();
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (((SettingColor)this.newChunksLineColor.get()).a > 5 || ((SettingColor)this.newChunksSideColor.get()).a > 5) {
         synchronized (this.newChunks) {
            for (ChunkPos c : this.newChunks) {
               if (this.mc.getCameraEntity().blockPosition().closerThan(c.getWorldPosition(), 1024.0)) {
                  this.render(
                     new AABB(
                        Vec3.atLowerCornerOf(c.getWorldPosition()), Vec3.atLowerCornerOf(c.getWorldPosition().offset(16, (Integer)this.renderHeight.get(), 16))
                     ),
                     (Color)this.newChunksSideColor.get(),
                     (Color)this.newChunksLineColor.get(),
                     (ShapeMode)this.shapeMode.get(),
                     event
                  );
               }
            }
         }
      }

      if (((SettingColor)this.oldChunksLineColor.get()).a > 5 || ((SettingColor)this.oldChunksSideColor.get()).a > 5) {
         synchronized (this.oldChunks) {
            for (ChunkPos cx : this.oldChunks) {
               if (this.mc.getCameraEntity().blockPosition().closerThan(cx.getWorldPosition(), 1024.0)) {
                  this.render(
                     new AABB(
                        Vec3.atLowerCornerOf(cx.getWorldPosition()),
                        Vec3.atLowerCornerOf(cx.getWorldPosition().offset(16, (Integer)this.renderHeight.get(), 16))
                     ),
                     (Color)this.oldChunksSideColor.get(),
                     (Color)this.oldChunksLineColor.get(),
                     (ShapeMode)this.shapeMode.get(),
                     event
                  );
               }
            }
         }
      }
   }

   private void render(AABB box, Color sides, Color lines, ShapeMode shapeMode, Render3DEvent event) {
      event.renderer.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, sides, lines, shapeMode, 0);
   }

   @EventHandler
   private void onReadPacket(Receive event) {
      if (event.packet instanceof ClientboundSectionBlocksUpdatePacket packet) {
         packet.runUpdates((posx, state) -> {
            if (!state.getFluidState().isEmpty() && !state.getFluidState().isSource()) {
               ChunkPos chunkPos = ChunkPos.containing(posx);

               for (Direction dir : searchDirs) {
                  if (this.mc.level.getBlockState(posx.relative(dir)).getFluidState().isSource() && !this.oldChunks.contains(chunkPos)) {
                     this.newChunks.add(chunkPos);
                     return;
                  }
               }
            }
         });
      } else if (event.packet instanceof ClientboundBlockUpdatePacket packet) {
         if (!packet.getBlockState().getFluidState().isEmpty() && !packet.getBlockState().getFluidState().isSource()) {
            ChunkPos chunkPos = ChunkPos.containing(packet.getPos());

            for (Direction dir : searchDirs) {
               if (this.mc.level.getBlockState(packet.getPos().relative(dir)).getFluidState().isSource() && !this.oldChunks.contains(chunkPos)) {
                  this.newChunks.add(chunkPos);
                  return;
               }
            }
         }
      } else if (event.packet instanceof ClientboundLevelChunkWithLightPacket && this.mc.level != null) {
         ClientboundLevelChunkWithLightPacket packetx = (ClientboundLevelChunkWithLightPacket)event.packet;
         ChunkPos pos = new ChunkPos(packetx.getX(), packetx.getZ());
         if (!this.newChunks.contains(pos) && this.mc.level.getChunkSource().getChunkForLighting(packetx.getX(), packetx.getZ()) == null) {
            LevelChunk chunk = new LevelChunk(this.mc.level, pos);

            try {
               this.taskExecutor
                  .execute(
                     () -> chunk.replaceWithPacketData(
                        packet.getChunkData().getReadBuffer(), new HashMap(), packet.getChunkData().getBlockEntitiesTagsConsumer(packet.getX(), packet.getZ())
                     )
                  );
            } catch (ArrayIndexOutOfBoundsException var9) {
               return;
            }

            for (int x = 0; x < 16; x++) {
               for (int z = 0; z < 16; z++) {
                  for (int y = this.mc.level.getMinY(); y < this.mc.level.getHeight(Types.MOTION_BLOCKING, x, z); y++) {
                     FluidState fluid = chunk.getFluidState(x, y, z);
                     if (!fluid.isEmpty() && !fluid.isSource()) {
                        this.oldChunks.add(pos);
                        return;
                     }
                  }
               }
            }
         }
      }
   }
}
