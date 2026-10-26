package anticope.rejects.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class RejectsConfig extends System<RejectsConfig> {
   private static final RejectsConfig INSTANCE = new RejectsConfig();
   public RejectsConfig.HttpAllowed httpAllowed = RejectsConfig.HttpAllowed.Everything;
   public String httpUserAgent = "Meteor Client";
   public Set<String> hiddenModules = new HashSet<>();
   public boolean loadSystemFonts = true;
   public boolean duplicateModuleNames = false;

   public RejectsConfig() {
      super("rejects-config");
      this.init();
      this.load(MeteorClient.FOLDER);
   }

   public static RejectsConfig get() {
      return INSTANCE;
   }

   public void setHiddenModules(List<Module> newList) {
      if (newList.size() < this.hiddenModules.size()) {
         ((OkPrompt)((OkPrompt)((OkPrompt)OkPrompt.create().title("Hidden Modules"))
                  .message("In order to see the modules you have removed from the list you need to restart Minecraft."))
               .id("hidden-modules-unhide"))
            .show();
      }

      this.hiddenModules.clear();

      for (Module module : newList) {
         if (module != null) {
            if (module.isActive()) {
               module.toggle();
            }

            this.hiddenModules.add(module.name);
         }
      }
   }

   public List<Module> getHiddenModules() {
      Modules modules = Modules.get();
      return modules == null ? List.of() : this.hiddenModules.stream().<Module>map(modules::get).collect(Collectors.toList());
   }

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("httpAllowed", this.httpAllowed.toString());
      tag.putString("httpUserAgent", this.httpUserAgent);
      tag.putBoolean("loadSystemFonts", this.loadSystemFonts);
      tag.putBoolean("duplicateModuleNames", this.duplicateModuleNames);
      ListTag modulesTag = new ListTag();

      for (String module : this.hiddenModules) {
         modulesTag.add(StringTag.valueOf(module));
      }

      tag.put("hiddenModules", modulesTag);
      return tag;
   }

   public RejectsConfig fromTag(CompoundTag tag) {
      tag.getString("httpAllowed").ifPresent(s -> this.httpAllowed = RejectsConfig.HttpAllowed.valueOf(s));
      this.httpUserAgent = tag.getString("httpUserAgent").orElse(this.httpUserAgent);
      this.loadSystemFonts = tag.getBoolean("loadSystemFonts").orElse(this.loadSystemFonts);
      this.duplicateModuleNames = tag.getBoolean("duplicateModuleNames").orElse(this.duplicateModuleNames);
      tag.getList("hiddenModules").ifPresent(valueTag -> {
         for (Tag tagI : valueTag) {
            tagI.asString().ifPresent(this.hiddenModules::add);
         }
      });
      return this;
   }

   public static enum HttpAllowed {
      Everything,
      NotMeteorApi,
      NotMeteorPing,
      Nothing;
   }
}
