package anticope.rejects.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.phys.Vec3;

public class SetVelocityCommand extends Command {
   public SetVelocityCommand() {
      super("set-velocity", "Sets player velocity", new String[]{"velocity", "vel"});
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(argument("y", DoubleArgumentType.doubleArg()).executes(ctx -> {
         Vec3 currentVelocity = mc.player.getDeltaMovement();
         mc.player.setDeltaMovement(currentVelocity.x, DoubleArgumentType.getDouble(ctx, "y"), currentVelocity.z);
         return 1;
      }));
      builder.then(argument("x", DoubleArgumentType.doubleArg()).then(argument("z", DoubleArgumentType.doubleArg()).executes(ctx -> {
         double x = DoubleArgumentType.getDouble(ctx, "x");
         double z = DoubleArgumentType.getDouble(ctx, "z");
         mc.player.setDeltaMovement(x, mc.player.getDeltaMovement().y, z);
         return 1;
      })));
      builder.then(
         argument("x", DoubleArgumentType.doubleArg())
            .then(argument("y", DoubleArgumentType.doubleArg()).then(argument("z", DoubleArgumentType.doubleArg()).executes(ctx -> {
               double x = DoubleArgumentType.getDouble(ctx, "x");
               double y = DoubleArgumentType.getDouble(ctx, "y");
               double z = DoubleArgumentType.getDouble(ctx, "z");
               mc.player.setDeltaMovement(x, y, z);
               return 1;
            })))
      );
   }
}
