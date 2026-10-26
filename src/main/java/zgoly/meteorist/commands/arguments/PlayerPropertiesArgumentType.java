package zgoly.meteorist.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;

public class PlayerPropertiesArgumentType implements ArgumentType<List<String>> {
   public static final List<String> PROPERTIES = Arrays.asList("Player", "UUID", "GameMode", "Skin_URL", "Latency");
   private static final PlayerPropertiesArgumentType INSTANCE = new PlayerPropertiesArgumentType();
   private static final DynamicCommandExceptionType INVALID_KEYWORD_EXCEPTION = new DynamicCommandExceptionType(
      o -> Component.nullToEmpty("Invalid keyword: " + o)
   );

   public static PlayerPropertiesArgumentType create() {
      return INSTANCE;
   }

   public static List<String> get(CommandContext<?> context) {
      List<?> properties = (List<?>)context.getArgument("properties", List.class);
      return properties.stream().filter(String.class::isInstance).map(String.class::cast).toList();
   }

   public List<String> parse(StringReader reader) throws CommandSyntaxException {
      String text = reader.getRemaining();
      reader.setCursor(reader.getTotalLength());
      List<String> keywords = List.of(text.split("\\s+"));
      Optional<String> unmatchedKeyword = keywords.stream()
         .filter(keyword -> PROPERTIES.stream().noneMatch(property -> property.equalsIgnoreCase(keyword)))
         .findAny();
      if (unmatchedKeyword.isPresent()) {
         throw INVALID_KEYWORD_EXCEPTION.createWithContext(reader, unmatchedKeyword.get());
      } else {
         return keywords;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      List<String> suggestions = new ArrayList<>(PROPERTIES);
      String remainingLowerCase = builder.getRemainingLowerCase();
      List<String> parsedProperties = remainingLowerCase.isEmpty() ? Collections.emptyList() : Arrays.asList(remainingLowerCase.split(" "));
      parsedProperties.forEach(property -> suggestions.removeIf(text -> text.equalsIgnoreCase(property)));
      int offset;
      if (!remainingLowerCase.endsWith(" ") && !remainingLowerCase.isEmpty()) {
         String last = parsedProperties.getLast();
         suggestions.removeIf(text -> !text.toLowerCase().startsWith(last));
         offset = builder.getInput().length() - last.length();
      } else {
         offset = builder.getInput().length();
      }

      builder = builder.createOffset(offset);
      suggestions.forEach(builder::suggest);
      return builder.buildFuture();
   }

   public Collection<String> getExamples() {
      return PROPERTIES;
   }
}
