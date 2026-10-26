package cn.blockforge.meteorzhcn.util.trans_engine;

import cn.blockforge.meteorzhcn.util.TransUtil;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public abstract class AbstractTransEngine implements IKeyGenerate {
   public String transModuleName(Module module) {
      return TransUtil.trans(this.getModuleNameKey(module), module.name);
   }

   public String transModuleDescription(Module module) {
      return TransUtil.trans(this.getModuleDescriptionKey(module), module.description);
   }

   public String transSettingName(Module module, SettingGroup group, Setting setting) {
      return TransUtil.trans(this.getSettingNameKey(module, group, setting), setting.name);
   }

   public String transSettingDes(Module module, SettingGroup group, Setting setting) {
      return TransUtil.trans(this.getSettingDesKey(module, group, setting), setting.description);
   }
}
