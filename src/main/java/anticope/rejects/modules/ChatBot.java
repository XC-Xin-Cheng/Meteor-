package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.settings.StringMapSetting;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import org.meteordev.starscript.Script;
import org.meteordev.starscript.Section;
import org.meteordev.starscript.compiler.Compiler;
import org.meteordev.starscript.compiler.Parser;
import org.meteordev.starscript.compiler.Parser.Result;
import org.meteordev.starscript.utils.Error;
import org.meteordev.starscript.utils.StarscriptError;

public class ChatBot extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<String> prefix = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("prefix")).description("Command prefix for the bot.")).defaultValue("!")).build());
   private final Setting<Boolean> help = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("help"))
                  .description("Add help command."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Map<String, String>> commands = this.sgGeneral
      .add(
         ((StringMapSetting.Builder)((StringMapSetting.Builder)new StringMapSetting.Builder().name("commands")).description("Commands."))
            .renderer(StarscriptTextBoxRenderer.class)
            .defaultValue(new LinkedHashMap<String, String>() {
               {
                  Objects.requireNonNull(ChatBot.this);
                  this.put("ping", "Pong!");
                  this.put("tps", "Current TPS: {server.tps}");
                  this.put("time", "It's currently {server.time}");
                  this.put("pos", "I am @ {player.pos}");
               }
            })
            .build()
      );

   public ChatBot() {
      super(MeteorRejectsAddon.CATEGORY, "chat-bot", "Bot which automatically responds to chat messages.");
   }

   @EventHandler
   private void onMessageReceive(ReceiveMessageEvent event) {
      String msg = event.getMessage().getString();
      if ((Boolean)this.help.get() && msg.endsWith((String)this.prefix.get() + "help")) {
         ChatUtils.sendPlayerMsg("Available commands: " + String.join(", ", ((Map)this.commands.get()).keySet()));
      } else {
         for (String cmd : ((Map)this.commands.get()).keySet()) {
            if (msg.endsWith((String)this.prefix.get() + cmd)) {
               Script script = compile((String)((Map)this.commands.get()).get(cmd));
               if (script == null) {
                  ChatUtils.sendPlayerMsg("An error occurred");
               }

               try {
                  Section section = MeteorStarscript.ss.run(script);
                  ChatUtils.sendPlayerMsg(section.text);
               } catch (StarscriptError var7) {
                  MeteorStarscript.printChatError(var7);
                  ChatUtils.sendPlayerMsg("An error occurred");
               }

               return;
            }
         }
      }
   }

   private static Script compile(String script) {
      if (script == null) {
         return null;
      } else {
         Result result = Parser.parse(script);
         if (result.hasErrors()) {
            MeteorStarscript.printChatError((Error)result.errors.get(0));
            return null;
         } else {
            return Compiler.compile(result);
         }
      }
   }
}
