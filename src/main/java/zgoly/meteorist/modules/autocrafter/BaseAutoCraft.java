package zgoly.meteorist.modules.autocrafter;

import java.util.List;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import zgoly.meteorist.settings.ItemsSetting;

public class BaseAutoCraft implements ISerializable<BaseAutoCraft> {
   Settings settings = new Settings();
   SettingGroup sgAutoCraft = this.settings.createGroup("Auto Craft");
   public final Setting<List<Item>> inputWhitelist = this.sgAutoCraft
      .add(
         ((ItemsSetting.Builder)((ItemsSetting.Builder)((ItemsSetting.Builder)new ItemsSetting.Builder().name("input-whitelist"))
                  .description("Items allowed as craft ingredients."))
               .defaultValue(List.of(Items.GOLD_INGOT)))
            .build()
      );
   public final Setting<Boolean> useFirstMatchingVariant = this.sgAutoCraft
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("use-first-matching-variant"))
                  .description("If enabled, only the first matching variant per ingredient will be used for " + this.inputWhitelist.title + " filtering."))
               .defaultValue(true))
            .build()
      );
   public final Setting<List<Item>> inputBlacklist = this.sgAutoCraft
      .add(
         ((ItemsSetting.Builder)((ItemsSetting.Builder)new ItemsSetting.Builder().name("input-blacklist"))
               .description("Items not allowed as craft ingredients."))
            .build()
      );
   public final Setting<Item> outputItem = this.sgAutoCraft
      .add(
         ((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                     .name("output-item"))
                  .description("The item to be crafted."))
               .defaultValue(Items.GOLD_NUGGET))
            .build()
      );
   public final Setting<Boolean> stackPerTick = this.sgAutoCraft
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("stack-per-tick")).description("Craft full stack per tick instead of one item per tick."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> keepIngredientsForExtra = this.sgAutoCraft
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("keep-ingredients-for-extra"))
                  .description("Keep ingredients for one extra item. Will not work with " + this.stackPerTick.title + "."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> dropOnCraft = this.sgAutoCraft
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("drop-on-craft")).description("Drop the crafted item instead of placing it in inventory."))
               .defaultValue(false))
            .build()
      );

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.put("settings", this.settings.toTag());
      return tag;
   }

   public BaseAutoCraft fromTag(CompoundTag tag) {
      CompoundTag settingsTag = (CompoundTag)tag.get("settings");
      if (settingsTag != null) {
         this.settings.fromTag(settingsTag);
      }

      return this;
   }

   public BaseAutoCraft copy() {
      return new BaseAutoCraft().fromTag(this.toTag());
   }
}
