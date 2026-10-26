package anticope.rejects.mixin.meteor.modules;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.world.AutoSign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {AutoSign.class},
   remap = false
)
public class AutoSignMixin extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   @Shadow
   private String[] text;
   private final Setting<Boolean> random = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("random")).description("Spams trash text to make people lag.")).defaultValue(false)).build());
   private final Setting<Integer> length = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("random-length"))
                  .description("Random character length."))
               .defaultValue(500))
            .min(1)
            .sliderMax(1000)
            .build()
      );

   public AutoSignMixin(Category category, String name, String description) {
      super(category, name, description);
   }

   @Inject(
      method = {"onOpenScreen"},
      at = {@At(
         value = "INVOKE",
         target = "Lmeteordevelopment/meteorclient/mixin/AbstractSignEditScreenAccessor;getSign()Lnet/minecraft/block/entity/SignBlockEntity;"
      )}
   )
   private void beforeGetSign(OpenScreenEvent event, CallbackInfo info) {
      if ((Boolean)this.random.get()) {
         this.text = new String[4];
         IntStream.range(0, this.text.length).forEach(i -> {
            IntStream chars = new Random().ints(0, 1114111);
            int amount = (Integer)this.length.get();
            this.text[i] = chars.limit(amount * 5L).mapToObj(i1 -> String.valueOf((char)i1)).collect(Collectors.joining());
         });
      }
   }
}
