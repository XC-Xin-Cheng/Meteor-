package nekiplay.meteorplus.features.modules.movement.speed.modes.matrix;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedMode;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedModes;
import nekiplay.meteorplus.utils.MovementUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public class Matrix6_7_0 extends SpeedMode {
   private int noVelocityY = 0;

   public Matrix6_7_0() {
      super(SpeedModes.Matrix_6dot7dot0);
   }

   @Override
   public void onDeactivate() {
      this.mc.player.getAbilities().setFlyingSpeed(0.02F);
   }

   @Override
   public void onTickEventPre(Pre event) {
      this.work();
   }

   @Override
   public void onTickEventPost(Post event) {
   }

   @Override
   public void onReceivePacket(Receive event) {
      if (event.packet instanceof ClientboundSetEntityMotionPacket velocity
         && this.mc.player != null
         && this.mc.level != null
         && this.mc.level.getEntity(velocity.id()) != null
         && this.mc.player == this.mc.level.getEntity(velocity.id())) {
         this.noVelocityY = 10;
      }
   }

   private void work() {
      if (!this.mc.player.onGround() && this.noVelocityY <= 0) {
         if (this.mc.player.getDeltaMovement().y > 0.0) {
            this.mc.player.getDeltaMovement().add(0.0, -5.0E-4, 0.0);
         }

         this.mc.player.getDeltaMovement().add(0.0, -0.009400114514191982, 0.0);
      }

      if (!this.mc.player.onGround() && this.noVelocityY < 8 && MovementUtils.getSpeed() < 0.2177 && this.noVelocityY < 8) {
         MovementUtils.strafe(0.2177F);
      }

      if (Math.abs(this.mc.player.getAbilities().getFlyingSpeed()) < 0.1) {
         this.mc.player.getAbilities().setFlyingSpeed(0.026F);
      } else {
         this.mc.player.getAbilities().setFlyingSpeed(0.0247F);
      }

      if (this.mc.player.onGround() && PlayerUtils.isMoving()) {
         this.mc.options.keyJump.setDown(false);
         this.mc.player.jumpFromGround();
         IVec3 v = (IVec3)this.mc.player.getDeltaMovement();
         v.meteor$setY(0.4105000114514192);
         if (Math.abs(this.mc.player.getAbilities().getFlyingSpeed()) < 0.1) {
            MovementUtils.strafe(MovementUtils.getSpeed());
         }
      }

      if (!PlayerUtils.isMoving()) {
         IVec3 v = (IVec3)this.mc.player.getDeltaMovement();
         v.meteor$setXZ(0.0, 0.0);
      }
   }
}
