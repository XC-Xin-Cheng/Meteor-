package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixin.HandshakeC2SPacketAccessor;
import com.google.gson.Gson;
import com.mojang.authlib.properties.PropertyMap;
import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;

public class BungeeCordSpoof extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private static final Gson GSON = new Gson();
   private final Setting<Boolean> whitelist = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("whitelist")).description("Use whitelist.")).defaultValue(false)).build());
   private final Setting<List<String>> whitelistedServers = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)((meteordevelopment.meteorclient.settings.StringListSetting.Builder)new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                     .name("whitelisted-servers"))
                  .description("Will only work if you joined the servers above."))
               .visible(this.whitelist::get))
            .build()
      );
   private final Setting<Boolean> spoofProfile = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("spoof-profile")).description("Spoof account profile.")).defaultValue(false)).build());
   private final Setting<String> forwardedIP = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                     .name("forwarded-IP"))
                  .description("The forwarded IP address."))
               .defaultValue("127.0.0.1"))
            .build()
      );

   public BungeeCordSpoof() {
      super(MeteorRejectsAddon.CATEGORY, "bungeeCord-spoof", "Let you join BungeeCord servers, useful when bypassing proxies.");
      this.runInMainMenu = true;
   }

   @EventHandler
   private void onPacketSend(Send event) {
      if (event.packet instanceof ClientIntentionPacket packet && packet.intention() == ClientIntent.LOGIN) {
         if ((Boolean)this.whitelist.get() && !((List)this.whitelistedServers.get()).contains(Utils.getWorldName())) {
            return;
         }

         String address = packet.hostName()
            + "\u0000"
            + this.forwardedIP
            + "\u0000"
            + this.mc.getUser().getProfileId().toString().replace("-", "")
            + (this.spoofProfile.get() ? this.getProperty() : "");
         ((HandshakeC2SPacketAccessor)packet).setHostName(address);
      }
   }

   private String getProperty() {
      PropertyMap propertyMap = this.mc.getGameProfile().properties();
      return "\u0000" + GSON.toJson(propertyMap.values().toArray());
   }
}
