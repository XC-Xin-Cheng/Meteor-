package meteoraddonppp;

import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 26.x 家族适配：Category 收 Supplier&lt;ItemStack&gt;，Module 有 enable()。 */
public final class Compat {
    private Compat() {
    }

    public static Category category(String name) {
        return new Category(name, () -> new ItemStack(Items.NETHER_STAR));
    }

    public static void enableModule(Module module) {
        module.enable();
    }
}
