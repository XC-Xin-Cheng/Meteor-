package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.server.MinecraftServer;

public class ColorSigns extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> signs = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("signs")).description("Allows you to use colors in signs.")).defaultValue(true)).build());
   private final Setting<Boolean> books = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("books")).description("Allows you to use colors in books.")).defaultValue(false)).build());

   public ColorSigns() {
      super(MeteorRejectsAddon.CATEGORY, "color-signs", "Allows you to use colors on signs on NON-PAPER servers (use \"&\" for color symbols)");
   }

   @EventHandler
   private void onPacketSend(Send event) {
      if (event.packet instanceof ClientboundLoginPacket) {
         this.checkWarning();
      } else {
         if ((Boolean)this.signs.get() && event.packet instanceof ServerboundSignUpdatePacket packet) {
            for (int line = 0; line < packet.getLines().length; line++) {
               packet.getLines()[line] = packet.getLines()[line].replaceAll("(?i)(?:&|(?<!§)§)([0-9A-Z])", "§§$1$1");
            }
         }

         if ((Boolean)this.books.get() && event.packet instanceof ServerboundEditBookPacket packet) {
            List<String> newPages = packet.pages().stream().map(text -> text.replaceAll("(?i)&([0-9A-Z])", "§$1")).toList();
            if (!packet.pages().equals(newPages)) {
               assert this.mc.getConnection() != null;

               this.mc.getConnection().send(new ServerboundEditBookPacket(packet.slot(), newPages, packet.title()));
               event.cancel();
            }
         }
      }
   }

   public void onActivate() {
      super.onActivate();
      this.checkWarning();
   }

   private void checkWarning() {
      assert this.mc.player != null;

      MinecraftServer server = this.mc.player.level().getServer();
      if (server != null) {
         String brand = server.getServerModName();
         if (brand != null) {
            if (brand.contains("Paper")) {
               this.warning("You are on a paper server. Color signs won't work here", new Object[0]);
            }
         }
      }
   }
}
