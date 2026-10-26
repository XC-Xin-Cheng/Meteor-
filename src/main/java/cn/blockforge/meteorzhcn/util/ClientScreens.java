package cn.blockforge.meteorzhcn.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ClientScreens {
   private static Method setScreen;
   private static Object setScreenTarget;
   private static boolean resolved;

   private ClientScreens() {
   }

   public static void open(Screen screen) {
      Minecraft minecraft = Minecraft.getInstance();
      if (!resolved) {
         resolve(minecraft);
      }

      try {
         setScreen.invoke(setScreenTarget, screen);
      } catch (IllegalAccessException var5) {
         throw new RuntimeException("无法打开自定义界面", var5);
      } catch (InvocationTargetException var6) {
         Throwable cause = var6.getCause();
         throw cause instanceof RuntimeException runtime ? runtime : new RuntimeException("无法打开自定义界面", cause);
      }
   }

   private static void resolve(Minecraft minecraft) {
      resolved = true;

      try {
         setScreen = Minecraft.class.getMethod("setScreen", Screen.class);
         setScreenTarget = minecraft;
      } catch (NoSuchMethodException var3) {
         try {
            Object gui = Minecraft.class.getField("gui").get(minecraft);
            setScreen = gui.getClass().getMethod("setScreen", Screen.class);
            setScreenTarget = gui;
         } catch (ReflectiveOperationException var2) {
            throw new RuntimeException("当前版本的 Minecraft 找不到可用的 setScreen 方法", var2);
         }
      }
   }
}
