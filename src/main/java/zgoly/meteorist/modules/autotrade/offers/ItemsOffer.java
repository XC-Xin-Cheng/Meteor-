package zgoly.meteorist.modules.autotrade.offers;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import zgoly.meteorist.settings.StringPairSetting;

public class ItemsOffer extends BaseOffer {
   public static final String type = "Items";
   SettingGroup sgInput = this.settings.createGroup("Input");
   public final Setting<Boolean> checkFirstInputItem = this.sgInput
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("check-first-input-item")).description("Enable filtering for the first input item."))
                  .defaultValue(true))
               .onChanged(value -> reloadParent()))
            .build()
      );
   public final Setting<ItemsOffer.FilterMode> firstInputFilterMode = this.sgInput
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                           .name("first-input-filter-mode"))
                        .description("How to filter the first input item."))
                     .defaultValue(ItemsOffer.FilterMode.Item))
                  .onChanged(value -> reloadParent()))
               .visible(this.checkFirstInputItem::get))
            .build()
      );
   public final Setting<Item> firstInputItem = this.sgInput
      .add(
         ((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                           .name("first-input-item"))
                        .description("First item to input."))
                     .defaultValue(Items.BREAD))
                  .onChanged(value -> reloadParent()))
               .visible(() -> (Boolean)this.checkFirstInputItem.get() && this.firstInputFilterMode.get() == ItemsOffer.FilterMode.Item))
            .build()
      );
   public final Setting<Boolean> checkFirstInputItemCount = this.sgInput
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("check-first-input-item-count")).description("Check count of first input item."))
                     .defaultValue(true))
                  .onChanged(value -> reloadParent()))
               .visible(() -> (Boolean)this.checkFirstInputItem.get() && this.firstInputFilterMode.get() == ItemsOffer.FilterMode.Item))
            .build()
      );
   public final Setting<Boolean> useFinalCount = this.sgInput
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("use-final-count")).description("Use final count after discounts, or base count."))
                  .defaultValue(true))
               .visible(
                  () -> (Boolean)this.checkFirstInputItem.get()
                     && this.firstInputFilterMode.get() == ItemsOffer.FilterMode.Item
                     && (Boolean)this.checkFirstInputItemCount.get()
               ))
            .build()
      );
   public final Setting<Integer> minFirstInputItemCount = this.sgInput
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("min-first-input-item-count"))
                        .description("Minimum count of first input item."))
                     .defaultValue(1))
                  .sliderRange(1, 64)
                  .min(1)
                  .onChanged(value -> reloadParent()))
               .visible(
                  () -> (Boolean)this.checkFirstInputItem.get()
                     && this.firstInputFilterMode.get() == ItemsOffer.FilterMode.Item
                     && (Boolean)this.checkFirstInputItemCount.get()
               ))
            .build()
      );
   public final Setting<Integer> maxFirstInputItemCount = this.sgInput
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("max-first-input-item-count"))
                        .description("Maximum count of first input item."))
                     .defaultValue(16))
                  .sliderRange(1, 64)
                  .min(1)
                  .onChanged(value -> reloadParent()))
               .visible(
                  () -> (Boolean)this.checkFirstInputItem.get()
                     && this.firstInputFilterMode.get() == ItemsOffer.FilterMode.Item
                     && (Boolean)this.checkFirstInputItemCount.get()
               ))
            .build()
      );
   public final Setting<ItemsOffer.MatchMode> firstInputMatchMode = this.sgInput
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                           .name("first-input-match-mode"))
                        .description("Whether all NBT filters must match, or any one is enough."))
                     .defaultValue(ItemsOffer.MatchMode.All))
                  .visible(() -> (Boolean)this.checkFirstInputItem.get() && this.firstInputFilterMode.get() == ItemsOffer.FilterMode.Nbt))
               .onChanged(value -> reloadParent()))
            .build()
      );
   public final Setting<List<Tuple<String, String>>> firstInputNbtFilter = this.sgInput
      .add(
         ((StringPairSetting.Builder)((StringPairSetting.Builder)((StringPairSetting.Builder)((StringPairSetting.Builder)new StringPairSetting.Builder()
                        .name("first-input-nbt-filter"))
                     .description("NBT path and regex pattern pairs to match on first input item."))
                  .defaultValue(new ArrayList<>())
                  .placeholder(new Tuple("nbt.path", "regex"))
                  .visible(() -> (Boolean)this.checkFirstInputItem.get() && this.firstInputFilterMode.get() == ItemsOffer.FilterMode.Nbt))
               .onChanged(value -> reloadParent()))
            .build()
      );
   public final Setting<Boolean> checkSecondInputItem = this.sgInput
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("check-second-input-item")).description("Enable filtering for the second input item."))
                  .defaultValue(false))
               .onChanged(value -> reloadParent()))
            .build()
      );
   public final Setting<ItemsOffer.FilterMode> secondInputFilterMode = this.sgInput
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                           .name("second-input-filter-mode"))
                        .description("How to filter the second input item."))
                     .defaultValue(ItemsOffer.FilterMode.Item))
                  .onChanged(value -> reloadParent()))
               .visible(this.checkSecondInputItem::get))
            .build()
      );
   public final Setting<Item> secondInputItem = this.sgInput
      .add(
         ((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                           .name("second-input-item"))
                        .description("Second item to input."))
                     .defaultValue(Items.AIR))
                  .onChanged(value -> reloadParent()))
               .visible(() -> (Boolean)this.checkSecondInputItem.get() && this.secondInputFilterMode.get() == ItemsOffer.FilterMode.Item))
            .build()
      );
   public final Setting<Boolean> checkSecondInputItemCount = this.sgInput
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("check-second-input-item-count"))
                        .description("Check count of second input item."))
                     .defaultValue(false))
                  .onChanged(value -> reloadParent()))
               .visible(() -> (Boolean)this.checkSecondInputItem.get() && this.secondInputFilterMode.get() == ItemsOffer.FilterMode.Item))
            .build()
      );
   public final Setting<Integer> minSecondInputItemCount = this.sgInput
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("min-second-input-item-count"))
                        .description("Minimum count of second input item."))
                     .defaultValue(1))
                  .sliderRange(1, 64)
                  .min(1)
                  .onChanged(value -> reloadParent()))
               .visible(
                  () -> (Boolean)this.checkSecondInputItem.get()
                     && this.secondInputFilterMode.get() == ItemsOffer.FilterMode.Item
                     && (Boolean)this.checkSecondInputItemCount.get()
               ))
            .build()
      );
   public final Setting<Integer> maxSecondInputItemCount = this.sgInput
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("max-second-input-item-count"))
                        .description("Maximum count of second input item."))
                     .defaultValue(16))
                  .sliderRange(1, 64)
                  .min(1)
                  .onChanged(value -> reloadParent()))
               .visible(
                  () -> (Boolean)this.checkSecondInputItem.get()
                     && this.secondInputFilterMode.get() == ItemsOffer.FilterMode.Item
                     && (Boolean)this.checkSecondInputItemCount.get()
               ))
            .build()
      );
   public final Setting<ItemsOffer.MatchMode> secondInputMatchMode = this.sgInput
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                           .name("second-input-match-mode"))
                        .description("Whether all NBT filters must match, or any one is enough."))
                     .defaultValue(ItemsOffer.MatchMode.All))
                  .visible(() -> (Boolean)this.checkSecondInputItem.get() && this.secondInputFilterMode.get() == ItemsOffer.FilterMode.Nbt))
               .onChanged(value -> reloadParent()))
            .build()
      );
   public final Setting<List<Tuple<String, String>>> secondInputNbtFilter = this.sgInput
      .add(
         ((StringPairSetting.Builder)((StringPairSetting.Builder)((StringPairSetting.Builder)((StringPairSetting.Builder)new StringPairSetting.Builder()
                        .name("second-input-nbt-filter"))
                     .description("NBT path and regex pattern pairs to match on second input item."))
                  .defaultValue(new ArrayList<>())
                  .placeholder(new Tuple("nbt.path", "regex"))
                  .visible(() -> (Boolean)this.checkSecondInputItem.get() && this.secondInputFilterMode.get() == ItemsOffer.FilterMode.Nbt))
               .onChanged(value -> reloadParent()))
            .build()
      );
   SettingGroup sgOutput = this.settings.createGroup("Output");
   public final Setting<Boolean> checkOutputItem = this.sgOutput
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("check-output-item")).description("Enable filtering for the output item."))
                  .defaultValue(true))
               .onChanged(value -> reloadParent()))
            .build()
      );
   public final Setting<ItemsOffer.FilterMode> outputFilterMode = this.sgOutput
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                           .name("output-filter-mode"))
                        .description("How to filter the output item."))
                     .defaultValue(ItemsOffer.FilterMode.Item))
                  .onChanged(value -> reloadParent()))
               .visible(this.checkOutputItem::get))
            .build()
      );
   public final Setting<Item> outputItem = this.sgOutput
      .add(
         ((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)((meteordevelopment.meteorclient.settings.ItemSetting.Builder)new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                           .name("output-item"))
                        .description("Output item."))
                     .defaultValue(Items.EMERALD))
                  .onChanged(value -> reloadParent()))
               .visible(() -> (Boolean)this.checkOutputItem.get() && this.outputFilterMode.get() == ItemsOffer.FilterMode.Item))
            .build()
      );
   public final Setting<Boolean> checkOutputItemCount = this.sgOutput
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("check-output-item-count")).description("Check count of output item."))
                     .defaultValue(false))
                  .onChanged(value -> reloadParent()))
               .visible(() -> (Boolean)this.checkOutputItem.get() && this.outputFilterMode.get() == ItemsOffer.FilterMode.Item))
            .build()
      );
   public final Setting<Integer> minOutputItemCount = this.sgOutput
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("min-output-item-count"))
                        .description("Minimum count of output item."))
                     .defaultValue(1))
                  .sliderRange(1, 64)
                  .min(1)
                  .onChanged(value -> reloadParent()))
               .visible(
                  () -> (Boolean)this.checkOutputItem.get()
                     && this.outputFilterMode.get() == ItemsOffer.FilterMode.Item
                     && (Boolean)this.checkOutputItemCount.get()
               ))
            .build()
      );
   public final Setting<Integer> maxOutputItemCount = this.sgOutput
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("max-output-item-count"))
                        .description("Maximum count of output item."))
                     .defaultValue(16))
                  .sliderRange(1, 64)
                  .min(1)
                  .onChanged(value -> reloadParent()))
               .visible(
                  () -> (Boolean)this.checkOutputItem.get()
                     && this.outputFilterMode.get() == ItemsOffer.FilterMode.Item
                     && (Boolean)this.checkOutputItemCount.get()
               ))
            .build()
      );
   public final Setting<ItemsOffer.MatchMode> outputMatchMode = this.sgOutput
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                           .name("output-match-mode"))
                        .description("Whether all NBT filters must match, or any one is enough."))
                     .defaultValue(ItemsOffer.MatchMode.All))
                  .visible(() -> (Boolean)this.checkOutputItem.get() && this.outputFilterMode.get() == ItemsOffer.FilterMode.Nbt))
               .onChanged(value -> reloadParent()))
            .build()
      );
   public final Setting<List<Tuple<String, String>>> outputNbtFilter = this.sgOutput
      .add(
         ((StringPairSetting.Builder)((StringPairSetting.Builder)((StringPairSetting.Builder)((StringPairSetting.Builder)new StringPairSetting.Builder()
                        .name("output-nbt-filter"))
                     .description("NBT path and regex pattern pairs to match on output item."))
                  .defaultValue(List.of(new Tuple("components.\"minecraft:stored_enchantments\"", "minecraft:mending")))
                  .placeholder(new Tuple("nbt.path", "regex"))
                  .visible(() -> (Boolean)this.checkOutputItem.get() && this.outputFilterMode.get() == ItemsOffer.FilterMode.Nbt))
               .onChanged(value -> reloadParent()))
            .build()
      );

   @Override
   public String getTypeName() {
      return "Items";
   }

   public ItemsOffer copy() {
      return (ItemsOffer)new ItemsOffer().fromTag(this.toTag());
   }

   public static enum FilterMode {
      Item,
      Nbt;
   }

   public static enum MatchMode {
      Any,
      All;
   }
}
