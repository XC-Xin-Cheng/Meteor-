package nekiplay.meteorplus.features.modules.misc;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.MeteorPlusAddon;
import nekiplay.meteorplus.utils.ColorRemover;

public class AutoAccept extends Module {
   private final SettingGroup AASettings = this.settings.getDefaultGroup();
   private final Setting<AutoAccept.Mode> mode = this.AASettings
      .add(((Builder)((Builder)((Builder)new Builder().name("Mode")).description("Accept mode.")).defaultValue(AutoAccept.Mode.Auto)).build());
   private final Setting<String> custom_pattern = this.AASettings
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("Pattern command"))
                     .description("Custom pattern."))
                  .defaultValue(".*Игрок (.*) просит телепортироваться к вам!.*"))
               .visible(() -> this.mode.get() == AutoAccept.Mode.Custom))
            .build()
      );
   private final Setting<Integer> custom_group = this.AASettings
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("Pattern command"))
                     .description("Custom pattern."))
                  .defaultValue(1))
               .visible(() -> this.mode.get() == AutoAccept.Mode.Custom))
            .build()
      );
   private final Setting<String> accept_command = this.AASettings
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("Accept command"))
                     .description("Accept command."))
                  .defaultValue("/cmi tpaccept {username} tpa"))
               .visible(() -> this.mode.get() == AutoAccept.Mode.Custom))
            .build()
      );
   private final Setting<Boolean> FriendsOnly = this.AASettings
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("Friends only"))
                  .description("Accepts only friends requests."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Integer> Delay = this.AASettings
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                  .name("Delay"))
               .defaultValue(0))
            .build()
      );
   private final Setting<Boolean> Debug = this.AASettings
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("Debug"))
                  .description("Prints all incoming messages in console (raw format)."))
               .defaultValue(false))
            .build()
      );
   private final ArrayList<AutoAccept.TPPattern> patterns = new ArrayList<>();

   public AutoAccept() {
      super(Categories.Misc, "Auto Accept", "Automatically accepts incoming teleport requests.");
   }

   public void onActivate() {
      this.patterns.clear();
      AutoAccept.TPPattern MST_Network = new AutoAccept.TPPattern(".*Игрок (.*) просит телепортироваться к вам!.*", 1, "/cmi tpaccept {username} tpa");
      AutoAccept.TPPattern HolyWorld = new AutoAccept.TPPattern("(.*) просит телепортироваться.*", 1, "/tpaccept");
      AutoAccept.TPPattern SimpleTpa = new AutoAccept.TPPattern(".*\\[SimpleTpa\\] (.*) has sent you a teleport request!.*", 1, "/tpaccept");
      AutoAccept.TPPattern EssentialsEN = new AutoAccept.TPPattern("(.*) has requested to teleport to you\\..*", 1, "/tpaccept");
      AutoAccept.TPPattern DonutSMP = new AutoAccept.TPPattern("(.*) sent you a tpa request", 1, "/tpaccept");
      this.patterns.add(MST_Network);
      this.patterns.add(HolyWorld);
      this.patterns.add(SimpleTpa);
      this.patterns.add(EssentialsEN);
      this.patterns.add(DonutSMP);
   }

   public void onDeactivate() {
      this.patterns.clear();
   }

   private void BetterAccept(String username, AutoAccept.TPPattern pattern) {
      if (this.mc.player != null && (Boolean)this.FriendsOnly.get() && this.isFriend(username)) {
         this.info("Accepting request from §c" + username, new Object[0]);
         ChatUtils.sendPlayerMsg(pattern.command.replace("{username}", username));
      } else if (!(Boolean)this.FriendsOnly.get()) {
         this.info("Accepting request from §c" + username, new Object[0]);
         ChatUtils.sendPlayerMsg(pattern.command.replace("{username}", username));
      }
   }

   private void Accept(String username, AutoAccept.TPPattern pattern, String message) {
      if (this.mc.player != null && this.mode.get() == AutoAccept.Mode.Custom) {
         username = this.getName(pattern, message);
         if ((Boolean)this.FriendsOnly.get() && this.isFriend(username)) {
            this.info("Accepting request from §c" + username, new Object[0]);
            ChatUtils.sendPlayerMsg(((String)this.accept_command.get()).replace("{username}", username));
         } else if (!(Boolean)this.FriendsOnly.get()) {
            this.info("Accepting request from §c" + username, new Object[0]);
            ChatUtils.sendPlayerMsg(((String)this.accept_command.get()).replace("{username}", username));
         }
      } else {
         this.BetterAccept(username, pattern);
      }
   }

   @EventHandler
   public void onMessageReceive(ReceiveMessageEvent event) {
      if (event.getMessage() != null && this.mc.player != null) {
         String message = ColorRemover.GetVerbatim(event.getMessage().getString());
         if ((Boolean)this.Debug.get()) {
            MeteorPlusAddon.LOG.info(message);
         }

         Thread th = new Thread(
            () -> {
               AutoAccept.TPPattern custom = new AutoAccept.TPPattern(
                  (String)this.custom_pattern.get(), (Integer)this.custom_group.get(), (String)this.accept_command.get()
               );
               String nickname = this.getName(message);
               AutoAccept.TPPattern pattern = this.getPattern(message);
               if (pattern != null && this.mode.get() != AutoAccept.Mode.Custom) {
                  try {
                     Thread.sleep(((Integer)this.Delay.get()).intValue());
                  } catch (InterruptedException var7) {
                     var7.printStackTrace();
                  }

                  this.Accept(nickname, pattern, message);
               } else {
                  nickname = this.getName(custom, message);
                  if (!nickname.isEmpty()) {
                     try {
                        Thread.sleep(((Integer)this.Delay.get()).intValue());
                     } catch (InterruptedException var6) {
                        var6.printStackTrace();
                     }

                     this.Accept(nickname, custom, message);
                  }
               }
            }
         );
         th.start();
      }
   }

   private String getName(String message) {
      String nickname = "";

      for (AutoAccept.TPPattern tpPattern : this.patterns) {
         String nn = this.getName(tpPattern, message);
         if (!nn.isEmpty()) {
            nickname = nn;
         }
      }

      return nickname;
   }

   private String getName(AutoAccept.TPPattern tpPattern, String message) {
      String nickname = "";
      Pattern pattern = Pattern.compile(tpPattern.pattern);
      Matcher matcher = pattern.matcher(message);
      if (matcher.find()) {
         String player = matcher.group(tpPattern.group);
         if (!player.isEmpty()) {
            nickname = player;
         }
      }

      return nickname;
   }

   private AutoAccept.TPPattern getPattern(String message) {
      for (AutoAccept.TPPattern tpPattern : this.patterns) {
         Pattern pattern = Pattern.compile(tpPattern.pattern);
         Matcher matcher = pattern.matcher(message);
         if (matcher.find()) {
            String player = matcher.group(tpPattern.group);
            if (!player.isEmpty()) {
               return tpPattern;
            }
         }
      }

      return null;
   }

   private boolean isFriend(String username) {
      for (Friend f : Friends.get()) {
         if (f.name.equals(username)) {
            return true;
         }
      }

      return false;
   }

   public static enum Mode {
      Auto,
      Custom;
   }

   private static class TPPattern {
      public String pattern;
      public int group;
      public String command;

      public TPPattern(String pattern, int group, String command) {
         this.pattern = pattern;
         this.group = group;
         this.command = command;
      }
   }
}
