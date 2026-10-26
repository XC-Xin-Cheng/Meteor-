package cn.blockforge.meteorzhcn.font_fix;

import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackRange;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.stb.STBTTPackedchar.Buffer;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FontFix {
   public static final Logger LOG = LoggerFactory.getLogger("Meteor-I18n-XC/font");
   public final Texture texture;
   private final int height;
   private final float scale;
   private final float ascent;
   private final Int2ObjectOpenHashMap<FontFix.CharData> charMap = new Int2ObjectOpenHashMap();
   private static final int size = 2048;
   private static final int MAX_REGION_PIXELS = 1048576;
   private static final int CACHE_LIMIT = 4096;
   private static final int CACHE_MAX_LENGTH = 256;
   private static final int PACK_CHUNK_SIZE = 1024;
   private static Method regionUpload;
   private static boolean regionUploadResolved;
   private final ByteBuffer buffer;
   private final STBTTFontinfo fontInfo;
   private final ByteBuffer bitmap;
   private final STBTTPackContext packContext;
   private final ObjectOpenHashSet<String> loadedStrings = new ObjectOpenHashSet();
   private final Object2DoubleOpenHashMap<String> widthCache = new Object2DoubleOpenHashMap();
   private ByteBuffer uploadBuffer;
   private boolean closed = false;

   public FontFix(ByteBuffer buffer, int height, Texture texture) {
      this.buffer = buffer;
      this.height = height;
      this.texture = texture;
      this.fontInfo = STBTTFontinfo.create();
      if (!STBTruetype.stbtt_InitFont(this.fontInfo, buffer)) {
         throw new IllegalArgumentException("无法解析字体文件（stbtt_InitFont 失败）");
      } else {
         this.bitmap = BufferUtils.createByteBuffer(4194304);
         this.packContext = STBTTPackContext.create();
         STBTruetype.stbtt_PackBegin(this.packContext, this.bitmap, 2048, 2048, 0, 1);
         this.scale = STBTruetype.stbtt_ScaleForPixelHeight(this.fontInfo, height);
         MemoryStack stack = MemoryStack.stackPush();

         try {
            IntBuffer ascent = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(this.fontInfo, ascent, null, null);
            this.ascent = ascent.get(0);
         } finally {
            stack.close();
         }

         this.preloadAsciiCharacters();
      }
   }

   private void preloadAsciiCharacters() {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         Buffer cdata = STBTTPackedchar.malloc(128, stack);
         org.lwjgl.stb.STBTTPackRange.Buffer packRange = STBTTPackRange.malloc(1, stack);
         packRange.put(STBTTPackRange.malloc(stack).set(this.height, 32, null, 128, cdata, (byte)2, (byte)2));
         packRange.flip();
         STBTruetype.stbtt_PackFontRanges(this.packContext, this.buffer, 0, packRange);

         for (int i = 0; i < 128; i++) {
            this.putCharData(i + 32, (STBTTPackedchar)cdata.get(i));
         }
      } finally {
         stack.close();
      }

      this.texture.upload(this.bitmap);
   }

   private void loadCharacter(IntArrayList codePoints) {
      if (!this.closed) {
         IntArrayList missing = new IntArrayList(codePoints.size());
         IntOpenHashSet seen = new IntOpenHashSet(codePoints.size());

         for (int i = 0; i < codePoints.size(); i++) {
            int codePoint = codePoints.getInt(i);
            if (!this.charMap.containsKey(codePoint) && seen.add(codePoint)) {
               missing.add(codePoint);
            }
         }

         int count = missing.size();
         if (count != 0) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int start = 0; start < count; start += 1024) {
               int chunk = Math.min(1024, count - start);
               MemoryStack stack = MemoryStack.stackPush();

               try {
                  IntBuffer codePointBuffer = stack.mallocInt(chunk);

                  for (int ix = 0; ix < chunk; ix++) {
                     codePointBuffer.put(missing.getInt(start + ix));
                  }

                  codePointBuffer.flip();
                  Buffer cdata = STBTTPackedchar.malloc(chunk, stack);
                  org.lwjgl.stb.STBTTPackRange.Buffer packRange = STBTTPackRange.malloc(1, stack);
                  packRange.put(STBTTPackRange.malloc(stack).set(this.height, 0, codePointBuffer, chunk, cdata, (byte)2, (byte)2));
                  packRange.flip();
                  STBTruetype.stbtt_PackFontRanges(this.packContext, this.buffer, 0, packRange);

                  for (int ix = 0; ix < chunk; ix++) {
                     STBTTPackedchar packedChar = (STBTTPackedchar)cdata.get(ix);
                     this.putCharData(missing.getInt(start + ix), packedChar);
                     minX = Math.min(minX, packedChar.x0());
                     minY = Math.min(minY, packedChar.y0());
                     maxX = Math.max(maxX, packedChar.x1());
                     maxY = Math.max(maxY, packedChar.y1());
                  }
               } finally {
                  stack.close();
               }
            }

            this.uploadRegion(minX, minY, maxX - minX, maxY - minY);
         }
      }
   }

   private void putCharData(int codePoint, STBTTPackedchar packedChar) {
      float ipw = 4.8828125E-4F;
      float iph = 4.8828125E-4F;
      this.charMap
         .put(
            codePoint,
            new FontFix.CharData(
               packedChar.xoff(),
               packedChar.yoff(),
               packedChar.xoff2(),
               packedChar.yoff2(),
               packedChar.x0() * ipw,
               packedChar.y0() * iph,
               packedChar.x1() * ipw,
               packedChar.y1() * iph,
               packedChar.xadvance()
            )
         );
   }

   private void uploadRegion(int x, int y, int width, int height) {
      if (width > 0 && height > 0) {
         if ((long)width * height > 1048576L) {
            this.texture.upload(this.bitmap);
         } else {
            ByteBuffer destination = this.uploadBuffer;
            if (destination == null || destination.capacity() < width * height) {
               destination = BufferUtils.createByteBuffer(width * height);
               this.uploadBuffer = destination;
            }

            destination.clear();
            ByteBuffer source = this.bitmap;
            int previousLimit = source.limit();

            for (int row = 0; row < height; row++) {
               int offset = (y + row) * 2048 + x;
               source.limit(offset + width).position(offset);
               destination.put(source);
            }

            source.limit(previousLimit).position(0);
            destination.flip();
            Method upload = regionUpload();
            if (upload == null) {
               this.texture.upload(this.bitmap);
            } else {
               try {
                  Object encoder = RenderSystem.getDevice().createCommandEncoder();
                  if (upload.getParameterCount() == 9) {
                     upload.invoke(encoder, this.texture.getTexture(), destination, Format.LUMINANCE, 0, 0, x, y, width, height);
                  } else {
                     upload.invoke(encoder, this.texture.getTexture(), destination, 0, 0, x, y, width, height);
                  }
               } catch (Throwable var10) {
                  LOG.warn("按矩形上传字形失败，退回整张图集上传", var10);
                  this.texture.upload(this.bitmap);
               }
            }
         }
      }
   }

   private static Method regionUpload() {
      if (!regionUploadResolved) {
         regionUploadResolved = true;

         for (Method method : CommandEncoder.class.getMethods()) {
            Class<?>[] parameters = method.getParameterTypes();
            if (method.getName().equals("writeToTexture")) {
               if (parameters.length == 8 && parameters[1] == ByteBuffer.class) {
                  regionUpload = method;
                  break;
               }

               if (parameters.length == 9 && parameters[1] == ByteBuffer.class && parameters[2].isEnum()) {
                  regionUpload = method;
               }
            }
         }
      }

      return regionUpload;
   }

   public double getWidth(String string, int length) {
      boolean cacheable = length == string.length() && string.length() <= 256;
      if (cacheable && this.widthCache.containsKey(string)) {
         return this.widthCache.getDouble(string);
      } else {
         boolean ready = this.ensureGlyphs(string);
         double width = 0.0;

         for (int i = 0; i < length; i++) {
            FontFix.CharData c = (FontFix.CharData)this.charMap.get(string.charAt(i));
            if (c == null) {
               c = (FontFix.CharData)this.charMap.get(32);
            }

            if (c != null) {
               width += c.xAdvance;
            }
         }

         if (cacheable && ready) {
            this.widthCache.put(string, width);
         }

         return width;
      }
   }

   public int getHeight() {
      return this.height;
   }

   public void close() {
      if (!this.closed) {
         this.closed = true;
         STBTruetype.stbtt_PackEnd(this.packContext);
         this.charMap.clear();
         this.loadedStrings.clear();
         this.widthCache.clear();
         this.uploadBuffer = null;
      }
   }

   private boolean ensureGlyphs(String s) {
      if (this.loadedStrings.contains(s)) {
         return true;
      } else {
         IntArrayList charPoints = null;

         for (int i = 0; i < s.length(); i++) {
            int cp = s.charAt(i);
            if (this.charMap.get(cp) == null) {
               if (charPoints == null) {
                  charPoints = new IntArrayList();
               }

               charPoints.add(cp);
            }
         }

         if (charPoints == null) {
            this.rememberLoaded(s);
            return true;
         } else {
            this.loadCharacter(charPoints);

            for (int ix = 0; ix < charPoints.size(); ix++) {
               if (this.charMap.get(charPoints.getInt(ix)) == null) {
                  return false;
               }
            }

            this.rememberLoaded(s);
            return true;
         }
      }
   }

   private void rememberLoaded(String s) {
      if (s.length() <= 256) {
         if (this.loadedStrings.size() >= 4096) {
            this.loadedStrings.clear();
            this.widthCache.clear();
         }

         this.loadedStrings.add(s);
      }
   }

   public double render(MeshBuilder mesh, String string, double x, double y, Color color, double scale) {
      this.ensureGlyphs(string);
      y += this.ascent * this.scale * scale;
      int length = string.length();
      mesh.ensureCapacity(length * 4, length * 6);

      for (int i = 0; i < length; i++) {
         int cp = string.charAt(i);
         FontFix.CharData c = (FontFix.CharData)this.charMap.get(cp);
         if (c == null) {
            c = (FontFix.CharData)this.charMap.get(32);
         }

         if (c != null) {
            mesh.quad(
               mesh.vec2(x + c.x0 * scale, y + c.y0 * scale).vec2(c.u0, c.v0).color(color).next(),
               mesh.vec2(x + c.x0 * scale, y + c.y1 * scale).vec2(c.u0, c.v1).color(color).next(),
               mesh.vec2(x + c.x1 * scale, y + c.y1 * scale).vec2(c.u1, c.v1).color(color).next(),
               mesh.vec2(x + c.x1 * scale, y + c.y0 * scale).vec2(c.u1, c.v0).color(color).next()
            );
            x += c.xAdvance * scale;
         }
      }

      return x;
   }

   private record CharData(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float xAdvance) {
   }
}
