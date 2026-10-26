package zgoly.meteorist.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Setting.SettingBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import zgoly.meteorist.Meteorist;

public class StringPairSetting extends Setting<List<Tuple<String, String>>> {
   private final Tuple<String, String> placeholder;

   public StringPairSetting(
      String name,
      String description,
      List<Tuple<String, String>> defaultValue,
      Tuple<String, String> placeholder,
      Consumer<List<Tuple<String, String>>> onChanged,
      Consumer<Setting<List<Tuple<String, String>>>> onModuleActivated,
      IVisible visible
   ) {
      super(name, description, defaultValue, onChanged, onModuleActivated, visible);
      this.placeholder = placeholder;
   }

   public static void register() {
      DefaultSettingsWidgetFactory.registerCustomFactory(StringPairSetting.class, theme -> (table, setting) -> {
         WTable wtable = (WTable)table.add(theme.table()).expandX().widget();
         fillTable(theme, wtable, (StringPairSetting)setting);
      });
   }

   public static void fillTable(GuiTheme theme, WTable table, StringPairSetting setting) {
      table.clear();
      List<Tuple<String, String>> pairs = (List<Tuple<String, String>>)setting.get();
      Tuple<String, String> placeholder = setting.placeholder;

      for (Tuple<String, String> pair : pairs) {
         WContainer container = (WContainer)table.add(theme.horizontalList()).expandX().widget();
         int index = pairs.indexOf(pair);
         WTextBox textBoxKey = (WTextBox)container.add(theme.textBox((String)pair.getA(), (String)placeholder.getA())).minWidth(150.0).expandX().widget();
         textBoxKey.actionOnUnfocused = () -> pair.setA(textBoxKey.get());
         WTextBox textBoxValue = (WTextBox)container.add(theme.textBox((String)pair.getB(), (String)placeholder.getB())).minWidth(150.0).expandX().widget();
         textBoxValue.actionOnUnfocused = () -> pair.setB(textBoxValue.get());
         if (pairs.size() > 1) {
            WContainer moveContainer = (WContainer)container.add(theme.horizontalList()).expandX().widget();
            if (index > 0) {
               WButton moveUp = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_UP)).expandX().widget();
               moveUp.tooltip = "Move pair up.";
               moveUp.action = () -> {
                  pairs.remove(index);
                  pairs.add(index - 1, pair);
                  fillTable(theme, table, setting);
               };
            }

            if (index < pairs.size() - 1) {
               WButton moveDown = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_DOWN)).expandX().widget();
               moveDown.tooltip = "Move pair down.";
               moveDown.action = () -> {
                  pairs.remove(index);
                  pairs.add(index + 1, pair);
                  fillTable(theme, table, setting);
               };
            }
         }

         WButton copy = (WButton)container.add(theme.button(GuiRenderer.COPY)).widget();
         copy.tooltip = "Duplicate pair.";
         copy.action = () -> {
            pairs.add(index, new Tuple((String)pair.getA(), (String)pair.getB()));
            fillTable(theme, table, setting);
         };
         WMinus remove = (WMinus)container.add(theme.minus()).widget();
         remove.tooltip = "Remove pair.";
         remove.action = () -> {
            pairs.remove(pair);
            fillTable(theme, table, setting);
         };
         table.row();
      }

      if (!pairs.isEmpty()) {
         table.add(theme.horizontalSeparator()).expandX();
         table.row();
      }

      WButton add = (WButton)table.add(theme.button("Add")).expandX().widget();
      add.tooltip = "Add new pair to the list.";
      add.action = () -> {
         pairs.add(new Tuple("", ""));
         fillTable(theme, table, setting);
      };
      WButton reset = (WButton)table.add(theme.button(GuiRenderer.RESET)).widget();
      reset.action = () -> {
         setting.reset();
         fillTable(theme, table, setting);
      };
      table.row();
   }

   protected void resetImpl() {
      this.value = new ArrayList((Collection)this.defaultValue);
   }

   protected List<Tuple<String, String>> parseImpl(String str) {
      String[] values = str.split(",");
      List<Tuple<String, String>> pairs = new ArrayList<>(values.length / 2);

      try {
         for (int i = 0; i < values.length; i += 2) {
            pairs.add(new Tuple(values[i], values[i + 1]));
         }
      } catch (Exception var5) {
      }

      return pairs;
   }

   protected boolean isValueValid(List<Tuple<String, String>> value) {
      return true;
   }

   protected CompoundTag save(CompoundTag tag) {
      ListTag valueTag = new ListTag();

      for (Tuple<String, String> pair : (List)this.get()) {
         ListTag pairTag = new ListTag();
         pairTag.add(StringTag.valueOf((String)pair.getA()));
         pairTag.add(StringTag.valueOf((String)pair.getB()));
         valueTag.add(pairTag);
      }

      tag.put("value", valueTag);
      return tag;
   }

   protected List<Tuple<String, String>> load(CompoundTag tag) {
      ListTag valueTag = tag.getListOrEmpty("value");
      ((List)this.get()).clear();

      for (Tag nbtElement : valueTag) {
         if (nbtElement instanceof ListTag pairList) {
            String left = pairList.get(0).asString().orElse("");
            String right = pairList.get(1).asString().orElse("");
            ((List)this.get()).add(new Tuple(left, right));
         }
      }

      return (List<Tuple<String, String>>)this.get();
   }

   public static class Builder extends SettingBuilder<StringPairSetting.Builder, List<Tuple<String, String>>, StringPairSetting> {
      private Tuple<String, String> placeholder;

      public Builder() {
         super(new ArrayList(0));
      }

      public StringPairSetting.Builder defaultValue(List<Tuple<String, String>> pairs) {
         this.defaultValue = pairs;
         return this;
      }

      public StringPairSetting.Builder placeholder(Tuple<String, String> pair) {
         this.placeholder = pair;
         return this;
      }

      public StringPairSetting build() {
         return new StringPairSetting(
            this.name, this.description, (List<Tuple<String, String>>)this.defaultValue, this.placeholder, this.onChanged, this.onModuleActivated, this.visible
         );
      }
   }
}
