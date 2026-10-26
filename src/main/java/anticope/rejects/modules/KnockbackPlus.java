package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class KnockbackPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> ka = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("only-killaura")).description("Only perform more KB when using killaura.")).defaultValue(false))
            .build()
      );

   public KnockbackPlus() {
      super(MeteorRejectsAddon.CATEGORY, "knockback-plus", "Performs more KB when you hit your target.");
   }

   @EventHandler
   private void onSendPacket(Send event) {
      if (event.packet instanceof ServerboundAttackPacket packet) {
         Entity entity = this.mc.level.getEntity(packet.entityId());
         if (!(entity instanceof LivingEntity) || entity != ((KillAura)Modules.get().get(KillAura.class)).getTarget() && (Boolean)this.ka.get()) {
            return;
         }

         assert this.mc.player != null;

         this.mc.player.connection.send(new ServerboundPlayerCommandPacket(this.mc.player, Action.START_SPRINTING));
      }
   }
}
