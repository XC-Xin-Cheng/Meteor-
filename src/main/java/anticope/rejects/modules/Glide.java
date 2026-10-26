package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.OffGroundSpeedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Glide extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<Double> fallSpeed = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("fall-speed")).description("Fall speed.")).defaultValue(0.125).min(0.005).sliderRange(0.005, 0.25).build());
   public final Setting<Double> moveSpeed = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("move-speed")).description("Horizontal movement factor."))
            .defaultValue(1.2)
            .min(1.0)
            .sliderRange(1.0, 5.0)
            .build()
      );
   public final Setting<Double> minHeight = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("min-height")).description("Won't glide when you are too close to the ground."))
            .defaultValue(0.0)
            .min(0.0)
            .sliderRange(0.0, 2.0)
            .build()
      );

   public Glide() {
      super(MeteorRejectsAddon.CATEGORY, "glide", "Yeehaw!");
   }

   @EventHandler
   private void onTick(Post event) {
      LocalPlayer player = this.mc.player;
      Vec3 v = player.getDeltaMovement();
      if (!player.onGround() && !player.isInWater() && !player.isInLava() && !player.onClimbable() && !(v.y >= 0.0)) {
         if ((Double)this.minHeight.get() > 0.0) {
            AABB box = player.getBoundingBox();
            box = box.minmax(box.move(0.0, -(Double)this.minHeight.get(), 0.0));
            if (!this.mc.level.noCollision(box)) {
               return;
            }
         }

         player.setDeltaMovement(v.x, Math.max(v.y, -(Double)this.fallSpeed.get()), v.z);
      }
   }

   @EventHandler
   private void onOffGroundSpeed(OffGroundSpeedEvent event) {
      event.speed = event.speed * ((Double)this.moveSpeed.get()).floatValue();
   }
}
