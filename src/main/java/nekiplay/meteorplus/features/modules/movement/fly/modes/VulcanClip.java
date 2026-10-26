package nekiplay.meteorplus.features.modules.movement.fly.modes;

import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent.Pre;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import nekiplay.meteorplus.features.modules.movement.fly.FlyMode;
import nekiplay.meteorplus.features.modules.movement.fly.FlyModes;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.world.phys.Vec3;

public class VulcanClip extends FlyMode {
   private boolean waitFlag = false;
   private boolean canGlide = false;
   private int ticks = 0;
   private Timer timer;

   public VulcanClip() {
      super(FlyModes.Vulcan_Clip);
   }

   @Override
   public void onDeactivate() {
      this.timer.setOverride(1.0);
   }

   @Override
   public void onActivate() {
      this.timer = (Timer)Modules.get().get(Timer.class);
      if (this.mc.player.onGround() && (Boolean)this.settings.canClip.get()) {
         this.clip(0.0F, -0.1F);
         this.waitFlag = true;
         this.canGlide = false;
         this.ticks = 0;
         this.timer.setOverride(0.1F);
      } else {
         this.waitFlag = false;
         this.canGlide = true;
      }
   }

   @Override
   public void onPlayerMoveSendPre(Pre event) {
      if (this.canGlide) {
         this.timer.setOverride(1.0);
         Vec3 velocity = this.mc.player.getDeltaMovement();
         velocity.add(0.0, -(this.ticks % 2 == 0 ? 0.17 : 0.1), 0.0);
         if (this.ticks == 0) {
            velocity.add(0.0, -0.07, 0.0);
         }

         this.mc.player.setDeltaMovement(velocity);
         this.ticks++;
      }
   }

   @Override
   public void onReceivePacket(Receive event) {
      super.onReceivePacket(event);
      if (event.packet instanceof ClientboundPlayerPositionPacket packet && this.waitFlag) {
         Vec3 playerPos = this.mc.player.position();
         this.waitFlag = false;
         this.mc.player.setPos(packet.change().position().x, packet.change().position().y, packet.change().position().z);
         this.mc.player.connection.send(new Pos(playerPos.x, playerPos.y, playerPos.z, false, this.mc.player.horizontalCollision));
         event.cancel();
         this.mc.player.jumpFromGround();
         this.clip(0.127318F, 0.0F);
         this.clip(3.425559F, 3.7F);
         this.clip(3.14285F, 3.54F);
         this.clip(2.88522F, 3.4F);
         this.canGlide = true;
      }
   }

   private void clip(float dist, float y) {
      float tickDelta = this.mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
      double yaw = Math.toRadians(this.mc.player.getViewYRot(tickDelta));
      double x = -Math.sin(yaw) * dist;
      double z = Math.cos(yaw) * dist;
      this.mc.player.setPos(this.mc.player.position().x + x, this.mc.player.position().y + y, this.mc.player.position().z + z);
      this.mc.player.connection.send(new Pos(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), false, this.mc.player.horizontalCollision));
   }
}
