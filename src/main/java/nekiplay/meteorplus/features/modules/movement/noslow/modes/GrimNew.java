package nekiplay.meteorplus.features.modules.movement.noslow.modes;

import nekiplay.main.events.PlayerUseMultiplierEvent;
import nekiplay.meteorplus.features.modules.movement.noslow.NoSlowMode;
import nekiplay.meteorplus.features.modules.movement.noslow.NoSlowModes;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

public class GrimNew extends NoSlowMode {
   public GrimNew() {
      super(NoSlowModes.Grim_New);
   }

   @Override
   public void onUse(PlayerUseMultiplierEvent event) {
      if (this.mc.player.isShiftKeyDown()) {
         event.setForward(((Double)this.settings.sneakForward.get()).floatValue());
         event.setSideways(((Double)this.settings.sneakSideways.get()).floatValue());
      } else if (this.mc.player.isUsingItem()) {
         event.setForward(((Double)this.settings.usingForward.get()).floatValue());
         event.setSideways(((Double)this.settings.usingSideways.get()).floatValue());
      } else {
         event.setForward(((Double)this.settings.otherForward.get()).floatValue());
         event.setSideways(((Double)this.settings.otherSideways.get()).floatValue());
      }

      InteractionHand hand = this.mc.player.getUsedItemHand();
      ClientPacketListener network = this.mc.getConnection();

      assert network != null;

      if (hand == InteractionHand.MAIN_HAND) {
         network.send(new ServerboundUseItemPacket(InteractionHand.OFF_HAND, 0, 0.0F, 0.0F));
      } else if (hand == InteractionHand.OFF_HAND) {
         network.send(new ServerboundSetCarriedItemPacket(this.mc.player.getInventory().getSelectedSlot() % 8 + 1));
         network.send(new ServerboundSetCarriedItemPacket(this.mc.player.getInventory().getSelectedSlot()));
      }
   }
}
