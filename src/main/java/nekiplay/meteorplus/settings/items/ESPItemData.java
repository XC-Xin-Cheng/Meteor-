package nekiplay.meteorplus.settings.items;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.IGeneric;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

public class ESPItemData implements IGeneric<ESPItemData>, IChangeable, IItemData<ESPItemData> {
   public ShapeMode shapeMode;
   public SettingColor lineColor;
   public SettingColor sideColor;
   public boolean tracer;
   public SettingColor tracerColor;
   private boolean changed;

   public ESPItemData(ShapeMode shapeMode, SettingColor lineColor, SettingColor sideColor, boolean tracer, SettingColor tracerColor) {
      this.shapeMode = shapeMode;
      this.lineColor = lineColor;
      this.sideColor = sideColor;
      this.tracer = tracer;
      this.tracerColor = tracerColor;
   }

   @Override
   public WidgetScreen createScreen(GuiTheme theme, Item block, ItemDataSetting<ESPItemData> setting) {
      return new ESPItemDataScreen(theme, this, block, setting);
   }

   public WidgetScreen createScreen(GuiTheme theme, GenericSetting<ESPItemData> setting) {
      return new ESPItemDataScreen(theme, this, setting);
   }

   public boolean isChanged() {
      return this.changed;
   }

   public void changed() {
      this.changed = true;
   }

   public void tickRainbow() {
      this.lineColor.update();
      this.sideColor.update();
      this.tracerColor.update();
   }

   public ESPItemData set(ESPItemData value) {
      this.shapeMode = value.shapeMode;
      this.lineColor.set(value.lineColor);
      this.sideColor.set(value.sideColor);
      this.tracer = value.tracer;
      this.tracerColor.set(value.tracerColor);
      this.changed = value.changed;
      return this;
   }

   public ESPItemData copy() {
      return new ESPItemData(
         this.shapeMode, new SettingColor(this.lineColor), new SettingColor(this.sideColor), this.tracer, new SettingColor(this.tracerColor)
      );
   }

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("shapeMode", this.shapeMode.name());
      tag.put("lineColor", this.lineColor.toTag());
      tag.put("sideColor", this.sideColor.toTag());
      tag.putBoolean("tracer", this.tracer);
      tag.put("tracerColor", this.tracerColor.toTag());
      tag.putBoolean("changed", this.changed);
      return tag;
   }

   public ESPItemData fromTag(CompoundTag tag) {
      this.shapeMode = ShapeMode.valueOf((String)tag.getString("shapeMode").get());
      this.lineColor.fromTag((CompoundTag)tag.getCompound("lineColor").get());
      this.sideColor.fromTag((CompoundTag)tag.getCompound("sideColor").get());
      this.tracer = (Boolean)tag.getBoolean("tracer").get();
      this.tracerColor.fromTag((CompoundTag)tag.getCompound("tracerColor").get());
      this.changed = (Boolean)tag.getBoolean("changed").get();
      return this;
   }
}
