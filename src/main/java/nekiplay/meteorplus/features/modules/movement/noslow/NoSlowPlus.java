package nekiplay.meteorplus.features.modules.movement.noslow;

import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.main.events.PlayerUseMultiplierEvent;
import nekiplay.meteorplus.features.modules.movement.noslow.modes.Grim;
import nekiplay.meteorplus.features.modules.movement.noslow.modes.GrimNew;
import nekiplay.meteorplus.features.modules.movement.noslow.modes.Matrix;
import nekiplay.meteorplus.features.modules.movement.noslow.modes.NCPStrict;
import nekiplay.meteorplus.features.modules.movement.noslow.modes.Vanilla;

public class NoSlowPlus extends Module {
   public SettingGroup defaultGroup = this.settings.getDefaultGroup();
   private NoSlowMode currentMode;
   public final Setting<NoSlowModes> mode = this.defaultGroup
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("The method of applying no slow."))
                     .defaultValue(NoSlowModes.Vanilla))
                  .onModuleActivated(spiderModesSetting -> this.onModeChanged((NoSlowModes)spiderModesSetting.get())))
               .onChanged(this::onModeChanged))
            .build()
      );
   public SettingGroup usingItemGroup = this.settings.createGroup("Using item");
   public SettingGroup sneakGroup = this.settings.createGroup("Sneak");
   public SettingGroup otherGroup = this.settings.createGroup("Other");
   public final Setting<Double> usingForward = this.usingItemGroup
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("forward-multiplier"))
               .defaultValue(1.0)
               .min(0.2)
               .sliderRange(0.2, 1.0)
               .visible(() -> this.mode.get() != NoSlowModes.Matrix))
            .build()
      );
   public final Setting<Double> usingSideways = this.usingItemGroup
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("sideways-multiplier"))
               .defaultValue(1.0)
               .min(0.2)
               .sliderRange(0.2, 1.0)
               .visible(() -> this.mode.get() != NoSlowModes.Matrix))
            .build()
      );
   public final Setting<Double> sneakForward = this.sneakGroup
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("forward-multiplier"))
               .defaultValue(1.0)
               .min(0.2)
               .sliderRange(0.2, 1.0)
               .visible(() -> this.mode.get() != NoSlowModes.Matrix))
            .build()
      );
   public final Setting<Double> sneakSideways = this.sneakGroup
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("sideways-multiplier"))
               .defaultValue(1.0)
               .min(0.2)
               .sliderRange(0.2, 1.0)
               .visible(() -> this.mode.get() != NoSlowModes.Matrix))
            .build()
      );
   public final Setting<Double> otherForward = this.otherGroup
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("forward-multiplier"))
               .defaultValue(1.0)
               .min(0.2)
               .sliderRange(0.2, 1.0)
               .visible(() -> this.mode.get() != NoSlowModes.Matrix))
            .build()
      );
   public final Setting<Double> otherSideways = this.otherGroup
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("sideways-multiplier"))
               .defaultValue(1.0)
               .min(0.2)
               .sliderRange(0.2, 1.0)
               .visible(() -> this.mode.get() != NoSlowModes.Matrix))
            .build()
      );

   public NoSlowPlus() {
      super(Categories.Movement, "no-slow+", "Remove or increase slowness.");
   }

   private void onModeChanged(NoSlowModes mode) {
      switch (mode) {
         case Vanilla:
            this.currentMode = new Vanilla();
            break;
         case NCP_Strict:
            this.currentMode = new NCPStrict();
            break;
         case Grim_1dot8:
            this.currentMode = new Grim();
            break;
         case Grim_New:
            this.currentMode = new GrimNew();
            break;
         case Matrix:
            this.currentMode = new Matrix();
      }
   }

   @EventHandler
   private void onUse(PlayerUseMultiplierEvent event) {
      this.currentMode.onUse(event);
   }

   @EventHandler
   private void onTickEventPre(Pre event) {
      this.currentMode.onTickEventPre(event);
   }

   public void onActivate() {
      this.currentMode.onActivate();
   }
}
