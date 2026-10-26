package nekiplay.meteorplus.mixin.minecraft;

import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({NormalCraftingRecipe.class})
public interface ShapelessRecipeAccessor {
   @Accessor("bookInfo")
   CraftingBookInfo getBookInfo();
}
