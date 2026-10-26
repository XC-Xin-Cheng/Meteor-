package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ShieldBypass extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> ignoreAxe = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("ignore-axe")).description("Ignore if you are holding an axe.")).defaultValue(true)).build());

   public ShieldBypass() {
      super(MeteorRejectsAddon.CATEGORY, "shield-bypass", "Attempts to teleport you behind enemies to bypass shields.");
   }

   @EventHandler
   private void onMouseButton(MouseClickEvent event) {
      if (!Modules.get().isActive(KillAura.class)) {
         if (this.mc.screen == null
            && !this.mc.player.isUsingItem()
            && event.action == KeyAction.Press
            && event.button() == 0
            && this.mc.hitResult instanceof EntityHitResult result) {
            this.bypass(result.getEntity(), event);
         }
      }
   }

   private boolean isBlocked(Vec3 pos, LivingEntity target) {
      Vec3 targetPos = new Vec3(target.getX(), target.getY(), target.getZ());
      Vec3 vec3d3 = pos.vectorTo(targetPos).normalize();
      return new Vec3(vec3d3.x, 0.0, vec3d3.z).dot(target.getViewVector(1.0F)) >= 0.0;
   }

   public void bypass(Entity target, Cancellable event) {
      if (target instanceof LivingEntity e && e.isBlocking()) {
         if ((Boolean)this.ignoreAxe.get() && InvUtils.testInMainHand(ix -> ix.getItem() instanceof AxeItem)) {
            return;
         }

         Vec3 playerPos = new Vec3(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
         if (this.isBlocked(playerPos, e)) {
            return;
         }

         Vec3 offset = Vec3.directionFromRotation(0.0F, this.mc.player.getYRot()).normalize().scale(2.0);
         Vec3 ePos = new Vec3(e.getX(), e.getY(), e.getZ());
         Vec3 newPos = ePos.add(offset);
         boolean inside = false;

         for (float i = 0.0F; i < 4.0F; i = (float)(i + 0.25)) {
            Vec3 targetPos = newPos.add(0.0, i, 0.0);
            boolean collides = !this.mc.level.noCollision(null, e.getBoundingBox().move(offset).move(targetPos.subtract(newPos)));
            if (!inside && collides) {
               inside = true;
            } else if (inside && !collides) {
               newPos = targetPos;
               break;
            }
         }

         if (!this.isBlocked(newPos, e)) {
            return;
         }

         event.cancel();
         this.mc.getConnection().send(new Pos(newPos.x(), newPos.y(), newPos.z(), true, false));
         this.mc.getConnection().send(new ServerboundAttackPacket(e.getId()));
         this.mc.getConnection().send(new ServerboundSwingPacket(this.mc.player.getUsedItemHand()));
         this.mc.player.resetOnlyAttackStrengthTicker();
         this.mc.getConnection().send(new Pos(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), true, this.mc.player.horizontalCollision));
      }
   }
}
