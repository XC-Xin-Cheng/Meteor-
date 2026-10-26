package zgoly.meteorist.modules.placer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.gui.screens.PlacerScreen;
import zgoly.meteorist.gui.widgets.WVisibilityCheckbox;
import zgoly.meteorist.utils.MeteoristUtils;
import zgoly.meteorist.utils.config.MeteoristConfigManager;

public class Placer extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgTiming = this.settings.createGroup("Timing");
   private final SettingGroup sgPause = this.settings.createGroup("Pause");
   private final Setting<Boolean> checkConditions = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("check-conditions"))
                  .description(
                     "Check conditions for placing blocks, such as whether block can be placed and whether there is entity that blocking block from being placed."
                  ))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> rotateHead = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("rotate-head")).description("Rotate head when placing a block.")).defaultValue(true)).build());
   private final Setting<Boolean> limitRange = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("limit-range")).description("Limit block placement range.")).defaultValue(true)).build());
   private final Setting<Double> maxRange = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("max-range"))
                  .description("Max range to place blocks."))
               .defaultValue(4.0)
               .visible(this.limitRange::get))
            .sliderRange(1.0, 6.0)
            .build()
      );
   private final Setting<Boolean> renderEachBlock = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("render-each-block")).description("Render each block in placer area.")).defaultValue(true)).build());
   private final Setting<Boolean> useDelay = this.sgTiming
      .add(((Builder)((Builder)((Builder)new Builder().name("use-delay")).description("Use delay between placing blocks.")).defaultValue(true)).build());
   private final Setting<Integer> maxBlocksPerTick = this.sgTiming
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("max-blocks-per-tick"))
                     .description("Maximum blocks to place per tick."))
                  .defaultValue(1))
               .visible(this.useDelay::get))
            .min(1)
            .sliderRange(1, 10)
            .build()
      );
   private final Setting<Integer> delay = this.sgTiming
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("delay"))
                     .description("Delay after placing block(s) in ticks (20 ticks = 1 sec)."))
                  .defaultValue(1))
               .visible(this.useDelay::get))
            .range(1, 120)
            .sliderRange(1, 40)
            .build()
      );
   private final Setting<Boolean> pauseOnAutoEat = this.sgPause
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("pause-on-auto-eat")).description("Pause when Auto Eat is active and eats food.")).defaultValue(true))
            .build()
      );
   private final Setting<Boolean> pauseOnAutoGap = this.sgPause
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("pause-on-auto-gap")).description("Pause when Auto Gap is active and eats gaps.")).defaultValue(true))
            .build()
      );
   private final Setting<Boolean> pauseOnKillAura = this.sgPause
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("pause-on-kill-aura")).description("Pause when Kill Aura is active and attacks target entities."))
               .defaultValue(true))
            .build()
      );
   List<BasePlacer> placers = new ArrayList<>();
   private int timer;
   private boolean work;

   public Placer() {
      super(Meteorist.CATEGORY, "placer", "Places blocks in range.");
   }

   public CompoundTag toTag() {
      CompoundTag tag = super.toTag();
      ListTag list = new ListTag();

      for (BasePlacer placer : this.placers) {
         CompoundTag mTag = new CompoundTag();
         mTag.put("placer", placer.toTag());
         list.add(mTag);
      }

      tag.put("placers", list);
      return tag;
   }

   public Module fromTag(CompoundTag tag) {
      super.fromTag(tag);
      this.placers.clear();

      for (Tag tagII : tag.getListOrEmpty("placers")) {
         CompoundTag tagI = (CompoundTag)tagII;
         BasePlacer placer = new BasePlacer();
         CompoundTag placerTag = (CompoundTag)tagI.get("placer");
         if (placerTag != null) {
            placer.fromTag(placerTag);
         }

         placer.settings.registerColorSettings(null);
         this.placers.add(placer);
      }

      return this;
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      this.fillWidget(theme, list);
      return list;
   }

   public void fillWidget(GuiTheme theme, WVerticalList list) {
      list.clear();
      WTable table = (WTable)list.add(theme.table()).expandX().widget();

      for (BasePlacer placer : this.placers) {
         WTextBox name = (WTextBox)table.add(theme.textBox((String)placer.name.get(), "Name")).expandX().widget();
         name.tooltip = placer.name.description;
         name.actionOnUnfocused = () -> placer.name.set(name.get());
         WVisibilityCheckbox visible = (WVisibilityCheckbox)table.add(new WVisibilityCheckbox((Boolean)placer.visible.get())).widget();
         visible.tooltip = placer.visible.description;
         visible.action = () -> placer.visible.set(visible.checked);
         WCheckbox active = (WCheckbox)table.add(theme.checkbox((Boolean)placer.active.get())).widget();
         active.tooltip = placer.active.description;
         active.action = () -> placer.active.set(active.checked);
         WButton edit = (WButton)table.add(theme.button(GuiRenderer.EDIT)).widget();
         edit.tooltip = "Edit the placer.";
         edit.action = () -> this.mc.setScreen(new PlacerScreen(theme, placer));
         if (this.placers.size() > 1) {
            WContainer moveContainer = (WContainer)table.add(theme.horizontalList()).expandX().widget();
            int index = this.placers.indexOf(placer);
            if (index > 0) {
               WButton moveUp = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_UP)).expandX().widget();
               moveUp.tooltip = "Move placer up.";
               moveUp.action = () -> {
                  this.placers.remove(index);
                  this.placers.add(index - 1, placer);
                  this.fillWidget(theme, list);
               };
            }

            if (index < this.placers.size() - 1) {
               WButton moveDown = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_DOWN)).expandX().widget();
               moveDown.tooltip = "Move placer down.";
               moveDown.action = () -> {
                  this.placers.remove(index);
                  this.placers.add(index + 1, placer);
                  this.fillWidget(theme, list);
               };
            }
         }

         WButton copy = (WButton)table.add(theme.button(GuiRenderer.COPY)).widget();
         copy.tooltip = "Duplicate placer.";
         copy.action = () -> {
            BasePlacer newPlacer = placer.copy();
            newPlacer.settings.registerColorSettings(null);
            this.placers.add(this.placers.indexOf(placer), newPlacer);
            this.fillWidget(theme, list);
         };
         WMinus remove = (WMinus)table.add(theme.minus()).widget();
         remove.tooltip = "Remove placer.";
         remove.action = () -> {
            placer.settings.unregisterColorSettings();
            this.placers.remove(placer);
            this.fillWidget(theme, list);
         };
         table.row();
      }

      if (!this.placers.isEmpty()) {
         list.add(theme.horizontalSeparator()).expandX();
      }

      WTable controls = (WTable)list.add(theme.table()).expandX().widget();
      WButton create = (WButton)controls.add(theme.button("New Placer")).expandX().widget();
      create.action = () -> {
         BasePlacer placer = new BasePlacer();
         placer.settings.registerColorSettings(null);
         placer.name.set("Placer #" + (this.placers.size() + 1));
         this.placers.add(placer);
         this.fillWidget(theme, list);
      };
      WButton removeAll = (WButton)controls.add(theme.button("Remove All Placers")).expandX().widget();
      removeAll.action = () -> {
         this.placers.forEach(placer -> placer.settings.unregisterColorSettings());
         this.placers.clear();
         this.fillWidget(theme, list);
      };
      MeteoristConfigManager.configManager(theme, list, this);
   }

   private Tuple<BlockPos, BlockPos> getPlacePos(BasePlacer placer) {
      BlockPos finalPos1 = this.rotateBlockPos((BlockPos)placer.cornerPos1.get(), (Boolean)placer.rotateY1.get());
      if (!(Boolean)placer.rotateX1.get()) {
         finalPos1 = new BlockPos(((BlockPos)placer.cornerPos1.get()).getX(), finalPos1.getY(), finalPos1.getZ());
      }

      if (!(Boolean)placer.rotateZ1.get()) {
         finalPos1 = new BlockPos(finalPos1.getX(), finalPos1.getY(), ((BlockPos)placer.cornerPos1.get()).getZ());
      }

      finalPos1 = finalPos1.offset(this.mc.player.blockPosition());
      if ((Boolean)placer.anchorX1.get()) {
         finalPos1 = new BlockPos(((BlockPos)placer.cornerPos1.get()).getX(), finalPos1.getY(), finalPos1.getZ());
      }

      if ((Boolean)placer.anchorY1.get()) {
         finalPos1 = new BlockPos(finalPos1.getX(), ((BlockPos)placer.cornerPos1.get()).getY(), finalPos1.getZ());
      }

      if ((Boolean)placer.anchorZ1.get()) {
         finalPos1 = new BlockPos(finalPos1.getX(), finalPos1.getY(), ((BlockPos)placer.cornerPos1.get()).getZ());
      }

      finalPos1 = finalPos1.offset((Vec3i)placer.cornerAnchorPos1.get());
      BlockPos finalPos2 = this.rotateBlockPos((BlockPos)placer.cornerPos2.get(), (Boolean)placer.rotateY2.get());
      if (!(Boolean)placer.rotateX2.get()) {
         finalPos2 = new BlockPos(((BlockPos)placer.cornerPos2.get()).getX(), finalPos2.getY(), finalPos2.getZ());
      }

      if (!(Boolean)placer.rotateZ2.get()) {
         finalPos2 = new BlockPos(finalPos2.getX(), finalPos2.getY(), ((BlockPos)placer.cornerPos2.get()).getZ());
      }

      finalPos2 = finalPos2.offset(this.mc.player.blockPosition());
      if ((Boolean)placer.anchorX2.get()) {
         finalPos2 = new BlockPos(((BlockPos)placer.cornerPos2.get()).getX(), finalPos2.getY(), finalPos2.getZ());
      }

      if ((Boolean)placer.anchorY2.get()) {
         finalPos2 = new BlockPos(finalPos2.getX(), ((BlockPos)placer.cornerPos2.get()).getY(), finalPos2.getZ());
      }

      if ((Boolean)placer.anchorZ2.get()) {
         finalPos2 = new BlockPos(finalPos2.getX(), finalPos2.getY(), ((BlockPos)placer.cornerPos2.get()).getZ());
      }

      finalPos2 = finalPos2.offset((Vec3i)placer.cornerAnchorPos2.get());
      return new Tuple(finalPos1, finalPos2);
   }

   private Stream<BlockPos> getPlaceStream(Tuple<BlockPos, BlockPos> finalPos) {
      return BlockPos.betweenClosedStream((BlockPos)finalPos.getA(), (BlockPos)finalPos.getB());
   }

   public void onActivate() {
      this.timer = 0;
      this.work = true;
   }

   @EventHandler
   private void onTick(Post event) {
      if (!(Boolean)this.pauseOnAutoEat.get() || !((AutoEat)Modules.get().get(AutoEat.class)).eating) {
         if (!(Boolean)this.pauseOnAutoGap.get() || !((AutoGap)Modules.get().get(AutoGap.class)).isEating()) {
            if (!(Boolean)this.pauseOnKillAura.get() || !((KillAura)Modules.get().get(KillAura.class)).attacking) {
               if (this.work) {
                  int loopCount = 0;

                  label86:
                  for (BasePlacer placer : this.placers) {
                     if ((Boolean)placer.active.get()) {
                        List<BlockPos> blockPosList = new ArrayList<>();
                        Tuple<BlockPos, BlockPos> finalPos = this.getPlacePos(placer);
                        BlockPos.betweenClosedStream((BlockPos)finalPos.getA(), (BlockPos)finalPos.getB())
                           .filter(
                              blockPosx -> !(Boolean)this.limitRange.get()
                                 || !(blockPosx.getCenter().distanceTo(this.mc.player.getEyePosition()) > (Double)this.maxRange.get())
                           )
                           .forEach(blockPosx -> blockPosList.add(new BlockPos(blockPosx)));
                        blockPosList.sort(Comparator.comparingDouble(blockPosx -> blockPosx.getCenter().distanceTo(this.mc.player.getEyePosition())));

                        for (BlockPos blockPos : blockPosList) {
                           if ((Boolean)this.useDelay.get() && loopCount >= (Integer)this.maxBlocksPerTick.get()) {
                              break label86;
                           }

                           if (!(Boolean)this.checkConditions.get() || BlockUtils.canPlace(blockPos) && !MeteoristUtils.isCollidesEntity(blockPos)) {
                              Random random = new Random();
                              List<Block> hotbarBlocks = new ArrayList<>();

                              for (Block block : (List)placer.blocks.get()) {
                                 FindItemResult itemResult = InvUtils.findInHotbar(new Item[]{block.asItem()});
                                 if (itemResult.found()) {
                                    hotbarBlocks.add(block);
                                 }
                              }

                              if (!hotbarBlocks.isEmpty()) {
                                 Block blockToPlace = hotbarBlocks.get(random.nextInt(hotbarBlocks.size()));
                                 FindItemResult itemResult = InvUtils.findInHotbar(new Item[]{blockToPlace.asItem()});
                                 BlockUtils.place(blockPos, itemResult, (Boolean)this.rotateHead.get(), 0);
                              }

                              if ((Boolean)this.useDelay.get()) {
                                 loopCount++;
                              }
                           }
                        }
                     }
                  }

                  if ((Boolean)this.useDelay.get()) {
                     this.work = false;
                  }
               }

               if ((Boolean)this.useDelay.get()) {
                  if (this.timer + 1 >= (Integer)this.delay.get()) {
                     this.timer = 0;
                     this.work = true;
                  } else {
                     this.timer++;
                  }
               }
            }
         }
      }
   }

   public BlockPos rotateBlockPos(BlockPos pos, boolean rotateY) {
      if (rotateY) {
         float pitch = this.mc.player.getXRot();
         if (pitch > 45.0F) {
            pos = new BlockPos(pos.getY(), -pos.getX(), pos.getZ());
         } else if (pitch < -45.0F) {
            pos = new BlockPos(-pos.getY(), pos.getX(), pos.getZ());
         }
      }

      Direction direction = Direction.fromYRot(this.mc.player.getYRot());

      return switch (direction) {
         case NORTH -> new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
         case SOUTH -> new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
         case WEST -> new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
         default -> pos;
      };
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      for (BasePlacer placer : this.placers) {
         if ((Boolean)placer.visible.get()) {
            if ((Boolean)this.renderEachBlock.get()) {
               this.getPlaceStream(this.getPlacePos(placer))
                  .forEach(blockPos -> event.renderer.box(blockPos, (Color)placer.sideColor.get(), (Color)placer.lineColor.get(), ShapeMode.Both, 0));
            } else {
               Tuple<BlockPos, BlockPos> finalPos = this.getPlacePos(placer);
               event.renderer
                  .box(
                     AABB.encapsulatingFullBlocks((BlockPos)finalPos.getA(), (BlockPos)finalPos.getB()),
                     (Color)placer.sideColor.get(),
                     (Color)placer.lineColor.get(),
                     ShapeMode.Both,
                     0
                  );
            }
         }
      }
   }
}
