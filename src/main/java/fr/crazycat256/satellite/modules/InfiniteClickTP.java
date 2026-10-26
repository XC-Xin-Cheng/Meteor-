package fr.crazycat256.satellite.modules;

import com.google.common.collect.Streams;
import fr.crazycat256.satellite.Addon;
import fr.crazycat256.satellite.utils.TPUtils;
import java.util.ArrayList;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class InfiniteClickTP extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRender = this.settings.createGroup("render");
   private final Setting<InfiniteClickTP.Mode> mode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("mode")).description("The mode of the teleportation.")).defaultValue(InfiniteClickTP.Mode.Paper))
            .build()
      );
   private final Setting<Integer> maxDistance = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("max-distance"))
                  .description("The maximum distance you can teleport."))
               .defaultValue(100))
            .min(0)
            .sliderRange(3, 200)
            .build()
      );
   private final Setting<Boolean> onlyIfSneaking = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("only-if-sneaking"))
                  .description("Only teleport if you are sneaking."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> preventWrongMoves = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("prevent-wrong-moves"))
                     .description("Tries to prevent teleports that would rollback."))
                  .defaultValue(true))
               .visible(() -> this.mode.get() != InfiniteClickTP.Mode.Pathfinder))
            .build()
      );
   private final Setting<Boolean> showBlock = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("show-block"))
                  .description("Shows the trail of your teleportation."))
               .defaultValue(true))
            .build()
      );
   private final Setting<SettingColor> blockColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("block-color"))
                  .description("The color of the block highlight."))
               .defaultValue(new SettingColor(0, 255, 255, 255))
               .visible(this.showBlock::get))
            .build()
      );
   private final Setting<SettingColor> wrongBlockColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("wrong-block-color"))
                  .description("The color of the block highlight if the move is wrong."))
               .defaultValue(new SettingColor(255, 0, 0, 255))
               .visible(() -> (Boolean)this.showBlock.get() && this.preventWrongMoves.isVisible() && (Boolean)this.preventWrongMoves.get()))
            .build()
      );
   private final Setting<Boolean> showTrail = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("show-trail"))
                  .description("Shows the trail of your teleportation."))
               .defaultValue(true))
            .build()
      );
   private final Setting<SettingColor> trailColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("trail-color"))
                  .description("The color of the block highlight."))
               .defaultValue(new SettingColor(255, 255, 255, 255))
               .visible(this.showTrail::get))
            .build()
      );
   private final Setting<Boolean> showSteps = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("show-steps"))
                  .description("Shows the trail of your teleportation."))
               .defaultValue(true))
            .build()
      );
   private final Setting<SettingColor> stepColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("box-color"))
                  .description("The color of the block highlight."))
               .defaultValue(new SettingColor(255, 156, 0, 96))
               .visible(this.showSteps::get))
            .build()
      );
   private final Setting<Integer> renderTime = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("trail-render-time"))
                  .description("The time in seconds the trail is rendered."))
               .defaultValue(3))
            .sliderRange(0, 10)
            .build()
      );
   private boolean cancel;
   private long lastTP;
   private final ArrayList<Vec3> positions = new ArrayList<>();
   private final MutableBlockPos blockPos = new MutableBlockPos();
   private boolean wrongMove = true;

   public InfiniteClickTP() {
      super(Addon.CATEGORY, "infinite-click-tp", "Teleports you to the block you click on.");
   }

   public void onActivate() {
      this.cancel = false;
      this.lastTP = 0L;
   }

   @EventHandler
   private void onPreTick(Pre event) {
      if (this.mc.player != null && this.mc.level != null) {
         if (this.mc.options.keyShift.isDown() || !(Boolean)this.onlyIfSneaking.get()) {
            ClipContext context = new ClipContext(
               this.mc.player.getEyePosition(),
               this.mc.player.getEyePosition().add(this.mc.player.getLookAngle().scale(((Integer)this.maxDistance.get()).intValue())),
               Block.COLLIDER,
               Fluid.ANY,
               this.mc.player
            );
            this.blockPos.set(this.mc.level.clip(context).getBlockPos());
            AABB box = this.mc.player.getBoundingBox().move(this.mc.player.position().reverse()).move(0.0, 1.0, 0.0);

            while (!Streams.stream(this.mc.level.getBlockCollisions(this.mc.player, box.move(Vec3.atBottomCenterOf(this.blockPos)))).toList().isEmpty()) {
               this.blockPos.set(this.blockPos.above());
            }

            Vec3 tpPos = Vec3.atBottomCenterOf(this.blockPos).add(0.0, 1.0, 0.0);
            Vec3 tpVec = tpPos.subtract(this.mc.player.position());
            this.wrongMove = false;
            if (this.mode.get() != InfiniteClickTP.Mode.Paper) {
               if (this.mode.get() == InfiniteClickTP.Mode.Straight) {
                  Vec3 tempPos = this.mc.player.position();

                  for (int i = 10; i < tpVec.length(); i += 10) {
                     Vec3 tempPos2 = tempPos.add(tpVec.normalize().scale(10.0));
                     if (!TPUtils.isTPValid(tempPos, tempPos2)) {
                        this.wrongMove = true;
                        break;
                     }
                  }
               }
            } else {
               this.wrongMove = this.mc.player.position().distanceToSqr(tpPos) > 40000.0 || TPUtils.isWrongMove(this.mc.player.position(), tpPos);
            }

            if (this.mode.get() != InfiniteClickTP.Mode.Pathfinder && (Boolean)this.preventWrongMoves.get() && this.wrongMove) {
               return;
            }

            if (this.mc.options.keyUse.isDown() && !this.cancel) {
               this.positions.clear();
               this.positions.add(this.mc.player.position());
               switch ((InfiniteClickTP.Mode)this.mode.get()) {
                  case Paper:
                     TPUtils.PaperTP(tpPos);
                     this.positions.add(tpPos);
                     break;
                  case Straight:
                     for (int ix = 10; ix < tpVec.length(); ix += 10) {
                        Vec3 vec = this.mc.player.position().add(tpVec.normalize().scale(ix));
                        this.mc.player.connection.send(new Pos(vec.x, vec.y, vec.z, true, this.mc.player.horizontalCollision));
                        this.positions.add(vec);
                     }

                     this.mc.player.connection.send(new Pos(tpPos.x, tpPos.y, tpPos.z, true, this.mc.player.horizontalCollision));
                     this.positions.add(tpPos);
                     break;
                  case Pathfinder:
                     ArrayList<Vec3> steps = TPUtils.findTPPath(this.mc.player.position(), tpPos, 20, 0.0, 9.0);
                     if (steps != null) {
                        this.positions.addAll(steps);

                        for (Vec3 vec : steps) {
                           this.mc.player.connection.send(new Pos(vec.x, vec.y, vec.z, true, this.mc.player.horizontalCollision));
                        }

                        this.mc.player.connection.send(new Pos(tpPos.x, tpPos.y, tpPos.z, true, this.mc.player.horizontalCollision));
                     }
               }

               this.mc.player.setPos(tpPos.x, tpPos.y, tpPos.z);
               this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
               this.cancel = true;
               this.lastTP = System.currentTimeMillis();
            }
         }

         if (!this.mc.options.keyUse.isDown()) {
            this.cancel = false;
         }
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if ((this.mc.options.keyShift.isDown() || !(Boolean)this.onlyIfSneaking.get()) && (Boolean)this.showBlock.get()) {
         Color color;
         if (this.mode.get() != InfiniteClickTP.Mode.Pathfinder && this.wrongMove && (Boolean)this.preventWrongMoves.get()) {
            color = (Color)this.wrongBlockColor.get();
         } else {
            color = (Color)this.blockColor.get();
         }

         double x1 = this.blockPos.getX();
         double x2 = this.blockPos.getX() + 1;
         double y = this.blockPos.getY() + 1;
         double z1 = this.blockPos.getZ();
         double z2 = this.blockPos.getZ() + 1;
         event.renderer.line(x1, y, z1, x2, y, z1, color);
         event.renderer.line(x2, y, z1, x2, y, z2, color);
         event.renderer.line(x2, y, z2, x1, y, z2, color);
         event.renderer.line(x1, y, z2, x1, y, z1, color);
      }

      if (this.lastTP + (Integer)this.renderTime.get() * 1000 >= System.currentTimeMillis()) {
         if ((Boolean)this.showTrail.get()) {
            for (int i = 0; i < this.positions.size() - 1; i++) {
               Vec3 p1 = this.positions.get(i);
               Vec3 p2 = this.positions.get(i + 1);
               event.renderer.line(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, (Color)this.trailColor.get());
            }
         }

         if ((Boolean)this.showSteps.get()) {
            for (Vec3 vec : this.positions) {
               event.renderer
                  .box(
                     vec.x - 0.375,
                     vec.y,
                     vec.z - 0.375,
                     vec.x + 0.375,
                     vec.y + 2.0,
                     vec.z + 0.375,
                     (Color)this.stepColor.get(),
                     (Color)this.stepColor.get(),
                     ShapeMode.Sides,
                     0
                  );
            }
         }
      }
   }

   public static enum Mode {
      Paper,
      Straight,
      Pathfinder;
   }
}
