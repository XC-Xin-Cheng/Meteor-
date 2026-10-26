package meteoraddonppp.mixin;

import meteoraddonppp.MeteorAddonPPP;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Module.class})
public abstract class ModuleCategoryMixin {
   @Shadow
   @Final
   @Mutable
   public Category category;

   @Inject(
      method = {"<init>(Lmeteordevelopment/meteorclient/systems/modules/Category;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V"},
      at = {@At("RETURN")}
   )
   private void meteoraddonppp$moveToMainCategory(Category var1, String var2, String var3, String[] var4, CallbackInfo var5) {
      Module var6 = (Module)this;
      if (MeteorAddonPPP.movesToMainCategory(var6.getClass().getName())) {
         this.category = MeteorAddonPPP.CATEGORY;
      }
   }
}
