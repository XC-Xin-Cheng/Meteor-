package zgoly.meteorist.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import org.apache.commons.lang3.RandomStringUtils;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.utils.misc.DebugLogger;

public class DmSpam extends Module {
   private final SettingGroup sgCommand = this.settings.createGroup("Command");
   private final SettingGroup sgSelection = this.settings.createGroup("Selection");
   private final SettingGroup sgDelay = this.settings.createGroup("Delay");
   private final SettingGroup sgDisableSettings = this.settings.createGroup("Disable Settings");
   private final SettingGroup sgSpecialCases = this.settings.createGroup("Special Cases");
   private final Setting<String> messageCommand = this.sgCommand
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("message-command")).description("Specified command to direct message a player."))
               .defaultValue("/msg {player} {message}"))
            .build()
      );
   private final Setting<List<String>> spamMessages = this.sgSelection
      .add(
         ((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                     .name("spam-messages"))
                  .description("List of messages that can be sent to the players."))
               .defaultValue(List.of("Meteorist :handshake: Meteor")))
            .build()
      );
   private final Setting<DmSpam.Mode> messageMode = this.sgSelection
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("message-mode"))
                  .description("'Sequential' - send messages in the order they appear in the list; 'Random' - pick a random message from the list."))
               .defaultValue(DmSpam.Mode.Sequential))
            .build()
      );
   private final Setting<DmSpam.Mode> playerMode = this.sgSelection
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("player-mode"))
                  .description("'Sequential' - select players in the order they appear; 'Random' - pick a random player."))
               .defaultValue(DmSpam.Mode.Sequential))
            .build()
      );
   private final Setting<Integer> delayBetweenMessages = this.sgDelay
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay-between-messages"))
                  .description("Time delay in ticks between the sending of individual messages."))
               .defaultValue(20))
            .min(1)
            .sliderMax(1200)
            .build()
      );
   private final Setting<Integer> delayBetweenPlayers = this.sgDelay
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay-between-players"))
                  .description("Time delay in ticks after all messages have been sent to all players."))
               .defaultValue(100))
            .min(1)
            .sliderMax(1200)
            .build()
      );
   private final Setting<DmSpam.DisableTrigger> disableTrigger = this.sgDisableSettings
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("disable-trigger"))
                  .description("'None' - never; 'NoMoreMessages' - no more messages to send; 'NoMorePlayers' - no more players to send messages to."))
               .defaultValue(DmSpam.DisableTrigger.None))
            .build()
      );
   private final Setting<Boolean> disableOnExit = this.sgDisableSettings
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("disable-on-exit"))
                  .description("Stops the spam message flow when you leave a server."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> disableOnDisconnect = this.sgDisableSettings
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("disable-on-disconnect"))
                  .description("Stops the spam message flow if you are disconnected from the server (eg. kicked)."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> excludeSelf = this.sgSpecialCases
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("exclude-self"))
                  .description("If set to 'true', the system will not send messages to yourself."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> antiSpamBypass = this.sgSpecialCases
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("anti-spam-bypass"))
                  .description("Adds random text at the end of each message to avoid spam detection."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Integer> bypassTextLength = this.sgSpecialCases
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("bypass-text-length"))
                     .description("Defines the number of characters used to bypass spam detection."))
                  .visible(this.antiSpamBypass::get))
               .defaultValue(16))
            .sliderRange(1, 256)
            .build()
      );
   private final Setting<DmSpam.PlayerMode> playerModeSetting = this.sgSpecialCases
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("player-mode"))
                  .description("Choose between Blacklist (exclude players) or Whitelist (only include players)."))
               .defaultValue(DmSpam.PlayerMode.Blacklist))
            .build()
      );
   private final Setting<List<String>> playerWhitelist = this.sgSpecialCases
      .add(
         ((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                  .name("player-whitelist"))
               .description("List of player names to include when in Whitelist mode."))
            .build()
      );
   private final Setting<List<String>> playerBlacklist = this.sgSpecialCases
      .add(
         ((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                  .name("player-blacklist"))
               .description("List of player names to exclude from receiving messages when in Blacklist mode."))
            .build()
      );
   private final List<UUID> usedPlayerUUIDs = new ArrayList<>();
   private final List<Integer> usedMessageIds = new ArrayList<>();
   private long currentTick;
   private final DebugLogger debugLogger = new DebugLogger(this, this.settings);

   public DmSpam() {
      super(Meteorist.CATEGORY, "dm-spam", "Spams messages in players direct messages.");
   }

   @EventHandler
   private void onScreenOpen(OpenScreenEvent event) {
      if ((Boolean)this.disableOnDisconnect.get() && event.screen instanceof DisconnectedScreen) {
         this.toggle();
      }
   }

   public void onActivate() {
      this.currentTick = this.mc.level.getGameTime();
   }

   @EventHandler
   public void onDeactivate() {
      this.usedPlayerUUIDs.clear();
      this.usedMessageIds.clear();
   }

   @EventHandler
   private void onGameLeft(GameLeftEvent event) {
      if ((Boolean)this.disableOnExit.get()) {
         this.toggle();
      }
   }

   @EventHandler
   private void onTick(Post event) {
      List<UUID> allPlayerUUIDs = new ArrayList<>(this.mc.getConnection().getOnlinePlayerIds());
      if (!allPlayerUUIDs.isEmpty() && !((String)this.messageCommand.get()).isEmpty()) {
         List<UUID> unusedPlayerUUIDs = new ArrayList<>(allPlayerUUIDs);
         unusedPlayerUUIDs.removeAll(this.usedPlayerUUIDs);
         if ((Boolean)this.excludeSelf.get()) {
            unusedPlayerUUIDs.remove(this.mc.player.getUUID());
         }

         long currentWorldTime = this.mc.level.getGameTime();
         if (!unusedPlayerUUIDs.isEmpty() && this.currentTick <= currentWorldTime) {
            UUID selectedPlayerUUID = this.playerMode.get() == DmSpam.Mode.Sequential
               ? unusedPlayerUUIDs.getFirst()
               : unusedPlayerUUIDs.get(new Random().nextInt(unusedPlayerUUIDs.size()));
            String playerName = this.mc
               .getConnection()
               .getOnlinePlayers()
               .stream()
               .filter(player -> player.getProfile().id().equals(selectedPlayerUUID))
               .map(player -> player.getProfile().name())
               .findFirst()
               .orElse("");
            if (this.playerModeSetting.get() == DmSpam.PlayerMode.Blacklist) {
               if (((List)this.playerBlacklist.get()).contains(playerName)) {
                  this.debugLogger.info("Skipping blacklisted player: " + playerName);
                  this.usedPlayerUUIDs.add(selectedPlayerUUID);
                  return;
               }
            } else if (this.playerModeSetting.get() == DmSpam.PlayerMode.Whitelist && !((List)this.playerWhitelist.get()).contains(playerName)) {
               this.debugLogger.info("Skipping non-whitelisted player: " + playerName);
               this.usedPlayerUUIDs.add(selectedPlayerUUID);
               return;
            }

            List<Integer> allMessageIds = IntStream.rangeClosed(0, ((List)this.spamMessages.get()).size() - 1)
               .boxed()
               .collect(Collectors.toCollection(LinkedList::new));
            allMessageIds.removeAll(this.usedMessageIds);
            if (allMessageIds.isEmpty()) {
               if (this.disableTrigger.get() == DmSpam.DisableTrigger.NoMoreMessages) {
                  this.toggle();
                  return;
               }

               this.usedMessageIds.clear();
               allMessageIds = IntStream.rangeClosed(0, ((List)this.spamMessages.get()).size() - 1).boxed().collect(Collectors.toCollection(LinkedList::new));
            }

            int selectedMessageId = this.messageMode.get() == DmSpam.Mode.Sequential
               ? allMessageIds.getFirst()
               : allMessageIds.get(new Random().nextInt(allMessageIds.size()));
            String selectedMessage = (String)((List)this.spamMessages.get()).get(selectedMessageId);
            if ((Boolean)this.antiSpamBypass.get()) {
               selectedMessage = selectedMessage + " " + RandomStringUtils.insecure().nextAlphabetic((Integer)this.bypassTextLength.get()).toLowerCase();
            }

            ChatUtils.sendPlayerMsg(((String)this.messageCommand.get()).replace("{player}", playerName).replace("{message}", selectedMessage));
            this.debugLogger.info("Sent '" + selectedMessage + "' to '" + playerName + "'. Handling a delay of " + this.delayBetweenMessages.get() + " ticks.");
            this.usedMessageIds.add(selectedMessageId);
            this.usedPlayerUUIDs.add(selectedPlayerUUID);
            this.currentTick = currentWorldTime + ((Integer)this.delayBetweenMessages.get()).intValue();
         } else if (unusedPlayerUUIDs.isEmpty() && this.disableTrigger.get() == DmSpam.DisableTrigger.NoMorePlayers) {
            this.toggle();
         } else if (this.currentTick <= currentWorldTime) {
            this.currentTick = currentWorldTime + ((Integer)this.delayBetweenPlayers.get()).intValue();
            this.debugLogger.info("The players ended, handling a delay of " + this.delayBetweenPlayers.get() + " ticks.");
            this.usedPlayerUUIDs.clear();
         }
      }
   }

   public static enum DisableTrigger {
      None,
      NoMoreMessages,
      NoMorePlayers;
   }

   public static enum Mode {
      Sequential,
      Random;
   }

   public static enum PlayerMode {
      Blacklist,
      Whitelist;
   }
}
