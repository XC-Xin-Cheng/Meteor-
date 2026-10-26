package zgoly.meteorist.modules;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import zgoly.meteorist.Meteorist;

public class AutoMud extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> range = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("range")).description("Range to use water bottle.")).defaultValue(4))
            .min(0)
            .sliderRange(0, 10)
            .build()
      );
   private final Setting<Boolean> fillBottle = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("fill-bottle"))
                  .description("Fill bottle with water when there are no water bottles remaining."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> swingHand = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("swing-hand"))
                  .description("Swing hand client-side."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> swapBack = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("swap-back"))
                  .description("Swap back when everything is done."))
               .defaultValue(true))
            .build()
      );

   public AutoMud() {
      super(Meteorist.CATEGORY, "auto-mud", "Automatically uses water bottle on dirt variants to get mud.");
   }

   private BlockPos findBlockPos(Predicate<BlockState> predicate) {
      for (BlockPos blockPos : BlockPos.withinManhattan(
         this.mc.player.blockPosition(), (Integer)this.range.get(), (Integer)this.range.get(), (Integer)this.range.get()
      )) {
         BlockState blockState = this.mc.level.getBlockState(blockPos);
         if (predicate.test(blockState)) {
            return blockPos;
         }
      }

      return null;
   }

   @EventHandler
   private void onTick(Post event) {
      FindItemResult waterBottle = InvUtils.findInHotbar(
         itemStack -> ((PotionContents)itemStack.getComponents().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)).is(Potions.WATER)
      );
      FindItemResult emptyBottle = InvUtils.findInHotbar(new Item[]{Items.GLASS_BOTTLE});
      if (waterBottle.found()) {
         BlockPos target = this.findBlockPos(blockState -> blockState.is(BlockTags.CONVERTABLE_TO_MUD));
         if (target == null) {
            return;
         }

         InvUtils.swap(waterBottle.slot(), (Boolean)this.swapBack.get());
         BlockUtils.interact(new BlockHitResult(Utils.vec3(target), Direction.UP, target, false), InteractionHand.MAIN_HAND, (Boolean)this.swingHand.get());
      } else if (emptyBottle.found() && (Boolean)this.fillBottle.get()) {
         BlockPos target = this.findBlockPos(blockState -> blockState.getFluidState().isSource());
         if (target == null) {
            return;
         }

         InvUtils.swap(emptyBottle.slot(), (Boolean)this.swapBack.get());
         Rotations.rotate(
            Rotations.getYaw(target), Rotations.getPitch(target), 10, true, () -> this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND)
         );
      }
   }
}
