package zgoly.meteorist.modules.slotclick;

import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.gui.screens.SlotSelectionScreen;
import zgoly.meteorist.modules.slotclick.selections.BaseSlotSelection;
import zgoly.meteorist.modules.slotclick.selections.DefaultSlotSelection;
import zgoly.meteorist.modules.slotclick.selections.DelaySelection;
import zgoly.meteorist.modules.slotclick.selections.SingleSlotSelection;
import zgoly.meteorist.modules.slotclick.selections.SlotRangeSelection;
import zgoly.meteorist.modules.slotclick.selections.SwapSlotSelection;
import zgoly.meteorist.utils.config.MeteoristConfigManager;
import zgoly.meteorist.utils.misc.DebugLogger;

public class SlotClick extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> disableAfterIteration = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("disable-after-iteration")).description("Disables the module after one iteration."))
               .defaultValue(false))
            .build()
      );
   public static List<BaseSlotSelection> slotSelections = new ArrayList<>();
   private final SelectionFactory factory = new SelectionFactory();
   private int startTick = -1;
   private final DebugLogger debugLogger = new DebugLogger(this, this.settings);

   public SlotClick() {
      super(Meteorist.CATEGORY, "slot-click", "Module that automates clicking on slots.");
   }

   public static List<Integer> createList(int start, int end) {
      List<Integer> list = IntStream.rangeClosed(Math.min(start, end), Math.max(start, end)).boxed().toList();
      return start > end ? list.reversed() : list;
   }

   public CompoundTag toTag() {
      CompoundTag tag = super.toTag();
      ListTag list = new ListTag();

      for (BaseSlotSelection slotSelection : slotSelections) {
         CompoundTag mTag = new CompoundTag();
         mTag.putString("type", slotSelection.getTypeName());
         mTag.put("slotSelection", slotSelection.toTag());
         list.add(mTag);
      }

      tag.put("slotSelections", list);
      return tag;
   }

   public Module fromTag(CompoundTag tag) {
      super.fromTag(tag);
      slotSelections.clear();

      for (Tag tagII : tag.getListOrEmpty("slotSelections")) {
         CompoundTag tagI = (CompoundTag)tagII;
         String type = tagI.getStringOr("type", "");
         BaseSlotSelection slotSelection = this.factory.createSelection(type);
         if (slotSelection != null) {
            CompoundTag slotSelectionTag = (CompoundTag)tagI.get("slotSelection");
            if (slotSelectionTag != null) {
               slotSelection.fromTag(slotSelectionTag);
            }

            slotSelections.add(slotSelection);
         }
      }

      return this;
   }

   public void onActivate() {
      this.startTick = -1;
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      this.fillWidget(theme, list);
      return list;
   }

   public void fillWidget(GuiTheme theme, WVerticalList list) {
      list.clear();
      WTable table = (WTable)list.add(theme.table()).expandX().widget();

      for (BaseSlotSelection slotSelection : slotSelections) {
         table.add(theme.label(slotSelection.getTypeName())).expandX();
         WContainer infoContainer = (WContainer)table.add(theme.horizontalList()).expandX().widget();
         switch (slotSelection) {
            case SingleSlotSelection singleSlotSelection:
               WIntEdit slot = (WIntEdit)infoContainer.add(theme.intEdit((Integer)singleSlotSelection.slot.get(), 0, Integer.MAX_VALUE, true)).widget();
               slot.tooltip = singleSlotSelection.slot.description;
               slot.actionOnRelease = () -> singleSlotSelection.slot.set(slot.get());
               break;
            case SlotRangeSelection slotRangeSelection: {
               WIntEdit fromSlot = (WIntEdit)infoContainer.add(theme.intEdit((Integer)slotRangeSelection.fromSlot.get(), 0, Integer.MAX_VALUE, true)).widget();
               fromSlot.tooltip = slotRangeSelection.fromSlot.description;
               fromSlot.actionOnRelease = () -> slotRangeSelection.fromSlot.set(fromSlot.get());
               WIntEdit toSlot = (WIntEdit)infoContainer.add(theme.intEdit((Integer)slotRangeSelection.toSlot.get(), 0, Integer.MAX_VALUE, true)).widget();
               toSlot.tooltip = slotRangeSelection.toSlot.description;
               toSlot.actionOnRelease = () -> slotRangeSelection.toSlot.set(toSlot.get());
               break;
            }
            case SwapSlotSelection swapSlotSelection: {
               WIntEdit fromSlot = (WIntEdit)infoContainer.add(theme.intEdit((Integer)swapSlotSelection.fromSlot.get(), 0, Integer.MAX_VALUE, true)).widget();
               fromSlot.tooltip = swapSlotSelection.fromSlot.description;
               fromSlot.actionOnRelease = () -> swapSlotSelection.fromSlot.set(fromSlot.get());
               WIntEdit toSlot = (WIntEdit)infoContainer.add(theme.intEdit((Integer)swapSlotSelection.toSlot.get(), 0, Integer.MAX_VALUE, true)).widget();
               toSlot.tooltip = swapSlotSelection.toSlot.description;
               toSlot.actionOnRelease = () -> swapSlotSelection.toSlot.set(toSlot.get());
               break;
            }
            case DelaySelection delaySelection:
               WIntEdit delay = (WIntEdit)infoContainer.add(theme.intEdit((Integer)delaySelection.delay.get(), 0, Integer.MAX_VALUE, true)).widget();
               delay.tooltip = delaySelection.delay.description;
               delay.actionOnRelease = () -> delaySelection.delay.set(delay.get());
               break;
            default:
         }

         WButton edit = (WButton)table.add(theme.button(GuiRenderer.EDIT)).widget();
         edit.tooltip = "Edit the slot selection.";
         edit.action = () -> this.mc.setScreen(new SlotSelectionScreen(theme, slotSelection));
         if (slotSelections.size() > 1) {
            WContainer moveContainer = (WContainer)table.add(theme.horizontalList()).expandX().widget();
            int index = slotSelections.indexOf(slotSelection);
            if (index > 0) {
               WButton moveUp = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_UP)).expandX().widget();
               moveUp.tooltip = "Move slot selection up.";
               moveUp.action = () -> {
                  slotSelections.remove(index);
                  slotSelections.add(index - 1, slotSelection);
                  this.fillWidget(theme, list);
               };
            }

            if (index < slotSelections.size() - 1) {
               WButton moveDown = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_DOWN)).expandX().widget();
               moveDown.tooltip = "Move slot selection down.";
               moveDown.action = () -> {
                  slotSelections.remove(index);
                  slotSelections.add(index + 1, slotSelection);
                  this.fillWidget(theme, list);
               };
            }
         }

         WButton copy = (WButton)table.add(theme.button(GuiRenderer.COPY)).widget();
         copy.tooltip = "Copy slot selection.";
         copy.action = () -> {
            slotSelections.add(slotSelections.indexOf(slotSelection), slotSelection.copy());
            this.fillWidget(theme, list);
         };
         WMinus remove = (WMinus)table.add(theme.minus()).widget();
         remove.tooltip = "Remove slot selection.";
         remove.action = () -> {
            slotSelections.remove(slotSelection);
            this.fillWidget(theme, list);
         };
         table.row();
      }

      if (!slotSelections.isEmpty()) {
         list.add(theme.horizontalSeparator()).expandX();
      }

      WTable controls = (WTable)list.add(theme.table()).expandX().widget();
      WButton createSingleSlotSelection = (WButton)controls.add(theme.button("New Single")).widget();
      createSingleSlotSelection.action = () -> {
         slotSelections.add(new SingleSlotSelection());
         this.fillWidget(theme, list);
      };
      WButton createSlotRangeSelection = (WButton)controls.add(theme.button("New Range")).widget();
      createSlotRangeSelection.action = () -> {
         slotSelections.add(new SlotRangeSelection());
         this.fillWidget(theme, list);
      };
      WButton createSwapSlotSelection = (WButton)controls.add(theme.button("New Swap")).widget();
      createSwapSlotSelection.action = () -> {
         slotSelections.add(new SwapSlotSelection());
         this.fillWidget(theme, list);
      };
      WButton createDelaySelection = (WButton)controls.add(theme.button("New Delay")).widget();
      createDelaySelection.action = () -> {
         slotSelections.add(new DelaySelection());
         this.fillWidget(theme, list);
      };
      WButton removeAll = (WButton)controls.add(theme.button("Remove All Selections")).widget();
      removeAll.action = () -> {
         slotSelections.clear();
         this.fillWidget(theme, list);
      };
      MeteoristConfigManager.configManager(theme, list, this);
   }

   @EventHandler
   public void onTick(Pre event) {
      int currentTick = (int)this.mc.level.getGameTime();
      if (this.startTick == -1) {
         this.startTick = currentTick;
      }

      Map<Integer, List<BaseSlotSelection>> map = new HashMap<>();
      int tick = 0;

      for (BaseSlotSelection slotSelection : slotSelections) {
         if (slotSelection instanceof DelaySelection delaySelection) {
            tick += delaySelection.delay.get();
         } else if (!(slotSelection instanceof SingleSlotSelection) && !(slotSelection instanceof SwapSlotSelection)) {
            if (slotSelection instanceof SlotRangeSelection slotRangeSelection) {
               List<Integer> slots = createList((Integer)slotRangeSelection.fromSlot.get(), (Integer)slotRangeSelection.toSlot.get());

               for (int slot : slots) {
                  SlotRangeSelection copy = slotRangeSelection.copy();
                  copy.calculatedSlot = slot;
                  map.computeIfAbsent(tick, ArrayList::new).add(copy);
                  if (slots.indexOf(slot) < slots.size() - 1) {
                     tick += copy.delay.get();
                  }
               }
            }
         } else {
            map.computeIfAbsent(tick, ArrayList::new).add(slotSelection);
         }
      }

      label183:
      for (Entry<Integer, List<BaseSlotSelection>> entry : map.entrySet()) {
         if (this.startTick + entry.getKey() == currentTick) {
            Iterator var29 = entry.getValue().iterator();

            while (true) {
               AbstractContainerMenu screenHandler;
               while (true) {
                  while (true) {
                     Screen screen;
                     while (true) {
                        if (!var29.hasNext()) {
                           continue label183;
                        }

                        BaseSlotSelection baseSlotSelection = (BaseSlotSelection)var29.next();
                        if (baseSlotSelection instanceof DefaultSlotSelection defaultSlotSelection) {
                           this.debugLogger.info("Slot selection: (highlight)%s", defaultSlotSelection.getTypeName());
                           screen = this.mc.screen;
                           screenHandler = this.mc.player.containerMenu;
                           if (!(Boolean)defaultSlotSelection.checkContainerType.get()) {
                              break;
                           }

                           this.debugLogger.info("Checking container type...");

                           try {
                              boolean containerTypeMatched;
                              if (defaultSlotSelection.containerTypeMode.get() == DefaultSlotSelection.ContainerTypeMode.Whitelist) {
                                 containerTypeMatched = ((List)defaultSlotSelection.containerType.get()).contains(screenHandler.getType());
                              } else {
                                 containerTypeMatched = !((List)defaultSlotSelection.containerType.get()).contains(screenHandler.getType());
                              }

                              if (containerTypeMatched) {
                                 this.debugLogger.info("Container type (highlight)%s(default) matched!", screenHandler.getType());
                                 break;
                              }

                              this.debugLogger.warning("Container type (highlight)%s(default) not matched!", screenHandler.getType());
                           } catch (Exception var24) {
                              this.debugLogger.error(var24.getMessage());
                              if (!(Boolean)defaultSlotSelection.ignoreMenuTypeMismatch.get()) {
                                 continue;
                              }
                              break;
                           }
                        }
                     }

                     if (!(Boolean)defaultSlotSelection.checkScreenName.get()) {
                        break;
                     }

                     this.debugLogger.info("Checking screen name...");

                     try {
                        Pattern screenNamePattern = Pattern.compile((String)defaultSlotSelection.screenName.get());
                        String screenName = screen != null ? screen.getTitle().getString() : "null";
                        this.debugLogger.info("Screen name: (highlight)%s", screenName);
                        this.debugLogger.info("Regular expression: (highlight)%s", screenNamePattern.pattern());
                        boolean checkScreenName = screenNamePattern.matcher(screenName).find();
                        if (checkScreenName) {
                           this.debugLogger.info("Screen name matched!");
                           break;
                        }

                        this.debugLogger.warning("Screen name not matched!");
                     } catch (Exception var25) {
                        this.debugLogger.error(var25.getMessage());
                        break;
                     }
                  }

                  if (!(Boolean)defaultSlotSelection.checkSlotItemData.get()) {
                     break;
                  }

                  this.debugLogger.info("Checking slot item data...");
                  int slot = 0;

                  slot = switch (defaultSlotSelection) {
                     case SingleSlotSelection singleSlotSelection -> singleSlotSelection.slot.get();
                     case SwapSlotSelection swapSlotSelection -> swapSlotSelection.fromSlot.get();
                     case SlotRangeSelection slotRangeSelection -> slotRangeSelection.calculatedSlot;
                  };

                  try {
                     ItemStack itemStack = screenHandler.getSlot(slot).getItem();
                     if (itemStack.isEmpty()) {
                        this.debugLogger.warning("There is no item in the slot (highlight)%s(default)!", slot);
                        break;
                     }

                     DataResult<Tag> dataResult = ItemStack.CODEC
                        .encodeStart(this.mc.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), itemStack);
                     if (!dataResult.result().isPresent()) {
                        this.debugLogger.warning("Cannot find any data for item (highlight)%s(default) in slot (highlight)%s(default)!", itemStack, slot);
                        break;
                     }

                     Tag element = (Tag)dataResult.result().get();
                     this.debugLogger.info(Component.literal("Item data: ").withStyle(ChatFormatting.GRAY).append(NbtUtils.toPrettyComponent(element)));
                     boolean matchedAny = false;
                     boolean matchedAll = true;

                     for (Tuple<String, String> pair : (List)defaultSlotSelection.slotItemData.get()) {
                        try {
                           Pattern pattern = Pattern.compile((String)pair.getB());
                           Tag value = (Tag)NbtPath.of((String)pair.getA()).get(element).getFirst();
                           this.debugLogger
                              .info(
                                 Component.literal("Found element for path \"" + (String)pair.getA() + "\": ")
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(NbtUtils.toPrettyComponent(value))
                              );
                           if (pattern.matcher(value.toString()).find()) {
                              matchedAny = true;
                              this.debugLogger.info("Pattern (highlight)%s(default) matched!", pair.getB());
                           } else {
                              matchedAll = false;
                              this.debugLogger.warning("Pattern (highlight)%s(default) not matched!", pair.getB());
                           }
                        } catch (Exception var23) {
                           this.debugLogger.error(var23.getMessage());
                        }
                     }

                     boolean isAnyMatch = defaultSlotSelection.slotItemMatchMode.get() == DefaultSlotSelection.SlotItemMatchMode.Any;
                     if (isAnyMatch && matchedAny || !isAnyMatch && matchedAll) {
                        break;
                     }
                  } catch (Exception var26) {
                     this.debugLogger.error(var26.getMessage());
                     break;
                  }
               }

               if (this.mc.gameMode != null) {
                  try {
                     switch (defaultSlotSelection) {
                        case SingleSlotSelection singleSlotSelection:
                           this.mc
                              .gameMode
                              .handleContainerInput(
                                 screenHandler.containerId,
                                 (Integer)singleSlotSelection.slot.get(),
                                 (Integer)singleSlotSelection.button.get(),
                                 (ContainerInput)singleSlotSelection.action.get(),
                                 this.mc.player
                              );
                           break;
                        case SwapSlotSelection swapSlotSelection:
                           this.mc
                              .gameMode
                              .handleContainerInput(
                                 screenHandler.containerId, (Integer)swapSlotSelection.fromSlot.get(), 0, ContainerInput.PICKUP, this.mc.player
                              );
                           this.mc
                              .gameMode
                              .handleContainerInput(
                                 screenHandler.containerId, (Integer)swapSlotSelection.toSlot.get(), 0, ContainerInput.PICKUP, this.mc.player
                              );
                           break;
                        case SlotRangeSelection slotRangeSelection:
                           this.mc
                              .gameMode
                              .handleContainerInput(
                                 screenHandler.containerId,
                                 slotRangeSelection.calculatedSlot,
                                 (Integer)slotRangeSelection.button.get(),
                                 (ContainerInput)slotRangeSelection.action.get(),
                                 this.mc.player
                              );
                           break;
                        default:
                     }
                  } catch (Exception var22) {
                     this.debugLogger.error(var22.getMessage());
                  }
               }
            }
         }
      }

      if (this.startTick + tick <= currentTick) {
         if ((Boolean)this.disableAfterIteration.get()) {
            this.toggle();
         } else {
            this.startTick = -1;
         }
      }
   }
}
