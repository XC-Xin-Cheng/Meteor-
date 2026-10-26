package nekiplay.meteorplus.mixin.meteorclient.utils.misc;

import meteordevelopment.meteorclient.utils.misc.Keybind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {Keybind.class},
   remap = false
)
public class KeybindMixin {
   @Inject(
      method = {"canBindTo"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void canBind(boolean isKey, int value, int modifiers, CallbackInfoReturnable<Boolean> cir) {
      cir.setReturnValue(true);
   }
}
