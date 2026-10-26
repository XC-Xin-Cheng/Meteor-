package cn.blockforge.meteorzhcn.mixin;

import meteordevelopment.meteorclient.settings.Setting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
   value = {Setting.class},
   remap = false
)
public interface SettingAccessor {
   @Mutable
   @Accessor("title")
   void setTitle(String var1);

   @Mutable
   @Accessor("description")
   void setDescription(String var1);
}
