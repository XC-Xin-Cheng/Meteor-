package fr.crazycat256.satellite.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.crazycat256.satellite.utils.TPUtils;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.phys.Vec3;

public class TPCamCommand extends Command {
   public TPCamCommand() {
      super("tpcam", "Teleports you to the camera entity.", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.executes(context -> {
         if (mc.getCameraEntity() == null) {
            this.error("Camera entity doesn't exist.", new Object[0]);
            return 0;
         } else {
            Vec3 pos = mc.gameRenderer.getMainCamera().position();
            TPUtils.PaperTP(pos);
            mc.player.setPos(pos.x, pos.y, pos.z);
            return 1;
         }
      });
   }
}
