package anticope.rejects.utils.seeds;

import anticope.rejects.events.SeedChangedEvent;
import com.seedfinding.mccore.version.MCVersion;
import java.util.HashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.SuggestCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;

public class Seeds extends System<Seeds> {
   private static final Seeds INSTANCE = new Seeds();
   public HashMap<String, Seed> seeds = new HashMap<>();

   public Seeds() {
      super("seeds");
      this.init();
      this.load(MeteorClient.FOLDER);
   }

   public static Seeds get() {
      return INSTANCE;
   }

   public Seed getSeed() {
      if (MeteorClient.mc.hasSingleplayerServer() && MeteorClient.mc.getSingleplayerServer() != null) {
         MCVersion version = MCVersion.fromString(MeteorClient.mc.getSingleplayerServer().getServerVersion());
         if (version == null) {
            version = MCVersion.latest();
         }

         return new Seed(MeteorClient.mc.getSingleplayerServer().overworld().getSeed(), version);
      } else {
         return this.seeds.get(Utils.getWorldName());
      }
   }

   public void setSeed(String seed, MCVersion version) {
      if (!MeteorClient.mc.hasSingleplayerServer()) {
         long numSeed = toSeed(seed);
         this.seeds.put(Utils.getWorldName(), new Seed(numSeed, version));
         MeteorClient.EVENT_BUS.post(SeedChangedEvent.get(numSeed));
      }
   }

   public void setSeed(String seed) {
      if (!MeteorClient.mc.hasSingleplayerServer()) {
         ServerData server = MeteorClient.mc.getCurrentServer();
         MCVersion ver = null;
         if (server != null) {
            ver = MCVersion.fromString(server.version.getString());
         }

         if (ver == null) {
            String targetVer = "unknown";
            if (server != null) {
               targetVer = server.version.getString();
            }

            sendInvalidVersionWarning(seed, targetVer);
            ver = MCVersion.latest();
         }

         this.setSeed(seed, ver);
      }
   }

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      this.seeds.forEach((key, seed) -> {
         if (seed != null) {
            tag.put(key, seed.toTag());
         }
      });
      return tag;
   }

   public Seeds fromTag(CompoundTag tag) {
      tag.keySet().forEach(key -> tag.getCompound(key).ifPresent(compound -> this.seeds.put(key, Seed.fromTag(compound))));
      return this;
   }

   private static long toSeed(String inSeed) {
      try {
         return Long.parseLong(inSeed);
      } catch (NumberFormatException var2) {
         return inSeed.strip().hashCode();
      }
   }

   private static void sendInvalidVersionWarning(String seed, String targetVer) {
      MutableComponent msg = Component.literal(
         String.format(
            "Couldn't resolve minecraft version \"%s\". Using %s instead. If you wish to change the version run: ", targetVer, MCVersion.latest().name
         )
      );
      String cmd = String.format("%sseed %s ", Config.get().prefix, seed);
      MutableComponent cmdText = Component.literal(cmd + "<version>");
      cmdText.setStyle(
         cmdText.getStyle().withUnderlined(true).withClickEvent(new SuggestCommand(cmd)).withHoverEvent(new ShowText(Component.literal("run command")))
      );
      msg.append(cmdText);
      msg.setStyle(msg.getStyle().withColor(ChatFormatting.YELLOW));
      ChatUtils.sendMsg("Seed", msg);
   }
}
