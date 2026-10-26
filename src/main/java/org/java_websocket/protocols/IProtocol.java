package org.java_websocket.protocols;

public interface IProtocol {
   boolean acceptProvidedProtocol(String var1);

   String getProvidedProtocol();

   IProtocol copyInstance();

   @Override
   String toString();
}
