package com.kkllffaa.meteor_litematica_printer;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class Shredder extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgWorkMode = this.settings.createGroup("Work Mode");
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<Double> range = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("range")).description("Nuke range.")).defaultValue(5.0).min(1.0).sliderMin(1.0).max(20.0).build());
   private final Setting<Double> wallsRange = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("walls-range")).description("Range through walls.")).defaultValue(6.0).min(0.0).sliderMin(0.0).max(6.0).build()
      );
   private final Setting<Double> delay = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("delay")).description("Delay between breaks."))
            .defaultValue(0.0)
            .min(0.0)
            .sliderMin(0.0)
            .max(100.0)
            .sliderMax(20.0)
            .build()
      );
   private final Setting<Integer> bpt = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("blocks-per-tick"))
                  .description("Max blocks per tick."))
               .defaultValue(100))
            .min(1)
            .sliderMin(1)
            .max(100)
            .build()
      );
   private final Setting<Shredder.Mode> mode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("mode"))
                  .description("What to nuke."))
               .defaultValue(Shredder.Mode.ALL))
            .build()
      );
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate"))
                  .description("Rotate to target block."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> swing = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("swing"))
                  .description("Swing hand."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> silkTouch = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("silk-touch"))
                  .description("Prefer Silk Touch tool."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Shredder.FilterMode> listMode = this.sgWorkMode
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("list-mode"))
                  .description("Block list mode."))
               .defaultValue(Shredder.FilterMode.NONE))
            .build()
      );
   private final Setting<List<Block>> filterBlocks = this.sgWorkMode
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                     .name("filter-blocks"))
                  .description("Blocks to whitelist or blacklist."))
               .visible(() -> this.listMode.get() != Shredder.FilterMode.NONE))
            .build()
      );
   private final Setting<Boolean> showBroken = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("broken-blocks"))
                  .description("Show recently broken blocks."))
               .defaultValue(true))
            .build()
      );
   private final Setting<ShapeMode> nukerBlockMode = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("nuker-block-mode"))
                     .description("How broken blocks are rendered."))
                  .defaultValue(ShapeMode.Both))
               .visible(this.showBroken::get))
            .build()
      );
   private final Setting<SettingColor> nukerBlockSideColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("block-side-color"))
                  .description("Broken block side color."))
               .defaultValue(new SettingColor(255, 0, 0, 80))
               .visible(this.showBroken::get))
            .build()
      );
   private final Setting<SettingColor> nukerBlockLineColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("block-line-color"))
                  .description("Broken block line color."))
               .defaultValue(new SettingColor(255, 0, 0, 255))
               .visible(this.showBroken::get))
            .build()
      );
   private int timer;
   private final List<BlockPos> toBreak = new ArrayList<>();
   private final List<BlockPos> brokenBlocks = new ArrayList<>();
   private int brokenBlockLife;

   public Shredder() {
      super(Addon.CATEGORY, "shredder", "Breaks blocks based on Litematica schematic.");
   }

   public void onActivate() {
      this.timer = 0;
      this.toBreak.clear();
      this.brokenBlocks.clear();
      this.brokenBlockLife = 0;
   }

   public void onDeactivate() {
      this.toBreak.clear();
      this.brokenBlocks.clear();
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null && this.mc.level != null && this.mc.gameMode != null) {
         WorldSchematic ws = SchematicWorldHandler.getSchematicWorld();
         if (ws == null) {
            this.toggle();
         } else {
            if (this.brokenBlockLife > 0) {
               this.brokenBlockLife--;
            }

            if (this.brokenBlockLife <= 0) {
               this.brokenBlocks.clear();
            }

            if (this.timer < (Double)this.delay.get()) {
               this.timer++;
            } else {
               if ((Boolean)this.silkTouch.get()) {
                  this.equipSilkTool();
               }

               this.toBreak.clear();
               Shredder.Mode m = (Shredder.Mode)this.mode.get();
               double r = (Double)this.range.get();
               double wr = (Double)this.wallsRange.get();
               BlockIterator.register((int)Math.ceil(r + 1.0), (int)Math.ceil(r + 1.0), (pos, worldState) -> {
                  if (!worldState.isAir()) {
                     BlockState schemState = ws.getBlockState(pos);
                     if (!schemState.isAir() || this.isInsideAnySchematic(pos)) {
                        if (this.shouldBreak(m, schemState, worldState)) {
                           if (!this.mc.player.getBoundingBox().intersects(Vec3.atLowerCornerOf(pos), Vec3.atLowerCornerOf(pos).add(1.0, 1.0, 1.0))) {
                              Shredder.FilterMode fm = (Shredder.FilterMode)this.listMode.get();
                              if (fm != Shredder.FilterMode.NONE) {
                                 boolean inList = ((List)this.filterBlocks.get()).contains(worldState.getBlock());
                                 if (fm == Shredder.FilterMode.BLACKLIST ? inList : !inList) {
                                    return;
                                 }
                              }

                              double dist = this.mc.player.getEyePosition().distanceTo(pos.getCenter());
                              if (this.isBlockVisible(pos) || !(dist > wr)) {
                                 this.toBreak.add(new BlockPos(pos));
                              }
                           }
                        }
                     }
                  }
               });
               BlockIterator.after(() -> {
                  int broken = 0;

                  for (BlockPos pos : this.toBreak) {
                     boolean canInsta = this.mc.player.isCreative() || BlockUtils.canInstaBreak(pos);
                     if ((Boolean)this.rotate.get()) {
                        Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), () -> BlockUtils.breakBlock(pos, (Boolean)this.swing.get()));
                     } else {
                        BlockUtils.breakBlock(pos, (Boolean)this.swing.get());
                     }

                     if ((Boolean)this.showBroken.get()) {
                        this.brokenBlocks.add(new BlockPos(pos));
                        this.brokenBlockLife = 20;
                     }

                     if (!canInsta || ++broken >= (Integer)this.bpt.get()) {
                        break;
                     }
                  }
               });
               this.timer = 0;
            }
         }
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if ((Boolean)this.showBroken.get()) {
         for (BlockPos pos : this.brokenBlocks) {
            event.renderer.box(pos, (Color)this.nukerBlockSideColor.get(), (Color)this.nukerBlockLineColor.get(), (ShapeMode)this.nukerBlockMode.get(), 0);
         }
      }
   }

   private boolean isBlockVisible(BlockPos pos) {
      Vec3 eyePos = this.mc.player.getEyePosition();
      Vec3 blockCenter = pos.getCenter();
      return this.mc
         .level
         .clip(new ClipContext(eyePos, blockCenter, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, this.mc.player))
         .getBlockPos()
         .equals(pos);
   }

   private void equipSilkTool() {
      if (Utils.getEnchantmentLevel(this.mc.player.getMainHandItem(), Enchantments.SILK_TOUCH) <= 0) {
         int bestSlot = -1;

         for (int i = 0; i < 9; i++) {
            if (Utils.getEnchantmentLevel(this.mc.player.getInventory().getItem(i), Enchantments.SILK_TOUCH) > 0) {
               bestSlot = i;
               break;
            }
         }

         if (bestSlot != -1) {
            InvUtils.swap(bestSlot, false);
         }
      }
   }

   private boolean isInsideAnySchematic(BlockPos pos) {
      SchematicPlacementManager spm = DataManager.getSchematicPlacementManager();

      for (SchematicPlacement sp : spm.getAllSchematicsPlacements()) {
         BlockPos origin = sp.getOrigin();
         LitematicaSchematic schematic = sp.getSchematic();
         if (schematic != null) {
            Vec3i size = schematic.getTotalSize();
            if (pos.getX() >= origin.getX()
               && pos.getX() < origin.getX() + size.getX()
               && pos.getY() >= origin.getY()
               && pos.getY() < origin.getY() + size.getY()
               && pos.getZ() >= origin.getZ()
               && pos.getZ() < origin.getZ() + size.getZ()) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean shouldBreak(Shredder.Mode m, BlockState schem, BlockState world) {
      if (schem.isAir()) {
         return m == Shredder.Mode.EXTRA || m == Shredder.Mode.ALL;
      } else if (world.isAir()) {
         return false;
      } else if (world.getBlock() != schem.getBlock()) {
         return m == Shredder.Mode.WRONG_BLOCK || m == Shredder.Mode.ALL;
      } else {
         if (m == Shredder.Mode.WRONG_STATE || m == Shredder.Mode.ALL) {
            for (Property<?> prop : schem.getProperties()) {
               if (!world.hasProperty(prop)) {
                  return true;
               }

               if (!schem.getValue(prop).equals(world.getValue(prop))) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public static enum FilterMode {
      NONE,
      WHITELIST,
      BLACKLIST;
   }

   public static enum Mode {
      ALL,
      WRONG_BLOCK,
      WRONG_STATE,
      EXTRA;
   }
}
