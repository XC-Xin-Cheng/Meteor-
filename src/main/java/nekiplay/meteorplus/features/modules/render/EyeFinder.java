package nekiplay.meteorplus.features.modules.render;

import java.util.HashMap;
import java.util.Iterator;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EyeFinder extends Module {
   private final HashMap<Entity, HitResult> resultMap = new HashMap<>();
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgBlock = this.settings.createGroup("Block");
   private final Setting<ShapeMode> shapeMode = this.sgBlock
      .add(((Builder)((Builder)((Builder)new Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
   private final Setting<SettingColor> lineColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("line-color"))
               .description("The line color."))
            .defaultValue(new SettingColor(255, 255, 255, 255))
            .build()
      );
   private final Setting<SettingColor> sideColor = this.sgBlock
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("side-color"))
               .description("The side color."))
            .defaultValue(new SettingColor(255, 255, 255, 50))
            .build()
      );

   public EyeFinder() {
      super(Categories.Render, "eye-finder", "Find block player look.");
   }

   public void drawLine(Render3DEvent event, Entity entity, HitResult result) {
      event.renderer
         .line(
            entity.getEyePosition().x,
            entity.getEyePosition().y,
            entity.getEyePosition().z,
            result.getLocation().x,
            result.getLocation().y,
            result.getLocation().z,
            (Color)this.lineColor.get()
         );
   }

   public void renderBlock(Render3DEvent event, Entity entity, HitResult result) {
      if (result instanceof BlockHitResult blockHitResult && (blockHitResult.getType() == Type.BLOCK || blockHitResult.getType() == Type.MISS)) {
         BlockPos bp = new BlockPos(blockHitResult.getBlockPos());
         BlockState state = this.mc.level.getBlockState(bp);
         Direction side = blockHitResult.getDirection();
         VoxelShape shape = state.getShape(this.mc.level, bp);
         if (shape.isEmpty()) {
            return;
         }

         AABB box = shape.bounds();
         if (side == Direction.UP || side == Direction.DOWN) {
            event.renderer
               .sideHorizontal(
                  bp.getX() + box.minX,
                  bp.getY() + (side == Direction.DOWN ? box.minY : box.maxY),
                  bp.getZ() + box.minZ,
                  bp.getX() + box.maxX,
                  bp.getZ() + box.maxZ,
                  (Color)this.sideColor.get(),
                  (Color)this.lineColor.get(),
                  (ShapeMode)this.shapeMode.get()
               );
         } else if (side != Direction.SOUTH && side != Direction.NORTH) {
            double x = side == Direction.WEST ? box.minX : box.maxX;
            event.renderer
               .sideVertical(
                  bp.getX() + x,
                  bp.getY() + box.minY,
                  bp.getZ() + box.minZ,
                  bp.getX() + x,
                  bp.getY() + box.maxY,
                  bp.getZ() + box.maxZ,
                  (Color)this.sideColor.get(),
                  (Color)this.lineColor.get(),
                  (ShapeMode)this.shapeMode.get()
               );
         } else {
            double z = side == Direction.NORTH ? box.minZ : box.maxZ;
            event.renderer
               .sideVertical(
                  bp.getX() + box.minX,
                  bp.getY() + box.minY,
                  bp.getZ() + z,
                  bp.getX() + box.maxX,
                  bp.getY() + box.maxY,
                  bp.getZ() + z,
                  (Color)this.sideColor.get(),
                  (Color)this.lineColor.get(),
                  (ShapeMode)this.shapeMode.get()
               );
         }
      }
   }

   @EventHandler
   public void tickEvent(Pre event) {
      if (this.mc.level != null) {
         Iterator<Entity> entityIterator = this.mc.level.entitiesForRendering().iterator();
         HashMap<Entity, HitResult> cachMap = new HashMap<>();

         while (entityIterator.hasNext()) {
            Entity entity = entityIterator.next();
            if (entity instanceof Player && entity != this.mc.player) {
               HitResult result = entity.pick(5.0, this.mc.getDeltaTracker().getGameTimeDeltaPartialTick(true), false);
               cachMap.put(entity, result);
            }
         }

         this.resultMap.clear();
         this.resultMap.putAll(cachMap);
      }
   }

   @EventHandler
   public void on3dRender(Render3DEvent event) {
      for (Entity entity : this.resultMap.keySet()) {
         HitResult r = this.resultMap.get(entity);
         this.drawLine(event, entity, r);
         this.renderBlock(event, entity, r);
      }
   }
}
