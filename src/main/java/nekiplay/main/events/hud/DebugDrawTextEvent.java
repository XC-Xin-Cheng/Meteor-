package nekiplay.main.events.hud;

import java.util.ArrayList;
import java.util.List;
import nekiplay.main.events.Cancellable;

public class DebugDrawTextEvent extends Cancellable {
   private static final DebugDrawTextEvent INSTANCE = new DebugDrawTextEvent();
   private List<String> lines = new ArrayList<>();
   private boolean isLeft = false;

   public List<String> getLines() {
      return this.lines;
   }

   public boolean isLeft() {
      return this.isLeft;
   }

   public static DebugDrawTextEvent get(List<String> lines, boolean isLeft) {
      INSTANCE.lines = lines;
      INSTANCE.isLeft = isLeft;
      return INSTANCE;
   }
}
