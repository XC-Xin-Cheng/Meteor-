package nekiplay.meteorplus.features.modules.movement;

import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.world.phys.Vec3;

public class Freeze extends Module {
   private final SettingGroup FSettings = this.settings.getDefaultGroup();
   private final Setting<Boolean> FreezeLook = this.FSettings
      .add(((Builder)((Builder)((Builder)new Builder().name("Freeze look")).description("Freezes your pitch and yaw.")).defaultValue(false)).build());
   private final Setting<Boolean> Packet = this.FSettings
      .add(((Builder)((Builder)((Builder)new Builder().name("Packet mode")).description("Enable packet mode, better.")).defaultValue(true)).build());
   private final Setting<Boolean> FreezeLookSilent = this.FSettings
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("Freeze look silent")).description("Freezes your pitch and yaw silent."))
                  .defaultValue(true))
               .visible(() -> (Boolean)this.Packet.get() && (Boolean)this.FreezeLook.get()))
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

   public Freeze() {
      super(Categories.Movement, "Freeze", "Freezes your position for server.");
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
      if (this.mc.player != null && this.mc.getConnection() != null && (Boolean)this.FreezeLookPlace.get()) {
         Rot r = new Rot(this.mc.player.getYRot(), this.mc.player.getXRot(), this.mc.player.onGround(), this.mc.player.horizontalCollision);
         this.rotate = true;
         this.mc.getConnection().send(r);
         this.rotate = false;
      }
   }

   @EventHandler
   private void onMovePacket(Send event) {
      if (event.packet instanceof ServerboundMovePlayerPacket playerMove && (Boolean)this.Packet.get()) {
         this.setFreezeLook(event, playerMove);
      }
   }

   @EventHandler
   private void connectToServerEvent(GameLeftEvent event) {
      this.toggle();
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null) {
         this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         this.mc.player.setPosRaw(this.position.x, this.position.y, this.position.z);
      }
   }

   @EventHandler
   private void onPlayerMove(PlayerMoveEvent event) {
      event.movement = new Vec3(0.0, 0.0, 0.0);
   }

   @EventHandler
   private void remove(EntityRemovedEvent event) {
      if (event.entity == this.mc.player && this.isActive()) {
         this.toggle();
      }
   }
}
