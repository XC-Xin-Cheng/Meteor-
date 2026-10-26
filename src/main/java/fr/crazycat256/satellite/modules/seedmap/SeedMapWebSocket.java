package fr.crazycat256.satellite.modules.seedmap;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class SeedMapWebSocket extends WebSocketServer {
   private boolean onlyLocal;
   private boolean debugMessages;
   private final List<WebSocket> connections = new ArrayList<>();

   public SeedMapWebSocket(int port, boolean onlyLocal, boolean debugMessages) {
      super(new InetSocketAddress(port));
      this.onlyLocal = onlyLocal;
      this.debugMessages = debugMessages;
      this.setReuseAddr(true);
   }

   public boolean getOnlyLocal() {
      return this.onlyLocal;
   }

   public boolean getDebugMessages() {
      return this.debugMessages;
   }

   public void setOnlyLocal(boolean onlyLocal) {
      this.onlyLocal = onlyLocal;
      if (onlyLocal) {
         for (WebSocket conn : this.connections) {
            if (!getAddr(conn).equals("localhost") && conn.isOpen()) {
               conn.close();
            }
         }
      }
   }

   public void setDebugMessages(boolean debugMessages) {
      this.debugMessages = debugMessages;
   }

   @Override
   public void onOpen(WebSocket conn, ClientHandshake handshake) {
      if (!((SeedMap)Modules.get().get(SeedMap.class)).isActive()) {
         conn.close();
      } else if (this.onlyLocal && !getAddr(conn).equals("localhost")) {
         conn.close();
      } else {
         this.connections.add(conn);
         if (this.debugMessages) {
            ChatUtils.infoPrefix("SeedMap", "New connection from " + getAddr(conn) + ":" + conn.getRemoteSocketAddress().getPort(), new Object[0]);
         }
      }
   }

   @Override
   public void onClose(WebSocket conn, int code, String reason, boolean remote) {
      this.connections.remove(conn);
      if (this.debugMessages) {
         ChatUtils.infoPrefix("SeedMap", "Closed connection to " + getAddr(conn) + ":" + conn.getRemoteSocketAddress().getPort(), new Object[0]);
      }
   }

   @Override
   public void onMessage(WebSocket conn, String message) {
      if (this.debugMessages) {
         ChatUtils.infoPrefix("SeedMap", getAddr(conn) + ":" + conn.getRemoteSocketAddress().getPort() + " : " + message, new Object[0]);
      }
   }

   @Override
   public void onError(WebSocket conn, Exception ex) {
   }

   @Override
   public void onStart() {
      this.setConnectionLostTimeout(5);
   }

   public static String getAddr(WebSocket conn) {
      String addr = conn.getRemoteSocketAddress().getAddress().getHostAddress();
      if (addr.equals("0:0:0:0:0:0:0:1") || addr.equals("127.0.0.1")) {
         addr = "localhost";
      }

      return addr;
   }

   public void closeAll() {
      for (WebSocket conn : this.connections) {
         conn.close();
      }

      this.connections.clear();
   }
}
