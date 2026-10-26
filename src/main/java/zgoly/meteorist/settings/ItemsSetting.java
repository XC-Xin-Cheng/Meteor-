package zgoly.meteorist.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.settings.ItemSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Setting.SettingBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import zgoly.meteorist.Meteorist;

public class ItemsSetting extends Setting<List<Item>> {
   private final Predicate<Item> filter;

   public ItemsSetting(
      String name,
      String description,
      List<Item> defaultValue,
      Consumer<List<Item>> onChanged,
      Consumer<Setting<List<Item>>> onModuleActivated,
      IVisible visible,
      Predicate<Item> filter
   ) {
      super(name, description, defaultValue, onChanged, onModuleActivated, visible);
      this.filter = filter;
   }

   protected List<Item> parseImpl(String str) {
      return new ArrayList<>();
   }

   protected boolean isValueValid(List<Item> value) {
      return true;
   }

   public CompoundTag save(CompoundTag tag) {
      ListTag listTag = new ListTag();

      for (Item item : (List)this.get()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(item);
         listTag.add(StringTag.valueOf(id.toString()));
      }

      tag.put("value", listTag);
      return tag;
   }

   public List<Item> load(CompoundTag tag) {
      ListTag listTag = tag.getListOrEmpty("value");
      ((List)this.get()).clear();

      for (Tag element : listTag) {
         String idStr = element.asString().orElse("");
         Item item = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(idStr));
         if (item != Items.AIR) {
            ((List)this.get()).add(item);
         }
      }

      return (List<Item>)this.get();
   }

   public static void register() {
      DefaultSettingsWidgetFactory.registerCustomFactory(ItemsSetting.class, theme -> (table, setting) -> {
         WTable wtable = (WTable)table.add(theme.table()).expandX().widget();
         fillTable(theme, wtable, (ItemsSetting)setting);
      });
   }

   public static void fillTable(GuiTheme theme, WTable table, ItemsSetting setting) {
      table.clear();
      List<Item> items = (List<Item>)setting.get();

      for (int i = 0; i < items.size(); i++) {
         int index = i;
         Item item = items.get(index);
         WContainer container = (WContainer)table.add(theme.horizontalList()).expandX().widget();
         WWidget icon = container.add(theme.item(item.getDefaultInstance())).widget();
         icon.tooltip = item.getName(item.getDefaultInstance()).getString();
         WButton edit = (WButton)container.add(theme.button(GuiRenderer.EDIT)).widget();
         edit.tooltip = "Edit the item.";
         edit.action = () -> {
            final ItemSetting tempSetting = ((meteordevelopment.meteorclient.settings.ItemSetting.Builder)new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                  .defaultValue(item))
               .filter(setting.filter)
               .build();
            MeteorClient.mc.setScreen(new ItemSettingScreen(theme, tempSetting) {
               public void onClosed() {
                  Item result = (Item)tempSetting.get();
                  items.set(index, result);
                  ItemsSetting.fillTable(this.theme, table, setting);
               }
            });
         };
         if (items.size() > 1) {
            WContainer moveContainer = (WContainer)container.add(theme.horizontalList()).expandX().widget();
            if (index > 0) {
               WButton moveUp = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_UP)).expandX().widget();
               moveUp.tooltip = "Move item up.";
               moveUp.action = () -> {
                  items.remove(index);
                  items.add(index - 1, item);
                  fillTable(theme, table, setting);
               };
            }

            if (index < items.size() - 1) {
               WButton moveDown = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_DOWN)).expandX().widget();
               moveDown.tooltip = "Move item down.";
               moveDown.action = () -> {
                  items.remove(index);
                  items.add(index + 1, item);
                  fillTable(theme, table, setting);
               };
            }
         }

         WButton copy = (WButton)container.add(theme.button(GuiRenderer.COPY)).widget();
         copy.tooltip = "Duplicate item.";
         copy.action = () -> {
            items.add(index, item);
            fillTable(theme, table, setting);
         };
         WMinus remove = (WMinus)container.add(theme.minus()).widget();
         remove.tooltip = "Remove item.";
         remove.action = () -> {
            items.remove(index);
            fillTable(theme, table, setting);
         };
         table.row();
      }

      if (!items.isEmpty()) {
         table.add(theme.horizontalSeparator()).expandX();
         table.row();
      }

      WButton add = (WButton)table.add(theme.button("Add")).expandX().widget();
      add.tooltip = "Add new item to the list.";
      add.action = () -> {
         final ItemSetting tempSetting = new meteordevelopment.meteorclient.settings.ItemSetting.Builder().filter(setting.filter).build();
         MeteorClient.mc.setScreen(new ItemSettingScreen(theme, tempSetting) {
            public void onClosed() {
               Item result = (Item)tempSetting.get();
               if (result != null) {
                  tempSetting.set(null);
                  items.add(result);
                  ItemsSetting.fillTable(this.theme, table, setting);
               }
            }
         });
      };
      WButton reset = (WButton)table.add(theme.button(GuiRenderer.RESET)).widget();
      reset.action = () -> {
         setting.reset();
         fillTable(theme, table, setting);
      };
   }

   protected void resetImpl() {
      this.value = new ArrayList((Collection)this.defaultValue);
   }

   public static class Builder extends SettingBuilder<ItemsSetting.Builder, List<Item>, ItemsSetting> {
      private Predicate<Item> filter;

      public Builder() {
         super(new ArrayList());
      }

      public ItemsSetting.Builder filter(Predicate<Item> filter) {
         this.filter = filter;
         return this;
      }

      public ItemsSetting build() {
         return new ItemsSetting(this.name, this.description, (List<Item>)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter);
      }
   }
}
