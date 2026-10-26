package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.phys.Vec3;

public class Boost extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> strength = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("strength")).description("Strength to yeet you with.")).defaultValue(4.0).sliderMax(10.0).build());
   private final Setting<Boolean> autoBoost = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("auto-boost"))
                  .description("Automatically boosts you."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Integer> interval = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("interval"))
                     .description("Boost interval in ticks."))
                  .visible(this.autoBoost::get))
               .defaultValue(20))
            .sliderMax(120)
            .build()
      );
   private int timer = 0;

   public Boost() {
      super(MeteorRejectsAddon.CATEGORY, "boost", "Works like a dash move.");
   }

   public void onActivate() {
      this.timer = (Integer)this.interval.get();
      if (!(Boolean)this.autoBoost.get()) {
         if (this.mc.player != null) {
            this.boost();
         }

         this.toggle();
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      if ((Boolean)this.autoBoost.get()) {
         if (this.timer < 1) {
            this.boost();
            this.timer = (Integer)this.interval.get();
         } else {
            this.timer--;
         }
      }
   }

   private void boost() {
      Vec3 v = this.mc.player.getForward().scale((Double)this.strength.get());
      this.mc.player.push(v.x(), v.y(), v.z());
   }
}
