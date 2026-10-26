package cn.blockforge.meteorzhcn.util.trans_engine;

import java.util.LinkedHashMap;
import java.util.Set;

public class EngineManager {
   private static final EngineManager INSTANCE = new EngineManager();
   LinkedHashMap<String, AbstractTransEngine> engines = new LinkedHashMap<>();

   public static EngineManager getInstance() {
      return INSTANCE;
   }

   public Set<String> getEngineNames() {
      return this.engines.keySet();
   }

   public AbstractTransEngine getEngine(String name) {
      AbstractTransEngine engine = this.engines.get(name);
      return engine == null ? this.engines.get("AUTO") : engine;
   }

   private EngineManager() {
      this.engines.put("AUTO", new TransEngineAuto());
      this.engines.put("NEW", new TransEngineNew());
      this.engines.put("OLD", new TransEngineOld());
      this.engines.put("LEGACY", new TransEngineLegacy());
   }
}
