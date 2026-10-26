package zgoly.meteorist.modules.slotclick;

import java.util.HashMap;
import java.util.Map;
import zgoly.meteorist.modules.slotclick.selections.BaseSlotSelection;
import zgoly.meteorist.modules.slotclick.selections.DelaySelection;
import zgoly.meteorist.modules.slotclick.selections.SingleSlotSelection;
import zgoly.meteorist.modules.slotclick.selections.SlotRangeSelection;
import zgoly.meteorist.modules.slotclick.selections.SwapSlotSelection;

public class SelectionFactory {
   private final Map<String, SelectionFactory.Factory> factories = new HashMap<>();

   public SelectionFactory() {
      this.factories.put("Single", SingleSlotSelection::new);
      this.factories.put("Range", SlotRangeSelection::new);
      this.factories.put("Swap", SwapSlotSelection::new);
      this.factories.put("Delay", DelaySelection::new);
   }

   public BaseSlotSelection createSelection(String name) {
      return this.factories.containsKey(name) ? this.factories.get(name).create() : null;
   }

   private interface Factory {
      BaseSlotSelection create();
   }
}
