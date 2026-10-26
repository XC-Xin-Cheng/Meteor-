package anticope.rejects.utils.accounts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.authlib.Environment;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ServicesKeyInfo;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomYggdrasilLogin {
   public static Environment localYggdrasilApi = new Environment("/authserver", "/sessionserver", "/minecraftservices", "Custom-Yggdrasil");

   public static User login(String name, String password, String server) throws AuthenticationException {
      try {
         String url = server + "/authserver/authenticate";
         JsonObject agent = new JsonObject();
         agent.addProperty("name", "Minecraft");
         agent.addProperty("version", 1);
         JsonObject root = new JsonObject();
         root.add("agent", agent);
         root.addProperty("username", name);
         root.addProperty("password", password);
         String data = Http.post(url).bodyJson(root).sendString();
         JsonObject json = JsonParser.parseString(data).getAsJsonObject();
         if (json.has("error")) {
            throw new AuthenticationException(json.get("errorMessage").getAsString());
         } else {
            String token = json.get("accessToken").getAsString();
            UUID uuid = UUID.fromString(json.get("selectedProfile").getAsJsonObject().get("id").getAsString());
            String username = json.get("selectedProfile").getAsJsonObject().get("name").getAsString();
            return new User(username, uuid, token, Optional.empty(), Optional.empty());
         }
      } catch (Exception var11) {
         throw new AuthenticationException(var11);
      }
   }

   public static class LocalYggdrasilAuthenticationService extends YggdrasilAuthenticationService {
      public final String server;

      public LocalYggdrasilAuthenticationService(Proxy proxy, String server) {
         super(proxy, CustomYggdrasilLogin.localYggdrasilApi);
         this.server = server;
      }
   }

   public static class LocalYggdrasilMinecraftSessionService extends YggdrasilMinecraftSessionService {
      private static final Logger LOGGER = LogManager.getLogger();
      private final ServicesKeyInfo publicKey;
      private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

      public LocalYggdrasilMinecraftSessionService(YggdrasilAuthenticationService service, String serverUrl) {
         super(service.getServicesKeySet(), MeteorClient.mc.getProxy(), CustomYggdrasilLogin.localYggdrasilApi);
         String data = Http.get(serverUrl).sendString();
         JsonObject json = JsonParser.parseString(data).getAsJsonObject();
         this.publicKey = getPublicKey(json.get("signaturePublickey").getAsString());
      }

      private static ServicesKeyInfo getPublicKey(String key) {
         key = key.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");

         try {
            byte[] byteKey = Base64.getDecoder().decode(key.replace("\n", ""));
            return YggdrasilServicesKeyInfo.parse(byteKey);
         } catch (IllegalArgumentException var2) {
            var2.printStackTrace();
            return null;
         }
      }

      private SignatureState getPropertySignatureState(Property property) {
         if (!property.hasSignature()) {
            return SignatureState.UNSIGNED;
         } else {
            return !this.publicKey.validateProperty(property) ? SignatureState.INVALID : SignatureState.SIGNED;
         }
      }

      public MinecraftProfileTextures unpackTextures(Property packedTextures) {
         String value = packedTextures.value();
         SignatureState signatureState = this.getPropertySignatureState(packedTextures);

         MinecraftTexturesPayload result;
         try {
            String json = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
            result = (MinecraftTexturesPayload)this.gson.fromJson(json, MinecraftTexturesPayload.class);
         } catch (IllegalArgumentException | JsonParseException var6) {
            LOGGER.error("Could not decode textures payload", var6);
            return MinecraftProfileTextures.EMPTY;
         }

         if (result != null && result.textures() != null && !result.textures().isEmpty()) {
            Map<Type, MinecraftProfileTexture> textures = result.textures();
            return new MinecraftProfileTextures(textures.get(Type.SKIN), textures.get(Type.CAPE), textures.get(Type.ELYTRA), signatureState);
         } else {
            return MinecraftProfileTextures.EMPTY;
         }
      }
   }
}
