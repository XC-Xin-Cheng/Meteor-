package anticope.rejects.gui.servers;

import anticope.rejects.mixin.MultiplayerScreenAccessor;
import anticope.rejects.utils.server.LegacyServerPinger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.Type;

public class LegacyServerFinderScreen extends WindowScreen {
   private final JoinMultiplayerScreen multiplayerScreen;
   private final WTextBox ipBox;
   private final WIntEdit maxThreadsBox;
   private final WButton searchButton;
   private final WLabel stateLabel;
   private final WLabel checkedLabel;
   private final WLabel workingLabel;
   private LegacyServerFinderScreen.ServerFinderState state;
   private int maxThreads;
   private int checked;
   private int working;

   public LegacyServerFinderScreen(GuiTheme theme, JoinMultiplayerScreen multiplayerScreen, Screen parent) {
      super(theme, "Legacy Server Discovery");
      this.multiplayerScreen = multiplayerScreen;
      this.parent = parent;
      this.ipBox = theme.textBox("127.0.0.1");
      this.maxThreadsBox = theme.intEdit(128, 1, 256, 1, 256);
      this.stateLabel = theme.label("");
      this.checkedLabel = theme.label("");
      this.workingLabel = theme.label("");
      this.searchButton = theme.button("Search");
      this.state = LegacyServerFinderScreen.ServerFinderState.NOT_RUNNING;
   }

   public void initWidgets() {
      this.add(this.theme.label("This will search for servers with similar IPs"));
      this.add(this.theme.label("to the IP you type into the field below."));
      this.add(this.theme.label("The servers it finds will be added to your server list."));
      WTable table = (WTable)this.add(new WTable()).expandX().widget();
      table.add(this.theme.label("Server address:"));
      table.add(this.ipBox).expandX();
      table.row();
      table.add(this.theme.label("Max. Threads:"));
      table.add(this.maxThreadsBox);
      this.add(this.stateLabel);
      this.add(this.checkedLabel);
      this.add(this.workingLabel);
      this.add(this.searchButton).expandX();
      this.searchButton.action = this::searchOrCancel;
   }

   private void searchOrCancel() {
      if (this.state.isRunning()) {
         this.state = LegacyServerFinderScreen.ServerFinderState.CANCELLED;
      } else {
         this.state = LegacyServerFinderScreen.ServerFinderState.RESOLVING;
         this.maxThreads = this.maxThreadsBox.get();
         this.checked = 0;
         this.working = 0;
         new Thread(this::findServers, "Server Discovery").start();
      }
   }

   private void findServers() {
      try {
         InetAddress addr = InetAddress.getByName(this.ipBox.get().split(":")[0].trim());
         int[] ipParts = new int[4];

         for (int i = 0; i < 4; i++) {
            ipParts[i] = addr.getAddress()[i] & 255;
         }

         this.state = LegacyServerFinderScreen.ServerFinderState.SEARCHING;
         ArrayList<LegacyServerPinger> pingers = new ArrayList<>();
         int[] changes = new int[]{0, 1, -1, 2, -2, 3, -3};

         for (int change : changes) {
            for (int i2 = 0; i2 <= 255; i2++) {
               if (this.state == LegacyServerFinderScreen.ServerFinderState.CANCELLED) {
                  return;
               }

               int[] ipParts2 = (int[])ipParts.clone();
               ipParts2[2] = ipParts[2] + change & 0xFF;
               ipParts2[3] = i2;
               String ip = ipParts2[0] + "." + ipParts2[1] + "." + ipParts2[2] + "." + ipParts2[3];
               LegacyServerPinger pinger = new LegacyServerPinger();
               pinger.ping(ip);
               pingers.add(pinger);

               while (pingers.size() >= this.maxThreads) {
                  if (this.state == LegacyServerFinderScreen.ServerFinderState.CANCELLED) {
                     return;
                  }

                  this.updatePingers(pingers);
               }
            }
         }

         while (!pingers.isEmpty()) {
            if (this.state == LegacyServerFinderScreen.ServerFinderState.CANCELLED) {
               return;
            }

            this.updatePingers(pingers);
         }

         this.state = LegacyServerFinderScreen.ServerFinderState.DONE;
      } catch (UnknownHostException var13) {
         this.state = LegacyServerFinderScreen.ServerFinderState.UNKNOWN_HOST;
      } catch (Exception var14) {
         var14.printStackTrace();
         this.state = LegacyServerFinderScreen.ServerFinderState.ERROR;
      }
   }

   public void tick() {
      this.searchButton.set(this.state.isRunning() ? "Cancel" : "Search");
      if (this.state.isRunning()) {
         this.ipBox.setFocused(false);
         this.maxThreadsBox.set(this.maxThreads);
      }

      this.stateLabel.set(this.state.toString());
      this.checkedLabel.set("Checked: " + this.checked + " / 1792");
      this.workingLabel.set("Working: " + this.working);
      this.searchButton.visible = !this.ipBox.get().isEmpty();
   }

   private boolean isServerInList(String ip) {
      for (int i = 0; i < this.multiplayerScreen.getServers().size(); i++) {
         if (this.multiplayerScreen.getServers().get(i).ip.equals(ip)) {
            return true;
         }
      }

      return false;
   }

   private void updatePingers(ArrayList<LegacyServerPinger> pingers) {
      for (int i = 0; i < pingers.size(); i++) {
         if (!pingers.get(i).isStillPinging()) {
            this.checked++;
            if (pingers.get(i).isWorking()) {
               this.working++;
               if (!this.isServerInList(pingers.get(i).getServerIP())) {
                  this.multiplayerScreen.getServers().add(new ServerData("Server discovery " + this.working, pingers.get(i).getServerIP(), Type.OTHER), false);
                  this.multiplayerScreen.getServers().save();
                  ((MultiplayerScreenAccessor)this.multiplayerScreen).getServerListWidget().setSelected(null);
                  ((MultiplayerScreenAccessor)this.multiplayerScreen).getServerListWidget().updateOnlineServers(this.multiplayerScreen.getServers());
               }
            }

            pingers.remove(i);
         }
      }
   }

   public void onClose() {
      this.state = LegacyServerFinderScreen.ServerFinderState.CANCELLED;
      super.onClose();
   }

   static enum ServerFinderState {
      NOT_RUNNING(""),
      SEARCHING("Searching..."),
      RESOLVING("Resolving..."),
      UNKNOWN_HOST("Unknown Host!"),
      CANCELLED("Cancelled!"),
      DONE("Done!"),
      ERROR("An error occurred!");

      private final String name;

      private ServerFinderState(String name) {
         this.name = name;
      }

      public boolean isRunning() {
         return this == SEARCHING || this == RESOLVING;
      }

      @Override
      public String toString() {
         return this.name;
      }
   }
}
