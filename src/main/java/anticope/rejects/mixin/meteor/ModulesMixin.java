package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.RejectsConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Modules.class})
public class ModulesMixin {
   @Shadow(
      remap = false
   )
   @Final
   private Map<Category, List<Module>> groups;

   @Inject(
      method = {"getGroup"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void onGetGroup(Category category, CallbackInfoReturnable<List<Module>> cir) {
      Set<String> hiddenModules = RejectsConfig.get().hiddenModules;
      if (!hiddenModules.isEmpty()) {
         List<Module> foundModules = this.groups.computeIfAbsent(category, category1 -> new ArrayList<>());
         foundModules.removeIf(m -> hiddenModules.contains(m.name));
         cir.setReturnValue(foundModules);
      }
   }
}
