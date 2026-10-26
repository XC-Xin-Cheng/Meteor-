package fr.crazycat256.satellite.mixin;

import fr.crazycat256.satellite.modules.EventlessFly;
import fr.crazycat256.satellite.modules.VecFly;
import fr.crazycat256.satellite.utils.MathUtils;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public abstract class EntityMixin {
   @Inject(
      method = {"isSprinting"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onIsSprinting(CallbackInfoReturnable<Boolean> info) {
      if (((VecFly)Modules.get().get(VecFly.class)).isActive()) {
         info.setReturnValue(false);
      }
   }

   @Inject(
      method = {"turn"},
      at = {@At("HEAD")}
   )
   private void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
      EventlessFly eventlessFly = (EventlessFly)Modules.get().get(EventlessFly.class);
      if (eventlessFly.noRotate()) {
         eventlessFly.cameraYaw += (float)(cursorDeltaX / 8.0);
         eventlessFly.cameraYaw = (float)MathUtils.mod(eventlessFly.cameraYaw + 180.0F, 360.0) - 180.0F;
         eventlessFly.cameraPitch += (float)(cursorDeltaY / 8.0);
         eventlessFly.cameraPitch = Mth.clamp(eventlessFly.cameraPitch, -90.0F, 90.0F);
      }
   }
}
