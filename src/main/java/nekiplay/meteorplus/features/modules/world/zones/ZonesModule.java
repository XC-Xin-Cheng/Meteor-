package nekiplay.meteorplus.features.modules.world.zones;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.MeteorPlusAddon;
import net.minecraft.world.phys.AABB;

public class ZonesModule extends Module {
   public Gson gson = new Gson();
   public List<ZoneData> allZones = new ArrayList<>();
   public List<ZoneData> inWorldZones = new ArrayList<>();

   public ZonesModule() {
      super(Categories.World, "Zones", "Create custom zones for blocking baritone settings in zone");
   }

   public void onActivate() {
      super.onActivate();
      this.load();
   }

   @EventHandler
   private void onTick(Post event) {
      this.inWorldZones.clear();
      Dimension dim = PlayerUtils.getDimension();

      for (ZoneData hologramData : this.allZones) {
         if (hologramData.world.equals(Utils.getWorldName()) && hologramData.dimension.equals(dim.name())) {
            this.inWorldZones.add(hologramData);
         }
      }
   }

   private void render(AABB box, Color sides, Color lines, ShapeMode shapeMode, Render3DEvent event) {
      event.renderer.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, sides, lines, shapeMode, 0);
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      for (ZoneData zoneData : this.inWorldZones) {
         if (zoneData.showBoundingBox) {
            this.render(
               new AABB(zoneData.x_start, zoneData.y_start, zoneData.z_start, zoneData.x_end, zoneData.y_end, zoneData.z_end),
               Color.WHITE,
               Color.WHITE,
               ShapeMode.Both,
               event
            );
         }
      }
   }

   @EventHandler
   private void onStartBlockBreaking(StartBreakingBlockEvent event) {
   }

   private void load() {
      File dir = new File(MeteorClient.FOLDER, "zones");
      if (dir.exists()) {
         String world_name = Utils.getWorldName();
         File dir2 = new File(dir, world_name);
         if (dir2.exists()) {
            this.allZones.clear();
            File[] files = dir2.listFiles();
            if (files != null) {
               for (File file : files) {
                  if (file.exists()) {
                     MeteorPlusAddon.LOG.info("[Meteor+] Loading hologram: " + file.getName());

                     try {
                        BufferedReader reader = Files.newBufferedReader(Path.of(file.toURI()), StandardCharsets.UTF_8);

                        try {
                           String json = reader.lines().collect(Collectors.joining());
                           ZoneData hologramData = (ZoneData)this.gson.fromJson(json, ZoneData.class);
                           if (hologramData != null) {
                              this.allZones.add(hologramData);
                              MeteorPlusAddon.LOG.info("[Meteor+] Success loaded zone: " + file.getName());
                           }
                        } catch (JsonSyntaxException var12) {
                           MeteorPlusAddon.LOG.error("[Meteor+] Error in zone: " + var12);
                        }
                     } catch (IOException var13) {
                        MeteorPlusAddon.LOG.error("[Meteor+] Error in zone: " + var13);
                     }
                  }
               }
            }
         }
      }
   }
}
