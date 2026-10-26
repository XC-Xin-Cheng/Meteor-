package cn.blockforge.meteorzhcn.util.online;

public enum OnlineScope {
   MODULES("模块名与描述"),
   NAMES("模块名与设置名"),
   ALL("全部（含设置描述）");

   private final String label;

   private OnlineScope(String label) {
      this.label = label;
   }

   @Override
   public String toString() {
      return this.label;
   }
}
