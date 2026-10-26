package cn.blockforge.meteorzhcn.util;

import cn.blockforge.meteorzhcn.MeteorZhCn;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class ChatPrefix {
   public static final String NAME = "XC I18n";
   private static final String PACKAGE = "cn.blockforge.meteorzhcn";
   private static final Component COMPONENT = Component.empty()
      .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
      .append(Component.literal("XC I18n").withStyle(ChatFormatting.AQUA))
      .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
   private static boolean installed;

   private ChatPrefix() {
   }

   public static synchronized void install() {
      if (!installed) {
         if (ChatUtils.getMeteorPrefix() != null) {
            try {
               ChatUtils.registerCustomPrefix("cn.blockforge.meteorzhcn", () -> COMPONENT);
               installed = true;
            } catch (Throwable var1) {
               MeteorZhCn.LOG.warn("注册聊天前缀失败，继续沿用 Meteor 默认前缀", var1);
            }
         }
      }
   }
}
