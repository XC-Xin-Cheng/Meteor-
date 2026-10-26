package fr.crazycat256.satellite.modules.seedmap;

import com.google.gson.JsonObject;
import fr.crazycat256.satellite.Addon;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;

public class SeedMap extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> serverPort = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("port"))
                  .description("The port used for connections, your minecraft instance must be able to accept incoming connections on this port."))
               .defaultValue(8080))
            .range(1, 65535)
            .noSlider()
            .build()
      );
   private final Setting<Boolean> onlyLocal = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("only-local"))
                     .description("Only allow local connections (keep enabled unless you know what you're doing)."))
                  .defaultValue(true))
               .onChanged(b -> {
                  if (this.webSocket != null) {
                     this.webSocket.setOnlyLocal(b);
                  }
               }))
            .build()
      );
   private final Setting<Boolean> displayTooltip = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("display-tooltip"))
                  .description("Display the marker tooltip on the seed map."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> debugMessages = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("debug-messages"))
                     .description("Show debug messages in chat."))
                  .defaultValue(false))
               .onChanged(b -> {
                  if (this.webSocket != null) {
                     this.webSocket.setDebugMessages(b);
                  }
               }))
            .build()
      );
   private final String script;
   private SeedMapWebSocket webSocket;
   private int lastX = 0;
   private int lastZ = 0;
   private String lastDimension = null;

   public SeedMap() {
      super(Addon.CATEGORY, "seed-map", "Show dynamicly your position on Chunkbase's Seed Map");

      try {
         try (
            InputStream inputStream = SeedMap.class.getResourceAsStream("/assets/satellite/seedmap-script.js");
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(streamReader);
         ) {
            StringBuilder content = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
               content.append(line).append("\n");
            }

            this.script = content.toString();
         }
      } catch (IOException var12) {
         throw new RuntimeException(var12);
      }
   }

   public void onActivate() {
      this.close();
      this.webSocket = new SeedMapWebSocket((Integer)this.serverPort.get(), (Boolean)this.onlyLocal.get(), (Boolean)this.debugMessages.get());
      Thread thread = new Thread(this.webSocket);
      thread.start();
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      WHorizontalList b = (WHorizontalList)list.add(theme.horizontalList()).expandX().widget();
      WButton copyScript = (WButton)b.add(theme.button("Copy script")).expandX().widget();
      copyScript.action = () -> this.mc.keyboardHandler.setClipboard(String.format(this.script, this.serverPort.get()));
      WButton site = (WButton)list.add(theme.button("Open Seed Map")).expandX().widget();
      site.action = () -> Util.getPlatform().openUri("https://www.chunkbase.com/apps/seed-map");
      return list;
   }

   public void onDeactivate() {
      this.close();
   }

   public void close() {
      if (this.webSocket != null) {
         this.webSocket.closeAll();

         try {
            this.webSocket.stop();
         } catch (InterruptedException var2) {
         }

         this.webSocket = null;
      }
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.webSocket != null && this.mc.player != null && this.mc.level != null) {
         Vec3 pos = this.mc.player.position();
         int x = (int)pos.x;
         int z = (int)pos.z;
         String dimension = this.mc.level.dimension().identifier().toString();

         for (String dim : new String[]{"overworld", "nether", "end"}) {
            if (dimension.contains(dim)) {
               dimension = dim;
               break;
            }
         }

         JsonObject packet = new JsonObject();
         if (x != this.lastX) {
            packet.addProperty("x", x);
         }

         if (z != this.lastZ) {
            packet.addProperty("z", z);
         }

         if (!dimension.equals(this.lastDimension)) {
            packet.addProperty("dimension", dimension);
         }

         if (!packet.isEmpty()) {
            packet.addProperty("tooltip", (Boolean)this.displayTooltip.get());
            this.webSocket.broadcast(packet.toString());
         }

         this.lastX = x;
         this.lastZ = z;
         this.lastDimension = dimension;
      }
   }
}
