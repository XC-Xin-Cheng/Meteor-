package zgoly.meteorist.mixin;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.entity.player.StackedContents.IngredientInfo;
import net.minecraft.world.entity.player.StackedContents.Output;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({StackedItemContents.class})
public interface RecipeFinderAccessor {
   @Invoker("canCraft")
   boolean invokeIsCraftable(List<? extends IngredientInfo<Holder<Item>>> var1, int var2, @Nullable Output<Holder<Item>> var3);
}
