package fr.crazycat256.satellite.modules;

import fr.crazycat256.satellite.Addon;
import java.lang.reflect.Field;
import meteordevelopment.meteorclient.systems.modules.Module;

public class UiUtils extends Module {
   public static final Class<?> uiUtilsClass;
   private Field enabled;

   public UiUtils() {
      super(Addon.CATEGORY, "ui-utils", "Implementation of UI-TPUtils mod.");

      try {
         this.enabled = uiUtilsClass.getDeclaredField("enabled");
         this.enabled.setAccessible(true);
      } catch (NoSuchFieldException var2) {
      }

      this.set(this.isActive());
   }

   public void onActivate() {
      this.set(true);
   }

   public void onDeactivate() {
      this.set(false);
   }

   private void set(boolean value) {
      try {
         this.enabled.set(null, value);
      } catch (IllegalAccessException var3) {
      }
   }

   static {
      Class<?> klass;
      try {
         klass = Class.forName("org.uiutils.SharedVariables");
      } catch (ClassNotFoundException var2) {
         klass = null;
      }

      uiUtilsClass = klass;
   }
}
