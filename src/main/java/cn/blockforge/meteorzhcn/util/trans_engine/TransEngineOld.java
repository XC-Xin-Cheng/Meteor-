package cn.blockforge.meteorzhcn.util.trans_engine;

import cn.blockforge.meteorzhcn.util.KeyBuilder;
import cn.blockforge.meteorzhcn.util.TransUtil;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class TransEngineOld extends AbstractTransEngine {
   KeyBuilder builder = new KeyBuilder();

   @Override
   public String getModuleNameKey(Module module) {
      String moduleName = module.name;
      this.builder.reset();
      return this.builder.append(TransUtil.getAddonName(module)).append(TransUtil.baseFormat(module.category.name)).end(TransUtil.baseFormat(moduleName));
   }

   @Override
   public String getModuleDescriptionKey(Module module) {
      this.builder.reset();
      return this.builder.module(module).end("description");
   }

   @Override
   public String getSettingNameKey(Module module, SettingGroup group, Setting setting) {
      String settingName = setting.name;
      this.builder.reset();
      return this.builder.module(module).append("setting").append(group.name).end(settingName);
   }

   @Override
   public String getSettingDesKey(Module module, SettingGroup group, Setting setting) {
      String settingName = setting.name;
      this.builder.reset();
      return this.builder.module(module).append("setting").append(group.name).append(settingName).end("description");
   }
}
