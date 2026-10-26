package zgoly.meteorist.utils.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.utils.MeteoristUtils;

public class MeteoristConfig {
   public static void save(String folderName, String fileName, CompoundTag data) {
      try {
         Path basePath = FabricLoader.getInstance().getGameDir().resolve(Meteorist.MOD_ID);
         Path filePath = basePath.resolve(MeteoristUtils.removeInvalidChars(folderName)).resolve(fileName + ".nbt");
         Files.createDirectories(filePath.getParent());
         NbtIo.write(data, filePath);
      } catch (IOException var5) {
         var5.printStackTrace();
      }
   }

   public static CompoundTag load(String folderName, String fileName) {
      try {
         Path basePath = FabricLoader.getInstance().getGameDir().resolve(Meteorist.MOD_ID);
         Path filePath = basePath.resolve(MeteoristUtils.removeInvalidChars(folderName)).resolve(fileName + ".nbt");
         return !Files.exists(filePath) ? new CompoundTag() : NbtIo.read(filePath);
      } catch (IOException var4) {
         var4.printStackTrace();
         return new CompoundTag();
      }
   }
}
