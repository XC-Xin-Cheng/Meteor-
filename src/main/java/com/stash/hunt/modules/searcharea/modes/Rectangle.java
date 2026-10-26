package com.stash.hunt.modules.searcharea.modes;

import com.stash.hunt.Utils;
import com.stash.hunt.modules.searcharea.SearchAreaMode;
import com.stash.hunt.modules.searcharea.SearchAreaModes;
import java.io.File;
import java.io.FileReader;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.systems.modules.movement.EntityControl;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;

public class Rectangle extends SearchAreaMode {
   private Rectangle.PathingDataRectangle pd;
   private boolean goingToStart = true;
   private long startTime;

   public Rectangle() {
      super(SearchAreaModes.Rectangle);
   }

   @Override
   public void onActivate() {
      this.goingToStart = true;
      File file = this.getJsonFile(super.toString());
      if (file != null && file.exists()) {
         try {
            FileReader reader = new FileReader(file);
            this.pd = (Rectangle.PathingDataRectangle)GSON.fromJson(reader, Rectangle.PathingDataRectangle.class);
            reader.close();
         } catch (Exception var3) {
         }
      } else {
         this.pd = new Rectangle.PathingDataRectangle(
            (BlockPos)this.searchArea.startPos.get(),
            (BlockPos)this.searchArea.targetPos.get(),
            (BlockPos)this.searchArea.startPos.get(),
            90.0F,
            true,
            (int)this.mc.player.getZ()
         );
      }
   }

   @Override
   public void onDeactivate() {
      super.onDeactivate();
      super.saveToJson(this.goingToStart, this.pd);
   }

   private void printRectangleEstimate() {
      Class<? extends Module> entityControl = EntityControl.class;
      Module module = Modules.get().get(entityControl);
      double speedBPS = (Double)module.settings.get("horizontal-speed").get();
      double rowDistance = Math.abs(this.pd.initialPos.getX() - this.pd.targetPos.getX());
      int rowCount = Math.abs(this.pd.currPos.getZ() - this.pd.targetPos.getZ()) / 16 / (Integer)this.searchArea.rowGap.get();
      double totalBlocks = rowCount * (rowDistance + (Integer)this.searchArea.rowGap.get() * 16);
      long totalSeconds = (long)(totalBlocks / speedBPS);
      long hours = totalSeconds / 3600L;
      long minutes = totalSeconds % 3600L / 60L;
      long seconds = totalSeconds % 60L;
      ChatUtils.info(
         "Completion will take an estimated %02d hours %02d minutes %02d seconds with boatfly at a speed of %.2f and a gap of %d chunks between paths.",
         new Object[]{hours, minutes, seconds, speedBPS, this.searchArea.rowGap.get()}
      );
   }

   @Override
   public void onTick() {
      if (System.nanoTime() - this.startTime > 6.0E11) {
         this.startTime = System.nanoTime();
         super.saveToJson(this.goingToStart, this.pd);
      }

      if (this.goingToStart) {
         if (Math.sqrt(this.mc.player.blockPosition().distToLowCornerSqr(this.pd.currPos.getX(), this.mc.player.getY(), this.pd.currPos.getZ())) < 5.0) {
            this.goingToStart = false;
            this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
            this.printRectangleEstimate();
         } else {
            this.mc.player.setYRot((float)Rotations.getYaw(this.pd.currPos.getCenter()));
            Utils.setPressed(this.mc.options.keyUp, true);
         }
      } else {
         Utils.setPressed(this.mc.options.keyUp, true);
         this.mc.player.setYRot(this.pd.yawDirection);
         if (Math.sqrt(this.mc.player.blockPosition().distToLowCornerSqr(this.pd.targetPos.getX(), this.mc.player.getY(), this.pd.targetPos.getZ())) < 20.0) {
            Utils.setPressed(this.mc.options.keyUp, false);
            this.searchArea.toggle();
            if ((Boolean)this.searchArea.disconnectOnCompletion.get()) {
               AutoReconnect autoReconnect = (AutoReconnect)Modules.get().get(AutoReconnect.class);
               if (autoReconnect.isActive()) {
                  autoReconnect.toggle();
               }

               this.mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket(Component.literal("[Search Area] Path is complete")));
            }
         } else if (this.pd.mainPath
               && this.pd.yawDirection == -90.0F
               && this.mc.player.getX() >= Math.max(this.pd.initialPos.getX(), this.pd.targetPos.getX())
            || this.pd.yawDirection == 90.0F && this.mc.player.getX() <= Math.min(this.pd.initialPos.getX(), this.pd.targetPos.getX())) {
            this.pd.yawDirection = this.mc.player.getZ() < this.pd.targetPos.getZ() ? 0.0F : 180.0F;
            this.pd.mainPath = false;
            this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         } else if (!this.pd.mainPath && Math.abs(this.mc.player.getZ() - this.pd.lastCompleteRowZ) >= 16 * (Integer)this.searchArea.rowGap.get()) {
            this.pd.lastCompleteRowZ = (int)this.mc.player.getZ();
            this.pd.yawDirection = this.pd.initialPos.getX() > this.mc.player.getX() ? -90.0F : 90.0F;
            this.pd.mainPath = true;
            this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         }
      }
   }

   public static class PathingDataRectangle extends SearchAreaMode.PathingData {
      public BlockPos targetPos;
      public int lastCompleteRowZ;

      public PathingDataRectangle(BlockPos initialPos, BlockPos targetPos, BlockPos currPos, float yawDirection, boolean mainPath, int lastCompleteRowZ) {
         this.initialPos = initialPos;
         this.targetPos = targetPos;
         this.currPos = currPos;
         this.yawDirection = yawDirection;
         this.mainPath = mainPath;
         this.lastCompleteRowZ = lastCompleteRowZ;
      }
   }
}
