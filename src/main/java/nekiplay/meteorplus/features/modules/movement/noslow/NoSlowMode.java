package nekiplay.meteorplus.features.modules.movement.noslow;

import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import nekiplay.main.events.PlayerUseMultiplierEvent;
import net.minecraft.client.Minecraft;

public class NoSlowMode {
   protected final Minecraft mc;
   protected final NoSlowPlus settings = (NoSlowPlus)Modules.get().get(NoSlowPlus.class);
   private final NoSlowModes type;

   public NoSlowMode(NoSlowModes type) {
      this.mc = Minecraft.getInstance();
      this.type = type;
   }

   public void onUse(PlayerUseMultiplierEvent event) {
   }

   public void onTickEventPre(Pre event) {
   }

   public void onActivate() {
   }
}
