package com.stash.hunt.modules;

import com.stash.hunt.Addon;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoPortal extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final List<BlockPos> waitingForBreak = new ArrayList<>();
   private final Setting<Integer> placeDelay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("place-delay")).description("Ticks between each obsidian placement.")).defaultValue(1))
            .sliderRange(1, 20)
            .build()
      );
   private final Setting<Integer> blocksPerTick = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("blocks-per-tick")).description("How many blocks to place each tick.")).defaultValue(1))
            .sliderRange(1, 5)
            .build()
      );
   private final Setting<Boolean> render = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("render"))
                  .description("Renders the portal frame as it's being placed."))
               .defaultValue(true))
            .build()
      );
   private final Setting<ShapeMode> shapeMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("shape-mode"))
                  .description("How the box is rendered."))
               .defaultValue(ShapeMode.Both))
            .build()
      );
   private final Setting<SettingColor> sideColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder().name("side-color"))
            .defaultValue(new SettingColor(100, 100, 255, 10))
            .build()
      );
   private final Setting<SettingColor> lineColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder().name("line-color"))
            .defaultValue(new SettingColor(100, 100, 255, 255))
            .build()
      );
   private final List<BlockPos> portalBlocks = new ArrayList<>();
   private int delay = 0;
   private int index = 0;

   public AutoPortal() {
      super(Addon.CATEGORY, "auto-portal", "For the Base Hunter who has places to be.");
   }

   public void onActivate() {
      int obsidianCount = 0;

      for (int i = 0; i < 36; i++) {
         if (this.mc.player.getInventory().getItem(i).getItem() == Items.OBSIDIAN) {
            obsidianCount += this.mc.player.getInventory().getItem(i).getCount();
         }
      }

      if (obsidianCount < 10) {
         this.error("Not enough obsidian to build the portal (need at least 10)!", new Object[0]);
         this.toggle();
      } else {
         this.portalBlocks.clear();
         this.index = 0;
         this.delay = 0;
         Direction forward = this.mc.player.getDirection();
         Direction right = forward.getClockWise();
         BlockPos standingPos = this.mc.player.blockPosition();
         BlockPos blockBelow = standingPos.below();
         double blockHeight = this.mc.level.getBlockState(blockBelow).getCollisionShape(this.mc.level, blockBelow).max(Axis.Y);
         if (blockHeight < 1.0) {
            standingPos = standingPos.above();
         }

         BlockPos base = standingPos.relative(forward, 2).relative(right, -1);
         int obsidianCheck = 0;
         List<BlockPos> checkPositions = List.of(
            base.relative(right, 1),
            base.relative(right, 2),
            base.relative(right, 0).above(1),
            base.relative(right, 0).above(2),
            base.relative(right, 0).above(3),
            base.relative(right, 3).above(1),
            base.relative(right, 3).above(2),
            base.relative(right, 3).above(3),
            base.relative(right, 1).above(4),
            base.relative(right, 2).above(4)
         );
         boolean obstructed = checkPositions.stream().anyMatch(pos -> !this.mc.level.getBlockState(pos).canBeReplaced());
         if (obstructed) {
            this.error("Portal area obstructed. Move and try again.", new Object[0]);
            this.portalBlocks.clear();
            this.portalBlocks.addAll(checkPositions);
            this.index = checkPositions.size();
         } else {
            for (BlockPos checkPos : checkPositions) {
               if (this.mc.level.getBlockState(checkPos).getBlock().asItem() == Items.OBSIDIAN) {
                  obsidianCheck++;
               }
            }

            if (obsidianCheck >= checkPositions.size()) {
               this.error("A portal already exists here!", new Object[0]);
               this.toggle();
            } else {
               this.portalBlocks.add(base.relative(right, 1));
               this.portalBlocks.add(base.relative(right, 2));

               for (int ix = 1; ix <= 3; ix++) {
                  this.portalBlocks.add(base.relative(right, 0).above(ix));
               }

               for (int ix = 1; ix <= 3; ix++) {
                  this.portalBlocks.add(base.relative(right, 3).above(ix));
               }

               this.portalBlocks.add(base.relative(right, 1).above(4));
               this.portalBlocks.add(base.relative(right, 2).above(4));

               for (int ix = 0; ix < 9; ix++) {
                  if (this.mc.player.getInventory().getItem(ix).getItem() == Items.OBSIDIAN) {
                     this.mc.player.getInventory().setSelectedSlot(ix);
                     break;
                  }
               }
            }
         }
      }
   }

   public void onDeactivate() {
      this.portalBlocks.clear();
      this.index = 0;
      this.delay = 0;
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.player != null && this.mc.level != null) {
         if (this.mc.player.getMainHandItem().getItem() instanceof BlockItem blockItem) {
            if (blockItem.getBlock().asItem() == Items.OBSIDIAN) {
               if (this.index >= this.portalBlocks.size()) {
                  this.toggle();
               } else {
                  this.delay++;
                  if (this.delay >= (Integer)this.placeDelay.get()) {
                     for (int i = 0; i < this.blocksPerTick.get() && this.index < this.portalBlocks.size(); this.index++) {
                        BlockPos pos = this.portalBlocks.get(this.index);
                        if (!this.mc.level.getBlockState(pos).canBeReplaced()) {
                           if (!this.waitingForBreak.contains(pos)
                              && this.mc.level.getBlockState(pos).getBlock().asItem() != Items.OBSIDIAN
                              && this.mc.gameMode != null) {
                              this.mc.gameMode.startDestroyBlock(pos, Direction.UP);
                              this.mc.player.swing(InteractionHand.MAIN_HAND);
                              this.waitingForBreak.add(pos);
                           }

                           this.index--;
                           return;
                        }

                        this.waitingForBreak.remove(pos);
                        BlockHitResult bhr = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                        this.mc.player.connection.send(new ServerboundPlayerActionPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                        this.mc
                           .player
                           .connection
                           .send(new ServerboundUseItemOnPacket(InteractionHand.OFF_HAND, bhr, this.mc.player.containerMenu.getStateId() + 2));
                        this.mc.player.connection.send(new ServerboundPlayerActionPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                        this.mc.player.swing(InteractionHand.MAIN_HAND);
                        i++;
                     }

                     this.delay = 0;
                     if (this.index >= this.portalBlocks.size()) {
                        for (int i = 0; i < 9; i++) {
                           if (this.mc.player.getInventory().getItem(i).getItem() == Items.FLINT_AND_STEEL) {
                              this.mc.player.getInventory().setSelectedSlot(i);
                              BlockPos firePos = this.portalBlocks.get(0).above();
                              BlockHitResult fireHit = new BlockHitResult(Vec3.atCenterOf(firePos), Direction.UP, firePos, false);
                              this.mc.gameMode.useItemOn(this.mc.player, InteractionHand.MAIN_HAND, fireHit);
                              this.mc.player.swing(InteractionHand.MAIN_HAND);
                              break;
                           }
                        }

                        this.info("Portal complete. AutoPortal disabled.", new Object[0]);
                        this.toggle();
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if ((Boolean)this.render.get()) {
         for (int i = this.index; i < this.portalBlocks.size(); i++) {
            BlockPos pos = this.portalBlocks.get(i);
            event.renderer.box(pos, (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
         }
      }
   }
}
