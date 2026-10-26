package meteoraddonppp.mixin;

import meteoraddonppp.MeteorAddonPPP;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.utils.player.TitleScreenCredits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TitleScreenCredits.class})
public class TitleScreenCreditsMixin {
   @Inject(
      method = {"add(Lmeteordevelopment/meteorclient/addons/MeteorAddon;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void meteoraddonppp$hideBundledAddons(MeteorAddon var0, CallbackInfo var1) {
      if (MeteorAddonPPP.isBundledComponent(var0)) {
         var1.cancel();
      }
   }
}
