package nekiplay.meteorplus.mixin.meteorclient.modules;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Blink;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.utils.RaycastUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Freecam.class})
public class FreecamMixin {
   @Unique
   private final Freecam freecam = (Freecam)this;
   @Unique
   private final SettingGroup freecamMeteorPlusSetting = this.freecam.settings.createGroup("Meteor+");
   @Unique
   private final Setting<Boolean> moveBaritoneControl = this.freecamMeteorPlusSetting
      .add(
         ((Builder)((Builder)new Builder().name("baritone-move-control"))
               .description("click bind to set the destination on the selected block. Right mouse click to cancel."))
            .build()
      );
   @Unique
   private final Setting<Keybind> baritoneMoveKey = this.freecamMeteorPlusSetting
      .add(
         ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)new meteordevelopment.meteorclient.settings.KeybindSetting.Builder()
                        .name("baritone-move-keybind"))
                     .description("The bind for move."))
                  .visible(this.moveBaritoneControl::get))
               .defaultValue(Keybind.fromKey(0)))
            .build()
      );
   @Unique
   private final Setting<Boolean> blinkBaritoneControl = this.freecamMeteorPlusSetting
      .add(((Builder)((Builder)new Builder().name("baritone-blink-move-control")).description("Click bind to move to point in blink.")).build());
   @Unique
   private final Setting<Keybind> baritoneMoveBlinkKey = this.freecamMeteorPlusSetting
      .add(
         ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)new meteordevelopment.meteorclient.settings.KeybindSetting.Builder()
                     .name("baritone-blink-move-keybind"))
                  .description("The bind for move in blink."))
               .visible(this.blinkBaritoneControl::get))
            .build()
      );
   @Unique
   private final Setting<Keybind> baritoneStopKey = this.freecamMeteorPlusSetting
      .add(
         ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)new meteordevelopment.meteorclient.settings.KeybindSetting.Builder()
                        .name("baritone-stop-keybind"))
                     .description("The bind for stop baritone actions."))
                  .visible(() -> (Boolean)this.blinkBaritoneControl.get() || (Boolean)this.moveBaritoneControl.get()))
               .defaultValue(Keybind.fromKey(1)))
            .build()
      );
   @Unique
   private final Blink blink = (Blink)Modules.get().get(Blink.class);
   @Unique
   private boolean isBlinkMoving = false;

   @Unique
   private BlockPos tryGetValidPos(BlockPos pos) {
      assert MeteorClient.mc.level != null;

      BlockState state = MeteorClient.mc.level.getBlockState(pos);
      Block block = state.getBlock();
      return block != Blocks.FERN
            && block != Blocks.SHORT_GRASS
            && block != Blocks.TALL_GRASS
            && block != Blocks.GLOW_LICHEN
            && block != Blocks.DEAD_BUSH
            && block != Blocks.SNOW
            && block != Blocks.MOSS_CARPET
            && block != Blocks.TORCH
            && block != Blocks.WALL_TORCH
            && block != Blocks.SPRUCE_SIGN
            && block != Blocks.ACACIA_SIGN
            && block != Blocks.BIRCH_SIGN
            && block != Blocks.CHERRY_SIGN
            && block != Blocks.BAMBOO_SIGN
            && block != Blocks.OAK_SIGN
            && block != Blocks.CRIMSON_SIGN
            && block != Blocks.DARK_OAK_SIGN
            && block != Blocks.JUNGLE_SIGN
            && block != Blocks.MANGROVE_SIGN
            && block != Blocks.WARPED_SIGN
            && block != Blocks.SPRUCE_WALL_SIGN
            && block != Blocks.ACACIA_WALL_SIGN
            && block != Blocks.BIRCH_WALL_SIGN
            && block != Blocks.CHERRY_WALL_SIGN
            && block != Blocks.BAMBOO_WALL_SIGN
            && block != Blocks.OAK_WALL_SIGN
            && block != Blocks.CRIMSON_WALL_SIGN
            && block != Blocks.DARK_OAK_WALL_SIGN
            && block != Blocks.JUNGLE_WALL_SIGN
            && block != Blocks.MANGROVE_WALL_SIGN
            && block != Blocks.WARPED_WALL_SIGN
            && block != Blocks.BROWN_MUSHROOM
            && block != Blocks.RED_MUSHROOM
            && block != Blocks.CRIMSON_FUNGUS
            && block != Blocks.WARPED_FUNGUS
            && block != Blocks.DANDELION
            && block != Blocks.POPPY
            && block != Blocks.BLUE_ORCHID
            && block != Blocks.ALLIUM
            && block != Blocks.AZURE_BLUET
            && block != Blocks.RED_TULIP
            && block != Blocks.ORANGE_TULIP
            && block != Blocks.WHITE_TULIP
            && block != Blocks.PINK_TULIP
            && block != Blocks.OXEYE_DAISY
            && block != Blocks.CORNFLOWER
            && block != Blocks.LILY_OF_THE_VALLEY
            && block != Blocks.TORCHFLOWER
            && block != Blocks.PINK_PETALS
            && block != Blocks.SUGAR_CANE
            && block != Blocks.NETHER_WART
            && block != Blocks.PITCHER_CROP
            && block != Blocks.TORCHFLOWER_CROP
            && block != Blocks.BEETROOTS
            && block != Blocks.WHEAT
            && block != Blocks.CARROTS
            && block != Blocks.POTATOES
            && block != Blocks.MELON_STEM
            && block != Blocks.PUMPKIN_STEM
            && block != Blocks.SPRUCE_SAPLING
            && block != Blocks.ACACIA_SAPLING
            && block != Blocks.BIRCH_SAPLING
            && block != Blocks.BAMBOO_SAPLING
            && block != Blocks.CHERRY_SAPLING
            && block != Blocks.DARK_OAK_SAPLING
            && block != Blocks.JUNGLE_SAPLING
            && block != Blocks.OAK_SAPLING
            && block != Blocks.RAIL
            && block != Blocks.ACTIVATOR_RAIL
            && block != Blocks.POWERED_RAIL
            && block != Blocks.DETECTOR_RAIL
         ? pos.above()
         : pos;
   }

   @Unique
   @Nullable
   private BlockPos rayCastClicked() {
      BlockPos blockPos = null;
      Vec3 rotationVector = RaycastUtils.getRotationVector(
         (float)this.freecam.getPitch(MeteorClient.mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)),
         (float)this.freecam.getYaw(MeteorClient.mc.getDeltaTracker().getGameTimeDeltaPartialTick(true))
      );
      Vec3 pos = new Vec3(this.freecam.pos.x, this.freecam.pos.y, this.freecam.pos.z);
      HitResult result = RaycastUtils.raycast(pos, rotationVector, 256.0, MeteorClient.mc.getDeltaTracker().getGameTimeDeltaPartialTick(true), true);
      if (result.getType() == Type.BLOCK) {
         BlockHitResult blockHitResult = (BlockHitResult)result;
         blockPos = blockHitResult.getBlockPos();
      }

      return blockPos;
   }

   @Unique
   private void Work(Cancellable event) {
      if (((Keybind)this.baritoneMoveBlinkKey.get()).isPressed() && MeteorClient.mc.screen == null) {
         BlockPos clicked = this.rayCastClicked();
         if ((Boolean)this.blinkBaritoneControl.get()) {
            if (clicked == null) {
               return;
            }

            if (MeteorClient.mc.level == null) {
               return;
            }

            BlockState state = MeteorClient.mc.level.getBlockState(clicked);
            if (state.isAir()) {
               return;
            }

            this.isBlinkMoving = true;
            GoalBlock goal = new GoalBlock(this.tryGetValidPos(clicked));
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(null);
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
            event.cancel();
         }
      }

      if (((Keybind)this.baritoneMoveKey.get()).isPressed() && MeteorClient.mc.screen == null) {
         BlockPos clicked = this.rayCastClicked();
         if (clicked == null) {
            return;
         }

         if (MeteorClient.mc.level == null) {
            return;
         }

         BlockState state = MeteorClient.mc.level.getBlockState(clicked);
         if (state.isAir()) {
            return;
         }

         if (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().forceCancel();
         }

         GoalBlock goal = new GoalBlock(this.tryGetValidPos(clicked));
         BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(null);
         BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
         event.cancel();
      }

      if (((Keybind)this.baritoneStopKey.get()).isPressed() && MeteorClient.mc.screen == null) {
         BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().forceCancel();
         if (this.blink != null && this.blink.isActive()) {
            this.blink.toggle();
            this.isBlinkMoving = false;
         }

         event.cancel();
      }
   }

   @Unique
   @EventHandler
   private void onKeyEvent(KeyInputEvent event) {
      if (MeteorClient.mc.level != null && event.action == KeyAction.Press) {
         this.Work(event);
      }
   }

   @Unique
   @EventHandler
   private void onMouseClickEvent(MouseClickEvent event) {
      if (MeteorClient.mc.level != null && event.action == KeyAction.Press) {
         this.Work(event);
      }
   }

   @Unique
   @EventHandler
   private void onTickEvent(Pre event) {
      if (MeteorClient.mc.level != null && (Boolean)this.blinkBaritoneControl.get() && this.blink != null) {
         if (this.isBlinkMoving
            && (
               BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().hasPath()
                  || BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()
            )
            && !this.blink.isActive()) {
            this.blink.toggle();
         }

         if (this.isBlinkMoving
            && (
               !BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().hasPath()
                  || !BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()
            )
            && this.blink.isActive()) {
            this.blink.toggle();
            this.isBlinkMoving = false;
         }
      }
   }
}
