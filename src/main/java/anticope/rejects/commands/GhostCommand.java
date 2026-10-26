package anticope.rejects.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;

public class GhostCommand extends Command {
   public GhostCommand() {
      super("ghost", "Remove ghost blocks & bypass AntiXray", new String[]{"aax", "anti-anti-xray"});
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.executes(ctx -> {
         this.execute(4);
         return 1;
      });
      builder.then(argument("radius", IntegerArgumentType.integer(1)).executes(ctx -> {
         int radius = IntegerArgumentType.getInteger(ctx, "radius");
         this.execute(radius);
         return 1;
      }));
   }

   private void execute(int radius) {
      ClientPacketListener conn = mc.getConnection();
      if (conn != null) {
         BlockPos pos = mc.player.blockPosition();

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(
                     Action.ABORT_DESTROY_BLOCK, new BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz), Direction.UP
                  );
                  conn.send(packet);
               }
            }
         }
      }
   }
}
