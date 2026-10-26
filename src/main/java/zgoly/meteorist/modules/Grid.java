package zgoly.meteorist.modules;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import zgoly.meteorist.Meteorist;

public class Grid extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRange = this.settings.createGroup("Range");
   private final SettingGroup sgGrid = this.settings.createGroup("Grid");
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<Grid.FilterMode> filterMode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("filter-mode")).description("Filter placements based on mode."))
               .defaultValue(Grid.FilterMode.StaggeredOdd))
            .build()
      );
   private final Setting<List<Block>> blocks = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                  .name("blocks"))
               .description("Blocks to use for placements."))
            .defaultValue(new Block[]{Blocks.TORCH, Blocks.WALL_TORCH})
            .build()
      );
   private final Setting<Boolean> place = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("place"))
                  .description("Place blocks."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("rotate"))
                     .description("Rotate head when placing a block."))
                  .defaultValue(true))
               .visible(this.place::get))
            .build()
      );
   private final Setting<Boolean> dynamicHeight = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("dynamic-height"))
                  .description("Attaches placements to the surface."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Integer> gridSize = this.sgGrid
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("grid-size"))
                  .description("Grid size."))
               .defaultValue(2))
            .min(1)
            .build()
      );
   private final Setting<Integer> gridGap = this.sgGrid
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("grid-gap"))
                  .description("Gap between placements."))
               .defaultValue(13))
            .min(1)
            .build()
      );
   private final Setting<Integer> switchRange = this.sgRange
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("switch-range"))
                  .description("Range to switch to the nearest placement of grid."))
               .defaultValue(3))
            .min(1)
            .build()
      );
   private final Setting<Integer> placeRange = this.sgRange
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("place-range"))
                  .description("Range to place blocks."))
               .defaultValue(3))
            .min(1)
            .build()
      );
   private final Setting<Boolean> show = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("show"))
                  .description("Show placements."))
               .defaultValue(true))
            .build()
      );
   private final Setting<SettingColor> sideColor1 = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("main-placement-side-color"))
               .description("The color of the sides of the blocks being rendered."))
            .defaultValue(new SettingColor(0, 255, 0, 40))
            .build()
      );
   private final Setting<SettingColor> lineColor1 = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("main-placement-line-color"))
               .description("The color of the lines of the blocks being rendered."))
            .defaultValue(new SettingColor(0, 255, 0, 100))
            .build()
      );
   private final Setting<SettingColor> sideColor2 = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("placement-side-color"))
               .description("The color of the sides of the blocks being rendered."))
            .defaultValue(new SettingColor(255, 255, 0, 40))
            .build()
      );
   private final Setting<SettingColor> lineColor2 = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("placement-line-color"))
               .description("The color of the lines of the blocks being rendered."))
            .defaultValue(new SettingColor(255, 255, 0, 100))
            .build()
      );
   BlockPos centerPos = null;
   List<BlockPos> placements = new ArrayList<>();

   public Grid() {
      super(Meteorist.CATEGORY, "grid", "Allows you to place blocks on a grid.");
   }

   public void onDeactivate() {
      this.centerPos = null;
      this.placements.clear();
   }

   private BlockPos findNearest() {
      for (BlockPos blockPos : BlockPos.withinManhattan(
         this.mc.player.blockPosition(), (Integer)this.switchRange.get(), (Integer)this.switchRange.get(), (Integer)this.switchRange.get()
      )) {
         if (((List)this.blocks.get()).contains(this.mc.level.getBlockState(blockPos).getBlock())) {
            return blockPos;
         }
      }

      return null;
   }

   private BlockPos getHeight(BlockPos blockPos) {
      while (
         !this.mc.level.getBlockState(blockPos).canBeReplaced()
            && blockPos.getY() < this.mc.level.getHeight(Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ())
      ) {
         blockPos = blockPos.above(1);
      }

      while (this.mc.level.getBlockState(blockPos.below(1)).canBeReplaced() && blockPos.getY() > this.mc.level.getMinY()) {
         blockPos = blockPos.below(1);
      }

      return blockPos;
   }

   @EventHandler
   private void onTick(Pre event) {
      BlockPos nearPos = this.findNearest();
      if (nearPos != null) {
         this.centerPos = nearPos;
      }

      if (this.centerPos != null) {
         this.placements.clear();

         for (int x = -(Integer)this.gridSize.get(); x <= this.gridSize.get(); x++) {
            for (int z = -(Integer)this.gridSize.get(); z <= this.gridSize.get(); z++) {
               if ((x != 0 || z != 0)
                  && (this.filterMode.get() != Grid.FilterMode.StaggeredEven || (x & 1) != (z & 1))
                  && (this.filterMode.get() != Grid.FilterMode.StaggeredOdd || (x & 1) == (z & 1))) {
                  BlockPos finalBlockPos = this.centerPos.offset(x * (Integer)this.gridGap.get(), 0, z * (Integer)this.gridGap.get());
                  if ((Boolean)this.dynamicHeight.get()) {
                     finalBlockPos = this.getHeight(finalBlockPos);
                  }

                  this.placements.add(finalBlockPos);
               }
            }
         }

         for (BlockPos finalBlockPos : this.placements) {
            if ((Boolean)this.place.get()
               && this.mc.level.getBlockState(finalBlockPos).canBeReplaced()
               && finalBlockPos.closerThan(this.mc.player.blockPosition(), ((Integer)this.placeRange.get()).intValue())) {
               for (Block block : (List)this.blocks.get()) {
                  FindItemResult item = InvUtils.findInHotbar(new Item[]{block.asItem()});
                  BlockUtils.place(finalBlockPos, item, (Boolean)this.rotate.get(), 0);
               }
            }
         }
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.centerPos != null && (Boolean)this.show.get()) {
         event.renderer.box(this.centerPos, (Color)this.sideColor1.get(), (Color)this.lineColor1.get(), ShapeMode.Both, 0);

         for (BlockPos placement : this.placements) {
            event.renderer.box(placement, (Color)this.sideColor2.get(), (Color)this.lineColor2.get(), ShapeMode.Both, 0);
         }
      }
   }

   public static enum FilterMode {
      None,
      StaggeredEven,
      StaggeredOdd;
   }
}
