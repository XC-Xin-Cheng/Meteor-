package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.gui.screens.InteractionScreen;
import anticope.rejects.settings.StringMapSetting;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.meteordev.starscript.value.Value;
import org.meteordev.starscript.value.ValueMap;

public class InteractionMenu extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgStyle = this.settings.createGroup("Style");
   private final Setting<Set<EntityType<?>>> entities = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("entities")).description("Entities")).defaultValue(new EntityType[]{EntityType.PLAYER}).build());
   public final Setting<Keybind> keybind = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)new meteordevelopment.meteorclient.settings.KeybindSetting.Builder()
                  .name("keybind"))
               .description("The keybind to open."))
            .action(this::onKey)
            .build()
      );
   public final Setting<Boolean> useCrosshairTarget = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("use-crosshair-target"))
                  .description("Use crosshair target."))
               .defaultValue(false))
            .build()
      );
   public final Setting<SettingColor> selectedDotColor = this.sgStyle
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("selected-dot-color"))
               .description("Color of the dot when selected."))
            .defaultValue(new SettingColor(76, 255, 0))
            .build()
      );
   public final Setting<SettingColor> dotColor = this.sgStyle
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("dot-color"))
               .description("Color of the dot when."))
            .defaultValue(new SettingColor(0, 148, 255))
            .build()
      );
   public final Setting<SettingColor> backgroundColor = this.sgStyle
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("background-color"))
               .description("Color of the background."))
            .defaultValue(new SettingColor(128, 128, 128, 128))
            .build()
      );
   public final Setting<SettingColor> borderColor = this.sgStyle
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("border-color"))
               .description("Color of the border."))
            .defaultValue(new SettingColor(0, 0, 0))
            .build()
      );
   public final Setting<SettingColor> textColor = this.sgStyle
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("text-color"))
               .description("Color of the text."))
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
      );
   public final Setting<Map<String, String>> messages = this.sgGeneral
      .add(
         ((StringMapSetting.Builder)((StringMapSetting.Builder)new StringMapSetting.Builder().name("messages")).description("Messages."))
            .renderer(StarscriptTextBoxRenderer.class)
            .build()
      );

   public InteractionMenu() {
      super(MeteorRejectsAddon.CATEGORY, "interaction-menu", "An interaction screen when looking at an entity.");
      MeteorStarscript.ss.set("entity", () -> wrap(InteractionScreen.interactionMenuEntity));
   }

   public void onKey() {
      if (this.mc.player != null && this.mc.screen == null) {
         Entity e = null;
         if ((Boolean)this.useCrosshairTarget.get()) {
            e = this.mc.crosshairPickEntity;
         } else {
            Optional<Entity> lookingAt = DebugRenderer.getTargetedEntity(this.mc.player, 20);
            if (lookingAt.isPresent()) {
               e = lookingAt.get();
            }
         }

         if (e != null) {
            if (((Set)this.entities.get()).contains(e.getType())) {
               this.mc.setScreen(new InteractionScreen(e, this));
            }
         }
      }
   }

   private static Value wrap(Entity entity) {
      return entity == null
         ? Value.map(
            new ValueMap()
               .set("_toString", Value.null_())
               .set("health", Value.null_())
               .set("pos", Value.map(new ValueMap().set("_toString", Value.null_()).set("x", Value.null_()).set("y", Value.null_()).set("z", Value.null_())))
               .set("uuid", Value.null_())
         )
         : Value.map(
            new ValueMap()
               .set("_toString", Value.string(entity.getName().getString()))
               .set("health", Value.number(entity instanceof LivingEntity e ? e.getHealth() : 0.0))
               .set(
                  "pos",
                  Value.map(
                     new ValueMap()
                        .set("_toString", posString(entity.getX(), entity.getY(), entity.getZ()))
                        .set("x", Value.number(entity.getX()))
                        .set("y", Value.number(entity.getY()))
                        .set("z", Value.number(entity.getZ()))
                  )
               )
               .set("uuid", Value.string(entity.getStringUUID()))
         );
   }

   private static Value posString(double x, double y, double z) {
      return Value.string(String.format("X: %.0f Y: %.0f Z: %.0f", x, y, z));
   }
}
