package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import com.mojang.brigadier.suggestion.Suggestion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;

public class AntiVanish extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> interval = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("interval")).description("Vanish check interval.")).defaultValue(100)).min(0).sliderMax(300).build()
      );
   private final Setting<AntiVanish.Mode> mode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                  .name("mode"))
               .defaultValue(AntiVanish.Mode.LeaveMessage))
            .build()
      );
   private final Setting<String> command = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("command"))
                     .description("The completion command to detect player names."))
                  .defaultValue("minecraft:msg"))
               .visible(() -> this.mode.get() == AntiVanish.Mode.RealJoinMessage))
            .build()
      );
   private Map<UUID, String> playerCache = new HashMap<>();
   private final List<String> messageCache = new ArrayList<>();
   private final Random random = new Random();
   private final List<Integer> completionIDs = new ArrayList<>();
   private List<String> completionPlayerCache = new ArrayList<>();
   private int timer = 0;

   public AntiVanish() {
      super(MeteorRejectsAddon.CATEGORY, "anti-vanish", "Notifies user when a admin uses /vanish");
   }

   public void onActivate() {
      this.timer = 0;
      this.completionIDs.clear();
      this.messageCache.clear();
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList l = theme.verticalList();
      l.add(theme.label("LeaveMessage: If client didn't receive a quit game message (like essentials)."));
      l.add(theme.label("RealJoinMessage: Tell whether the player is really left by using player name completion."));
      return l;
   }

   @EventHandler
   private void onPacket(Receive event) {
      if (this.mode.get() == AntiVanish.Mode.RealJoinMessage
         && event.packet instanceof ClientboundCommandSuggestionsPacket packet
         && this.completionIDs.contains(packet.id())) {
         List<String> lastUsernames = this.completionPlayerCache.stream().toList();
         this.completionPlayerCache = packet.toSuggestions().getList().stream().<String>map(Suggestion::getText).toList();
         if (lastUsernames.isEmpty()) {
            return;
         }

         Predicate<String> joinedOrQuit = playerNamex -> lastUsernames.contains(playerNamex) != this.completionPlayerCache.contains(playerNamex);

         for (String playerName : this.completionPlayerCache) {
            if (!Objects.equals(playerName, this.mc.player.getName().getString())
               && !playerName.contains(" ")
               && playerName.length() >= 3
               && playerName.length() <= 16
               && joinedOrQuit.test(playerName)) {
               this.info("Player joined: " + playerName, new Object[0]);
            }
         }

         for (String playerNamex : lastUsernames) {
            if (!Objects.equals(playerNamex, this.mc.player.getName().getString())
               && !playerNamex.contains(" ")
               && playerNamex.length() >= 3
               && playerNamex.length() <= 16
               && joinedOrQuit.test(playerNamex)) {
               this.info("Player left: " + playerNamex, new Object[0]);
            }
         }

         this.completionIDs.remove(Integer.valueOf(packet.id()));
         event.cancel();
      }
   }

   @EventHandler
   private void onReceiveMessage(ReceiveMessageEvent event) {
      this.messageCache.add(event.getMessage().getString());
   }

   @EventHandler
   private void onTick(Post event) {
      this.timer++;
      if (this.timer >= (Integer)this.interval.get()) {
         switch ((AntiVanish.Mode)this.mode.get()) {
            case LeaveMessage:
               Map<UUID, String> oldPlayers = Map.copyOf(this.playerCache);
               this.playerCache = this.mc
                  .getConnection()
                  .getOnlinePlayers()
                  .stream()
                  .collect(Collectors.toMap(e -> e.getProfile().id(), e -> e.getProfile().name()));

               for (UUID uuid : oldPlayers.keySet()) {
                  if (!this.playerCache.containsKey(uuid)) {
                     String name = oldPlayers.get(uuid);
                     if (!name.contains(" ") && name.length() >= 3 && name.length() <= 16 && this.messageCache.stream().noneMatch(s -> s.contains(name))) {
                        this.warning(name + " has gone into vanish.", new Object[0]);
                     }
                  }
               }
               break;
            case RealJoinMessage:
               int id = this.random.nextInt(200);
               this.completionIDs.add(id);
               this.mc.getConnection().send(new ServerboundCommandSuggestionPacket(id, (String)this.command.get() + " "));
         }

         this.timer = 0;
         this.messageCache.clear();
      }
   }

   public static enum Mode {
      LeaveMessage,
      RealJoinMessage;
   }
}
