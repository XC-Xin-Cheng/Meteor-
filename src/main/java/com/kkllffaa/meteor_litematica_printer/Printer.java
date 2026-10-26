package com.kkllffaa.meteor_litematica_printer;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

public class Printer extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgWorkMode = this.settings.createGroup("Work Mode");
   private final SettingGroup sgRendering = this.settings.createGroup("Rendering");
   private final Setting<Integer> printing_range = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("printing-range")).description("The block place range.")).defaultValue(2))
            .min(1)
            .sliderMin(1)
            .max(60)
            .sliderMax(6)
            .build()
      );
   private final Setting<Integer> printing_delay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("printing-delay")).description("Delay between printing blocks in ticks.")).defaultValue(2))
            .min(0)
            .sliderMin(0)
            .max(100)
            .sliderMax(40)
            .build()
      );
   private final Setting<Integer> bpt = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("blocks/tick")).description("How many blocks place per tick.")).defaultValue(1))
            .min(1)
            .sliderMin(1)
            .max(100)
            .sliderMax(100)
            .build()
      );
   private final Setting<Boolean> advanced = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("advanced"))
                  .description("Respect block rotation (places blocks in weird places in singleplayer, multiplayer should work fine)."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> airPlace = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("air-place"))
                  .description("Allow the bot to place in the air."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> placeThroughWall = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("Place Through Wall"))
                  .description("Allow the bot to place through walls."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> swing = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("swing"))
                  .description("Swing hand when placing."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> returnHand = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("return-slot"))
                  .description("Return to old slot."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("rotate"))
                  .description("Rotate to the blocks being placed."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> clientSide = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("Client side Rotation"))
                     .description("Rotate to the blocks being placed on client side."))
                  .defaultValue(false))
               .visible(this.rotate::get))
            .build()
      );
   private final Setting<Boolean> dirtgrass = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("dirt-as-grass"))
                  .description("Use dirt instead of grass."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Printer.SortAlgorithm> firstAlgorithm = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("first-sorting-mode"))
                  .description("The blocks you want to place first."))
               .defaultValue(Printer.SortAlgorithm.None))
            .build()
      );
   private final Setting<Printer.SortingSecond> secondAlgorithm = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("second-sorting-mode"))
                     .description("Second pass of sorting eg. place first blocks higher and closest to you."))
                  .defaultValue(Printer.SortingSecond.None))
               .visible(() -> ((Printer.SortAlgorithm)this.firstAlgorithm.get()).applySecondSorting))
            .build()
      );
   private final Setting<Printer.FilterMode> listMode = this.sgWorkMode
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("list-mode"))
                  .description("Block list mode."))
               .defaultValue(Printer.FilterMode.NONE))
            .build()
      );
   private final Setting<List<Block>> filterBlocks = this.sgWorkMode
      .add(
         ((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)((meteordevelopment.meteorclient.settings.BlockListSetting.Builder)new meteordevelopment.meteorclient.settings.BlockListSetting.Builder()
                     .name("filter-blocks"))
                  .description("Blocks to whitelist or blacklist."))
               .visible(() -> this.listMode.get() != Printer.FilterMode.NONE))
            .build()
      );
   private final Setting<Boolean> renderBlocks = this.sgRendering
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("render-placed-blocks"))
                  .description("Renders block placements."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Integer> fadeTime = this.sgRendering
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("fade-time")).description("Time for the rendering to fade, in ticks.")).defaultValue(3))
               .min(1)
               .sliderMin(1)
               .max(1000)
               .sliderMax(20)
               .visible(this.renderBlocks::get))
            .build()
      );
   private final Setting<SettingColor> colour = this.sgRendering
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("colour"))
                  .description("The cubes colour."))
               .defaultValue(new SettingColor(95, 190, 255))
               .visible(this.renderBlocks::get))
            .build()
      );
   private final Setting<Boolean> useOffhand = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("use-offhand"))
                  .description("Automatically put block items in the offhand while printing."))
               .defaultValue(false))
            .build()
      );
   private int timer;
   private int usedSlot = -1;
   private final List<BlockPos> toSort = new ArrayList<>();
   private final List<Tuple<Integer, BlockPos>> placed_fade = new ArrayList<>();

   public Printer() {
      super(Addon.CATEGORY, "printer", "Automatically prints open schematics");
   }

   public void onActivate() {
      this.onDeactivate();
   }

   public void onDeactivate() {
      this.placed_fade.clear();
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.player != null && this.mc.level != null) {
         this.placed_fade.forEach(s -> s.setA((Integer)s.getA() - 1));
         this.placed_fade.removeIf(s -> (Integer)s.getA() <= 0);
         WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
         if (worldSchematic == null) {
            this.placed_fade.clear();
            this.toggle();
         } else {
            this.toSort.clear();
            if (this.timer >= (Integer)this.printing_delay.get()) {
               BlockIterator.register(
                  (Integer)this.printing_range.get() + 1,
                  (Integer)this.printing_range.get() + 1,
                  (pos, blockState) -> {
                     BlockState required = worldSchematic.getBlockState(pos);
                     if (this.mc.player.blockPosition().closerThan(pos, ((Integer)this.printing_range.get()).intValue())
                        && blockState.canBeReplaced()
                        && required.getFluidState().isEmpty()
                        && !required.isAir()
                        && blockState.getBlock() != required.getBlock()
                        && DataManager.getRenderLayerRange().isPositionWithinRange(pos)
                        && !this.mc.player.getBoundingBox().intersects(Vec3.atLowerCornerOf(pos), Vec3.atLowerCornerOf(pos).add(1.0, 1.0, 1.0))
                        && required.canSurvive(this.mc.level, pos)) {
                        boolean isBlockInLineOfSight = MyUtils.isBlockInLineOfSight(pos, required);
                        SlabType wantedSlabType = this.advanced.get() && required.hasProperty(BlockStateProperties.SLAB_TYPE)
                           ? (SlabType)required.getValue(BlockStateProperties.SLAB_TYPE)
                           : null;
                        Half wantedBlockHalf = this.advanced.get() && required.hasProperty(BlockStateProperties.HALF)
                           ? (Half)required.getValue(BlockStateProperties.HALF)
                           : null;
                        Direction wantedHorizontalOrientation = this.advanced.get() && required.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                           ? (Direction)required.getValue(BlockStateProperties.HORIZONTAL_FACING)
                           : null;
                        Axis wantedAxies = this.advanced.get() && required.hasProperty(BlockStateProperties.AXIS)
                           ? (Axis)required.getValue(BlockStateProperties.AXIS)
                           : null;
                        Direction wantedHopperOrientation = this.advanced.get() && required.hasProperty(BlockStateProperties.FACING_HOPPER)
                           ? (Direction)required.getValue(BlockStateProperties.FACING_HOPPER)
                           : null;
                        if ((Boolean)this.airPlace.get() && (Boolean)this.placeThroughWall.get()
                           || !(Boolean)this.airPlace.get()
                              && !(Boolean)this.placeThroughWall.get()
                              && isBlockInLineOfSight
                              && MyUtils.getVisiblePlaceSide(
                                    pos,
                                    required,
                                    wantedSlabType,
                                    wantedBlockHalf,
                                    wantedHorizontalOrientation != null ? wantedHorizontalOrientation : wantedHopperOrientation,
                                    wantedAxies,
                                    (Integer)this.printing_range.get(),
                                    this.advanced.get() ? this.dir(required) : null
                                 )
                                 != null
                           || (Boolean)this.airPlace.get() && !(Boolean)this.placeThroughWall.get() && isBlockInLineOfSight
                           || !(Boolean)this.airPlace.get() && (Boolean)this.placeThroughWall.get() && BlockUtils.getPlaceSide(pos) != null) {
                           Printer.FilterMode fm = (Printer.FilterMode)this.listMode.get();
                           if (fm == Printer.FilterMode.NONE
                              || fm == Printer.FilterMode.WHITELIST && ((List)this.filterBlocks.get()).contains(required.getBlock())
                              || fm == Printer.FilterMode.BLACKLIST && !((List)this.filterBlocks.get()).contains(required.getBlock())) {
                              this.toSort.add(new BlockPos(pos));
                           }
                        }
                     }
                  }
               );
               BlockIterator.after(() -> {
                  if (this.firstAlgorithm.get() != Printer.SortAlgorithm.None) {
                     if (((Printer.SortAlgorithm)this.firstAlgorithm.get()).applySecondSorting && this.secondAlgorithm.get() != Printer.SortingSecond.None) {
                        this.toSort.sort(((Printer.SortingSecond)this.secondAlgorithm.get()).algorithm);
                     }

                     this.toSort.sort(((Printer.SortAlgorithm)this.firstAlgorithm.get()).algorithm);
                  }

                  int placed = 0;

                  for (BlockPos pos : this.toSort) {
                     BlockState state = worldSchematic.getBlockState(pos);
                     Item item = state.getBlock().asItem();
                     if ((Boolean)this.dirtgrass.get() && item == Items.GRASS_BLOCK) {
                        item = Items.DIRT;
                     }

                     boolean placedBlock = false;
                     if ((Boolean)this.useOffhand.get()) {
                        placedBlock = this.switchItemOffhand(item, () -> this.place(state, pos));
                     } else {
                        placedBlock = this.switchItem(item, state, () -> this.place(state, pos));
                     }

                     if (placedBlock) {
                        this.timer = 0;
                        placed++;
                        if ((Boolean)this.renderBlocks.get()) {
                           this.placed_fade.add(new Tuple((Integer)this.fadeTime.get(), new BlockPos(pos)));
                        }

                        if (placed >= (Integer)this.bpt.get()) {
                           return;
                        }
                     }
                  }
               });
            } else {
               this.timer++;
            }
         }
      } else {
         this.placed_fade.clear();
      }
   }

   public boolean place(BlockState required, BlockPos pos) {
      if (this.mc.player == null || this.mc.level == null) {
         return false;
      } else if (!this.mc.level.getBlockState(pos).canBeReplaced()) {
         return false;
      } else {
         Direction wantedSide = this.advanced.get() ? this.dir(required) : null;
         SlabType wantedSlabType = this.advanced.get() && required.hasProperty(BlockStateProperties.SLAB_TYPE)
            ? (SlabType)required.getValue(BlockStateProperties.SLAB_TYPE)
            : null;
         Half wantedBlockHalf = this.advanced.get() && required.hasProperty(BlockStateProperties.HALF)
            ? (Half)required.getValue(BlockStateProperties.HALF)
            : null;
         Direction wantedHorizontalOrientation = this.advanced.get() && required.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
            ? (Direction)required.getValue(BlockStateProperties.HORIZONTAL_FACING)
            : null;
         Axis wantedAxies = this.advanced.get() && required.hasProperty(BlockStateProperties.AXIS) ? (Axis)required.getValue(BlockStateProperties.AXIS) : null;
         Direction wantedHopperOrientation = this.advanced.get() && required.hasProperty(BlockStateProperties.FACING_HOPPER)
            ? (Direction)required.getValue(BlockStateProperties.FACING_HOPPER)
            : null;
         Direction placeSide = this.placeThroughWall.get()
            ? MyUtils.getPlaceSide(
               pos,
               required,
               wantedSlabType,
               wantedBlockHalf,
               wantedHorizontalOrientation != null ? wantedHorizontalOrientation : wantedHopperOrientation,
               wantedAxies,
               wantedSide
            )
            : MyUtils.getVisiblePlaceSide(
               pos,
               required,
               wantedSlabType,
               wantedBlockHalf,
               wantedHorizontalOrientation != null ? wantedHorizontalOrientation : wantedHopperOrientation,
               wantedAxies,
               (Integer)this.printing_range.get(),
               wantedSide
            );
         return MyUtils.place(
            pos,
            placeSide,
            wantedSlabType,
            wantedBlockHalf,
            wantedHorizontalOrientation != null ? wantedHorizontalOrientation : wantedHopperOrientation,
            wantedAxies,
            (Boolean)this.airPlace.get(),
            (Boolean)this.swing.get(),
            (Boolean)this.rotate.get(),
            (Boolean)this.clientSide.get(),
            (Integer)this.printing_range.get(),
            this.useOffhand.get() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND
         );
      }
   }

   private boolean switchItem(Item item, BlockState state, Supplier<Boolean> action) {
      if (this.mc.player == null) {
         return false;
      } else {
         int selectedSlot = this.mc.player.getInventory().getSelectedSlot();
         boolean isCreative = this.mc.player.isCreative();
         FindItemResult result = InvUtils.find(new Item[]{item});
         if (this.mc.player.getMainHandItem().getItem() == item) {
            if (action.get()) {
               this.usedSlot = this.mc.player.getInventory().getSelectedSlot();
               return true;
            } else {
               return false;
            }
         } else if (this.usedSlot != -1 && this.mc.player.getInventory().getItem(this.usedSlot).getItem() == item) {
            InvUtils.swap(this.usedSlot, (Boolean)this.returnHand.get());
            if (action.get()) {
               return true;
            } else {
               InvUtils.swap(selectedSlot, (Boolean)this.returnHand.get());
               return false;
            }
         } else if (isCreative) {
            FindItemResult hotbarResult = InvUtils.find(stack -> stack.getItem() == item, 0, 8);
            if (hotbarResult.found()) {
               InvUtils.swap(hotbarResult.slot(), (Boolean)this.returnHand.get());
               this.usedSlot = this.mc.player.getInventory().getSelectedSlot();
               return action.get();
            } else {
               int slot = this.mc.player.getInventory().getSelectedSlot();
               this.mc.getConnection().send(new ServerboundSetCreativeModeSlotPacket((short)(36 + slot), new ItemStack(item)));
               return action.get();
            }
         } else if (result.found()) {
            if (result.isHotbar()) {
               InvUtils.swap(result.slot(), (Boolean)this.returnHand.get());
               if (action.get()) {
                  this.usedSlot = this.mc.player.getInventory().getSelectedSlot();
                  return true;
               } else {
                  InvUtils.swap(selectedSlot, (Boolean)this.returnHand.get());
                  return false;
               }
            } else if (result.isMain()) {
               FindItemResult empty = InvUtils.findEmpty();
               if (empty.found() && empty.isHotbar()) {
                  InvUtils.move().from(result.slot()).toHotbar(empty.slot());
                  InvUtils.swap(empty.slot(), (Boolean)this.returnHand.get());
                  if (action.get()) {
                     this.usedSlot = this.mc.player.getInventory().getSelectedSlot();
                     return true;
                  } else {
                     InvUtils.swap(selectedSlot, (Boolean)this.returnHand.get());
                     return false;
                  }
               } else if (this.usedSlot != -1) {
                  InvUtils.move().from(result.slot()).toHotbar(this.usedSlot);
                  InvUtils.swap(this.usedSlot, (Boolean)this.returnHand.get());
                  if (action.get()) {
                     return true;
                  } else {
                     InvUtils.swap(selectedSlot, (Boolean)this.returnHand.get());
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private boolean switchItemOffhand(Item item, Supplier<Boolean> action) {
      if (this.mc.player != null && (Boolean)this.useOffhand.get()) {
         if (this.mc.player.getOffhandItem().getItem() == item) {
            return action.get();
         } else {
            FindItemResult result = InvUtils.find(stack -> stack.getItem() == item, 0, 8);
            if (result.found()) {
               InvUtils.move().from(result.slot()).toOffhand();
               return action.get();
            } else if (this.mc.player.isCreative()) {
               this.mc
                  .getConnection()
                  .send(new ServerboundSetCreativeModeSlotPacket((short)(this.mc.player.getInventory().getSelectedSlot() + 36), new ItemStack(item)));
               return false;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private Direction dir(BlockState state) {
      if (state.hasProperty(BlockStateProperties.FACING)) {
         return (Direction)state.getValue(BlockStateProperties.FACING);
      } else if (state.hasProperty(BlockStateProperties.AXIS)) {
         return Direction.fromAxisAndDirection((Axis)state.getValue(BlockStateProperties.AXIS), AxisDirection.POSITIVE);
      } else {
         return state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)
            ? Direction.fromAxisAndDirection((Axis)state.getValue(BlockStateProperties.HORIZONTAL_AXIS), AxisDirection.POSITIVE)
            : Direction.UP;
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      this.placed_fade
         .forEach(
            s -> {
               Color a = new Color(
                  ((SettingColor)this.colour.get()).r,
                  ((SettingColor)this.colour.get()).g,
                  ((SettingColor)this.colour.get()).b,
                  (int)((float)((Integer)s.getA()).intValue() / ((Integer)this.fadeTime.get()).intValue() * ((SettingColor)this.colour.get()).a)
               );
               event.renderer.box((BlockPos)s.getB(), a, null, ShapeMode.Sides, 0);
            }
         );
   }

   public static enum FilterMode {
      NONE,
      WHITELIST,
      BLACKLIST;
   }

   public static enum SortAlgorithm {
      None(false, (a, b) -> 0),
      TopDown(true, Comparator.comparingInt(value -> value.getY() * -1)),
      DownTop(true, Comparator.comparingInt(Vec3i::getY)),
      Nearest(
         false,
         Comparator.comparingDouble(
            value -> MeteorClient.mc.player != null
               ? Utils.squaredDistance(
                  MeteorClient.mc.player.getX(),
                  MeteorClient.mc.player.getY(),
                  MeteorClient.mc.player.getZ(),
                  value.getX() + 0.5,
                  value.getY() + 0.5,
                  value.getZ() + 0.5
               )
               : 0.0
         )
      ),
      Furthest(
         false,
         Comparator.comparingDouble(
            value -> MeteorClient.mc.player != null
               ? Utils.squaredDistance(
                     MeteorClient.mc.player.getX(),
                     MeteorClient.mc.player.getY(),
                     MeteorClient.mc.player.getZ(),
                     value.getX() + 0.5,
                     value.getY() + 0.5,
                     value.getZ() + 0.5
                  )
                  * -1.0
               : 0.0
         )
      );

      final boolean applySecondSorting;
      final Comparator<BlockPos> algorithm;

      private SortAlgorithm(boolean applySecondSorting, Comparator<BlockPos> algorithm) {
         this.applySecondSorting = applySecondSorting;
         this.algorithm = algorithm;
      }
   }

   public static enum SortingSecond {
      None,
      Nearest,
      Furthest;

      final Comparator<BlockPos> algorithm;

      private SortingSecond(Comparator<BlockPos> algorithm) {
         this.algorithm = algorithm;
      }

      // $VF: Failed to inline enum fields
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      static {
         None = new Printer.SortingSecond(Printer.SortAlgorithm.None.algorithm);
         Nearest = new Printer.SortingSecond(Printer.SortAlgorithm.Nearest.algorithm);
         Furthest = new Printer.SortingSecond(Printer.SortAlgorithm.Furthest.algorithm);
      }
   }
}
