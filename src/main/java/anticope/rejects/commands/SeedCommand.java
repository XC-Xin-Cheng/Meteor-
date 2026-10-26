package anticope.rejects.commands;

import anticope.rejects.arguments.EnumArgumentType;
import anticope.rejects.utils.seeds.Seed;
import anticope.rejects.utils.seeds.Seeds;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.seedfinding.mccore.version.MCVersion;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SeedCommand extends Command {
   private static final SimpleCommandExceptionType NO_SEED = new SimpleCommandExceptionType(Component.literal("No seed for current world saved."));

   public SeedCommand() {
      super("seed", "Get or set seed for the current world.", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.executes(ctx -> {
         Seed seed = Seeds.get().getSeed();
         if (seed == null) {
            throw NO_SEED.create();
         } else {
            this.info(seed.toText());
            return 1;
         }
      });
      builder.then(literal("list").executes(ctx -> {
         Seeds.get().seeds.forEach((name, seed) -> {
            MutableComponent text = Component.literal(name + " ");
            text.append(seed.toText());
            this.info(text);
         });
         return 1;
      }));
      builder.then(literal("delete").executes(ctx -> {
         Seed seed = Seeds.get().getSeed();
         if (seed != null) {
            MutableComponent text = Component.literal("Deleted ");
            text.append(seed.toText());
            this.info(text);
         }

         Seeds.get().seeds.remove(Utils.getWorldName());
         return 1;
      }));
      builder.then(argument("seed", StringArgumentType.string()).executes(ctx -> {
         Seeds.get().setSeed(StringArgumentType.getString(ctx, "seed"));
         return 1;
      }));
      builder.then(argument("seed", StringArgumentType.string()).then(argument("version", EnumArgumentType.enumArgument(MCVersion.latest())).executes(ctx -> {
         Seeds.get().setSeed(StringArgumentType.getString(ctx, "seed"), EnumArgumentType.getEnum(ctx, "version", MCVersion.latest()));
         return 1;
      })));
   }
}
