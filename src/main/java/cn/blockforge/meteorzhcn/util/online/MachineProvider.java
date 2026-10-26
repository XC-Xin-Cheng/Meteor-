package cn.blockforge.meteorzhcn.util.online;

public enum MachineProvider {
   MYMEMORY("MyMemory（免费·免 Key）"),
   GOOGLE("Google（免费·免 Key）"),
   LIBRETRANSLATE("LibreTranslate（免费/自建）"),
   DEEPL("DeepL（需 API Key）"),
   MICROSOFT("微软翻译（需 Key）"),
   YANDEX("Yandex（需 Key）"),
   PAPAGO("Papago 韩语（需 Client ID+Secret）"),
   BAIDU("百度翻译（需 AppID+密钥）"),
   YOUDAO("有道智云（需 AppID+密钥）"),
   QUARK("夸克（需自填地址）"),
   CUSTOM("自定义地址模板");

   private final String label;

   private MachineProvider(String label) {
      this.label = label;
   }

   public boolean usesTemplate() {
      return this == MYMEMORY || this == GOOGLE || this == QUARK || this == CUSTOM;
   }

   public boolean usesServiceUrl() {
      return this == LIBRETRANSLATE;
   }

   public boolean requiresAppId() {
      return this == BAIDU || this == YOUDAO || this == PAPAGO;
   }

   public boolean requiresKey() {
      return this == BAIDU || this == YOUDAO || this == PAPAGO || this == DEEPL || this == MICROSOFT || this == YANDEX;
   }

   public boolean usesAppId() {
      return this.requiresAppId() || this == MICROSOFT || this == YANDEX;
   }

   public boolean acceptsKey() {
      return this.requiresKey() || this == LIBRETRANSLATE || this == CUSTOM;
   }

   public boolean needsCustomUrl() {
      return this == QUARK || this == CUSTOM;
   }

   public String defaultUrl() {
      return this == LIBRETRANSLATE ? "https://libretranslate.com/translate" : "";
   }

   public String shortName() {
      int bracket = this.label.indexOf(65288);
      return bracket < 0 ? this.label : this.label.substring(0, bracket);
   }

   @Override
   public String toString() {
      return this.label;
   }
}
