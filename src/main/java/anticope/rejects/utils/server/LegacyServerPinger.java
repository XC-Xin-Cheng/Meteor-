package anticope.rejects.utils.server;

import anticope.rejects.MeteorRejectsAddon;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.server.network.EventLoopGroupHolder;

public class LegacyServerPinger {
   private static final AtomicInteger threadNumber = new AtomicInteger(0);
   private ServerData server;
   private boolean done = false;
   private boolean failed = false;

   public void ping(String ip) {
      this.ping(ip, 25565);
   }

   public void ping(String ip, int port) {
      this.server = new ServerData("", ip + ":" + port, Type.OTHER);
      new Thread(() -> this.pingInCurrentThread(ip, port), "Server Pinger #" + threadNumber.incrementAndGet()).start();
   }

   private void pingInCurrentThread(String ip, int port) {
      ServerStatusPinger pinger = new ServerStatusPinger();
      MeteorRejectsAddon.LOG.info("Pinging {}:{}...", ip, port);

      try {
         pinger.pingServer(this.server, () -> {}, () -> {}, EventLoopGroupHolder.remote(false));
         MeteorRejectsAddon.LOG.info("Ping successful: {}:{}", ip, port);
      } catch (UnknownHostException var5) {
         MeteorRejectsAddon.LOG.warn("Unknown host: {}:{}", ip, port);
         this.failed = true;
      } catch (Exception var6) {
         MeteorRejectsAddon.LOG.warn("Ping failed: {}:{}", ip, port);
         this.failed = true;
      }

      pinger.removeAll();
      this.done = true;
   }

   public boolean isStillPinging() {
      return !this.done;
   }

   public boolean isWorking() {
      return !this.failed;
   }

   public String getServerIP() {
      return this.server.ip;
   }
}
