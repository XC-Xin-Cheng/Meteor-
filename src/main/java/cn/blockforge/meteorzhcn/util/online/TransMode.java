package cn.blockforge.meteorzhcn.util.online;

public enum TransMode {
   LOCAL("本地词库"),
   MACHINE("联网机翻（可选服务商）"),
   COLLOQUIAL("联网口语化（需 API Key）");

   private final String label;

   private TransMode(String label) {
      this.label = label;
   }

   public boolean isOnline() {
      return this != LOCAL;
   }

   @Override
   public String toString() {
      return this.label;
   }
}
