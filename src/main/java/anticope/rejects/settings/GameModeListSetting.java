package anticope.rejects.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Setting.SettingBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.GameType;

public class GameModeListSetting extends Setting<List<GameType>> {
   public GameModeListSetting(
      String name,
      String description,
      List<GameType> defaultValue,
      Consumer<List<GameType>> onChanged,
      Consumer<Setting<List<GameType>>> onModuleActivated,
      IVisible visible
   ) {
      super(name, description, defaultValue, onChanged, onModuleActivated, visible);
   }

   protected List<GameType> parseImpl(String str) {
      String[] values = str.split(",");
      List<GameType> modes = new ArrayList<>(values.length);

      for (String s : values) {
         GameType mode = GameType.byName(s);
         if (mode != null) {
            modes.add(mode);
         }
      }

      return modes;
   }

   protected boolean isValueValid(List<GameType> value) {
      return true;
   }

   protected void resetImpl() {
      this.value = new ArrayList();
   }

   public CompoundTag save(CompoundTag tag) {
      ListTag valueTag = new ListTag();

      for (GameType mode : (List)this.get()) {
         valueTag.add(StringTag.valueOf(mode.getName()));
      }

      tag.put("value", valueTag);
      return tag;
   }

   public List<GameType> load(CompoundTag tag) {
      ((List)this.get()).clear();
      tag.getList("value").ifPresent(valueTag -> {
         for (Tag tagI : valueTag) {
            tagI.asString().ifPresent(str -> {
               GameType mode = GameType.byName(str);
               if (mode != null) {
                  ((List)this.get()).add(mode);
               }
            });
         }
      });
      return (List<GameType>)this.get();
   }

   public static class Builder extends SettingBuilder<GameModeListSetting.Builder, List<GameType>, GameModeListSetting> {
      public Builder() {
         super(new ArrayList(0));
      }

      public GameModeListSetting.Builder defaultValue(List<GameType> map) {
         this.defaultValue = map;
         return this;
      }

      public GameModeListSetting build() {
         return new GameModeListSetting(this.name, this.description, (List<GameType>)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
      }
   }
}
