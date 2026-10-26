package zgoly.meteorist.modules;

import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura.EntityAge;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.utils.MeteoristUtils;

public class ZKillaura extends Module {
   private final SettingGroup sgFilter = this.settings.createGroup("Filter");
   private final SettingGroup sgAttack = this.settings.createGroup("Attack");
   private final SettingGroup sgShield = this.settings.createGroup("Shield");
   private final SettingGroup sgVisual = this.settings.createGroup("Visual");
   private final Setting<Set<EntityType<?>>> entities = this.sgFilter
      .add(
         ((Builder)((Builder)new Builder().name("entities")).description("Entities to attack."))
            .onlyAttackable()
            .defaultValue(new EntityType[]{EntityType.PLAYER})
            .build()
      );
   private final Setting<Double> range = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("range"))
               .description("The maximum range the entity can be to attack it."))
            .defaultValue(4.5)
            .min(0.0)
            .sliderMax(6.0)
            .build()
      );
   private final Setting<EntityAge> mobAgeFilter = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("mob-age-filter"))
                  .description("Determines the age of the mobs to target (baby, adult, or both)."))
               .defaultValue(EntityAge.Adult))
            .build()
      );
   private final Setting<Boolean> ignoreNamed = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-named"))
                  .description("Whether or not to attack mobs with a name."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> ignorePassive = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-passive"))
                  .description("Will only attack sometimes passive mobs if they are targeting you."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> ignoreTamed = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-tamed"))
                  .description("Will avoid attacking mobs you tamed."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> ignoreCreative = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-creative"))
                  .description("Will avoid attacking players in creative mode."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> ignoreFriends = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-friends"))
                  .description("Will avoid attacking players on your friends list."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> ignoreWalls = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-walls"))
                  .description("Whether or not to attack mobs through walls."))
               .defaultValue(false))
            .build()
      );
   private final Setting<ZKillaura.OnFallMode> onFallMode = this.sgAttack
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("on-fall-mode"))
                  .description("Chooses an attack strategy when falling to maximize critical damage."))
               .defaultValue(ZKillaura.OnFallMode.Value))
            .build()
      );
   private final Setting<Double> onFallValue = this.sgAttack
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("on-fall-value"))
                  .description("Defines a specific value for attacking while falling."))
               .min(0.0)
               .defaultValue(0.25)
               .sliderMax(1.0)
               .visible(() -> this.onFallMode.get() == ZKillaura.OnFallMode.Value))
            .build()
      );
   private final Setting<Double> onFallMinRandomValue = this.sgAttack
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("on-fall-min-random-value"))
                  .description("Specifies the minimum randomized value for attacking while falling."))
               .min(0.0)
               .defaultValue(0.2)
               .sliderMax(1.0)
               .visible(() -> this.onFallMode.get() == ZKillaura.OnFallMode.RandomValue))
            .build()
      );
   private final Setting<Double> onFallMaxRandomValue = this.sgAttack
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("on-fall-max-random-value"))
                  .description("Specifies the maximum randomized value for attacking while falling."))
               .min(0.0)
               .defaultValue(0.4)
               .sliderMax(1.0)
               .visible(() -> this.onFallMode.get() == ZKillaura.OnFallMode.RandomValue))
            .build()
      );
   private final Setting<ZKillaura.HitSpeedMode> hitSpeedMode = this.sgAttack
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("hit-speed-mode"))
                  .description("Selects a hit speed mode for attacking."))
               .defaultValue(ZKillaura.HitSpeedMode.Value))
            .build()
      );
   private final Setting<Double> hitSpeedValue = this.sgAttack
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("hit-speed-value"))
                  .description("Defines a specific hit speed value for attacking."))
               .defaultValue(0.0)
               .sliderRange(-10.0, 10.0)
               .visible(() -> this.hitSpeedMode.get() == ZKillaura.HitSpeedMode.Value))
            .build()
      );
   private final Setting<Double> hitSpeedMinRandomValue = this.sgAttack
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("hit-speed-min-random-value"))
                  .description("Specifies the minimum randomized hit speed value."))
               .defaultValue(0.0)
               .sliderRange(-10.0, 10.0)
               .visible(() -> this.hitSpeedMode.get() == ZKillaura.HitSpeedMode.RandomValue))
            .build()
      );
   private final Setting<Double> hitSpeedMaxRandomValue = this.sgAttack
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("hit-speed-max-random-value"))
                  .description("Specifies the maximum randomized hit speed value."))
               .defaultValue(0.0)
               .sliderRange(-10.0, 10.0)
               .visible(() -> this.hitSpeedMode.get() == ZKillaura.HitSpeedMode.RandomValue))
            .build()
      );
   private final Setting<ZKillaura.ShieldMode> shieldMode = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("shield-mode"))
                  .description("Will try and use an axe to break target shields."))
               .defaultValue(ZKillaura.ShieldMode.Break))
            .build()
      );
   private final Setting<ZKillaura.DelayMode> swapDelayMode = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("swap-delay-mode"))
                     .description("Selects a delay before swapping."))
                  .defaultValue(ZKillaura.DelayMode.Value))
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break))
            .build()
      );
   private final Setting<Integer> swapDelayValue = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("swap-delay-value"))
                     .description("Defines a specific delay (in ticks) before swapping."))
                  .defaultValue(2))
               .sliderRange(0, 20)
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break && this.swapDelayMode.get() == ZKillaura.DelayMode.Value))
            .build()
      );
   private final Setting<Integer> swapDelayMinRandomValue = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("swap-delay-min-random-value"))
                     .description("Specifies the minimum randomized delay (in ticks) before swapping."))
                  .defaultValue(1))
               .sliderRange(0, 20)
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break && this.swapDelayMode.get() == ZKillaura.DelayMode.RandomValue))
            .build()
      );
   private final Setting<Integer> swapDelayMaxRandomValue = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("swap-delay-max-random-value"))
                     .description("Specifies the maximum randomized delay (in ticks) before swapping."))
                  .defaultValue(5))
               .sliderRange(0, 20)
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break && this.swapDelayMode.get() == ZKillaura.DelayMode.RandomValue))
            .build()
      );
   private final Setting<ZKillaura.DelayMode> hitDelayMode = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("hit-delay-mode"))
                     .description("Selects a delay before hitting."))
                  .defaultValue(ZKillaura.DelayMode.Value))
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break))
            .build()
      );
   private final Setting<Integer> hitDelayValue = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("hit-delay-value"))
                     .description("Defines a specific delay (in ticks) before hitting."))
                  .defaultValue(2))
               .sliderRange(0, 20)
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break && this.hitDelayMode.get() == ZKillaura.DelayMode.Value))
            .build()
      );
   private final Setting<Integer> hitDelayMinRandomValue = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("hit-delay-min-random-value"))
                     .description("Specifies the minimum randomized delay (in ticks) before hitting."))
                  .defaultValue(1))
               .sliderRange(0, 20)
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break && this.hitDelayMode.get() == ZKillaura.DelayMode.RandomValue))
            .build()
      );
   private final Setting<Integer> hitDelayMaxRandomValue = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("hit-delay-max-random-value"))
                     .description("Specifies the maximum randomized delay (in ticks) before hitting."))
                  .defaultValue(5))
               .sliderRange(0, 20)
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break && this.hitDelayMode.get() == ZKillaura.DelayMode.RandomValue))
            .build()
      );
   private final Setting<ZKillaura.DelayMode> swapBackDelayMode = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("swap-back-delay-mode"))
                     .description("Selects a delay before swapping back."))
                  .defaultValue(ZKillaura.DelayMode.Value))
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break))
            .build()
      );
   private final Setting<Integer> swapBackDelayValue = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("swap-back-delay-value"))
                     .description("Defines a specific delay (in ticks) before swapping back."))
                  .defaultValue(2))
               .sliderRange(0, 20)
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break && this.swapBackDelayMode.get() == ZKillaura.DelayMode.Value))
            .build()
      );
   private final Setting<Integer> swapBackDelayMinRandomValue = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("swap-back-delay-min-random-value"))
                     .description("Specifies the minimum randomized delay (in ticks) before swapping back."))
                  .defaultValue(1))
               .sliderRange(0, 20)
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break && this.swapBackDelayMode.get() == ZKillaura.DelayMode.RandomValue))
            .build()
      );
   private final Setting<Integer> swapBackDelayMaxRandomValue = this.sgShield
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("swap-back-delay-max-random-value"))
                     .description("Specifies the maximum randomized delay (in ticks) before swapping back."))
                  .defaultValue(5))
               .sliderRange(0, 20)
               .visible(() -> this.shieldMode.get() == ZKillaura.ShieldMode.Break && this.swapBackDelayMode.get() == ZKillaura.DelayMode.RandomValue))
            .build()
      );
   private final Setting<Boolean> swingHand = this.sgVisual
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("swing-hand"))
                  .description("Makes hand swing visible client-side."))
               .defaultValue(true))
            .build()
      );
   float randomOnFallFloat = 0.0F;
   float randomHitSpeedFloat = 0.0F;
   private ZKillaura.ShieldState shieldState = ZKillaura.ShieldState.Idle;
   private int swapTimer = 0;
   private int hitTimer = 0;
   private int swapBackTimer = 0;
   private int originalSlot = -1;

   public void onActivate() {
      this.randomOnFallFloat = 0.0F;
      this.randomHitSpeedFloat = 0.0F;
      this.shieldState = ZKillaura.ShieldState.Idle;
      this.swapTimer = this.hitTimer = this.swapBackTimer = 0;
      this.originalSlot = -1;
   }

   public ZKillaura() {
      super(Meteorist.CATEGORY, "z-kill-aura", "Killaura which only attacks target if you aim at it.");
   }

   @EventHandler
   private void onTick(Pre event) {
      if (!this.mc.player.isDeadOrDying() && this.mc.level != null) {
         HitResult hitResult = MeteoristUtils.getCrosshairTarget(this.mc.player, (Double)this.range.get(), (Boolean)this.ignoreWalls.get(), this::entityCheck);
         if (hitResult != null && hitResult.getType() == Type.ENTITY) {
            Entity entity = ((EntityHitResult)hitResult).getEntity();
            LivingEntity livingEntity = entity instanceof LivingEntity ? (LivingEntity)entity : null;
            if (this.shieldMode.get() == ZKillaura.ShieldMode.Break && livingEntity != null) {
               if (this.shieldState == ZKillaura.ShieldState.Idle && livingEntity.isBlocking()) {
                  FindItemResult axe = InvUtils.findInHotbar(stack -> stack.getItem() instanceof AxeItem);
                  if (axe.found()) {
                     this.shieldState = ZKillaura.ShieldState.SwapAxe;
                     this.swapTimer = this.calculateDelay(
                        (ZKillaura.DelayMode)this.swapDelayMode.get(),
                        (Integer)this.swapDelayValue.get(),
                        (Integer)this.swapDelayMinRandomValue.get(),
                        (Integer)this.swapDelayMaxRandomValue.get()
                     );
                  }
               }

               if (this.shieldState == ZKillaura.ShieldState.SwapAxe && this.swapTimer-- <= 0) {
                  FindItemResult axe = InvUtils.findInHotbar(stack -> stack.getItem() instanceof AxeItem);
                  if (axe.found() && livingEntity.isBlocking()) {
                     this.originalSlot = this.mc.player.getInventory().getSelectedSlot();
                     InvUtils.swap(axe.slot(), false);
                     this.shieldState = ZKillaura.ShieldState.Attack;
                     this.hitTimer = this.calculateDelay(
                        (ZKillaura.DelayMode)this.hitDelayMode.get(),
                        (Integer)this.hitDelayValue.get(),
                        (Integer)this.hitDelayMinRandomValue.get(),
                        (Integer)this.hitDelayMaxRandomValue.get()
                     );
                  } else {
                     this.resetShield();
                  }
               }

               if (this.shieldState == ZKillaura.ShieldState.Attack && this.hitTimer-- <= 0) {
                  if (livingEntity.isBlocking()) {
                     this.mc.gameMode.attack(this.mc.player, livingEntity);
                     if ((Boolean)this.swingHand.get()) {
                        this.mc.player.swing(InteractionHand.MAIN_HAND);
                     }

                     this.shieldState = ZKillaura.ShieldState.SwapBack;
                     this.swapBackTimer = this.calculateDelay(
                        (ZKillaura.DelayMode)this.swapBackDelayMode.get(),
                        (Integer)this.swapBackDelayValue.get(),
                        (Integer)this.swapBackDelayMinRandomValue.get(),
                        (Integer)this.swapBackDelayMaxRandomValue.get()
                     );
                  } else {
                     InvUtils.swap(this.originalSlot, false);
                     this.resetShield();
                  }
               }

               if (this.shieldState == ZKillaura.ShieldState.SwapBack && this.swapBackTimer-- <= 0) {
                  if (this.originalSlot != this.mc.player.getInventory().getSelectedSlot()) {
                     InvUtils.swap(this.originalSlot, false);
                  }

                  this.resetShield();
               }

               if (this.shieldState == ZKillaura.ShieldState.Attack || this.shieldState == ZKillaura.ShieldState.SwapBack) {
                  return;
               }
            }

            ZKillaura.OnFallMode currOnFallMode = (ZKillaura.OnFallMode)this.onFallMode.get();
            if (currOnFallMode != ZKillaura.OnFallMode.None) {
               float onFall = currOnFallMode == ZKillaura.OnFallMode.Value ? ((Double)this.onFallValue.get()).floatValue() : this.randomOnFallFloat;
               if (!(this.mc.player.fallDistance > onFall)) {
                  return;
               }
            }

            ZKillaura.HitSpeedMode currHitSpeedMode = (ZKillaura.HitSpeedMode)this.hitSpeedMode.get();
            float hitSpeed = currHitSpeedMode == ZKillaura.HitSpeedMode.Value ? ((Double)this.hitSpeedValue.get()).floatValue() : this.randomHitSpeedFloat;
            if (currHitSpeedMode == ZKillaura.HitSpeedMode.None || !(this.mc.player.getAttackStrengthScale(hitSpeed) * 17.0F < 16.0F)) {
               this.mc.gameMode.attack(this.mc.player, entity);
               if ((Boolean)this.swingHand.get()) {
                  this.mc.player.swing(InteractionHand.MAIN_HAND);
               }

               if (currOnFallMode == ZKillaura.OnFallMode.RandomValue) {
                  float min = Math.min(((Double)this.onFallMinRandomValue.get()).floatValue(), ((Double)this.onFallMaxRandomValue.get()).floatValue());
                  float max = Math.max(((Double)this.onFallMinRandomValue.get()).floatValue(), ((Double)this.onFallMaxRandomValue.get()).floatValue());
                  this.randomOnFallFloat = min + this.mc.level.getRandom().nextFloat() * (max - min);
               }

               if (currHitSpeedMode == ZKillaura.HitSpeedMode.RandomValue) {
                  float min = Math.min(((Double)this.hitSpeedMinRandomValue.get()).floatValue(), ((Double)this.hitSpeedMaxRandomValue.get()).floatValue());
                  float max = Math.max(((Double)this.hitSpeedMinRandomValue.get()).floatValue(), ((Double)this.hitSpeedMaxRandomValue.get()).floatValue());
                  this.randomHitSpeedFloat = min + this.mc.level.getRandom().nextFloat() * (max - min);
               }
            }
         }
      }
   }

   private void resetShield() {
      this.shieldState = ZKillaura.ShieldState.Idle;
      this.swapTimer = this.hitTimer = this.swapBackTimer = 0;
      this.originalSlot = -1;
   }

   private int calculateDelay(ZKillaura.DelayMode mode, int value, int minRandom, int maxRandom) {
      return switch (mode) {
         case Value -> value;
         case RandomValue -> minRandom + this.mc.level.getRandom().nextInt(maxRandom - minRandom + 1);
      };
   }

   private boolean entityCheck(Entity entity) {
      if (entity == this.mc.player || entity == this.mc.getCameraEntity()) {
         return false;
      } else if (!entity.isAlive()) {
         return false;
      } else if (entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) {
         return false;
      } else if (!((Set)this.entities.get()).contains(entity.getType())) {
         return false;
      } else if ((Boolean)this.ignoreNamed.get() && entity.hasCustomName()) {
         return false;
      } else if ((Boolean)this.ignoreTamed.get() && entity instanceof OwnableEntity ownable && Objects.equals(ownable.getOwner(), this.mc.player)) {
         return false;
      } else if ((Boolean)this.ignorePassive.get() && this.isPassive(entity)) {
         return false;
      } else {
         if (entity instanceof Player player) {
            if ((Boolean)this.ignoreCreative.get() && player.isCreative()) {
               return false;
            }

            if ((Boolean)this.ignoreFriends.get() && !Friends.get().shouldAttack(player)) {
               return false;
            }

            if (this.shieldMode.get() == ZKillaura.ShieldMode.Ignore && player.isBlocking()) {
               return false;
            }
         }

         if (entity instanceof Animal animal) {
            return switch ((EntityAge)this.mobAgeFilter.get()) {
               case Baby -> animal.isBaby();
               case Adult -> !animal.isBaby();
               case Both -> true;
               default -> throw new MatchException(null, null);
            };
         } else {
            return true;
         }
      }
   }

   private boolean isPassive(Entity entity) {
      return switch (entity) {
         case EnderMan enderman -> !enderman.isCreepy();
         case ZombifiedPiglin piglin -> !piglin.isAggressive();
         case Wolf wolf -> !wolf.isAggressive();
         default -> false;
      };
   }

   public static enum DelayMode {
      Value,
      RandomValue;
   }

   public static enum HitSpeedMode {
      None,
      Value,
      RandomValue;
   }

   public static enum OnFallMode {
      None,
      Value,
      RandomValue;
   }

   public static enum ShieldMode {
      Ignore,
      Break,
      None;
   }

   private static enum ShieldState {
      Idle,
      SwapAxe,
      Attack,
      SwapBack;
   }
}
