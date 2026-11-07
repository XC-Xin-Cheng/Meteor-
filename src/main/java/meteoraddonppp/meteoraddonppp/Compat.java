package meteoraddonppp;

import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 1.21.x 家族适配：Category 直接收 ItemStack，Module 只有 toggle()。 */
public final class Compat {
    private Compat() {
    }

    public static Category category(String name) {
        return new Category(name, new ItemStack(Items.NETHER_STAR));
    }

    /** 仅在模块未激活时调用（调用方已检查 isActive）。 */
    public static void enableModule(Module module) {
        module.toggle();
    }
}
