package zgoly.meteorist.modules.autologin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.utils.config.MeteoristConfig;
import zgoly.meteorist.utils.config.MeteoristConfigManager;
import zgoly.meteorist.utils.misc.DebugLogger;

public class AutoLogin extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> autoSave = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("auto-save")).description("Automatically saves passwords when you login or register."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> ignoreSelf = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("ignore-self"))
                     .description("Ignore self commands. Recommended to leave enabled, otherwise Auto Save will trigger on Auto Login commands."))
                  .defaultValue(true))
               .visible(this.autoSave::get))
            .build()
      );
   private final Setting<String> loginCommand = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("login-command"))
                     .description("Command to login."))
                  .defaultValue("/login"))
               .visible(this.autoSave::get))
            .build()
      );
   private final Setting<List<String>> commandsToHandle = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                     .name("commands-to-handle"))
                  .description("Commands to handle."))
               .defaultValue(new String[]{"login", "log", "l", "register", "reg"})
               .visible(this.autoSave::get))
            .build()
      );
   private final Setting<Boolean> saveServerIp = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("save-server-ip")).description("Save server IP in filter.")).defaultValue(true))
               .visible(this.autoSave::get))
            .build()
      );
   private final Setting<Boolean> saveUsername = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("save-username")).description("Save username in filter.")).defaultValue(true))
               .visible(this.autoSave::get))
            .build()
      );
   private final Setting<Boolean> checkPasswordCommand = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("check-password-command"))
                     .description("Whether to check password command when adding new auto login."))
                  .defaultValue(true))
               .visible(this.autoSave::get))
            .build()
      );
   private final Setting<Boolean> checkExecutionMode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("check-execution-mode"))
                     .description("Whether to check execution mode when adding new auto login."))
                  .defaultValue(false))
               .visible(this.autoSave::get))
            .build()
      );
   private final Setting<Boolean> checkDelay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("check-delay")).description("Whether to check delay when adding new auto login."))
                  .defaultValue(false))
               .visible(this.autoSave::get))
            .build()
      );
   private final Setting<Boolean> checkUsername = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("check-username")).description("Whether to check username when adding new auto login."))
                  .defaultValue(false))
               .visible(this.autoSave::get))
            .build()
      );
   private final Setting<Boolean> checkServerIp = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("check-server-ip")).description("Whether to check server IP when adding new auto login."))
                  .defaultValue(false))
               .visible(this.autoSave::get))
            .build()
      );
   private final Setting<Boolean> checkLastLogin = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("check-last-login")).description("Whether to check last login when adding new auto login."))
                  .defaultValue(false))
               .visible(this.autoSave::get))
            .build()
      );
   public static List<BaseAutoLogin> autoLogins = new ArrayList<>();
   private long loginStartTime = -1L;
   private boolean shouldContinueProcessing = true;
   private boolean initialized = true;
   private boolean isSendingChatMessage = false;
   private final DebugLogger debugLogger = new DebugLogger(this, this.settings);

   public AutoLogin() {
      super(Meteorist.CATEGORY, "auto-login", "Automatically logs in your account using /login.");
   }

   public CompoundTag toTag() {
      CompoundTag superTag = super.toTag();
      CompoundTag tag = new CompoundTag();
      ListTag list = new ListTag();

      for (BaseAutoLogin autoLogin : autoLogins) {
         CompoundTag mTag = new CompoundTag();
         mTag.put("autoLogin", autoLogin.toTag());
         list.add(mTag);
      }

      tag.put("autoLogins", list);
      MeteoristConfig.save(this.name, "default", tag);
      return superTag;
   }

   public Module fromTag(CompoundTag superTag) {
      CompoundTag tag = MeteoristConfig.load(this.name, "default");
      autoLogins.clear();

      for (Tag tagII : tag.getListOrEmpty("autoLogins")) {
         CompoundTag tagI = (CompoundTag)tagII;
         BaseAutoLogin autoLogin = new BaseAutoLogin();
         CompoundTag autoLoginTag = (CompoundTag)tagI.get("autoLogin");
         if (autoLoginTag != null) {
            autoLogin.fromTag(autoLoginTag);
         }

         autoLogins.add(autoLogin);
      }

      return super.fromTag(superTag);
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      this.fillWidget(theme, list);
      return list;
   }

   private void fillWidget(GuiTheme theme, WVerticalList list) {
      list.clear();

      for (BaseAutoLogin autoLogin : autoLogins) {
         list.add(theme.settings(autoLogin.settings)).expandX();
         WContainer controls = (WContainer)list.add(theme.horizontalList()).widget();
         if (autoLogins.size() > 1) {
            WContainer moveContainer = (WContainer)controls.add(theme.horizontalList()).expandX().widget();
            int index = autoLogins.indexOf(autoLogin);
            if (index > 0) {
               WButton moveUp = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_UP)).expandX().widget();
               moveUp.tooltip = "Move auto login up.";
               moveUp.action = () -> {
                  autoLogins.remove(index);
                  autoLogins.add(index - 1, autoLogin);
                  this.fillWidget(theme, list);
               };
            }

            if (index < autoLogins.size() - 1) {
               WButton moveDown = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_DOWN)).expandX().widget();
               moveDown.tooltip = "Move auto login down.";
               moveDown.action = () -> {
                  autoLogins.remove(index);
                  autoLogins.add(index + 1, autoLogin);
                  this.fillWidget(theme, list);
               };
            }
         }

         WButton copy = (WButton)controls.add(theme.button(GuiRenderer.COPY)).widget();
         copy.tooltip = "Duplicate auto login.";
         copy.action = () -> {
            autoLogins.add(autoLogins.indexOf(autoLogin), autoLogin.copy());
            this.fillWidget(theme, list);
         };
         WMinus remove = (WMinus)controls.add(theme.minus()).widget();
         remove.tooltip = "Remove auto login.";
         remove.action = () -> {
            autoLogins.remove(autoLogin);
            this.fillWidget(theme, list);
         };
      }

      if (!autoLogins.isEmpty()) {
         list.add(theme.horizontalSeparator()).expandX();
      }

      WContainer controls = (WContainer)list.add(theme.horizontalList()).expandX().widget();
      WButton add = (WButton)controls.add(theme.button("New Auto Login")).expandX().widget();
      add.action = () -> {
         BaseAutoLogin autoLogin = new BaseAutoLogin();
         autoLogins.add(autoLogin);
         this.fillWidget(theme, list);
      };
      WButton removeAll = (WButton)controls.add(theme.button("Remove All Auto Logins")).expandX().widget();
      removeAll.action = () -> {
         autoLogins.clear();
         this.fillWidget(theme, list);
      };
      MeteoristConfigManager.configManager(theme, list, this);
   }

   public void onActivate() {
      this.loginStartTime = -1L;
      this.shouldContinueProcessing = true;
      this.initialized = true;
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.initialized) {
         this.loginStartTime = -1L;
         this.shouldContinueProcessing = true;
         this.initialized = false;
      }

      if (this.shouldContinueProcessing) {
         if (this.loginStartTime == -1L) {
            this.loginStartTime = this.mc.level.getGameTime();
         }

         boolean hasRemainingAutoLogins = false;

         for (BaseAutoLogin autoLogin : List.copyOf(autoLogins)) {
            if (this.mc.level.getGameTime() < this.loginStartTime + ((Integer)autoLogin.delay.get()).intValue()) {
               hasRemainingAutoLogins = true;
            } else {
               this.debugLogger.info("Delay check passed");
               BaseAutoLogin.ExecutionMode executionMode = (BaseAutoLogin.ExecutionMode)autoLogin.executionMode.get();
               if ((executionMode != BaseAutoLogin.ExecutionMode.Multiplayer || !this.mc.isLocalServer())
                  && (executionMode != BaseAutoLogin.ExecutionMode.Singleplayer || this.mc.isLocalServer())) {
                  this.debugLogger.info("Execution mode check passed");
                  String serverIpFilter = (String)autoLogin.serverIpFilter.get();
                  if (!serverIpFilter.isEmpty()) {
                     if (!Utils.getWorldName().equals(serverIpFilter)) {
                        continue;
                     }

                     this.debugLogger.info("Server ip check passed");
                  }

                  String usernameFilter = (String)autoLogin.usernameFilter.get();
                  if (!usernameFilter.isEmpty()) {
                     if (!this.mc.getUser().getName().equals(usernameFilter)) {
                        continue;
                     }

                     this.debugLogger.info("Username check passed");
                  }

                  this.isSendingChatMessage = true;
                  ChatUtils.sendPlayerMsg((String)autoLogin.loginCommand.get());
                  this.isSendingChatMessage = false;
                  if ((Boolean)autoLogin.lastLogin.get()) {
                     this.shouldContinueProcessing = false;
                     break;
                  }

                  if (!hasRemainingAutoLogins) {
                     this.shouldContinueProcessing = false;
                  }
               }
            }
         }
      }
   }

   @EventHandler
   private void onPacketSend(Send event) {
      if ((Boolean)this.autoSave.get() && event.packet instanceof ServerboundChatCommandPacket var2) {
         ServerboundChatCommandPacket var10000 = var2;

         try {
            var9 = var10000.command();
         } catch (Throwable var6) {
            throw new MatchException(var6.toString(), var6);
         }

         String autoLogin = var9;
         if ((Boolean)this.ignoreSelf.get() && this.isSendingChatMessage) {
            return;
         }

         String[] args = autoLogin.split(" ");
         if (args.length >= 2 && ((List)this.commandsToHandle.get()).contains(args[0])) {
            BaseAutoLogin autoLoginx = new BaseAutoLogin();
            autoLoginx.loginCommand.set((String)this.loginCommand.get() + " " + args[1]);
            if ((Boolean)this.saveUsername.get()) {
               autoLoginx.usernameFilter.set(this.mc.getUser().getName());
            }

            if ((Boolean)this.saveServerIp.get() && this.mc.getSingleplayerServer() != null) {
               autoLoginx.serverIpFilter.set(Utils.getWorldName());
            }

            if (!this.exists(autoLoginx)) {
               autoLogins.add(autoLoginx);
            }
         }
      }
   }

   public boolean exists(BaseAutoLogin toCheck) {
      for (BaseAutoLogin autoLogin : List.copyOf(autoLogins)) {
         boolean allChecksPassed = true;
         if ((Boolean)this.checkPasswordCommand.get()) {
            allChecksPassed &= ((String)autoLogin.loginCommand.get()).equals(toCheck.loginCommand.get());
         }

         if ((Boolean)this.checkExecutionMode.get()) {
            allChecksPassed &= autoLogin.executionMode.get() == toCheck.executionMode.get();
         }

         if ((Boolean)this.checkDelay.get()) {
            allChecksPassed &= Objects.equals(autoLogin.delay.get(), toCheck.delay.get());
         }

         if ((Boolean)this.checkUsername.get()) {
            allChecksPassed &= ((String)autoLogin.usernameFilter.get()).equals(toCheck.usernameFilter.get());
         }

         if ((Boolean)this.checkServerIp.get()) {
            allChecksPassed &= ((String)autoLogin.serverIpFilter.get()).equals(toCheck.serverIpFilter.get());
         }

         if ((Boolean)this.checkLastLogin.get()) {
            allChecksPassed &= autoLogin.lastLogin.get() == toCheck.lastLogin.get();
         }

         if (allChecksPassed) {
            return true;
         }
      }

      return false;
   }
}
