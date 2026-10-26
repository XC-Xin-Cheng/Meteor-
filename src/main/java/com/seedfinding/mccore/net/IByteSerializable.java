package com.seedfinding.mccore.net;

import java.io.IOException;

public interface IByteSerializable {
   void read(ByteBuffer var1) throws IOException;

   void write(ByteBuffer var1) throws IOException;
}
