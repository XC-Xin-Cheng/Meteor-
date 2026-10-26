package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.settings.StringMapSetting;
import anticope.rejects.utils.RejectsUtils;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

public class AutoLogin extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("delay")).description("Delay in ms before executing the command.")).defaultValue(1000))
            .min(0)
            .sliderMax(10000)
            .build()
      );
   private final Setting<Boolean> smart = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("smart"))
                  .description("Auto add the entries."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Map<String, String>> commands = this.sgGeneral
      .add(
         ((StringMapSetting.Builder)((StringMapSetting.Builder)new StringMapSetting.Builder().name("commands"))
               .description("Server and commands. (* for universal)"))
            .defaultValue(new LinkedHashMap<String, String>() {
               {
                  Objects.requireNonNull(AutoLogin.this);
                  this.put("localhost", "/login 123456");
               }
            })
            .build()
      );
   private final Timer timer = new Timer();

   public AutoLogin() {
      super(MeteorRejectsAddon.CATEGORY, "auto-login", "Runs command when joining specified server.");
      this.runInMainMenu = true;
   }

   public WWidget getWidget(GuiTheme theme) {
      WHorizontalList l = theme.horizontalList();
      WButton btn = (WButton)l.add(theme.button("Random generate password")).widget();
      btn.action = () -> {
         String password = RejectsUtils.getRandomPassword(16);
         MutableComponent text = Component.literal(ChatFormatting.BOLD + "Click here to register securely.");
         text.setStyle(text.getStyle().withClickEvent(new RunCommand(String.format("/register %s %s", password, password))));
         this.info(text);
      };
      return l;
   }

   @EventHandler
   private void onGameJoined(GameJoinedEvent event) {
      if (this.isActive()) {
         final String command = (String)((Map)this.commands.get()).getOrDefault("*", (String)((Map)this.commands.get()).get(Utils.getWorldName()));
         if (command != null) {
            this.timer.schedule(new TimerTask() {
               {
                  Objects.requireNonNull(AutoLogin.this);
               }

               @Override
               public void run() {
                  if (AutoLogin.this.mc.player != null) {
                     ChatUtils.sendPlayerMsg(command);
                  }
               }
            }, ((Integer)this.delay.get()).intValue());
         }
      }
   }

   @EventHandler
   private void onPacketSent(Send event) {
      if ((Boolean)this.smart.get()) {
         if (event.packet instanceof ServerboundChatCommandPacket packet) {
            String command = packet.command();
            List<String> hint = Arrays.asList("reg", "register", "l", "login", "log");
            String[] cmds = command.split(" ");
            if (cmds.length >= 2 && hint.contains(cmds[0])) {
               ((Map)this.commands.get()).put(Utils.getWorldName(), "/login " + cmds[1]);
            }
         }
      }
   }
}
