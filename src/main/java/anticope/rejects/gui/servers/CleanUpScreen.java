package anticope.rejects.gui.servers;

import anticope.rejects.mixin.MultiplayerScreenAccessor;
import anticope.rejects.mixin.ServerListAccessor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;

public class CleanUpScreen extends WindowScreen {
   private final JoinMultiplayerScreen multiplayerScreen;
   private final WCheckbox removeAll;
   private final WCheckbox removeFailed;
   private final WCheckbox removeOutdated;
   private final WCheckbox removeUnknown;
   private final WCheckbox removeGriefMe;
   private final WCheckbox removeDuplicates;
   private final WCheckbox rename;

   public CleanUpScreen(GuiTheme theme, JoinMultiplayerScreen multiplayerScreen, Screen parent) {
      super(theme, "Clean Up");
      this.multiplayerScreen = multiplayerScreen;
      this.parent = parent;
      this.removeUnknown = theme.checkbox(true);
      this.removeOutdated = theme.checkbox(false);
      this.removeFailed = theme.checkbox(true);
      this.removeGriefMe = theme.checkbox(false);
      this.removeAll = theme.checkbox(false);
      this.removeDuplicates = theme.checkbox(true);
      this.rename = theme.checkbox(true);
   }

   public void initWidgets() {
      WTable table = (WTable)this.add(new WTable()).widget();
      table.add(this.theme.label("Remove:"));
      table.row();
      ((WLabel)table.add(this.theme.label("Unknown Hosts:")).widget()).tooltip = "";
      table.add(this.removeUnknown).widget();
      table.row();
      table.add(this.theme.label("Outdated Servers:"));
      table.add(this.removeOutdated).widget();
      table.row();
      table.add(this.theme.label("Failed Ping:"));
      table.add(this.removeFailed).widget();
      table.row();
      table.add(this.theme.label("\"Server discovery\" Servers:"));
      table.add(this.removeGriefMe).widget();
      table.row();
      ((WLabel)table.add(this.theme.label("Everything:")).widget()).color = new Color(255, 0, 0);
      table.add(this.removeAll).widget();
      table.row();
      table.add(this.theme.label("Duplicates:"));
      table.add(this.removeDuplicates).widget();
      table.row();
      table.add(this.theme.label("Rename all Servers:"));
      table.add(this.rename).widget();
      table.row();
      ((WButton)table.add(this.theme.button("Execute!")).expandX().widget()).action = this::cleanUp;
   }

   private void cleanUp() {
      Set<String> knownIPs = new HashSet<>();
      List<ServerData> servers = ((ServerListAccessor)this.multiplayerScreen.getServers()).getServerList();

      for (ServerData server : servers.toArray(ServerData[]::new)) {
         if (this.removeAll.checked || this.shouldRemove(server, knownIPs)) {
            servers.remove(server);
         }
      }

      if (this.rename.checked) {
         for (int i = 0; i < servers.size(); i++) {
            ServerData serverx = servers.get(i);
            serverx.name = "Server discovery " + (i + 1);
         }
      }

      this.saveServerList();
      this.minecraft.setScreen(this.parent);
   }

   private boolean shouldRemove(ServerData server, Set<String> knownIPs) {
      return server != null
         && (
            this.removeUnknown.checked && this.isUnknownHost(server)
               || this.removeOutdated.checked && !this.isSameProtocol(server)
               || this.removeFailed.checked && this.isFailedPing(server)
               || this.removeGriefMe.checked && this.isGriefMeServer(server)
               || this.removeDuplicates.checked && !knownIPs.add(server.ip)
         );
   }

   private boolean isUnknownHost(ServerData server) {
      return server.motd != null && server.motd.getString() != null ? server.motd.getString().equals("§4Can't resolve hostname") : false;
   }

   private boolean isSameProtocol(ServerData server) {
      return server.protocol == SharedConstants.getCurrentVersion().protocolVersion();
   }

   private boolean isFailedPing(ServerData server) {
      return server.ping != -2L && server.ping < 0L;
   }

   private boolean isGriefMeServer(ServerData server) {
      return server.name != null && server.name.startsWith("Server discovery ");
   }

   private void saveServerList() {
      this.multiplayerScreen.getServers().save();
      ServerSelectionList serverListSelector = ((MultiplayerScreenAccessor)this.multiplayerScreen).getServerListWidget();
      serverListSelector.setSelected(null);
      serverListSelector.updateOnlineServers(this.multiplayerScreen.getServers());
   }
}
