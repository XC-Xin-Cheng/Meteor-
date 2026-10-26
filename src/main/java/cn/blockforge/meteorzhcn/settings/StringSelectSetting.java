package cn.blockforge.meteorzhcn.settings;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Setting.SettingBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class StringSelectSetting extends Setting<Set<String>> {
   public final Predicate<String> filter;
   private List<String> suggestions;
   public Set<String> validValues;
   private static final List<String> groups = List.of("animal", "wateranimal", "monster", "ambient", "misc");

   public StringSelectSetting(
      String name,
      String description,
      Set<String> defaultValue,
      Consumer<Set<String>> onChanged,
      Consumer<Setting<Set<String>>> onModuleActivated,
      IVisible visible,
      Predicate<String> filter,
      Set<String> validValues
   ) {
      super(name, description, defaultValue, onChanged, onModuleActivated, visible);
      this.validValues = validValues;
      this.filter = filter;
   }

   public void resetImpl() {
      this.value = new ObjectOpenHashSet((Collection)this.defaultValue);
   }

   protected Set<String> parseImpl(String str) {
      return Arrays.stream(str.split(",")).collect(Collectors.toSet());
   }

   protected boolean isValueValid(Set<String> value) {
      return true;
   }

   public List<String> getSuggestions() {
      if (this.suggestions == null) {
         this.suggestions = new ArrayList<>(groups);

         for (String str : this.validValues) {
            if (this.filter == null || this.filter.test(str)) {
               this.suggestions.add(str);
            }
         }
      }

      return this.suggestions;
   }

   public CompoundTag save(CompoundTag tag) {
      ListTag valueTag = new ListTag();

      for (String s : (Set)this.get()) {
         valueTag.add(StringTag.valueOf(s));
      }

      tag.put("value", valueTag);
      return tag;
   }

   public Set<String> load(CompoundTag tag) {
      ((Set)this.get()).clear();

      for (Tag tagI : tag.getListOrEmpty("value")) {
         String s = tagI.asString().orElse("");
         if ((this.filter == null || this.filter.test(s)) && this.validValues.contains(s)) {
            ((Set)this.get()).add(s);
         }
      }

      return (Set<String>)this.get();
   }

   public static class Builder extends SettingBuilder<StringSelectSetting.Builder, Set<String>, StringSelectSetting> {
      private Predicate<String> filter;
      private Set<String> validValues = new LinkedHashSet<>();

      public Builder() {
         super(new ObjectOpenHashSet(0));
      }

      public StringSelectSetting.Builder defaultValue(String... defaults) {
         return (StringSelectSetting.Builder)this.defaultValue(defaults != null ? new ObjectOpenHashSet(defaults) : new ObjectOpenHashSet(0));
      }

      public StringSelectSetting.Builder validValues(String... defaults) {
         this.validValues.addAll(List.of(defaults));
         return this;
      }

      public StringSelectSetting.Builder validValues(Set<String> validValues) {
         this.validValues.addAll(validValues);
         return this;
      }

      public StringSelectSetting.Builder filter(Predicate<String> filter) {
         this.filter = filter;
         return this;
      }

      public StringSelectSetting build() {
         return new StringSelectSetting(
            this.name, this.description, (Set<String>)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter, this.validValues
         );
      }
   }
}
