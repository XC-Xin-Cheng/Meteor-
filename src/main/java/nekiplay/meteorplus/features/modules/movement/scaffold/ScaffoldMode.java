package nekiplay.meteorplus.features.modules.movement.scaffold;

import meteordevelopment.meteorclient.systems.modules.Modules;
import nekiplay.main.events.PlayerUseMultiplierEvent;
import net.minecraft.client.Minecraft;

public class ScaffoldMode {
   protected final Minecraft mc;
   protected final ScaffoldPlus settings = (ScaffoldPlus)Modules.get().get(ScaffoldPlus.class);
   private final ScaffoldModes type;

   public ScaffoldMode(ScaffoldModes type) {
      this.mc = Minecraft.getInstance();
      this.type = type;
   }

   public void onUse(PlayerUseMultiplierEvent event) {
   }
}
