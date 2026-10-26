package org.java_websocket.handshake;

import java.util.Iterator;

public interface Handshakedata {
   Iterator<String> iterateHttpFields();

   String getFieldValue(String var1);

   boolean hasFieldValue(String var1);

   byte[] getContent();
}
