package cn.blockforge.meteorzhcn.util;

import meteordevelopment.meteorclient.systems.modules.Module;

public class KeyBuilder {
   StringBuilder sb = new StringBuilder();

   public KeyBuilder(Module m) {
      this.append("meteor").append(TransUtil.getAddonName(m)).append(TransUtil.baseFormat(m.category.name)).append(TransUtil.baseFormat(m.name));
   }

   public KeyBuilder() {
      this.append("meteor");
   }

   public KeyBuilder reset() {
      this.sb.setLength(0);
      this.append("meteor");
      return this;
   }

   public KeyBuilder module(Module m) {
      this.append(TransUtil.getAddonName(m)).append(TransUtil.baseFormat(m.category.name)).append(TransUtil.baseFormat(m.name));
      return this;
   }

   public KeyBuilder append(String s) {
      this.sb.append(s).append(".");
      return this;
   }

   public KeyBuilder appendWithFormat(String s) {
      this.sb.append(TransUtil.baseFormat(s)).append(".");
      return this;
   }

   public String end(String s) {
      return this.sb.append(s).toString();
   }

   public String endWithFormat(String s) {
      return this.sb.append(TransUtil.baseFormat(s)).toString();
   }
}
