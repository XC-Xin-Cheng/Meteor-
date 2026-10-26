package zgoly.meteorist.modules.autotrade;

import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
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
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.gui.screens.OfferScreen;
import zgoly.meteorist.modules.autotrade.offers.BaseOffer;
import zgoly.meteorist.modules.autotrade.offers.IdOffer;
import zgoly.meteorist.modules.autotrade.offers.ItemsOffer;
import zgoly.meteorist.utils.config.MeteoristConfigManager;
import zgoly.meteorist.utils.misc.DebugLogger;

public class AutoTrade extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> oneOfferPerTick = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("one-offer-per-tick")).description("One offer per tick.")).defaultValue(true)).build());
   private final Setting<Boolean> closeWhenDone = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("close-when-done")).description("Closes the trade screen when done.")).defaultValue(false)).build());
   private final OfferFactory factory = new OfferFactory();
   private final List<BaseOffer> offers = new ArrayList<>();
   private final DebugLogger debugLogger;
   private ClientboundMerchantOffersPacket lastTrade = null;

   public AutoTrade() {
      super(Meteorist.CATEGORY, "auto-trade", "Automatically trades items with villagers (idea by Hiradpi).");
      this.debugLogger = new DebugLogger(this, this.settings);
   }

   public CompoundTag toTag() {
      CompoundTag tag = super.toTag();
      ListTag list = new ListTag();

      for (BaseOffer offer : this.offers) {
         CompoundTag mTag = new CompoundTag();
         mTag.putString("type", offer.getTypeName());
         mTag.put("offer", offer.toTag());
         list.add(mTag);
      }

      tag.put("offers", list);
      return tag;
   }

   public Module fromTag(CompoundTag tag) {
      super.fromTag(tag);
      this.offers.clear();

      for (Tag tagII : tag.getListOrEmpty("offers")) {
         CompoundTag tagI = (CompoundTag)tagII;
         String type = tagI.getStringOr("type", "");
         BaseOffer offer = this.factory.createOffer(type);
         if (offer != null) {
            CompoundTag offerTag = (CompoundTag)tagI.get("offer");
            if (offerTag != null) {
               offer.fromTag(offerTag);
            }

            this.offers.add(offer);
         }
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

      for (BaseOffer offer : this.offers) {
         table.add(theme.label(offer.getTypeName())).expandX();
         WHorizontalList container = (WHorizontalList)table.add(theme.horizontalList()).expandX().widget();
         if (offer instanceof ItemsOffer itemsOffer) {
            int pad = 16;
            if ((Boolean)itemsOffer.checkFirstInputItem.get() && itemsOffer.firstInputFilterMode.get() == ItemsOffer.FilterMode.Item) {
               if ((Boolean)itemsOffer.checkFirstInputItemCount.get()) {
                  int min = (Integer)itemsOffer.minFirstInputItemCount.get();
                  int max = (Integer)itemsOffer.maxFirstInputItemCount.get();
                  String range = min == max ? "x" + min : "x" + min + " - x" + max;
                  container.add(theme.itemWithLabel(((Item)itemsOffer.firstInputItem.get()).getDefaultInstance(), range)).padRight(pad);
               } else {
                  container.add(theme.item(((Item)itemsOffer.firstInputItem.get()).getDefaultInstance())).padRight(pad);
               }
            }

            if ((Boolean)itemsOffer.checkFirstInputItem.get() && (Boolean)itemsOffer.checkSecondInputItem.get()) {
               container.add(theme.label(" + ")).padRight(pad);
            }

            if ((Boolean)itemsOffer.checkSecondInputItem.get() && itemsOffer.secondInputFilterMode.get() == ItemsOffer.FilterMode.Item) {
               if ((Boolean)itemsOffer.checkSecondInputItemCount.get()) {
                  int min = (Integer)itemsOffer.minSecondInputItemCount.get();
                  int max = (Integer)itemsOffer.maxSecondInputItemCount.get();
                  String range = min == max ? "x" + min : "x" + min + " - x" + max;
                  container.add(theme.itemWithLabel(((Item)itemsOffer.secondInputItem.get()).getDefaultInstance(), range)).padRight(pad);
               } else {
                  container.add(theme.item(((Item)itemsOffer.secondInputItem.get()).getDefaultInstance())).padRight(pad);
               }
            }

            container.add(theme.label(">")).padRight(pad);
            if ((Boolean)itemsOffer.checkOutputItem.get() && itemsOffer.outputFilterMode.get() == ItemsOffer.FilterMode.Item) {
               if ((Boolean)itemsOffer.checkOutputItemCount.get()) {
                  int min = (Integer)itemsOffer.minOutputItemCount.get();
                  int max = (Integer)itemsOffer.maxOutputItemCount.get();
                  String range = min == max ? "x" + min : "x" + min + " - x" + max;
                  container.add(theme.itemWithLabel(((Item)itemsOffer.outputItem.get()).getDefaultInstance(), range));
               } else {
                  container.add(theme.item(((Item)itemsOffer.outputItem.get()).getDefaultInstance()));
               }
            }
         } else if (offer instanceof IdOffer idOffer) {
            WIntEdit edit = (WIntEdit)container.add(theme.intEdit((Integer)idOffer.offerId.get(), 0, Integer.MAX_VALUE, true)).expandX().widget();
            edit.tooltip = idOffer.offerId.description;
            edit.actionOnRelease = () -> idOffer.offerId.set(edit.get());
         }

         WButton edit = (WButton)table.add(theme.button(GuiRenderer.EDIT)).widget();
         edit.tooltip = "Edit the offer.";
         edit.action = () -> this.mc.setScreen(new OfferScreen(theme, offer));
         if (this.offers.size() > 1) {
            WContainer moveContainer = (WContainer)table.add(theme.horizontalList()).expandX().widget();
            int index = this.offers.indexOf(offer);
            if (index > 0) {
               WButton moveUp = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_UP)).expandX().widget();
               moveUp.tooltip = "Move offer up.";
               moveUp.action = () -> {
                  this.offers.remove(index);
                  this.offers.add(index - 1, offer);
                  this.fillWidget(theme, list);
               };
            }

            if (index < this.offers.size() - 1) {
               WButton moveDown = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_DOWN)).expandX().widget();
               moveDown.tooltip = "Move offer down.";
               moveDown.action = () -> {
                  this.offers.remove(index);
                  this.offers.add(index + 1, offer);
                  this.fillWidget(theme, list);
               };
            }
         }

         WButton copy = (WButton)table.add(theme.button(GuiRenderer.COPY)).widget();
         copy.tooltip = "Copy offer.";
         copy.action = () -> {
            this.offers.add(this.offers.indexOf(offer), offer.copy());
            this.fillWidget(theme, list);
         };
         WMinus remove = (WMinus)table.add(theme.minus()).widget();
         remove.tooltip = "Remove offer.";
         remove.action = () -> {
            this.offers.remove(offer);
            this.fillWidget(theme, list);
         };
         table.row();
      }

      if (!this.offers.isEmpty()) {
         list.add(theme.horizontalSeparator()).expandX();
      }

      WTable controls = (WTable)list.add(theme.table()).expandX().widget();
      WButton createItemsOffer = (WButton)controls.add(theme.button("New Items Offer")).expandX().widget();
      createItemsOffer.action = () -> {
         this.offers.add(new ItemsOffer());
         this.fillWidget(theme, list);
      };
      WButton createIdOffer = (WButton)controls.add(theme.button("New ID Offer")).expandX().widget();
      createIdOffer.action = () -> {
         this.offers.add(new IdOffer());
         this.fillWidget(theme, list);
      };
      WButton removeAll = (WButton)controls.add(theme.button("Remove All Offers")).widget();
      removeAll.action = () -> {
         this.offers.clear();
         this.fillWidget(theme, list);
      };
      MeteoristConfigManager.configManager(theme, list, this);
   }

   private boolean matchesNbtFilters(ItemStack stack, List<Tuple<String, String>> filters, boolean matchAll) {
      if (filters.isEmpty()) {
         return true;
      } else if (stack.isEmpty()) {
         return false;
      } else {
         DataResult<Tag> result = ItemStack.CODEC.encodeStart(this.mc.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack);
         if (result.result().isEmpty()) {
            return false;
         } else {
            Tag nbt = (Tag)result.result().get();
            boolean anyMatch = false;
            boolean allMatch = true;

            for (Tuple<String, String> pair : filters) {
               try {
                  String path = (String)pair.getA();
                  String regex = (String)pair.getB();
                  Pattern pattern = Pattern.compile(regex);
                  List<Tag> found = NbtPath.of(path).get(nbt);
                  boolean matched = false;
                  this.debugLogger.info("Evaluating NBT filter: path=(highlight)%s(default), regex=(highlight)%s", path, regex);
                  this.debugLogger.info("Found (highlight)%d(default) elements", found.size());
                  Iterator var15 = found.iterator();

                  while (true) {
                     if (var15.hasNext()) {
                        Tag tag = (Tag)var15.next();
                        String tagStr = tag.toString();
                        this.debugLogger.info("Checking element: (highlight)%s", tagStr);
                        if (!pattern.matcher(tagStr).find()) {
                           this.debugLogger.info("Match failed");
                           continue;
                        }

                        this.debugLogger.info("Match succeeded");
                        matched = true;
                     }

                     if (matched) {
                        anyMatch = true;
                     } else {
                        allMatch = false;
                     }
                     break;
                  }
               } catch (Exception var18) {
                  this.debugLogger.error("Exception during NBT filter evaluation: (highlight)%s", var18.getMessage());
                  allMatch = false;
               }
            }

            boolean resultBool = matchAll ? allMatch : anyMatch;
            this.debugLogger
               .info(
                  "NBT filter result: match mode=(highlight)%s(default), anyMatch=(highlight)%s(default), allMatch=(highlight)%s(default) -> returning (highlight)%s",
                  matchAll ? "All" : "Any",
                  anyMatch,
                  allMatch,
                  resultBool
               );
            return resultBool;
         }
      }
   }

   @EventHandler
   private void onTick(Post event) {
      boolean successfulOffer = false;
      if (this.lastTrade != null && this.mc.screen instanceof MerchantScreen) {
         MerchantOffers tradeOffers = this.lastTrade.getOffers();
         boolean offerMatched = false;

         for (MerchantOffer tradeOffer : tradeOffers) {
            int tradeIndex = tradeOffers.indexOf(tradeOffer);

            for (BaseOffer offer : this.offers) {
               if ((Boolean)offer.enabled.get()) {
                  if (offer instanceof ItemsOffer itemsOffer) {
                     boolean firstInputItemMatched = true;
                     boolean secondInputItemMatched = true;
                     boolean outputItemMatched = true;
                     if ((Boolean)itemsOffer.checkFirstInputItem.get()) {
                        if (itemsOffer.firstInputFilterMode.get() == ItemsOffer.FilterMode.Item) {
                           Item item = (Item)itemsOffer.firstInputItem.get();
                           Item tradeItem = (Item)tradeOffer.getItemCostA().item().value();
                           if (item == tradeItem) {
                              if ((Boolean)itemsOffer.checkFirstInputItemCount.get()) {
                                 int count = itemsOffer.useFinalCount.get() ? tradeOffer.getCostA().getCount() : tradeOffer.getBaseCostA().getCount();
                                 int min = (Integer)itemsOffer.minFirstInputItemCount.get();
                                 int max = (Integer)itemsOffer.maxFirstInputItemCount.get();
                                 firstInputItemMatched = count >= min && count <= max;
                                 if (!firstInputItemMatched) {
                                    this.debugLogger.info("First input count mismatch: got (highlight)%d(default), expected [%d, %d]", count, min, max);
                                 }
                              }
                           } else {
                              firstInputItemMatched = false;
                              this.debugLogger.info("First input item mismatch: expected (highlight)%s(default), got (highlight)%s", item, tradeItem);
                           }
                        } else {
                           firstInputItemMatched = this.matchesNbtFilters(
                              tradeOffer.getCostA(),
                              (List<Tuple<String, String>>)itemsOffer.firstInputNbtFilter.get(),
                              itemsOffer.firstInputMatchMode.get() == ItemsOffer.MatchMode.All
                           );
                           if (!firstInputItemMatched) {
                              this.debugLogger.info("First input NBT filter failed");
                           }
                        }
                     }

                     if ((Boolean)itemsOffer.checkSecondInputItem.get()) {
                        if (tradeOffer.getItemCostB().isPresent()) {
                           if (itemsOffer.secondInputFilterMode.get() == ItemsOffer.FilterMode.Item) {
                              Item item = (Item)itemsOffer.secondInputItem.get();
                              Item tradeItem = (Item)((ItemCost)tradeOffer.getItemCostB().get()).item().value();
                              if (item == tradeItem) {
                                 if ((Boolean)itemsOffer.checkSecondInputItemCount.get()) {
                                    int count = tradeOffer.getCostB().getCount();
                                    int min = (Integer)itemsOffer.minSecondInputItemCount.get();
                                    int max = (Integer)itemsOffer.maxSecondInputItemCount.get();
                                    secondInputItemMatched = count >= min && count <= max;
                                    if (!secondInputItemMatched) {
                                       this.debugLogger.info("Second input count mismatch: got (highlight)%d(default), expected [%d, %d]", count, min, max);
                                    }
                                 }
                              } else {
                                 secondInputItemMatched = false;
                                 this.debugLogger.info("Second input item mismatch: expected (highlight)%s(default), got (highlight)%s", item, tradeItem);
                              }
                           } else {
                              secondInputItemMatched = this.matchesNbtFilters(
                                 tradeOffer.getCostB(),
                                 (List<Tuple<String, String>>)itemsOffer.secondInputNbtFilter.get(),
                                 itemsOffer.secondInputMatchMode.get() == ItemsOffer.MatchMode.All
                              );
                              if (!secondInputItemMatched) {
                                 this.debugLogger.info("Second input NBT filter failed");
                              }
                           }
                        } else {
                           secondInputItemMatched = false;
                           this.debugLogger.info("Second input expected but trade has no second cost");
                        }
                     }

                     if ((Boolean)itemsOffer.checkOutputItem.get()) {
                        if (itemsOffer.outputFilterMode.get() == ItemsOffer.FilterMode.Item) {
                           Item item = (Item)itemsOffer.outputItem.get();
                           Item tradeItem = tradeOffer.getResult().getItem();
                           if (item == tradeItem) {
                              if ((Boolean)itemsOffer.checkOutputItemCount.get()) {
                                 int count = tradeOffer.getResult().getCount();
                                 int min = (Integer)itemsOffer.minOutputItemCount.get();
                                 int max = (Integer)itemsOffer.maxOutputItemCount.get();
                                 outputItemMatched = count >= min && count <= max;
                                 if (!outputItemMatched) {
                                    this.debugLogger.info("Output count mismatch: got (highlight)%d(default), expected [%d, %d]", count, min, max);
                                 }
                              }
                           } else {
                              outputItemMatched = false;
                              this.debugLogger.info("Output item mismatch: expected (highlight)%s(default), got (highlight)%s", item, tradeItem);
                           }
                        } else {
                           outputItemMatched = this.matchesNbtFilters(
                              tradeOffer.getResult(),
                              (List<Tuple<String, String>>)itemsOffer.outputNbtFilter.get(),
                              itemsOffer.outputMatchMode.get() == ItemsOffer.MatchMode.All
                           );
                           if (!outputItemMatched) {
                              this.debugLogger.info("Output NBT filter failed");
                           }
                        }
                     }

                     if (firstInputItemMatched && secondInputItemMatched && outputItemMatched && !tradeOffer.isOutOfStock()) {
                        this.debugLogger.info("Matched trade offer (highlight)#%d", tradeIndex);
                        offerMatched = true;
                        break;
                     }
                  } else if (offer instanceof IdOffer idOffer && (Integer)idOffer.offerId.get() == tradeIndex && !tradeOffer.isOutOfStock()) {
                     this.debugLogger.info("Matched trade offer (highlight)#%d(default) by ID", tradeIndex);
                     offerMatched = true;
                     break;
                  }
               }
            }

            if (offerMatched) {
               successfulOffer = true;
               this.mc.level.sendPacketToServer(new ServerboundSelectTradePacket(tradeIndex));
               this.mc.gameMode.handleContainerInput(this.mc.player.containerMenu.containerId, 2, 0, ContainerInput.QUICK_MOVE, this.mc.player);
               if ((Boolean)this.oneOfferPerTick.get()) {
                  break;
               }
            }
         }

         if ((Boolean)this.closeWhenDone.get() && successfulOffer) {
            this.mc.screen.onClose();
         }
      }
   }

   @EventHandler
   public void onPacket(Receive event) {
      if (event.packet instanceof ClientboundMerchantOffersPacket packet) {
         this.lastTrade = packet;
      }
   }
}
