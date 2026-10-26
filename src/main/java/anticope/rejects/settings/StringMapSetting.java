package anticope.rejects.settings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox.Renderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Setting.SettingBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;

public class StringMapSetting extends Setting<Map<String, String>> {
   public final Class<? extends Renderer> renderer;

   public StringMapSetting(
      String name,
      String description,
      Map<String, String> defaultValue,
      Consumer<Map<String, String>> onChanged,
      Consumer<Setting<Map<String, String>>> onModuleActivated,
      IVisible visible,
      Class<? extends Renderer> renderer
   ) {
      super(name, description, defaultValue, onChanged, onModuleActivated, visible);
      this.renderer = renderer;
   }

   protected Map<String, String> parseImpl(String str) {
      String[] values = str.split(",");
      Map<String, String> map = new LinkedHashMap<>(values.length / 2);

      try {
         String left = null;

         for (int i = 0; i < values.length; i++) {
            if (i % 2 == 0) {
               left = values[i];
            } else {
               map.put(left, values[i]);
            }
         }
      } catch (Exception var6) {
      }

      return map;
   }

   protected boolean isValueValid(Map<String, String> value) {
      return true;
   }

   protected void resetImpl() {
      this.value = new LinkedHashMap((Map)this.defaultValue);
   }

   protected CompoundTag save(CompoundTag tag) {
      CompoundTag valueTag = new CompoundTag();

      for (String key : ((Map)this.get()).keySet()) {
         valueTag.put(key, StringTag.valueOf((String)((Map)this.get()).get(key)));
      }

      tag.put("map", valueTag);
      return tag;
   }

   protected Map<String, String> load(CompoundTag tag) {
      ((Map)this.get()).clear();
      tag.getCompound("map").ifPresent(valueTag -> {
         for (String key : valueTag.keySet()) {
            valueTag.getString(key).ifPresent(value -> ((Map)this.get()).put(key, value));
         }
      });
      return (Map<String, String>)this.get();
   }

   public static void fillTable(GuiTheme theme, WTable table, StringMapSetting setting) {
      table.clear();
      Map<String, String> map = (Map<String, String>)setting.get();

      for (String key : map.keySet()) {
         AtomicReference<String> key2 = new AtomicReference<>(key);
         WTextBox textBoxK = (WTextBox)table.add(theme.textBox(key2.get())).minWidth(150.0).expandX().widget();
         textBoxK.actionOnUnfocused = () -> {
            String text = textBoxK.get();
            if (map.containsKey(text)) {
               textBoxK.set(key2.get());
            } else {
               String value = map.remove(key2.get());
               key2.set(text);
               map.put(text, value);
            }
         };
         WTextBox textBoxV = (WTextBox)table.add(theme.textBox(map.get(key2.get()), (text1, c) -> true, setting.renderer)).minWidth(150.0).expandX().widget();
         textBoxV.actionOnUnfocused = () -> map.replace(key2.get(), textBoxV.get());
         WMinus delete = (WMinus)table.add(theme.minus()).widget();
         delete.action = () -> {
            map.remove(key2.get());
            fillTable(theme, table, setting);
         };
         table.row();
      }

      if (!map.isEmpty()) {
         table.add(theme.horizontalSeparator()).expandX();
         table.row();
      }

      WButton reset = (WButton)table.add(theme.button(GuiRenderer.RESET)).widget();
      reset.action = () -> {
         setting.reset();
         fillTable(theme, table, setting);
      };
      WPlus add = (WPlus)table.add(theme.plus()).widget();
      add.action = () -> {
         map.put("", "");
         fillTable(theme, table, setting);
      };
      table.row();
   }

   public static class Builder extends SettingBuilder<StringMapSetting.Builder, Map<String, String>, StringMapSetting> {
      private Class<? extends Renderer> renderer;

      public Builder() {
         super(new LinkedHashMap(0));
      }

      public StringMapSetting.Builder defaultValue(Map<String, String> map) {
         this.defaultValue = map;
         return this;
      }

      public StringMapSetting.Builder renderer(Class<? extends Renderer> renderer) {
         this.renderer = renderer;
         return this;
      }

      public StringMapSetting build() {
         return new StringMapSetting(
            this.name, this.description, (Map<String, String>)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.renderer
         );
      }
   }
}
