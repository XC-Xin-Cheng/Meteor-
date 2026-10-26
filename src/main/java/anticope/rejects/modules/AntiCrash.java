package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

public class AntiCrash extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> log = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("log")).description("Logs when crash packet detected.")).defaultValue(false)).build());

   public AntiCrash() {
      super(MeteorRejectsAddon.CATEGORY, "anti-crash", "Attempts to cancel packets that may crash the client.");
   }

   @EventHandler
   private void onPacketReceive(Receive event) {
      if (event.packet instanceof ClientboundExplodePacket packet) {
         Vec3 explodePos = packet.center();
         Vec3 playerKnockback = new Vec3(0.0, 0.0, 0.0);
         if (packet.playerKnockback().isPresent()) {
            playerKnockback = (Vec3)packet.playerKnockback().get();
         }

         if (explodePos.x() > 3.0E7
            || explodePos.y() > 3.0E7
            || explodePos.z() > 3.0E7
            || explodePos.x() < -3.0E7
            || explodePos.y() < -3.0E7
            || explodePos.z() < -3.0E7
            || playerKnockback.x > 3.0E7
            || playerKnockback.y > 3.0E7
            || playerKnockback.z > 3.0E7
            || playerKnockback.x < -3.0E7
            || playerKnockback.y < -3.0E7
            || playerKnockback.z < -3.0E7) {
            this.cancel(event);
         }
      } else if (event.packet instanceof ClientboundLevelParticlesPacket packet) {
         if (packet.getCount() > 100000) {
            this.cancel(event);
         }
      } else if (event.packet instanceof ClientboundPlayerPositionPacket packetx) {
         Vec3 playerPos = packetx.change().position();
         if (playerPos.x > 3.0E7 || playerPos.y > 3.0E7 || playerPos.z > 3.0E7 || playerPos.x < -3.0E7 || playerPos.y < -3.0E7 || playerPos.z < -3.0E7) {
            this.cancel(event);
         }
      } else if (event.packet instanceof ClientboundSetEntityMotionPacket packetxx
         && (
            packetxx.movement().x > 1000.0
               || packetxx.movement().y > 1000.0
               || packetxx.movement().z > 1000.0
               || packetxx.movement().x < -1000.0
               || packetxx.movement().y < -1000.0
               || packetxx.movement().z < -1000.0
         )) {
         this.cancel(event);
      }
   }

   private void cancel(Receive event) {
      if ((Boolean)this.log.get()) {
         this.warning("Server attempts to crash you", new Object[0]);
      }

      event.cancel();
   }
}
