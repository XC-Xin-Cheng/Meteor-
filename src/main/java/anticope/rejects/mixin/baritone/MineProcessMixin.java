package anticope.rejects.mixin.baritone;

import anticope.rejects.modules.OreSim;
import baritone.api.utils.BlockOptionalMetaLookup;
import baritone.pathing.movement.CalculationContext;
import baritone.process.MineProcess;
import java.util.List;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MineProcess.class})
public class MineProcessMixin {
   @Shadow(
      remap = false
   )
   private List<BlockPos> a;

   @Inject(
      method = {"a(Ljava/util/List;Lbaritone/pathing/movement/CalculationContext;)V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void onRescan(List<BlockPos> already, CalculationContext context, CallbackInfo ci) {
      OreSim oreSim = (OreSim)Modules.get().get(OreSim.class);
      if (oreSim != null && oreSim.baritone()) {
         this.a = oreSim.oreGoals;
         ci.cancel();
      }
   }

   @Redirect(
      method = {"a(Lbaritone/pathing/movement/CalculationContext;Lbaritone/api/utils/BlockOptionalMetaLookup;Ljava/util/List;Lnet/minecraft/core/BlockPos;)Z"},
      at = @At(
         value = "INVOKE",
         target = "Lbaritone/api/utils/BlockOptionalMetaLookup;has(Lnet/minecraft/world/level/block/state/BlockState;)Z"
      )
   )
   private static boolean onPruneStream(BlockOptionalMetaLookup instance, BlockState blockState) {
      OreSim oreSim = (OreSim)Modules.get().get(OreSim.class);
      return oreSim != null && oreSim.baritone() ? !blockState.isAir() : instance.has(blockState);
   }
}
