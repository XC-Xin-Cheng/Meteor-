package nekiplay.meteorplus.features.modules.integrations;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import nekiplay.MixinPlugin;
import nekiplay.meteorplus.MeteorPlusAddon;

public class MapIntegration extends Module {
   private final SettingGroup baritoneIntegration = this.settings.createGroup("Baritone");
   public final Setting<Boolean> baritoneGoto = this.baritoneIntegration
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("Baritone support")).description("Moving in baritone at the selected location.")).defaultValue(true))
            .build()
      );
   public final Setting<Boolean> baritoneElytra = this.baritoneIntegration
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("Baritone elytra")).description("Elytra to location.")).defaultValue(true)).visible(() -> {
            boolean allow = false;

            for (IBaritone baritone : BaritoneAPI.getProvider().getAllBaritones()) {
               if (!baritone.getCommandManager()
                  .getRegistry()
                  .stream()
                  .filter(a -> ((String)a.getNames().get(0)).equalsIgnoreCase("elytra"))
                  .findAny()
                  .isEmpty()) {
                  allow = true;
                  break;
               }
            }

            if (allow) {
               allow = (Boolean)this.baritoneGoto.get();
            }

            return allow;
         })).build()
      );
   private final SettingGroup fullMap = this.settings.createGroup("Full map");
   public final Setting<Boolean> showBlock = this.fullMap
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("Show block")).description("Shows the name of the block in the clicked position."))
                  .visible(() -> MixinPlugin.isXaeroWorldMapPresent))
               .defaultValue(true))
            .build()
      );

   public MapIntegration() {
      super(MeteorPlusAddon.CATEGORYMODS, "Mini-map", "Improvements for mods on mini-maps.");
   }
}
