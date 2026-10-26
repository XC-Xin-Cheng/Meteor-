package anticope.rejects.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class TerrainExport extends Command {
   private static final SimpleCommandExceptionType IO_EXCEPTION = new SimpleCommandExceptionType(Component.literal("An IOException occurred"));
   private final PointerBuffer filters = BufferUtils.createPointerBuffer(1);

   public TerrainExport() {
      super("terrain-export", "Export an area to the c++ terrain finder format (very popbob command).", new String[0]);
      ByteBuffer txtFilter = MemoryUtil.memASCII("*.txt");
      this.filters.put(txtFilter);
      this.filters.rewind();
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(argument("distance", IntegerArgumentType.integer(1)).executes(context -> {
         int distance = IntegerArgumentType.getInteger(context, "distance");
         StringBuilder stringBuilder = new StringBuilder();

         for (int x = -distance; x <= distance; x++) {
            for (int z = -distance; z <= distance; z++) {
               for (int y = distance; y >= -distance; y--) {
                  BlockPos pos = mc.player.blockPosition().offset(x, y, z);
                  if (mc.level.getBlockState(pos).isCollisionShapeFullBlock(mc.level, pos)) {
                     stringBuilder.append(String.format("%d, %d, %d\n", x + distance, y + distance, z + distance));
                  }
               }
            }
         }

         String path = TinyFileDialogs.tinyfd_saveFileDialog("Save data", null, this.filters, null);
         if (path == null) {
            throw IO_EXCEPTION.create();
         } else {
            if (!path.endsWith(".txt")) {
               path = path + ".txt";
            }

            try {
               FileWriter file = new FileWriter(path);
               file.write(stringBuilder.toString().trim());
               file.close();
               return 1;
            } catch (IOException var8) {
               throw IO_EXCEPTION.create();
            }
         }
      }));
   }
}
