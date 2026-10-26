package zgoly.meteorist.modules.autotrade.offers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.CompoundTag;
import zgoly.meteorist.gui.screens.OfferScreen;

public class BaseOffer implements ISerializable<BaseOffer> {
   public Settings settings = new Settings();
   SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<Boolean> enabled = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("enabled")).description("Enables this offer.")).defaultValue(true)).build());

   public static void reloadParent() {
      if (MeteorClient.mc.screen instanceof OfferScreen offerScreen && offerScreen.parent instanceof WindowScreen windowScreen) {
         windowScreen.reload();
      }
   }

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.put("settings", this.settings.toTag());
      return tag;
   }

   public BaseOffer fromTag(CompoundTag tag) {
      CompoundTag settingsTag = (CompoundTag)tag.get("settings");
      if (settingsTag != null) {
         this.settings.fromTag(settingsTag);
      }

      return this;
   }

   public String getTypeName() {
      return null;
   }

   public BaseOffer copy() {
      return null;
   }
}
