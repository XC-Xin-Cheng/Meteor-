package fr.crazycat256.satellite.mixin;

import fr.crazycat256.satellite.modules.NBTTooltip;
import java.util.List;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ItemStack.class})
public abstract class ItemStackMixin {
   @Inject(
      method = {"getTooltipLines"},
      at = {@At("RETURN")}
   )
   private void getTooltip(TooltipContext tooltipContext, @Nullable Player entity, TooltipFlag tooltipType, CallbackInfoReturnable<List<Component>> info) {
      String nbtStr = ((NBTTooltip)Modules.get().get(NBTTooltip.class)).getTooltip((ItemStack)this, tooltipContext);
      if (nbtStr != null) {
         List<Component> tooltip = (List<Component>)info.getReturnValue();

         for (String line : nbtStr.split("\n")) {
            tooltip.add(Component.literal(line));
         }
      }
   }
}
