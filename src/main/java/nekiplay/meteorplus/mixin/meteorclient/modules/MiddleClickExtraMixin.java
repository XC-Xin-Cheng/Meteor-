package nekiplay.meteorplus.mixin.meteorclient.modules;

import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.player.MiddleClickExtra;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {MiddleClickExtra.class},
   remap = false
)
public class MiddleClickExtraMixin extends Module {
   @Final
   @Shadow
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   @Unique
   private final Setting<Boolean> noInventory = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("Anti-inventory")).description("Not work in inventory.")).defaultValue(true)).build());

   public MiddleClickExtraMixin() {
      super(Categories.Player, "middle-click-extra", "Perform various actions when you middle click.");
   }

   @Inject(
      method = {"onMouseClick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onMouseClick(MouseClickEvent event, CallbackInfo ci) {
      if (event.action == KeyAction.Press && event.button() == 2 && this.mc.screen == null) {
         if (event.action != KeyAction.Press || event.button() != 2) {
            return;
         }

         if ((Boolean)this.noInventory.get() && this.mc.screen != null) {
            ci.cancel();
         }
      }
   }
}
