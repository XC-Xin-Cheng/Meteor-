package com.stash.hunt.modules;

import com.stash.hunt.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoLogPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> logOnY = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("log-on-y")).description("Logs out if you are below a certain Y level.")).defaultValue(false))
            .build()
      );
   private final Setting<Double> yLevel = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("y-level"))
               .defaultValue(256.0)
               .min(-128.0)
               .sliderRange(-128.0, 320.0)
               .visible(this.logOnY::get))
            .build()
      );
   private final Setting<Boolean> logArmor = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("log-armor")).description("Logs out if your armor goes below a certain durability amount."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> ignoreElytra = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("ignore-elytra")).description("Ignores the elytra when checking armor durability."))
                  .defaultValue(false))
               .visible(this.logArmor::get))
            .build()
      );
   private final Setting<Double> armorPercent = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("armor-percent"))
               .defaultValue(5.0)
               .min(0.0)
               .sliderRange(0.0, 100.0)
               .visible(this.logArmor::get))
            .build()
      );
   private final Setting<Boolean> logPortal = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("log-on-portal")).description("Logs out if you are in a portal for too long.")).defaultValue(false))
            .build()
      );
   private final Setting<Integer> portalTicks = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("portal-ticks"))
                     .description("The amount of ticks in a portal before you get kicked (It takes 80 ticks to go through a portal)."))
                  .defaultValue(30))
               .min(1)
               .sliderMax(70)
               .visible(this.logPortal::get))
            .build()
      );
   private final Setting<Boolean> logPosition = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("log-position"))
                  .description("Logs out if you are within x blocks of this position. Y Position is not included"))
               .defaultValue(false))
            .build()
      );
   private final Setting<BlockPos> position = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                        .name("position"))
                     .description("The position to log out at. Y position is ignored."))
                  .defaultValue(new BlockPos(0, 0, 0)))
               .visible(this.logPosition::get))
            .build()
      );
   private final Setting<Double> distance = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("distance"))
                  .description("The distance from the position to log out at."))
               .defaultValue(100.0)
               .sliderRange(0.0, 1000.0)
               .visible(this.logPosition::get))
            .build()
      );
   private final Setting<Boolean> serverNotResponding = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("server-not-responding")).description("Logs out if the server is not responding."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Double> serverNotRespondingSecs = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("seconds-not-responding"))
                  .description("The amount of seconds the server is not responding before you log out."))
               .defaultValue(10.0)
               .min(1.0)
               .sliderMax(60.0)
               .visible(this.serverNotResponding::get))
            .build()
      );
   private final Setting<Boolean> reconnectAfterNotResponding = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("reconnect-after-not-responding"))
                     .description("Reconnects after the server is not responding."))
                  .defaultValue(false))
               .visible(this.serverNotResponding::get))
            .build()
      );
   private final Setting<Double> secondsToReconnect = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("reconnect-seconds"))
                  .description("The amount of seconds to wait before reconnecting (Will temporarily overwrite Meteor's AutoReconnect."))
               .defaultValue(60.0)
               .min(10.0)
               .sliderMax(300.0)
               .visible(() -> (Boolean)this.reconnectAfterNotResponding.get() && (Boolean)this.serverNotResponding.get()))
            .build()
      );
   private final Setting<Boolean> illegalDisconnect = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("illegal-disconnect")).description("Disconnects from the server using the slot method."))
               .defaultValue(false))
            .build()
      );
   private int currPortalTicks = 0;
   private double oldDelay;
   private boolean autoReconnectEnabled;
   private boolean waitingForReconnection = false;

   public AutoLogPlus() {
      super(Addon.CATEGORY, "auto-log-plus", "Provides some additional triggers to log out.");
   }

   public void onActivate() {
      this.currPortalTicks = 0;
      if (this.waitingForReconnection) {
         this.waitingForReconnection = false;
         AutoReconnect autoReconnect = (AutoReconnect)Modules.get().get(AutoReconnect.class);
         Setting<Double> delay = autoReconnect.settings.get("delay");
         delay.set(this.oldDelay);
         if (!this.autoReconnectEnabled && autoReconnect.isActive()) {
            autoReconnect.toggle();
         }
      }
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.player != null && !this.mc.player.getAbilities().mayfly) {
         if ((Boolean)this.serverNotResponding.get()
            && !this.waitingForReconnection
            && TickRate.INSTANCE.getTimeSinceLastTick() > (Double)this.serverNotRespondingSecs.get()) {
            if ((Boolean)this.reconnectAfterNotResponding.get()) {
               AutoReconnect autoReconnect = (AutoReconnect)Modules.get().get(AutoReconnect.class);
               this.autoReconnectEnabled = autoReconnect.isActive();
               Setting<Double> delay = autoReconnect.settings.get("delay");
               this.oldDelay = (Double)delay.get();
               delay.set((Double)this.secondsToReconnect.get());
               if (!this.autoReconnectEnabled) {
                  autoReconnect.toggle();
               }

               this.waitingForReconnection = true;
            }

            this.logOut("Server was not responding for " + this.serverNotRespondingSecs.get() + " seconds.", !(Boolean)this.reconnectAfterNotResponding.get());
         } else {
            if ((Boolean)this.logPortal.get() && this.mc.player.portalProcess != null) {
               if (this.mc.player.portalProcess.isInsidePortalThisTick()) {
                  this.currPortalTicks++;
                  if (this.currPortalTicks > (Integer)this.portalTicks.get()) {
                     this.logOut("Player was in a portal for " + this.currPortalTicks + " ticks.", true);
                     return;
                  }
               } else {
                  this.currPortalTicks = 0;
               }
            }

            if ((Boolean)this.logOnY.get() && this.mc.player.getY() < (Double)this.yLevel.get()) {
               this.logOut("Player was at Y=" + this.mc.player.getY() + " which is below your limit of Y=" + this.yLevel.get(), true);
            } else {
               if ((Boolean)this.logArmor.get()) {
                  for (int i = 0; i < 4; i++) {
                     ItemStack armorPiece = this.mc.player.getInventory().getItem(36 + i);
                     if ((!(Boolean)this.ignoreElytra.get() || armorPiece.getItem() != Items.ELYTRA) && armorPiece.isDamageableItem()) {
                        int max = armorPiece.getMaxDamage();
                        int current = armorPiece.getDamageValue();
                        double percentUndamaged = 100.0 - (double)current / max * 100.0;
                        if (percentUndamaged < (Double)this.armorPercent.get()) {
                           this.logOut("You had low armor", true);
                           return;
                        }
                     }
                  }
               }

               if ((Boolean)this.logPosition.get()) {
                  double distanceToTarget = this.mc
                     .player
                     .position()
                     .multiply(1.0, 0.0, 1.0)
                     .distanceTo(((BlockPos)this.position.get()).getCenter().multiply(1.0, 0.0, 1.0));
                  if (distanceToTarget < (Double)this.distance.get()) {
                     this.logOut("Player was within " + distanceToTarget + " blocks of the target position.", true);
                     return;
                  }
               }
            }
         }
      }
   }

   private void logOut(String reason, boolean turnOffReconnect) {
      if (this.mc.player != null) {
         if (turnOffReconnect && ((AutoReconnect)Modules.get().get(AutoReconnect.class)).isActive()) {
            ((AutoReconnect)Modules.get().get(AutoReconnect.class)).toggle();
         }

         if ((Boolean)this.illegalDisconnect.get()) {
            this.mc.player.connection.sendChat(String.valueOf('\u0000'));
         } else {
            this.mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket(Component.literal("[AutoLogPlus] " + reason)));
         }
      }
   }
}
