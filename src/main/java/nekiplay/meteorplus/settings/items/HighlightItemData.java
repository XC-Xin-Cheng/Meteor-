package nekiplay.meteorplus.settings.items;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.IGeneric;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

public class HighlightItemData implements IGeneric<HighlightItemData>, IChangeable, IItemData<HighlightItemData> {
   public SettingColor Color;
   private boolean changed;

   public HighlightItemData(SettingColor color) {
      this.Color = color;
   }

   @Override
   public WidgetScreen createScreen(GuiTheme theme, Item block, ItemDataSetting<HighlightItemData> setting) {
      return new HighlightItemDataScreen(theme, this, block, setting);
   }

   public WidgetScreen createScreen(GuiTheme theme, GenericSetting<HighlightItemData> setting) {
      return new HighlightItemDataScreen(theme, this, setting);
   }

   public boolean isChanged() {
      return this.changed;
   }

   public void changed() {
      this.changed = true;
   }

   public void tickRainbow() {
      this.Color.update();
   }

   public HighlightItemData set(HighlightItemData value) {
      this.Color.set(value.Color);
      this.changed = value.changed;
      return this;
   }

   public HighlightItemData copy() {
      return new HighlightItemData(new SettingColor(this.Color));
   }

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.put("color", this.Color.toTag());
      tag.putBoolean("changed", this.changed);
      return tag;
   }

   public HighlightItemData fromTag(CompoundTag tag) {
      this.Color.fromTag((CompoundTag)tag.getCompound("color").get());
      this.changed = (Boolean)tag.getBoolean("changed").get();
      return this;
   }
}
