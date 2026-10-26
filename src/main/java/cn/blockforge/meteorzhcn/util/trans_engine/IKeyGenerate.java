package cn.blockforge.meteorzhcn.util.trans_engine;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public interface IKeyGenerate {
   String getModuleNameKey(Module var1);

   String getModuleDescriptionKey(Module var1);

   String getSettingNameKey(Module var1, SettingGroup var2, Setting<?> var3);

   String getSettingDesKey(Module var1, SettingGroup var2, Setting<?> var3);
}
