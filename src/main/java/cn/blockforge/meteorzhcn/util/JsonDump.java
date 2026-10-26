package cn.blockforge.meteorzhcn.util;

import cn.blockforge.meteorzhcn.mixin.SettingGroupAccessor;
import cn.blockforge.meteorzhcn.modules.Translation;
import cn.blockforge.meteorzhcn.util.trans_engine.AbstractTransEngine;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

public class JsonDump {
   private static final JsonDump INSTANCE = new JsonDump();
   private LinkedHashMap<String, String> entMap = new LinkedHashMap<>();
   private BufferedWriter dumpBW;

   public static JsonDump getINSTANCE() {
      return INSTANCE;
   }

   private Translation getTran() {
      return (Translation)Modules.get().get(Translation.class);
   }

   public void write(AbstractTransEngine engine, AbstractTransEngine engine2) {
      this.dump2Set(engine, engine2);

      label103: {
         try {
            File path = new File((String)this.getTran().sSetDumpPath.get());
            if (path.exists() || path.createNewFile()) {
               this.dumpBW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, false), StandardCharsets.UTF_8));

               for (Entry<String, String> entry : this.entMap.entrySet()) {
                  this.dumpBW.write("\"" + entry.getKey() + "\":\"" + TransUtil.formatValue(entry.getValue()) + "\",");
                  this.dumpBW.newLine();
               }

               this.dumpBW.flush();
               this.dumpBW.close();
               break label103;
            }

            ChatUtils.warning("DumpError Can't Create Dump File", new Object[0]);
            this.entMap.clear();
         } catch (IOException var15) {
            ChatUtils.error(var15.getMessage(), new Object[0]);
            this.entMap.clear();
            return;
         } finally {
            try {
               if (this.dumpBW != null) {
                  this.dumpBW.close();
               }
            } catch (IOException var14) {
            }
         }

         return;
      }

      this.entMap.clear();
   }

   private void dump2Set(AbstractTransEngine engine, AbstractTransEngine engine2) {
      boolean dumpText = (Boolean)this.getTran().bSetDumpText.get();

      for (Module module : Modules.get().getAll()) {
         String addonName = TransUtil.getAddonName(module);
         if (((Set)this.getTran().translationModules.get()).contains(addonName)) {
            String nameKey = engine.getModuleNameKey(module);
            this.addEntry(nameKey, dumpText ? engine2.transModuleName(module) : module.name);
            String desKey = engine.getModuleDescriptionKey(module);
            this.addEntry(desKey, dumpText ? engine2.transModuleDescription(module) : module.description);

            for (SettingGroup group : module.settings.groups) {
               for (Setting<?> setting : ((SettingGroupAccessor)group).getSettings()) {
                  String settingNameKey = engine.getSettingNameKey(module, group, setting);
                  this.addEntry(settingNameKey, dumpText ? engine2.transSettingName(module, group, setting) : setting.name);
                  String settDescKey = engine.getSettingDesKey(module, group, setting);
                  this.addEntry(settDescKey, dumpText ? engine2.transSettingDes(module, group, setting) : setting.description);
               }
            }
         }
      }
   }

   private void addEntry(String key, String value) {
      this.entMap.putIfAbsent(key, value);
   }
}
