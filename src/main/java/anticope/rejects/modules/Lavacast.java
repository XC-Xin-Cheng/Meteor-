package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class Lavacast extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgShape = this.settings.createGroup("Shape", false);
   private final Setting<Integer> tickInterval = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("tick-interval")).description("Interval")).defaultValue(2)).min(0).sliderMax(20).build());
   private final Setting<Integer> distMin = this.sgShape
      .add(((Builder)((Builder)((Builder)new Builder().name("minimum-distance")).description("Top plane cutoff")).defaultValue(5)).min(0).sliderMax(10).build());
   private final Setting<Integer> lavaDownMult = this.sgShape
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("lava-down-mulipiler")).description("Controls the shape of the cast")).defaultValue(40))
            .min(1)
            .sliderMax(100)
            .build()
      );
   private final Setting<Integer> lavaUpMult = this.sgShape
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("lava-up-mulipiler")).description("Controls the shape of the cast")).defaultValue(8))
            .min(1)
            .sliderMax(100)
            .build()
      );
   private final Setting<Integer> waterDownMult = this.sgShape
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("water-down-mulipiler")).description("Controls the shape of the cast")).defaultValue(4))
            .min(1)
            .sliderMax(100)
            .build()
      );
   private final Setting<Integer> waterUpMult = this.sgShape
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("water-up-mulipiler")).description("Controls the shape of the cast")).defaultValue(1))
            .min(1)
            .sliderMax(100)
            .build()
      );
   private int dist;
   private BlockPos placeFluidPos;
   private int tick;
   private Lavacast.Stage stage = Lavacast.Stage.None;

   public Lavacast() {
      super(MeteorRejectsAddon.CATEGORY, "lavacast", "Automatically Lavacasts");
   }

   public void onActivate() {
      if (this.mc.player == null || this.mc.level == null) {
         this.toggle();
      }

      this.tick = 0;
      this.stage = Lavacast.Stage.None;
      this.placeFluidPos = this.getTargetBlockPos();
      if (this.placeFluidPos == null) {
         this.placeFluidPos = this.mc.player.blockPosition().below(2);
      } else {
         this.placeFluidPos = this.placeFluidPos.above();
      }

      this.dist = -1;
      this.getDistance(new Vec3i(1, 0, 0));
      this.getDistance(new Vec3i(-1, 0, 0));
      this.getDistance(new Vec3i(0, 0, 1));
      this.getDistance(new Vec3i(0, 0, -1));
      if (this.dist < 1) {
         this.error("Couldn't locate bottom.", new Object[0]);
         this.toggle();
      } else {
         this.info("Distance: (highlight)%d(default).", new Object[]{this.dist});
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null && this.mc.level != null) {
         this.tick++;
         if (!this.shouldBreakOnTick()) {
            if (this.dist < (Integer)this.distMin.get()) {
               this.toggle();
            }

            this.tick = 0;
            if (!this.checkMineBlock()) {
               switch (this.stage) {
                  case None:
                     Rotations.rotate(Rotations.getYaw(this.placeFluidPos), Rotations.getPitch(this.placeFluidPos), 100, this::placeLava);
                     this.stage = Lavacast.Stage.LavaDown;
                     break;
                  case LavaDown:
                     Rotations.rotate(Rotations.getYaw(this.placeFluidPos), Rotations.getPitch(this.placeFluidPos), 100, this::pickupLiquid);
                     this.stage = Lavacast.Stage.LavaUp;
                     break;
                  case LavaUp:
                     Rotations.rotate(Rotations.getYaw(this.placeFluidPos), Rotations.getPitch(this.placeFluidPos), 100, this::placeWater);
                     this.stage = Lavacast.Stage.WaterDown;
                     break;
                  case WaterDown:
                     Rotations.rotate(Rotations.getYaw(this.placeFluidPos), Rotations.getPitch(this.placeFluidPos), 100, this::pickupLiquid);
                     this.stage = Lavacast.Stage.WaterUp;
                     break;
                  case WaterUp:
                     this.dist--;
                     Rotations.rotate(Rotations.getYaw(this.placeFluidPos), Rotations.getPitch(this.placeFluidPos), 100, this::placeLava);
                     this.stage = Lavacast.Stage.LavaDown;
               }
            }
         }
      }
   }

   private boolean shouldBreakOnTick() {
      if (this.stage == Lavacast.Stage.LavaDown && this.tick < this.dist * (Integer)this.lavaDownMult.get()) {
         return true;
      } else if (this.stage == Lavacast.Stage.LavaUp && this.tick < this.dist * (Integer)this.lavaUpMult.get()) {
         return true;
      } else if (this.stage == Lavacast.Stage.WaterDown && this.tick < this.dist * (Integer)this.waterDownMult.get()) {
         return true;
      } else {
         return this.stage == Lavacast.Stage.WaterUp && this.tick < this.dist * this.waterUpMult.get() ? true : this.tick < (Integer)this.tickInterval.get();
      }
   }

   private boolean checkMineBlock() {
      if (this.stage == Lavacast.Stage.None && this.mc.level.getBlockState(this.placeFluidPos).getBlock() != Blocks.AIR) {
         Rotations.rotate(Rotations.getYaw(this.placeFluidPos), Rotations.getPitch(this.placeFluidPos), 100, this::updateBlockBreakingProgress);
         return true;
      } else {
         return false;
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.placeFluidPos != null) {
         double x1 = this.placeFluidPos.getX();
         double y1 = this.placeFluidPos.getY();
         double z1 = this.placeFluidPos.getZ();
         double x2 = x1 + 1.0;
         double y2 = y1 + 1.0;
         double z2 = z1 + 1.0;
         SettingColor lineColor = new SettingColor(128, 128, 128);
         if (this.stage == Lavacast.Stage.LavaDown) {
            lineColor = new SettingColor(255, 180, 10);
         }

         if (this.stage == Lavacast.Stage.LavaUp) {
            lineColor = new SettingColor(255, 180, 128);
         }

         if (this.stage == Lavacast.Stage.WaterDown) {
            lineColor = new SettingColor(10, 10, 255);
         }

         if (this.stage == Lavacast.Stage.WaterUp) {
            lineColor = new SettingColor(128, 128, 255);
         }

         SettingColor sideColor = new SettingColor(lineColor.r, lineColor.g, lineColor.b, 75);
         event.renderer.box(x1, y1, z1, x2, y2, z2, sideColor, lineColor, ShapeMode.Both, 0);
      }
   }

   private void placeLava() {
      FindItemResult findItemResult = InvUtils.findInHotbar(new Item[]{Items.LAVA_BUCKET});
      if (!findItemResult.found()) {
         this.error("No lava bucket found.", new Object[0]);
         this.toggle();
      } else {
         InvUtils.swap(findItemResult.slot(), true);
         this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND);
         InvUtils.swapBack();
      }
   }

   private void placeWater() {
      FindItemResult findItemResult = InvUtils.findInHotbar(new Item[]{Items.WATER_BUCKET});
      if (!findItemResult.found()) {
         this.error("No water bucket found.", new Object[0]);
         this.toggle();
      } else {
         InvUtils.swap(findItemResult.slot(), true);
         this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND);
         InvUtils.swapBack();
      }
   }

   private void pickupLiquid() {
      FindItemResult findItemResult = InvUtils.findInHotbar(new Item[]{Items.BUCKET});
      if (!findItemResult.found()) {
         this.error("No bucket found.", new Object[0]);
         this.toggle();
      } else {
         InvUtils.swap(findItemResult.slot(), true);
         this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND);
         InvUtils.swapBack();
      }
   }

   private void updateBlockBreakingProgress() {
      this.mc.gameMode.continueDestroyBlock(this.placeFluidPos, Direction.UP);
   }

   private BlockPos getTargetBlockPos() {
      HitResult blockHit = this.mc.hitResult;
      return blockHit.getType() != Type.BLOCK ? null : ((BlockHitResult)blockHit).getBlockPos();
   }

   private void getDistance(Vec3i offset) {
      BlockPos pos = this.placeFluidPos.below().offset(offset);
      BlockHitResult result = this.mc
         .level
         .clip(new ClipContext(Vec3.atCenterOf(pos), Vec3.atCenterOf(pos.below(250)), Block.COLLIDER, Fluid.ANY, this.mc.player));
      if (result != null && result.getType() == Type.BLOCK) {
         int new_dist = this.placeFluidPos.getY() - result.getBlockPos().getY();
         if (new_dist > this.dist) {
            this.dist = new_dist;
         }
      }
   }

   public String getInfoString() {
      return this.stage.toString();
   }

   private static enum Stage {
      None,
      LavaDown,
      LavaUp,
      WaterDown,
      WaterUp;
   }
}
