package zgoly.meteorist.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.utils.baritone.MeteoristBaritoneUtils;

public class ItemSucker extends Module {
   private final SettingGroup sgFilter = this.settings.createGroup("Filter");
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<Boolean> onlyPickupable = this.sgFilter
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("only-pickupable")).description("Only pickup items that can be picked up.")).defaultValue(true))
            .build()
      );
   private final Setting<ItemSucker.OperationMode> itemFilteringMode = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("item-filtering-mode"))
                  .description("Defines how items will be filtered when using the item sucker."))
               .defaultValue(ItemSucker.OperationMode.Blacklist))
            .build()
      );
   private final Setting<List<Item>> itemWhitelist = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)new meteordevelopment.meteorclient.settings.ItemListSetting.Builder()
                     .name("item-whitelist"))
                  .description("Items to be exclusively picked up by the item sucker."))
               .defaultValue(new Item[]{Items.DIAMOND})
               .visible(() -> this.itemFilteringMode.get() == ItemSucker.OperationMode.Whitelist))
            .build()
      );
   private final Setting<List<Item>> itemBlacklist = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)new meteordevelopment.meteorclient.settings.ItemListSetting.Builder()
                     .name("item-blacklist"))
                  .description("Items which the item sucker should ignore."))
               .defaultValue(new Item[]{Items.POISONOUS_POTATO})
               .visible(() -> this.itemFilteringMode.get() == ItemSucker.OperationMode.Blacklist))
            .build()
      );
   private final Setting<Double> suckingRange = this.sgFilter
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("sucking-range"))
               .description("Range within which the Baritone can pick up items."))
            .defaultValue(5.0)
            .min(1.0)
            .sliderRange(1.0, 25.0)
            .build()
      );
   private final Setting<Boolean> onlyOnGround = this.sgFilter
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("only-on-ground")).description("Only pick up items that are on the floor.")).defaultValue(true))
            .build()
      );
   private final Setting<ItemSucker.MoveMode> moveMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("move-mode"))
                  .description("Set the move mode of the item sucker."))
               .defaultValue(ItemSucker.MoveMode.Teleport))
            .build()
      );
   private final Setting<Boolean> checkCollisions = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("check-collisions"))
                     .description("Check if player can teleport to an item and not collide with blocks."))
                  .defaultValue(true))
               .visible(() -> this.moveMode.get() == ItemSucker.MoveMode.Teleport))
            .build()
      );
   private final Setting<Integer> itemRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("item-range"))
                     .description("The radius within which Baritone will attempt to pick up items (relative to the item's position)."))
                  .defaultValue(0))
               .min(0)
               .visible(() -> this.moveMode.get() == ItemSucker.MoveMode.Baritone))
            .build()
      );
   private final Setting<Boolean> tpToOrigin = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("tp-to-origin"))
                     .description("Automatically teleport player to initial position once all items have been picked up."))
                  .defaultValue(true))
               .visible(() -> this.moveMode.get() == ItemSucker.MoveMode.Teleport))
            .build()
      );
   private final Setting<Integer> waitTime = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("wait-time"))
                     .description("Time to wait after teleport (in ticks)."))
                  .min(1)
                  .defaultValue(10))
               .visible(() -> this.moveMode.get() == ItemSucker.MoveMode.Teleport && (Boolean)this.tpToOrigin.get()))
            .build()
      );
   private final Setting<Boolean> resetTimeAfterTp = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("reset-time-after-tp")).description("Resets wait time after teleport.")).defaultValue(true))
               .visible(() -> this.moveMode.get() == ItemSucker.MoveMode.Teleport && (Boolean)this.tpToOrigin.get()))
            .build()
      );
   private final Setting<Boolean> returnToOrigin = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("return-to-origin"))
                     .description("Automatically return player to initial position once all items have been picked up."))
                  .defaultValue(true))
               .visible(() -> this.moveMode.get() == ItemSucker.MoveMode.Baritone))
            .build()
      );
   private final Setting<Integer> returnRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("return-range"))
                     .description("The radius within which Baritone will return to its initial position (relative to the initial position)."))
                  .defaultValue(0))
               .min(0)
               .visible(() -> this.moveMode.get() == ItemSucker.MoveMode.Baritone && (Boolean)this.returnToOrigin.get()))
            .build()
      );
   private final Setting<Boolean> modifySpeed = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("modify-speed"))
                     .description("Whether or not the speed of the player should be altered when using Baritone."))
                  .defaultValue(true))
               .visible(() -> this.moveMode.get() == ItemSucker.MoveMode.Baritone))
            .build()
      );
   private final Setting<Double> moveSpeed = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("move-speed"))
                  .description("Modifies the player's movement speed when 'Modify Speed' is enabled."))
               .defaultValue(10.0)
               .min(1.0)
               .sliderRange(1.0, 30.0)
               .visible(() -> this.moveMode.get() == ItemSucker.MoveMode.Baritone && (Boolean)this.modifySpeed.get()))
            .build()
      );
   private final Setting<Boolean> disableOnItemCount = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("disable-on-item-count"))
                  .description("Disables the module when a certain number of items are picked up."))
               .defaultValue(false))
            .build()
      );
   private final Setting<ItemSucker.ItemCountMode> itemCountMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("item-count-mode"))
                     .description("Defines how the maximum number of items to pick up is calculated."))
                  .defaultValue(ItemSucker.ItemCountMode.Stacks))
               .visible(this.disableOnItemCount::get))
            .build()
      );
   private final Setting<Integer> maxItemCount = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("max-item-count"))
                     .description("Maximum number of items to pick up."))
                  .defaultValue(10))
               .min(1)
               .sliderRange(1, 10)
               .visible(this.disableOnItemCount::get))
            .build()
      );
   private final Setting<Integer> maxItemsAtOnce = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("max-item-at-once"))
                  .description("Maximum number of hitboxes to render at once."))
               .defaultValue(10))
            .min(1)
            .sliderRange(1, 10)
            .build()
      );
   private final Setting<Boolean> showTeleportBox = this.sgRender
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("show-teleport-box"))
                  .description("Displays player hitbox at items position when using \"Teleport\" move mode."))
               .defaultValue(true))
            .build()
      );
   private final Setting<SettingColor> sideColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("side-color"))
                  .description("The color of the sides of box."))
               .defaultValue(new SettingColor(0, 0, 255, 40))
               .visible(this.showTeleportBox::get))
            .build()
      );
   private final Setting<SettingColor> lineColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("line-color"))
                  .description("The color of the lines of box."))
               .defaultValue(new SettingColor(0, 0, 255, 100))
               .visible(this.showTeleportBox::get))
            .build()
      );
   int timer = 0;
   Vec3 startPos = null;
   int pickedUpStacksCount = 0;
   int pickedUpItemsCount = 0;
   MeteoristBaritoneUtils baritoneUtils = new MeteoristBaritoneUtils();

   public ItemSucker() {
      super(Meteorist.CATEGORY, "item-sucker", "Automatically picks up dropped items.");
   }

   public void onActivate() {
      this.timer = 0;
      this.startPos = null;
      this.pickedUpStacksCount = 0;
      this.pickedUpItemsCount = 0;
      this.baritoneUtils.cancelEverything();
   }

   public void onDeactivate() {
      this.baritoneUtils.cancelEverything();
   }

   public AABB getBoundingBoxAtPosition(Vec3 pos) {
      Vec3 offset = pos.subtract(this.mc.player.getBoundingBox().getBottomCenter());
      return this.mc.player.getBoundingBox().move(offset.x(), offset.y(), offset.z());
   }

   public boolean canTeleportToItem(Vec3 pos) {
      AABB box = this.getBoundingBoxAtPosition(pos);
      Iterable<VoxelShape> collisions = this.mc.level.getBlockCollisions(this.mc.player, box);
      List<VoxelShape> collisionsList = StreamSupport.stream(collisions.spliterator(), false).filter(voxelShape -> !voxelShape.isEmpty()).toList();
      return collisionsList.isEmpty();
   }

   private boolean filter(Entity entity) {
      if (!(entity instanceof ItemEntity itemEntity)) {
         return false;
      } else {
         boolean isPickupable = true;
         if ((Boolean)this.onlyPickupable.get()) {
            isPickupable = !itemEntity.hasPickUpDelay();
         }

         boolean isWithinRange = PlayerUtils.isWithin(entity, (Double)this.suckingRange.get());
         boolean isOnGround = true;
         if ((Boolean)this.onlyOnGround.get()) {
            isOnGround = entity.onGround();
         }

         boolean isItemAllowed = this.itemFilteringMode.get() == ItemSucker.OperationMode.Blacklist
               && !((List)this.itemBlacklist.get()).contains(itemEntity.getItem().getItem())
            || this.itemFilteringMode.get() == ItemSucker.OperationMode.Whitelist && ((List)this.itemWhitelist.get()).contains(itemEntity.getItem().getItem());
         boolean canTeleport = true;
         if (this.moveMode.get() == ItemSucker.MoveMode.Teleport) {
            canTeleport = (Boolean)this.checkCollisions.get() && this.canTeleportToItem(itemEntity.position());
         }

         return isPickupable && isWithinRange && isOnGround && isItemAllowed && canTeleport;
      }
   }

   @EventHandler
   public void onMove(PlayerMoveEvent event) {
      if ((Boolean)this.modifySpeed.get() && this.baritoneUtils.isPathing()) {
         Vec3 vel = PlayerUtils.getHorizontalVelocity((Double)this.moveSpeed.get());
         ((IVec3)event.movement).meteor$set(vel.x(), event.movement.y, vel.z());
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      if ((Boolean)this.disableOnItemCount.get()) {
         int currentItemCount = this.itemCountMode.get() == ItemSucker.ItemCountMode.Stacks ? this.pickedUpStacksCount : this.pickedUpItemsCount;
         if (currentItemCount > (Integer)this.maxItemCount.get()) {
            this.toggle();
         }
      }

      if (this.moveMode.get() == ItemSucker.MoveMode.Teleport) {
         Entity target = TargetUtils.get(this::filter, SortPriority.LowestDistance);
         if (this.timer > 0) {
            this.timer--;
         }

         if (target != null) {
            if ((Boolean)this.tpToOrigin.get()) {
               if ((Boolean)this.resetTimeAfterTp.get()) {
                  this.timer = (Integer)this.waitTime.get();
               }

               if (this.startPos == null) {
                  this.startPos = this.mc.player.position();
               }
            }

            this.mc.player.setPos(target.getX(), target.getY(), target.getZ());
         }

         if (this.timer <= 0 && (Boolean)this.tpToOrigin.get() && this.startPos != null) {
            this.mc.player.setPos(this.startPos.x(), this.startPos.y(), this.startPos.z());
            this.startPos = null;
            this.timer = (Integer)this.waitTime.get();
         }
      } else if (BaritoneUtils.IS_AVAILABLE) {
         List<Entity> targets = new ArrayList<>();
         TargetUtils.getList(targets, this::filter, SortPriority.LowestDistance, (Integer)this.maxItemsAtOnce.get());
         if (!targets.isEmpty()) {
            this.baritoneUtils.setGoalNear(targets, (Integer)this.itemRange.get());
            if ((Boolean)this.returnToOrigin.get() && this.startPos == null) {
               this.startPos = this.mc.player.blockPosition().getCenter();
            }
         } else if ((Boolean)this.returnToOrigin.get() && this.startPos != null) {
            this.baritoneUtils.setGoalNear(BlockPos.containing(this.startPos), (Integer)this.returnRange.get());
            this.startPos = null;
         }
      } else {
         ((OkPrompt)((OkPrompt)((OkPrompt)OkPrompt.create().title("Baritone is not available"))
                  .message("Looks like Baritone is not installed. Install Baritone to use this move mode."))
               .dontShowAgainCheckboxVisible(false))
            .show();
         this.moveMode.set(ItemSucker.MoveMode.Teleport);
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.moveMode.get() == ItemSucker.MoveMode.Teleport) {
         List<Entity> entities = new ArrayList<>();
         TargetUtils.getList(entities, this::filter, SortPriority.LowestDistance, (Integer)this.maxItemsAtOnce.get());
         entities.forEach(
            entity -> event.renderer
               .box(this.getBoundingBoxAtPosition(entity.position()), (Color)this.sideColor.get(), (Color)this.lineColor.get(), ShapeMode.Both, 0)
         );
      }
   }

   @EventHandler
   public void onItemPickup(Receive event) {
      if (event.packet instanceof ClientboundTakeItemEntityPacket packet) {
         this.pickedUpStacksCount++;
         this.pickedUpItemsCount = this.pickedUpItemsCount + packet.getAmount();
      }
   }

   public static enum ItemCountMode {
      Stacks,
      Items;
   }

   public static enum MoveMode {
      Teleport,
      Baritone;
   }

   public static enum OperationMode {
      Whitelist,
      Blacklist;
   }
}
