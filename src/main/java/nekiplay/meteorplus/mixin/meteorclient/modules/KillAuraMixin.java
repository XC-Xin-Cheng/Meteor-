package nekiplay.meteorplus.mixin.meteorclient.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import nekiplay.meteorplus.features.modules.combat.AntiBotPlus;
import nekiplay.meteorplus.features.modules.combat.Teams;
import nekiplay.meteorplus.features.modules.combat.criticals.CriticalsPlus;
import nekiplay.meteorplus.features.modules.movement.elytrafly.ElytraFlyPlus;
import nekiplay.meteorplus.features.modules.movement.fly.FlyPlus;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {KillAura.class},
   remap = false,
   priority = 1001
)
public class KillAuraMixin extends Module {
   @Unique
   private final SettingGroup sgTimingPlus = this.settings.createGroup("Meteor+ Timing");
   @Final
   @Shadow
   private final SettingGroup sgTargeting = this.settings.getGroup("Targeting");
   @Final
   @Shadow
   private final SettingGroup sgTiming = this.settings.getGroup("Timing");
   @Final
   @Shadow
   private final Setting<Set<EntityType<?>>> entities = this.sgTargeting.get("entities");
   @Final
   @Shadow
   private final List<Entity> targets = new ArrayList<>();
   @Shadow
   @Final
   private final Setting<Boolean> customDelay = this.sgTiming.get("custom-delay");
   @Unique
   private final Setting<Boolean> smartDelayv2 = this.sgTimingPlus
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("smart-delay-v2")).description("Calculate sword damage to enemy.")).defaultValue(true))
               .visible(() -> !(Boolean)this.customDelay.get()))
            .build()
      );
   @Unique
   private final Setting<Integer> maxHurtTime = this.sgTimingPlus
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("max-enemy-hurt-time"))
                  .defaultValue(0))
               .min(0)
               .sliderMax(20)
               .visible(this.smartDelayv2::get))
            .build()
      );
   @Unique
   private final Setting<Boolean> onlyCrits = this.sgTimingPlus
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("only-crits")).description("Attack enemy only if this attack crit after jump.")).defaultValue(false))
            .build()
      );
   @Unique
   private final Setting<Boolean> onlyCritsIgnoreFlight = this.sgTimingPlus
      .add(((Builder)((Builder)((Builder)new Builder().name("ignore-only-crits-on-flight")).defaultValue(true)).visible(this.onlyCrits::get)).build());
   @Unique
   private final Setting<Boolean> ignoreOnlyCritsOnLevitation = this.sgTimingPlus
      .add(((Builder)((Builder)((Builder)new Builder().name("ignore-only-crits-on-levetation")).defaultValue(true)).visible(this.onlyCrits::get)).build());
   @Unique
   private final Setting<Boolean> ignoreSmartDelayForShulkerBulletAndGhastCharge = this.sgTimingPlus
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("ignore-delay-for-one-hit-entities"))
                     .description("Ignore attack delay for shulker bullet and fireball."))
                  .defaultValue(true))
               .visible(() -> ((Set)this.entities.get()).contains(EntityType.SHULKER_BULLET) || ((Set)this.entities.get()).contains(EntityType.FIREBALL)))
            .build()
      );
   @Unique
   private final Setting<Boolean> ignoreOnlyCritsForOneHitEntity = this.sgTimingPlus
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("ignore-only-crits-for-one-hit-entities"))
                     .description("Ignore only crits delay for shulker bullet and fireball."))
                  .defaultValue(true))
               .visible(() -> (Boolean)this.ignoreSmartDelayForShulkerBulletAndGhastCharge.get() && (Boolean)this.onlyCrits.get()))
            .build()
      );
   @Unique
   private final Setting<Boolean> customDelayOneHit = this.sgTimingPlus
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("custom-delay-for-one-hit-entities")).defaultValue(true))
               .visible(() -> ((Set)this.entities.get()).contains(EntityType.SHULKER_BULLET) || ((Set)this.entities.get()).contains(EntityType.FIREBALL)))
            .build()
      );
   @Unique
   private final Setting<Integer> hitDelay = this.sgTimingPlus
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("delay-for-one-hit-entities"))
                     .description("How fast you hit the entity in ticks."))
                  .defaultValue(2))
               .min(0)
               .sliderMax(60)
               .visible(this.customDelayOneHit::get))
            .build()
      );
   @Unique
   private int hitTimer;

   @Shadow
   public Entity getTarget() {
      return !this.targets.isEmpty() ? this.targets.get(0) : null;
   }

   public KillAuraMixin(Category category, String name, String description) {
      super(category, name, description);
   }

   @Inject(
      method = {"delayCheck"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void delayCheck(CallbackInfoReturnable<Boolean> cir) {
      Modules modules = Modules.get();
      if ((Boolean)this.onlyCrits.get() && !CriticalsPlus.allowCrit() && CriticalsPlus.needCrit(this.getTarget())) {
         if ((Boolean)this.ignoreOnlyCritsOnLevitation.get() && !Objects.requireNonNull(this.mc.player).hasEffect(MobEffects.LEVITATION)) {
            cir.setReturnValue(false);
            return;
         }

         if ((Boolean)this.onlyCritsIgnoreFlight.get()
            && !modules.isActive(Flight.class)
            && !modules.isActive(FlyPlus.class)
            && !modules.isActive(ElytraFly.class)
            && !modules.isActive(ElytraFlyPlus.class)) {
            cir.setReturnValue(false);
            return;
         }

         if (!(Boolean)this.ignoreOnlyCritsOnLevitation.get() && !(Boolean)this.onlyCritsIgnoreFlight.get()) {
            cir.setReturnValue(false);
            return;
         }
      }

      float delay = this.customDelayOneHit.get() ? ((Integer)this.hitDelay.get()).intValue() : 0.5F;
      if (this.oneHitEntity()) {
         if ((Boolean)this.customDelayOneHit.get()) {
            if (this.hitTimer < delay) {
               this.hitTimer++;
               cir.setReturnValue(false);
               return;
            }

            if ((Boolean)this.ignoreOnlyCritsForOneHitEntity.get()) {
               cir.setReturnValue(true);
               return;
            }

            if (this.oneHitEntity() && (Boolean)this.ignoreSmartDelayForShulkerBulletAndGhastCharge.get() && !(Boolean)this.onlyCrits.get()) {
               cir.setReturnValue(true);
               return;
            }
         } else {
            if ((Boolean)this.ignoreOnlyCritsForOneHitEntity.get()) {
               cir.setReturnValue(true);
               return;
            }

            if (this.oneHitEntity() && (Boolean)this.ignoreSmartDelayForShulkerBulletAndGhastCharge.get() && !(Boolean)this.onlyCrits.get()) {
               cir.setReturnValue(true);
               return;
            }
         }
      }

      if ((Boolean)this.smartDelayv2.get()
         && this.getTarget() instanceof LivingEntity livingEntity
         && DamageUtils.getAttackDamage(this.mc.player, livingEntity) >= livingEntity.getHealth() + 1.5
         && this.mc.player.getAttackStrengthScale(0.5F) >= 0.25
         && livingEntity.hurtTime <= (Integer)this.maxHurtTime.get()) {
         cir.setReturnValue(true);
      }
   }

   @Unique
   private boolean oneHitEntity() {
      return this.getTarget() == null
         ? false
         : (Boolean)this.ignoreSmartDelayForShulkerBulletAndGhastCharge.get()
            && (this.getTarget().getType() == EntityType.FIREBALL || this.getTarget().getType() == EntityType.SHULKER_BULLET);
   }

   @Inject(
      method = {"attack"},
      at = {@At("RETURN")}
   )
   private void onAttackEntity(Entity target, CallbackInfo ci) {
      this.hitTimer = 0;
   }

   @Inject(
      method = {"entityCheck"},
      at = {@At("RETURN")},
      cancellable = true
   )
   protected void entityCheck(Entity entity, CallbackInfoReturnable<Boolean> cir) {
      AntiBotPlus antiBotPlus = (AntiBotPlus)Modules.get().get(AntiBotPlus.class);
      Teams teams = (Teams)Modules.get().get(Teams.class);
      if (antiBotPlus != null && teams != null && entity instanceof Player && cir.getReturnValueZ()) {
         boolean ignore = !antiBotPlus.isBot(entity);
         if (ignore) {
            ignore = !teams.isInYourTeam(entity);
         }

         cir.setReturnValue(ignore);
      }
   }
}
