package nekiplay;

import nekiplay.main.items.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
   public static final Logger LOG = LoggerFactory.getLogger(Main.class);
   public static final String METEOR_LOGPREFIX = "[Meteor+]";

   public void onInitialize() {
      LOG.info("[Meteor+] Initializing items...");
      ModItems.initialize();
      LOG.info("[Meteor+] Loaded items");
   }
}
