package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixininterface.IInventoryTweaks;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ChestAura extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("rotate")).description("Rotates when opening.")).defaultValue(true)).build());
   private final Setting<Double> range = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("range"))
               .description("The interact range."))
            .defaultValue(4.0)
            .min(0.0)
            .build()
      );
   private final Setting<List<BlockEntityType<?>>> blocks = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StorageBlockListSetting.Builder)((meteordevelopment.meteorclient.settings.StorageBlockListSetting.Builder)new meteordevelopment.meteorclient.settings.StorageBlockListSetting.Builder()
                  .name("blocks"))
               .description("The blocks you open."))
            .defaultValue(new BlockEntityType[]{BlockEntityType.CHEST})
            .build()
      );
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay"))
                  .description("Delay between opening chests."))
               .defaultValue(10))
            .sliderMax(20)
            .build()
      );
   private final Setting<Integer> forget = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("forget-after"))
                  .description("How many ticks to wait before forgetting which chest to open. 0 for infinite."))
               .defaultValue(0))
            .min(0)
            .sliderMax(1000)
            .build()
      );
   private final Setting<ChestAura.CloseCondition> closeCondition = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("close-condition"))
                  .defaultValue(ChestAura.CloseCondition.IfEmpty))
               .description("When to close the chest screen."))
            .build()
      );
   private final Map<BlockPos, Integer> openedBlocks = new HashMap<>();
   private final ChestAura.CloseListener closeListener = new ChestAura.CloseListener();
   private int timer = 0;

   public ChestAura() {
      super(MeteorRejectsAddon.CATEGORY, "chest-aura", "Automatically open chests in radius");
   }

   public void onActivate() {
      this.timer = 0;
      this.openedBlocks.clear();
   }

   @EventHandler
   private void onTick(Pre event) {
      if ((Integer)this.forget.get() != 0) {
         for (Entry<BlockPos, Integer> e : new HashMap<>(this.openedBlocks).entrySet()) {
            int time = e.getValue();
            if (time > (Integer)this.forget.get()) {
               this.openedBlocks.remove(e.getKey());
            } else {
               this.openedBlocks.replace(e.getKey(), time + 1);
            }
         }
      }

      if (this.timer <= 0 || this.mc.screen == null) {
         for (BlockEntity block : Utils.blockEntities()) {
            if (((List)this.blocks.get()).contains(block.getType())
               && !(this.mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(block.getBlockPos())) >= (Double)this.range.get())) {
               BlockPos pos = block.getBlockPos();
               if (!this.openedBlocks.containsKey(pos)) {
                  Runnable click = () -> this.mc
                     .gameMode
                     .useItemOn(
                        this.mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false)
                     );
                  if ((Boolean)this.rotate.get()) {
                     Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), click);
                  } else {
                     click.run();
                  }

                  BlockState state = block.getBlockState();
                  if (state.hasProperty(ChestBlock.TYPE)) {
                     Direction direction = (Direction)state.getValue(ChestBlock.FACING);
                     switch ((ChestType)state.getValue(ChestBlock.TYPE)) {
                        case LEFT:
                           this.openedBlocks.put(pos.relative(direction.getClockWise()), 0);
                           break;
                        case RIGHT:
                           this.openedBlocks.put(pos.relative(direction.getCounterClockWise()), 0);
                     }
                  }

                  this.openedBlocks.put(pos, 0);
                  this.timer = (Integer)this.delay.get();
                  MeteorClient.EVENT_BUS.subscribe(this.closeListener);
                  break;
               }
            }
         }

         this.timer--;
      }
   }

   public static enum CloseCondition {
      Always,
      IfEmpty,
      AfterSteal,
      Never;
   }

   public class CloseListener {
      public CloseListener() {
         Objects.requireNonNull(ChestAura.this);
         super();
      }

      @EventHandler(
         priority = 100
      )
      private void onInventory(InventoryEvent event) {
         AbstractContainerMenu handler = ChestAura.this.mc.player.containerMenu;
         if (event.packet.containerId() == handler.containerId) {
            switch ((ChestAura.CloseCondition)ChestAura.this.closeCondition.get()) {
               case Always:
                  ChestAura.this.mc.player.closeContainer();
                  break;
               case IfEmpty:
                  NonNullList<ItemStack> stacks = NonNullList.create();
                  IntStream.range(0, SlotUtils.indexToId(9)).mapToObj(handler.slots::get).map(Slot::getItem).forEach(stacks::add);
                  if (stacks.stream().allMatch(ItemStack::isEmpty)) {
                     ChestAura.this.mc.player.closeContainer();
                  }
                  break;
               case AfterSteal:
                  ((IInventoryTweaks)Modules.get().get(InventoryTweaks.class)).stealCallback(() -> ChestAura.this.mc.player.closeContainer());
            }
         }

         MeteorClient.EVENT_BUS.unsubscribe(this);
      }
   }
}
