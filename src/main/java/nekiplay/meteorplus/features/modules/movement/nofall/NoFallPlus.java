package nekiplay.meteorplus.features.modules.movement.nofall;

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
import nekiplay.meteorplus.features.modules.movement.nofall.modes.Eclip;
import nekiplay.meteorplus.features.modules.movement.nofall.modes.ElytraFly;
import nekiplay.meteorplus.features.modules.movement.nofall.modes.MatrixNew;
import nekiplay.meteorplus.features.modules.movement.nofall.modes.No_Ground;
import nekiplay.meteorplus.features.modules.movement.nofall.modes.No_Ground_Elytra;
import nekiplay.meteorplus.features.modules.movement.nofall.modes.Verus;
import nekiplay.meteorplus.features.modules.movement.nofall.modes.Vulcan;
import nekiplay.meteorplus.features.modules.movement.nofall.modes.Vulcan277;

public class NoFallPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private NoFallMode currentMode;
   public final Setting<NoFallModes> mode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("The method of applying nofall."))
                     .defaultValue(NoFallModes.Elytra_Clip))
                  .onModuleActivated(spiderModesSetting -> this.onModeChanged((NoFallModes)spiderModesSetting.get())))
               .onChanged(this::onModeChanged))
            .build()
      );

   public NoFallPlus() {
      super(Categories.Movement, "no-fall+", "Bypass fall damage or reduce fall damage");
      this.onModeChanged((NoFallModes)this.mode.get());
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

   private void onModeChanged(NoFallModes mode) {
      switch (mode) {
         case No_Ground_Elytra:
            this.currentMode = new No_Ground_Elytra();
            break;
         case No_Ground:
            this.currentMode = new No_Ground();
            break;
         case Elytra_Fly:
            this.currentMode = new ElytraFly();
            break;
         case Elytra_Clip:
            this.currentMode = new Eclip();
            break;
         case Matrix_New:
            this.currentMode = new MatrixNew();
            break;
         case Verus:
            this.currentMode = new Verus();
            break;
         case Vulcan:
            this.currentMode = new Vulcan();
            break;
         case Vulcan_2dot7dot7:
            this.currentMode = new Vulcan277();
      }
   }
}
