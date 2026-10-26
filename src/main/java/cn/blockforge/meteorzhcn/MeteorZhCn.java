package cn.blockforge.meteorzhcn;

import cn.blockforge.meteorzhcn.modules.Translation;
import cn.blockforge.meteorzhcn.settings.StringSelectSetting;
import cn.blockforge.meteorzhcn.settings.StringSelectWidgetFactory;
import cn.blockforge.meteorzhcn.util.ChatPrefix;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class MeteorZhCn extends MeteorAddon {
   public static final Logger LOG = LogUtils.getLogger();
   public static final Category CATEGORY = new Category("XC Meteor翻译");

   public void onInitialize() {
      SettingsWidgetFactory.registerCustomFactory(StringSelectSetting.class, StringSelectWidgetFactory::new);
      ChatPrefix.install();
      Modules.get().add(new Translation());
   }

   public void onRegisterCategories() {
      Modules.registerCategory(CATEGORY);
   }

   public String getPackage() {
      return "cn.blockforge.meteorzhcn";
   }
}
