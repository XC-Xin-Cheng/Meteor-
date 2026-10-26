package zgoly.meteorist.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset.DownloadedTexture;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import zgoly.meteorist.commands.arguments.PlayerPropertiesArgumentType;

public class PlayersInfoCommand extends Command {
   public PlayersInfoCommand() {
      super("playersinfo", "Saves in file / copies to clipboard info about players on current server.", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(
         ((LiteralArgumentBuilder)literal("copy").executes(this::copyPlayersInfo))
            .then(argument("properties", PlayerPropertiesArgumentType.create()).executes(this::copyPlayersInfo))
      );
      builder.then(
         ((LiteralArgumentBuilder)literal("save").executes(this::savePlayersInfo))
            .then(argument("properties", PlayerPropertiesArgumentType.create()).executes(this::savePlayersInfo))
      );
   }

   private int savePlayersInfo(CommandContext<ClientSuggestionProvider> context) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         PointerBuffer filterBuffer = stack.mallocPointer(1);
         filterBuffer.put(stack.UTF8("*.csv"));
         filterBuffer.flip();
         String result = TinyFileDialogs.tinyfd_saveFileDialog("Save Players Info", "players_info.csv", filterBuffer, "CSV File (*.csv)");
         if (result != null) {
            try {
               FileOutputStream outputStream = new FileOutputStream(result);
               outputStream.write(this.getPlayersInfo(context).getBytes());
               outputStream.close();
               this.info("Players info was saved to (highlight)%s(default)", new Object[]{result});
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }
      } catch (Throwable var8) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var6) {
               var8.addSuppressed(var6);
            }
         }

         throw var8;
      }

      if (stack != null) {
         stack.close();
      }

      return 1;
   }

   private int copyPlayersInfo(CommandContext<ClientSuggestionProvider> context) {
      mc.keyboardHandler.setClipboard(this.getPlayersInfo(context));
      this.info("Players info was copied to clipboard", new Object[0]);
      return 1;
   }

   private String getPlayersInfo(CommandContext<ClientSuggestionProvider> context) {
      List<String> properties;
      try {
         properties = PlayerPropertiesArgumentType.get(context);
      } catch (Exception var7) {
         properties = PlayerPropertiesArgumentType.PROPERTIES;
      }

      StringBuilder info = new StringBuilder();
      info.append(String.join(",", properties.isEmpty() ? PlayerPropertiesArgumentType.PROPERTIES : properties)).append("\n");
      if (mc.getConnection() != null) {
         for (PlayerInfo player : mc.getConnection()
            .getOnlinePlayers()
            .stream()
            .sorted((p1, p2) -> p1.getProfile().name().compareToIgnoreCase(p2.getProfile().name()))
            .toList()) {
            info.append(String.join(",", this.getProperties(player, properties))).append("\n");
         }
      }

      return info.toString();
   }

   private List<String> getProperties(PlayerInfo player, List<String> properties) {
      List<String> finalString = new ArrayList<>();

      for (String property : properties) {
         String var6 = property.toLowerCase();
         switch (var6) {
            case "player":
               finalString.add(player.getProfile().name());
               break;
            case "uuid":
               finalString.add(String.valueOf(player.getProfile().id()));
               break;
            case "gamemode":
               finalString.add(String.valueOf(player.getGameMode()));
               break;
            case "skin_url":
               String string2 = player.getSkin().body() instanceof DownloadedTexture skinAssetInfo ? skinAssetInfo.url() : null;
               finalString.add(string2);
               break;
            case "latency":
               finalString.add(String.valueOf(player.getLatency()));
         }
      }

      return finalString;
   }
}
