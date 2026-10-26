package com.stash.hunt.modules.searcharea;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stash.hunt.Utils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class SearchAreaMode {
   protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   protected final SearchArea searchArea;
   protected final Minecraft mc;
   private final SearchAreaModes type;
   protected long paused = 0L;

   public SearchAreaMode(SearchAreaModes type) {
      this.searchArea = (SearchArea)Modules.get().get(SearchArea.class);
      this.mc = Minecraft.getInstance();
      this.type = type;
   }

   public void onTick() {
   }

   public void onActivate() {
   }

   public void onDeactivate() {
      Utils.setPressed(this.mc.options.keyUp, false);
   }

   public void disable() {
      if (this.searchArea.isActive()) {
         this.searchArea.toggle();
      }
   }

   protected File getJsonFile(String fileName) {
      try {
         return new File(new File(new File(MeteorClient.FOLDER, "search-area"), (String)this.searchArea.saveLocation.get()), fileName + ".json");
      } catch (NullPointerException var3) {
         return null;
      }
   }

   protected void saveToJson(boolean goingToStart, SearchAreaMode.PathingData pd) {
      if (pd != null) {
         if (!goingToStart) {
            pd.currPos = this.mc.player.blockPosition();
         }

         try {
            File file = this.getJsonFile(this.type.toString());
            if (file == null) {
               return;
            }

            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            GSON.toJson(pd, writer);
            writer.close();
         } catch (IOException var5) {
            var5.printStackTrace();
         }
      }
   }

   public void clear() {
      File file = this.getJsonFile(this.type.toString());
      file.delete();
   }

   public void clear(String mode) {
      File file = this.getJsonFile(mode);
      file.delete();
   }

   public void clearAll() {
      for (SearchAreaModes mode : SearchAreaModes.values()) {
         this.clear(mode.toString());
      }
   }

   @Override
   public String toString() {
      return this.type.toString();
   }

   protected static class PathingData {
      public BlockPos initialPos;
      public BlockPos currPos;
      public float yawDirection;
      public boolean mainPath;
   }
}
