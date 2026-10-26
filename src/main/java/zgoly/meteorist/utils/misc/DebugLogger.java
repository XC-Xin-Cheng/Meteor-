package zgoly.meteorist.utils.misc;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.chat.Component;

public class DebugLogger {
   private final Module module;
   private final Setting<Boolean> printDebugInfo;

   public DebugLogger(Module module, Settings settings) {
      this.module = module;
      SettingGroup sgDebug = settings.createGroup("Debug");
      this.printDebugInfo = sgDebug.add(
         ((Builder)((Builder)((Builder)new Builder().name("print-debug-info")).description("Prints debug information in the chat.")).defaultValue(false))
            .build()
      );
   }

   public void info(Component message) {
      if ((Boolean)this.printDebugInfo.get()) {
         this.module.info(message);
      }
   }

   public void info(String message, Object... args) {
      if ((Boolean)this.printDebugInfo.get()) {
         this.module.info(message, args);
      }
   }

   public void warning(String message, Object... args) {
      if ((Boolean)this.printDebugInfo.get()) {
         this.module.warning(message, args);
      }
   }

   public void error(String message, Object... args) {
      if ((Boolean)this.printDebugInfo.get()) {
         this.module.error(message, args);
      }
   }

   public boolean isDebugEnabled() {
      return (Boolean)this.printDebugInfo.get();
   }

   public void toggleDebug() {
      this.printDebugInfo.set(!(Boolean)this.printDebugInfo.get());
   }
}
