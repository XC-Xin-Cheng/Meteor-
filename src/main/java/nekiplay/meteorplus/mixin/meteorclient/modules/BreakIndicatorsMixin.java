package nekiplay.meteorplus.mixin.meteorclient.modules;

import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.mixin.LevelRendererAccessor;
import meteordevelopment.meteorclient.mixin.MultiPlayerGameModeAccessor;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.systems.modules.world.PacketMine;
import meteordevelopment.meteorclient.systems.modules.world.PacketMine.MyBlock;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({BreakIndicators.class})
public class BreakIndicatorsMixin extends Module {
   @Unique
   private final SettingGroup sgPercentageRenderPlus = this.settings.createGroup("Meteor+ Percentage Render");
   @Unique
   public final Setting<Boolean> percentageRender = this.sgPercentageRenderPlus
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("enable-percentage-render")).description("Enable percentage text render.")).defaultValue(true))
            .build()
      );
   @Unique
   public final Setting<Boolean> packetMine = this.sgPercentageRenderPlus
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("packet-mine")).description("Render packet mine blocks.")).defaultValue(true))
               .visible(this.percentageRender::get))
            .build()
      );
   @Unique
   private final Setting<SettingColor> percentageColor = this.sgPercentageRenderPlus
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("percentage-color"))
                  .description("The color for the percentage text."))
               .defaultValue(new SettingColor(25, 252, 25, 150))
               .visible(this.percentageRender::get))
            .build()
      );

   public BreakIndicatorsMixin(Category category, String name, String description, String... aliases) {
      super(category, name, description, aliases);
   }

   @Unique
   @EventHandler
   private void on2DRender(Render2DEvent event) {
      Map<Integer, BlockDestructionProgress> blocks = ((LevelRendererAccessor)this.mc.levelRenderer).meteor$getDestroyingBlocks();
      float ownBreakingStage = ((MultiPlayerGameModeAccessor)this.mc.gameMode).meteor$getBreakingProgress();
      BlockPos ownBreakingPos = ((MultiPlayerGameModeAccessor)this.mc.gameMode).meteor$getCurrentBreakingBlockPos();
      if (ownBreakingPos != null && ownBreakingStage > 0.0F) {
         double shrinkFactor = 1.0 - ownBreakingStage;
         BlockState state = this.mc.level.getBlockState(ownBreakingPos);
         VoxelShape shape = state.getShape(this.mc.level, ownBreakingPos);
         if (shape == null || shape.isEmpty()) {
            return;
         }

         AABB orig = shape.bounds();
         this.renderBlock(event, ownBreakingPos, shrinkFactor, orig);
      }

      blocks.values().forEach(info -> {
         BlockPos pos = info.getPos();
         int stage = info.getProgress();
         if (!pos.equals(ownBreakingPos)) {
            BlockState statex = this.mc.level.getBlockState(pos);
            VoxelShape shapex = statex.getShape(this.mc.level, pos);
            if (shapex != null && !shapex.isEmpty()) {
               AABB origx = shapex.bounds();
               double shrinkFactorx = (9 - (stage + 1)) / 9.0;
               double progress = 1.0 - shrinkFactorx;
               this.renderBlock(event, pos, shrinkFactorx, origx);
            }
         }
      });
      if ((Boolean)this.packetMine.get() && !((PacketMine)Modules.get().get(PacketMine.class)).blocks.isEmpty()) {
         this.renderPacket(event, ((PacketMine)Modules.get().get(PacketMine.class)).blocks);
      }
   }

   @Unique
   private void renderBlock(Render2DEvent event, BlockPos pos, double shrinkFactor, AABB orig) {
      Vector3d vector3d = new Vector3d(pos.getX() + orig.getCenter().x, pos.getY() + orig.getCenter().y, pos.getZ() + orig.getCenter().z);
      if ((Boolean)this.percentageRender.get() && NametagUtils.to2D(vector3d, 1.0, true)) {
         TextRenderer text = TextRenderer.get();
         NametagUtils.begin(vector3d, event.graphics);
         text.beginBig();
         String label = String.format("%1$,.0f", shrinkFactor * 100.0) + "%";
         double hologramWidth = text.getWidth(label, true);
         double heightDown = text.getHeight(true);
         double widthHalf = hologramWidth / 2.0;
         double hX = -widthHalf;
         double hY = -heightDown;
         text.render(label, hX, hY, (Color)this.percentageColor.get(), true);
         text.end();
         NametagUtils.end(event.graphics);
      }
   }

   @Unique
   private void renderPacket(Render2DEvent event, List<MyBlock> blocks) {
      for (MyBlock block : blocks) {
         if (block.mining && block.progress() != Double.POSITIVE_INFINITY) {
            VoxelShape shape = block.blockState.getShape(this.mc.level, block.blockPos);
            if (shape == null || shape.isEmpty()) {
               return;
            }

            AABB orig = shape.bounds();
            double progressNormalised = Math.min(1.0, block.progress());
            double shrinkFactor = 1.0 - progressNormalised;
            BlockPos pos = block.blockPos;
            this.renderBlock(event, pos, shrinkFactor, orig);
         }
      }
   }
}
