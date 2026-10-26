package zgoly.meteorist.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.mixin.MobSpawnerLogicAccessor;

public class NerdVision extends Module {
   private final SettingGroup sgSpawn = this.settings.createGroup("Spawn");
   private final SettingGroup sgTurtleEggs = this.settings.createGroup("Turtle Eggs");
   private final SettingGroup sgSpawners = this.settings.createGroup("Spawners");
   private final SettingGroup sgIronGolems = this.settings.createGroup("Iron Golems");
   private final Setting<Boolean> spawnRangesEnabled = this.sgSpawn
      .add(((Builder)((Builder)((Builder)new Builder().name("spawn-ranges-enabled")).description("Enables spawn range rendering.")).defaultValue(true)).build());
   private final Setting<NerdVision.SphereType> spawnRangesSphereType = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("spawn-range-sphere-type"))
                     .description("Choose how spawn ranges are visualized: default spherical or cubic grid representation."))
                  .defaultValue(NerdVision.SphereType.Default))
               .visible(this.spawnRangesEnabled::get))
            .build()
      );
   private final Setting<NerdVision.CenterPositionType> centerPositionType = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("center-position-type"))
                     .description("Selects the type of position for the center."))
                  .defaultValue(NerdVision.CenterPositionType.Pos))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && this.spawnRangesSphereType.get() == NerdVision.SphereType.Default))
            .build()
      );
   private final Setting<Vector3d> centerPosition = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)new meteordevelopment.meteorclient.settings.Vector3dSetting.Builder()
                     .name("center-position"))
                  .description("Sets the position for the center."))
               .defaultValue(new Vector3d(0.0, 0.0, 0.0))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && this.centerPositionType.get() == NerdVision.CenterPositionType.Pos))
            .build()
      );
   private final Setting<BlockPos> centerBlockPosition = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                        .name("center-block-position"))
                     .description("Sets the block position for the center."))
                  .defaultValue(new BlockPos(0, 0, 0)))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && this.centerPositionType.get() == NerdVision.CenterPositionType.BlockPos))
            .build()
      );
   private final Setting<Integer> spawnRangeSpherePhiCount = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("spawn-range-sphere-phi-count"))
                     .description("Sets the sphere phi count for the spawn range."))
                  .defaultValue(16))
               .min(4)
               .sliderRange(4, 128)
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && this.spawnRangesSphereType.get() == NerdVision.SphereType.Default))
            .build()
      );
   private final Setting<Integer> spawnRangeSphereThetaCount = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("spawn-range-sphere-theta-count"))
                     .description("Sets the sphere theta count for the spawn range."))
                  .defaultValue(16))
               .min(4)
               .sliderRange(4, 128)
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && this.spawnRangesSphereType.get() == NerdVision.SphereType.Default))
            .build()
      );
   private final Setting<Integer> idleRangeSpherePhiCount = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("idle-range-sphere-phi-count"))
                     .description("Sets the sphere phi count for the idle range."))
                  .defaultValue(24))
               .min(4)
               .sliderRange(4, 128)
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && this.spawnRangesSphereType.get() == NerdVision.SphereType.Default))
            .build()
      );
   private final Setting<Integer> idleRangeSphereThetaCount = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("idle-range-sphere-theta-count"))
                     .description("Sets the sphere theta count for the idle range."))
                  .defaultValue(24))
               .min(4)
               .sliderRange(4, 128)
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && this.spawnRangesSphereType.get() == NerdVision.SphereType.Default))
            .build()
      );
   private final Setting<Integer> despawnRangeSpherePhiCount = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("despawn-range-sphere-phi-count"))
                     .description("Sets the sphere phi count for the despawn range."))
                  .defaultValue(32))
               .min(4)
               .sliderRange(4, 128)
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && this.spawnRangesSphereType.get() == NerdVision.SphereType.Default))
            .build()
      );
   private final Setting<Integer> despawnRangeSphereThetaCount = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("despawn-range-sphere-theta-count"))
                     .description("Sets the sphere theta count for the despawn range."))
                  .defaultValue(32))
               .min(4)
               .sliderRange(4, 128)
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && this.spawnRangesSphereType.get() == NerdVision.SphereType.Default))
            .build()
      );
   private final Setting<NerdVision.CenterPositionMode> centerPositionMode = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("center-position-mode"))
                     .description("Determines how the center is calculated: relative to the player or absolute."))
                  .defaultValue(NerdVision.CenterPositionMode.Relative))
               .visible(this.spawnRangesEnabled::get))
            .build()
      );
   private final Setting<Boolean> displayCenterPosition = this.sgSpawn
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("display-center-position")).description("Displays the center position indicator."))
                  .defaultValue(true))
               .visible(this.spawnRangesEnabled::get))
            .build()
      );
   private final Setting<Vector3d> centerPositionSize = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)new meteordevelopment.meteorclient.settings.Vector3dSetting.Builder()
                     .name("center-position-size"))
                  .description("Sets the size of the center position indicator."))
               .defaultValue(new Vector3d(1.0, 0.0, 1.0))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.displayCenterPosition.get()))
            .build()
      );
   private final Setting<ShapeMode> centerPositionShapeMode = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("center-position-shape-mode"))
                     .description("Sets the shape mode for the center position."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.displayCenterPosition.get()))
            .build()
      );
   private final Setting<SettingColor> centerPositionSideColor = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("center-position-side-color"))
                  .description("Sets the center position side color."))
               .defaultValue(new SettingColor(0, 255, 255, 1))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.displayCenterPosition.get()))
            .build()
      );
   private final Setting<SettingColor> centerPositionLineColor = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("center-position-line-color"))
                  .description("Sets the center position line color."))
               .defaultValue(new SettingColor(0, 255, 255, 255))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.displayCenterPosition.get()))
            .build()
      );
   private final Setting<Boolean> spawnRangeEnabled = this.sgSpawn
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("spawn-range-enabled")).description("Enables spawn range rendering.")).defaultValue(true))
               .visible(this.spawnRangesEnabled::get))
            .build()
      );
   private final Setting<Double> spawnSphereRange = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("spawn-sphere-range"))
                  .description("Sets the spawn sphere range."))
               .defaultValue(24.0)
               .min(0.0)
               .sliderRange(0.0, 24.0)
               .visible(
                  () -> (Boolean)this.spawnRangesEnabled.get()
                     && (Boolean)this.spawnRangeEnabled.get()
                     && this.spawnRangesSphereType.get() == NerdVision.SphereType.Default
               ))
            .build()
      );
   private final Setting<Integer> spawnBlockSphereRange = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("spawn-block-sphere-range"))
                     .description("Sets the spawn sphere range."))
                  .defaultValue(24))
               .min(1)
               .sliderRange(1, 24)
               .visible(
                  () -> (Boolean)this.spawnRangesEnabled.get()
                     && (Boolean)this.spawnRangeEnabled.get()
                     && this.spawnRangesSphereType.get() == NerdVision.SphereType.Cubic
               ))
            .build()
      );
   private final Setting<ShapeMode> spawnRangeShapeMode = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("spawn-range-shape-mode"))
                     .description("Sets the shape mode for the spawn range."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.spawnRangeEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnRangeSideColor = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("spawn-range-side-color"))
                  .description("Sets the spawn range side color."))
               .defaultValue(new SettingColor(255, 0, 0, 10))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.spawnRangeEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnRangeLineColor = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("spawn-range-line-color"))
                  .description("Sets the spawn range line color."))
               .defaultValue(new SettingColor(255, 0, 0, 255))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.spawnRangeEnabled.get()))
            .build()
      );
   private final Setting<Boolean> idleRangeEnabled = this.sgSpawn
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("idle-range-enabled")).description("Enables idle range rendering.")).defaultValue(true))
               .visible(this.spawnRangesEnabled::get))
            .build()
      );
   private final Setting<Double> idleSphereRange = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("idle-sphere-range"))
                  .description("Sets the idle sphere range."))
               .defaultValue(32.0)
               .min(0.0)
               .sliderRange(0.0, 32.0)
               .visible(
                  () -> (Boolean)this.spawnRangesEnabled.get()
                     && (Boolean)this.idleRangeEnabled.get()
                     && this.spawnRangesSphereType.get() == NerdVision.SphereType.Default
               ))
            .build()
      );
   private final Setting<Integer> idleBlockSphereRange = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("idle-block-sphere-range"))
                     .description("Sets the idle sphere range."))
                  .defaultValue(32))
               .min(1)
               .sliderRange(1, 32)
               .visible(
                  () -> (Boolean)this.spawnRangesEnabled.get()
                     && (Boolean)this.idleRangeEnabled.get()
                     && this.spawnRangesSphereType.get() == NerdVision.SphereType.Cubic
               ))
            .build()
      );
   private final Setting<ShapeMode> idleRangeShapeMode = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("idle-range-shape-mode"))
                     .description("Sets the shape mode for the idle range."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.idleRangeEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> idleRangeSideColor = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("idle-range-side-color"))
                  .description("Sets the idle range side color."))
               .defaultValue(new SettingColor(255, 255, 0, 10))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.idleRangeEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> idleRangeLineColor = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("idle-range-line-color"))
                  .description("Sets the idle range line color."))
               .defaultValue(new SettingColor(255, 255, 0, 255))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.idleRangeEnabled.get()))
            .build()
      );
   private final Setting<Boolean> despawnRangeEnabled = this.sgSpawn
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("despawn-range-enabled")).description("Enables despawn range rendering."))
                  .defaultValue(true))
               .visible(this.spawnRangesEnabled::get))
            .build()
      );
   private final Setting<Double> despawnSphereRange = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("despawn-sphere-range"))
                  .description("Sets the despawn sphere range."))
               .defaultValue(128.0)
               .min(0.0)
               .sliderRange(0.0, 128.0)
               .visible(
                  () -> (Boolean)this.spawnRangesEnabled.get()
                     && (Boolean)this.despawnRangeEnabled.get()
                     && this.spawnRangesSphereType.get() == NerdVision.SphereType.Default
               ))
            .build()
      );
   private final Setting<Integer> despawnBlockSphereRange = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("despawn-block-sphere-range"))
                     .description("Sets the despawn sphere range."))
                  .defaultValue(128))
               .min(1)
               .sliderRange(1, 128)
               .visible(
                  () -> (Boolean)this.spawnRangesEnabled.get()
                     && (Boolean)this.despawnRangeEnabled.get()
                     && this.spawnRangesSphereType.get() == NerdVision.SphereType.Cubic
               ))
            .build()
      );
   private final Setting<ShapeMode> despawnRangeShapeMode = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("despawn-range-shape-mode"))
                     .description("Sets the shape mode for the despawn range."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.despawnRangeEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> despawnRangeSideColor = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("despawn-range-side-color"))
                  .description("Sets the despawn range side color."))
               .defaultValue(new SettingColor(0, 255, 0, 10))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.despawnRangeEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> despawnRangeLineColor = this.sgSpawn
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("despawn-range-line-color"))
                  .description("Sets the despawn range line color."))
               .defaultValue(new SettingColor(0, 255, 0, 255))
               .visible(() -> (Boolean)this.spawnRangesEnabled.get() && (Boolean)this.despawnRangeEnabled.get()))
            .build()
      );
   private final Setting<Boolean> turtleEggsRenderEnabled = this.sgTurtleEggs
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("enabled")).description("Enables turtle eggs rendering.")).defaultValue(false))
               .onChanged(value -> this.onActivate()))
            .build()
      );
   private final Setting<Vector3d> turtleEggsRangeSize = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)new meteordevelopment.meteorclient.settings.Vector3dSetting.Builder()
                     .name("range-size"))
                  .description("Sets the size of the turtle eggs range."))
               .defaultValue(new Vector3d(47.0, 7.0, 47.0))
               .visible(this.turtleEggsRenderEnabled::get))
            .build()
      );
   private final Setting<Boolean> turtleEggsRangeEnabled = this.sgTurtleEggs
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("range-enabled")).description("Enables turtle eggs range rendering."))
                     .defaultValue(true))
                  .onChanged(value -> this.onActivate()))
               .visible(this.turtleEggsRenderEnabled::get))
            .build()
      );
   private final Setting<ShapeMode> turtleEggsRangeShapeMode = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("range-shape-mode"))
                     .description("Sets the shape mode for the turtle eggs range."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.turtleEggsRangeEnabled.get() && (Boolean)this.turtleEggsRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> turtleEggsRangeSideColor = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("range-side-color"))
                  .description("Sets the turtle eggs range side color."))
               .defaultValue(new SettingColor(255, 255, 255, 10))
               .visible(() -> (Boolean)this.turtleEggsRangeEnabled.get() && (Boolean)this.turtleEggsRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> turtleEggsRangeLineColor = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("range-line-color"))
                  .description("Sets the turtle eggs range line color."))
               .defaultValue(new SettingColor(255, 255, 255, 255))
               .visible(() -> (Boolean)this.turtleEggsRangeEnabled.get() && (Boolean)this.turtleEggsRenderEnabled.get()))
            .build()
      );
   private final Setting<Boolean> turtleEggsZombiesRangeEnabled = this.sgTurtleEggs
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("zombies-range-enabled")).description("Enables turtle eggs zombies range rendering."))
                  .defaultValue(false))
               .visible(this.turtleEggsRenderEnabled::get))
            .build()
      );
   private final Setting<ShapeMode> turtleEggsZombiesRangeShapeMode = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("zombies-range-shape-mode"))
                     .description("Sets the shape mode for the turtle eggs zombies range."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.turtleEggsZombiesRangeEnabled.get() && (Boolean)this.turtleEggsRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> turtleEggsZombiesRangeSideColor = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("zombies-range-side-color"))
                  .description("Sets the turtle eggs zombies range side color."))
               .defaultValue(new SettingColor(255, 0, 0, 10))
               .visible(() -> (Boolean)this.turtleEggsZombiesRangeEnabled.get() && (Boolean)this.turtleEggsRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> turtleEggsZombiesRangeLineColor = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("zombies-range-line-color"))
                  .description("Sets the turtle eggs zombies range line color."))
               .defaultValue(new SettingColor(255, 0, 0, 255))
               .visible(() -> (Boolean)this.turtleEggsZombiesRangeEnabled.get() && (Boolean)this.turtleEggsRenderEnabled.get()))
            .build()
      );
   private final Setting<Boolean> turtleEggsAggressionRenderEnabled = this.sgTurtleEggs
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("aggression-render-enabled"))
                        .description("Enables turtle eggs aggression render."))
                     .defaultValue(true))
                  .onChanged(value -> this.onActivate()))
               .visible(this.turtleEggsRenderEnabled::get))
            .build()
      );
   private final Setting<Boolean> turtleEggsAggressionEggRenderEnabled = this.sgTurtleEggs
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("aggression-egg-render-enabled")).description("Enables turtle eggs aggression egg render."))
                  .defaultValue(true))
               .visible(() -> (Boolean)this.turtleEggsAggressionRenderEnabled.get() && (Boolean)this.turtleEggsRenderEnabled.get()))
            .build()
      );
   private final Setting<ShapeMode> turtleEggsAggressionEggRenderShapeMode = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("aggression-egg-render-shape-mode"))
                     .description("Sets the shape mode for the turtle eggs aggression render."))
                  .defaultValue(ShapeMode.Both))
               .visible(
                  () -> (Boolean)this.turtleEggsAggressionEggRenderEnabled.get()
                     && (Boolean)this.turtleEggsAggressionRenderEnabled.get()
                     && (Boolean)this.turtleEggsRenderEnabled.get()
               ))
            .build()
      );
   private final Setting<SettingColor> turtleEggsAggressionEggRenderSideColor = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("aggression-egg-render-side-color"))
                  .description("Sets the turtle eggs aggression render side color."))
               .defaultValue(new SettingColor(255, 255, 0, 10))
               .visible(
                  () -> (Boolean)this.turtleEggsAggressionEggRenderEnabled.get()
                     && (Boolean)this.turtleEggsAggressionRenderEnabled.get()
                     && (Boolean)this.turtleEggsRenderEnabled.get()
               ))
            .build()
      );
   private final Setting<Boolean> turtleEggsAggressionZombieRenderEnabled = this.sgTurtleEggs
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("aggression-zombie-render-enabled"))
                     .description("Enables turtle eggs aggression zombie render."))
                  .defaultValue(true))
               .visible(() -> (Boolean)this.turtleEggsAggressionRenderEnabled.get() && (Boolean)this.turtleEggsRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> turtleEggsAggressionEggRenderLineColor = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("aggression-egg-render-line-color"))
                  .description("Sets the turtle eggs aggression render line color."))
               .defaultValue(new SettingColor(255, 255, 0, 255))
               .visible(
                  () -> (Boolean)this.turtleEggsAggressionEggRenderEnabled.get()
                     && (Boolean)this.turtleEggsAggressionRenderEnabled.get()
                     && (Boolean)this.turtleEggsRenderEnabled.get()
               ))
            .build()
      );
   private final Setting<ShapeMode> turtleEggsAggressionZombieRenderShapeMode = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("aggression-zombie-render-shape-mode"))
                     .description("Sets the shape mode for the turtle eggs aggression render."))
                  .defaultValue(ShapeMode.Both))
               .visible(
                  () -> (Boolean)this.turtleEggsAggressionZombieRenderEnabled.get()
                     && (Boolean)this.turtleEggsAggressionRenderEnabled.get()
                     && (Boolean)this.turtleEggsRenderEnabled.get()
               ))
            .build()
      );
   private final Setting<SettingColor> turtleEggsAggressionZombieRenderSideColor = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("aggression-zombie-render-side-color"))
                  .description("Sets the turtle eggs aggression render side color."))
               .defaultValue(new SettingColor(255, 0, 0, 10))
               .visible(
                  () -> (Boolean)this.turtleEggsAggressionZombieRenderEnabled.get()
                     && (Boolean)this.turtleEggsAggressionRenderEnabled.get()
                     && (Boolean)this.turtleEggsRenderEnabled.get()
               ))
            .build()
      );
   private final Setting<SettingColor> turtleEggsAggressionZombieRenderLineColor = this.sgTurtleEggs
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("aggression-zombie-render-line-color"))
                  .description("Sets the turtle eggs aggression render line color."))
               .defaultValue(new SettingColor(255, 0, 0, 255))
               .visible(
                  () -> (Boolean)this.turtleEggsAggressionZombieRenderEnabled.get()
                     && (Boolean)this.turtleEggsAggressionRenderEnabled.get()
                     && (Boolean)this.turtleEggsRenderEnabled.get()
               ))
            .build()
      );
   private final Setting<Boolean> spawnersRenderEnabled = this.sgSpawners
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("enabled")).description("Enables spawners rendering.")).defaultValue(false))
               .onChanged(value -> this.onActivate()))
            .build()
      );
   private final Setting<Boolean> spawnersRenderSphereEnabled = this.sgSpawners
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("render-sphere-enabled")).description("Enables spawners render sphere rendering."))
                  .defaultValue(true))
               .visible(this.spawnersRenderEnabled::get))
            .build()
      );
   private final Setting<NerdVision.SphereType> spawnersRenderSphereType = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("render-sphere-visualization-type"))
                     .description("Choose how spawners render spheres are visualized: default spherical or cubic grid representation."))
                  .defaultValue(NerdVision.SphereType.Default))
               .visible(() -> (Boolean)this.spawnersRenderEnabled.get() && (Boolean)this.spawnersRenderSphereEnabled.get()))
            .build()
      );
   private final Setting<Integer> spawnersRenderSpherePhiCount = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("render-sphere-phi-count"))
                     .description("Sets the sphere phi count for the spawners render sphere."))
                  .defaultValue(16))
               .min(4)
               .sliderRange(4, 128)
               .visible(
                  () -> (Boolean)this.spawnersRenderEnabled.get()
                     && (Boolean)this.spawnersRenderSphereEnabled.get()
                     && this.spawnersRenderSphereType.get() == NerdVision.SphereType.Default
               ))
            .build()
      );
   private final Setting<Integer> spawnersRenderSphereThetaCount = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("render-sphere-theta-count"))
                     .description("Sets the sphere theta count for the spawners render sphere."))
                  .defaultValue(16))
               .min(4)
               .sliderRange(4, 128)
               .visible(
                  () -> (Boolean)this.spawnersRenderEnabled.get()
                     && (Boolean)this.spawnersRenderSphereEnabled.get()
                     && this.spawnersRenderSphereType.get() == NerdVision.SphereType.Default
               ))
            .build()
      );
   private final Setting<ShapeMode> spawnersRenderSphereShapeMode = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("render-sphere-shape-mode"))
                     .description("Sets the shape mode for the spawners render sphere."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.spawnersRenderEnabled.get() && (Boolean)this.spawnersRenderSphereEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnersRenderSphereSideColor = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("render-sphere-side-color"))
                  .description("Sets the spawners render sphere side color."))
               .defaultValue(new SettingColor(0, 255, 0, 10))
               .visible(() -> (Boolean)this.spawnersRenderEnabled.get() && (Boolean)this.spawnersRenderSphereEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnersRenderSphereLineColor = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("render-sphere-line-color"))
                  .description("Sets the spawners render sphere line color."))
               .defaultValue(new SettingColor(0, 255, 0, 255))
               .visible(() -> (Boolean)this.spawnersRenderEnabled.get() && (Boolean)this.spawnersRenderSphereEnabled.get()))
            .build()
      );
   private final Setting<Boolean> spawnersRenderDetectingZoneEnabled = this.sgSpawners
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("detecting-zone-enabled")).description("Enables spawners detecting zone rendering."))
                  .defaultValue(true))
               .visible(this.spawnersRenderEnabled::get))
            .build()
      );
   private final Setting<ShapeMode> spawnersRenderDetectingZoneShapeMode = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("detecting-zone-shape-mode"))
                     .description("Sets the shape mode for the spawners detecting zone."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.spawnersRenderDetectingZoneEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnersRenderDetectingZoneSideColor = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("detecting-zone-side-color"))
                  .description("Sets the spawners detecting zone side color."))
               .defaultValue(new SettingColor(255, 255, 255, 10))
               .visible(() -> (Boolean)this.spawnersRenderDetectingZoneEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnersRenderDetectingZoneLineColor = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("detecting-zone-line-color"))
                  .description("Sets the spawners detecting zone line color."))
               .defaultValue(new SettingColor(255, 255, 255, 255))
               .visible(() -> (Boolean)this.spawnersRenderDetectingZoneEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<Boolean> spawnersRenderSpawningZoneEnabled = this.sgSpawners
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("spawning-zone-enabled")).description("Enables spawners spawning zone rendering."))
                  .defaultValue(true))
               .visible(this.spawnersRenderEnabled::get))
            .build()
      );
   private final Setting<Boolean> spawnersRenderSpawningZoneRounding = this.sgSpawners
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("spawning-zone-rounding")).description("Round spawning zone size to block size."))
                  .defaultValue(false))
               .visible(() -> (Boolean)this.spawnersRenderSpawningZoneEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<ShapeMode> spawnersRenderSpawningZoneShapeMode = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("spawning-zone-shape-mode"))
                     .description("Sets the shape mode for the spawners spawning zone."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.spawnersRenderSpawningZoneEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnersRenderSpawningZoneSideColor = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("spawning-zone-side-color"))
                  .description("Sets the spawners spawning zone side color."))
               .defaultValue(new SettingColor(255, 0, 0, 10))
               .visible(() -> (Boolean)this.spawnersRenderSpawningZoneEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnersRenderSpawningZoneLineColor = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("spawning-zone-line-color"))
                  .description("Sets the spawners spawning zone line color."))
               .defaultValue(new SettingColor(255, 0, 0, 255))
               .visible(() -> (Boolean)this.spawnersRenderSpawningZoneEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<Boolean> spawnersRenderActiveSpawnerEnabled = this.sgSpawners
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("active-spawner-enabled")).description("Enables spawners active spawner rendering."))
                  .defaultValue(true))
               .visible(this.spawnersRenderEnabled::get))
            .build()
      );
   private final Setting<ShapeMode> spawnersRenderActiveSpawnerShapeMode = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("active-spawner-shape-mode"))
                     .description("Sets the shape mode for the spawners active spawner."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.spawnersRenderActiveSpawnerEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnersRenderActiveSpawnerSideColor = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("active-spawner-side-color"))
                  .description("Sets the spawners active spawner side color."))
               .defaultValue(new SettingColor(0, 255, 0, 10))
               .visible(() -> (Boolean)this.spawnersRenderActiveSpawnerEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnersRenderActiveSpawnerLineColor = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("active-spawner-line-color"))
                  .description("Sets the spawners active spawner line color."))
               .defaultValue(new SettingColor(0, 255, 0, 255))
               .visible(() -> (Boolean)this.spawnersRenderActiveSpawnerEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<Boolean> spawnersRenderInactiveSpawnerEnabled = this.sgSpawners
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("inactive-spawner-enabled")).description("Enables spawners inactive spawner rendering."))
                  .defaultValue(true))
               .visible(this.spawnersRenderEnabled::get))
            .build()
      );
   private final Setting<ShapeMode> spawnersRenderInactiveSpawnerShapeMode = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("inactive-spawner-shape-mode"))
                     .description("Sets the shape mode for the spawners inactive spawner."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.spawnersRenderInactiveSpawnerEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnersRenderInactiveSpawnerSideColor = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("inactive-spawner-side-color"))
                  .description("Sets the spawners inactive spawner side color."))
               .defaultValue(new SettingColor(255, 0, 0, 10))
               .visible(() -> (Boolean)this.spawnersRenderInactiveSpawnerEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> spawnersRenderInactiveSpawnerLineColor = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("inactive-spawner-line-color"))
                  .description("Sets the spawners inactive spawner line color."))
               .defaultValue(new SettingColor(255, 0, 0, 255))
               .visible(() -> (Boolean)this.spawnersRenderInactiveSpawnerEnabled.get() && (Boolean)this.spawnersRenderEnabled.get()))
            .build()
      );
   private final Setting<Boolean> spawnersRenderDebugInfoEnabled = this.sgSpawners
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("render-debug-info-enabled"))
                     .description("Enables debug info rendering for the spawners render."))
                  .defaultValue(true))
               .visible(this.spawnersRenderEnabled::get))
            .build()
      );
   private final Setting<Vector3d> spawnersRenderDebugInfoOffset = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)new meteordevelopment.meteorclient.settings.Vector3dSetting.Builder()
                     .name("render-debug-info-offset"))
                  .description("Sets the spawners render debug info offset."))
               .defaultValue(new Vector3d(0.0, 0.0, 0.0))
               .visible(() -> (Boolean)this.spawnersRenderEnabled.get() && (Boolean)this.spawnersRenderDebugInfoEnabled.get()))
            .build()
      );
   private final Setting<Double> spawnersRenderDebugInfoSize = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("render-debug-info-size"))
                  .description("Sets the spawners render debug info size."))
               .defaultValue(1.0)
               .min(0.0)
               .visible(() -> (Boolean)this.spawnersRenderEnabled.get() && (Boolean)this.spawnersRenderDebugInfoEnabled.get()))
            .build()
      );
   private final Setting<Double> spawnersRenderDebugInfoMargin = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("render-debug-info-margin"))
                  .description("Sets the vertical margin between debug info pairs."))
               .defaultValue(1.0)
               .min(0.0)
               .visible(() -> (Boolean)this.spawnersRenderEnabled.get() && (Boolean)this.spawnersRenderDebugInfoEnabled.get()))
            .build()
      );
   private final Setting<List<String>> spawnersRenderDebugInfoAllowedKeys = this.sgSpawners
      .add(
         ((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                        .name("render-debug-info-allowed-keys"))
                     .description("Sets the spawners render debug info allowed keys."))
                  .defaultValue(List.of("Delay", "RequiredPlayerRange")))
               .visible(() -> (Boolean)this.spawnersRenderEnabled.get() && (Boolean)this.spawnersRenderDebugInfoEnabled.get()))
            .build()
      );
   private final Setting<Boolean> ironGolemsRenderEnabled = this.sgIronGolems
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("enabled")).description("Enables iron golems rendering.")).defaultValue(false))
               .onChanged(value -> this.onActivate()))
            .build()
      );
   private final Setting<Boolean> ironGolemsRenderDetectingZoneEnabled = this.sgIronGolems
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("detecting-zone-enabled")).description("Enables iron golems detecting zone rendering."))
                  .defaultValue(true))
               .visible(this.ironGolemsRenderEnabled::get))
            .build()
      );
   private final Setting<Vector3d> ironGolemsRenderDetectingZoneSize = this.sgIronGolems
      .add(
         ((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)new meteordevelopment.meteorclient.settings.Vector3dSetting.Builder()
                     .name("detecting-zone-size"))
                  .description("Sets the iron golems detecting zone size."))
               .defaultValue(new Vector3d(32.0, 32.0, 32.0))
               .visible(() -> (Boolean)this.ironGolemsRenderDetectingZoneEnabled.get() && (Boolean)this.ironGolemsRenderEnabled.get()))
            .build()
      );
   private final Setting<ShapeMode> ironGolemsRenderDetectingZoneShapeMode = this.sgIronGolems
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("detecting-zone-shape-mode"))
                     .description("Sets the shape mode for the iron golems detecting zone."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.ironGolemsRenderDetectingZoneEnabled.get() && (Boolean)this.ironGolemsRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> ironGolemsRenderDetectingZoneSideColor = this.sgIronGolems
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("detecting-zone-side-color"))
                  .description("Sets the iron golems detecting zone side color."))
               .defaultValue(new SettingColor(255, 255, 255, 10))
               .visible(() -> (Boolean)this.ironGolemsRenderDetectingZoneEnabled.get() && (Boolean)this.ironGolemsRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> ironGolemsRenderDetectingZoneLineColor = this.sgIronGolems
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("detecting-zone-line-color"))
                  .description("Sets the iron golems detecting zone line color."))
               .defaultValue(new SettingColor(255, 255, 255, 255))
               .visible(() -> (Boolean)this.ironGolemsRenderDetectingZoneEnabled.get() && (Boolean)this.ironGolemsRenderEnabled.get()))
            .build()
      );
   private final Setting<Boolean> ironGolemsRenderSpawningZoneEnabled = this.sgIronGolems
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("spawning-zone-enabled")).description("Enables iron golems spawning zone rendering."))
                  .defaultValue(true))
               .visible(this.ironGolemsRenderEnabled::get))
            .build()
      );
   private final Setting<Vector3d> ironGolemsRenderSpawningZoneSize = this.sgIronGolems
      .add(
         ((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)((meteordevelopment.meteorclient.settings.Vector3dSetting.Builder)new meteordevelopment.meteorclient.settings.Vector3dSetting.Builder()
                     .name("spawning-zone-size"))
                  .description("Sets the iron golems spawning zone size."))
               .defaultValue(new Vector3d(17.0, 13.0, 17.0))
               .visible(() -> (Boolean)this.ironGolemsRenderSpawningZoneEnabled.get() && (Boolean)this.ironGolemsRenderEnabled.get()))
            .build()
      );
   private final Setting<ShapeMode> ironGolemsRenderSpawningZoneShapeMode = this.sgIronGolems
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("spawning-zone-shape-mode"))
                     .description("Sets the shape mode for the iron golems spawning zone."))
                  .defaultValue(ShapeMode.Both))
               .visible(() -> (Boolean)this.ironGolemsRenderSpawningZoneEnabled.get() && (Boolean)this.ironGolemsRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> ironGolemsRenderSpawningZoneSideColor = this.sgIronGolems
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("spawning-zone-side-color"))
                  .description("Sets the iron golems spawning zone side color."))
               .defaultValue(new SettingColor(0, 255, 0, 10))
               .visible(() -> (Boolean)this.ironGolemsRenderSpawningZoneEnabled.get() && (Boolean)this.ironGolemsRenderEnabled.get()))
            .build()
      );
   private final Setting<SettingColor> ironGolemsRenderSpawningZoneLineColor = this.sgIronGolems
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("spawning-zone-line-color"))
                  .description("Sets the iron golems spawning zone line color."))
               .defaultValue(new SettingColor(0, 255, 0, 255))
               .visible(() -> (Boolean)this.ironGolemsRenderSpawningZoneEnabled.get() && (Boolean)this.ironGolemsRenderEnabled.get()))
            .build()
      );
   private static final List<NerdVision.NbtDataDisplay> nbtDataDisplays = new ArrayList<>();
   private final ExecutorService workerThread = Executors.newSingleThreadExecutor();
   private final Set<Block> blocksToTrack = new HashSet<>();
   private final Set<BlockPos> trackedBlocks = new HashSet<>();
   private DimensionType lastDimension;

   public NerdVision() {
      super(Meteorist.CATEGORY, "nerd-vision", "Allows you to visualize various game mechanics/farms.");
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if (!nbtDataDisplays.isEmpty()) {
         for (NerdVision.NbtDataDisplay display : nbtDataDisplays) {
            if (NametagUtils.to2D(display.pos, display.size)) {
               NametagUtils.begin(display.pos);
               TextRenderer text = TextRenderer.get();
               text.begin(display.size);
               String longestPair = "";
               String separator = ": ";

               for (Entry<String, String> entry : display.attributes.entrySet()) {
                  String pair = entry.getKey() + separator + entry.getValue();
                  if (text.getWidth(pair) > text.getWidth(longestPair)) {
                     longestPair = pair;
                  }
               }

               double startX = -text.getWidth(longestPair) / 2.0;
               double totalHeight = 0.0;

               for (Entry<String, String> ignored : display.attributes.entrySet()) {
                  totalHeight += text.getHeight();
               }

               totalHeight += (display.attributes.size() - 1) * display.margin;
               double yOffset = -totalHeight / 2.0;

               for (Entry<String, String> entryx : display.attributes.entrySet()) {
                  String key = entryx.getKey();
                  String value = entryx.getValue();
                  text.render(key + separator, startX, yOffset, Color.GREEN, true);
                  text.render(value, startX + text.getWidth(key + separator), yOffset, Color.CYAN, true);
                  yOffset += text.getHeight() + display.margin;
               }

               text.end();
               NametagUtils.end();
            }
         }
      }
   }

   @EventHandler
   private void onRender3D(Render3DEvent event) {
      nbtDataDisplays.clear();
      this.renderEntities(event);
      this.renderTrackedBlocks(event);
      this.renderSpawnRanges(event);
   }

   private void renderEntities(Render3DEvent event) {
      for (Entity entity : this.mc.level.entitiesForRendering()) {
         if ((Boolean)this.turtleEggsRenderEnabled.get() && entity instanceof Zombie zombie) {
            this.renderZombieForTurtleEggs(event, zombie);
         }

         if ((Boolean)this.ironGolemsRenderEnabled.get() && entity instanceof Villager villager) {
            this.renderVillagerForIronGolems(event, villager);
         }
      }
   }

   private void renderZombieForTurtleEggs(Render3DEvent event, Zombie zombie) {
      if ((Boolean)this.turtleEggsZombiesRangeEnabled.get()) {
         Vector3d size = (Vector3d)this.turtleEggsRangeSize.get();
         event.renderer
            .box(
               AABB.ofSize(zombie.position(), size.x, size.y, size.z),
               (Color)this.turtleEggsZombiesRangeSideColor.get(),
               (Color)this.turtleEggsZombiesRangeLineColor.get(),
               (ShapeMode)this.turtleEggsZombiesRangeShapeMode.get(),
               0
            );
      }
   }

   private void renderVillagerForIronGolems(Render3DEvent event, Villager villager) {
      if ((Boolean)this.ironGolemsRenderDetectingZoneEnabled.get()) {
         Vector3d size = (Vector3d)this.ironGolemsRenderDetectingZoneSize.get();
         AABB box = AABB.ofSize(villager.position(), size.x, size.y, size.z);
         event.renderer
            .box(
               box,
               (Color)this.ironGolemsRenderDetectingZoneSideColor.get(),
               (Color)this.ironGolemsRenderDetectingZoneLineColor.get(),
               (ShapeMode)this.ironGolemsRenderDetectingZoneShapeMode.get(),
               0
            );
      }

      if ((Boolean)this.ironGolemsRenderSpawningZoneEnabled.get()) {
         Vector3d size = (Vector3d)this.ironGolemsRenderSpawningZoneSize.get();
         AABB box = AABB.ofSize(villager.position(), size.x, size.y, size.z);
         event.renderer
            .box(
               box,
               (Color)this.ironGolemsRenderSpawningZoneSideColor.get(),
               (Color)this.ironGolemsRenderSpawningZoneLineColor.get(),
               (ShapeMode)this.ironGolemsRenderSpawningZoneShapeMode.get(),
               0
            );
      }
   }

   private void renderTrackedBlocks(Render3DEvent event) {
      for (BlockPos blockPos : this.trackedBlocks) {
         Block block = this.mc.level.getBlockState(blockPos).getBlock();
         Vec3 centerPos = blockPos.getCenter();
         if (block == Blocks.TURTLE_EGG && (Boolean)this.turtleEggsRenderEnabled.get()) {
            this.renderTurtleEgg(event, blockPos, centerPos);
         } else if (block == Blocks.SPAWNER && (Boolean)this.spawnersRenderEnabled.get()) {
            this.renderSpawner(event, blockPos, centerPos);
         }
      }
   }

   private void renderTurtleEgg(Render3DEvent event, BlockPos blockPos, Vec3 centerPos) {
      if ((Boolean)this.turtleEggsRangeEnabled.get()) {
         Vector3d size = (Vector3d)this.turtleEggsRangeSize.get();
         AABB box = AABB.ofSize(centerPos, size.x, size.y, size.z);
         event.renderer
            .box(box, (Color)this.turtleEggsRangeSideColor.get(), (Color)this.turtleEggsRangeLineColor.get(), (ShapeMode)this.turtleEggsRangeShapeMode.get(), 0);
      }

      if ((Boolean)this.turtleEggsAggressionRenderEnabled.get()) {
         Vector3d size = (Vector3d)this.turtleEggsRangeSize.get();
         AABB dangerBox = AABB.ofSize(centerPos, size.x, size.y, size.z);
         boolean isInDanger = false;

         for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (entity instanceof Zombie zombie) {
               AABB boundingBox = entity.getBoundingBox();
               if (dangerBox.intersects(boundingBox)) {
                  isInDanger = true;
                  if ((Boolean)this.turtleEggsAggressionZombieRenderEnabled.get()) {
                     event.renderer
                        .box(
                           boundingBox,
                           (Color)this.turtleEggsAggressionZombieRenderSideColor.get(),
                           (Color)this.turtleEggsAggressionZombieRenderLineColor.get(),
                           (ShapeMode)this.turtleEggsAggressionZombieRenderShapeMode.get(),
                           0
                        );
                  }
               }
            }
         }

         if (isInDanger && (Boolean)this.turtleEggsAggressionEggRenderEnabled.get()) {
            event.renderer
               .box(
                  blockPos,
                  (Color)this.turtleEggsAggressionEggRenderSideColor.get(),
                  (Color)this.turtleEggsAggressionEggRenderLineColor.get(),
                  (ShapeMode)this.turtleEggsAggressionEggRenderShapeMode.get(),
                  0
               );
         }
      }
   }

   private void renderSpawner(Render3DEvent event, BlockPos blockPos, Vec3 centerPos) {
      if (this.mc.level.getBlockEntity(blockPos) instanceof SpawnerBlockEntity mobSpawner) {
         BaseSpawner spawnerLogic = mobSpawner.getSpawner();
         int spawnRange = ((MobSpawnerLogicAccessor)spawnerLogic).getSpawnRange();
         if ((Boolean)this.spawnersRenderSphereEnabled.get()) {
            int requiredPlayerRange = ((MobSpawnerLogicAccessor)spawnerLogic).getRequiredPlayerRange();
            if (this.spawnersRenderSphereType.get() == NerdVision.SphereType.Default) {
               this.renderQuadSphere(
                  event,
                  centerPos,
                  requiredPlayerRange,
                  (Integer)this.spawnersRenderSpherePhiCount.get(),
                  (Integer)this.spawnersRenderSphereThetaCount.get(),
                  (SettingColor)this.spawnersRenderSphereSideColor.get(),
                  (SettingColor)this.spawnersRenderSphereLineColor.get(),
                  (ShapeMode)this.spawnersRenderSphereShapeMode.get()
               );
            } else {
               this.renderCubicSphere(
                  event,
                  centerPos,
                  requiredPlayerRange,
                  (SettingColor)this.spawnersRenderSphereSideColor.get(),
                  (SettingColor)this.spawnersRenderSphereLineColor.get(),
                  (ShapeMode)this.spawnersRenderSphereShapeMode.get()
               );
            }
         }

         if ((Boolean)this.spawnersRenderDetectingZoneEnabled.get()) {
            AABB box = new AABB(blockPos).inflate(spawnRange);
            event.renderer
               .box(
                  box,
                  (Color)this.spawnersRenderDetectingZoneSideColor.get(),
                  (Color)this.spawnersRenderDetectingZoneLineColor.get(),
                  (ShapeMode)this.spawnersRenderDetectingZoneShapeMode.get(),
                  0
               );
         }

         if ((Boolean)this.spawnersRenderSpawningZoneEnabled.get()) {
            Entity entity = spawnerLogic.getOrCreateDisplayEntity(this.mc.level, blockPos);
            Vec3 size = new Vec3(spawnRange, 1.0, spawnRange).scale(2.0);
            if (entity != null) {
               AABB spawnBox = entity.getBoundingBox();
               size = size.add(spawnBox.getXsize(), spawnBox.getYsize(), spawnBox.getZsize());
            }

            Vec3 bottomCenterPos = blockPos.getBottomCenter();
            if ((Boolean)this.spawnersRenderSpawningZoneRounding.get()) {
               size = new Vec3(Math.ceil(size.x), Math.ceil(size.y), Math.ceil(size.z));
               if (size.x % 2.0 == 0.0) {
                  size = size.add(1.0, 0.0, 0.0);
               }

               if (size.z % 2.0 == 0.0) {
                  size = size.add(0.0, 0.0, 1.0);
               }
            }

            Vec3 pos = bottomCenterPos.add(0.0, -1.0 + size.y / 2.0, 0.0);
            AABB box = AABB.ofSize(pos, size.x, size.y, size.z);
            event.renderer
               .box(
                  box,
                  (Color)this.spawnersRenderSpawningZoneSideColor.get(),
                  (Color)this.spawnersRenderSpawningZoneLineColor.get(),
                  (ShapeMode)this.spawnersRenderSpawningZoneShapeMode.get(),
                  0
               );
         }

         boolean isPlayerInRange = ((MobSpawnerLogicAccessor)spawnerLogic).invokeIsPlayerInRange(this.mc.level, blockPos);
         if (isPlayerInRange && (Boolean)this.spawnersRenderActiveSpawnerEnabled.get()) {
            event.renderer
               .box(
                  blockPos,
                  (Color)this.spawnersRenderActiveSpawnerSideColor.get(),
                  (Color)this.spawnersRenderActiveSpawnerLineColor.get(),
                  (ShapeMode)this.spawnersRenderActiveSpawnerShapeMode.get(),
                  0
               );
         } else if (!isPlayerInRange && (Boolean)this.spawnersRenderInactiveSpawnerEnabled.get()) {
            event.renderer
               .box(
                  blockPos,
                  (Color)this.spawnersRenderInactiveSpawnerSideColor.get(),
                  (Color)this.spawnersRenderInactiveSpawnerLineColor.get(),
                  (ShapeMode)this.spawnersRenderInactiveSpawnerShapeMode.get(),
                  0
               );
         }

         if ((Boolean)this.spawnersRenderDebugInfoEnabled.get()) {
            Vector3d debugInfoOffset = (Vector3d)this.spawnersRenderDebugInfoOffset.get();
            Vector3d debugInfoPos = new Vector3d(centerPos.x + debugInfoOffset.x, centerPos.y + debugInfoOffset.y, centerPos.z + debugInfoOffset.z);
            Map<String, String> debugInfo = new HashMap<>();
            CompoundTag nbt = mobSpawner.saveWithFullMetadata(this.mc.level.registryAccess());

            for (String key : (List)this.spawnersRenderDebugInfoAllowedKeys.get()) {
               if (nbt.contains(key)) {
                  debugInfo.put(key, String.valueOf(nbt.get(key).asString()));
               }
            }

            nbtDataDisplays.add(
               new NerdVision.NbtDataDisplay(
                  (Double)this.spawnersRenderDebugInfoSize.get(), (Double)this.spawnersRenderDebugInfoMargin.get(), debugInfoPos, debugInfo
               )
            );
         }
      }
   }

   private void renderSpawnRanges(Render3DEvent event) {
      if ((Boolean)this.spawnRangesEnabled.get()) {
         Vec3 pos = this.getCenterPosition();
         if ((Boolean)this.despawnRangeEnabled.get()) {
            this.renderDespawnRange(event, pos);
         }

         if ((Boolean)this.idleRangeEnabled.get()) {
            this.renderIdleRange(event, pos);
         }

         if ((Boolean)this.spawnRangeEnabled.get()) {
            this.renderSpawnRange(event, pos);
         }

         if ((Boolean)this.displayCenterPosition.get()) {
            this.renderCenterPosition(event, pos);
         }
      }
   }

   private Vec3 getCenterPosition() {
      if (this.centerPositionType.get() == NerdVision.CenterPositionType.Pos) {
         Vector3d centerPos = (Vector3d)this.centerPosition.get();
         Vec3 pos = new Vec3(centerPos.x, centerPos.y, centerPos.z);
         return this.centerPositionMode.get() == NerdVision.CenterPositionMode.Relative ? pos.add(this.mc.player.position()) : pos.add(0.5, 1.0, 0.5);
      } else {
         Vec3 pos = ((BlockPos)this.centerBlockPosition.get()).getBottomCenter();
         return this.centerPositionMode.get() == NerdVision.CenterPositionMode.Relative
            ? pos.add(Vec3.atLowerCornerOf(this.mc.player.blockPosition()))
            : pos.add(0.0, 1.0, 0.0);
      }
   }

   private void renderDespawnRange(Render3DEvent event, Vec3 pos) {
      if (this.spawnRangesSphereType.get() == NerdVision.SphereType.Default) {
         this.renderQuadSphere(
            event,
            pos,
            (Double)this.despawnSphereRange.get(),
            (Integer)this.despawnRangeSpherePhiCount.get(),
            (Integer)this.despawnRangeSphereThetaCount.get(),
            (SettingColor)this.despawnRangeSideColor.get(),
            (SettingColor)this.despawnRangeLineColor.get(),
            (ShapeMode)this.despawnRangeShapeMode.get()
         );
      } else {
         this.renderCubicSphere(
            event,
            pos,
            (Integer)this.despawnBlockSphereRange.get(),
            (SettingColor)this.despawnRangeSideColor.get(),
            (SettingColor)this.despawnRangeLineColor.get(),
            (ShapeMode)this.despawnRangeShapeMode.get()
         );
      }
   }

   private void renderIdleRange(Render3DEvent event, Vec3 pos) {
      if (this.spawnRangesSphereType.get() == NerdVision.SphereType.Default) {
         this.renderQuadSphere(
            event,
            pos,
            (Double)this.idleSphereRange.get(),
            (Integer)this.idleRangeSpherePhiCount.get(),
            (Integer)this.idleRangeSphereThetaCount.get(),
            (SettingColor)this.idleRangeSideColor.get(),
            (SettingColor)this.idleRangeLineColor.get(),
            (ShapeMode)this.idleRangeShapeMode.get()
         );
      } else {
         this.renderCubicSphere(
            event,
            pos,
            (Integer)this.idleBlockSphereRange.get(),
            (SettingColor)this.idleRangeSideColor.get(),
            (SettingColor)this.idleRangeLineColor.get(),
            (ShapeMode)this.idleRangeShapeMode.get()
         );
      }
   }

   private void renderSpawnRange(Render3DEvent event, Vec3 pos) {
      if (this.spawnRangesSphereType.get() == NerdVision.SphereType.Default) {
         this.renderQuadSphere(
            event,
            pos,
            (Double)this.spawnSphereRange.get(),
            (Integer)this.spawnRangeSpherePhiCount.get(),
            (Integer)this.spawnRangeSphereThetaCount.get(),
            (SettingColor)this.spawnRangeSideColor.get(),
            (SettingColor)this.spawnRangeLineColor.get(),
            (ShapeMode)this.spawnRangeShapeMode.get()
         );
      } else {
         this.renderCubicSphere(
            event,
            pos,
            (Integer)this.spawnBlockSphereRange.get(),
            (SettingColor)this.spawnRangeSideColor.get(),
            (SettingColor)this.spawnRangeLineColor.get(),
            (ShapeMode)this.spawnRangeShapeMode.get()
         );
      }
   }

   private void renderCenterPosition(Render3DEvent event, Vec3 pos) {
      Vector3d size = (Vector3d)this.centerPositionSize.get();
      event.renderer
         .box(
            AABB.ofSize(pos, size.x, size.y, size.z),
            (Color)this.centerPositionSideColor.get(),
            (Color)this.centerPositionLineColor.get(),
            (ShapeMode)this.centerPositionShapeMode.get(),
            0
         );
   }

   private void renderCubicSphere(Render3DEvent event, Vec3 center, int radius, SettingColor sideColor, SettingColor lineColor, ShapeMode shapeMode) {
      for (BlockPos blockPos : this.getSphereBlocks(BlockPos.containing(center), radius)) {
         event.renderer.box(blockPos, sideColor, lineColor, shapeMode, 0);
      }
   }

   private Set<BlockPos> getSphereBlocks(BlockPos center, int radius) {
      Set<BlockPos> blocks = new HashSet<>();
      double radiusDouble = radius + 0.5;
      double invRadius = 1.0 / radiusDouble;
      int ceilRadius = (int)Math.ceil(radiusDouble);

      for (int x = 0; x <= ceilRadius; x++) {
         for (int y = 0; y <= ceilRadius; y++) {
            for (int z = 0; z <= ceilRadius; z++) {
               double xn = x * invRadius;
               double yn = y * invRadius;
               double zn = z * invRadius;
               double distanceSq = xn * xn + yn * yn + zn * zn;
               if (distanceSq > 1.0) {
                  break;
               }

               double nextXSq = (x + 1) * invRadius * (x + 1) * invRadius;
               double nextYSq = (y + 1) * invRadius * (y + 1) * invRadius;
               double nextZSq = (z + 1) * invRadius * (z + 1) * invRadius;
               if (!(nextXSq + yn * yn + zn * zn <= 1.0) || !(xn * xn + nextYSq + zn * zn <= 1.0) || !(xn * xn + yn * yn + nextZSq <= 1.0)) {
                  blocks.add(center.offset(x, y, z));
                  blocks.add(center.offset(-x, y, z));
                  blocks.add(center.offset(x, -y, z));
                  blocks.add(center.offset(x, y, -z));
                  blocks.add(center.offset(-x, -y, z));
                  blocks.add(center.offset(x, -y, -z));
                  blocks.add(center.offset(-x, y, -z));
                  blocks.add(center.offset(-x, -y, -z));
               }
            }
         }
      }

      return blocks;
   }

   private void renderQuadSphere(
      Render3DEvent event, Vec3 center, double radius, int phiCount, int thetaCount, SettingColor sideColor, SettingColor lineColor, ShapeMode shapeMode
   ) {
      double phiStep = Math.PI / phiCount;
      double thetaStep = (Math.PI * 2) / thetaCount;

      for (int i = 0; i < phiCount; i++) {
         double phi = i * phiStep;
         double phiNext = (i + 1) * phiStep;

         for (int j = 0; j < thetaCount; j++) {
            double theta = j * thetaStep;
            double thetaNext = (j + 1) * thetaStep;
            double y1 = center.y + radius * Math.cos(phi);
            double x1 = center.x + radius * Math.sin(phi) * Math.cos(theta);
            double z1 = center.z + radius * Math.sin(phi) * Math.sin(theta);
            double x2 = center.x + radius * Math.sin(phi) * Math.cos(thetaNext);
            double z2 = center.z + radius * Math.sin(phi) * Math.sin(thetaNext);
            double y2 = center.y + radius * Math.cos(phiNext);
            double x3 = center.x + radius * Math.sin(phiNext) * Math.cos(thetaNext);
            double z3 = center.z + radius * Math.sin(phiNext) * Math.sin(thetaNext);
            double x4 = center.x + radius * Math.sin(phiNext) * Math.cos(theta);
            double z4 = center.z + radius * Math.sin(phiNext) * Math.sin(theta);
            if (shapeMode.sides()) {
               event.renderer.quad(x1, y1, z1, x2, y1, z2, x3, y2, z3, x4, y2, z4, sideColor);
            }

            if (shapeMode.lines()) {
               event.renderer.line(x1, y1, z1, x2, y1, z2, lineColor);
               event.renderer.line(x2, y1, z2, x3, y2, z3, lineColor);
            }
         }
      }
   }

   public void onActivate() {
      this.blocksToTrack.clear();
      if ((Boolean)this.turtleEggsRenderEnabled.get()) {
         this.blocksToTrack.add(Blocks.TURTLE_EGG);
      }

      if ((Boolean)this.spawnersRenderEnabled.get()) {
         this.blocksToTrack.add(Blocks.SPAWNER);
      }

      if (!this.blocksToTrack.isEmpty() && this.mc.level != null) {
         this.trackedBlocks.clear();

         for (ChunkAccess chunk : Utils.chunks()) {
            this.searchChunk(chunk);
         }

         this.lastDimension = this.mc.level.dimensionType();
      }
   }

   @EventHandler
   private void onBlockUpdate(BlockUpdateEvent event) {
      if (!this.blocksToTrack.isEmpty()) {
         Block newBlock = event.newState.getBlock();
         Block oldBlock = event.oldState.getBlock();
         if (this.blocksToTrack.contains(newBlock)) {
            this.trackedBlocks.add(event.pos);
         } else if (this.blocksToTrack.contains(oldBlock)) {
            this.trackedBlocks.remove(event.pos);
         }
      }
   }

   @EventHandler
   private void onPostTick(Post event) {
      if (!this.blocksToTrack.isEmpty() && this.mc.level != null) {
         DimensionType dimension = this.mc.level.dimensionType();
         if (this.lastDimension != dimension) {
            this.lastDimension = dimension;
            this.onActivate();
         }
      }
   }

   @EventHandler
   private void onChunkData(ChunkDataEvent event) {
      if (!this.blocksToTrack.isEmpty()) {
         this.searchChunk(event.chunk());
      }
   }

   private void searchChunk(ChunkAccess chunk) {
      this.workerThread.submit(() -> {
         ChunkPos chunkPos = chunk.getPos();
         int bottomY = this.mc.level.getMinY();
         int topY = this.mc.level.getHeight(Types.WORLD_SURFACE, 0, 0);

         for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
               for (int y = bottomY; y < topY; y++) {
                  BlockPos pos = new BlockPos(x, y, z);
                  Block block = chunk.getBlockState(pos).getBlock();
                  if (this.blocksToTrack.contains(block)) {
                     this.trackedBlocks.add(pos);
                  }
               }
            }
         }
      });
   }

   @EventHandler
   private void onReceivePacket(Receive event) {
      if (!this.blocksToTrack.isEmpty() && event.packet instanceof ClientboundForgetLevelChunkPacket startX) {
         ClientboundForgetLevelChunkPacket var10000 = startX;

         try {
            var12 = var10000.pos();
         } catch (Throwable var8) {
            throw new MatchException(var8.toString(), var8);
         }

         ChunkPos endX = var12;
         int var9 = endX.getMinBlockX();
         int var10 = endX.getMinBlockZ();
         int var11 = endX.getMaxBlockX();
         int endZ = endX.getMaxBlockZ();
         this.trackedBlocks.removeIf(blockPos -> blockPos.getX() >= var9 && blockPos.getX() <= var11 && blockPos.getZ() >= var10 && blockPos.getZ() <= endZ);
      }
   }

   private static enum CenterPositionMode {
      Relative,
      Absolute;
   }

   private static enum CenterPositionType {
      BlockPos,
      Pos;
   }

   public record NbtDataDisplay(Double size, Double margin, Vector3d pos, Map<String, String> attributes) {
   }

   private static enum SphereType {
      Default,
      Cubic;
   }
}
