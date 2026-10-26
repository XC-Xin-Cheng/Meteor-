package nekiplay.meteorplus.mixin.meteorclient.modules;

import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.ItemHighlight;
import nekiplay.meteorplus.features.modules.render.ItemHighlightPlus;
import nekiplay.meteorplus.settings.items.HighlightItemData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ItemHighlight.class})
public class ItemHighlightMixin {
   @Inject(
      method = {"getColor"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void getColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
      if ((Integer)cir.getReturnValue() == -1 && stack != null) {
         ItemHighlightPlus itemHighlightPlus = (ItemHighlightPlus)Modules.get().get(ItemHighlightPlus.class);
         if (itemHighlightPlus != null && itemHighlightPlus.isActive() && ((List)itemHighlightPlus.whitelist.get()).contains(stack.getItem().asItem())) {
            HighlightItemData espItemData = (HighlightItemData)((Map)itemHighlightPlus.itemsConfigs.get()).get(stack.getItem());
            if (espItemData != null) {
               cir.setReturnValue(espItemData.Color.getPacked());
            } else {
               cir.setReturnValue(((HighlightItemData)itemHighlightPlus.defaultBlockConfig.get()).Color.getPacked());
            }
         }
      }
   }
}
