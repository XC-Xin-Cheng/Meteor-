package nekiplay.meteorplus.features.modules.render;

import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.ItemListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import nekiplay.meteorplus.settings.items.HighlightItemData;
import nekiplay.meteorplus.settings.items.ItemDataSetting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ItemHighlightPlus extends Module {
   public final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<List<Item>> whitelist = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("whitelist")).description("Items to highlight.")).defaultValue(new Item[]{Items.ELYTRA}).build());
   public final Setting<HighlightItemData> defaultBlockConfig = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.GenericSetting.Builder)((meteordevelopment.meteorclient.settings.GenericSetting.Builder)((meteordevelopment.meteorclient.settings.GenericSetting.Builder)new meteordevelopment.meteorclient.settings.GenericSetting.Builder()
                     .name("whitelist-default-config"))
                  .description("Default item config."))
               .defaultValue(new HighlightItemData(new SettingColor(0, 255, 200, 25))))
            .build()
      );
   public final Setting<Map<Item, HighlightItemData>> itemsConfigs = this.sgGeneral
      .add(
         ((ItemDataSetting.Builder)((ItemDataSetting.Builder)new ItemDataSetting.Builder().name("whitelist-items-configs"))
               .description("Config for each highlight."))
            .defaultData(this.defaultBlockConfig)
            .build()
      );

   public ItemHighlightPlus() {
      super(Categories.Render, "Item-Highlight+", "Highlights selected items when in inventorys and guis.");
   }
}
