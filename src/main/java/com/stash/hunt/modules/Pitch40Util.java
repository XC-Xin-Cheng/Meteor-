package com.stash.hunt.modules;

import com.stash.hunt.Addon;
import com.stash.hunt.Utils;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;

public class Pitch40Util extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<Boolean> autoBoundAdjust = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("auto-adjust-bounds"))
                  .description("Adjusts your bounds to make you continue to gain height. Good for fixing falling on reconnect or lag, etc."))
               .defaultValue(true))
            .build()
      );
   public final Setting<Double> boundGap = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("bound-gap"))
               .description("The gap between the upper and lower bounds. Used when reconnecting, or when at max height if Auto Adjust Bounds is enabled."))
            .defaultValue(60.0)
            .sliderRange(50.0, 100.0)
            .build()
      );
   public final Setting<Boolean> autoFirework = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("auto-firework")).description("Uses a firework automatically if your velocity is too low."))
               .defaultValue(true))
            .build()
      );
   public final Setting<Double> velocityThreshold = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("velocity-threshold"))
                  .description("Velocity must be below this value when going up for firework to activate."))
               .defaultValue(-0.05)
               .sliderRange(-0.5, 1.0)
               .visible(this.autoFirework::get))
            .build()
      );
   public final Setting<Integer> fireworkCooldownTicks = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("cooldown-ticks"))
                     .description("Cooldown after using a firework in ticks."))
                  .defaultValue(10))
               .sliderRange(0, 100)
               .visible(this.autoFirework::get))
            .build()
      );
   Module elytraFly = Modules.get().get(ElytraFly.class);
   private ElytraFlightModes oldValue;
   private Setting<ElytraFlightModes> elytraFlyMode = this.elytraFly.settings.get("mode");
   int fireworkCooldown = 0;
   boolean goingUp = true;
   int elytraSwapSlot = -1;

   public Pitch40Util() {
      super(
         Addon.CATEGORY, "Pitch40Util", "Makes sure pitch 40 stays on when reconnecting to 2b2t, and sets your bounds as you reach highest point each climb."
      );
   }

   public void onActivate() {
      this.oldValue = (ElytraFlightModes)this.elytraFlyMode.get();
      this.elytraFlyMode.set(ElytraFlightModes.Pitch40);
   }

   public void onDeactivate() {
      if (this.elytraFly.isActive()) {
         this.elytraFly.toggle();
      }

      this.elytraFlyMode.set(this.oldValue);
   }

   private void resetBounds() {
      Setting<Double> upperBounds = this.elytraFly.settings.get("pitch40-upper-bounds");
      upperBounds.set(this.mc.player.getY() - 5.0);
      Setting<Double> lowerBounds = this.elytraFly.settings.get("pitch40-lower-bounds");
      lowerBounds.set(this.mc.player.getY() - 5.0 - (Double)this.boundGap.get());
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.elytraFly.isActive()) {
         if (this.fireworkCooldown > 0) {
            this.fireworkCooldown--;
         }

         if (this.elytraSwapSlot != -1) {
            InvUtils.swap(this.elytraSwapSlot, true);
            this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND);
            InvUtils.swapBack();
            this.elytraSwapSlot = -1;
         }

         if ((Boolean)this.autoBoundAdjust.get() && this.mc.player.getY() <= (Double)this.elytraFly.settings.get("pitch40-lower-bounds").get() - 10.0) {
            this.resetBounds();
            return;
         }

         if (this.mc.player.getXRot() == -40.0F) {
            this.goingUp = true;
            if ((Boolean)this.autoFirework.get()
               && this.mc.player.getDeltaMovement().y < (Double)this.velocityThreshold.get()
               && this.mc.player.getY() < (Double)this.elytraFly.settings.get("pitch40-upper-bounds").get()
               && this.fireworkCooldown == 0) {
               int launchStatus = Utils.firework(this.mc, false);
               if (launchStatus >= 0) {
                  this.fireworkCooldown = (Integer)this.fireworkCooldownTicks.get();
                  if (launchStatus != 200) {
                     this.elytraSwapSlot = launchStatus;
                  }
               }
            }
         } else if ((Boolean)this.autoBoundAdjust.get() && this.goingUp && this.mc.player.getDeltaMovement().y <= 0.0) {
            this.goingUp = false;
            this.resetBounds();
         }
      } else if (!this.mc.player.getAbilities().mayfly) {
         this.elytraFly.toggle();
         this.resetBounds();
      }
   }
}
