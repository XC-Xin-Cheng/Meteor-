package cn.blockforge.meteorzhcn.util.trans_engine;

import cn.blockforge.meteorzhcn.util.KeyBuilder;
import cn.blockforge.meteorzhcn.util.TransUtil;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class TransEngineLegacy extends AbstractTransEngine {
   KeyBuilder builder = new KeyBuilder();

   @Override
   public String getModuleNameKey(Module module) {
      this.builder.reset();
      this.builder.append(TransUtil.baseFormat(module.category.name));
      return this.builder.end(TransUtil.baseFormat(module.name));
   }

   @Override
   public String getModuleDescriptionKey(Module module) {
      this.builder.reset();
      this.builder.append(TransUtil.baseFormat(module.category.name)).append(TransUtil.baseFormat(module.name));
      return this.builder.end("description");
   }

   @Override
   public String getSettingNameKey(Module module, SettingGroup group, Setting<?> setting) {
      this.builder.reset();
      this.builder.append(TransUtil.baseFormat(module.category.name)).append(TransUtil.baseFormat(module.name));
      return this.builder.append("setting").append(group.name).end(setting.name);
   }

   @Override
   public String getSettingDesKey(Module module, SettingGroup group, Setting<?> setting) {
      this.builder.reset();
      this.builder.append(TransUtil.baseFormat(module.category.name)).append(TransUtil.baseFormat(module.name));
      return this.builder.append("setting").append(group.name).append(setting.name).end("description");
   }
}
