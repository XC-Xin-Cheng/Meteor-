package anticope.rejects.mixin.meteor.modules;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Flight.class})
public class FlightMixin {
   @Shadow(
      remap = false
   )
   @Final
   private SettingGroup sgGeneral;
   private Setting<Boolean> stopMomentum = null;

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")},
      remap = false
   )
   private void onInit(CallbackInfo ci) {
      this.stopMomentum = this.sgGeneral
         .add(((Builder)((Builder)((Builder)new Builder().name("stop-momentum")).description("Stops momentum on flight disable")).defaultValue(false)).build());
   }

   @Inject(
      method = {"onDeactivate"},
      at = {@At("TAIL")},
      remap = false
   )
   private void onDeactivate(CallbackInfo ci) {
      if (MeteorClient.mc.player != null && this.stopMomentum != null && (Boolean)this.stopMomentum.get()) {
         MeteorClient.mc.options.keyUp.setDown(false);
         MeteorClient.mc.options.keyLeft.setDown(false);
         MeteorClient.mc.options.keyDown.setDown(false);
         MeteorClient.mc.options.keyRight.setDown(false);
         MeteorClient.mc.player.setDeltaMovement(Vec3.ZERO);
      }
   }
}
