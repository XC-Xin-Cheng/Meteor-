package fr.crazycat256.satellite.modules;

import fr.crazycat256.satellite.Addon;
import fr.crazycat256.satellite.utils.TPUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.ItemListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemFrameItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class AutoFrameDupe extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgSpeedrun = this.settings.createGroup("Speedrun");
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   public final Setting<List<Item>> dupeItems = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("items")).description("Items to dupe."))
               .defaultValue(
                  Arrays.asList(
                     Items.SHULKER_BOX,
                     Items.WHITE_SHULKER_BOX,
                     Items.ORANGE_SHULKER_BOX,
                     Items.MAGENTA_SHULKER_BOX,
                     Items.LIGHT_BLUE_SHULKER_BOX,
                     Items.YELLOW_SHULKER_BOX,
                     Items.LIME_SHULKER_BOX,
                     Items.PINK_SHULKER_BOX,
                     Items.GRAY_SHULKER_BOX,
                     Items.LIGHT_GRAY_SHULKER_BOX,
                     Items.CYAN_SHULKER_BOX,
                     Items.PURPLE_SHULKER_BOX,
                     Items.BLUE_SHULKER_BOX,
                     Items.BROWN_SHULKER_BOX,
                     Items.GREEN_SHULKER_BOX,
                     Items.RED_SHULKER_BOX,
                     Items.BLACK_SHULKER_BOX
                  )
               ))
            .build()
      );
   private final Setting<AutoFrameDupe.Mode> mode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("mode"))
                  .description("The mode to use when duping."))
               .defaultValue(AutoFrameDupe.Mode.Normal))
            .build()
      );
   private final Setting<Integer> minStackSize = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("min-stack-size"))
                     .description("The minimum stack size to use when duping."))
                  .defaultValue(5))
               .min(1)
               .sliderMax(64)
               .visible(() -> this.mode.get() == AutoFrameDupe.Mode.Fast))
            .build()
      );
   private final Setting<Boolean> replaceItemFrames = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("replace-item-frames"))
                     .description("Replaces item frames when they are dropped."))
                  .defaultValue(true))
               .visible(() -> this.mode.get() == AutoFrameDupe.Mode.Fast))
            .build()
      );
   private final Setting<Integer> maxPlacements = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("max-placements"))
                  .description("The maximum number of item to place per tick."))
               .defaultValue(12))
            .min(0)
            .sliderRange(1, 32)
            .build()
      );
   private final Setting<Integer> maxSwaps = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("max-swaps"))
                  .description("The maximum number of item swaps to perform per tick."))
               .defaultValue(8))
            .min(0)
            .sliderRange(1, 8)
            .build()
      );
   private final Setting<Integer> maxInventoryMoves = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("max-inventory-moves"))
                  .description("The maximum number of item moves from inventory to hotbar to perform per tick (recommended to leave at 0)."))
               .defaultValue(0))
            .min(0)
            .sliderMax(20)
            .build()
      );
   private final Setting<Boolean> chronometer = this.sgSpeedrun
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("chronometer"))
                  .description("Prints the time elapsed since the start of the dupe."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> autoDisable = this.sgSpeedrun
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("auto-disable"))
                  .description("Automatically stops the dupe when you have enough items."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Integer> itemCount = this.sgSpeedrun
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("item-count"))
                     .description("The number of items to dupe."))
                  .defaultValue(2304))
               .min(0)
               .sliderMax(3000)
               .visible(this.autoDisable::get))
            .build()
      );
   private final Setting<Boolean> renderPlace = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("render-empty"))
                  .description("Renders the item frames where the items will be placed."))
               .defaultValue(true))
            .build()
      );
   private final Setting<SettingColor> emptyColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("empty-color"))
                  .description("The color of the item frame where the item will be placed."))
               .defaultValue(new Color(0, 255, 0, 32))
               .visible(this.renderPlace::get))
            .build()
      );
   private final Setting<Boolean> renderDrop = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("render-filled"))
                  .description("Renders the item frames where the items will be dropped."))
               .defaultValue(true))
            .build()
      );
   private final Setting<SettingColor> filledColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("filled-color"))
                  .description("The color of the item frame where the item will be dropped."))
               .defaultValue(new Color(255, 0, 0, 32))
               .visible(this.renderDrop::get))
            .build()
      );
   private final List<ItemFrame> reachableItemFrames = new ArrayList<>();
   private final List<ItemFrame> toReplace = new ArrayList<>();
   private final List<ItemFrame> dontHit = new ArrayList<>();
   private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
   private long startTime = 0L;

   public AutoFrameDupe() {
      super(Addon.CATEGORY, "auto-frame-dupe", "Automate Cafouillage item frame dupe.");
   }

   public void onActivate() {
      this.startTime = System.currentTimeMillis();
      if ((Boolean)this.chronometer.get()) {
         this.info("Started at §f" + this.sdf.format(this.startTime), new Object[0]);
      }

      this.reachableItemFrames.clear();
      this.toReplace.clear();
      this.dontHit.clear();
   }

   public void onDeactivate() {
      if ((Boolean)this.chronometer.get()) {
         long stopTime = System.currentTimeMillis();
         this.info("Stopped at §f" + this.sdf.format(stopTime), new Object[0]);
         this.info("Duration: §f" + DurationFormatUtils.formatDurationWords(stopTime - this.startTime, true, true), new Object[0]);
      }

      if (this.mc.player != null && this.mc.level != null && this.mc.gameMode != null) {
         MeteorExecutor.execute(
            () -> {
               try {
                  Thread.sleep(100L);
               } catch (InterruptedException var8) {
               }

               List<ItemFrame> itemFrames = new ArrayList<>();
               Inventory inv = this.mc.player.getInventory();

               for (Entity entity : this.mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemFrame itemFrame && entity.distanceToSqr(this.mc.player) < 25.0) {
                     if (((List)this.dupeItems.get()).contains(itemFrame.getItem().getItem())) {
                        this.mc.gameMode.attack(this.mc.player, itemFrame);
                     }

                     itemFrames.add(itemFrame);
                  }
               }

               if (this.mode.get() == AutoFrameDupe.Mode.Fast && (Boolean)this.replaceItemFrames.get()) {
                  for (ItemFrame itemFrame : this.reachableItemFrames) {
                     if (itemFrame.distanceToSqr(this.mc.player) < 25.0 && !itemFrames.contains(itemFrame)) {
                        this.toReplace.add(itemFrame);
                     }
                  }

                  label101:
                  for (ItemFrame itemFramex : this.toReplace) {
                     for (ItemFrame existingItemFrame : itemFrames) {
                        if (existingItemFrame.position().equals(itemFramex.position()) && existingItemFrame.getDirection() == itemFramex.getDirection()) {
                           continue label101;
                        }
                     }

                     BlockPos pos = TPUtils.vec3ToBlockPos(itemFramex.position().subtract(itemFramex.getLookAngle().normalize()));
                     if (inv.getItem(40).getItem() instanceof ItemFrameItem) {
                        this.mc
                           .gameMode
                           .useItemOn(this.mc.player, InteractionHand.OFF_HAND, new BlockHitResult(Vec3.atCenterOf(pos), itemFramex.getDirection(), pos, false));
                     } else {
                        boolean swapped = false;
                        if (!(inv.getSelectedItem().getItem() instanceof ItemFrameItem)) {
                           for (int i = 0; i < 9; i++) {
                              if (inv.getItem(i).getItem() instanceof ItemFrameItem) {
                                 InvUtils.swap(i, false);
                                 swapped = true;
                                 break;
                              }
                           }
                        }

                        if (!(inv.getSelectedItem().getItem() instanceof ItemFrameItem) && !swapped) {
                           for (int ix = 0; ix < inv.getContainerSize(); ix++) {
                              if (inv.getItem(ix).getItem() instanceof ItemFrameItem) {
                                 InvUtils.move().from(ix).toHotbar(inv.getSelectedSlot());
                                 break;
                              }
                           }
                        }

                        if (inv.getSelectedItem().getItem() instanceof ItemFrameItem) {
                           this.mc
                              .gameMode
                              .useItemOn(
                                 this.mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), itemFramex.getDirection(), pos, false)
                              );
                           ItemStack stack = this.mc.player.getItemInHand(InteractionHand.MAIN_HAND).copy();
                           stack.setCount(stack.getCount() - 1);
                           if (stack.getCount() == 0) {
                              stack = ItemStack.EMPTY;
                           }

                           this.mc.player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                        }
                     }
                  }
               }
            }
         );
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null && this.mc.level != null && this.mc.gameMode != null) {
         if ((Boolean)this.chronometer.get()) {
            this.mc
               .gui
               .setOverlayMessage(
                  Component.literal("Duration: §f" + DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - this.startTime, true, true)), false
               );
         }

         Inventory inv = this.mc.player.getInventory();
         if ((Boolean)this.autoDisable.get()) {
            int count = 0;

            for (Item item : (List)this.dupeItems.get()) {
               count += this.countInventoryItem(inv, item);
            }

            for (Entity entity : this.mc.level.entitiesForRendering()) {
               if (entity instanceof ItemEntity item
                  && entity.distanceToSqr(this.mc.player) < 49.0
                  && ((List)this.dupeItems.get()).contains(item.getItem().getItem())) {
                  count += item.getItem().getCount();
               }

               if (entity instanceof ItemFrame itemFrame
                  && entity.distanceToSqr(this.mc.player) < 25.0
                  && ((List)this.dupeItems.get()).contains(itemFrame.getItem().getItem())) {
                  count++;
               }
            }

            if (count >= (Integer)this.itemCount.get()) {
               this.info("You have reached the item count, disabled.", new Object[0]);
               this.toggle();
               return;
            }
         }

         if (this.mode.get() == AutoFrameDupe.Mode.Normal) {
            List<ItemFrame> emptyItemFrames = new ArrayList<>();
            List<ItemFrame> filledItemFrames = new ArrayList<>();

            for (Entity entity : this.mc.level.entitiesForRendering()) {
               if (entity instanceof ItemFrame itemFrame) {
                  if (itemFrame.getItem().getItem() == Items.AIR) {
                     emptyItemFrames.add(itemFrame);
                  } else if (((List)this.dupeItems.get()).contains(itemFrame.getItem().getItem())) {
                     filledItemFrames.add(itemFrame);
                  }
               }
            }

            int placements = 0;
            int swaps = 0;
            int moves = 0;
            boolean swapped = false;

            for (ItemFrame emptyItemFrame : emptyItemFrames) {
               if (!((List)this.dupeItems.get()).contains(inv.getSelectedItem().getItem()) && swaps < (Integer)this.maxSwaps.get()) {
                  for (int i = 0; i < 9; i++) {
                     if (((List)this.dupeItems.get()).contains(inv.getItem(i).getItem())) {
                        InvUtils.swap(i, false);
                        swaps++;
                        swapped = true;
                        break;
                     }
                  }
               }

               if (!((List)this.dupeItems.get()).contains(inv.getSelectedItem().getItem()) && moves < (Integer)this.maxInventoryMoves.get() && !swapped) {
                  for (int ix = 0; ix < inv.getContainerSize(); ix++) {
                     if (((List)this.dupeItems.get()).contains(inv.getItem(ix).getItem())) {
                        InvUtils.move().from(ix).toHotbar(inv.getSelectedSlot());
                        moves++;
                        break;
                     }
                  }
               }

               if (((List)this.dupeItems.get()).contains(inv.getSelectedItem().getItem()) && placements < (Integer)this.maxPlacements.get()) {
                  this.interactItemFrameEntity(emptyItemFrame);
                  ItemStack stack = this.mc.player.getItemInHand(InteractionHand.MAIN_HAND).copy();
                  stack.setCount(stack.getCount() - 1);
                  if (stack.getCount() == 0) {
                     stack = ItemStack.EMPTY;
                  }

                  this.mc.player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                  this.dontHit.remove(emptyItemFrame);
                  placements++;
               }
            }

            for (ItemFrame itemFramex : filledItemFrames) {
               if (!this.dontHit.contains(itemFramex)) {
                  this.mc.gameMode.attack(this.mc.player, itemFramex);
                  this.dontHit.add(itemFramex);
               }
            }
         } else if (this.mode.get() == AutoFrameDupe.Mode.Fast) {
            List<ItemFrame> itemFrames = new ArrayList<>();

            for (Entity entityx : this.mc.level.entitiesForRendering()) {
               if (entityx instanceof ItemFrame itemFramexx && itemFramexx.distanceToSqr(this.mc.player) < 25.0) {
                  if (itemFramexx.getItem().getItem() == Items.AIR) {
                     itemFrames.add(itemFramexx);
                  } else if (((List)this.dupeItems.get()).contains(itemFramexx.getItem().getItem())) {
                     itemFrames.add(itemFramexx);
                  }
               }
            }

            int placements = 0;
            int swaps = 0;
            int moves = 0;
            if ((Boolean)this.replaceItemFrames.get()) {
               for (ItemFrame itemFramexxx : List.copyOf(this.reachableItemFrames)) {
                  if (itemFramexxx.distanceToSqr(this.mc.player) > 25.0) {
                     this.reachableItemFrames.remove(itemFramexxx);
                  } else if (!itemFrames.contains(itemFramexxx)) {
                     this.reachableItemFrames.remove(itemFramexxx);
                     this.toReplace.add(itemFramexxx);
                  }
               }

               label282:
               for (ItemFrame itemFramexxxx : List.copyOf(this.toReplace)) {
                  for (ItemFrame existingItemFrame : itemFrames) {
                     if (existingItemFrame.position().equals(itemFramexxxx.position()) && existingItemFrame.getDirection() == itemFramexxxx.getDirection()) {
                        this.toReplace.remove(itemFramexxxx);
                        continue label282;
                     }
                  }

                  BlockPos pos = TPUtils.vec3ToBlockPos(itemFramexxxx.position().subtract(itemFramexxxx.getLookAngle().normalize()));
                  if (inv.getItem(40).getItem() instanceof ItemFrameItem) {
                     this.mc
                        .gameMode
                        .useItemOn(this.mc.player, InteractionHand.OFF_HAND, new BlockHitResult(Vec3.atCenterOf(pos), itemFramexxxx.getDirection(), pos, false));
                     placements++;
                  } else {
                     boolean swapped = false;
                     if (!(inv.getSelectedItem().getItem() instanceof ItemFrameItem)) {
                        for (int ixx = 0; ixx < 9; ixx++) {
                           if (inv.getItem(ixx).getItem() instanceof ItemFrameItem) {
                              InvUtils.swap(ixx, false);
                              swaps++;
                              swapped = true;
                              break;
                           }
                        }
                     }

                     if (!(inv.getSelectedItem().getItem() instanceof ItemFrameItem) && !swapped) {
                        for (int ixxx = 0; ixxx < inv.getContainerSize(); ixxx++) {
                           if (inv.getItem(ixxx).getItem() instanceof ItemFrameItem) {
                              InvUtils.move().from(ixxx).toHotbar(inv.getSelectedSlot());
                              moves++;
                              break;
                           }
                        }
                     }

                     if (inv.getSelectedItem().getItem() instanceof ItemFrameItem) {
                        this.mc
                           .gameMode
                           .useItemOn(
                              this.mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), itemFramexxxx.getDirection(), pos, false)
                           );
                        ItemStack stack = this.mc.player.getItemInHand(InteractionHand.MAIN_HAND).copy();
                        stack.setCount(stack.getCount() - 1);
                        if (stack.getCount() == 0) {
                           stack = ItemStack.EMPTY;
                        }

                        this.mc.player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                     }
                  }
               }
            }

            this.reachableItemFrames.clear();
            this.reachableItemFrames.addAll(itemFrames);
            boolean swappedx = false;

            for (ItemFrame itemFramexxxx : itemFrames) {
               if ((
                     !((List)this.dupeItems.get()).contains(inv.getSelectedItem().getItem())
                        || inv.getSelectedItem().getCount() < (Integer)this.minStackSize.get()
                  )
                  && swaps < (Integer)this.maxSwaps.get()) {
                  for (int ixxxx = 0; ixxxx < 9; ixxxx++) {
                     if (((List)this.dupeItems.get()).contains(inv.getItem(ixxxx).getItem())
                        && inv.getItem(ixxxx).getCount() >= (Integer)this.minStackSize.get()) {
                        InvUtils.swap(ixxxx, false);
                        swappedx = true;
                        swaps++;
                        break;
                     }
                  }
               }

               if ((
                     !((List)this.dupeItems.get()).contains(inv.getSelectedItem().getItem())
                        || inv.getSelectedItem().getCount() < (Integer)this.minStackSize.get()
                  )
                  && moves < (Integer)this.maxInventoryMoves.get()
                  && !swappedx) {
                  for (int ixxxxx = 0; ixxxxx < inv.getContainerSize(); ixxxxx++) {
                     if (((List)this.dupeItems.get()).contains(inv.getItem(ixxxxx).getItem())
                        && inv.getItem(ixxxxx).getCount() >= (Integer)this.minStackSize.get()) {
                        InvUtils.move().from(ixxxxx).toHotbar(inv.getSelectedSlot());
                        moves++;
                        swappedx = true;
                        break;
                     }
                  }
               }

               if (!swappedx) {
                  for (int ixxxxxx = 0; ixxxxxx < 9; ixxxxxx++) {
                     if (((List)this.dupeItems.get()).contains(inv.getItem(ixxxxxx).getItem())) {
                        InvUtils.swap(ixxxxxx, false);
                        swappedx = true;
                        swaps++;
                        break;
                     }
                  }
               }

               if (!swappedx) {
                  for (int ixxxxxxx = 0; ixxxxxxx < inv.getContainerSize(); ixxxxxxx++) {
                     if (((List)this.dupeItems.get()).contains(inv.getItem(ixxxxxxx).getItem())) {
                        InvUtils.move().from(ixxxxxxx).toHotbar(inv.getSelectedSlot());
                        moves++;
                        swappedx = true;
                        break;
                     }
                  }
               }

               if (((List)this.dupeItems.get()).contains(inv.getSelectedItem().getItem())
                  && itemFramexxxx.getItem().getItem() == Items.AIR
                  && placements < (Integer)this.maxPlacements.get()) {
                  this.interactItemFrame(itemFramexxxx);
                  this.dontHit.remove(itemFramexxxx);
                  placements++;
               } else if (!this.dontHit.contains(itemFramexxxx)
                  && ((List)this.dupeItems.get()).contains(itemFramexxxx.getItem().getItem())
                  && placements < (Integer)this.maxPlacements.get()) {
                  this.dontHit.add(itemFramexxxx);
                  this.mc.gameMode.attack(this.mc.player, itemFramexxxx);
                  if (((List)this.dupeItems.get()).contains(inv.getSelectedItem().getItem())) {
                     this.interactItemFrame(itemFramexxxx);
                     this.dontHit.remove(itemFramexxxx);
                     placements++;
                  }
               }
            }
         }
      }
   }

   private void interactItemFrame(ItemFrame itemFrame) {
      this.interactItemFrameEntity(itemFrame);
      ItemStack stack = this.mc.player.getItemInHand(InteractionHand.MAIN_HAND).copy();
      stack.setCount(stack.getCount() - 1);
      if (stack.getCount() == 0) {
         stack = ItemStack.EMPTY;
      }

      this.mc.player.setItemInHand(InteractionHand.MAIN_HAND, stack);
   }

   private void interactItemFrameEntity(ItemFrame itemFrame) {
      this.mc.gameMode.interact(this.mc.player, itemFrame, new EntityHitResult(itemFrame, itemFrame.getBoundingBox().getCenter()), InteractionHand.MAIN_HAND);
   }

   private int countInventoryItem(Inventory inv, Item item) {
      int count = 0;

      for (int i = 0; i < inv.getContainerSize(); i++) {
         ItemStack stack = inv.getItem(i);
         if (stack.getItem() == item) {
            count += stack.getCount();
         }
      }

      return count;
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.mc.player != null && this.mc.level != null) {
         for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (entity instanceof ItemFrame itemFrame) {
               if ((Boolean)this.renderPlace.get() && itemFrame.getItem().getItem() == Items.AIR) {
                  this.renderItemFrame(event.renderer, itemFrame, (Color)this.emptyColor.get());
               } else if ((Boolean)this.renderDrop.get() && ((List)this.dupeItems.get()).contains(itemFrame.getItem().getItem())) {
                  this.renderItemFrame(event.renderer, itemFrame, (Color)this.filledColor.get());
               }
            }
         }
      }
   }

   private void renderItemFrame(Renderer3D renderer, ItemFrame itemFrame, Color color) {
      Vec3 pos = itemFrame.position();
      renderer.boxSides(pos.x - 0.25, pos.y - 0.25, pos.z - 0.25, pos.x + 0.25, pos.y + 0.25, pos.z + 0.25, color, 0);
   }

   public static enum Mode {
      Normal,
      Fast;
   }
}
