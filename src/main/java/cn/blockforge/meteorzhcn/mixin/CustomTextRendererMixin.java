package cn.blockforge.meteorzhcn.mixin;

import cn.blockforge.meteorzhcn.font_fix.FontFixOwner;
import meteordevelopment.meteorclient.renderer.text.CustomTextRenderer;
import meteordevelopment.meteorclient.renderer.text.Font;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {CustomTextRenderer.class},
   remap = false
)
public abstract class CustomTextRendererMixin {
   @Shadow
   @Final
   private Font[] fonts;

   @Inject(
      method = {"destroy"},
      at = {@At("TAIL")},
      require = 0
   )
   private void meteorzhcn$releaseFonts(CallbackInfo ci) {
      if (this.fonts != null) {
         for (Font font : this.fonts) {
            if (font instanceof FontFixOwner owner) {
               owner.meteorzhcn$release();
            }
         }
      }
   }
}
