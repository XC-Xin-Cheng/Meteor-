package org.java_websocket.extensions;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;

public interface IExtension {
   void decodeFrame(Framedata var1) throws InvalidDataException;

   void encodeFrame(Framedata var1);

   boolean acceptProvidedExtensionAsServer(String var1);

   boolean acceptProvidedExtensionAsClient(String var1);

   void isFrameValid(Framedata var1) throws InvalidDataException;

   String getProvidedExtensionAsClient();

   String getProvidedExtensionAsServer();

   IExtension copyInstance();

   void reset();

   @Override
   String toString();
}
