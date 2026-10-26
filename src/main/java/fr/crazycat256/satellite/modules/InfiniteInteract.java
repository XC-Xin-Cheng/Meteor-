package fr.crazycat256.satellite.modules;

import fr.crazycat256.satellite.Addon;
import fr.crazycat256.satellite.utils.TPUtils;
import java.util.ArrayList;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class InfiniteInteract extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<Boolean> notIfSneaking = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("not-if-sneaking")).description("Will not interact if you are sneaking.")).defaultValue(true))
            .build()
      );
   private final Setting<Boolean> hitEntities = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("hit-entities")).description("Allows you to hit entities at a distance.")).defaultValue(true))
            .build()
      );
   private final Setting<Boolean> interactEntities = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("interact-entities")).description("Allows you to interact with entities at a distance."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> breakBlocks = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("break-blocks")).description("Allows you to break blocks at a distance.")).defaultValue(true))
            .build()
      );
   private final Setting<Boolean> interactBlocks = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("interact-blocks")).description("Allows you to interact with blocks at a distance."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> showTrail = this.sgRender
      .add(((Builder)((Builder)((Builder)new Builder().name("show-trail")).description("Shows the trail of your teleportation.")).defaultValue(true)).build());
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
      .add(((Builder)((Builder)((Builder)new Builder().name("show-steps")).description("Shows the boxes of your teleportation.")).defaultValue(false)).build());
   private final Setting<SettingColor> boxColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("box-color"))
                  .description("The color of the block highlight."))
               .defaultValue(new SettingColor(255, 156, 0, 96))
               .visible(this.showSteps::get))
            .build()
      );
   private final Setting<Integer> trailRenderTime = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("trail-render-time"))
                  .description("The time in seconds the trail is rendered."))
               .defaultValue(3))
            .sliderRange(0, 10)
            .build()
      );
   private final ArrayList<Vec3> positions = new ArrayList<>();
   private long lastTP = 0L;
   private Vec3 playerPos = null;
   private Vec3 tpBackPos = null;

   public InfiniteInteract() {
      super(Addon.CATEGORY, "infinite-interact", "Allows you to interact with blocks and entities at a distance. (require reach module)");
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.tpBackPos != null) {
         this.tpBack(this.tpBackPos, this.playerPos);
         this.tpBackPos = null;
      }
   }

   @EventHandler
   private void onAttack(AttackEntityEvent event) {
      if (this.mc.player != null && (Boolean)this.hitEntities.get()) {
         if (!(Boolean)this.notIfSneaking.get() || !this.mc.player.isShiftKeyDown()) {
            Entity entity = event.entity;
            Vec3 entityPos = entity.position();
            if (!(this.mc.player.distanceTo(entity) <= 3.0F)) {
               Vec3 playerPos = this.mc.player.position();
               Vec3 tempPos = this.tpTo(entityPos);
               this.mc.player.connection.send(new ServerboundAttackPacket(entity.getId()));
               event.setCancelled(true);
               this.mc.player.resetAttackStrengthTicker();
               this.tpBack(tempPos, playerPos);
               this.lastTP = System.currentTimeMillis();
            }
         }
      }
   }

   @EventHandler
   private void onInteractBlock(InteractBlockEvent event) {
      if (this.mc.player != null && (Boolean)this.interactBlocks.get()) {
         if (!(Boolean)this.notIfSneaking.get() || !this.mc.player.isShiftKeyDown()) {
            Vec3 blockPos = event.result.getLocation();
            if (!(blockPos.distanceTo(this.mc.player.position()) <= 5.0)) {
               this.lastTP = System.currentTimeMillis();
            }
         }
      }
   }

   @EventHandler
   private void onInteractEntity(InteractEntityEvent event) {
      if (this.mc.player != null && this.mc.gameMode != null && (Boolean)this.interactEntities.get()) {
         if (!(Boolean)this.notIfSneaking.get() || !this.mc.player.isShiftKeyDown()) {
            Entity entity = event.entity;
            Vec3 entityPos = entity.position();
            if (!(this.mc.player.distanceTo(entity) <= 3.0F)) {
               Vec3 playerPos = this.mc.player.position();
               Vec3 tempPos = this.tpTo(entityPos);
               this.mc.gameMode.interact(this.mc.player, entity, new EntityHitResult(entity, entity.getBoundingBox().getCenter()), InteractionHand.MAIN_HAND);
               event.setCancelled(true);
               this.tpBack(tempPos, playerPos);
               this.lastTP = System.currentTimeMillis();
            }
         }
      }
   }

   @EventHandler
   private void onStartBreakingBlock(StartBreakingBlockEvent event) {
      if (this.mc.player != null && (Boolean)this.breakBlocks.get()) {
         if (!(Boolean)this.notIfSneaking.get() || !this.mc.player.isShiftKeyDown()) {
            Vec3 blockPos = new Vec3(event.blockPos.getX(), event.blockPos.getY(), event.blockPos.getZ());
            if (!(blockPos.distanceTo(this.mc.player.position()) <= 5.0)) {
               Vec3 playerPos = this.mc.player.position();
               Vec3 tempPos = this.tpTo(blockPos);
               this.mc.player.connection.send(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, event.blockPos, event.direction));
               this.tpBack(tempPos, playerPos);
               this.lastTP = System.currentTimeMillis();
            }
         }
      }
   }

   @EventHandler
   private void onBreakBlock(BreakBlockEvent event) {
      if (this.mc.player != null && (Boolean)this.breakBlocks.get()) {
         if (!(Boolean)this.notIfSneaking.get() || !this.mc.player.isShiftKeyDown()) {
            Vec3 blockPos = new Vec3(event.blockPos.getX(), event.blockPos.getY(), event.blockPos.getZ());
            if (!(blockPos.distanceTo(this.mc.player.position()) <= 5.0)) {
               Vec3 playerPos = this.mc.player.position();
               Vec3 tempPos = this.tpTo(blockPos);
               this.mc.player.connection.send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, event.blockPos, Direction.UP));
               this.tpBack(tempPos, playerPos);
               this.lastTP = System.currentTimeMillis();
            }
         }
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.lastTP + (Integer)this.trailRenderTime.get() * 1000 >= System.currentTimeMillis()) {
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
                     (Color)this.boxColor.get(),
                     (Color)this.boxColor.get(),
                     ShapeMode.Sides,
                     0
                  );
            }
         }
      }
   }

   private void sendPositionPacket(double x, double y, double z) {
      this.mc.player.connection.send(new Pos(x, y, z, true, this.mc.player.horizontalCollision));
   }

   private Vec3 tpTo(Vec3 tpPos) {
      Vec3 playerPos = this.mc.player.position();
      ArrayList<Vec3> path = TPUtils.findTPPath(playerPos, tpPos, 20, 3.0, 8.0);
      if (path == null) {
         return tpPos;
      } else {
         this.positions.clear();
         this.positions.add(playerPos);

         for (int i = 0; i < 20 - path.size(); i++) {
            this.mc.player.connection.send(new StatusOnly(true, this.mc.player.horizontalCollision));
         }

         for (Vec3 vec : path) {
            this.sendPositionPacket(vec.x, vec.y, vec.z);
            this.positions.add(vec);
         }

         Vec3 endPos = path.getLast();
         this.sendPositionPacket(endPos.x, endPos.y, endPos.z);
         this.playerPos = playerPos;
         this.tpBackPos = endPos;
         return endPos;
      }
   }

   private void tpBack(Vec3 tempPos, Vec3 initialPos) {
      ArrayList<Vec3> path = TPUtils.findTPPath(tempPos, initialPos, 10, 0.0, 9.0);
      if (path != null) {
         for (int i = 0; i < 20 - path.size(); i++) {
            this.mc.player.connection.send(new StatusOnly(true, this.mc.player.horizontalCollision));
         }

         for (Vec3 vec : path) {
            this.sendPositionPacket(vec.x, vec.y, vec.z);
         }

         this.sendPositionPacket(initialPos.x, initialPos.y, initialPos.z);
         this.playerPos = null;
         this.tpBackPos = null;
      }
   }
}
