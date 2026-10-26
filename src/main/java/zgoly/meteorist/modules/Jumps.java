package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;

public class Jumps extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("delay")).description("Delay between extra jumps in ticks (20 ticks = 1 second).")).defaultValue(3))
            .range(1, 20)
            .sliderRange(1, 10)
            .build()
      );
   private final Setting<Integer> extraJumps = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("extra-jumps")).description("Number of additional jumps to perform after the initial jump."))
               .defaultValue(2))
            .range(1, 10)
            .sliderRange(1, 10)
            .build()
      );
   private final Setting<Boolean> onlyOnGround = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("only-on-ground"))
                  .description("Only activate the jump sequence if the player is standing on the ground."))
               .defaultValue(false))
            .build()
      );
   private int jumpsRemaining;
   private int tickTimer = 0;

   public Jumps() {
      super(Meteorist.CATEGORY, "jumps", "Performs multiple jumps to gain extra height. Works best with NoFall.");
   }

   @EventHandler
   private void onKey(KeyInputEvent event) {
      if (event.action == KeyAction.Press && this.mc.options.keyJump.matches(event.input)) {
         if (this.mc.player != null) {
            if (!(Boolean)this.onlyOnGround.get() || this.mc.player.onGround()) {
               this.mc.player.jumpFromGround();
               this.jumpsRemaining = (Integer)this.extraJumps.get();
               this.tickTimer = 0;
            }
         }
      }
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.player != null) {
         if (this.jumpsRemaining > 0) {
            this.tickTimer++;
            if (this.tickTimer >= (Integer)this.delay.get()) {
               this.mc.player.fallDistance = 0.0;
               this.mc.player.jumpFromGround();
               this.jumpsRemaining--;
               this.tickTimer = 0;
            }
         }
      }
   }

   public String getInfoString() {
      return String.valueOf(this.jumpsRemaining);
   }
}
