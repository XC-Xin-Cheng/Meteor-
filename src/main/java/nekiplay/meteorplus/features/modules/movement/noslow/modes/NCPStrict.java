package nekiplay.meteorplus.features.modules.movement.noslow.modes;

import nekiplay.main.events.PlayerUseMultiplierEvent;
import nekiplay.meteorplus.features.modules.movement.noslow.NoSlowMode;
import nekiplay.meteorplus.features.modules.movement.noslow.NoSlowModes;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;

public class NCPStrict extends NoSlowMode {
   public NCPStrict() {
      super(NoSlowModes.NCP_Strict);
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

      if (this.mc.player.isUsingItem()) {
         ClientPacketListener network = this.mc.getConnection();
         network.send(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, this.mc.player.blockPosition(), Direction.DOWN));
      }
   }
}
