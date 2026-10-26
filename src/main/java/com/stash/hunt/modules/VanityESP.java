package com.stash.hunt.modules;

import com.stash.hunt.Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

public class VanityESP extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgColors = this.settings.createGroup("Colors");
   private final Setting<Boolean> highlightItemFrames = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("item-frames")).description("highlights item frames that contain maps.")).defaultValue(true)).build()
      );
   private final Setting<Boolean> highlightBanners = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("banners")).description("highlights banners.")).defaultValue(true)).build());
   private final Setting<SettingColor> mapColor = this.sgColors
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("map-color"))
               .description("fill color for item frames containing maps."))
            .defaultValue(new SettingColor(255, 255, 0, 50))
            .build()
      );
   private final Setting<SettingColor> mapOutlineColor = this.sgColors
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("map-outline-color"))
               .description("outline color for item frames containing maps."))
            .defaultValue(new SettingColor(255, 255, 0, 255))
            .build()
      );
   private final Setting<SettingColor> bannerColor = this.sgColors
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("banner-fill"))
               .description("fill color for banners."))
            .defaultValue(new SettingColor(255, 0, 0, 50))
            .build()
      );
   private final Setting<SettingColor> bannerOutline = this.sgColors
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("banner-outline"))
               .description("outline color for banners."))
            .defaultValue(new SettingColor(255, 0, 0, 255))
            .build()
      );

   public VanityESP() {
      super(Addon.CATEGORY, "VanityESP", "Highlights maparts and banners");
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.mc.level != null && this.mc.player != null) {
         if ((Boolean)this.highlightItemFrames.get()) {
            for (ItemFrame frame : this.mc
               .level
               .getEntitiesOfClass(
                  ItemFrame.class,
                  this.mc.player.getBoundingBox().inflate(64.0),
                  e -> e.getItem().getItem().getDescriptionId().equals("item.minecraft.filled_map")
               )) {
               float pitch = frame.getXRot();
               AABB box;
               if (pitch != 90.0F && pitch != -90.0F) {
                  box = frame.getBoundingBox().inflate(0.12, 0.12, 0.01);
               } else {
                  box = frame.getBoundingBox().inflate(0.12, 0.01, 0.12);
               }

               Color fill = new Color((Color)this.mapColor.get());
               Color outline = new Color((Color)this.mapOutlineColor.get());
               event.renderer.box(box, fill, outline, ShapeMode.Both, 0);
            }
         }

         if ((Boolean)this.highlightBanners.get()) {
            int radius = 8;
            BlockPos playerPos = this.mc.player.blockPosition();

            for (int dx = -radius; dx <= radius; dx++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  LevelChunk chunk = this.mc.level.getChunk(playerPos.getX() / 16 + dx, playerPos.getZ() / 16 + dz);
                  if (chunk != null) {
                     for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be instanceof BannerBlockEntity banner) {
                           BlockPos pos = banner.getBlockPos();
                           BlockState state = this.mc.level.getBlockState(pos);
                           Color fill = new Color((Color)this.bannerColor.get());
                           Color outline = new Color((Color)this.bannerOutline.get());
                           if (state.hasProperty(WallBannerBlock.FACING)) {
                              Direction facing = (Direction)state.getValue(WallBannerBlock.FACING);
                              double centerX = pos.getX() + 0.5;
                              double centerZ = pos.getZ() + 0.5;
                              double offset = 0.1;
                              double depth = 0.03;
                              double width = 0.45;
                              double y1 = pos.getY() - 0.95;
                              double y2 = pos.getY() + 0.85;
                              AABB box;
                              switch (facing) {
                                 case NORTH:
                                    box = new AABB(centerX - width, y1, pos.getZ() + 1 - offset - depth, centerX + width, y2, pos.getZ() + 1 - offset);
                                    break;
                                 case SOUTH:
                                    box = new AABB(centerX - width, y1, pos.getZ() + offset, centerX + width, y2, pos.getZ() + offset + depth);
                                    break;
                                 case WEST:
                                    box = new AABB(pos.getX() + 1 - offset - depth, y1, centerZ - width, pos.getX() + 1 - offset, y2, centerZ + width);
                                    break;
                                 case EAST:
                                    box = new AABB(pos.getX() + offset, y1, centerZ - width, pos.getX() + offset + depth, y2, centerZ + width);
                                    break;
                                 default:
                                    continue;
                              }

                              event.renderer.box(box, fill, outline, ShapeMode.Both, 0);
                           } else if (state.hasProperty(BannerBlock.ROTATION)) {
                              int rotation = (Integer)state.getValue(BannerBlock.ROTATION);
                              double centerX = pos.getX() + 0.5;
                              double centerZ = pos.getZ() + 0.5;
                              double y1 = pos.getY();
                              double y2 = pos.getY() + 1.85;
                              AABB box;
                              if (rotation == 0 || rotation == 8) {
                                 double width = 0.45;
                                 double depth = 0.03;
                                 box = new AABB(centerX - width, y1, centerZ - depth, centerX + width, y2, centerZ + depth);
                              } else if (rotation != 4 && rotation != 12) {
                                 double size = 0.3;
                                 box = new AABB(centerX - size, y1, centerZ - size, centerX + size, y2, centerZ + size);
                              } else {
                                 double width = 0.03;
                                 double depth = 0.45;
                                 box = new AABB(centerX - width, y1, centerZ - depth, centerX + width, y2, centerZ + depth);
                              }

                              event.renderer.box(box, fill, outline, ShapeMode.Both, 0);
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
