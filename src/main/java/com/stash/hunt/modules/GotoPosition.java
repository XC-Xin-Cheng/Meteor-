package com.stash.hunt.modules;

import com.stash.hunt.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BlockPosSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.world.phys.Vec3;

public class GotoPosition extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<BlockPos> target = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("target-position")).description("Coords to go to. Y is ignored."))
               .defaultValue(new BlockPos(0, 0, 0)))
            .build()
      );
   public final Setting<Boolean> disconnectOnComplete = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("disconnect-when-complete"))
                  .description("Disconnects when you get to the target"))
               .defaultValue(false))
            .build()
      );

   public GotoPosition() {
      super(Addon.CATEGORY, "goto-position", "Goes in a straight line towards the position you give and stops once there.");
   }

   public void onActivate() {
      double distance = Math.sqrt(
         this.mc.player.blockPosition().distToLowCornerSqr(((BlockPos)this.target.get()).getX(), this.mc.player.getY(), ((BlockPos)this.target.get()).getZ())
      );
      long totalSeconds = (long)(distance / 70.0);
      long hours = totalSeconds / 3600L;
      long minutes = totalSeconds % 3600L / 60L;
      long seconds = totalSeconds % 60L;
      this.info("Completion will take an estimated %02d hours %02d minutes %02d seconds at an average speed of 70bps", new Object[]{hours, minutes, seconds});
   }

   public void onDeactivate() {
      this.mc.options.keyUp.setDown(false);
      Input.setKeyState(this.mc.options.keyUp, false);
      this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
   }

   @EventHandler
   private void onTick(Post event) {
      if (Math.sqrt(
            this.mc
               .player
               .blockPosition()
               .distToLowCornerSqr(((BlockPos)this.target.get()).getX(), this.mc.player.getY(), ((BlockPos)this.target.get()).getZ())
         )
         > 5.0) {
         this.mc
            .player
            .setYRot((float)Rotations.getYaw(new Vec3(((BlockPos)this.target.get()).getX(), (int)this.mc.player.getY(), ((BlockPos)this.target.get()).getZ())));
         this.mc.options.keyUp.setDown(true);
         Input.setKeyState(this.mc.options.keyUp, true);
      } else {
         this.mc.options.keyUp.setDown(false);
         Input.setKeyState(this.mc.options.keyUp, false);
         this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         if ((Boolean)this.disconnectOnComplete.get()) {
            this.mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket(Component.literal("[GotoPosition] You are at your destination!")));
         }

         this.target.reset();
         this.toggle();
      }
   }
}
