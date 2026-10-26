package anticope.rejects.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class PanicCommand extends Command {
   public PanicCommand() {
      super("panic", "Disables all modules.", new String[]{"disable-all"});
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.executes(context -> {
         new ArrayList(Modules.get().getActive()).forEach(Module::toggle);
         return 1;
      });
   }
}
