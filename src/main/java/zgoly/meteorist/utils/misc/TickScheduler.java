package zgoly.meteorist.utils.misc;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.orbit.EventHandler;

public class TickScheduler {
   private final List<TickScheduler.ScheduledTask> tasks = new ArrayList<>();

   @EventHandler
   private void onTick(Pre event) {
      List<TickScheduler.ScheduledTask> toRemove = new ArrayList<>();

      for (TickScheduler.ScheduledTask task : this.tasks) {
         task.delay--;
         if (task.delay <= 0) {
            task.runnable.run();
            toRemove.add(task);
         }
      }

      this.tasks.removeAll(toRemove);
   }

   public void runTaskLater(Runnable task, int delay) {
      this.tasks.add(new TickScheduler.ScheduledTask(task, delay));
   }

   public void cancelTasks() {
      this.tasks.clear();
   }

   private static class ScheduledTask {
      private final Runnable runnable;
      private int delay;

      private ScheduledTask(Runnable runnable, int delay) {
         this.runnable = runnable;
         this.delay = delay;
      }
   }
}
