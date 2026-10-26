package org.java_websocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

public interface WebSocketListener {
   ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket var1, Draft var2, ClientHandshake var3) throws InvalidDataException;

   void onWebsocketHandshakeReceivedAsClient(WebSocket var1, ClientHandshake var2, ServerHandshake var3) throws InvalidDataException;

   void onWebsocketHandshakeSentAsClient(WebSocket var1, ClientHandshake var2) throws InvalidDataException;

   void onWebsocketMessage(WebSocket var1, String var2);

   void onWebsocketMessage(WebSocket var1, ByteBuffer var2);

   void onWebsocketOpen(WebSocket var1, Handshakedata var2);

   void onWebsocketClose(WebSocket var1, int var2, String var3, boolean var4);

   void onWebsocketClosing(WebSocket var1, int var2, String var3, boolean var4);

   void onWebsocketCloseInitiated(WebSocket var1, int var2, String var3);

   void onWebsocketError(WebSocket var1, Exception var2);

   void onWebsocketPing(WebSocket var1, Framedata var2);

   PingFrame onPreparePing(WebSocket var1);

   void onWebsocketPong(WebSocket var1, Framedata var2);

   void onWriteDemand(WebSocket var1);

   InetSocketAddress getLocalSocketAddress(WebSocket var1);

   InetSocketAddress getRemoteSocketAddress(WebSocket var1);
}
