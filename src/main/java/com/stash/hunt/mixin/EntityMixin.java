package com.stash.hunt.mixin;

import com.stash.hunt.modules.ElytraFlyPlusPlus;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public class EntityMixin {
   @Shadow
   protected UUID uuid;
   @Unique
   ElytraFlyPlusPlus efly = (ElytraFlyPlusPlus)Modules.get().get(ElytraFlyPlusPlus.class);

   @Inject(
      at = {@At("HEAD")},
      method = {"getPose()Lnet/minecraft/world/entity/Pose;"},
      cancellable = true
   )
   private void getPose(CallbackInfoReturnable<Pose> cir) {
      if (this.efly != null && this.efly.enabled() && this.uuid == MeteorClient.mc.player.getUUID()) {
         cir.setReturnValue(Pose.STANDING);
      }
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"isSprinting()Z"},
      cancellable = true
   )
   private void isSprinting(CallbackInfoReturnable<Boolean> cir) {
      if (this.efly != null && this.efly.enabled() && this.uuid == MeteorClient.mc.player.getUUID()) {
         cir.setReturnValue(true);
      }
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"push(Lnet/minecraft/world/entity/Entity;)V"},
      cancellable = true
   )
   private void pushAwayFrom(Entity entity, CallbackInfo ci) {
      if (MeteorClient.mc.player != null
         && this.uuid == MeteorClient.mc.player.getUUID()
         && this.efly != null
         && this.efly.enabled()
         && !entity.getUUID().equals(this.uuid)) {
         ci.cancel();
      }
   }
}
