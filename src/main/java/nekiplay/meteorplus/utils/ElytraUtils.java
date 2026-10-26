package nekiplay.meteorplus.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;

public class ElytraUtils {
   public static void startFly() {
      if (MeteorClient.mc.player != null && MeteorClient.mc.player.connection != null) {
         MeteorClient.mc.player.connection.send(new ServerboundPlayerCommandPacket(MeteorClient.mc.player, Action.START_FALL_FLYING));
      }
   }

   public static void fakeInventoryOpen(boolean open) {
      if (MeteorClient.mc.player != null && MeteorClient.mc.player.connection != null) {
         if (open) {
            MeteorClient.mc.player.connection.send(new ServerboundPlayerCommandPacket(MeteorClient.mc.player, Action.OPEN_INVENTORY));
         } else {
            MeteorClient.mc.player.connection.send(new ServerboundContainerClosePacket(0));
         }
      }
   }
}
