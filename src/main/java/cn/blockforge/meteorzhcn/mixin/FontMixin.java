package cn.blockforge.meteorzhcn.mixin;

import cn.blockforge.meteorzhcn.font_fix.FontFix;
import cn.blockforge.meteorzhcn.font_fix.FontFixOwner;
import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.renderer.MeshBuilder;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.renderer.text.Font;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {Font.class},
   remap = false
)
public abstract class FontMixin implements FontFixOwner {
   @Shadow
   @Final
   public Texture texture;
   @Unique
   private FontFix meteorzhcn$fix;

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")}
   )
   private void meteorzhcn$onInit(ByteBuffer buffer, int height, CallbackInfo ci) {
      try {
         this.meteorzhcn$fix = new FontFix(buffer, height, this.texture);
      } catch (Throwable var5) {
         this.meteorzhcn$fix = null;
         FontFix.LOG.warn("动态字形图集初始化失败，中文将回退为 Meteor 原行为", var5);
      }
   }

   @Inject(
      method = {"getWidth"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void meteorzhcn$getWidth(String string, int length, CallbackInfoReturnable<Double> cir) {
      if (this.meteorzhcn$fix != null) {
         cir.setReturnValue(this.meteorzhcn$fix.getWidth(string, length));
      }
   }

   @Inject(
      method = {"getHeight"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void meteorzhcn$getHeight(CallbackInfoReturnable<Integer> cir) {
      if (this.meteorzhcn$fix != null) {
         cir.setReturnValue(this.meteorzhcn$fix.getHeight());
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void meteorzhcn$render(MeshBuilder mesh, String string, double x, double y, Color color, double scale, CallbackInfoReturnable<Double> cir) {
      if (this.meteorzhcn$fix != null) {
         cir.setReturnValue(this.meteorzhcn$fix.render(mesh, string, x, y, color, scale));
      }
   }

   @Override
   public void meteorzhcn$release() {
      if (this.meteorzhcn$fix != null) {
         this.meteorzhcn$fix.close();
         this.meteorzhcn$fix = null;
      }
   }
}
