package nekiplay.meteorplus.features.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import nekiplay.meteorplus.features.modules.player.AutoDropPlus;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public class ClearInventoryCommand extends Command {
   public ClearInventoryCommand() {
      super("clearinv", "Clear inventory", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.executes(context -> {
         for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = mc.player.getInventory().getItem(i);
            if (itemStack != null) {
               mc.gameMode.handleContainerInput(0, AutoDropPlus.invIndexToSlotId(i), 300, ContainerInput.SWAP, mc.player);
            }
         }

         return 1;
      });
   }
}
