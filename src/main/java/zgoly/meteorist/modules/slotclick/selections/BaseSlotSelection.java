package zgoly.meteorist.modules.slotclick.selections;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.CompoundTag;
import zgoly.meteorist.gui.screens.SlotSelectionScreen;

public class BaseSlotSelection implements ISerializable<BaseSlotSelection> {
   public Settings settings = new Settings();

   public static void reloadParent() {
      if (MeteorClient.mc.screen instanceof SlotSelectionScreen slotSelectionScreen && slotSelectionScreen.parent instanceof WindowScreen windowScreen) {
         windowScreen.reload();
      }
   }

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.put("settings", this.settings.toTag());
      return tag;
   }

   public BaseSlotSelection fromTag(CompoundTag tag) {
      CompoundTag settingsTag = (CompoundTag)tag.get("settings");
      if (settingsTag != null) {
         this.settings.fromTag(settingsTag);
      }

      return this;
   }

   public String getTypeName() {
      return null;
   }

   public BaseSlotSelection copy() {
      return null;
   }
}
