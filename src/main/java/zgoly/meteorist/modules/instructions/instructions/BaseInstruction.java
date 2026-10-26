package zgoly.meteorist.modules.instructions.instructions;

import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.CompoundTag;

public class BaseInstruction implements ISerializable<BaseInstruction> {
   public final Settings settings = new Settings();

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.put("settings", this.settings.toTag());
      return tag;
   }

   public BaseInstruction fromTag(CompoundTag tag) {
      CompoundTag settingsTag = (CompoundTag)tag.get("settings");
      if (settingsTag != null) {
         this.settings.fromTag(settingsTag);
      }

      return this;
   }

   public String getTypeName() {
      return null;
   }

   public BaseInstruction copy() {
      return null;
   }
}
