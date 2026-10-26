package anticope.rejects.gui.servers;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixin.MultiplayerScreenAccessor;
import anticope.rejects.mixin.ServerListAccessor;
import anticope.rejects.utils.server.IPAddress;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.network.chat.Component;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class ServerManagerScreen extends WindowScreen {
   private static final PointerBuffer saveFileFilters = BufferUtils.createPointerBuffer(1);
   private final JoinMultiplayerScreen multiplayerScreen;

   public ServerManagerScreen(GuiTheme theme, JoinMultiplayerScreen multiplayerScreen) {
      super(theme, "Manage Servers");
      this.parent = multiplayerScreen;
      this.multiplayerScreen = multiplayerScreen;
   }

   public static Runnable tryHandle(ServerManagerScreen.ThrowingRunnable<?> tr, Consumer<Throwable> handler) {
      return Objects.requireNonNull(tr).addHandler(handler);
   }

   public void initWidgets() {
      WHorizontalList l = (WHorizontalList)this.add(this.theme.horizontalList()).expandX().widget();
      this.addButton(l, "Find Servers (new)", () -> new ServerFinderScreen(this.theme, this.multiplayerScreen, this));
      this.addButton(l, "Find Servers (legacy)", () -> new LegacyServerFinderScreen(this.theme, this.multiplayerScreen, this));
      this.addButton(l, "Clean Up", () -> new CleanUpScreen(this.theme, this.multiplayerScreen, this));
      l = (WHorizontalList)this.add(this.theme.horizontalList()).expandX().widget();
      ((WButton)l.add(this.theme.button("Save IPs")).expandX().widget()).action = tryHandle(() -> {
         String targetPath = TinyFileDialogs.tinyfd_saveFileDialog("Save IPs", null, saveFileFilters, null);
         if (targetPath != null) {
            if (!targetPath.endsWith(".txt")) {
               targetPath = targetPath + ".txt";
            }

            Path filePath = Path.of(targetPath);
            int newIPs = 0;
            Set<IPAddress> hashedIPs = new HashSet<>();
            if (Files.exists(filePath)) {
               try {
                  for (String ip : Files.readAllLines(filePath)) {
                     IPAddress parsedIP = IPAddress.fromText(ip);
                     if (parsedIP != null) {
                        hashedIPs.add(parsedIP);
                     }
                  }
               } catch (IOException var11) {
                  var11.printStackTrace();
               }
            }

            ServerList servers = this.multiplayerScreen.getServers();

            for (int i = 0; i < servers.size(); i++) {
               ServerData info = servers.get(i);
               IPAddress addr = IPAddress.fromText(info.ip);
               if (addr != null && hashedIPs.add(addr)) {
                  newIPs++;
               }
            }

            StringBuilder fileOutput = new StringBuilder();

            for (IPAddress ipx : hashedIPs) {
               String stringIP = ipx.toString();
               if (stringIP != null) {
                  fileOutput.append(stringIP).append("\n");
               }
            }

            try {
               Files.writeString(filePath, fileOutput.toString());
            } catch (IOException var10) {
               var10.printStackTrace();
            }

            this.toast("Success!", newIPs == 1 ? "Saved %s new IP" : "Saved %s new IPs", newIPs);
         }
      }, e -> {
         MeteorRejectsAddon.LOG.error("Could not save IPs");
         this.toast("Something went wrong", "The IPs could not be saved, look at the log for details");
      });
      ((WButton)l.add(this.theme.button("Load IPs")).expandX().widget()).action = tryHandle(() -> {
         String targetPath = TinyFileDialogs.tinyfd_openFileDialog("Load IPs", null, saveFileFilters, "", false);
         if (targetPath != null) {
            Path filePath = Path.of(targetPath);
            if (Files.exists(filePath)) {
               List<ServerData> servers = ((ServerListAccessor)this.multiplayerScreen.getServers()).getServerList();
               Set<String> presentAddresses = new HashSet<>();
               int newIPs = 0;

               for (ServerData server : servers) {
                  presentAddresses.add(server.ip);
               }

               for (String addr : Minecraft.getInstance().keyboardHandler.getClipboard().split("[\r\n]+")) {
                  if (presentAddresses.add(addr = addr.split(" ")[0])) {
                     servers.add(new ServerData("Server discovery #" + presentAddresses.size(), addr, Type.OTHER));
                     newIPs++;
                  }
               }

               this.multiplayerScreen.getServers().save();
               ((MultiplayerScreenAccessor)this.multiplayerScreen).getServerListWidget().setSelected(null);
               ((MultiplayerScreenAccessor)this.multiplayerScreen).getServerListWidget().updateOnlineServers(this.multiplayerScreen.getServers());
               this.toast("Success!", newIPs == 1 ? "Loaded %s new IP" : "Loaded %s new IPs", newIPs);
            }
         }
      }, e -> {
         MeteorRejectsAddon.LOG.error("Could not load IPs");
         this.toast("Something went wrong", "The IPs could not be loaded, look at the log for details");
      });
   }

   private void toast(String titleKey, String descriptionKey, Object... params) {
      SystemToast.add(this.minecraft.getToastManager(), SystemToastId.WORLD_BACKUP, Component.literal(titleKey), Component.translatable(descriptionKey, params));
   }

   private void addButton(WContainer c, String text, IGetter<Screen> action) {
      WButton button = (WButton)c.add(this.theme.button(text)).expandX().widget();
      button.action = () -> this.minecraft.setScreen((Screen)action.get());
   }

   static {
      saveFileFilters.put(MemoryUtil.memASCII("*.txt"));
      saveFileFilters.rewind();
   }

   public interface ThrowingRunnable<TEx extends Throwable> {
      void run() throws TEx;

      default Runnable addHandler(Consumer<Throwable> handler) {
         Objects.requireNonNull(handler);
         return () -> {
            try {
               this.run();
            } catch (Throwable var3) {
               handler.accept(var3);
            }
         };
      }
   }
}
