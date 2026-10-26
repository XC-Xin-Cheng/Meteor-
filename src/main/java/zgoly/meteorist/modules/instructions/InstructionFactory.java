package zgoly.meteorist.modules.instructions;

import java.util.HashMap;
import java.util.Map;
import zgoly.meteorist.modules.instructions.instructions.BaseInstruction;
import zgoly.meteorist.modules.instructions.instructions.CommandInstruction;
import zgoly.meteorist.modules.instructions.instructions.DelayInstruction;

public class InstructionFactory {
   private final Map<String, InstructionFactory.Factory> factories = new HashMap<>();

   public InstructionFactory() {
      this.factories.put("Command", CommandInstruction::new);
      this.factories.put("Delay", DelayInstruction::new);
   }

   public BaseInstruction createInstruction(String name) {
      return this.factories.containsKey(name) ? this.factories.get(name).create() : null;
   }

   private interface Factory {
      BaseInstruction create();
   }
}
