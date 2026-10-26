package nekiplay.meteorplus.features.modules.render;

import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.ItemListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.settings.items.ESPItemData;
import nekiplay.meteorplus.settings.items.ItemDataSetting;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public class ItemFrameEsp extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<List<Item>> whitelist = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("whitelist")).description("Which items to show esp.")).defaultValue(new Item[]{Items.ELYTRA}).build());
   private final Setting<ESPItemData> defaultBlockConfig = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.GenericSetting.Builder)((meteordevelopment.meteorclient.settings.GenericSetting.Builder)((meteordevelopment.meteorclient.settings.GenericSetting.Builder)new meteordevelopment.meteorclient.settings.GenericSetting.Builder()
                     .name("whitelist-default-config"))
                  .description("Default item config."))
               .defaultValue(
                  new ESPItemData(ShapeMode.Lines, new SettingColor(0, 255, 200), new SettingColor(0, 255, 200, 25), false, new SettingColor(0, 255, 200, 125))
               ))
            .build()
      );
   private final Setting<Map<Item, ESPItemData>> blockConfigs = this.sgGeneral
      .add(
         ((ItemDataSetting.Builder)((ItemDataSetting.Builder)new ItemDataSetting.Builder().name("whitelist-items-configs"))
               .description("Config for each item."))
            .defaultData(this.defaultBlockConfig)
            .build()
      );

   public ItemFrameEsp() {
      super(Categories.Render, "ItemFrame-esp", "back lightning of the frames in which the selected items");
   }

   @EventHandler
   private void onRender2D(Render3DEvent event) {
      if (this.mc.level != null) {
         for (Entity entity : this.mc.level.entitiesForRendering()) {
            double xl = entity.getX();
            double yl = entity.getY();
            double zl = entity.getZ();
            if (entity instanceof ItemFrame itemFrame) {
               ItemStack held = itemFrame.getItem();
               if (((List)this.whitelist.get()).contains(held.getItem())) {
                  ESPItemData espItemData = (ESPItemData)((Map)this.blockConfigs.get()).get(held.getItem());
                  if (espItemData != null) {
                     if (espItemData.tracer) {
                        event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, xl, yl, zl, espItemData.tracerColor);
                     }

                     double x = Mth.lerp(event.tickDelta, entity.xOld, entity.getX()) - entity.getX();
                     double y = Mth.lerp(event.tickDelta, entity.yOld, entity.getY()) - entity.getY();
                     double z = Mth.lerp(event.tickDelta, entity.zOld, entity.getZ()) - entity.getZ();
                     AABB box = entity.getBoundingBox();
                     event.renderer
                        .box(
                           x + box.minX,
                           y + box.minY,
                           z + box.minZ,
                           x + box.maxX,
                           y + box.maxY,
                           z + box.maxZ,
                           espItemData.sideColor,
                           espItemData.lineColor,
                           espItemData.shapeMode,
                           0
                        );
                  } else {
                     if (((ESPItemData)this.defaultBlockConfig.get()).tracer) {
                        event.renderer
                           .line(
                              RenderUtils.center.x,
                              RenderUtils.center.y,
                              RenderUtils.center.z,
                              xl,
                              yl,
                              zl,
                              ((ESPItemData)this.defaultBlockConfig.get()).tracerColor
                           );
                     }

                     double x = Mth.lerp(event.tickDelta, entity.xOld, entity.getX()) - entity.getX();
                     double y = Mth.lerp(event.tickDelta, entity.yOld, entity.getY()) - entity.getY();
                     double z = Mth.lerp(event.tickDelta, entity.zOld, entity.getZ()) - entity.getZ();
                     AABB box = entity.getBoundingBox();
                     event.renderer
                        .box(
                           x + box.minX,
                           y + box.minY,
                           z + box.minZ,
                           x + box.maxX,
                           y + box.maxY,
                           z + box.maxZ,
                           ((ESPItemData)this.defaultBlockConfig.get()).sideColor,
                           ((ESPItemData)this.defaultBlockConfig.get()).lineColor,
                           ((ESPItemData)this.defaultBlockConfig.get()).shapeMode,
                           0
                        );
                  }
               }
            }
         }
      }
   }
}
