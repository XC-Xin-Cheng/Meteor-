package com.stash.hunt.mixin;

import com.stash.hunt.modules.ElytraFlyPlusPlus;
import com.stash.hunt.modules.NoJumpDelay;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin extends Entity {
   @Shadow
   private int noJumpDelay;
   @Unique
   NoJumpDelay noJumpDelay_0 = (NoJumpDelay)Modules.get().get(NoJumpDelay.class);
   @Unique
   ElytraFlyPlusPlus efly = (ElytraFlyPlusPlus)Modules.get().get(ElytraFlyPlusPlus.class);

   public LivingEntityMixin(EntityType<?> type, Level world) {
      super(type, world);
   }

   @Shadow
   public abstract Brain<?> getBrain();

   @Inject(
      at = {@At("HEAD")},
      method = {"aiStep()V"}
   )
   private void tickMovement(CallbackInfo ci) {
      if (MeteorClient.mc.player != null && MeteorClient.mc.player.getBrain().equals(this.getBrain()) && this.efly != null && this.efly.enabled()
         || this.noJumpDelay_0 != null && this.noJumpDelay_0.isActive()) {
         this.noJumpDelay = 0;
      }
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"isFallFlying"},
      cancellable = true
   )
   private void isGliding(CallbackInfoReturnable<Boolean> cir) {
      if (MeteorClient.mc.player != null && MeteorClient.mc.player.getBrain().equals(this.getBrain()) && this.efly != null && this.efly.enabled()) {
         cir.setReturnValue(true);
      }
   }
}
