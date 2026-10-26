package cn.blockforge.meteorzhcn.mixin;

import meteordevelopment.meteorclient.systems.modules.Module;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
   value = {Module.class},
   remap = false
)
public interface ModuleAccessor {
   @Mutable
   @Accessor("title")
   void setTitle(String var1);

   @Mutable
   @Accessor("description")
   void setDescription(String var1);
}
