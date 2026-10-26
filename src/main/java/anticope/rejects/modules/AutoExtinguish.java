package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.concurrent.atomic.AtomicInteger;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class AutoExtinguish extends Module {
   private final SettingGroup sgGeneral = this.settings.createGroup("Extinguish Fire around you");
   private final SettingGroup sgBucket = this.settings.createGroup("Extinguish yourself");
   private final Setting<Boolean> extinguish = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("extinguish")).description("Automatically extinguishes fire around you.")).defaultValue(false))
            .build()
      );
   private final Setting<Integer> horizontalRadius = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("horizontal-radius"))
                  .description("Horizontal radius in which to search for fire."))
               .defaultValue(4))
            .min(0)
            .sliderMax(6)
            .build()
      );
   private final Setting<Integer> verticalRadius = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("vertical-radius"))
                  .description("Vertical radius in which to search for fire."))
               .defaultValue(4))
            .min(0)
            .sliderMax(6)
            .build()
      );
   private final Setting<Integer> maxBlockPerTick = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("block-per-tick"))
                  .description("Maximum amount of Blocks to extinguish per tick."))
               .defaultValue(5))
            .min(1)
            .sliderMax(50)
            .build()
      );
   private final Setting<Boolean> waterBucket = this.sgBucket
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("water"))
                  .description("Automatically places water when you are on fire (and don't have fire resistance)."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> center = this.sgBucket
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("center")).description("Automatically centers you when placing water.")).defaultValue(false)).build()
      );
   private final Setting<Boolean> onGround = this.sgBucket
      .add(((Builder)((Builder)((Builder)new Builder().name("on-ground")).description("Only place when you are on ground.")).defaultValue(false)).build());
   private boolean hasPlacedWater = false;
   private BlockPos blockPos = null;
   private boolean doesWaterBucketWork = true;
   private static final MobEffect FIRE_RESISTANCE = (MobEffect)BuiltInRegistries.MOB_EFFECT.getValue(Identifier.parse("fire_resistance"));

   public AutoExtinguish() {
      super(MeteorRejectsAddon.CATEGORY, "auto-extinguish", "Automatically extinguishes fire around you");
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.level.dimension() == Level.NETHER) {
         if (this.doesWaterBucketWork) {
            this.warning("Water Buckets don't work in this dimension!", new Object[0]);
            this.doesWaterBucketWork = false;
         }
      } else if (!this.doesWaterBucketWork) {
         this.warning("Enabled Water Buckets!", new Object[0]);
         this.doesWaterBucketWork = true;
      }

      if (!(Boolean)this.onGround.get() || this.mc.player.onGround()) {
         if ((Boolean)this.waterBucket.get() && this.doesWaterBucketWork) {
            if (this.hasPlacedWater) {
               int slot = this.findSlot(Items.BUCKET);
               this.blockPos = this.mc.player.blockPosition();
               this.place(slot);
               this.hasPlacedWater = false;
            } else if (!this.mc.player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(FIRE_RESISTANCE)) && this.mc.player.isOnFire()) {
               this.blockPos = this.mc.player.blockPosition();
               int slot = this.findSlot(Items.WATER_BUCKET);
               if (this.mc.level.getBlockState(this.blockPos).getBlock() == Blocks.FIRE
                  || this.mc.level.getBlockState(this.blockPos).getBlock() == Blocks.SOUL_FIRE) {
                  float yaw = this.mc.gameRenderer.getMainCamera().yRot() % 360.0F;
                  float pitch = this.mc.gameRenderer.getMainCamera().xRot() % 360.0F;
                  if ((Boolean)this.center.get()) {
                     PlayerUtils.centerPlayer();
                  }

                  Rotations.rotate(yaw, 90.0);
                  this.mc.getConnection().send(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, this.blockPos, Direction.UP));
                  this.mc.player.swing(InteractionHand.MAIN_HAND);
                  this.mc.getConnection().send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, Direction.UP));
                  Rotations.rotate(yaw, pitch);
               }

               this.place(slot);
               this.hasPlacedWater = true;
            }
         }

         if ((Boolean)this.extinguish.get()) {
            AtomicInteger blocksPerTick = new AtomicInteger();
            BlockIterator.register(
               (Integer)this.horizontalRadius.get(),
               (Integer)this.verticalRadius.get(),
               (blockPos, blockState) -> {
                  if (blocksPerTick.get() <= (Integer)this.maxBlockPerTick.get()
                     && (blockState.getBlock() == Blocks.FIRE || this.mc.level.getBlockState(blockPos).getBlock() == Blocks.SOUL_FIRE)) {
                     this.extinguishFire(blockPos);
                     blocksPerTick.getAndIncrement();
                  }
               }
            );
         }
      }
   }

   private void place(int slot) {
      if (slot != -1) {
         int preSlot = this.mc.player.getInventory().getSelectedSlot();
         if ((Boolean)this.center.get()) {
            PlayerUtils.centerPlayer();
         }

         this.mc.player.getInventory().setSelectedSlot(slot);
         float yaw = this.mc.gameRenderer.getMainCamera().yRot() % 360.0F;
         float pitch = this.mc.gameRenderer.getMainCamera().xRot() % 360.0F;
         Rotations.rotate(yaw, 90.0);
         this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND);
         this.mc.player.getInventory().setSelectedSlot(preSlot);
         Rotations.rotate(yaw, pitch);
      }
   }

   private void extinguishFire(BlockPos blockPos) {
      this.mc.getConnection().send(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
      this.mc.player.swing(InteractionHand.MAIN_HAND);
      this.mc.getConnection().send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
   }

   private int findSlot(Item item) {
      int slot = -1;

      for (int i = 0; i < 9; i++) {
         ItemStack block = this.mc.player.getInventory().getItem(i);
         if (block.getItem() == item) {
            slot = i;
            break;
         }
      }

      return slot;
   }
}
