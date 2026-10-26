package nekiplay.meteorplus.features.modules.combat.velocity;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.features.modules.combat.velocity.modes.GrimCancel;
import nekiplay.meteorplus.features.modules.combat.velocity.modes.GrimCancel_v2;
import nekiplay.meteorplus.features.modules.combat.velocity.modes.GrimSkip;

public class VelocityPlus extends Module {
   private final SettingGroup settingsGroup = this.settings.getDefaultGroup();
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<VelocityModes> mode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("Velocity mode.")).defaultValue(VelocityModes.Grim_Cancel))
                  .onModuleActivated(timerModesSetting -> this.onTimerModeChanged((VelocityModes)timerModesSetting.get())))
               .onChanged(this::onTimerModeChanged))
            .build()
      );
   private VelocityMode currentMode;

   public VelocityPlus() {
      super(Categories.Movement, "velocity+", "Bypass velocity.");
   }

   private void onTimerModeChanged(VelocityModes mode) {
      switch (mode) {
         case Grim_Cancel:
            this.currentMode = new GrimCancel();
            break;
         case Grim_Cancel_v2:
            this.currentMode = new GrimCancel_v2();
            break;
         case Grim_Skip:
            this.currentMode = new GrimSkip();
      }
   }

   public void onActivate() {
      this.currentMode.onActivate();
   }

   public void onDeactivate() {
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

   @EventHandler
   private void onReceivePacket(Receive event) {
      this.currentMode.onReceivePacket(event);
   }
}
