package anticope.rejects.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider.TextCoordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.world.phys.Vec3;

public class ClientPosArgumentType implements ArgumentType<Vec3> {
   private static final Minecraft mc = Minecraft.getInstance();

   public static ClientPosArgumentType pos() {
      return new ClientPosArgumentType();
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      if (!(context.getSource() instanceof SharedSuggestionProvider)) {
         return Suggestions.empty();
      } else {
         String string = builder.getRemaining();
         Collection<TextCoordinates> collection2 = ((SharedSuggestionProvider)context.getSource()).getRelevantCoordinates();
         return SharedSuggestionProvider.suggestCoordinates(string, collection2, builder, Commands.createValidator(this::parse));
      }
   }

   public static Vec3 getPos(CommandContext<?> context, String name) {
      return (Vec3)context.getArgument(name, Vec3.class);
   }

   public Vec3 parse(StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();
      WorldCoordinate coordinateArgument = WorldCoordinate.parseInt(reader);
      if (reader.canRead() && reader.peek() == ' ') {
         reader.skip();
         WorldCoordinate coordinateArgument2 = WorldCoordinate.parseInt(reader);
         if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            WorldCoordinate coordinateArgument3 = WorldCoordinate.parseInt(reader);
            double x = coordinateArgument.get(mc.player.getX());
            double y = coordinateArgument2.get(mc.player.getY());
            double z = coordinateArgument3.get(mc.player.getZ());
            return new Vec3(x, y, z);
         } else {
            reader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
         }
      } else {
         reader.setCursor(i);
         throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
      }
   }
}
