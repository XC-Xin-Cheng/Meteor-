package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.settings.GameModeListSetting;
import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.world.level.GameType;

public class GamemodeNotifier extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<List<GameType>> gamemodes = this.sgGeneral
      .add(
         ((GameModeListSetting.Builder)((GameModeListSetting.Builder)new GameModeListSetting.Builder().name("gamemode"))
               .description("Which gamemodes to notify."))
            .build()
      );

   public GamemodeNotifier() {
      super(MeteorRejectsAddon.CATEGORY, "gamemode-notifier", "Notifies user a player's gamemode was changed.");
   }

   @EventHandler
   public void onPacket(Receive event) {
      if (event.packet instanceof ClientboundPlayerInfoUpdatePacket packet) {
         for (Entry entry : packet.entries()) {
            if (packet.actions().contains(Action.UPDATE_GAME_MODE)) {
               PlayerInfo entry1 = this.mc.getConnection().getPlayerInfo(entry.profileId());
               if (entry1 != null) {
                  GameType gameMode = entry.gameMode();
                  if (entry1.getGameMode() != gameMode && ((List)this.gamemodes.get()).contains(gameMode)) {
                     this.info("Player %s changed gamemode to %s", new Object[]{entry1.getProfile().name(), entry.gameMode()});
                  }
               }
            }
         }
      }
   }
}
