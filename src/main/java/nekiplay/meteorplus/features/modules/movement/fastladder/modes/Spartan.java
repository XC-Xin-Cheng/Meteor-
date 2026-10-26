package nekiplay.meteorplus.features.modules.movement.fastladder.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixin.ServerboundMovePlayerPacketAccessor;
import nekiplay.meteorplus.features.modules.movement.fastladder.FastLadderMode;
import nekiplay.meteorplus.features.modules.movement.fastladder.FastLadderModes;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Spartan extends FastLadderMode {
   private int tick = 0;
   private boolean modify = false;
   private boolean start = false;
   private double startY = 0.0;
   private double lastY = 0.0;
   private final double coff = 3.26E-11;
   private boolean block = false;

   public Spartan() {
      super(FastLadderModes.Spartan);
   }

   @Override
   public void onActivate() {
      this.tick = 0;
      this.start = false;
      this.modify = false;

      assert this.mc.player != null;

      this.startY = this.mc.player.position().y;
   }

   private boolean YGround(double height, double min, double max) {
      String yString = String.valueOf(height);
      yString = yString.substring(yString.indexOf("."));
      double y = Double.parseDouble(yString);
      return y >= min && y <= max;
   }

   private double RGround(double height) {
      String yString = String.valueOf(height);
      yString = yString.substring(yString.indexOf("."));
      return Double.parseDouble(yString);
   }

   @Override
   public void onSendPacket(Send event) {
      this.work(event.packet);
   }

   @Override
   public void onSentPacket(Sent event) {
      this.work(event.packet);
   }

   private void work(Packet<?> packet) {
      if (this.modify) {
         if (packet instanceof ServerboundMovePlayerPacket move) {
            assert this.mc.player != null;

            double y = this.mc.player.getY();
            y = move.getY(y);
            if (this.YGround(y, this.RGround(this.startY) - 0.1, this.RGround(this.startY) + 0.1)) {
               ((ServerboundMovePlayerPacketAccessor)packet).meteor$setOnGround(true);
            }

            if (this.mc.player.onGround() && this.block) {
               this.block = false;
               this.startY = this.mc.player.position().y;
               this.start = false;
            }
         }
      } else {
         assert this.mc.player != null;

         if (this.mc.player.onGround() && this.block) {
            this.block = false;
            this.startY = this.mc.player.position().y;
            this.start = false;
         }
      }
   }

   @Override
   public void onTickEventPre(Pre event) {
      if (this.modify) {
         LocalPlayer player = this.mc.player;
         double y = player.position().y;
         if (this.lastY == y && this.tick > 1) {
            this.block = true;
         } else {
            this.lastY = y;
         }
      }
   }

   @Override
   public void onTickEventPost(Post event) {
      LocalPlayer player = this.mc.player;

      assert player != null;

      Vec3 pl_velocity = player.getDeltaMovement();
      Vec3 pos = player.position();
      ClientPacketListener h = this.mc.getConnection();
      BlockState state = this.mc.level.getBlockState(player.blockPosition());
      BlockState state2 = this.mc.level.getBlockState(player.blockPosition().offset(0, 1, 0));
      if (this.mc.player.onClimbable()) {
         this.modify = player.horizontalCollision && player.isSuppressingSlidingDownLadder();
         if (this.mc.player.onGround()) {
            this.block = false;
            this.startY = this.mc.player.position().y;
            this.start = false;
         }

         if (player.horizontalCollision) {
            if (!this.start) {
               this.start = true;
               this.startY = this.mc.player.position().y;
               this.lastY = this.mc.player.getY();
            }

            if (!this.block) {
               if (this.tick == 0) {
                  this.mc.player.setDeltaMovement(pl_velocity.x, 0.41999998688698, pl_velocity.z);
                  this.tick = 1;
               } else if (this.tick == 1) {
                  this.mc.player.setDeltaMovement(pl_velocity.x, 0.33319999360438, pl_velocity.z);
                  this.tick = 2;
               } else if (this.tick == 2) {
                  this.mc.player.setDeltaMovement(pl_velocity.x, 0.24813599859438, pl_velocity.z);
                  this.tick = 0;
               }
            }
         } else {
            this.modify = false;
            this.tick = 0;
         }
      }
   }
}
