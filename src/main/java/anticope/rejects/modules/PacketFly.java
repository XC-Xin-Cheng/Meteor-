package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.HashSet;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent.Pre;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;

public class PacketFly extends Module {
   private final HashSet<ServerboundMovePlayerPacket> packets = new HashSet<>();
   private final SettingGroup sgMovement = this.settings.createGroup("movement");
   private final SettingGroup sgClient = this.settings.createGroup("client");
   private final SettingGroup sgBypass = this.settings.createGroup("bypass");
   private final Setting<Double> horizontalSpeed = this.sgMovement
      .add(
         ((Builder)((Builder)new Builder().name("horizontal-speed")).description("Horizontal speed in blocks per second."))
            .defaultValue(5.2)
            .min(0.0)
            .max(20.0)
            .sliderMin(0.0)
            .sliderMax(20.0)
            .build()
      );
   private final Setting<Double> verticalSpeed = this.sgMovement
      .add(
         ((Builder)((Builder)new Builder().name("vertical-speed")).description("Vertical speed in blocks per second."))
            .defaultValue(1.24)
            .min(0.0)
            .max(20.0)
            .sliderMin(0.0)
            .sliderMax(20.0)
            .build()
      );
   private final Setting<Boolean> sendTeleport = this.sgMovement
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("teleport"))
                  .description("Sends teleport packets."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> setYaw = this.sgClient
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("set-yaw"))
                  .description("Sets yaw client side."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> setMove = this.sgClient
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("set-move"))
                  .description("Sets movement client side."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> setPos = this.sgClient
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("set-pos"))
                  .description("Sets position client side."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> setID = this.sgClient
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("set-id"))
                  .description("Updates teleport id when a position packet is received."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> antiKick = this.sgBypass
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("anti-kick"))
                  .description("Moves down occasionally to prevent kicks."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Integer> downDelay = this.sgBypass
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("down-delay"))
                  .description("How often you move down when not flying upwards. (ticks)"))
               .defaultValue(4))
            .sliderMin(1)
            .sliderMax(30)
            .min(1)
            .max(30)
            .build()
      );
   private final Setting<Integer> downDelayFlying = this.sgBypass
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("flying-down-delay"))
                  .description("How often you move down when flying upwards. (ticks)"))
               .defaultValue(10))
            .sliderMin(1)
            .sliderMax(30)
            .min(1)
            .max(30)
            .build()
      );
   private final Setting<Boolean> invalidPacket = this.sgBypass
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("invalid-packet"))
                  .description("Sends invalid movement packets."))
               .defaultValue(false))
            .build()
      );
   private int flightCounter = 0;
   private int teleportID = 0;

   public PacketFly() {
      super(MeteorRejectsAddon.CATEGORY, "packet-fly", "Fly using packets.");
   }

   @EventHandler
   public void onSendMovementPackets(Pre event) {
      this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
      double speed = 0.0;
      boolean checkCollisionBoxes = this.checkHitBoxes();
      speed = this.mc.player.input.keyPresses.jump()
            && (checkCollisionBoxes || this.mc.player.input.getMoveVector().y == 0.0 && this.mc.player.input.getMoveVector().x == 0.0)
         ? (
            this.antiKick.get() && !checkCollisionBoxes
               ? (this.resetCounter((Integer)this.downDelayFlying.get()) ? -0.032 : (Double)this.verticalSpeed.get() / 20.0)
               : (Double)this.verticalSpeed.get() / 20.0
         )
         : (
            this.mc.player.input.keyPresses.shift()
               ? (Double)this.verticalSpeed.get() / -20.0
               : (!checkCollisionBoxes ? (this.resetCounter((Integer)this.downDelay.get()) ? (this.antiKick.get() ? -0.04 : 0.0) : 0.0) : 0.0)
         );
      Vec3 horizontal = PlayerUtils.getHorizontalVelocity((Double)this.horizontalSpeed.get());
      this.mc.player.setDeltaMovement(horizontal.x, speed, horizontal.z);
      this.sendPackets(
         this.mc.player.getDeltaMovement().x, this.mc.player.getDeltaMovement().y, this.mc.player.getDeltaMovement().z, (Boolean)this.sendTeleport.get()
      );
   }

   @EventHandler
   public void onMove(PlayerMoveEvent event) {
      if ((Boolean)this.setMove.get() && this.flightCounter != 0) {
         event.movement = new Vec3(this.mc.player.getDeltaMovement().x, this.mc.player.getDeltaMovement().y, this.mc.player.getDeltaMovement().z);
      }
   }

   @EventHandler
   public void onPacketSent(Send event) {
      if (event.packet instanceof ServerboundMovePlayerPacket && !this.packets.remove((ServerboundMovePlayerPacket)event.packet)) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onPacketReceive(Receive event) {
      if (event.packet instanceof ClientboundPlayerPositionPacket && this.mc.player != null && this.mc.level != null) {
         ClientboundPlayerPositionPacket packet = (ClientboundPlayerPositionPacket)event.packet;
         PositionMoveRotation oldPos = packet.change();
         if ((Boolean)this.setYaw.get()) {
            PositionMoveRotation newPos = new PositionMoveRotation(
               oldPos.position(), oldPos.deltaMovement(), this.mc.player.getYRot(), this.mc.player.getXRot()
            );
            event.packet = ClientboundPlayerPositionPacket.of(packet.id(), newPos, packet.relatives());
         }

         if ((Boolean)this.setID.get()) {
            this.teleportID = packet.id();
         }
      }
   }

   private boolean checkHitBoxes() {
      return !this.mc.level.getBlockCollisions(this.mc.player, this.mc.player.getBoundingBox().expandTowards(-0.0625, -0.0625, -0.0625)).iterator().hasNext();
   }

   private boolean resetCounter(int counter) {
      if (++this.flightCounter >= counter) {
         this.flightCounter = 0;
         return true;
      } else {
         return false;
      }
   }

   private void sendPackets(double x, double y, double z, boolean teleport) {
      Vec3 vec = new Vec3(x, y, z);
      Vec3 playerPos = new Vec3(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
      Vec3 position = playerPos.add(vec);
      Vec3 outOfBoundsVec = this.outOfBoundsVec(vec, position);
      this.packetSender(new Pos(position.x, position.y, position.z, this.mc.player.onGround(), this.mc.player.horizontalCollision));
      if ((Boolean)this.invalidPacket.get()) {
         this.packetSender(new Pos(outOfBoundsVec.x, outOfBoundsVec.y, outOfBoundsVec.z, this.mc.player.onGround(), this.mc.player.horizontalCollision));
      }

      if ((Boolean)this.setPos.get()) {
         this.mc.player.setPosRaw(position.x, position.y, position.z);
      }

      this.teleportPacket(position, teleport);
   }

   private void teleportPacket(Vec3 pos, boolean shouldTeleport) {
      if (shouldTeleport) {
         this.mc.player.connection.send(new ServerboundAcceptTeleportationPacket(++this.teleportID));
      }
   }

   private Vec3 outOfBoundsVec(Vec3 offset, Vec3 position) {
      return position.add(0.0, 1500.0, 0.0);
   }

   private void packetSender(ServerboundMovePlayerPacket packet) {
      this.packets.add(packet);
      this.mc.player.connection.send(packet);
   }
}
