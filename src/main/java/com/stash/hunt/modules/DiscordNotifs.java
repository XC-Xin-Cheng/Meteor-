package com.stash.hunt.modules;

import com.mojang.authlib.GameProfile;
import com.stash.hunt.Addon;
import com.stash.hunt.Utils;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class DiscordNotifs extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<String> webhookURL = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("webhook-link"))
                  .description("The discord webhook to use, looks like this: https://discord.com/api/webhooks/webhookUserId/webHookTokenOrSomething"))
               .defaultValue(""))
            .build()
      );
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("message-delay"))
                  .description("The delay between messages in milliseconds."))
               .defaultValue(2000))
            .build()
      );
   private final Setting<Boolean> queueMessages = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("queue-messages"))
                  .description(
                     "Will queue messages if they are sent too quickly. This could result in a long delay between messages being logged if the queue gets too big."
                  ))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> timestamp = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("timestamp"))
                  .description("If the message should have a timestamp."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> logAll = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("all-messages"))
                  .description("Logs all messages."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> connections = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("disconnect"))
                     .description("If a message should be logged when leaving."))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.logAll.get()))
            .build()
      );
   private final Setting<Boolean> playerRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("player-range"))
                     .description("If a message should be logged when players enter/exit your render distance."))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.logAll.get()))
            .build()
      );
   private final Setting<Boolean> queue = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("2b2t-queue"))
                     .description("If your position in queue should be logged."))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.logAll.get()))
            .build()
      );
   private final Setting<Boolean> whisper = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("whisper"))
                     .description("If whispers should be logged."))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.logAll.get()))
            .build()
      );
   private final Setting<Boolean> chat = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("chat-messages"))
                     .description("Logs chat messages"))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.logAll.get()))
            .build()
      );
   private final Setting<Boolean> commands = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("commands-client-info"))
                     .description("Logs commands and most messages from clients."))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.logAll.get()))
            .build()
      );
   private final Setting<Boolean> deathMessages = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("death-messages"))
                     .description("Logs death messages."))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.logAll.get()))
            .build()
      );
   private long delayTimer = 0L;
   private int lastQueuePos;
   private final Queue<String> messageQueue = new LinkedList<>();
   private final Set<GameProfile> playersInRange = ConcurrentHashMap.newKeySet();

   public DiscordNotifs() {
      super(Addon.CATEGORY, "discord-notifs", "Sends notifications to a discord webhook.");
   }

   public void onActivate() {
      this.messageQueue.clear();
      this.playersInRange.clear();
      this.delayTimer = 0L;
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.delayTimer > 0L) {
         this.delayTimer--;
      } else if ((Boolean)this.queueMessages.get() && !this.messageQueue.isEmpty()) {
         this.sendWebhookMessage(this.messageQueue.poll());
      }

      Set<UUID> uuidsCurrentlyInRange = new HashSet<>();
      if ((Boolean)this.playerRange.get() && this.mc.level != null) {
         for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (!entity.getUUID().equals(this.mc.player.getUUID()) && entity instanceof Player playerEntity) {
               uuidsCurrentlyInRange.add(playerEntity.getUUID());
               if (!this.playersInRange.contains(playerEntity.getGameProfile())) {
                  this.playersInRange.add(playerEntity.getGameProfile());
                  this.handleMessage(playerEntity.getGameProfile().name() + " has entered visual range!", DiscordNotifs.MessageType.PLAYER_RANGE);
               }
            }
         }
      }

      for (GameProfile profile : this.playersInRange) {
         if (!uuidsCurrentlyInRange.contains(profile.id())) {
            this.playersInRange.remove(profile);
            this.handleMessage(profile.name() + " has left visual range!", DiscordNotifs.MessageType.PLAYER_RANGE);
         }
      }
   }

   @EventHandler(
      priority = 999
   )
   private void onReceivePacket(Receive event) {
      if (event.packet instanceof ClientboundSetSubtitleTextPacket packet) {
         String message = packet.text().getString();
         int queueIndex = message.indexOf("Position in queue: ");
         if (queueIndex != -1) {
            int queuePos = Integer.parseInt(message.substring(queueIndex + 19));
            if (queuePos != this.lastQueuePos) {
               this.handleMessage(message, DiscordNotifs.MessageType.QUEUE);
               this.lastQueuePos = queuePos;
            }
         }
      }
   }

   @EventHandler(
      priority = 999
   )
   private void onMessageReceive(ReceiveMessageEvent event) {
      Component message = event.getMessage();

      for (Component sibling : message.getSiblings()) {
         TextColor color = sibling.getStyle().getColor();
         if (color != null && color.getValue() == 43690) {
            this.handleMessage(message.getString(), DiscordNotifs.MessageType.DEATH);
            return;
         }
      }

      this.handleMessage(message.getString(), DiscordNotifs.MessageType.NORMAL);
   }

   public void handleMessage(String message, DiscordNotifs.MessageType messageType) {
      if (!((String)this.webhookURL.get()).isBlank()) {
         if ((Boolean)this.logAll.get()) {
            this.sendWebhookMessage(message);
         } else if ((Boolean)this.connections.get() && messageType.equals(DiscordNotifs.MessageType.DISCONNECT)) {
            this.sendWebhookMessage(message);
         } else if ((Boolean)this.playerRange.get() && messageType.equals(DiscordNotifs.MessageType.PLAYER_RANGE)) {
            this.sendWebhookMessage(message);
         } else if ((Boolean)this.queue.get() && messageType.equals(DiscordNotifs.MessageType.QUEUE)) {
            this.sendWebhookMessage(message);
         } else if ((!(Boolean)this.whisper.get() || message.startsWith("<") || !message.contains("whispers: ")) && !message.startsWith("to ")) {
            if ((Boolean)this.chat.get() && message.startsWith("<")) {
               this.sendWebhookMessage(message);
            } else if ((Boolean)this.deathMessages.get() && messageType.equals(DiscordNotifs.MessageType.DEATH)) {
               this.sendWebhookMessage(message);
            } else if ((Boolean)this.commands.get()
               && !message.startsWith("<")
               && !message.startsWith("to ")
               && messageType.equals(DiscordNotifs.MessageType.NORMAL)) {
               this.sendWebhookMessage(message);
            }
         } else {
            this.sendWebhookMessage(message);
         }
      }
   }

   @EventHandler
   private void onDisconnect(GameLeftEvent event) {
      this.handleMessage("Disconnected", DiscordNotifs.MessageType.DISCONNECT);
   }

   private void sendWebhookMessage(String message) {
      if (this.delayTimer > 0L) {
         if ((Boolean)this.queueMessages.get()) {
            this.messageQueue.offer(message);
         }
      } else {
         this.delayTimer = (Integer)this.delay.get() / 1000 * 20;
         if ((Boolean)this.timestamp.get()) {
            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String timestamp = now.format(formatter);
            message = "[" + timestamp + "] " + message;
         }

         String json = "{\n\"embeds\": [{\"description\": \"" + message + "\"}]}";
         new Thread(() -> Utils.sendWebhook((String)this.webhookURL.get(), json, null)).start();
      }
   }

   public static enum MessageType {
      NORMAL,
      DEATH,
      QUEUE,
      DISCONNECT,
      PLAYER_RANGE;
   }
}
