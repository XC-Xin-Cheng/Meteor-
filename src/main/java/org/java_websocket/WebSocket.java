package org.java_websocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import javax.net.ssl.SSLSession;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.Opcode;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.framing.Framedata;
import org.java_websocket.protocols.IProtocol;

public interface WebSocket {
   void close(int var1, String var2);

   void close(int var1);

   void close();

   void closeConnection(int var1, String var2);

   void send(String var1);

   void send(ByteBuffer var1);

   void send(byte[] var1);

   void sendFrame(Framedata var1);

   void sendFrame(Collection<Framedata> var1);

   void sendPing();

   void sendFragmentedFrame(Opcode var1, ByteBuffer var2, boolean var3);

   boolean hasBufferedData();

   InetSocketAddress getRemoteSocketAddress();

   InetSocketAddress getLocalSocketAddress();

   boolean isOpen();

   boolean isClosing();

   boolean isFlushAndClose();

   boolean isClosed();

   Draft getDraft();

   ReadyState getReadyState();

   String getResourceDescriptor();

   <T> void setAttachment(T var1);

   <T> T getAttachment();

   boolean hasSSLSupport();

   SSLSession getSSLSession() throws IllegalArgumentException;

   IProtocol getProtocol();
}
