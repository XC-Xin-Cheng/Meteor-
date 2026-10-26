package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;

public class CustomPackets extends Module {
   private static final Gson GSON_NON_PRETTY = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
   private static final Type BADLION_MODS_TYPE = (new TypeToken<Map<String, CustomPackets.BadlionMod>>() {}).getType();
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgBadlion = this.settings.createGroup("Bad Lion");
   private final Setting<Boolean> unknownPackets = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("unknown-packets")).description("Whether to print unknown packets.")).defaultValue(false)).build());
   private final Setting<Boolean> mods = this.sgBadlion
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("disallowed-mods")).description("Whether to print what badlion mods are disallowed."))
               .defaultValue(true))
            .build()
      );
   FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

   public CustomPackets() {
      super(MeteorRejectsAddon.CATEGORY, "custom-packets", "Handles different non-vanilla protocols.");
   }

   @EventHandler
   private void onCustomPayloadPacket(Receive event) {
      if (event.packet instanceof ClientboundCustomPayloadPacket packet) {
         if (packet.payload().type().toString().equals("badlion:mods")) {
            event.setCancelled(this.onBadlionModsPacket(packet));
         } else {
            this.onUnknownPacket(packet);
         }
      }
   }

   private void onUnknownPacket(ClientboundCustomPayloadPacket packet) {
      if ((Boolean)this.unknownPackets.get()) {
         MutableComponent text = Component.literal(packet.payload().type().toString());
         this.buffer.clear();
         text.setStyle(text.getStyle().withHoverEvent(new ShowText(Component.literal(this.readString(this.buffer)))));
         this.info(text);
      }
   }

   private boolean onBadlionModsPacket(ClientboundCustomPayloadPacket packet) {
      if (!(Boolean)this.mods.get()) {
         return false;
      } else {
         this.buffer.clear();
         String json = this.readString(this.buffer);
         Map<String, CustomPackets.BadlionMod> mods = (Map<String, CustomPackets.BadlionMod>)GSON_NON_PRETTY.fromJson(json, BADLION_MODS_TYPE);
         ChatUtils.sendMsg("Badlion", this.format("Mods", this.formatMods(mods)));
         return true;
      }
   }

   private MutableComponent format(String type, MutableComponent message) {
      MutableComponent text = Component.literal(String.format("[%s%s%s]", ChatFormatting.AQUA, type, ChatFormatting.GRAY));
      text.append(" ");
      text.append(message);
      return text;
   }

   private String readString(FriendlyByteBuf data) {
      return data.readCharSequence(data.readableBytes(), StandardCharsets.UTF_8).toString();
   }

   private MutableComponent formatMods(Map<String, CustomPackets.BadlionMod> mods) {
      MutableComponent text = Component.literal("Disallowed mods: \n");
      mods.forEach((name, data) -> {
         MutableComponent modLine = Component.literal(String.format("- %s%s%s ", ChatFormatting.YELLOW, name, ChatFormatting.GRAY));
         modLine.append(data.disabled ? "disabled" : "enabled");
         modLine.append("\n");
         if (data.extra_data != null) {
            modLine.setStyle(modLine.getStyle().withHoverEvent(new ShowText(Component.literal(data.extra_data.toString()))));
         }

         text.append(modLine);
      });
      return text;
   }

   private static class BadlionMod {
      private boolean disabled;
      private JsonObject extra_data;
      private JsonObject settings;
   }
}
