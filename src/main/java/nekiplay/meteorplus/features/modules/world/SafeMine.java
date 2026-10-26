package nekiplay.meteorplus.features.modules.world;

import java.util.ArrayList;
import java.util.Iterator;
import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.Shapes;

public class SafeMine extends Module {
   private final SettingGroup ALSettings = this.settings.getDefaultGroup();
   private final SettingGroup FSettings = this.settings.createGroup("Freeze Settings");
   public final Setting<Boolean> solidLava = this.ALSettings
      .add(((Builder)((Builder)((Builder)new Builder().name("Solid lava")).description("Solid lava.")).defaultValue(true)).build());
   public final Setting<Boolean> solidLavaFreeze = this.ALSettings
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("Solid lava freeze")).description("Solid lava.")).defaultValue(true))
               .visible(this.solidLava::get))
            .build()
      );
   public final Setting<Boolean> antiMine = this.ALSettings
      .add(((Builder)((Builder)((Builder)new Builder().name("Anti lava mine")).description("Block mine block is nearbly lava.")).defaultValue(true)).build());
   public final Setting<Boolean> replaceLava = this.ALSettings
      .add(((Builder)((Builder)((Builder)new Builder().name("Replace lava")).description("Place blocks in lava in offhand.")).defaultValue(true)).build());
   private final Setting<Integer> delay = this.ALSettings
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("Replace delay"))
                     .description("Delay for replace lava."))
                  .defaultValue(0))
               .min(0)
               .visible(this.replaceLava::get))
            .sliderRange(0, 20)
            .build()
      );
   ArrayList<BlockPos> lava = new ArrayList<>();
   private Integer tick = 0;
   private boolean freeze = false;
   private final Setting<Boolean> FreezeLook = this.FSettings
      .add(((Builder)((Builder)((Builder)new Builder().name("Freeze look")).description("Freezes your pitch and yaw.")).defaultValue(false)).build());
   private final Setting<Boolean> Packet = this.FSettings
      .add(((Builder)((Builder)((Builder)new Builder().name("Packet mode")).description("Enable packet mode, better.")).defaultValue(true)).build());
   private final Setting<Boolean> FreezeLookSilent = this.FSettings
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("Freeze look silent")).description("Freezes your pitch and yaw silent."))
                  .defaultValue(true))
               .visible(this.Packet::get))
            .build()
      );
   private final Setting<Boolean> FreezeLookPlace = this.FSettings
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("Freeze look place support")).description("Unfreeze you yaw and pitch on place"))
                  .defaultValue(false))
               .visible(this.FreezeLookSilent::get))
            .build()
      );
   private float yaw = 0.0F;
   private float pitch = 0.0F;
   private Vec3 position = Vec3.ZERO;
   private boolean rotate = false;

   public SafeMine() {
      super(Categories.World, "Safe Mine", "Save you from lava.");
   }

   @EventHandler
   private void onTickEvent(Post event) {
      if (this.mc.hitResult != null && this.mc.hitResult.getType() == Type.BLOCK && (Boolean)this.replaceLava.get()) {
         synchronized (this.lava) {
            Iterator<BlockPos> iterator = this.lava.iterator();
            if (iterator.hasNext()) {
               if (this.tick == 0) {
                  if (this.mc.player != null) {
                     BlockPos block = iterator.next();
                     BlockUtils.place(block, InteractionHand.OFF_HAND, this.mc.player.getInventory().getSelectedSlot(), false, 0, false, false, false);
                     iterator.remove();
                     this.tick = (Integer)this.delay.get();
                  }
               } else {
                  Integer var7 = this.tick;
                  this.tick = this.tick - 1;
               }
            }
         }
      }
   }

   @EventHandler
   private void onCanContactLava(Post event) {
      if (this.mc.player != null && this.mc.level != null) {
         Vec3 underpos = this.mc.player.position().add(0.0, -1.0, 0.0);
         BlockPos under = new BlockPos((int)underpos.x, (int)underpos.y, (int)underpos.z);
         if (this.mc.level.getBlockState(under).is(Blocks.LAVA)) {
            if ((Boolean)this.solidLavaFreeze.get() && this.mc.player.onGround() && !this.freeze) {
               this.freeze = true;
               this.yaw = this.mc.player.getYRot();
               this.pitch = this.mc.player.getXRot();
               this.position = this.mc.player.position();
            }
         } else {
            this.freeze = false;
         }
      }
   }

   @EventHandler
   private void onCanWalkOnFluid(CanWalkOnFluidEvent event) {
      if ((event.fluidState.getType() == Fluids.LAVA || event.fluidState.getType() == Fluids.FLOWING_LAVA) && (Boolean)this.solidLava.get()) {
         event.walkOnFluid = true;
         if ((Boolean)this.solidLavaFreeze.get()) {
            this.freeze = true;
         }
      }
   }

   @EventHandler
   private void onFluidCollisionShape(CollisionShapeEvent event) {
      if (!event.state.getFluidState().isEmpty()
         && this.mc.player != null
         && event.state != null
         && event.state.is(Blocks.LAVA)
         && !this.mc.player.isInLava()
         && (Boolean)this.solidLava.get()) {
         event.shape = Shapes.block();
      }
   }

   @EventHandler(
      priority = 100
   )
   private void onStartBreakingBlock(StartBreakingBlockEvent event) {
      ArrayList<BlockPos> lavaBlocks = this.isExposedLava(event.blockPos);
      if (!lavaBlocks.isEmpty() && (Boolean)this.antiMine.get()) {
         this.mc.options.keyAttack.setDown(false);
         event.setCancelled(true);
         synchronized (this.lava) {
            this.lava = this.isExposedLava(event.blockPos);
         }
      }
   }

   private ArrayList<BlockPos> isExposedLava(BlockPos pos) {
      ArrayList<BlockPos> blocks = new ArrayList<>();
      if (this.mc.level != null) {
         if (this.mc.level.getBlockState(pos).is(Blocks.LAVA)) {
            blocks.add(pos);
         }

         if (this.mc.level.getBlockState(pos.offset(1, 0, 0)).is(Blocks.LAVA)) {
            blocks.add(pos.offset(1, 0, 0));
         }

         if (this.mc.level.getBlockState(pos.offset(-1, 0, 0)).is(Blocks.LAVA)) {
            blocks.add(pos.offset(-1, 0, 0));
         }

         if (this.mc.level.getBlockState(pos.offset(0, 1, 0)).is(Blocks.LAVA)) {
            blocks.add(pos.offset(0, 1, 0));
         }

         if (this.mc.level.getBlockState(pos.offset(0, -1, 0)).is(Blocks.LAVA)) {
            blocks.add(pos.offset(0, -1, 0));
         }

         if (this.mc.level.getBlockState(pos.offset(0, 0, 1)).is(Blocks.LAVA)) {
            blocks.add(pos.offset(0, 0, 1));
         }

         if (this.mc.level.getBlockState(pos.offset(0, 0, -1)).is(Blocks.LAVA)) {
            blocks.add(pos.offset(0, 0, -1));
         }
      }

      return blocks;
   }

   public void onActivate() {
      if (this.mc.player != null) {
         this.yaw = this.mc.player.getYRot();
         this.pitch = this.mc.player.getXRot();
         this.position = this.mc.player.position();
      }
   }

   private void setFreezeLook(Send event, ServerboundMovePlayerPacket playerMove) {
      if (playerMove.hasRotation() && (Boolean)this.FreezeLook.get() && (Boolean)this.FreezeLookSilent.get() && !this.rotate) {
         event.setCancelled(true);
      } else if (this.mc.player != null && playerMove.hasRotation() && (Boolean)this.FreezeLook.get() && !(Boolean)this.FreezeLookSilent.get()) {
         event.setCancelled(true);
         this.mc.player.setYRot(this.yaw);
         this.mc.player.setXRot(this.pitch);
      }

      if (this.mc.player != null && playerMove.hasPosition()) {
         this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         this.mc.player.setPosRaw(this.position.x, this.position.y, this.position.z);
         event.setCancelled(true);
      }
   }

   @EventHandler
   private void InteractBlockEvent(InteractBlockEvent event) {
      if (this.mc.player != null && this.mc.getConnection() != null && (Boolean)this.FreezeLookPlace.get() && this.freeze) {
         Rot r = new Rot(this.mc.player.getYRot(), this.mc.player.getXRot(), this.mc.player.onGround(), this.mc.player.horizontalCollision);
         this.rotate = true;
         this.mc.getConnection().send(r);
         this.rotate = false;
      }
   }

   @EventHandler
   private void onMovePacket(Send event) {
      if (this.freeze && event.packet instanceof ServerboundMovePlayerPacket playerMove && (Boolean)this.Packet.get()) {
         this.setFreezeLook(event, playerMove);
      }
   }

   @EventHandler
   private void onMovePacket2(Send event) {
      if (this.freeze && event.packet instanceof ServerboundMovePlayerPacket playerMove && (Boolean)this.Packet.get()) {
         this.setFreezeLook(event, playerMove);
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.freeze && this.mc.player != null) {
         this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         this.mc.player.setPosRaw(this.position.x, this.position.y, this.position.z);
      }
   }
}
