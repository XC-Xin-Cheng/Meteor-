package fr.crazycat256.satellite.mixin;

import fr.crazycat256.satellite.modules.EventlessFly;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.ICamera;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.world.HighwayBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({Camera.class})
public abstract class CameraMixin implements ICamera {
   @ModifyArgs(
      method = {"alignWithEntity"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Camera;setRotation(FF)V"
      )
   )
   private void onUpdateSetRotationArgs(Args args) {
      Freecam freecam = (Freecam)Modules.get().get(Freecam.class);
      FreeLook freeLook = (FreeLook)Modules.get().get(FreeLook.class);
      HighwayBuilder highwayBuilder = (HighwayBuilder)Modules.get().get(HighwayBuilder.class);
      EventlessFly eventlessFly = (EventlessFly)Modules.get().get(EventlessFly.class);
      if (eventlessFly.noRotate() && !freecam.isActive() && !highwayBuilder.isActive() && !freeLook.isActive()) {
         if (MeteorClient.mc.options.getCameraType() != CameraType.THIRD_PERSON_FRONT) {
            args.set(0, eventlessFly.cameraYaw);
         } else {
            args.set(0, eventlessFly.cameraYaw + 180.0F);
         }

         args.set(1, eventlessFly.cameraPitch);
      }
   }
}
