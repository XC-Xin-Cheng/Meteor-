package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.OffGroundSpeedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class Jetpack extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> jetpackSpeed = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("jetpack-speed")).description("How fast while ascending.")).defaultValue(0.42).min(0.0).sliderMax(1.0).build()
      );

   public Jetpack() {
      super(MeteorRejectsAddon.CATEGORY, "jetpack", "Flies as if using a jetpack.");
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.options.keyJump.isDown()) {
         ((IVec3)this.mc.player.getDeltaMovement()).meteor$setY((Double)this.jetpackSpeed.get());
      }
   }

   @EventHandler
   private void onOffGroundSpeed(OffGroundSpeedEvent event) {
      event.speed = this.mc.player.getSpeed() * ((Double)this.jetpackSpeed.get()).floatValue();
   }
}
