package nekiplay.meteorplus.features.modules.misc;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

public class ChatPrefix extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<String> prefix = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("prefix")).description("Which prefix to be displayed for Meteor+ modules."))
                  .defaultValue("Meteor+"))
               .onChanged(reload -> this.setPrefixes()))
            .build()
      );
   private final Setting<SettingColor> prefixColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("color"))
               .description("Which color to use for the prefix."))
            .defaultValue(new SettingColor(0, 220, 4, 255))
            .build()
      );

   public ChatPrefix() {
      super(Categories.Misc, "meteor+-chat-prefix", "prefix for enabling and disabling Meteor+ modules.");
   }

   public void onActivate() {
      this.setPrefixes();
   }

   public void onDeactivate() {
      ChatUtils.unregisterCustomPrefix("nekiplay.meteorplus.features.modules");
   }

   public void setPrefixes() {
      if (this.isActive()) {
         ChatUtils.registerCustomPrefix("nekiplay.meteorplus.features.modules", this::getPrefix);
      }
   }

   public Component getPrefix() {
      MutableComponent value = Component.literal((String)this.prefix.get());
      MutableComponent prefix = Component.literal("");
      value.setStyle(value.getStyle().withColor(TextColor.fromRgb(((SettingColor)this.prefixColor.get()).getPacked())));
      prefix.setStyle(prefix.getStyle().applyFormat(ChatFormatting.GRAY)).append(Component.literal("[")).append(value).append(Component.literal("] "));
      return prefix;
   }
}
