package com.stash.hunt.modules;

import com.stash.hunt.Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class GrimAirPlace extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRange = this.settings.createGroup("range");
   private final Setting<Integer> placeDelay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("place-delay")).description("The delay in ticks between block placements.")).defaultValue(0)).build()
      );
   private final Setting<Boolean> render = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("render"))
                  .description("Renders a block overlay where the obsidian will be placed."))
               .defaultValue(true))
            .build()
      );
   private final Setting<ShapeMode> shapeMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("shape-mode"))
                  .description("How the shapes are rendered."))
               .defaultValue(ShapeMode.Both))
            .build()
      );
   private final Setting<SettingColor> sideColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("side-color"))
               .description("The color of the sides of the blocks being rendered."))
            .defaultValue(new SettingColor(204, 0, 0, 10))
            .build()
      );
   private final Setting<SettingColor> lineColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("line-color"))
               .description("The color of the lines of the blocks being rendered."))
            .defaultValue(new SettingColor(204, 0, 0, 255))
            .build()
      );
   private final Setting<Boolean> customRange = this.sgRange
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("custom-range"))
                  .description("Use custom range for air place."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Double> range = this.sgRange
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("range"))
                  .description("Custom range to place at."))
               .visible(this.customRange::get))
            .defaultValue(5.0)
            .min(0.0)
            .sliderMax(5.5)
            .build()
      );
   private HitResult hitResult;
   private int delay = 0;

   public GrimAirPlace() {
      super(Addon.CATEGORY, "grim-air-place", "Places a block where your crosshair is pointing at.");
   }

   public void onActivate() {
      this.delay = 0;
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.player != null) {
         this.delay++;
         double r = this.customRange.get() ? (Double)this.range.get() : this.mc.player.blockInteractionRange();
         this.hitResult = this.mc.getCameraEntity().pick(r, 0.0F, false);
         if (this.hitResult instanceof BlockHitResult blockHitResult
            && (this.mc.player.getMainHandItem().getItem() instanceof BlockItem || this.mc.player.getMainHandItem().getItem() instanceof SpawnEggItem)) {
            if (this.delay >= (Integer)this.placeDelay.get()) {
               if (this.mc.options.keyUse.isDown()) {
                  this.mc.player.connection.send(new ServerboundPlayerActionPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
                  this.mc
                     .player
                     .connection
                     .send(new ServerboundUseItemOnPacket(InteractionHand.OFF_HAND, blockHitResult, this.mc.player.containerMenu.getStateId() + 2));
                  this.mc.player.connection.send(new ServerboundPlayerActionPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
                  this.mc.player.swing(InteractionHand.MAIN_HAND);
                  this.delay = 0;
               }
            }
         }
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.hitResult instanceof BlockHitResult blockHitResult
         && this.mc.level.getBlockState(blockHitResult.getBlockPos()).canBeReplaced()
         && (this.mc.player.getMainHandItem().getItem() instanceof BlockItem || this.mc.player.getMainHandItem().getItem() instanceof SpawnEggItem)
         && (Boolean)this.render.get()) {
         event.renderer.box(blockHitResult.getBlockPos(), (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
      }
   }
}
