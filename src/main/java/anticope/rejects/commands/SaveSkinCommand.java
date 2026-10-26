package anticope.rejects.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class SaveSkinCommand extends Command {
   private static final SimpleCommandExceptionType IO_EXCEPTION = new SimpleCommandExceptionType(Component.literal("An exception occurred"));
   private final PointerBuffer filters;
   private final Gson GSON = new Gson();

   public SaveSkinCommand() {
      super("save-skin", "Download a player's skin by name.", new String[]{"skin", "skinsteal"});
      this.filters = BufferUtils.createPointerBuffer(1);
      ByteBuffer pngFilter = MemoryUtil.memASCII("*.png");
      this.filters.put(pngFilter);
      this.filters.rewind();
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(argument("player", PlayerListEntryArgumentType.create()).executes(ctx -> {
         UUID id = PlayerListEntryArgumentType.get(ctx).getProfile().id();
         String path = TinyFileDialogs.tinyfd_saveFileDialog("Save image", null, this.filters, null);
         if (path == null) {
            IO_EXCEPTION.create();
         }

         if (path != null) {
            if (!path.endsWith(".png")) {
               path = path + ".png";
            }

            this.saveSkin(id.toString(), path);
         }

         return 1;
      }));
   }

   private void saveSkin(String uuid, String path) throws CommandSyntaxException {
      try {
         String PROFILE_REQUEST_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s";
         JsonObject object = (JsonObject)Http.get(String.format(PROFILE_REQUEST_URL, uuid)).sendJson(JsonObject.class);
         JsonArray array = object.getAsJsonArray("properties");
         JsonObject property = array.get(0).getAsJsonObject();
         String base64String = property.get("value").getAsString();
         byte[] bs = Base64.decodeBase64(base64String);
         String secondResponse = new String(bs, StandardCharsets.UTF_8);
         JsonObject finalResponseObject = (JsonObject)this.GSON.fromJson(secondResponse, JsonObject.class);
         JsonObject texturesObject = finalResponseObject.getAsJsonObject("textures");
         JsonObject skinObj = texturesObject.getAsJsonObject("SKIN");
         String skinURL = skinObj.get("url").getAsString();
         InputStream in = new BufferedInputStream(new URL(skinURL).openStream());
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         byte[] buf = new byte[1024];
         int n = 0;

         while (-1 != (n = in.read(buf))) {
            out.write(buf, 0, n);
         }

         out.close();
         in.close();
         byte[] response = out.toByteArray();
         File file = new File(path);
         FileOutputStream fos = new FileOutputStream(file.getPath());
         fos.write(response);
         fos.close();
      } catch (NullPointerException | IOException var21) {
         throw IO_EXCEPTION.create();
      }
   }
}
