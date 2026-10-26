package nekiplay.meteorplus.features.modules.world.timer;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.features.modules.world.timer.modes.NCP;
import nekiplay.meteorplus.features.modules.world.timer.modes.NCPv2;
import nekiplay.meteorplus.features.modules.world.timer.modes.Vulcan;

public class TimerPlus extends Module {
   public static int workingDelay = 27;
   public static int workingTimer = 0;
   public static int rechargeTimer = 0;
   public static int rechargeDelay = 352;
   public static double timerMultiplier = 2.0;
   public static double timerMultiplierInAir = 1.5;
   public static double timerMultiplierOnRecharge = 1.0;
   private final SettingGroup settingsGroup = this.settings.getDefaultGroup();
   public final Setting<Boolean> onlyInMove = this.settingsGroup
      .add(((Builder)((Builder)((Builder)new Builder().name("work-only-in-move")).description("Prevent false un charge.")).defaultValue(true)).build());
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<TimerModes> mode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                           .name("mode"))
                        .description("Timer mode."))
                     .defaultValue(TimerModes.NCP))
                  .onModuleActivated(timerModesSetting -> this.onTimerModeChanged((TimerModes)timerModesSetting.get())))
               .onChanged(this::onTimerModeChanged))
            .build()
      );
   private final Setting<Boolean> rechargeOnDisable = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("recharge-on-disable")).description("Recharge timer delay on disable.")).defaultValue(false)).build()
      );
   private final Setting<Integer> rechargeDelaySetting = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("recharge-delay"))
                        .description("Recharge timer delay."))
                     .defaultValue(352))
                  .visible(() -> this.mode.get() == TimerModes.Custom || this.mode.get() == TimerModes.Custom_v2))
               .onChanged(a -> {
                  rechargeDelay = a;
                  rechargeTimer = 0;
               }))
            .build()
      );
   private final Setting<Integer> boostDelaySetting = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("boost-delay"))
                        .description("Working timer delay."))
                     .defaultValue(27))
                  .visible(() -> this.mode.get() == TimerModes.Custom || this.mode.get() == TimerModes.Custom_v2))
               .onChanged(a -> {
                  workingDelay = a;
                  workingTimer = 0;
               }))
            .build()
      );
   private final Setting<Double> boostMultiplierInAir = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                        .name("multiplier-in-air"))
                     .description("Timer multiplier in air."))
                  .defaultValue(2.0)
                  .visible(() -> this.mode.get() == TimerModes.Custom_v2))
               .onChanged(a -> timerMultiplierInAir = a))
            .build()
      );
   private final Setting<Double> boostMultiplier = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                        .name("multiplier"))
                     .description("Timer multiplier."))
                  .defaultValue(2.0)
                  .visible(() -> this.mode.get() == TimerModes.Custom || this.mode.get() == TimerModes.Custom_v2))
               .onChanged(a -> timerMultiplier = a))
            .build()
      );
   private final Setting<Double> boostMultiplierOnRecharge = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                        .name("multiplier-on-recharge"))
                     .description("Timer multiplier on recharge."))
                  .defaultValue(1.0)
                  .visible(() -> this.mode.get() == TimerModes.Custom || this.mode.get() == TimerModes.Custom_v2))
               .onChanged(a -> timerMultiplierOnRecharge = a))
            .build()
      );
   private TimerMode currentMode;

   public TimerPlus() {
      super(Categories.World, "timer+", "Bypass timer.");
      this.autoSubscribe = false;
      MeteorClient.EVENT_BUS.subscribe(this);
   }

   private void onTimerModeChanged(TimerModes mode) {
      switch (mode) {
         case NCP:
            this.currentMode = new NCP();
            workingDelay = 27;
            rechargeDelay = 352;
            timerMultiplier = 2.0;
            timerMultiplierOnRecharge = 1.0;
            break;
         case Intave:
            this.currentMode = new NCP();
            workingDelay = 30;
            rechargeDelay = 105;
            timerMultiplier = 1.25;
            timerMultiplierOnRecharge = 1.0;
            break;
         case Vulcan:
            this.currentMode = new Vulcan();
            break;
         case Grim:
            this.currentMode = new NCP();
            workingDelay = 14;
            rechargeDelay = 16;
            timerMultiplier = 2.0;
            timerMultiplierOnRecharge = 1.0;
            break;
         case OldFag:
            this.currentMode = new NCPv2();
            workingDelay = 16;
            rechargeDelay = 155;
            timerMultiplier = 2.0;
            timerMultiplierInAir = 1.7;
            timerMultiplierOnRecharge = 1.0;
            break;
         case Custom:
            this.currentMode = new NCP();
            workingDelay = (Integer)this.boostDelaySetting.get();
            rechargeDelay = (Integer)this.rechargeDelaySetting.get();
            timerMultiplier = (Double)this.boostMultiplier.get();
            timerMultiplierOnRecharge = (Double)this.boostMultiplierOnRecharge.get();
            break;
         case Custom_v2:
            this.currentMode = new NCPv2();
            workingDelay = (Integer)this.boostDelaySetting.get();
            rechargeDelay = (Integer)this.rechargeDelaySetting.get();
            timerMultiplier = (Double)this.boostMultiplier.get();
            timerMultiplierInAir = (Double)this.boostMultiplierInAir.get();
            timerMultiplierOnRecharge = (Double)this.boostMultiplierOnRecharge.get();
      }
   }

   public void onActivate() {
      this.currentMode.onActivate();
   }

   public void onDeactivate() {
      if ((Boolean)this.rechargeOnDisable.get()) {
         workingTimer = 0;
         rechargeTimer = 0;
      }

      this.currentMode.onDeactivate();
   }

   @EventHandler
   private void onPreTick(Pre event) {
      this.currentMode.onTickEventPre(event);
   }

   @EventHandler
   private void onPostTick(Post event) {
      this.currentMode.onTickEventPost(event);
   }

   @EventHandler
   public void onSendPacket(Send event) {
      this.currentMode.onSendPacket(event);
   }

   @EventHandler
   public void onSentPacket(Sent event) {
      this.currentMode.onSentPacket(event);
   }

   public static double getPercentage() {
      return find_percent(0.0, rechargeDelay, rechargeTimer);
   }

   public String getInfoString() {
      double percentage = find_percent(0.0, rechargeDelay, rechargeTimer);
      return String.format("%.1f", percentage);
   }

   public static double find_percent(double start, double end, double val) {
      end -= start;
      val -= start;
      start = 0.0;
      return (1.0 - val / end) * 100.0;
   }
}
