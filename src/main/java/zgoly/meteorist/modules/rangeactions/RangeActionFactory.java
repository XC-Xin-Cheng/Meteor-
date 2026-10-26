package zgoly.meteorist.modules.rangeactions;

import java.util.HashMap;
import java.util.Map;
import zgoly.meteorist.modules.rangeactions.rangeactions.BaseRangeAction;
import zgoly.meteorist.modules.rangeactions.rangeactions.CommandsRangeAction;
import zgoly.meteorist.modules.rangeactions.rangeactions.DespawnerRangeAction;
import zgoly.meteorist.modules.rangeactions.rangeactions.InteractionRangeAction;

public class RangeActionFactory {
   private final Map<String, RangeActionFactory.Factory> factories = new HashMap<>();

   public RangeActionFactory() {
      this.factories.put("Interaction", InteractionRangeAction::new);
      this.factories.put("Despawner", DespawnerRangeAction::new);
      this.factories.put("Commands", CommandsRangeAction::new);
   }

   public BaseRangeAction createRangeAction(String name) {
      return this.factories.containsKey(name) ? this.factories.get(name).create() : null;
   }

   private interface Factory {
      BaseRangeAction create();
   }
}
