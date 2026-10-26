package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ExtraElytra extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> instantFly = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("instant-fly")).description("Jump to fly, no weird double-jump needed!")).defaultValue(true)).build()
      );
   private final Setting<Boolean> speedCtrl = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("speed-ctrl"))
                  .description("Control your speed with the Forward and Back keys.\n(default: W and S)\nNo fireworks needed!"))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> heightCtrl = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("height-ctrl"))
                  .description("Control your height with the Jump and Sneak keys.\n(default: Spacebar and Shift)\nNo fireworks needed!"))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> stopInWater = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("stop-in-water")).description("Stop flying in water")).defaultValue(true)).build());
   private int jumpTimer;

   public void onActivate() {
      this.jumpTimer = 0;
   }

   public ExtraElytra() {
      super(MeteorRejectsAddon.CATEGORY, "extra-elytra", "Easier elytra");
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.jumpTimer > 0) {
         this.jumpTimer--;
      }

      ItemStack chest = this.mc.player.getItemBySlot(EquipmentSlot.CHEST);
      if (chest.getItem() == Items.ELYTRA) {
         if (this.mc.player.isFallFlying()) {
            if ((Boolean)this.stopInWater.get() && this.mc.player.isInWater()) {
               this.sendStartStopPacket();
            } else {
               this.controlSpeed();
               this.controlHeight();
            }
         } else {
            if (chest.getDamageValue() < chest.getMaxDamage() - 1 && this.mc.options.keyJump.isDown()) {
               this.doInstantFly();
            }
         }
      }
   }

   private void sendStartStopPacket() {
      ServerboundPlayerCommandPacket packet = new ServerboundPlayerCommandPacket(this.mc.player, Action.START_FALL_FLYING);
      this.mc.player.connection.send(packet);
   }

   private void controlHeight() {
      if ((Boolean)this.heightCtrl.get()) {
         Vec3 v = this.mc.player.getDeltaMovement();
         if (this.mc.options.keyJump.isDown()) {
            this.mc.player.setDeltaMovement(v.x, v.y + 0.08, v.z);
         } else if (this.mc.options.keyShift.isDown()) {
            this.mc.player.setDeltaMovement(v.x, v.y - 0.04, v.z);
         }
      }
   }

   private void controlSpeed() {
      if ((Boolean)this.speedCtrl.get()) {
         float yaw = (float)Math.toRadians(this.mc.player.getYRot());
         Vec3 forward = new Vec3(-Mth.sin(yaw) * 0.05, 0.0, Mth.cos(yaw) * 0.05);
         Vec3 v = this.mc.player.getDeltaMovement();
         if (this.mc.options.keyUp.isDown()) {
            this.mc.player.setDeltaMovement(v.add(forward));
         } else if (this.mc.options.keyDown.isDown()) {
            this.mc.player.setDeltaMovement(v.subtract(forward));
         }
      }
   }

   private void doInstantFly() {
      if ((Boolean)this.instantFly.get()) {
         if (this.jumpTimer <= 0) {
            this.jumpTimer = 20;
            this.mc.player.setJumping(false);
            this.mc.player.setSprinting(true);
            this.mc.player.jumpFromGround();
         }

         this.sendStartStopPacket();
      }
   }
}
