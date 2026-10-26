package zgoly.meteorist.utils.misc;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.meteordev.starscript.value.Value;
import org.meteordev.starscript.value.ValueMap;

public class MeteoristStarscript {
   private static final MeteoristStarscript.PlayerFallState FALL_STATE = new MeteoristStarscript.PlayerFallState();

   public static void init() {
      MeteorStarscript.ss
         .set(
            "meteorist",
            new ValueMap()
               .set("fall_distance", () -> Value.number(FALL_STATE.getDistance(false)))
               .set("max_fall_distance", () -> Value.number(FALL_STATE.getDistance(true)))
               .set("fall_damage", () -> Value.number(FALL_STATE.getFallDamage(false)))
               .set("max_fall_damage", () -> Value.number(FALL_STATE.getFallDamage(true)))
               .set("is_falling", () -> Value.bool(FALL_STATE.isFalling()))
               .set("would_take_damage", () -> Value.bool(FALL_STATE.wouldTakeDamage()))
         );
      MeteorClient.EVENT_BUS.subscribe(FALL_STATE);
   }

   private static class PlayerFallState {
      private double maxY = 0.0;
      private boolean wasOnGround = true;
      private double cachedMaxDamage = 0.0;

      @EventHandler
      private void onGameJoined(GameJoinedEvent event) {
         this.reset();
      }

      public void reset() {
         this.maxY = 0.0;
         this.wasOnGround = true;
         this.cachedMaxDamage = 0.0;
      }

      public double getDistance(boolean max) {
         Player player = MeteorClient.mc.player;
         if (player != null && MeteorClient.mc.level != null) {
            double currentY = player.getY();
            boolean onGround = player.onGround();
            if (!onGround) {
               this.maxY = this.wasOnGround ? currentY : Math.max(this.maxY, currentY);
            }

            this.wasOnGround = onGround;
            if (onGround) {
               return 0.0;
            } else {
               Vec3 start = max ? new Vec3(player.getX(), this.maxY, player.getZ()) : player.position();
               Vec3 end = new Vec3(player.getX(), MeteorClient.mc.level.getMinY(), player.getZ());
               BlockHitResult result = MeteorClient.mc.level.clip(new ClipContext(start, end, Block.OUTLINE, Fluid.ANY, player));
               return result.getLocation().distanceTo(start);
            }
         } else {
            return 0.0;
         }
      }

      public double getFallDamage(boolean max) {
         Player player = MeteorClient.mc.player;
         if (player == null) {
            return 0.0;
         } else if (!player.getAbilities().flying
            && !player.hasEffect(MobEffects.SLOW_FALLING)
            && !player.hasEffect(MobEffects.LEVITATION)
            && !Modules.get().isActive(NoFall.class)
            && !EntityUtils.isAboveWater(player)) {
            float damage = DamageUtils.fallDamage(player);
            if (max) {
               this.cachedMaxDamage = Math.max(this.cachedMaxDamage, (double)damage);
               if (player.onGround()) {
                  double result = this.cachedMaxDamage;
                  this.cachedMaxDamage = 0.0;
                  return result;
               } else {
                  return this.cachedMaxDamage;
               }
            } else {
               return damage;
            }
         } else {
            if (player.onGround()) {
               this.cachedMaxDamage = 0.0;
            }

            return max ? this.cachedMaxDamage : 0.0;
         }
      }

      public boolean isFalling() {
         Player player = MeteorClient.mc.player;
         return player != null && player.fallDistance > 0.0;
      }

      public boolean wouldTakeDamage() {
         Player player = MeteorClient.mc.player;
         return player != null && !player.onGround() ? this.getFallDamage(false) > 0.0 : false;
      }
   }
}
