package anticope.rejects.gui.servers;

import anticope.rejects.mixin.MultiplayerScreenAccessor;
import anticope.rejects.utils.server.IServerFinderDoneListener;
import anticope.rejects.utils.server.MServerInfo;
import anticope.rejects.utils.server.ServerPinger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Stack;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.Type;

public class ServerFinderScreen extends WindowScreen implements IServerFinderDoneListener {
   public static ServerFinderScreen instance = null;
   private static int searchNumber = 0;
   private final JoinMultiplayerScreen multiplayerScreen;
   private final WTextBox ipBox;
   private final WTextBox versionBox;
   private final WIntEdit maxThreadsBox;
   private final WButton searchButton;
   private final WLabel stateLabel;
   private final WLabel checkedLabel;
   private final WLabel workingLabel;
   private final WCheckbox scanPortsBox;
   private final Stack<String> ipsToPing = new Stack<>();
   private final Object serverFinderLock = new Object();
   private ServerFinderScreen.ServerFinderState state;
   private int maxThreads;
   private volatile int numActiveThreads;
   private volatile int checked;
   private volatile int working;
   private int targetChecked = 1792;
   private ArrayList<String> versionFilters = new ArrayList<>();
   private int playerCountFilter = 0;

   public ServerFinderScreen(GuiTheme theme, JoinMultiplayerScreen multiplayerScreen, Screen parent) {
      super(theme, "Server Discovery");
      this.multiplayerScreen = multiplayerScreen;
      this.parent = parent;
      this.ipBox = theme.textBox("127.0.0.1");
      this.versionBox = theme.textBox("1.20; 1.19; 1.18; 1.17; 1.16; 1.15; 1.14; 1.13; 1.12; 1.11; 1.10; 1.9; 1.8");
      this.maxThreadsBox = theme.intEdit(128, 1, 1024, 1, 1024);
      this.stateLabel = theme.label("");
      this.checkedLabel = theme.label("");
      this.searchButton = theme.button("Search");
      this.workingLabel = theme.label("");
      this.scanPortsBox = theme.checkbox(true);
      this.state = ServerFinderScreen.ServerFinderState.NOT_RUNNING;
      this.newSearch();
      instance = this;
   }

   public static int getSearchNumber() {
      return searchNumber;
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
      table.row();
      table.add(this.theme.label("Scan ports"));
      table.add(this.scanPortsBox);
      table.row();
      table.add(this.theme.label("Versions:"));
      table.add(this.versionBox).expandX();
      this.add(this.stateLabel);
      this.add(this.checkedLabel);
      this.add(this.workingLabel);
      WHorizontalList list = (WHorizontalList)this.add(this.theme.horizontalList()).expandX().widget();
      list.add(this.searchButton).expandX();
      this.searchButton.action = this::searchOrCancel;
   }

   private void newSearch() {
      searchNumber = (searchNumber + 1) % 1000;
   }

   public void incrementTargetChecked(int amount) {
      synchronized (this.serverFinderLock) {
         if (this.state != ServerFinderScreen.ServerFinderState.CANCELLED) {
            this.targetChecked += amount;
         }
      }
   }

   public ServerFinderScreen.ServerFinderState getState() {
      return this.state;
   }

   private void searchOrCancel() {
      if (this.state.isRunning()) {
         this.state = ServerFinderScreen.ServerFinderState.CANCELLED;
      } else {
         this.state = ServerFinderScreen.ServerFinderState.RESOLVING;
         this.maxThreads = this.maxThreadsBox.get();
         this.ipsToPing.clear();
         this.targetChecked = 1792;
         this.numActiveThreads = 0;
         this.checked = 0;
         this.working = 0;
         this.newSearch();
         this.parseVersionFilters();
         this.findServers();
      }
   }

   private void parseVersionFilters() {
      String filter = this.versionBox.get();
      String[] versions = filter.split(";");
      if (this.versionFilters == null) {
         this.versionFilters = new ArrayList<>();
      }

      this.versionFilters.clear();

      for (String version : versions) {
         String trimmed = version.trim();
         if (!trimmed.isEmpty()) {
            this.versionFilters.add(version.trim());
         }
      }
   }

   private void findServers() {
      try {
         InetAddress addr = InetAddress.getByName(this.ipBox.get().split(":")[0].trim());
         int[] ipParts = new int[4];

         for (int i = 0; i < 4; i++) {
            ipParts[i] = addr.getAddress()[i] & 255;
         }

         this.state = ServerFinderScreen.ServerFinderState.SEARCHING;
         int[] changes = new int[]{0, 1, -1, 2, -2, 3, -3};

         for (int change : changes) {
            for (int i2 = 0; i2 <= 255; i2++) {
               if (this.state == ServerFinderScreen.ServerFinderState.CANCELLED) {
                  return;
               }

               int[] ipParts2 = (int[])ipParts.clone();
               ipParts2[2] = ipParts[2] + change & 0xFF;
               ipParts2[3] = i2;
               String ip = ipParts2[0] + "." + ipParts2[1] + "." + ipParts2[2] + "." + ipParts2[3];
               this.ipsToPing.push(ip);
            }
         }

         while (this.numActiveThreads < this.maxThreads && this.pingNewIP()) {
         }
      } catch (UnknownHostException var11) {
         this.state = ServerFinderScreen.ServerFinderState.UNKNOWN_HOST;
      } catch (Exception var12) {
         var12.printStackTrace();
         this.state = ServerFinderScreen.ServerFinderState.ERROR;
      }
   }

   private boolean pingNewIP() {
      synchronized (this.serverFinderLock) {
         if (!this.ipsToPing.isEmpty()) {
            String ip = this.ipsToPing.pop();
            ServerPinger pinger = new ServerPinger(this.scanPortsBox.checked, searchNumber);
            pinger.addServerFinderDoneListener(this);
            pinger.ping(ip);
            this.numActiveThreads++;
            return true;
         } else {
            return false;
         }
      }
   }

   public void tick() {
      this.searchButton.set(this.state.isRunning() ? "Cancel" : "Search");
      if (this.state.isRunning()) {
         this.ipBox.setFocused(false);
         this.maxThreadsBox.set(this.maxThreads);
      }

      this.stateLabel.set(this.state.toString());
      this.checkedLabel.set("Checked: " + this.checked + " / " + this.targetChecked);
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

   public void onClose() {
      this.state = ServerFinderScreen.ServerFinderState.CANCELLED;
      super.onClose();
   }

   private boolean filterPass(MServerInfo info) {
      if (info == null) {
         return false;
      } else if (info.playerCount < this.playerCountFilter) {
         return false;
      } else {
         for (String version : this.versionFilters) {
            if (info.version != null && info.version.contains(version)) {
               return true;
            }
         }

         return this.versionFilters.isEmpty();
      }
   }

   @Override
   public void onServerDone(ServerPinger pinger) {
      if (this.state != ServerFinderScreen.ServerFinderState.CANCELLED && pinger != null && pinger.getSearchNumber() == searchNumber) {
         synchronized (this.serverFinderLock) {
            this.checked++;
            this.numActiveThreads--;
         }

         if (pinger.isWorking() && !this.isServerInList(pinger.getServerIP()) && this.filterPass(pinger.getServerInfo())) {
            synchronized (this.serverFinderLock) {
               this.working++;
               this.multiplayerScreen.getServers().add(new ServerData("Server discovery #" + this.working, pinger.getServerIP(), Type.OTHER), false);
               this.multiplayerScreen.getServers().save();
               ((MultiplayerScreenAccessor)this.multiplayerScreen).getServerListWidget().setSelected(null);
               ((MultiplayerScreenAccessor)this.multiplayerScreen).getServerListWidget().updateOnlineServers(this.multiplayerScreen.getServers());
            }
         }

         while (this.numActiveThreads < this.maxThreads && this.pingNewIP()) {
         }

         synchronized (this.serverFinderLock) {
            if (this.checked == this.targetChecked) {
               this.state = ServerFinderScreen.ServerFinderState.DONE;
            }
         }
      }
   }

   @Override
   public void onServerFailed(ServerPinger pinger) {
      if (this.state != ServerFinderScreen.ServerFinderState.CANCELLED && pinger != null && pinger.getSearchNumber() == searchNumber) {
         synchronized (this.serverFinderLock) {
            this.checked++;
            this.numActiveThreads--;
         }

         while (this.numActiveThreads < this.maxThreads && this.pingNewIP()) {
         }

         synchronized (this.serverFinderLock) {
            if (this.checked == this.targetChecked) {
               this.state = ServerFinderScreen.ServerFinderState.DONE;
            }
         }
      }
   }

   public static enum ServerFinderState {
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
