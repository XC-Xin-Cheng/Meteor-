package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.entity.BoatMoveEvent;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public class BoatGlitch extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> toggleAfter = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("toggle-after")).description("Disables the module when finished.")).defaultValue(true)).build());
   private final Setting<Boolean> remount = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("remount")).description("Remounts the boat when finished.")).defaultValue(true)).build());
   private Entity boat = null;
   private int dismountTicks = 0;
   private int remountTicks = 0;
   private boolean dontPhase = true;
   private boolean boatPhaseEnabled;

   public BoatGlitch() {
      super(MeteorRejectsAddon.CATEGORY, "boat-glitch", "Glitches your boat into the block beneath you.  Dismount to trigger.");
   }

   public void onActivate() {
      this.dontPhase = true;
      this.dismountTicks = 0;
      this.remountTicks = 0;
      this.boat = null;
      if (Modules.get().isActive(BoatPhase.class)) {
         this.boatPhaseEnabled = true;
         ((BoatPhase)Modules.get().get(BoatPhase.class)).toggle();
      } else {
         this.boatPhaseEnabled = false;
      }
   }

   public void onDeactivate() {
      if (this.boat != null) {
         this.boat.noPhysics = false;
         this.boat = null;
      }

      if (this.boatPhaseEnabled && !Modules.get().isActive(BoatPhase.class)) {
         ((BoatPhase)Modules.get().get(BoatPhase.class)).toggle();
      }
   }

   @EventHandler
   private void onBoatMove(BoatMoveEvent event) {
      if (this.dismountTicks == 0 && !this.dontPhase) {
         if (this.boat != event.boat) {
            if (this.boat != null) {
               this.boat.noPhysics = false;
            }

            if (this.mc.player.getVehicle() != null && event.boat == this.mc.player.getVehicle()) {
               this.boat = event.boat;
            } else {
               this.boat = null;
            }
         }

         if (this.boat != null) {
            this.boat.noPhysics = true;
            this.dismountTicks = 5;
         }
      }
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.dismountTicks > 0) {
         this.dismountTicks--;
         if (this.dismountTicks == 0) {
            if (this.boat != null) {
               this.boat.noPhysics = false;
               if ((Boolean)this.toggleAfter.get() && !(Boolean)this.remount.get()) {
                  this.toggle();
               } else if ((Boolean)this.remount.get()) {
                  this.remountTicks = 5;
               }
            }

            this.dontPhase = true;
         }
      }

      if (this.remountTicks > 0) {
         this.remountTicks--;
         if (this.remountTicks == 0) {
            this.mc.getConnection().send(new ServerboundInteractPacket(this.boat.getId(), InteractionHand.MAIN_HAND, this.boat.position(), false));
            if ((Boolean)this.toggleAfter.get()) {
               this.toggle();
            }
         }
      }
   }

   @EventHandler
   private void onKey(KeyInputEvent event) {
      if (event.key() == this.mc.options.keyShift.getDefaultKey().getValue()
         && event.action == KeyAction.Press
         && this.mc.player.getVehicle() instanceof AbstractBoat) {
         this.dontPhase = false;
         this.boat = null;
      }
   }
}
