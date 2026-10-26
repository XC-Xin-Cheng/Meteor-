package nekiplay.meteorplus.features.modules.movement.elytrafly;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.features.modules.movement.elytrafly.modes.Control;
import nekiplay.meteorplus.features.modules.movement.elytrafly.modes.Wasp;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class ElytraFlyPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<ElytraFlyModes> flightMode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("The mode of flying.")).defaultValue(ElytraFlyModes.Control))
                  .onModuleActivated(flightModesSetting -> this.onModeChanged((ElytraFlyModes)flightModesSetting.get())))
               .onChanged(this::onModeChanged))
            .build()
      );
   public final Setting<Double> horizontal_wasp = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("horizontal-speed"))
                  .description("How many blocks to move each tick horizontally."))
               .defaultValue(1.0)
               .min(0.0)
               .sliderRange(0.0, 5.0)
               .visible(() -> this.flightMode.get() == ElytraFlyModes.Wasp))
            .build()
      );
   public final Setting<Double> fallSpeed_wasp = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("fall-speed"))
                  .description("How many blocks to fall down each tick."))
               .defaultValue(0.0)
               .min(0.0)
               .sliderRange(0.0, 1.0)
               .visible(() -> this.flightMode.get() == ElytraFlyModes.Wasp))
            .build()
      );
   public final Setting<Boolean> smartFall_wasp = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("smart-fall"))
                     .description("Only falls down when looking down."))
                  .defaultValue(true))
               .visible(() -> this.flightMode.get() == ElytraFlyModes.Wasp))
            .build()
      );
   public final Setting<Double> up_wasp = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("up-speed"))
                  .description("How many blocks to move up each tick."))
               .defaultValue(1.0)
               .min(0.0)
               .sliderRange(0.0, 5.0)
               .visible(() -> this.flightMode.get() == ElytraFlyModes.Wasp))
            .build()
      );
   public final Setting<Double> down_wasp = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("down-speed"))
                  .description("How many blocks to move down each tick."))
               .defaultValue(1.0)
               .min(0.0)
               .sliderRange(0.0, 5.0)
               .visible(() -> this.flightMode.get() == ElytraFlyModes.Wasp))
            .build()
      );
   public final Setting<Double> speed_control = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("Speed"))
                  .description("How many blocks to move each tick."))
               .defaultValue(1.0)
               .min(0.0)
               .sliderRange(0.0, 5.0)
               .visible(() -> this.flightMode.get() == ElytraFlyModes.Control))
            .build()
      );
   public final Setting<Double> upMultiplier_control = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("up-multiplier"))
                  .description("How many times faster should we fly up."))
               .defaultValue(1.0)
               .min(0.0)
               .sliderRange(0.0, 5.0)
               .visible(() -> this.flightMode.get() == ElytraFlyModes.Control))
            .build()
      );
   public final Setting<Double> downSpeed_control = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("down-speed"))
                  .description("How many blocks to move down each tick."))
               .defaultValue(1.0)
               .min(0.0)
               .sliderRange(0.0, 5.0)
               .visible(() -> this.flightMode.get() == ElytraFlyModes.Control))
            .build()
      );
   public final Setting<Double> fallSpeed_control = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("fall-speed"))
                  .description("How many blocks to fall down each tick."))
               .defaultValue(0.0)
               .min(0.0)
               .sliderRange(0.0, 1.0)
               .visible(() -> this.flightMode.get() == ElytraFlyModes.Control))
            .build()
      );
   public final Setting<Boolean> resetSpeed = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("reset-speed"))
                  .description("Reset you speed after disable"))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> noCrash = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("no-crash"))
                  .description("Stops you from going into walls."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Integer> crashLookAhead = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("crash-look-ahead"))
                     .description("Distance to look ahead when flying."))
                  .defaultValue(5))
               .range(1, 15)
               .sliderMin(1)
               .visible(this.noCrash::get))
            .build()
      );
   private ElytraFlyMode currentMode = new Control();

   public ElytraFlyPlus() {
      super(Categories.Movement, "elytra-fly+", "Gives you more control over your elytra.");
   }

   public void onActivate() {
      this.currentMode.onActivate();
   }

   public void onDeactivate() {
      this.currentMode.onDeactivate();
   }

   @EventHandler
   private void onPlayerMove(PlayerMoveEvent event) {
      this.currentMode.onPlayerMove(event);
      if ((Boolean)this.noCrash.get() && this.mc.player.isFallFlying()) {
         Vec3 lookAheadPos = this.mc
            .player
            .position()
            .add(this.mc.player.getDeltaMovement().normalize().scale(((Integer)this.crashLookAhead.get()).intValue()));
         ClipContext raycastContext = new ClipContext(
            this.mc.player.position(), new Vec3(lookAheadPos.x(), this.mc.player.getY(), lookAheadPos.z()), Block.COLLIDER, Fluid.NONE, this.mc.player
         );
         BlockHitResult hitResult = this.mc.level.clip(raycastContext);
         if (hitResult != null && hitResult.getType() == Type.BLOCK) {
            ((IVec3)event.movement).meteor$set(0.0, this.currentMode.velY, 0.0);
         }
      }
   }

   @EventHandler
   private void onTick(Post event) {
      this.currentMode.onTick();
   }

   @EventHandler
   private void onPreTick(Pre event) {
      this.currentMode.onPreTick();
   }

   @EventHandler
   private void onPacketSend(Send event) {
      this.currentMode.onPacketSend(event);
   }

   @EventHandler
   private void onPacketReceive(Receive event) {
      this.currentMode.onPacketReceive(event);
   }

   private void onModeChanged(ElytraFlyModes mode) {
      switch (mode) {
         case Control:
            this.currentMode = new Control();
            break;
         case Wasp:
            this.currentMode = new Wasp();
      }
   }
}
