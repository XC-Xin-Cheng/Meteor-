package nekiplay.meteorplus.settings.items;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Setting.SettingBuilder;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class ItemDataSetting<T extends ICopyable<T> & ISerializable<T> & IChangeable & IItemData<T>> extends Setting<Map<Item, T>> {
   public final IGetter<T> defaultData;

   public ItemDataSetting(
      String name,
      String description,
      Map<Item, T> defaultValue,
      Consumer<Map<Item, T>> onChanged,
      Consumer<Setting<Map<Item, T>>> onModuleActivated,
      IGetter<T> defaultData,
      IVisible visible
   ) {
      super(name, description, defaultValue, onChanged, onModuleActivated, visible);
      this.defaultData = defaultData;
   }

   public void resetImpl() {
      this.value = new HashMap((Map)this.defaultValue);
   }

   protected Map<Item, T> parseImpl(String str) {
      return new HashMap<>(0);
   }

   protected boolean isValueValid(Map<Item, T> value) {
      return true;
   }

   protected CompoundTag save(CompoundTag tag) {
      CompoundTag valueTag = new CompoundTag();

      for (Item block : ((Map)this.get()).keySet()) {
         valueTag.put(BuiltInRegistries.ITEM.getKey(block).toString(), ((ISerializable)((ICopyable)((Map)this.get()).get(block))).toTag());
      }

      tag.put("value", valueTag);
      return tag;
   }

   protected Map<Item, T> load(CompoundTag tag) {
      ((Map)this.get()).clear();
      CompoundTag valueTag = (CompoundTag)tag.getCompound("value").get();

      for (String key : valueTag.keySet()) {
         ((Map)this.get())
            .put(
               (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(key)),
               (ICopyable)((ISerializable)((ICopyable)this.defaultData.get()).copy()).fromTag((CompoundTag)valueTag.getCompound(key).get())
            );
      }

      return (Map<Item, T>)this.get();
   }

   public static class Builder<T extends ICopyable<T> & ISerializable<T> & IChangeable & IItemData<T>>
      extends SettingBuilder<ItemDataSetting.Builder<T>, Map<Item, T>, ItemDataSetting<T>> {
      private IGetter<T> defaultData;

      public Builder() {
         super(new HashMap(0));
      }

      public ItemDataSetting.Builder<T> defaultData(IGetter<T> defaultData) {
         this.defaultData = defaultData;
         return this;
      }

      public ItemDataSetting<T> build() {
         return new ItemDataSetting<>(
            this.name, this.description, (Map<Item, T>)this.defaultValue, this.onChanged, this.onModuleActivated, this.defaultData, this.visible
         );
      }
   }
}
