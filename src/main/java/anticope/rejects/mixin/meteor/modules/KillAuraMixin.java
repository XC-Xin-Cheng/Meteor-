package anticope.rejects.mixin.meteor.modules;

import anticope.rejects.modules.ShieldBypass;
import anticope.rejects.utils.RejectsUtils;
import java.util.Random;
import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(
   value = {KillAura.class},
   remap = false
)
public class KillAuraMixin extends Module {
   @Shadow
   @Final
   private SettingGroup sgGeneral;
   @Shadow
   @Final
   private SettingGroup sgTargeting;
   @Shadow
   @Final
   private Setting<Boolean> onlyOnLook;
   @Shadow
   private int hitTimer;
   @Shadow
   @Final
   private SettingGroup sgTiming;
   @Shadow
   @Final
   private Setting<Boolean> customDelay;
   private final Random random = new Random();
   private Setting<Double> fov;
   private Setting<Boolean> ignoreInvisible;
   private Setting<Boolean> randomTeleport;
   private Setting<Double> hitChance;
   private Setting<Integer> randomDelayMax;

   public KillAuraMixin(Category category, String name, String description) {
      super(category, name, description);
   }

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void onInit(CallbackInfo info) {
      this.fov = this.sgGeneral
         .add(((Builder)((Builder)new Builder().name("fov")).description("Will only aim entities in the fov.")).defaultValue(360.0).min(0.0).max(360.0).build());
      this.ignoreInvisible = this.sgTargeting
         .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("ignore-invisible"))
                     .description("Whether or not to attack invisible entities."))
                  .defaultValue(false))
               .build()
         );
      this.randomTeleport = this.sgGeneral
         .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                           .name("random-teleport"))
                        .description("Randomly teleport around the target."))
                     .defaultValue(false))
                  .visible(() -> !(Boolean)this.onlyOnLook.get()))
               .build()
         );
      this.hitChance = this.sgGeneral
         .add(
            ((Builder)((Builder)new Builder().name("hit-chance")).description("The probability of your hits landing."))
               .defaultValue(100.0)
               .range(1.0, 100.0)
               .sliderRange(1.0, 100.0)
               .build()
         );
      this.randomDelayMax = this.sgTiming
         .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("random-delay-max"))
                        .description("The maximum value for random delay."))
                     .defaultValue(4))
                  .min(0)
                  .sliderMax(20)
                  .visible(this.customDelay::get))
               .build()
         );
   }

   @Inject(
      method = {"entityCheck"},
      at = {@At(
         value = "RETURN",
         ordinal = 14
      )},
      cancellable = true
   )
   private void onReturn(Entity entity, CallbackInfoReturnable<Boolean> info) {
      if ((Boolean)this.ignoreInvisible.get() && entity.isInvisible()) {
         info.setReturnValue(false);
      }

      if (!RejectsUtils.inFov(entity, (Double)this.fov.get())) {
         info.setReturnValue(false);
      }

      info.setReturnValue(info.getReturnValueZ());
   }

   @Inject(
      method = {"attack"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onAttack(Entity entity, CallbackInfo info) {
      if ((Double)this.hitChance.get() < 100.0 && Math.random() > (Double)this.hitChance.get() / 100.0) {
         info.cancel();
      }
   }

   @Inject(
      method = {"onTick"},
      at = {@At("TAIL")},
      locals = LocalCapture.CAPTURE_FAILSOFT
   )
   private void onTick(Pre event, CallbackInfo ci, Entity primary) {
      if ((Boolean)this.randomTeleport.get() && !(Boolean)this.onlyOnLook.get()) {
         this.mc.player.setPos(primary.getX() + this.randomOffset(), primary.getY(), primary.getZ() + this.randomOffset());
      }
   }

   @Inject(
      method = {"attack"},
      at = {@At("TAIL")}
   )
   private void modifyHitDelay(CallbackInfo info) {
      if ((Integer)this.randomDelayMax.get() != 0) {
         this.hitTimer = this.hitTimer - this.random.nextInt((Integer)this.randomDelayMax.get());
      }
   }

   @Inject(
      method = {"attack"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"
      )},
      cancellable = true
   )
   private void onHit(Entity target, CallbackInfo info) {
      ShieldBypass shieldBypass = (ShieldBypass)Modules.get().get(ShieldBypass.class);
      if (shieldBypass.isActive()) {
         Cancellable dummyEvent = new Cancellable();
         shieldBypass.bypass(target, dummyEvent);
         if (dummyEvent.isCancelled()) {
            this.hitTimer = 0;
            info.cancel();
         }
      }
   }

   private double randomOffset() {
      return Math.random() * 4.0 - 2.0;
   }
}
