package cn.blockforge.meteorzhcn.mixin;

import java.util.List;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
   value = {SettingGroup.class},
   remap = false
)
public interface SettingGroupAccessor {
   @Accessor("settings")
   List<Setting<?>> getSettings();
}
