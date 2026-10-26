package anticope.rejects.utils.server;

import anticope.rejects.gui.servers.ServerFinderScreen;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerPinger implements IServerFinderDoneListener, IServerFinderDisconnectListener {
   private static final AtomicInteger threadNumber = new AtomicInteger(0);
   private final Object portPingerLock = new Object();
   private MServerInfo server;
   private boolean done = false;
   private boolean failed = false;
   private Thread thread;
   private int pingPort;
   private ServerListPinger pinger;
   private boolean notifiedDoneListeners = false;
   private boolean scanPorts;
   private int searchNumber;
   private int currentIncrement = 1;
   private boolean startingIncrement = true;
   private ArrayList<IServerFinderDoneListener> doneListeners = new ArrayList<>();
   private int portPingers = 0;
   private int successfulPortPingers = 0;
   private String pingIP;

   public ServerPinger(boolean scanPorts, int searchNumber) {
      this.pinger = new ServerListPinger();
      this.pinger.addServerFinderDisconnectListener(this);
      this.scanPorts = scanPorts;
      this.searchNumber = searchNumber;
   }

   public void addServerFinderDoneListener(IServerFinderDoneListener listener) {
      this.doneListeners.add(listener);
   }

   public void ping(String ip) {
      this.ping(ip, 25565);
   }

   public int getSearchNumber() {
      return this.searchNumber;
   }

   public Thread getThread() {
      return this.thread;
   }

   public int getPingPort() {
      return this.pingPort;
   }

   public MServerInfo getServerInfo() {
      return this.server;
   }

   public void ping(String ip, int port) {
      if (!this.isOldSearch()) {
         this.pingIP = ip;
         this.pingPort = port;
         this.server = new MServerInfo("", ip + ":" + port);
         this.server.version = null;
         if (this.scanPorts) {
            this.thread = new Thread(() -> this.pingInCurrentThread(ip, port), "Server Pinger #" + threadNumber.incrementAndGet());
         } else {
            this.thread = new Thread(() -> this.pingInCurrentThread(ip, port), "Server Pinger #" + threadNumber + ", " + port);
         }

         this.thread.start();
      }
   }

   public ServerListPinger getServerListPinger() {
      return this.pinger;
   }

   private boolean isOldSearch() {
      return ServerFinderScreen.instance == null
         || ServerFinderScreen.instance.getState() == ServerFinderScreen.ServerFinderState.CANCELLED
         || ServerFinderScreen.getSearchNumber() != this.searchNumber;
   }

   private void runPortIncrement(String ip) {
      synchronized (this.portPingerLock) {
         this.portPingers = 0;
         this.successfulPortPingers = 0;
      }

      for (int i = this.startingIncrement ? 1 : this.currentIncrement; i < this.currentIncrement * 2; i++) {
         if (this.isOldSearch()) {
            return;
         }

         ServerPinger pp1 = new ServerPinger(false, this.searchNumber);
         ServerPinger pp2 = new ServerPinger(false, this.searchNumber);

         for (IServerFinderDoneListener doneListener : this.doneListeners) {
            pp1.addServerFinderDoneListener(doneListener);
            pp2.addServerFinderDoneListener(doneListener);
         }

         pp1.addServerFinderDoneListener(this);
         pp2.addServerFinderDoneListener(this);
         if (ServerFinderScreen.instance != null && !this.isOldSearch()) {
            ServerFinderScreen.instance.incrementTargetChecked(2);
         }

         pp1.ping(ip, 25565 - i);
         pp2.ping(ip, 25565 + i);
      }

      synchronized (this.portPingerLock) {
         this.currentIncrement *= 2;
      }
   }

   private void pingInCurrentThread(String ip, int port) {
      if (!this.isOldSearch()) {
         try {
            this.pinger.add(this.server, () -> {});
         } catch (Exception var4) {
            this.failed = true;
         }

         this.startingIncrement = true;
         if (!this.failed) {
            this.currentIncrement = 8;
         }

         if (!this.failed && this.scanPorts) {
            this.runPortIncrement(ip);
         }

         if (this.failed) {
            this.pinger.cancel();
            this.done = true;
            this.notifyDoneListeners(false);
         }
      }
   }

   public boolean isStillPinging() {
      return !this.done;
   }

   public boolean isWorking() {
      return !this.failed;
   }

   public boolean isOtherVersion() {
      return this.server.protocolVersion != 47;
   }

   public String getServerIP() {
      return this.server.address;
   }

   @Override
   public void onServerDisconnect() {
      if (!this.isOldSearch()) {
         this.pinger.cancel();
         this.done = true;
         this.notifyDoneListeners(false);
      }
   }

   private void notifyDoneListeners(boolean failure) {
      synchronized (this) {
         if (!this.notifiedDoneListeners) {
            this.notifiedDoneListeners = true;

            for (IServerFinderDoneListener doneListener : this.doneListeners) {
               if (doneListener != null) {
                  if (failure) {
                     doneListener.onServerFailed(this);
                  } else {
                     doneListener.onServerDone(this);
                  }
               }
            }
         }
      }
   }

   @Override
   public void onServerFailed() {
      if (!this.isOldSearch()) {
         this.pinger.cancel();
         this.done = true;
         this.notifyDoneListeners(true);
      }
   }

   @Override
   public void onServerDone(ServerPinger pinger) {
      synchronized (this.portPingerLock) {
         this.portPingers++;
         if (pinger.isWorking()) {
            this.successfulPortPingers++;
         }

         if (this.portPingers == (this.startingIncrement ? this.currentIncrement * 2 - 2 : this.currentIncrement)
            && this.currentIncrement <= 5000
            && this.successfulPortPingers > 0) {
            this.startingIncrement = false;
            new Thread(() -> this.runPortIncrement(this.pingIP)).start();
         }
      }
   }

   @Override
   public void onServerFailed(ServerPinger pinger) {
      synchronized (this.portPingerLock) {
         this.portPingers++;
      }
   }
}
