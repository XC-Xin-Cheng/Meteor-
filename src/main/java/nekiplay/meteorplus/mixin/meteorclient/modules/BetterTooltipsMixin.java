package nekiplay.meteorplus.mixin.meteorclient.modules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import meteordevelopment.meteorclient.events.render.TooltipDataEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips.DisplayWhen;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.EChestMemory;
import meteordevelopment.meteorclient.utils.tooltip.ContainerTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.TextTooltipComponent;
import nekiplay.meteorplus.utils.ColorRemover;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(
   value = {BetterTooltips.class},
   priority = 1001
)
public class BetterTooltipsMixin extends Module {
   @Shadow
   @Final
   private Setting<Keybind> keybind;
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgPreviews = this.settings.getGroup("Previews");
   public final Setting<Boolean> echest = this.sgPreviews.get("echests");
   private final Setting<DisplayWhen> displayWhen = this.sgGeneral.get("display-when");

   public BetterTooltipsMixin(Category category, String name, String description, String... aliases) {
      super(category, name, description, aliases);
   }

   @ModifyArgs(
      method = {"appendTooltip"},
      at = @At(
         value = "INVOKE",
         target = "Lmeteordevelopment/meteorclient/events/game/ItemStackTooltipEvent;appendStart(Lnet/minecraft/network/chat/Component;)V"
      )
   )
   private void appendTooltipStart(Args args) {
      Component text = (Component)args.get(0);
      String str = ColorRemover.GetVerbatim(text.getString());
      if (str.startsWith("Honey level:")) {
         Pattern pattern = Pattern.compile("Honey level: (.*)");
         Matcher matcher = pattern.matcher(str);
         if (matcher.find()) {
            String val = matcher.group(1).replaceAll("\\.", "");
            args.set(0, Component.literal(I18n.get("modules.meteor-client.better-tooltips.beehive.honey-level", new Object[]{val})));
         }
      } else if (str.startsWith("Bees:")) {
         Pattern pattern = Pattern.compile("Bees: (.*)");
         Matcher matcher = pattern.matcher(str);
         if (matcher.find()) {
            String val = matcher.group(1).replaceAll("\\.", "");
            args.set(0, Component.literal(I18n.get("modules.meteor-client.better-tooltips.beehive.bees", new Object[]{val})));
         }
      }
   }

   @ModifyArgs(
      method = {"appendTooltip"},
      at = @At(
         value = "INVOKE",
         target = "Lmeteordevelopment/meteorclient/events/game/ItemStackTooltipEvent;appendEnd(Lnet/minecraft/network/chat/Component;)V"
      )
   )
   private void appendTooltipEnd(Args args) {
      Component text = (Component)args.get(0);
      String str = ColorRemover.GetVerbatim(text.getString());
      if (str.endsWith("kb")) {
         Pattern pattern = Pattern.compile("(.*) kb");
         Matcher matcher = pattern.matcher(str);
         if (matcher.find()) {
            String val = matcher.group(1);
            args.set(0, Component.literal(I18n.get("modules.meteor-client.better-tooltips.kilobytes", new Object[]{val})));
         }
      } else if (str.endsWith("bytes")) {
         Pattern pattern = Pattern.compile("(.*) bytes");
         Matcher matcher = pattern.matcher(str);
         if (matcher.find()) {
            String val = matcher.group(1);
            args.set(0, Component.literal(I18n.get("modules.meteor-client.better-tooltips.bytes", new Object[]{val})));
         }
      } else if (str.equals("Error getting bytes.")) {
         args.set(0, Component.literal(I18n.get("modules.meteor-client.better-tooltips.error-getting-bytes", new Object[0])));
      }
   }

   @ModifyArgs(
      method = {"appendPreviewTooltipText"},
      at = @At(
         value = "INVOKE",
         target = "Lmeteordevelopment/meteorclient/events/game/ItemStackTooltipEvent;appendEnd(Lnet/minecraft/network/chat/Component;)V"
      )
   )
   private void appendPreviewTooltipTextEnd(Args args) {
      Component text = (Component)args.get(0);
      String str = ColorRemover.GetVerbatim(text.getString());
      if (str.endsWith("to preview")) {
         args.set(0, Component.literal(I18n.get("modules.meteor-client.better-tooltips.hold-to-preview", new Object[]{this.keybind})));
      }
   }

   @Inject(
      method = {"getTooltipData"},
      at = {@At("RETURN")},
      remap = false
   )
   private void getTooltipData(TooltipDataEvent event, CallbackInfo ci) {
      if (event.itemStack.getItem() == Items.ENDER_CHEST && this.previewEChest()) {
         event.tooltipData = (TooltipComponent)(EChestMemory.isKnown()
            ? new ContainerTooltipComponent((ItemStack[])EChestMemory.ITEMS.toArray(new ItemStack[27]), BetterTooltips.ECHEST_COLOR)
            : new TextTooltipComponent(Component.literal(I18n.get("modules.meteor-client.better-tooltips.unknown-inventory", new Object[0]))));
      }
   }

   @Shadow
   private boolean previewEChest() {
      return this.isPressed() && (Boolean)this.echest.get();
   }

   @Shadow
   private boolean isPressed() {
      return ((Keybind)this.keybind.get()).isPressed() && this.displayWhen.get() == DisplayWhen.Keybind || this.displayWhen.get() == DisplayWhen.Always;
   }
}
