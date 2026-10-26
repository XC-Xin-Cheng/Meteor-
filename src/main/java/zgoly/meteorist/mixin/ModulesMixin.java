package zgoly.meteorist.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.util.Tuple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.utils.config.MeteoristConfigScreen;

@Mixin({Modules.class})
public abstract class ModulesMixin {
   @ModifyReturnValue(
      method = {"loopCategories"},
      at = {@At("RETURN")}
   )
   private static Iterable<Category> filterCategories(Iterable<Category> categories) {
      return (Iterable<Category>)(MeteoristConfigScreen.showDevModules.get()
         ? categories
         : StreamSupport.stream(categories.spliterator(), false).filter(category -> category != Meteorist.DEV_CATEGORY).collect(Collectors.toList()));
   }

   @ModifyReturnValue(
      method = {"searchTitles"},
      at = {@At("RETURN")}
   )
   private static List<Tuple<Module, String>> filterSearchTitles(List<Tuple<Module, String>> results) {
      return MeteoristConfigScreen.showDevModules.get()
         ? results
         : results.stream().filter(result -> ((Module)result.getA()).category != Meteorist.DEV_CATEGORY).collect(Collectors.toList());
   }

   @ModifyReturnValue(
      method = {"searchSettingTitles"},
      at = {@At("RETURN")}
   )
   private static Set<Module> filterSearchSettings(Set<Module> results) {
      return MeteoristConfigScreen.showDevModules.get()
         ? results
         : results.stream().filter(module -> module.category != Meteorist.DEV_CATEGORY).collect(Collectors.toSet());
   }
}
