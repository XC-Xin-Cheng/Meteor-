package org.fusesource.hawtjni.runtime;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class Library {
   public static final String STRATEGY_PROPERTY = "hawtjni.strategy";
   public static final String STRATEGY_SHA1 = "sha1";
   public static final String STRATEGY_TEMP = "temp";
   private static Path tempExtractDir = null;
   static final String SLASH = System.getProperty("file.separator");
   static final String STRATEGY = System.getProperty("hawtjni.strategy", "windows".equals(getOperatingSystem()) ? "sha1" : "temp");
   private final String name;
   private final String version;
   private final ClassLoader classLoader;
   private boolean loaded;
   private String nativeLibraryPath;
   private URL nativeLibrarySourceUrl;

   public Library(String name) {
      this(name, null, null);
   }

   public Library(String name, Class<?> clazz) {
      this(name, version(clazz), clazz.getClassLoader());
   }

   public Library(String name, String version) {
      this(name, version, null);
   }

   public Library(String name, String version, ClassLoader classLoader) {
      if (name == null) {
         throw new IllegalArgumentException("name cannot be null");
      } else {
         this.name = name;
         this.version = version;
         this.classLoader = classLoader;
      }
   }

   private static String version(Class<?> clazz) {
      try {
         return clazz.getPackage().getImplementationVersion();
      } catch (Throwable var2) {
         return null;
      }
   }

   public String getNativeLibraryPath() {
      return this.nativeLibraryPath;
   }

   public URL getNativeLibrarySourceUrl() {
      return this.nativeLibrarySourceUrl;
   }

   public static String getOperatingSystem() {
      String name = System.getProperty("os.name").toLowerCase().trim();
      if (name.startsWith("linux")) {
         return "linux";
      } else if (name.startsWith("mac os x")) {
         return "osx";
      } else {
         return name.startsWith("win") ? "windows" : name.replaceAll("\\W+", "_");
      }
   }

   public static String getPlatform() {
      return getOperatingSystem() + getBitModel();
   }

   public static int getBitModel() {
      String prop = System.getProperty("sun.arch.data.model");
      if (prop == null) {
         prop = System.getProperty("com.ibm.vm.bitmode");
      }

      if (prop != null) {
         return Integer.parseInt(prop);
      } else {
         String arch = System.getProperty("os.arch");
         return arch.endsWith("64") && "Substrate VM".equals(System.getProperty("java.vm.name")) ? 64 : 64;
      }
   }

   public synchronized void load() {
      if (!this.loaded) {
         this.doLoad();
         this.loaded = true;
      }
   }

   private void doLoad() {
      String version = System.getProperty("library." + this.name + ".version");
      if (version == null) {
         version = this.version;
      }

      ArrayList<Throwable> errors = new ArrayList<>();
      String[] specificDirs = this.getSpecificSearchDirs();
      String libFilename = this.map(this.name);
      String versionlibFilename = version == null ? null : this.map(this.name + "-" + version);
      String customPath = System.getProperty("library." + this.name + ".path");
      if (customPath != null) {
         for (String dir : specificDirs) {
            if (version != null && this.load(errors, this.file(customPath, dir, versionlibFilename))) {
               return;
            }

            if (this.load(errors, this.file(customPath, dir, libFilename))) {
               return;
            }
         }
      }

      if (version == null || !this.loadLibrary(errors, this.name + getBitModel() + "-" + version)) {
         if (version == null || !this.loadLibrary(errors, this.name + "-" + version)) {
            if (!this.loadLibrary(errors, this.name)) {
               if (this.classLoader != null) {
                  String targetLibName = version != null ? versionlibFilename : libFilename;

                  for (String dir : specificDirs) {
                     if (version != null && this.extractAndLoad(errors, customPath, dir, versionlibFilename, targetLibName)) {
                        return;
                     }

                     if (this.extractAndLoad(errors, customPath, dir, libFilename, targetLibName)) {
                        return;
                     }
                  }
               }

               UnsatisfiedLinkError e = new UnsatisfiedLinkError("Could not load library. Reasons: " + errors.toString());

               try {
                  Method method = Throwable.class.getMethod("addSuppressed", Throwable.class);

                  for (Throwable t : errors) {
                     method.invoke(e, t);
                  }
               } catch (Throwable var12) {
               }

               throw e;
            }
         }
      }
   }

   @Deprecated
   public final String getArchSpecifcResourcePath() {
      return this.getArchSpecificResourcePath();
   }

   public final String getArchSpecificResourcePath() {
      return "META-INF/native/" + getPlatform() + "/" + System.getProperty("os.arch") + "/" + this.map(this.name);
   }

   @Deprecated
   public final String getOperatingSystemSpecifcResourcePath() {
      return this.getOperatingSystemSpecificResourcePath();
   }

   public final String getOperatingSystemSpecificResourcePath() {
      return this.getPlatformSpecificResourcePath(getOperatingSystem());
   }

   @Deprecated
   public final String getPlatformSpecifcResourcePath() {
      return this.getPlatformSpecificResourcePath();
   }

   public final String getPlatformSpecificResourcePath() {
      return this.getPlatformSpecificResourcePath(getPlatform());
   }

   @Deprecated
   public final String getPlatformSpecifcResourcePath(String platform) {
      return this.getPlatformSpecificResourcePath(platform);
   }

   public final String getPlatformSpecificResourcePath(String platform) {
      return "META-INF/native/" + platform + "/" + this.map(this.name);
   }

   @Deprecated
   public final String getResorucePath() {
      return this.getResourcePath();
   }

   public final String getResourcePath() {
      return "META-INF/native/" + this.map(this.name);
   }

   public final String getLibraryFileName() {
      return this.map(this.name);
   }

   public final String[] getSpecificSearchDirs() {
      return new String[]{getPlatform() + "/" + System.getProperty("os.arch"), getPlatform(), getOperatingSystem(), "."};
   }

   private boolean extractAndLoad(ArrayList<Throwable> errors, String customPath, String dir, String libName, String targetLibName) {
      String resourcePath = "META-INF/native/" + (dir == null ? "" : dir + '/') + libName;
      URL resource = this.classLoader.getResource(resourcePath);
      if (resource != null) {
         int idx = targetLibName.lastIndexOf(46);
         String prefix = targetLibName.substring(0, idx) + "-";

         for (File path : Arrays.asList(
            customPath != null ? this.file(customPath) : null,
            this.file(System.getProperty("java.io.tmpdir")),
            this.file(System.getProperty("user.home"), ".hawtjni", this.name)
         )) {
            if (path != null) {
               File target;
               if ("sha1".equals(STRATEGY)) {
                  target = this.extractSha1(errors, resource, prefix, targetLibName, path);
               } else {
                  target = this.extractTemp(errors, resource, prefix, targetLibName, path);
               }

               if (target != null && this.load(errors, target)) {
                  this.nativeLibrarySourceUrl = resource;
                  return true;
               }
            }
         }
      }

      return false;
   }

   private File file(String... paths) {
      File rc = null;

      for (String path : paths) {
         if (rc == null) {
            rc = new File(path);
         } else if (path != null) {
            rc = new File(rc, path);
         }
      }

      return rc;
   }

   private String map(String libName) {
      return System.mapLibraryName(libName);
   }

   private File extractSha1(ArrayList<Throwable> errors, URL source, String prefix, String suffix, File directory) {
      File target = null;
      directory = directory.getAbsoluteFile();
      if (!directory.exists() && !directory.mkdirs()) {
         errors.add(new IOException("Unable to create directory: " + directory));
         return null;
      } else {
         try {
            String sha1 = this.computeSha1(source.openStream());
            String sha1f = "";
            target = new File(directory, prefix + sha1 + suffix);
            if (target.isFile() && target.canRead()) {
               sha1f = this.computeSha1(new FileInputStream(target));
            }

            if (sha1f.equals(sha1)) {
               return target;
            } else {
               FileOutputStream os = null;
               InputStream is = null;

               File var20;
               try {
                  is = source.openStream();
                  if (is != null) {
                     byte[] buffer = new byte[4096];
                     os = new FileOutputStream(target);

                     int read;
                     while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                     }

                     this.chmod755(target);
                  }

                  var20 = target;
               } finally {
                  close(os);
                  close(is);
               }

               return var20;
            }
         } catch (Throwable var17) {
            IOException io;
            if (target != null) {
               target.delete();
               io = new IOException("Unable to extract library from " + source + " to " + target);
            } else {
               io = new IOException("Unable to create temporary file in " + directory);
            }

            io.initCause(var17);
            errors.add(io);
            return null;
         }
      }
   }

   private String computeSha1(InputStream is) throws NoSuchAlgorithmException, IOException {
      String sha1;
      try {
         MessageDigest mDigest = MessageDigest.getInstance("SHA1");
         byte[] buffer = new byte[4096];

         int read;
         while ((read = is.read(buffer)) != -1) {
            mDigest.update(buffer, 0, read);
         }

         byte[] result = mDigest.digest();
         StringBuilder sb = new StringBuilder();

         for (byte b : result) {
            sb.append(Integer.toString((b & 255) + 256, 16).substring(1));
         }

         sha1 = sb.toString();
      } finally {
         close(is);
      }

      return sha1;
   }

   private File extractTemp(ArrayList<Throwable> errors, URL source, String prefix, String targetLibName, File directory) {
      File target = null;
      directory = directory.getAbsoluteFile();
      if (!directory.exists() && !directory.mkdirs()) {
         errors.add(new IOException("Unable to create directory: " + directory));
         return null;
      } else {
         try {
            if (tempExtractDir == null) {
               tempExtractDir = Files.createTempDirectory(directory.toPath(), prefix);
            }

            FileOutputStream os = null;
            InputStream is = null;

            File var19;
            try {
               Path targetPath = Paths.get(tempExtractDir.toString(), targetLibName);
               target = Files.createFile(targetPath).toFile();
               is = source.openStream();
               if (is != null) {
                  byte[] buffer = new byte[4096];
                  os = new FileOutputStream(target);

                  int read;
                  while ((read = is.read(buffer)) != -1) {
                     os.write(buffer, 0, read);
                  }

                  this.chmod755(target);
               }

               target.deleteOnExit();
               var19 = target;
            } finally {
               close(os);
               close(is);
            }

            return var19;
         } catch (Throwable var16) {
            IOException io;
            if (target != null) {
               target.delete();
               io = new IOException("Unable to extract library from " + source + " to " + target);
            } else {
               io = new IOException("Unable to create temporary file in " + directory);
            }

            io.initCause(var16);
            errors.add(io);
            return null;
         }
      }
   }

   private static void close(Closeable file) {
      if (file != null) {
         try {
            file.close();
         } catch (Exception var2) {
         }
      }
   }

   private void chmod755(File file) {
      if (!getPlatform().startsWith("windows")) {
         try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            Class<?> posixFilePermissionsClass = classLoader.loadClass("java.nio.file.attribute.PosixFilePermissions");
            Method fromStringMethod = posixFilePermissionsClass.getMethod("fromString", String.class);
            Object permissionSet = fromStringMethod.invoke(null, "rwxr-xr-x");
            Object path = file.getClass().getMethod("toPath").invoke(file);
            Class<?> pathClass = classLoader.loadClass("java.nio.file.Path");
            Class<?> filesClass = classLoader.loadClass("java.nio.file.Files");
            Method setPosixFilePermissionsMethod = filesClass.getMethod("setPosixFilePermissions", pathClass, Set.class);
            setPosixFilePermissionsMethod.invoke(null, path, permissionSet);
         } catch (Throwable var11) {
            try {
               Runtime.getRuntime().exec(new String[]{"chmod", "755", file.getCanonicalPath()}).waitFor();
            } catch (Throwable var10) {
            }
         }
      }
   }

   private boolean load(ArrayList<Throwable> errors, File lib) {
      try {
         System.load(lib.getPath());
         this.nativeLibraryPath = lib.getPath();
         return true;
      } catch (UnsatisfiedLinkError var5) {
         LinkageError le = new LinkageError("Unable to load library from " + lib);
         le.initCause(var5);
         errors.add(le);
         return false;
      }
   }

   private boolean loadLibrary(ArrayList<Throwable> errors, String lib) {
      try {
         System.loadLibrary(lib);
         this.nativeLibraryPath = "java.library.path,sun.boot.library.pathlib:" + lib;
         return true;
      } catch (UnsatisfiedLinkError var5) {
         LinkageError le = new LinkageError("Unable to load library " + lib);
         le.initCause(var5);
         errors.add(le);
         return false;
      }
   }
}
