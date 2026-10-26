package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;

public class AutoWither extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<Integer> horizontalRadius = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("horizontal-radius")).description("Horizontal radius for placement")).defaultValue(4))
            .min(0)
            .sliderMax(6)
            .build()
      );
   private final Setting<Integer> verticalRadius = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("vertical-radius")).description("Vertical radius for placement")).defaultValue(3))
            .min(0)
            .sliderMax(6)
            .build()
      );
   private final Setting<AutoWither.Priority> priority = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("priority"))
                  .description("Priority"))
               .defaultValue(AutoWither.Priority.Random))
            .build()
      );
   private final Setting<Integer> witherDelay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("wither-delay")).description("Delay in ticks between wither placements")).defaultValue(1))
            .min(1)
            .sliderMax(10)
            .build()
      );
   private final Setting<Integer> blockDelay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("block-delay")).description("Delay in ticks between block placements")).defaultValue(1))
            .min(0)
            .sliderMax(10)
            .build()
      );
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate"))
                  .description("Whether or not to rotate while building"))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> turnOff = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("turn-off"))
                  .description("Turns off automatically after building a single wither."))
               .defaultValue(true))
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
   private final Setting<SettingColor> sideColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("side-color"))
               .description("The side color of the target block rendering."))
            .defaultValue(new SettingColor(197, 137, 232, 10))
            .build()
      );
   private final Setting<SettingColor> lineColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("line-color"))
               .description("The line color of the target block rendering."))
            .defaultValue(new SettingColor(197, 137, 232))
            .build()
      );
   private final Pool<AutoWither.Wither> witherPool = new Pool(AutoWither.Wither::new);
   private final ArrayList<AutoWither.Wither> withers = new ArrayList<>();
   private AutoWither.Wither wither;
   private int witherTicksWaited;
   private int blockTicksWaited;

   public AutoWither() {
      super(MeteorRejectsAddon.CATEGORY, "auto-wither", "Automatically builds withers.");
   }

   public void onDeactivate() {
      this.wither = null;
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.wither == null) {
         if (this.witherTicksWaited < (Integer)this.witherDelay.get() - 1) {
            return;
         }

         for (AutoWither.Wither wither : this.withers) {
            this.witherPool.free(wither);
         }

         this.withers.clear();
         BlockIterator.register((Integer)this.horizontalRadius.get(), (Integer)this.verticalRadius.get(), (blockPos, blockState) -> {
            Direction dir = Direction.fromYRot(Rotations.getYaw(blockPos)).getOpposite();
            if (this.isValidSpawn(blockPos, dir)) {
               this.withers.add(((AutoWither.Wither)this.witherPool.get()).set(blockPos, dir));
            }
         });
      }
   }

   @EventHandler
   private void onPostTick(Post event) {
      if (this.wither == null) {
         if (this.witherTicksWaited < (Integer)this.witherDelay.get() - 1) {
            this.witherTicksWaited++;
            return;
         }

         if (this.withers.isEmpty()) {
            return;
         }

         switch ((AutoWither.Priority)this.priority.get()) {
            case Closest:
               this.withers.sort(Comparator.comparingDouble(w -> PlayerUtils.distanceTo(w.foot)));
            case Furthest:
               this.withers.sort((w1, w2) -> {
                  int sort = Double.compare(PlayerUtils.distanceTo(w1.foot), PlayerUtils.distanceTo(w2.foot));
                  if (sort == 0) {
                     return 0;
                  } else {
                     return sort > 0 ? -1 : 1;
                  }
               });
            case Random:
               Collections.shuffle(this.withers);
            default:
               this.wither = this.withers.get(0);
         }
      }

      FindItemResult findSoulSand = InvUtils.findInHotbar(new Item[]{Items.SOUL_SAND});
      if (!findSoulSand.found()) {
         findSoulSand = InvUtils.findInHotbar(new Item[]{Items.SOUL_SOIL});
      }

      FindItemResult findWitherSkull = InvUtils.findInHotbar(new Item[]{Items.WITHER_SKELETON_SKULL});
      if (findSoulSand.found() && findWitherSkull.found()) {
         if ((Integer)this.blockDelay.get() == 0) {
            BlockUtils.place(this.wither.foot, findSoulSand, (Boolean)this.rotate.get(), -50);
            BlockUtils.place(this.wither.foot.above(), findSoulSand, (Boolean)this.rotate.get(), -50);
            BlockUtils.place(this.wither.foot.above().relative(this.wither.axis, -1), findSoulSand, (Boolean)this.rotate.get(), -50);
            BlockUtils.place(this.wither.foot.above().relative(this.wither.axis, 1), findSoulSand, (Boolean)this.rotate.get(), -50);
            BlockUtils.place(this.wither.foot.above().above(), findWitherSkull, (Boolean)this.rotate.get(), -50);
            BlockUtils.place(this.wither.foot.above().above().relative(this.wither.axis, -1), findWitherSkull, (Boolean)this.rotate.get(), -50);
            BlockUtils.place(this.wither.foot.above().above().relative(this.wither.axis, 1), findWitherSkull, (Boolean)this.rotate.get(), -50);
            this.wither = null;
            if ((Boolean)this.turnOff.get()) {
               this.toggle();
            }
         } else {
            if (this.blockTicksWaited < (Integer)this.blockDelay.get() - 1) {
               this.blockTicksWaited++;
               return;
            }

            switch (this.wither.stage) {
               case 0:
                  if (BlockUtils.place(this.wither.foot, findSoulSand, (Boolean)this.rotate.get(), -50)) {
                     this.wither.stage++;
                  }
                  break;
               case 1:
                  if (BlockUtils.place(this.wither.foot.above(), findSoulSand, (Boolean)this.rotate.get(), -50)) {
                     this.wither.stage++;
                  }
                  break;
               case 2:
                  if (BlockUtils.place(this.wither.foot.above().relative(this.wither.axis, -1), findSoulSand, (Boolean)this.rotate.get(), -50)) {
                     this.wither.stage++;
                  }
                  break;
               case 3:
                  if (BlockUtils.place(this.wither.foot.above().relative(this.wither.axis, 1), findSoulSand, (Boolean)this.rotate.get(), -50)) {
                     this.wither.stage++;
                  }
                  break;
               case 4:
                  if (BlockUtils.place(this.wither.foot.above().above(), findWitherSkull, (Boolean)this.rotate.get(), -50)) {
                     this.wither.stage++;
                  }
                  break;
               case 5:
                  if (BlockUtils.place(this.wither.foot.above().above().relative(this.wither.axis, -1), findWitherSkull, (Boolean)this.rotate.get(), -50)) {
                     this.wither.stage++;
                  }
                  break;
               case 6:
                  if (BlockUtils.place(this.wither.foot.above().above().relative(this.wither.axis, 1), findWitherSkull, (Boolean)this.rotate.get(), -50)) {
                     this.wither.stage++;
                  }
                  break;
               case 7:
                  this.wither = null;
                  this.blockTicksWaited = 0;
                  if ((Boolean)this.turnOff.get()) {
                     this.toggle();
                  }
            }
         }

         this.witherTicksWaited = 0;
      } else {
         this.error("Not enough resources in hotbar", new Object[0]);
         this.toggle();
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.wither != null) {
         event.renderer.box(this.wither.foot, (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
         event.renderer.box(this.wither.foot.above(), (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
         event.renderer
            .box(
               this.wither.foot.above().relative(this.wither.axis, -1),
               (Color)this.sideColor.get(),
               (Color)this.lineColor.get(),
               (ShapeMode)this.shapeMode.get(),
               0
            );
         event.renderer
            .box(
               this.wither.foot.above().relative(this.wither.axis, 1),
               (Color)this.sideColor.get(),
               (Color)this.lineColor.get(),
               (ShapeMode)this.shapeMode.get(),
               0
            );
         BlockPos midHead = this.wither.foot.above().above();
         BlockPos leftHead = this.wither.foot.above().above().relative(this.wither.axis, -1);
         BlockPos rightHead = this.wither.foot.above().above().relative(this.wither.axis, 1);
         event.renderer
            .box(
               midHead.getX() + 0.2,
               midHead.getX(),
               midHead.getX() + 0.2,
               midHead.getX() + 0.8,
               midHead.getX() + 0.7,
               midHead.getX() + 0.8,
               (Color)this.sideColor.get(),
               (Color)this.lineColor.get(),
               (ShapeMode)this.shapeMode.get(),
               0
            );
         event.renderer
            .box(
               leftHead.getX() + 0.2,
               leftHead.getX(),
               leftHead.getX() + 0.2,
               leftHead.getX() + 0.8,
               leftHead.getX() + 0.7,
               leftHead.getX() + 0.8,
               (Color)this.sideColor.get(),
               (Color)this.lineColor.get(),
               (ShapeMode)this.shapeMode.get(),
               0
            );
         event.renderer
            .box(
               rightHead.getX() + 0.2,
               rightHead.getX(),
               rightHead.getX() + 0.2,
               rightHead.getX() + 0.8,
               rightHead.getX() + 0.7,
               rightHead.getX() + 0.8,
               (Color)this.sideColor.get(),
               (Color)this.lineColor.get(),
               (ShapeMode)this.shapeMode.get(),
               0
            );
      }
   }

   private boolean isValidSpawn(BlockPos blockPos, Direction direction) {
      if (blockPos.getY() > 252) {
         return false;
      } else {
         int widthX = 0;
         int widthZ = 0;
         if (direction == Direction.EAST || direction == Direction.WEST) {
            widthZ = 1;
         }

         if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            widthX = 1;
         }

         MutableBlockPos bp = new MutableBlockPos();

         for (int x = blockPos.getX() - widthX; x <= blockPos.getX() + widthX; x++) {
            for (int z = blockPos.getZ() - widthZ; z <= blockPos.getZ(); z++) {
               for (int y = blockPos.getY(); y <= blockPos.getY() + 2; y++) {
                  bp.set(x, y, z);
                  if (!this.mc.level.getBlockState(bp).canBeReplaced()) {
                     return false;
                  }

                  if (!this.mc.level.isUnobstructed(Blocks.STONE.defaultBlockState(), bp, CollisionContext.empty())) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   public static enum Priority {
      Closest,
      Furthest,
      Random;
   }

   private static class Wither {
      public int stage;
      public MutableBlockPos foot = new MutableBlockPos();
      public Direction facing;
      public Axis axis;

      public AutoWither.Wither set(BlockPos pos, Direction dir) {
         this.stage = 0;
         this.foot.set(pos);
         this.facing = dir;
         this.axis = dir.getAxis();
         return this;
      }
   }
}
