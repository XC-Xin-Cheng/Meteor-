package fr.crazycat256.satellite.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.crazycat256.satellite.utils.TPUtils;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.entity.player.Player;

public class TPCommand extends Command {
   public TPCommand() {
      super("tp", "Teleports you to a player.", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(argument("player", PlayerArgumentType.create()).executes(ctx -> {
         Player player = PlayerArgumentType.get(ctx);
         if (player == mc.player) {
            this.error("You can't teleport to yourself.", new Object[0]);
            return 0;
         } else if (player == mc.getCameraEntity()) {
            this.error("Camera entity doesn't exist.", new Object[0]);
            return 0;
         } else {
            TPUtils.PaperTP(player.position());
            mc.player.setPos(player.getX(), player.getY(), player.getZ());
            return 1;
         }
      }));
   }
}
