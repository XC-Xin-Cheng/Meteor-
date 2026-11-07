package meteoraddonppp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteoraddonppp.mixin.ModuleTitleAccessor;

/**
 * 「Meteor+++ 设置」：按模组整组显示/隐藏收录组件的全部模块。
 * 隐藏 = 把模块从其所在分类的显示列表里摘除（模块保持注册、功能不受影响）；
 * 显示 = 放回列表。组件显示名保持英文原名，不做汉化。
 */
public class ComponentVisibility extends Module {
    private static ComponentVisibility instance;
    private final Map<String, Setting<Boolean>> toggles = new HashMap<>();

    public ComponentVisibility() {
        super(MeteorAddonPPP.CATEGORY, "meteor-addon-ppp-settings", "", new String[0]);
        ModuleTitleAccessor accessor = (ModuleTitleAccessor) this;
        accessor.setTitle(" Meteor+++ \u200B设置");
        accessor.setDescription("按模组整组显示或隐藏收录组件的全部模块，改动在重新打开 GUI 后生效。");
        instance = this;

        SettingGroup group = settings.getDefaultGroup();
        for (String[] component : MeteorAddonPPP.COMPONENTS) {
            String label = component[0];
            String pkg = component[1];
            Setting<Boolean> toggle = new BoolSetting.Builder()
                .name("显示 " + label)
                .description("在模块列表中显示或隐藏 " + label + " 的全部模块。")
                .defaultValue(true)
                .onChanged(value -> apply())
                .build();
            group.add(toggle);
            toggles.put(pkg, toggle);
        }
    }

    private boolean isVisibleFor(String className) {
        for (String[] component : MeteorAddonPPP.COMPONENTS) {
            if (className.startsWith(component[1])) {
                Setting<Boolean> toggle = toggles.get(component[1]);
                return toggle == null || toggle.get();
            }
        }
        return true;
    }

    /** 按当前开关整体重建 Meteor+++ 显示列表（确定性自愈）；幂等，可在任意时机调用。 */
    public static void apply() {
        if (instance == null) return;
        Modules modules = Modules.get();
        Collection<Module> all = modules.getAll();
        if (all == null) return;

        // Meteor+++ 主列表：清空后按当前开关重建，无论之前处于什么状态都能恢复一致
        List<Module> group = modules.getGroup(MeteorAddonPPP.CATEGORY);
        if (group == null) return;
        List<Module> shouldShow = new ArrayList<>();
        // 「设置」模块固定第一，不依赖任何排序
        if (instance != null) {
            shouldShow.add(instance);
        }
        for (Module module : all.toArray(new Module[0])) {
            String className = module.getClass().getName();
            if (className.startsWith("meteoraddonppp.")) continue;
            if (MeteorAddonPPP.movesToMainCategory(className) && instance.isVisibleFor(className)) {
                shouldShow.add(module);
            }
        }
        group.clear();
        group.addAll(shouldShow);

        // Meteor+ 模块保持原分类：隐藏时从其所在分类列表移除，显示时放回
        int plusHidden = 0;
        for (Module module : all.toArray(new Module[0])) {
            String className = module.getClass().getName();
            if (!className.startsWith("nekiplay.meteorplus")) continue;
            List<Module> g = modules.getGroup(module.category);
            if (g == null) continue;
            if (instance.isVisibleFor(className)) {
                if (!g.contains(module)) g.add(module);
            } else {
                g.remove(module);
                plusHidden++;
            }
        }

        MeteorClient.LOG.info("[{}] visibility applied: {} modules in '{}', Meteor+ hidden {}.",
            MeteorAddonPPP.DISPLAY_NAME, shouldShow.size(), "Meteor+++", plusHidden);
    }
}
