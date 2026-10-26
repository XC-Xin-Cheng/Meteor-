package cn.blockforge.meteorzhcn.modules;

import cn.blockforge.meteorzhcn.MeteorZhCn;
import cn.blockforge.meteorzhcn.mixin.ModuleAccessor;
import cn.blockforge.meteorzhcn.mixin.SettingAccessor;
import cn.blockforge.meteorzhcn.mixin.SettingGroupAccessor;
import cn.blockforge.meteorzhcn.settings.StringSelectSetting;
import cn.blockforge.meteorzhcn.util.JsonDump;
import cn.blockforge.meteorzhcn.util.TransUtil;
import cn.blockforge.meteorzhcn.util.online.MachineProvider;
import cn.blockforge.meteorzhcn.util.online.OnlineScope;
import cn.blockforge.meteorzhcn.util.online.OnlineTranslator;
import cn.blockforge.meteorzhcn.util.online.TargetLang;
import cn.blockforge.meteorzhcn.util.online.TransMode;
import cn.blockforge.meteorzhcn.util.trans_engine.AbstractTransEngine;
import cn.blockforge.meteorzhcn.util.trans_engine.EngineManager;
import java.util.Set;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

public class Translation extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgOnline = this.settings.createGroup("Online Translation");
   public final Setting<Boolean> bSetAutoTranslation = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("auto-translation")).description("打开模块时自动执行一次汉化。")).defaultValue(false)).build());
   public final Setting<Set<String>> translationModules = this.sgGeneral
      .add(
         ((StringSelectSetting.Builder)((StringSelectSetting.Builder)((StringSelectSetting.Builder)new StringSelectSetting.Builder()
                     .validValues(TransUtil.getAddonNames())
                     .defaultValue(TransUtil.getAddonNames()))
                  .name("translation-modules"))
               .description("选择要汉化哪些附加模组，默认全选。"))
            .build()
      );
   public final Setting<TransMode> transMode = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("translation-mode"))
                  .description("本地词库用内置汉化文件；联网机翻可选 MyMemory、Google、DeepL、微软、Yandex、Papago、LibreTranslate、百度、有道等服务商；联网口语化调用大模型，需要自己填 API Key。"))
               .defaultValue(TransMode.LOCAL))
            .build()
      );
   public final Setting<TargetLang> targetLanguage = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("target-language"))
                     .description(
                        "联网翻译翻成哪种语言，默认简体中文。内置的本地词库固定是中文，不受此项影响；机翻会按所选服务商自动换算语言代码，口语化则写进模型的提示词。若某家服务商不支持所选语言（如 DeepL 不支持阿语、泰语，Papago 只覆盖中日韩英与越泰印尼法西俄德意），会在聊天栏列出能用的服务商，不会发出必然报错的请求。"
                     ))
                  .defaultValue(TargetLang.ZH_CN))
               .visible(() -> ((TransMode)this.transMode.get()).isOnline()))
            .build()
      );
   public final Setting<OnlineScope> onlineScope = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("online-scope"))
                     .description("联网翻译翻哪些内容。设置描述数量极大，默认只翻模块名与模块描述。"))
                  .defaultValue(OnlineScope.MODULES))
               .visible(() -> ((TransMode)this.transMode.get()).isOnline()))
            .build()
      );
   public final Setting<Integer> iSetThreads = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("online-threads"))
                     .description("联网翻译的并发请求数，太大容易被接口限流。"))
                  .defaultValue(4))
               .range(1, 8)
               .sliderRange(1, 8)
               .visible(() -> ((TransMode)this.transMode.get()).isOnline()))
            .build()
      );
   public final Setting<Integer> iSetTimeout = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("online-timeout"))
                     .description("单个网络请求的超时秒数。"))
                  .defaultValue(15))
               .range(3, 120)
               .sliderRange(3, 60)
               .visible(() -> ((TransMode)this.transMode.get()).isOnline()))
            .build()
      );
   public final Setting<MachineProvider> machineProvider = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("machine-provider"))
                     .description(
                        "机翻用哪家接口。MyMemory、Google 免 Key，LibreTranslate 可用公共实例或自建实例；DeepL 翻译质量高（俄、日、德等表现好），微软翻译覆盖语种最全，Yandex 擅长俄语，Papago 擅长韩语（只覆盖中日韩英与越泰印尼法西俄德意），这四家需要填 Key；百度翻译、有道智云走国内官方接口，需要 AppID 与密钥；夸克没有公开接口，需要自己填抓到的地址模板。"
                     ))
                  .defaultValue(MachineProvider.MYMEMORY))
               .visible(() -> this.transMode.get() == TransMode.MACHINE))
            .build()
      );
   public final Setting<String> sSetMachineUrl = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("machine-api-url"))
                     .description(
                        "机翻接口地址模板，{text} 会被替换成 URL 编码后的原文，{lang} 会替换成当前目标语言在这次服务商下的代码，{source} 会替换成自动识别出的源语言代码。MyMemory、Google 留空即按目标语言用内置地址；LibreTranslate 留空用公共实例、填了就用自建实例；夸克与自定义必须自己填。例如 Google：https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl={lang}&dt=t&q={text}"
                     ))
                  .defaultValue(""))
               .wide()
               .visible(
                  () -> this.transMode.get() == TransMode.MACHINE
                     && (((MachineProvider)this.machineProvider.get()).usesTemplate() || ((MachineProvider)this.machineProvider.get()).usesServiceUrl())
               ))
            .build()
      );
   public final Setting<String> sSetMachineAppId = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("machine-appid"))
                     .description("百度翻译的 AppID、有道智云的应用 ID、Papago 的 Client ID；微软翻译这里填可选的区域(region)，Yandex 这里填可选的 folderId，留空即由服务端推断。只存在本地配置里。"))
                  .defaultValue(""))
               .wide()
               .visible(() -> this.transMode.get() == TransMode.MACHINE && ((MachineProvider)this.machineProvider.get()).usesAppId()))
            .build()
      );
   public final Setting<String> sSetMachineKey = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("machine-key"))
                     .description("DeepL、微软翻译、Yandex 的 API Key，Papago 的 Client Secret，百度翻译的密钥，或有道智云的应用密钥；LibreTranslate 公共实例现在多要 key，可在同一栏填。只存在本地配置里。"))
                  .defaultValue(""))
               .wide()
               .visible(() -> this.transMode.get() == TransMode.MACHINE && ((MachineProvider)this.machineProvider.get()).acceptsKey()))
            .build()
      );
   public final Setting<String> sSetApiUrl = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("api-url"))
                     .description("口语化翻译的接口地址，兼容 OpenAI 的 /v1/chat/completions 格式。"))
                  .defaultValue("https://api.openai.com/v1/chat/completions"))
               .wide()
               .visible(() -> this.transMode.get() == TransMode.COLLOQUIAL))
            .build()
      );
   public final Setting<String> sSetApiKey = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("api-key"))
                     .description("口语化翻译需要的 API Key，以 Bearer 方式发送。只存在本地配置里。"))
                  .defaultValue(""))
               .wide()
               .visible(() -> this.transMode.get() == TransMode.COLLOQUIAL))
            .build()
      );
   public final Setting<String> sSetModel = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("api-model"))
                     .description("口语化翻译使用的模型名，例如 gpt-4o-mini、deepseek-chat。"))
                  .defaultValue("gpt-4o-mini"))
               .visible(() -> this.transMode.get() == TransMode.COLLOQUIAL))
            .build()
      );
   public final Setting<Integer> iSetBatchSize = this.sgOnline
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("api-batch-size"))
                     .description("口语化翻译每次请求打包多少条文本，太小会变慢，太大会被模型截断。"))
                  .defaultValue(20))
               .range(1, 50)
               .sliderRange(1, 50)
               .visible(() -> this.transMode.get() == TransMode.COLLOQUIAL))
            .build()
      );
   private final SettingGroup sgDev = this.settings.createGroup("Dev", false);
   public final Setting<String> strSetTransEngine = this.sgDev
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                     .defaultValue("AUTO"))
                  .name("translation-engine"))
               .description("使用哪一套汉化键规则。AUTO 会依次尝试新式、旧式、早期三套键写法，自动适配 1.21.1 到 26.2 的词库，正常情况下不用改；NEW / OLD / LEGACY 只在排查键对不上时手动固定。"))
            .build()
      );
   public final Setting<String> sSetDumpPath = this.sgDev
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                     .name("dump-path"))
                  .description("导出键骨架时写到的文件路径。"))
               .defaultValue("meteor-zh-cn-dump.json"))
            .build()
      );
   public final Setting<Boolean> bSetDumpText = this.sgDev
      .add(((Builder)((Builder)((Builder)new Builder().name("dump-text")).description("导出时是否连译文一起写出。")).defaultValue(false)).build());
   public final Setting<String> strSetDumpTextEngine = this.sgDev
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .defaultValue("AUTO"))
                     .visible(this.bSetDumpText::get))
                  .name("dump-text-engine"))
               .description("导出译文时使用哪一套键规则。"))
            .build()
      );
   private boolean isTranslation = false;

   public Translation() {
      super(MeteorZhCn.CATEGORY, "翻译", "把 Meteor 本体与各附加模组的模块名、设置名翻译成中文。");
   }

   public void onActivate() {
      if ((Boolean)this.bSetAutoTranslation.get() && !this.isTranslation) {
         this.isTranslation = true;
         this.tran();
      }
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      WHorizontalList l1 = (WHorizontalList)list.add(theme.horizontalList()).expandX().widget();
      WButton start = (WButton)l1.add(theme.button("Translate")).expandX().widget();
      start.action = () -> {
         if (this.isActive()) {
            this.isTranslation = true;
            this.tran();
         } else {
            ChatUtils.warning("你首先要开启此模块", new Object[0]);
         }
      };
      if (((TransMode)this.transMode.get()).isOnline()) {
         WHorizontalList l2 = (WHorizontalList)list.add(theme.horizontalList()).expandX().widget();
         WLabel cacheLabel = (WLabel)l2.add(theme.label("cache: " + OnlineTranslator.get().cacheSize())).widget();
         cacheLabel.color(theme.textSecondaryColor());
         WButton clear = (WButton)l2.add(theme.button("Clear cache")).expandX().widget();
         clear.action = () -> {
            OnlineTranslator.get().clearCache();
            cacheLabel.set("cache: 0");
         };
      }

      if (!this.sgDev.sectionExpanded) {
         return list;
      } else {
         WHorizontalList l3 = (WHorizontalList)list.add(theme.horizontalList()).expandX().widget();
         WButton dump = (WButton)l3.add(theme.button("Dump")).expandX().widget();
         dump.action = () -> JsonDump.getINSTANCE()
            .write(
               EngineManager.getInstance().getEngine((String)this.strSetTransEngine.get()),
               EngineManager.getInstance().getEngine((String)this.strSetDumpTextEngine.get())
            );
         return list;
      }
   }

   public void tran() {
      TransMode mode = (TransMode)this.transMode.get();
      if (mode.isOnline()) {
         OnlineTranslator.get().start(this, mode);
      } else {
         this.tran(EngineManager.getInstance().getEngine((String)this.strSetTransEngine.get()));
      }
   }

   private void tran(AbstractTransEngine engine) {
      Set<String> selected = (Set<String>)this.translationModules.get();

      for (Module module : Modules.get().getAll()) {
         String addonName = TransUtil.getAddonName(module);
         if (selected.contains(addonName)) {
            String tranName = engine.transModuleName(module);
            if (tranName != null && !tranName.isBlank()) {
               ((ModuleAccessor)module).setTitle(Utils.nameToTitle(tranName));
            }

            String tranDescry = engine.transModuleDescription(module);
            if (tranDescry != null && !tranDescry.isBlank()) {
               ((ModuleAccessor)module).setDescription(Utils.nameToTitle(tranDescry));
            }

            for (SettingGroup group : module.settings.groups) {
               for (Setting<?> setting : ((SettingGroupAccessor)group).getSettings()) {
                  String tranSettName = engine.transSettingName(module, group, setting);
                  if (tranSettName != null && !tranSettName.isBlank()) {
                     ((SettingAccessor)setting).setTitle(Utils.nameToTitle(tranSettName));
                  }

                  String tranSettDesc = engine.transSettingDes(module, group, setting);
                  if (tranSettDesc != null && !tranSettDesc.isBlank()) {
                     ((SettingAccessor)setting).setDescription(Utils.nameToTitle(tranSettDesc));
                  }
               }
            }
         }
      }
   }
}
