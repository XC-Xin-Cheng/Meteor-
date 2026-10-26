package com.stash.hunt.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.stash.hunt.Addon;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ChestIndex extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> searchRange = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("range")).description("Search chests within this range of the player.")).defaultValue(4))
            .min(1)
            .max(10)
            .sliderRange(1, 10)
            .build()
      );
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("delay")).description("Delay in ticks between chest interactions.")).defaultValue(5))
            .min(0)
            .max(40)
            .build()
      );
   private final Setting<ChestIndex.DisplayType> displayType = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("chat-output-type"))
                  .description("Unit to use when displaying results."))
               .defaultValue(ChestIndex.DisplayType.ItemCount))
            .build()
      );
   private final Setting<Boolean> highlightSearched = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                  .name("highlight-searched-blocks"))
               .defaultValue(true))
            .build()
      );
   private final Setting<ShapeMode> shapeMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("box-render-mode"))
                  .description("How the shape for the bounding box is rendered."))
               .defaultValue(ShapeMode.Both))
            .build()
      );
   private final Setting<SettingColor> sideColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("side-color"))
               .description("The side color of the bounding box."))
            .defaultValue(new SettingColor(16, 106, 144, 100))
            .build()
      );
   private final Setting<SettingColor> lineColor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("line-color"))
               .description("The line color of the bounding box."))
            .defaultValue(new SettingColor(16, 106, 144, 255))
            .build()
      );
   private HashSet<BlockPos> searched = new HashSet<>();
   private HashMap<String, Integer> blocks = new HashMap<>();
   private boolean awaiting;
   private BlockPos[] currPos;
   private int tickCounter;

   public ChestIndex() {
      super(Addon.CATEGORY, "chest-index", "Displays a total count of blocks in your chests (buggy and will probably break for lots of chests)");
   }

   private void saveToJson(Gson gson, String fileName, JsonObject json) throws IOException {
      String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
      File file = new File(new File(MeteorClient.FOLDER, "ChestIndex"), fileName + "-" + timeStamp + ".json");
      file.getParentFile().mkdirs();
      Writer writer = new FileWriter(file);
      gson.toJson(json, writer);
      writer.close();
   }

   private List<Entry<String, Integer>> sortBlocks() {
      return this.blocks.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).toList();
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      WButton showBlocks = (WButton)list.add(theme.button("Display the blocks logged")).widget();
      showBlocks.action = () -> {
         this.info("showing blocks", new Object[0]);
         ArrayList<Entry<String, Integer>> blockList = new ArrayList<>(this.blocks.entrySet());
         blockList.sort(Entry.comparingByValue());
         Collections.reverse(blockList);

         double factor = switch ((ChestIndex.DisplayType)this.displayType.get()) {
            case DubCount -> 93312.0;
            case ShulkerCount -> 1728.0;
            default -> 1.0;
         };

         for (Entry<String, Integer> block : blockList) {
            this.info(block.getKey() + ": " + block.getValue().intValue() / factor, new Object[0]);
         }
      };
      WButton clearBlocks = (WButton)list.add(theme.button("Clear Blocks")).widget();
      clearBlocks.action = () -> {
         this.searched = new HashSet<>();
         this.blocks = new HashMap<>();
      };
      Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
      WButton exportByID = (WButton)list.add(theme.button("Export (ID)")).widget();
      exportByID.action = () -> {
         try {
            List<Entry<String, Integer>> sortedBlocks = this.sortBlocks();
            JsonObject json = new JsonObject();

            for (Entry<String, Integer> entry : sortedBlocks) {
               json.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
            }

            this.saveToJson(gson, "blocks_id", json);
            this.info("Exported blocks to ChestIndex/blocks_id.json using IDs (sorted, pretty-printed).", new Object[0]);
         } catch (IOException var6x) {
            this.error("Failed to export blocks (ID): " + var6x.getMessage(), new Object[0]);
         }
      };
      WButton exportByName = (WButton)list.add(theme.button("Export (Name)")).widget();
      exportByName.action = () -> {
         try {
            List<Entry<String, Integer>> sortedBlocks = this.sortBlocks();
            JsonObject json = new JsonObject();

            for (Entry<String, Integer> entry : sortedBlocks) {
               Identifier identifier = Identifier.tryParse(entry.getKey());
               Item item = (Item)BuiltInRegistries.ITEM.getValue(identifier);
               String displayName = Names.get(item);
               json.add(displayName, new JsonPrimitive(entry.getValue()));
            }

            this.saveToJson(gson, "blocks_name", json);
            this.info("Exported blocks to ChestIndex/blocks_name.json using human-readable names (sorted, pretty-printed).", new Object[0]);
         } catch (IOException var9x) {
            this.error("Failed to export blocks (Name): " + var9x.getMessage(), new Object[0]);
         }
      };
      WButton exportDubs = (WButton)list.add(theme.button("Export (Dubs)")).widget();
      exportDubs.action = () -> {
         try {
            List<Entry<String, Integer>> sortedBlocks = this.sortBlocks();
            JsonObject json = new JsonObject();

            for (Entry<String, Integer> entry : sortedBlocks) {
               Identifier identifier = Identifier.tryParse(entry.getKey());
               Item item = (Item)BuiltInRegistries.ITEM.getValue(identifier);
               String displayName = Names.get(item);
               int stackSize = item.getDefaultMaxStackSize();
               double dubs = entry.getValue().intValue() / (stackSize * 27.0 * 54.0);
               json.add(displayName, new JsonPrimitive(String.format("%.2f", dubs)));
            }

            this.saveToJson(gson, "blocks_dubs", json);
            this.info("Exported blocks to ChestIndex/blocks_dubs.json (calculated as dubs with shulkers).", new Object[0]);
         } catch (IOException var12) {
            this.error("Failed to export blocks (Dubs): " + var12.getMessage(), new Object[0]);
         }
      };
      WButton exportShulkers = (WButton)list.add(theme.button("Export (Shulkers)")).widget();
      exportShulkers.action = () -> {
         try {
            List<Entry<String, Integer>> sortedBlocks = this.sortBlocks();
            JsonObject json = new JsonObject();

            for (Entry<String, Integer> entry : sortedBlocks) {
               try {
                  Identifier identifier = Identifier.tryParse(entry.getKey());
                  Item item = (Item)BuiltInRegistries.ITEM.getValue(identifier);
                  String displayName = Names.get(item);
                  int stackSize = item.getDefaultMaxStackSize();
                  double shulkers = entry.getValue().intValue() / (stackSize * 27.0);
                  json.add(displayName, new JsonPrimitive(String.format("%.2f", shulkers)));
               } catch (Exception var12) {
                  json.add(entry.getKey(), new JsonPrimitive("0.00"));
               }
            }

            this.saveToJson(gson, "blocks_shulkers", json);
            this.info("Exported blocks to ChestIndex/blocks_shulkers.json (calculated as shulkers, by name).", new Object[0]);
         } catch (IOException var13) {
            this.error("Failed to export blocks (Shulkers): " + var13.getMessage(), new Object[0]);
         }
      };
      return list;
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if ((Boolean)this.highlightSearched.get()) {
         for (BlockPos blockPos : this.searched) {
            RenderUtils.renderTickingBlock(
               blockPos.immutable(), (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0, 8, true, false
            );
         }
      }
   }

   public void onActivate() {
      this.awaiting = false;
      this.currPos = new BlockPos[2];
      this.tickCounter = 0;
   }

   @EventHandler
   private void onTick(Pre event) {
      if (!(this.mc.screen instanceof ContainerScreen)) {
         if (this.tickCounter < (Integer)this.delay.get()) {
            this.tickCounter++;
         } else {
            this.tickCounter = 0;
            BlockIterator.register(
               (Integer)this.searchRange.get(),
               (Integer)this.searchRange.get(),
               (blockPos, blockState) -> {
                  if (!this.awaiting
                     && !this.searched.contains(blockPos.immutable())
                     && (
                        blockState.getBlock() == Blocks.CHEST
                           || blockState.getBlock() == Blocks.TRAPPED_CHEST
                           || blockState.getBlock() == Blocks.BARREL
                           || blockState.getBlock() instanceof ShulkerBoxBlock
                     )) {
                     Vec3 vec = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                     BlockHitResult hitResult = new BlockHitResult(vec, Direction.UP, blockPos, false);
                     if (this.mc.gameMode.useItemOn(this.mc.player, InteractionHand.MAIN_HAND, hitResult) == InteractionResult.SUCCESS) {
                        this.awaiting = true;
                        this.mc.player.swing(InteractionHand.MAIN_HAND);
                        this.info("interacted", new Object[0]);
                        this.currPos[0] = blockPos.immutable();
                        if (blockState.getBlock() == Blocks.CHEST || blockState.getBlock() == Blocks.TRAPPED_CHEST) {
                           ChestType chestType = (ChestType)blockState.getValue(ChestBlock.TYPE);
                           if (chestType == ChestType.LEFT || chestType == ChestType.RIGHT) {
                              Direction facing = (Direction)blockState.getValue(ChestBlock.FACING);
                              BlockPos otherPartPos = blockPos.relative(chestType == ChestType.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
                              this.currPos[1] = otherPartPos;
                           }
                        }
                     }
                  }
               }
            );
         }
      }
   }

   @EventHandler
   private void onInventory(InventoryEvent event) {
      AbstractContainerMenu handler = this.mc.player.containerMenu;
      this.awaiting = false;

      for (BlockPos blockPos : this.currPos) {
         if (blockPos != null) {
            this.searched.add(blockPos);
         }
      }

      NonNullList<Slot> slots = handler.slots;

      for (int i = 0; i < slots.size() - 36; i++) {
         ItemStack stack = ((Slot)slots.get(i)).getItem();
         if (!stack.isEmpty()) {
            if (Utils.hasItems(stack)) {
               ItemStack[] items = new ItemStack[27];
               Utils.getItemsInContainerItem(stack, items);

               for (ItemStack item : items) {
                  if (!item.isEmpty()) {
                     String nameOfblock = item.getItem().toString();
                     this.blocks.compute(nameOfblock, (k, currentCount) -> currentCount == null ? item.getCount() : currentCount + item.getCount());
                  }
               }
            } else {
               String nameOfblock = stack.getItem().toString();
               this.blocks.compute(nameOfblock, (k, currentCount) -> currentCount == null ? stack.getCount() : currentCount + stack.getCount());
            }
         }
      }

      this.mc.player.closeContainer();
   }

   private static enum DisplayType {
      ItemCount,
      DubCount,
      ShulkerCount;
   }
}
