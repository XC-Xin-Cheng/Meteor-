package zgoly.meteorist.modules;

import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import zgoly.meteorist.Meteorist;

public class AutoSneak extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgFilter = this.settings.createGroup("Filter");
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<Double> width = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("width")).description("Width of the box.")).defaultValue(0.25).range(0.05, 1.0).sliderRange(0.05, 1.0).build()
      );
   private final Setting<Double> height = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("height")).description("Height of the box.")).defaultValue(0.6).range(0.05, 1.0).sliderRange(0.05, 1.0).build()
      );
   private final Setting<Double> playerPosPrediction = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("player-pos-prediction")).description("Predict player position based on velocity to move box to it."))
            .defaultValue(1.0)
            .sliderRange(0.0, 5.0)
            .build()
      );
   private final Setting<AutoSneak.SneakBlocksMode> sneakBlocksMode = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("sneak-blocks-mode"))
                  .description("Sneak blocks mode."))
               .defaultValue(AutoSneak.SneakBlocksMode.Whitelist))
            .build()
      );
   private final Setting<List<Block>> sneakBlocksWhitelist = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                     .name("sneak-blocks-whitelist"))
                  .description("Sneak on blocks from list, but not others."))
               .visible(() -> this.sneakBlocksMode.get() == AutoSneak.SneakBlocksMode.Whitelist))
            .build()
      );
   private final Setting<List<Block>> sneakBlocksBlacklist = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                     .name("sneak-blocks-blacklist"))
                  .description("Sneak on other blocks, but not from list."))
               .visible(() -> this.sneakBlocksMode.get() == AutoSneak.SneakBlocksMode.Blacklist))
            .build()
      );
   private final Setting<AutoSneak.IgnoreBlocksMode> ignoreBlocksMode = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("ignore-blocks-mode"))
                  .description("Ignore blocks mode."))
               .defaultValue(AutoSneak.IgnoreBlocksMode.Whitelist))
            .build()
      );
   private final Setting<List<Block>> ignoreBlocksWhitelist = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                     .name("ignore-blocks-whitelist"))
                  .description("Ignore blocks from list, but not others."))
               .visible(() -> this.ignoreBlocksMode.get() == AutoSneak.IgnoreBlocksMode.Whitelist))
            .build()
      );
   private final Setting<List<Block>> ignoreBlocksBlacklist = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                     .name("ignore-blocks-blacklist"))
                  .description("Ignore other blocks, but not from list."))
               .visible(() -> this.ignoreBlocksMode.get() == AutoSneak.IgnoreBlocksMode.Blacklist))
            .build()
      );
   private final Setting<Boolean> showBox = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("show-box"))
                  .description("Show box."))
               .defaultValue(true))
            .build()
      );
   private final Setting<SettingColor> sideColorOff = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("side-color-off"))
                  .description("The color of the sides of box when not sneaking."))
               .defaultValue(new SettingColor(255, 0, 0, 40))
               .visible(this.showBox::get))
            .build()
      );
   private final Setting<SettingColor> lineColorOff = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("line-color-off"))
                  .description("The color of the lines of box when not sneaking."))
               .defaultValue(new SettingColor(255, 0, 0, 100))
               .visible(this.showBox::get))
            .build()
      );
   private final Setting<SettingColor> sideColorOn = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("side-color-on"))
                  .description("The color of the sides of box when sneaking."))
               .defaultValue(new SettingColor(0, 255, 0, 40))
               .visible(this.showBox::get))
            .build()
      );
   private final Setting<SettingColor> lineColorOn = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("line-color-on"))
                  .description("The color of the lines of box when sneaking."))
               .defaultValue(new SettingColor(0, 255, 0, 100))
               .visible(this.showBox::get))
            .build()
      );
   boolean sneaking = false;

   public AutoSneak() {
      super(Meteorist.CATEGORY, "auto-sneak", "Automatically sneaks at block edge (idea by kokqi).");
   }

   private AABB calcBox() {
      assert this.mc.player != null;

      Vec3 pos = this.mc.player.position().add(this.mc.player.getDeltaMovement().scale((Double)this.playerPosPrediction.get()));
      pos = new Vec3(pos.x, this.mc.player.position().y, pos.z);
      return new AABB(
         pos.x() + (Double)this.width.get() / 2.0,
         pos.y(),
         pos.z() + (Double)this.width.get() / 2.0,
         pos.x() - (Double)this.width.get() / 2.0,
         pos.y() - (Double)this.height.get(),
         pos.z() - (Double)this.width.get() / 2.0
      );
   }

   public void onDeactivate() {
      this.sneaking = false;
      if (this.mc.player != null) {
         this.mc.player.setShiftKeyDown(false);
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      if ((this.mc.player.getAbilities().flying || this.mc.player.fallDistance > 0.0) && this.sneaking) {
         this.mc.options.keyShift.setDown(false);
         this.sneaking = false;
      }

      if (this.mc.player.onGround()) {
         AABB box = this.calcBox();
         Iterable<VoxelShape> iterable = this.mc.level.getBlockCollisions(this.mc.player, box);
         if (iterable.iterator().hasNext()) {
            iterable.forEach(blockBox -> {
               BlockPos blockPos = BlockPos.containing(blockBox.min(Axis.X), blockBox.min(Axis.Y), blockBox.min(Axis.Z));
               if (this.mc.level.getBlockState(blockPos) == this.mc.player.getBlockStateOn()) {
                  if (this.sneaking) {
                     this.mc.options.keyShift.setDown(false);
                     this.sneaking = false;
                  }
               } else if (this.mc.level.getBlockState(blockPos).canBeReplaced()) {
                  this.mc.options.keyShift.setDown(true);
                  this.sneaking = true;
               }
            });
         } else {
            this.mc.options.keyShift.setDown(true);
            this.sneaking = true;
         }

         Block steppingBlock = this.mc.player.getBlockStateOn().getBlock();
         boolean shouldSneak = this.sneakBlocksMode.get() == AutoSneak.SneakBlocksMode.Whitelist
            ? ((List)this.sneakBlocksWhitelist.get()).contains(steppingBlock)
            : !((List)this.sneakBlocksBlacklist.get()).contains(steppingBlock);
         boolean shouldIgnore = this.ignoreBlocksMode.get() == AutoSneak.IgnoreBlocksMode.Whitelist
            ? ((List)this.ignoreBlocksWhitelist.get()).contains(steppingBlock)
            : !((List)this.ignoreBlocksBlacklist.get()).contains(steppingBlock);
         if (shouldSneak) {
            this.mc.options.keyShift.setDown(true);
            this.sneaking = true;
         }

         if (shouldIgnore) {
            this.mc.options.keyShift.setDown(false);
            this.sneaking = false;
         }
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if ((Boolean)this.showBox.get()) {
         if (this.mc.player == null) {
            return;
         }

         event.renderer
            .box(
               this.calcBox(),
               this.sneaking ? (Color)this.sideColorOn.get() : (Color)this.sideColorOff.get(),
               this.sneaking ? (Color)this.lineColorOn.get() : (Color)this.lineColorOff.get(),
               ShapeMode.Both,
               0
            );
      }
   }

   public static enum IgnoreBlocksMode {
      Whitelist,
      Blacklist;
   }

   public static enum SneakBlocksMode {
      Whitelist,
      Blacklist;
   }
}
