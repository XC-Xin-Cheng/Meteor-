package anticope.rejects.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class EnumStringArgumentType implements ArgumentType<String> {
   private Collection<String> EXAMPLES;
   private static final DynamicCommandExceptionType INVALID_OPTION = new DynamicCommandExceptionType(
      name -> Component.literal(name + " is not a valid option.")
   );

   public EnumStringArgumentType(Collection<String> examples) {
      this.EXAMPLES = examples;
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      String arg = reader.readUnquotedString();
      if (!this.EXAMPLES.contains(arg)) {
         throw INVALID_OPTION.create(arg);
      } else {
         return arg;
      }
   }

   public static String getString(CommandContext<?> context, String name) {
      return (String)context.getArgument(name, String.class);
   }

   public Collection<String> getExamples() {
      return this.EXAMPLES;
   }

   @Override
   public String toString() {
      return "string()";
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return SharedSuggestionProvider.suggest(this.EXAMPLES, builder);
   }
}
