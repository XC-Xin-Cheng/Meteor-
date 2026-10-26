package zgoly.meteorist.modules.autotrade;

import java.util.HashMap;
import java.util.Map;
import zgoly.meteorist.modules.autotrade.offers.BaseOffer;
import zgoly.meteorist.modules.autotrade.offers.IdOffer;
import zgoly.meteorist.modules.autotrade.offers.ItemsOffer;

public class OfferFactory {
   private final Map<String, OfferFactory.Factory> factories = new HashMap<>();

   public OfferFactory() {
      this.factories.put("Items", ItemsOffer::new);
      this.factories.put("ID", IdOffer::new);
   }

   public BaseOffer createOffer(String name) {
      return this.factories.containsKey(name) ? this.factories.get(name).create() : null;
   }

   private interface Factory {
      BaseOffer create();
   }
}
