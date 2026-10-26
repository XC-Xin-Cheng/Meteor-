package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.DeathScreen;

public class GhostMode extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> fullFood = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("full-food")).description("Sets the food level client-side to max.")).defaultValue(true)).build());
   private boolean active = false;

   public GhostMode() {
      super(MeteorRejectsAddon.CATEGORY, "ghost-mode", "Allows you to keep playing after you die. Works on Forge, Fabric and Vanilla servers.");
   }

   public void onDeactivate() {
      super.onDeactivate();
      this.active = false;
      this.warning("You are no longer in a ghost mode!", new Object[0]);
      if (this.mc.player != null && this.mc.player.connection != null) {
         this.mc.player.respawn();
         this.info("Respawn request has been sent to the server.", new Object[0]);
      }
   }

   @EventHandler
   private void onGameJoin(GameJoinedEvent event) {
      this.active = false;
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.active) {
         if (this.mc.player.getHealth() < 1.0F) {
            this.mc.player.setHealth(20.0F);
         }

         if ((Boolean)this.fullFood.get() && this.mc.player.getFoodData().getFoodLevel() < 20) {
            this.mc.player.getFoodData().setFoodLevel(20);
         }
      }
   }

   @EventHandler
   private void onOpenScreen(OpenScreenEvent event) {
      if (event.screen instanceof DeathScreen) {
         event.cancel();
         if (!this.active) {
            this.active = true;
            this.info("You are now in a ghost mode. ", new Object[0]);
         }
      }
   }
}
