package nekiplay.meteorplus.features.modules.movement.nofall.modes;

import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallMode;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallModes;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ElytraFly extends NoFallMode {
   public ElytraFly() {
      super(NoFallModes.Elytra_Fly);
   }

   @Override
   public void onTickEventPre(Pre event) {
      if (this.mc.player.fallDistance > 2.0) {
         FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
         if (elytra.found()) {
            int slot = elytra.slot();
            if (this.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
               InvUtils.move().from(slot).toArmor(2);
            }
         }

         if (this.mc.player.fallDistance > 2.7) {
            this.mc.player.connection.send(new ServerboundPlayerCommandPacket(this.mc.player, Action.START_FALL_FLYING));
            this.mc.player.connection.send(new StatusOnly(true, this.mc.player.horizontalCollision));
            Vec3 vel = this.mc.player.getDeltaMovement();
            this.mc.player.setDeltaMovement(vel.x, 0.0, vel.z);
            this.mc.player.fallDistance = 0.0;
            this.mc.player.setOnGround(true);
         }
      }
   }
}
