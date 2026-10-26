package com.stash.hunt.modules.searcharea.modes;

import com.stash.hunt.Utils;
import com.stash.hunt.modules.searcharea.SearchAreaMode;
import com.stash.hunt.modules.searcharea.SearchAreaModes;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.core.BlockPos;

public class Spiral extends SearchAreaMode {
   private Spiral.PathingDataSpiral pd;
   private boolean goingToStart = true;
   private long startTime;

   public Spiral() {
      super(SearchAreaModes.Spiral);
   }

   @Override
   public void onActivate() {
      this.startTime = System.nanoTime();
      this.goingToStart = true;
      File file = this.getJsonFile(super.toString());
      if (file != null && file.exists()) {
         try {
            FileReader reader = new FileReader(file);
            this.pd = (Spiral.PathingDataSpiral)GSON.fromJson(reader, Spiral.PathingDataSpiral.class);
            reader.close();
            ChatUtils.info("Loaded previously saved path, heading to where you left off.", new Object[0]);
         } catch (IOException var3) {
            ChatUtils.info("Failed to load saved path, check logs for more details. Disabling module.", new Object[0]);
            var3.printStackTrace();
            this.disable();
         }
      } else {
         this.pd = new Spiral.PathingDataSpiral(this.mc.player.blockPosition(), this.mc.player.blockPosition(), -90.0F, true, 0, 0);
      }
   }

   @Override
   public void onDeactivate() {
      super.onDeactivate();
      super.saveToJson(this.goingToStart, this.pd);
   }

   @Override
   public void onTick() {
      if (System.nanoTime() - this.startTime > 6.0E11) {
         this.startTime = System.nanoTime();
         super.saveToJson(this.goingToStart, this.pd);
      }

      if (System.nanoTime() < this.paused) {
         Utils.setPressed(this.mc.options.keyUp, false);
      } else if (this.goingToStart) {
         if (Math.sqrt(this.mc.player.blockPosition().distToLowCornerSqr(this.pd.currPos.getX(), this.mc.player.getY(), this.pd.currPos.getZ())) < 5.0) {
            this.goingToStart = false;
            this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         } else {
            this.mc.player.setYRot((float)Rotations.getYaw(this.pd.currPos.getCenter()));
            Utils.setPressed(this.mc.options.keyUp, true);
         }
      } else {
         Utils.setPressed(this.mc.options.keyUp, true);
         this.mc.player.setYRot(this.pd.yawDirection);
         int blockGap = 16 * (Integer)this.searchArea.rowGap.get();
         if (this.pd.mainPath && Math.abs(this.mc.player.getX() - this.pd.initialPos.getX()) >= blockGap + this.pd.spiralWidth) {
            this.pd.yawDirection += 90.0F;
            this.pd.initialPos = new BlockPos((int)this.mc.player.getX(), this.pd.initialPos.getY(), this.pd.initialPos.getZ());
            this.pd.spiralWidth += blockGap;
            this.pd.mainPath = false;
            this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         } else if (!this.pd.mainPath && Math.abs(this.mc.player.getZ() - this.pd.initialPos.getZ()) >= blockGap + this.pd.spiralHeight) {
            this.pd.yawDirection += 90.0F;
            this.pd.initialPos = new BlockPos(this.pd.initialPos.getX(), this.pd.initialPos.getY(), (int)this.mc.player.getZ());
            this.pd.spiralHeight += blockGap;
            this.pd.mainPath = true;
            this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
         }
      }
   }

   public static class PathingDataSpiral extends SearchAreaMode.PathingData {
      public int spiralWidth = 0;
      public int spiralHeight = 0;

      public PathingDataSpiral(BlockPos initialPos, BlockPos currPos, float yawDirection, boolean mainPath, int spiralWidth, int spiralHeight) {
         this.initialPos = initialPos;
         this.currPos = currPos;
         this.yawDirection = yawDirection;
         this.mainPath = mainPath;
         this.spiralWidth = spiralWidth;
         this.spiralHeight = spiralHeight;
      }
   }
}
