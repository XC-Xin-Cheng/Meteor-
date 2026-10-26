package anticope.rejects.gui.screens;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.AABB;

public class StatsScreen extends WindowScreen {
   public final Entity entity;
   private boolean effectListExpanded = true;
   private boolean attribListExpanded = true;
   private boolean dimensionExpanded = true;

   public StatsScreen(Entity e) {
      super(GuiThemes.get(), e.getName().getString());
      this.entity = e;
      this.updateData();
      MeteorClient.EVENT_BUS.subscribe(this);
   }

   protected void onClosed() {
      MeteorClient.EVENT_BUS.unsubscribe(this);
      super.onClosed();
   }

   private void updateData() {
      this.clear();
      GuiTheme theme = GuiThemes.get();
      Language lang = ClientLanguage.getInstance();
      this.add(theme.label(String.format("Type: %s", lang.getOrDefault(this.entity.getType().getDescriptionId()))));
      this.add(theme.label(String.format("Age: %d", this.entity.tickCount)));
      this.add(theme.label(String.format("UUID: %s", this.entity.getStringUUID())));
      if (this.entity instanceof LivingEntity liv) {
         this.add(theme.label(String.format("Health: %.2f/%.2f", liv.getHealth(), liv.getMaxHealth())));
         this.add(theme.label(String.format("Armor: %d/20", liv.getArmorValue())));
         WSection effectList = (WSection)this.add(theme.section("Status Effects", this.effectListExpanded)).expandX().widget();
         effectList.action = () -> this.effectListExpanded = effectList.isExpanded();
         liv.getActiveEffectsMap().forEach((effect, instance) -> {
            String status = lang.getOrDefault(((MobEffect)effect.value()).getDescriptionId());
            float tps = TickRate.INSTANCE.getTickRate();
            if (instance.getAmplifier() != 0) {
               status = status + String.format(" %d (%s)", instance.getAmplifier() + 1, MobEffectUtil.formatDuration(instance, 1.0F, tps));
            } else {
               status = status + String.format(" (%s)", MobEffectUtil.formatDuration(instance, 1.0F, tps));
            }

            effectList.add(theme.label(status)).expandX();
         });
         if (liv.getActiveEffectsMap().isEmpty()) {
            effectList.add(theme.label("No effects")).expandX();
         }

         WSection attribList = (WSection)this.add(theme.section("Attributes", this.attribListExpanded)).expandX().widget();
         attribList.action = () -> this.attribListExpanded = attribList.isExpanded();
         liv.getAttributes()
            .getAttributesToSync()
            .forEach(
               attrib -> attribList.add(
                     theme.label(String.format("%s: %.2f", lang.getOrDefault(((Attribute)attrib.getAttribute().value()).getDescriptionId()), attrib.getValue()))
                  )
                  .expandX()
            );
      }

      WSection dimension = (WSection)this.add(theme.section("Dimensions", this.dimensionExpanded)).expandX().widget();
      dimension.action = () -> this.dimensionExpanded = dimension.isExpanded();
      dimension.add(theme.label(String.format("Position: %.2f, %.2f, %.2f", this.entity.getX(), this.entity.getY(), this.entity.getZ()))).expandX();
      dimension.add(theme.label(String.format("Yaw: %.2f, Pitch: %.2f", this.entity.getYRot(), this.entity.getXRot()))).expandX();
      AABB box = this.entity.getBoundingBox();
      dimension.add(theme.label(String.format("Bounding Box: %.2f, %.2f, %.2f", box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ))).expandX();
   }

   @EventHandler
   private void onTick(Post event) {
      this.updateData();
   }

   public void initWidgets() {
   }
}
