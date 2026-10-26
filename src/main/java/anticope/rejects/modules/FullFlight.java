package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.RejectsUtils;
import com.google.common.collect.Streams;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FullFlight extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgAntiKick = this.settings.createGroup("Anti Kick");
   private final Setting<Double> speed = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("speed")).description("Your speed when flying.")).defaultValue(0.3).min(0.0).sliderMax(10.0).build());
   private final Setting<Boolean> verticalSpeedMatch = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("vertical-speed-match"))
                  .description("Matches your vertical speed to your horizontal speed, otherwise uses vanilla ratio."))
               .defaultValue(false))
            .build()
      );
   private final Setting<FullFlight.AntiKickMode> antiKickMode = this.sgAntiKick
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("mode"))
                  .description("The mode for anti kick."))
               .defaultValue(FullFlight.AntiKickMode.PaperNew))
            .build()
      );
   private int delayLeft = 20;
   private double lastPacketY = Double.MAX_VALUE;
   private int floatingTicks = 0;

   public FullFlight() {
      super(MeteorRejectsAddon.CATEGORY, "fullflight", "FullFlight.");
   }

   private double calculateGround() {
      for (double ground = this.mc.player.getY(); ground > 0.0; ground -= 0.05) {
         AABB box = this.mc.player.getBoundingBox();
         AABB adjustedBox = box.move(0.0, ground - this.mc.player.getY(), 0.0);
         Stream<VoxelShape> blockCollisions = Streams.stream(this.mc.level.getBlockCollisions(this.mc.player, adjustedBox));
         if (blockCollisions.findAny().isPresent()) {
            return ground + 0.05;
         }
      }

      return 0.0;
   }

   private boolean isEntityOnAir(Entity entity) {
      return this.mc.level.getBlockStates(entity.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0)).allMatch(BlockStateBase::isAir);
   }

   private boolean shouldFlyDown(double currentY, double lastY) {
      return currentY >= lastY ? true : lastY - currentY < 0.0313;
   }

   private ServerboundMovePlayerPacket antiKickPacket(ServerboundMovePlayerPacket packet, double currentY) {
      if (this.delayLeft <= 0 && this.lastPacketY != Double.MAX_VALUE && this.shouldFlyDown(currentY, this.lastPacketY) && this.isEntityOnAir(this.mc.player)) {
         double newY = this.lastPacketY - 0.0313;
         this.lastPacketY = newY;
         this.delayLeft = 20;
         return (ServerboundMovePlayerPacket)(packet.hasRotation()
            ? new PosRot(
               packet.getX(0.0), newY, packet.getZ(0.0), packet.getYRot(0.0F), packet.getXRot(0.0F), packet.isOnGround(), packet.horizontalCollision()
            )
            : new Pos(packet.getX(0.0), newY, packet.getZ(0.0), packet.isOnGround(), packet.horizontalCollision()));
      } else {
         this.lastPacketY = currentY;
         if (!this.isEntityOnAir(this.mc.player)) {
            this.delayLeft = 20;
         }

         if (this.delayLeft > 0) {
            this.delayLeft--;
         }

         return packet;
      }
   }

   @EventHandler
   private void onSendPacket(Send event) {
      if (this.mc.player.getVehicle() != null
         && event.packet instanceof ServerboundMovePlayerPacket packet
         && this.antiKickMode.get() == FullFlight.AntiKickMode.PaperNew) {
         double currentY = packet.getY(Double.MAX_VALUE);
         ServerboundMovePlayerPacket modifiedPacket;
         if (currentY != Double.MAX_VALUE) {
            modifiedPacket = this.antiKickPacket(packet, currentY);
         } else {
            ServerboundMovePlayerPacket fullPacket;
            if (packet.hasRotation()) {
               fullPacket = new PosRot(
                  this.mc.player.getX(),
                  this.mc.player.getY(),
                  this.mc.player.getZ(),
                  packet.getYRot(0.0F),
                  packet.getXRot(0.0F),
                  packet.isOnGround(),
                  packet.horizontalCollision()
               );
            } else {
               fullPacket = new Pos(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), packet.isOnGround(), packet.horizontalCollision());
            }

            modifiedPacket = this.antiKickPacket(fullPacket, this.mc.player.getY());
         }

         if (modifiedPacket != packet) {
            event.cancel();
            this.mc.getConnection().send(modifiedPacket);
         }
      }
   }

   @EventHandler
   private void onPlayerMove(PlayerMoveEvent event) {
      if (this.floatingTicks >= 20) {
         switch ((FullFlight.AntiKickMode)this.antiKickMode.get()) {
            case Old:
               AABB boxx = this.mc.player.getBoundingBox();
               AABB adjustedBoxx = boxx.move(0.0, -0.4, 0.0);
               Stream<VoxelShape> blockCollisionsx = Streams.stream(this.mc.level.getBlockCollisions(this.mc.player, adjustedBoxx));
               if (blockCollisionsx.findAny().isPresent()) {
                  break;
               }

               double ground = this.calculateGround();
               double groundExtra = ground + 0.1;

               for (double posY = this.mc.player.getY(); posY > groundExtra; posY -= 4.0) {
                  this.mc.getConnection().send(new Pos(this.mc.player.getX(), posY, this.mc.player.getZ(), true, this.mc.player.horizontalCollision));
                  if (posY - 4.0 < groundExtra) {
                     break;
                  }
               }

               this.mc.getConnection().send(new Pos(this.mc.player.getX(), groundExtra, this.mc.player.getZ(), true, this.mc.player.horizontalCollision));

               for (double posYx = groundExtra; posYx < this.mc.player.getY(); posYx += 4.0) {
                  this.mc
                     .getConnection()
                     .send(new Pos(this.mc.player.getX(), posYx, this.mc.player.getZ(), this.mc.player.onGround(), this.mc.player.horizontalCollision));
                  if (posYx + 4.0 > this.mc.player.getY()) {
                     break;
                  }
               }

               this.mc
                  .getConnection()
                  .send(
                     new Pos(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.onGround(), this.mc.player.horizontalCollision)
                  );
               break;
            case New:
               AABB box = this.mc.player.getBoundingBox();
               AABB adjustedBox = box.move(0.0, -0.4, 0.0);
               Stream<VoxelShape> blockCollisions = Streams.stream(this.mc.level.getBlockCollisions(this.mc.player, adjustedBox));
               if (!blockCollisions.findAny().isPresent()) {
                  this.mc
                     .getConnection()
                     .send(
                        new Pos(
                           this.mc.player.getX(),
                           this.mc.player.getY() - 0.4,
                           this.mc.player.getZ(),
                           this.mc.player.onGround(),
                           this.mc.player.horizontalCollision
                        )
                     );
                  this.mc
                     .getConnection()
                     .send(
                        new Pos(
                           this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.onGround(), this.mc.player.horizontalCollision
                        )
                     );
               }
         }

         this.floatingTicks = 0;
      }

      float ySpeed = RejectsUtils.fullFlightMove(event, (Double)this.speed.get(), (Boolean)this.verticalSpeedMatch.get());
      if (this.floatingTicks < 20) {
         if (ySpeed >= -0.1) {
            this.floatingTicks++;
         } else if (this.antiKickMode.get() == FullFlight.AntiKickMode.New) {
            this.floatingTicks = 0;
         }
      }
   }

   public static enum AntiKickMode {
      Old,
      New,
      PaperNew,
      None;
   }
}
