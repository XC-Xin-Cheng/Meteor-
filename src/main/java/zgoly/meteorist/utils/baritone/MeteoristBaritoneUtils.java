package zgoly.meteorist.utils.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalComposite;
import baritone.api.pathing.goals.GoalNear;
import java.util.List;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public class MeteoristBaritoneUtils {
   MeteoristBaritoneUtils.BaritoneOperations baritoneOperations;

   public MeteoristBaritoneUtils() {
      if (BaritoneUtils.IS_AVAILABLE) {
         this.baritoneOperations = new MeteoristBaritoneUtils.BasedBaritoneOperations();
      } else {
         this.baritoneOperations = new MeteoristBaritoneUtils.NoBaritoneOperations();
      }
   }

   public void cancelEverything() {
      this.baritoneOperations.cancelEverything();
   }

   public boolean isPathing() {
      return this.baritoneOperations.isPathing();
   }

   public void setGoalNear(BlockPos blockPos, int range) {
      this.baritoneOperations.setGoalNear(blockPos, range);
   }

   public void setGoalNear(List<Entity> entities, int range) {
      this.baritoneOperations.setGoalNear(entities, range);
   }

   public interface BaritoneOperations {
      void cancelEverything();

      boolean isPathing();

      void setGoalNear(BlockPos var1, int var2);

      void setGoalNear(List<Entity> var1, int var2);
   }

   public static class BasedBaritoneOperations implements MeteoristBaritoneUtils.BaritoneOperations {
      private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

      @Override
      public void cancelEverything() {
         this.baritone.getPathingBehavior().cancelEverything();
      }

      @Override
      public boolean isPathing() {
         return this.baritone.getPathingBehavior().isPathing();
      }

      @Override
      public void setGoalNear(BlockPos blockPos, int range) {
         this.baritone.getCustomGoalProcess().setGoalAndPath(new GoalNear(blockPos, range));
      }

      @Override
      public void setGoalNear(List<Entity> entities, int range) {
         this.baritone
            .getCustomGoalProcess()
            .setGoalAndPath(new GoalComposite(entities.stream().map(entity -> new GoalNear(entity.blockPosition(), range)).toArray(Goal[]::new)));
      }
   }

   public static class NoBaritoneOperations implements MeteoristBaritoneUtils.BaritoneOperations {
      @Override
      public void cancelEverything() {
      }

      @Override
      public boolean isPathing() {
         return false;
      }

      @Override
      public void setGoalNear(BlockPos blockPos, int range) {
      }

      @Override
      public void setGoalNear(List<Entity> entities, int range) {
      }
   }
}
