package nekiplay.meteorplus.features.modules.movement.nofall.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IServerboundMovePlayerPacket;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallMode;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallModes;
import nekiplay.meteorplus.mixin.minecraft.entity.ServerboundMovePlayerPacketAccessor;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class No_Ground_Elytra extends NoFallMode {
   public No_Ground_Elytra() {
      super(NoFallModes.No_Ground_Elytra);
   }

   @Override
   public void onActivate() {
      FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
      if (!elytra.found()) {
         ChatUtils.error("Elytra not found, this bypass for working need elytra", new Object[0]);
      }
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
      }
   }

   @Override
   public void onSendPacket(Send event) {
      if (event.packet instanceof IServerboundMovePlayerPacket move) {
         ServerboundMovePlayerPacketAccessor move2 = (ServerboundMovePlayerPacketAccessor)move;
         if (move2.getOnGround()) {
            this.mc.player.connection.send(new ServerboundPlayerCommandPacket(this.mc.player, Action.START_FALL_FLYING));
            move2.setOnGround(false);
         }
      }
   }
}
