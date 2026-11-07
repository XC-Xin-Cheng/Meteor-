package meteoraddonppp;

import java.lang.reflect.Field;
import java.util.List;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MeteorAddonPPP extends MeteorAddon {
    public static final String MOD_ID = "meteor-addon-ppp";
    public static final String DISPLAY_NAME = "Meteor Addon+++";
    public static final Category CATEGORY = Compat.category("Meteor+++");

    /** 全部收录组件（含纯主题组件）：标题致谢合并用。 */
    public static final String[] BUNDLED_PACKAGES = {
        "anticope.rejects",
        "com.kkllffaa.meteor_litematica_printer",
        "nekiplay.meteorplus",
        "fr.crazycat256.satellite",
        "zgoly.meteorist",
        "cn.blockforge.meteorzhcn",
        "com.stash.hunt",
        "dev.stardust",
        "me.noramibu.tweaks",
        "spigey.asteroide",
        "me.pindour.catppuccin",
        "com.julflips.nerv_printer",
        "hyperglide"
    };

    /** 模块会被移入 Meteor+++ 分类的组件（纯主题组件除外）。 */
    public static final String[] MOVED_PACKAGES = {
        "anticope.rejects", "com.kkllffaa.meteor_litematica_printer", "fr.crazycat256.satellite",
        "zgoly.meteorist", "cn.blockforge.meteorzhcn", "com.stash.hunt",
        "dev.stardust", "me.noramibu.tweaks", "spigey.asteroide",
        "com.julflips.nerv_printer", "hyperglide"
    };

    /** 组件显示名与包前缀：{显示名, 包前缀}。显示名保持英文原名，不做汉化。 */
    public static final String[][] COMPONENTS = {
        {"Meteor Rejects", "anticope.rejects"},
        {"Meteor Litematica Printer", "com.kkllffaa.meteor_litematica_printer"},
        {"Meteor+", "nekiplay.meteorplus"},
        {"Meteor Satellite Addon", "fr.crazycat256.satellite"},
        {"Meteorist", "zgoly.meteorist"},
        {"Meteor-I18n-XC", "cn.blockforge.meteorzhcn"},
        {"Jefff's Mods", "com.stash.hunt"},
        {"Stardust", "dev.stardust"},
        {"Nora Tweaks", "me.noramibu.tweaks"},
        {"Asteroide", "spigey.asteroide"},
        {"Nerv Printer", "com.julflips.nerv_printer"},
        {"Hyperglide", "hyperglide"}
    };



    /** 需要从分类栏移除的组件旧分类：{addon类全名, 分类静态字段}。类不存在时静默跳过。 */
    private static final String[][] OLD_CATEGORY_FIELDS = {
        {"anticope.rejects.MeteorRejectsAddon", "CATEGORY"},
        {"com.kkllffaa.meteor_litematica_printer.Addon", "CATEGORY"},
        {"fr.crazycat256.satellite.Addon", "CATEGORY"},
        {"zgoly.meteorist.Meteorist", "CATEGORY"},
        {"zgoly.meteorist.Meteorist", "DEV_CATEGORY"},
        {"cn.blockforge.meteorzhcn.MeteorZhCn", "CATEGORY"},
        {"com.stash.hunt.Addon", "CATEGORY"},
        {"dev.stardust.Stardust", "CATEGORY"},
        {"me.noramibu.tweaks.NoraTweaks", "CATEGORY"},
        {"spigey.asteroide.AsteroideAddon", "CATEGORY"},
        {"com.julflips.nerv_printer.Addon", "CATEGORY"},
        {"hyperglide.Hyperglide", "CATEGORY"}
    };

    /** 标题致谢恢复组件原名：{addon类全名, 显示名}。 */
    private static final String[][] ORIGINAL_NAMES = {
        {"anticope.rejects.MeteorRejectsAddon", "Meteor Rejects"},
        {"com.kkllffaa.meteor_litematica_printer.Addon", "Meteor Litematica Printer"},
        {"nekiplay.meteorplus.MeteorPlusAddon", "Meteor+"},
        {"fr.crazycat256.satellite.Addon", "Meteor Satellite Addon"},
        {"zgoly.meteorist.Meteorist", "Meteorist"},
        {"cn.blockforge.meteorzhcn.MeteorZhCn", "Meteor-I18n-XC"},
        {"com.stash.hunt.Addon", "Jefff's Mods"},
        {"dev.stardust.Stardust", "Stardust"},
        {"me.noramibu.tweaks.NoraTweaks", "Nora Tweaks"},
        {"spigey.asteroide.AsteroideAddon", "Asteroide"},
        {"me.pindour.catppuccin.CatppuccinAddon", "Catppuccin Addon"},
        {"com.julflips.nerv_printer.Addon", "Nerv Printer"},
        {"hyperglide.Hyperglide", "Hyperglide"}
    };

    public void onInitialize() {
        // 组件的模块此时已全部注册（本 addon 在 entrypoints 里排最后）
        Module visibility = new ComponentVisibility();
        Modules.get().add(visibility);
        Modules.get().sortModules();
        // 「设置」固定置顶：直接重排分组列表，不依赖标题排序
        List<Module> group = Modules.get().getGroup(CATEGORY);
        if (group != null) {
            group.remove(visibility);
            group.add(0, visibility);
        }

        int total = -1;
        int inCat = -1;
        try {
            total = Modules.get().getAll().size();
            inCat = 0;
            for (Module module : Modules.get().getAll()) {
                if (module.category == CATEGORY) inCat++;
            }
        } catch (Throwable ignored) {
        }
        MeteorClient.LOG.info("[{}] {} modules loaded, {} of them in category '{}'.", DISPLAY_NAME, total, inCat, "Meteor+++");
    }

    public void onRegisterCategories() {
        restoreOriginalAddonNames();
        Modules.registerCategory(CATEGORY);
        List<Category> categories = getCategories();
        if (categories != null) {
            categories.remove(CATEGORY);
            categories.add(0, CATEGORY);

            for (String[] field : OLD_CATEGORY_FIELDS) {
                Category old = readCategory(field[0], field[1]);
                if (old != null) categories.remove(old);
            }
            // Nora-Tweaks 在其分类为空时会自动添加示例分类，移除之
            categories.removeIf(c -> "Example Category".equals(c.name));
        }
    }

    private static List<Category> getCategories() {
        try {
            Field field = Modules.class.getDeclaredField("CATEGORIES");
            field.setAccessible(true);
            return (List<Category>) field.get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Category readCategory(String className, String fieldName) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.getField(fieldName).get(null) instanceof Category category) {
                return category;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void restoreOriginalAddonNames() {
        for (MeteorAddon addon : AddonManager.ADDONS) {
            String className = addon.getClass().getName();
            for (String[] entry : ORIGINAL_NAMES) {
                if (entry[0].equals(className)) {
                    addon.name = entry[1];
                    break;
                }
            }
        }
    }

    public static boolean movesToMainCategory(String className) {
        for (String prefix : MOVED_PACKAGES) {
            if (className.startsWith(prefix)) return true;
        }
        return false;
    }

    public static boolean isBundledComponent(MeteorAddon addon) {
        if (addon == null) return false;
        String pkg = addon.getPackage();
        if (pkg == null) return false;
        for (String bundled : BUNDLED_PACKAGES) {
            if (bundled.equals(pkg)) return true;
        }
        return false;
    }

    public String getPackage() {
        return "meteoraddonppp";
    }

    public String getWebsite() {
        return null;
    }

    public GithubRepo getRepo() {
        return null;
    }

    public String getCommit() {
        return null;
    }
}
