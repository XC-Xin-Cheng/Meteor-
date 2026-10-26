package com.stash.hunt.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class CommandExample extends Command {
   public CommandExample() {
      super("example", "Sends a message.", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.executes(context -> {
         this.info("hi", new Object[0]);
         return 1;
      });
   }
}
