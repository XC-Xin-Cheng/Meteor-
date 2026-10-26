package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoTNT extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> ignite = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("ignite")).description("Whether to ignite tnt.")).defaultValue(true)).build());
   private final Setting<Integer> igniteDelay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("ignition-delay"))
                     .description("Delay in ticks between ignition"))
                  .defaultValue(1))
               .visible(this.ignite::get))
            .build()
      );
   private final Setting<Integer> horizontalRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("horizontal-range"))
                  .description("Horizontal range of ignition"))
               .defaultValue(4))
            .build()
      );
   private final Setting<Integer> verticalRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("vertical-range"))
                  .description("Vertical range of ignition"))
               .defaultValue(4))
            .build()
      );
   private final Setting<Boolean> antiBreak = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("anti-break")).description("Whether to save flint and steel from breaking."))
                  .defaultValue(true))
               .visible(this.ignite::get))
            .build()
      );
   private final Setting<Integer> durability = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("durability"))
                     .description("Decides the amount of durability to leave on the flint and steel"))
                  .min(1)
                  .max(63)
                  .sliderMax(63)
                  .defaultValue(10))
               .visible(this.antiBreak::get))
            .build()
      );
   private final Setting<Boolean> fireCharge = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("fire-charge")).description("Whether to also use fire charges.")).defaultValue(true))
               .visible(this.ignite::get))
            .build()
      );
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("rotate")).description("Whether to rotate towards action.")).defaultValue(true)).build());
   private final List<MutableBlockPos> blocksToIgnite = new ArrayList<>();
   private final Pool<MutableBlockPos> ignitePool = new Pool(MutableBlockPos::new);
   private int igniteTick = 0;

   public AutoTNT() {
      super(MeteorRejectsAddon.CATEGORY, "auto-tnt", "Ignites tnt automatically. Good for griefing.");
   }

   public void onDeactivate() {
      this.igniteTick = 0;
   }

   @EventHandler
   private void onPreTick(Pre event) {
      if ((Boolean)this.ignite.get() && this.igniteTick > (Integer)this.igniteDelay.get()) {
         for (MutableBlockPos blockPos : this.blocksToIgnite) {
            this.ignitePool.free(blockPos);
         }

         this.blocksToIgnite.clear();
         BlockIterator.register((Integer)this.horizontalRange.get(), (Integer)this.verticalRange.get(), (blockPosx, blockState) -> {
            if (blockState.getBlock() instanceof TntBlock) {
               this.blocksToIgnite.add(((MutableBlockPos)this.ignitePool.get()).set(blockPosx));
            }
         });
      }
   }

   @EventHandler
   private void onPostTick(Post event) {
      if ((Boolean)this.ignite.get() && !this.blocksToIgnite.isEmpty() && this.igniteTick > (Integer)this.igniteDelay.get()) {
         this.blocksToIgnite.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
         FindItemResult itemResult = InvUtils.findInHotbar(item -> {
            if (!(item.getItem() instanceof FlintAndSteelItem)) {
               return item.getItem() instanceof FireChargeItem ? (Boolean)this.fireCharge.get() : false;
            } else {
               return !(Boolean)this.antiBreak.get() || item.getMaxDamage() - item.getDamageValue() > (Integer)this.durability.get();
            }
         });
         if (!itemResult.found()) {
            this.error("No flint and steel in hotbar or durability too low.", new Object[0]);
            this.toggle();
            return;
         }

         this.ignite((BlockPos)this.blocksToIgnite.get(0), itemResult);
         this.igniteTick = 0;
      }

      this.igniteTick++;
   }

   private void ignite(BlockPos pos, FindItemResult item) {
      Runnable action = () -> {
         InvUtils.swap(item.slot(), true);
         this.mc
            .gameMode
            .useItemOn(
               this.mc.player,
               InteractionHand.MAIN_HAND,
               new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true)
            );
         InvUtils.swapBack();
      };
      if ((Boolean)this.rotate.get()) {
         Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), action);
      } else {
         action.run();
      }
   }
}
