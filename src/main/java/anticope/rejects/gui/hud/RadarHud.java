package anticope.rejects.gui.hud;

import anticope.rejects.MeteorRejectsAddon;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.ColorSetting.Builder;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class RadarHud extends HudElement {
   public static final HudElementInfo<RadarHud> INFO = new HudElementInfo(
      MeteorRejectsAddon.HUD_GROUP, "radar", "Draws a Radar on your HUD telling you where entities are.", RadarHud::new
   );
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<SettingColor> backgroundColor = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("background-color")).description("Color of background.")).defaultValue(new SettingColor(0, 0, 0, 64)).build());
   private final Setting<Set<EntityType<?>>> entities = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder)((meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder)new meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder()
                  .name("entities"))
               .description("Select specific entities."))
            .defaultValue(new EntityType[]{EntityType.PLAYER})
            .build()
      );
   private final Setting<Boolean> letters = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("letters"))
                  .description("Use entity's type first letter."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> showWaypoints = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("waypoints"))
                  .description("Show waypoints."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Double> scale = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("scale"))
                  .description("The scale."))
               .defaultValue(1.0)
               .min(1.0)
               .sliderRange(0.01, 5.0)
               .onChanged(aDouble -> this.calculateSize()))
            .build()
      );
   private final Setting<Double> zoom = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("zoom"))
               .description("Radar zoom."))
            .defaultValue(1.0)
            .min(0.01)
            .sliderRange(0.01, 3.0)
            .build()
      );

   public RadarHud() {
      super(INFO);
      this.calculateSize();
   }

   public void calculateSize() {
      this.setSize(200.0 * (Double)this.scale.get(), 200.0 * (Double)this.scale.get());
   }

   public void render(HudRenderer renderer) {
      ESP esp = (ESP)Modules.get().get(ESP.class);
      if (esp != null) {
         renderer.post(() -> {
            if (MeteorClient.mc.player != null) {
               double width = this.getWidth();
               double height = this.getHeight();
               Renderer2D.COLOR.begin();
               Renderer2D.COLOR.quad(this.x, this.y, width, height, (Color)this.backgroundColor.get());
               Renderer2D.COLOR.render();
               if (MeteorClient.mc.level != null) {
                  for (Entity entity : MeteorClient.mc.level.entitiesForRendering()) {
                     if (!((Set)this.entities.get()).contains(entity.getType())) {
                        return;
                     }

                     double xPos = (entity.getX() - MeteorClient.mc.player.getX()) * (Double)this.scale.get() * (Double)this.zoom.get() + width / 2.0;
                     double yPos = (entity.getZ() - MeteorClient.mc.player.getZ()) * (Double)this.scale.get() * (Double)this.zoom.get() + height / 2.0;
                     if (!(xPos < 0.0) && !(yPos < 0.0) && !(xPos > width - (Double)this.scale.get()) && !(yPos > height - (Double)this.scale.get())) {
                        String icon = "*";
                        if ((Boolean)this.letters.get()) {
                           icon = entity.getType().toShortString().substring(0, 1).toUpperCase();
                        }

                        Color c = esp.getColor(entity);
                        if (c == null) {
                           c = Color.WHITE;
                        }

                        renderer.text(icon, xPos + this.x, yPos + this.y, c, false);
                     }
                  }
               }

               if ((Boolean)this.showWaypoints.get()) {
                  for (Waypoint waypoint : Waypoints.get()) {
                     BlockPos blockPos = waypoint.getPos();
                     Vec3 coords = new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
                     double xPos = (coords.x() - MeteorClient.mc.player.getX()) * (Double)this.scale.get() * (Double)this.zoom.get() + width / 2.0;
                     double yPos = (coords.z() - MeteorClient.mc.player.getZ()) * (Double)this.scale.get() * (Double)this.zoom.get() + height / 2.0;
                     if (!(xPos < 0.0) && !(yPos < 0.0) && !(xPos > width - (Double)this.scale.get()) && !(yPos > height - (Double)this.scale.get())) {
                        String iconx = "*";
                        if ((Boolean)this.letters.get() && !((String)waypoint.name.get()).isEmpty()) {
                           iconx = ((String)waypoint.name.get()).substring(0, 1);
                        }

                        renderer.text(iconx, xPos + this.x, yPos + this.y, (Color)waypoint.color.get(), false);
                     }
                  }
               }

               Renderer2D.COLOR.render();
            }
         });
      }
   }
}
