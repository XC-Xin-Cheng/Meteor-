package fr.crazycat256.satellite.modules;

import fr.crazycat256.satellite.Addon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;

public class SpeedBypass extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<SpeedBypass.Mode> mode = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("mode")).description("The mode to use.")).defaultValue(SpeedBypass.Mode.Dynamic)).build());
   private final Setting<Integer> packets = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("packets"))
                     .description("The amount of packets to send per tick."))
                  .defaultValue(1))
               .min(1)
               .sliderMax(10)
               .visible(() -> this.mode.get() == SpeedBypass.Mode.Static))
            .build()
      );

   public SpeedBypass() {
      super(Addon.CATEGORY, "speed-bypass", "Allows you to bypass the speed check.");
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null) {
         if (this.mode.get() == SpeedBypass.Mode.Static) {
            for (int i = 0; i < this.packets.get(); i++) {
               this.mc.player.connection.send(new StatusOnly(this.mc.player.onGround(), this.mc.player.horizontalCollision));
            }
         }
      }
   }

   @EventHandler
   private void onPlayerMove(PlayerMoveEvent event) {
      if (this.mc.player != null) {
         if (this.mode.get() == SpeedBypass.Mode.Dynamic) {
            int packets = (int)event.movement.length() / 10;

            for (int i = 0; i < packets; i++) {
               this.mc.player.connection.send(new StatusOnly(this.mc.player.onGround(), this.mc.player.horizontalCollision));
            }
         }
      }
   }

   public static enum Mode {
      Static,
      Dynamic;
   }
}
