package nekiplay.meteorplus.mixin.whereisit;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import nekiplay.meteorplus.features.modules.integrations.WhereIsIt;
import nekiplay.meteorplus.utils.ColorRemover;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import red.jackf.whereisit.client.render.Rendering;

@Mixin({Rendering.class})
public class RenderingMixin {
   @Unique
   private static WhereIsIt whereIsIt;

   @ModifyArgs(
      method = {"renderLabel"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4fc;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V"
      )
   )
   private static void changeColor(Args args) {
      if (whereIsIt == null) {
         whereIsIt = (WhereIsIt)Modules.get().get(WhereIsIt.class);
      }

      if (whereIsIt != null && whereIsIt.isActive()) {
         Component text1 = (Component)args.get(0);
         String text2 = text1.getString();
         if ((Boolean)whereIsIt.support_color_symbols.get()) {
            String text3 = ColorRemover.GetVerbatim(text2);
            args.set(0, Component.nullToEmpty(text3));
            int width = MeteorClient.mc.font.width(text3);
            float x = -width / 2.0F;
            args.set(1, x);
            args.set(3, getColor(text2));
         } else {
            args.set(3, ((SettingColor)whereIsIt.text_color.get()).getPacked());
         }
      }
   }

   @ModifyArgs(
      method = {"renderLabel"},
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V"
      )
   )
   private static void translatePosition(Args args) {
      if (whereIsIt == null) {
         whereIsIt = (WhereIsIt)Modules.get().get(WhereIsIt.class);
      }

      if (whereIsIt != null && whereIsIt.isActive()) {
         args.set(1, (Double)args.get(1) + (Double)whereIsIt.y_offset.get());
      }
   }

   @ModifyArgs(
      method = {"renderLabel"},
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(Lorg/joml/Matrix4fc;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
      )
   )
   private static void backgroundModifier(Args args) {
      if (whereIsIt == null) {
         whereIsIt = (WhereIsIt)Modules.get().get(WhereIsIt.class);
      }

      if (whereIsIt != null && whereIsIt.isActive() && !(Boolean)whereIsIt.background.get()) {
         args.set(1, 0.0F);
         args.set(2, 0.0F);
      }
   }

   @Unique
   private static int getColor(String text) {
      if (text.length() >= 2) {
         char first_char = text.charAt(0);
         char color_char = text.charAt(1);
         if (first_char == '&' || first_char == 167) {
            switch (color_char) {
               case '0':
                  return 0;
               case '1':
                  return 170;
               case '2':
                  return 43520;
               case '3':
                  return 43690;
               case '4':
                  return 11141120;
               case '5':
                  return 11141290;
               case '6':
                  return 16755200;
               case '7':
                  return 11184810;
               case '8':
                  return 5592405;
               case '9':
                  return 5592575;
               case ':':
               case ';':
               case '<':
               case '=':
               case '>':
               case '?':
               case '@':
               case 'A':
               case 'B':
               case 'C':
               case 'D':
               case 'E':
               case 'F':
               case 'G':
               case 'H':
               case 'I':
               case 'J':
               case 'K':
               case 'L':
               case 'M':
               case 'N':
               case 'O':
               case 'P':
               case 'Q':
               case 'R':
               case 'S':
               case 'T':
               case 'U':
               case 'V':
               case 'W':
               case 'X':
               case 'Y':
               case 'Z':
               case '[':
               case '\\':
               case ']':
               case '^':
               case '_':
               case '`':
               default:
                  return 16777215;
               case 'a':
                  return 5635925;
               case 'b':
                  return 5636095;
               case 'c':
                  return 16733525;
               case 'd':
                  return 16733695;
               case 'e':
                  return 16777045;
               case 'f':
                  return 16777215;
            }
         }
      }

      return 16777215;
   }
}
