package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.phys.Vec3;
import zgoly.meteorist.Meteorist;

public class JumpFlight extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> modifyHorizontalSpeed = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("modify-horizontal-speed")).description("Whether to override player's horizontal movement speed."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Double> horizontalSpeed = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("horizontal-speed"))
                  .description("How fast you move forward/backward and strafe left/right while flying."))
               .defaultValue(16.0)
               .sliderRange(1.0, 128.0)
               .min(1.0)
               .visible(this.modifyHorizontalSpeed::get))
            .build()
      );
   private final Setting<Integer> verticalStep = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("vertical-step"))
                  .description("How many blocks to ascend or descend per adjustment."))
               .defaultValue(1))
            .min(1)
            .build()
      );
   private final Setting<Integer> verticalAdjustTickDelay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("vertical-adjust-delay"))
                  .description("Delay in ticks between vertical adjustments while holding Jump/Sneak."))
               .defaultValue(4))
            .min(1)
            .build()
      );
   private final Setting<Boolean> scrollControl = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("scroll-to-adjust-speed"))
                     .description("Allow adjusting horizontal speed with the mouse scroll wheel."))
                  .defaultValue(true))
               .visible(this.modifyHorizontalSpeed::get))
            .build()
      );
   private final Setting<Double> scrollSensitivity = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("scroll-sensitivity"))
                  .description("How much the speed changes per scroll tick."))
               .defaultValue(1.0)
               .sliderRange(0.1, 5.0)
               .min(0.1)
               .visible(() -> (Boolean)this.modifyHorizontalSpeed.get() && (Boolean)this.scrollControl.get()))
            .build()
      );
   private boolean targetYInitialized;
   private boolean jumpHeld;
   private boolean sneakHeld;
   private int tickCounter;
   private int targetYLevel;

   public JumpFlight() {
      super(Meteorist.CATEGORY, "jump-flight", "Flight that uses repeated jumps to stay in the air. Works best with NoFall.");
   }

   public void onActivate() {
      this.targetYInitialized = false;
      this.jumpHeld = false;
      this.sneakHeld = false;
      this.tickCounter = 0;
   }

   @EventHandler
   public void onMove(PlayerMoveEvent event) {
      if ((Boolean)this.modifyHorizontalSpeed.get()) {
         Vec3 vel = PlayerUtils.getHorizontalVelocity((Double)this.horizontalSpeed.get());
         ((IVec3)event.movement).meteor$set(vel.x(), event.movement.y, vel.z());
      }
   }

   @EventHandler
   private void onMouseScroll(MouseScrollEvent event) {
      if ((Boolean)this.modifyHorizontalSpeed.get() && (Boolean)this.scrollControl.get()) {
         this.horizontalSpeed.set((Double)this.horizontalSpeed.get() + event.value * (Double)this.scrollSensitivity.get());
      }
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.player != null && this.mc.level != null) {
         if (!this.targetYInitialized) {
            this.targetYLevel = this.mc.player.blockPosition().getY();
            this.targetYInitialized = true;
         }

         if (!Modules.get().isActive(Freecam.class) && this.mc.screen == null) {
            this.jumpHeld = this.mc.options.keyJump.isDown();
            this.sneakHeld = this.mc.options.keyShift.isDown();
         }

         this.tickCounter++;
         if (this.tickCounter >= (Integer)this.verticalAdjustTickDelay.get()) {
            if (this.jumpHeld) {
               this.targetYLevel = this.targetYLevel + (Integer)this.verticalStep.get();
               this.tickCounter = 0;
            } else if (this.sneakHeld) {
               this.targetYLevel = this.targetYLevel - (Integer)this.verticalStep.get();
               this.tickCounter = 0;
            }
         }

         if (this.mc.player.blockPosition().getY() <= this.targetYLevel) {
            this.mc.player.jumpFromGround();
         }
      }
   }

   public String getInfoString() {
      return "y=" + this.targetYLevel + (this.modifyHorizontalSpeed.get() ? "; s=" + this.horizontalSpeed.get() : "");
   }
}
