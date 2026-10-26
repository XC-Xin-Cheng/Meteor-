package zgoly.meteorist.modules.autotrade.offers;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;

public class IdOffer extends BaseOffer {
   public static final String type = "ID";
   SettingGroup sgId = this.settings.createGroup("ID");
   public final Setting<Integer> offerId = this.sgId
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("offer-id")).description("ID of offer to select.")).defaultValue(0))
               .min(0)
               .onChanged(value -> reloadParent()))
            .build()
      );

   @Override
   public String getTypeName() {
      return "ID";
   }

   public IdOffer copy() {
      return (IdOffer)new IdOffer().fromTag(this.toTag());
   }
}
