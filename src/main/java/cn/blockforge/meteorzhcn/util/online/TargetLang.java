package cn.blockforge.meteorzhcn.util.online;

import java.util.ArrayList;
import java.util.List;

public enum TargetLang {
   ZH_CN("简体中文", "zh-CN", "zh-CN", "zh", "zh-CHS", "ZH-HANS", "zh-Hans", "zh", "zh-CN", "zh", "简体中文（Simplified Chinese）"),
   ZH_TW("繁体中文", "zh-TW", "zh-TW", "cht", "zh-CHT", "ZH-HANT", "zh-Hant", "zh", "zh-TW", "zt", "繁体中文（Traditional Chinese）"),
   JA("日语（日本語）", "ja-JP", "ja", "jp", "ja", "JA", "ja", "ja", "ja", "ja", "日语（Japanese）"),
   KO("韩语（한국어）", "ko-KR", "ko", "kor", "ko", "KO", "ko", "ko", "ko", "ko", "韩语（Korean）"),
   RU("俄语（Русский）", "ru-RU", "ru", "ru", "ru", "RU", "ru", "ru", "ru", "ru", "俄语（Russian）"),
   DE("德语（Deutsch）", "de-DE", "de", "de", "de", "DE", "de", "de", "de", "de", "德语（German）"),
   FR("法语（Français）", "fr-FR", "fr", "fra", "fr", "FR", "fr", "fr", "fr", "fr", "法语（French）"),
   ES("西班牙语（Español）", "es-ES", "es", "spa", "es", "ES", "es", "es", "es", "es", "西班牙语（Spanish）"),
   PT("葡萄牙语（Português）", "pt-PT", "pt", "pt", "pt", "PT-BR", "pt", "pt", null, "pt", "葡萄牙语（Portuguese）"),
   IT("意大利语（Italiano）", "it-IT", "it", "it", "it", "IT", "it", "it", "it", "it", "意大利语（Italian）"),
   AR("阿拉伯语（العربية）", "ar-SA", "ar", "ara", "ar", null, "ar", "ar", null, "ar", "阿拉伯语（Arabic）"),
   TH("泰语（ไทย）", "th-TH", "th", "th", "th", null, "th", "th", "th", "th", "泰语（Thai）"),
   VI("越南语（Tiếng Việt）", "vi-VN", "vi", "vie", "vi", null, "vi", "vi", "vi", "vi", "越南语（Vietnamese）"),
   ID("印尼语（Bahasa Indonesia）", "id-ID", "id", "ind", "id", "ID", "id", "id", "id", "id", "印尼语（Indonesian）"),
   MS("马来语（Bahasa Melayu）", "ms-MY", "ms", "may", "ms", null, "ms", "ms", null, "ms", "马来语（Malay）"),
   TR("土耳其语（Türkçe）", "tr-TR", "tr", "tr", "tr", "TR", "tr", "tr", null, "tr", "土耳其语（Turkish）"),
   PL("波兰语（Polski）", "pl-PL", "pl", "pl", "pl", "PL", "pl", "pl", null, "pl", "波兰语（Polish）"),
   NL("荷兰语（Nederlands）", "nl-NL", "nl", "nl", "nl", "NL", "nl", "nl", null, "nl", "荷兰语（Dutch）"),
   UK("乌克兰语（Українська）", "uk-UA", "uk", "ukr", "uk", "UK", "uk", "uk", null, "uk", "乌克兰语（Ukrainian）"),
   HI("印地语（हिन्दी）", "hi-IN", "hi", "hi", "hi", null, "hi", "hi", null, "hi", "印地语（Hindi）");

   private final String label;
   private final String myMemoryCode;
   private final String googleCode;
   private final String baiduCode;
   private final String youdaoCode;
   private final String deeplCode;
   private final String bingCode;
   private final String yandexCode;
   private final String papagoCode;
   private final String libreCode;
   private final String promptName;

   private TargetLang(
      String label,
      String myMemoryCode,
      String googleCode,
      String baiduCode,
      String youdaoCode,
      String deeplCode,
      String bingCode,
      String yandexCode,
      String papagoCode,
      String libreCode,
      String promptName
   ) {
      this.label = label;
      this.myMemoryCode = myMemoryCode;
      this.googleCode = googleCode;
      this.baiduCode = baiduCode;
      this.youdaoCode = youdaoCode;
      this.deeplCode = deeplCode;
      this.bingCode = bingCode;
      this.yandexCode = yandexCode;
      this.papagoCode = papagoCode;
      this.libreCode = libreCode;
      this.promptName = promptName;
   }

   public String myMemoryCode() {
      return this.myMemoryCode;
   }

   public String googleCode() {
      return this.googleCode;
   }

   public String baiduCode() {
      return this.baiduCode;
   }

   public String youdaoCode() {
      return this.youdaoCode;
   }

   public String deeplCode() {
      return this.deeplCode;
   }

   public String bingCode() {
      return this.bingCode;
   }

   public String yandexCode() {
      return this.yandexCode;
   }

   public String papagoCode() {
      return this.papagoCode;
   }

   public String libreCode() {
      return this.libreCode;
   }

   public String promptName() {
      return this.promptName;
   }

   public boolean needsTranslationFromChinese() {
      return this != ZH_CN && this != ZH_TW;
   }

   public String codeFor(MachineProvider provider) {
      return switch (provider) {
         case MYMEMORY -> this.myMemoryCode;
         case GOOGLE -> this.googleCode;
         case BAIDU -> this.baiduCode;
         case YOUDAO -> this.youdaoCode;
         case DEEPL -> this.deeplCode;
         case MICROSOFT -> this.bingCode;
         case YANDEX -> this.yandexCode;
         case PAPAGO -> this.papagoCode;
         case LIBRETRANSLATE -> this.libreCode;
         default -> this.myMemoryCode;
      };
   }

   public boolean supportedBy(MachineProvider provider) {
      return provider.usesTemplate() || this.codeFor(provider) != null;
   }

   public String providerSuggestions() {
      List<String> names = new ArrayList<>();

      for (MachineProvider provider : MachineProvider.values()) {
         if (provider != MachineProvider.QUARK && provider != MachineProvider.CUSTOM && this.supportedBy(provider)) {
            names.add(provider.shortName());
         }
      }

      return String.join("、", names);
   }

   @Override
   public String toString() {
      return this.label;
   }

   public boolean alreadyInTargetScript(String text) {
      if (text == null) {
         return false;
      } else {
         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            boolean hit = switch (this) {
               case ZH_CN, ZH_TW -> isHan(c);
               case JA -> isKana(c);
               case KO -> isHangul(c);
               case RU, UK -> isCyrillic(c);
               default -> false;
               case AR -> isArabic(c);
               case TH -> isThai(c);
               case HI -> isDevanagari(c);
            };
            if (hit) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean isHan(char c) {
      return c >= 19968 && c <= '鿿' || c >= 13312 && c <= 19903 || c >= '豈' && c <= '\ufaff' || c >= 12288 && c <= 12351 || c >= '\uff00' && c <= '\uffef';
   }

   private static boolean isKana(char c) {
      return c >= 12352 && c <= 12447 || c >= 12448 && c <= 12543;
   }

   private static boolean isHangul(char c) {
      return c >= '가' && c <= '힣' || c >= 4352 && c <= 4607 || c >= 12592 && c <= 12687;
   }

   private static boolean isCyrillic(char c) {
      return c >= 1024 && c <= 1279 || c >= 1280 && c <= 1327;
   }

   private static boolean isArabic(char c) {
      return c >= 1536 && c <= 1791 || c >= 1872 && c <= 1919 || c >= 'ﭐ' && c <= '﷿';
   }

   private static boolean isThai(char c) {
      return c >= 3584 && c <= 3711;
   }

   private static boolean isDevanagari(char c) {
      return c >= 2304 && c <= 2431 || c >= '꣠' && c <= 'ꣿ';
   }
}
