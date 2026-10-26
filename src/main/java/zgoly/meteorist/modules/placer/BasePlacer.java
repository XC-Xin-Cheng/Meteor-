package zgoly.meteorist.modules.placer;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import zgoly.meteorist.gui.screens.PlacerScreen;

public class BasePlacer implements ISerializable<BasePlacer> {
   public final Settings settings = new Settings();
   protected final SettingGroup sgName = this.settings.createGroup("Name");
   protected final SettingGroup sgCornerPos = this.settings.createGroup("Corner Positions");
   protected final SettingGroup sgCornerAnchorPos = this.settings.createGroup("Corner Anchor Positions");
   protected final SettingGroup sgRotation = this.settings.createGroup("Rotation");
   protected final SettingGroup sgAnchor = this.settings.createGroup("Anchor");
   protected final SettingGroup sgBlocks = this.settings.createGroup("Blocks");
   protected final SettingGroup sgColor = this.settings.createGroup("Color");
   protected final SettingGroup sgControls = this.settings.createGroup("Controls");
   public final Setting<String> name = this.sgName
      .add(((Builder)((Builder)((Builder)new Builder().name("name")).description("The name of the placer.")).onChanged(value -> reloadParent())).build());
   public final Setting<BlockPos> cornerPos1 = this.sgCornerPos
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                  .name("corner-pos-1"))
               .description("The first corner position of the placer."))
            .build()
      );
   public final Setting<BlockPos> cornerPos2 = this.sgCornerPos
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                  .name("corner-pos-2"))
               .description("The second corner position of the placer."))
            .build()
      );
   public final Setting<BlockPos> cornerAnchorPos1 = this.sgCornerAnchorPos
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                  .name("corner-anchor-pos-1"))
               .description("The first corner anchor position of the placer."))
            .build()
      );
   public final Setting<BlockPos> cornerAnchorPos2 = this.sgCornerAnchorPos
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                  .name("corner-anchor-pos-2"))
               .description("The second corner anchor position of the placer."))
            .build()
      );
   public final Setting<Boolean> rotateX1 = this.sgRotation
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate-x-1"))
                  .description("Is the first X position should be rotated where is the player looking at."))
               .defaultValue(true))
            .build()
      );
   public final Setting<Boolean> rotateY1 = this.sgRotation
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate-y-1"))
                  .description("Is the first Y position should be rotated where is the player looking at."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> rotateZ1 = this.sgRotation
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate-z-1"))
                  .description("Is the first Z position should be rotated where is the player looking at."))
               .defaultValue(true))
            .build()
      );
   public final Setting<Boolean> rotateX2 = this.sgRotation
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate-x-2"))
                  .description("Is the second X position should be rotated where is the player looking at."))
               .defaultValue(true))
            .build()
      );
   public final Setting<Boolean> rotateY2 = this.sgRotation
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate-y-2"))
                  .description("Is the second Y position should be rotated where is the player looking at."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> rotateZ2 = this.sgRotation
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate-z-2"))
                  .description("Is the second Z position should be rotated where is the player looking at."))
               .defaultValue(true))
            .build()
      );
   public final Setting<Boolean> anchorX1 = this.sgAnchor
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("anchor-x-1"))
                  .description("Is the first X position should be anchored (fixed) in the world space or be relative to the player."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> anchorY1 = this.sgAnchor
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("anchor-y-1"))
                  .description("Is the first Y position should be anchored (fixed) in the world space or be relative to the player."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> anchorZ1 = this.sgAnchor
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("anchor-z-1"))
                  .description("Is the first Z position should be anchored (fixed) in the world space or be relative to the player."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> anchorX2 = this.sgAnchor
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("anchor-x-2"))
                  .description("Is the second X position should be anchored (fixed) in the world space or be relative to the player."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> anchorY2 = this.sgAnchor
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("anchor-y-2"))
                  .description("Is the second Y position should be anchored (fixed) in the world space or be relative to the player."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> anchorZ2 = this.sgAnchor
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("anchor-z-2"))
                  .description("Is the second Z position should be anchored (fixed) in the world space or be relative to the player."))
               .defaultValue(false))
            .build()
      );
   public final Setting<List<Block>> blocks = this.sgBlocks
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                     .name("blocks"))
                  .description("The blocks that the placer should place."))
               .defaultValue(List.of(Blocks.COBBLESTONE)))
            .build()
      );
   public final Setting<SettingColor> sideColor = this.sgColor
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("side-color"))
               .description("The color of the side of the placer hologram."))
            .defaultValue(new SettingColor(0, 255, 0, 50))
            .build()
      );
   public final Setting<SettingColor> lineColor = this.sgColor
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("line-color"))
               .description("The color of the line of the placer hologram."))
            .defaultValue(new SettingColor(0, 255, 0, 255))
            .build()
      );
   public final Setting<Boolean> visible = this.sgControls
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("visible"))
                     .description("Is the placer visible."))
                  .defaultValue(true))
               .onChanged(value -> reloadParent()))
            .build()
      );
   public final Setting<Boolean> active = this.sgControls
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("active"))
                     .description("Is the placer active."))
                  .defaultValue(true))
               .onChanged(value -> reloadParent()))
            .build()
      );

   public static void reloadParent() {
      if (MeteorClient.mc.screen instanceof PlacerScreen placerScreen && placerScreen.parent instanceof WindowScreen windowScreen) {
         windowScreen.reload();
      }
   }

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.put("settings", this.settings.toTag());
      return tag;
   }

   public BasePlacer fromTag(CompoundTag tag) {
      CompoundTag settingsTag = (CompoundTag)tag.get("settings");
      if (settingsTag != null) {
         this.settings.fromTag(settingsTag);
      }

      return this;
   }

   public BasePlacer copy() {
      return new BasePlacer().fromTag(this.toTag());
   }
}
