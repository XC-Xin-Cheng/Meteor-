package zgoly.meteorist.modules.autologin;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.CompoundTag;

public class BaseAutoLogin implements ISerializable<BaseAutoLogin> {
   Settings settings = new Settings();
   SettingGroup sgAutoLogin = this.settings.createGroup("Auto Login");
   public final Setting<String> loginCommand = this.sgAutoLogin
      .add(((Builder)((Builder)new Builder().name("login-command")).description("Command to login.")).placeholder("/login 12345678").build());
   public final Setting<BaseAutoLogin.ExecutionMode> executionMode = this.sgAutoLogin
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("execution-mode"))
                  .description("Execution mode."))
               .defaultValue(BaseAutoLogin.ExecutionMode.Multiplayer))
            .build()
      );
   public final Setting<Integer> delay = this.sgAutoLogin
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay"))
                  .description("Delay in ticks before logging in."))
               .defaultValue(20))
            .min(0)
            .sliderRange(0, 20)
            .build()
      );
   public final Setting<String> usernameFilter = this.sgAutoLogin
      .add(
         ((Builder)((Builder)new Builder().name("username-filter")).description("Username to check when logging in. Leave it empty if you don't need it."))
            .placeholder("Keep empty to skip")
            .build()
      );
   public final Setting<String> serverIpFilter = this.sgAutoLogin
      .add(
         ((Builder)((Builder)new Builder().name("server-ip-filter")).description("Server IP to check when logging in. Leave it empty if you don't need it."))
            .placeholder("Keep empty to skip")
            .build()
      );
   public final Setting<Boolean> lastLogin = this.sgAutoLogin
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                  .name("last-login"))
               .description("Enable if this password is the last Auto Login to use."))
            .build()
      );

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.put("settings", this.settings.toTag());
      return tag;
   }

   public BaseAutoLogin fromTag(CompoundTag tag) {
      CompoundTag settingsTag = (CompoundTag)tag.get("settings");
      if (settingsTag != null) {
         this.settings.fromTag(settingsTag);
      }

      return this;
   }

   public BaseAutoLogin copy() {
      return new BaseAutoLogin().fromTag(this.toTag());
   }

   public static enum ExecutionMode {
      Multiplayer,
      Singleplayer,
      Both;
   }
}
