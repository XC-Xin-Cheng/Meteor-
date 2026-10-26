package zgoly.meteorist.modules.autocrafter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.Nullable;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.mixin.ClientRecipeBookAccessor;
import zgoly.meteorist.mixin.IngredientAccessor;
import zgoly.meteorist.mixin.RecipeFinderAccessor;
import zgoly.meteorist.utils.MeteoristUtils;
import zgoly.meteorist.utils.config.MeteoristConfigManager;
import zgoly.meteorist.utils.misc.DebugLogger;

public class AutoCrafter extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<Boolean> fixDesync = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("fix-desync"))
                  .description("Attempts to fix inventory desync when crafting. Use it only if you have a desync issue."))
               .defaultValue(false))
            .build()
      );
   public static List<BaseAutoCraft> autoCrafts = new ArrayList<>();
   public boolean shouldCraft = false;
   public ContainerInput actionType;
   private final DebugLogger debugLogger = new DebugLogger(this, this.settings);

   public AutoCrafter() {
      super(Meteorist.CATEGORY, "auto-crafter", "Automatically craft items.");
   }

   public CompoundTag toTag() {
      CompoundTag tag = super.toTag();
      ListTag list = new ListTag();

      for (BaseAutoCraft autoCraft : autoCrafts) {
         CompoundTag mTag = new CompoundTag();
         mTag.put("autoCraft", autoCraft.toTag());
         list.add(mTag);
      }

      tag.put("autoCrafts", list);
      return tag;
   }

   public Module fromTag(CompoundTag tag) {
      super.fromTag(tag);
      autoCrafts.clear();

      for (Tag tagII : tag.getListOrEmpty("autoCrafts")) {
         CompoundTag tagI = (CompoundTag)tagII;
         BaseAutoCraft autoCraft = new BaseAutoCraft();
         CompoundTag autoCraftTag = (CompoundTag)tagI.get("autoCraft");
         if (autoCraftTag != null) {
            autoCraft.fromTag(autoCraftTag);
         }

         autoCrafts.add(autoCraft);
      }

      return this;
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      this.fillWidget(theme, list);
      return list;
   }

   private void fillWidget(GuiTheme theme, WVerticalList list) {
      list.clear();

      for (BaseAutoCraft autoCraft : autoCrafts) {
         list.add(theme.settings(autoCraft.settings)).expandX();
         WContainer controls = (WContainer)list.add(theme.horizontalList()).widget();
         if (autoCrafts.size() > 1) {
            WContainer moveContainer = (WContainer)controls.add(theme.horizontalList()).expandX().widget();
            int index = autoCrafts.indexOf(autoCraft);
            if (index > 0) {
               WButton moveUp = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_UP)).expandX().widget();
               moveUp.tooltip = "Move auto craft up.";
               moveUp.action = () -> {
                  autoCrafts.remove(index);
                  autoCrafts.add(index - 1, autoCraft);
                  this.fillWidget(theme, list);
               };
            }

            if (index < autoCrafts.size() - 1) {
               WButton moveDown = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_DOWN)).expandX().widget();
               moveDown.tooltip = "Move auto craft down.";
               moveDown.action = () -> {
                  autoCrafts.remove(index);
                  autoCrafts.add(index + 1, autoCraft);
                  this.fillWidget(theme, list);
               };
            }
         }

         WButton copy = (WButton)controls.add(theme.button(GuiRenderer.COPY)).widget();
         copy.tooltip = "Duplicate auto craft.";
         copy.action = () -> {
            autoCrafts.add(autoCrafts.indexOf(autoCraft), autoCraft.copy());
            this.fillWidget(theme, list);
         };
         WMinus remove = (WMinus)controls.add(theme.minus()).widget();
         remove.tooltip = "Remove auto craft.";
         remove.action = () -> {
            autoCrafts.remove(autoCraft);
            this.fillWidget(theme, list);
         };
      }

      if (!autoCrafts.isEmpty()) {
         list.add(theme.horizontalSeparator()).expandX();
      }

      WContainer controls = (WContainer)list.add(theme.horizontalList()).expandX().widget();
      WButton add = (WButton)controls.add(theme.button("New Auto Craft")).expandX().widget();
      add.action = () -> {
         BaseAutoCraft autoCraft = new BaseAutoCraft();
         autoCrafts.add(autoCraft);
         this.fillWidget(theme, list);
      };
      WButton removeAll = (WButton)controls.add(theme.button("Remove All Auto Crafts")).expandX().widget();
      removeAll.action = () -> {
         autoCrafts.clear();
         this.fillWidget(theme, list);
      };
      MeteoristConfigManager.configManager(theme, list, this);
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.gameMode != null) {
         if (this.mc.player.containerMenu instanceof AbstractCraftingMenu currentScreenHandler) {
            if (this.shouldCraft) {
               if (((Slot)currentScreenHandler.slots.getFirst()).hasItem()) {
                  this.mc.gameMode.handleContainerInput(currentScreenHandler.containerId, 0, 1, this.actionType, this.mc.player);
                  this.debugLogger.info("Crafting...");
               }

               this.shouldCraft = false;
            } else {
               for (BaseAutoCraft autoCraft : autoCrafts) {
                  if ((Boolean)this.fixDesync.get()) {
                     this.mc.player.getInventory().tick();
                  }

                  this.debugLogger.info(" ");
                  this.debugLogger.info(autoCraft.outputItem.get() + " searching...");
                  RecipeDisplayEntry foundRecipe = this.findRecipe(autoCraft);
                  this.debugLogger.info(autoCraft.outputItem.get() + " found: " + foundRecipe);
                  if (foundRecipe != null) {
                     this.mc.gameMode.handlePlaceRecipe(currentScreenHandler.containerId, foundRecipe.id(), (Boolean)autoCraft.stackPerTick.get());
                     this.actionType = autoCraft.dropOnCraft.get() ? ContainerInput.THROW : ContainerInput.QUICK_MOVE;
                     this.debugLogger.info("Selecting recipe...");
                     this.shouldCraft = true;
                     return;
                  }
               }
            }
         }
      }
   }

   public void onDeactivate() {
      this.shouldCraft = false;
   }

   @Nullable
   private RecipeDisplayEntry findRecipe(BaseAutoCraft baseAutoCraft) {
      List<Item> inputWhitelist = (List<Item>)baseAutoCraft.inputWhitelist.get();
      List<Item> inputBlacklist = (List<Item>)baseAutoCraft.inputBlacklist.get();
      ClientRecipeBookAccessor recipeBook = (ClientRecipeBookAccessor)this.mc.player.getRecipeBook();
      Iterator var5 = recipeBook.getRecipes().entrySet().iterator();

      RecipeDisplayEntry recipe;
      label91:
      while (true) {
         Optional<List<Ingredient>> craftingRequirements;
         while (true) {
            if (!var5.hasNext()) {
               return null;
            }

            Entry<RecipeDisplayId, RecipeDisplayEntry> mapEntry = (Entry<RecipeDisplayId, RecipeDisplayEntry>)var5.next();
            recipe = mapEntry.getValue();
            craftingRequirements = recipe.craftingRequirements();
            if (!craftingRequirements.isEmpty() && MeteoristUtils.canDisplayRecipe((AbstractCraftingMenu)this.mc.player.containerMenu, recipe.display())) {
               this.debugLogger.info("Can display check passed");
               StackedItemContents customRecipeFinder = new StackedItemContents();
               this.mc.player.getInventory().fillStackedContents(customRecipeFinder);
               if (customRecipeFinder.canCraft(craftingRequirements.get(), null)) {
                  this.debugLogger.info("Craftable check passed");
                  List<ItemStack> outputStacks = recipe.display().result().resolveForStacks(SlotDisplayContext.fromLevel(this.mc.level));
                  if (outputStacks.getFirst().is((Item)baseAutoCraft.outputItem.get())) {
                     this.debugLogger.info("Output check passed");
                     if (!(Boolean)baseAutoCraft.keepIngredientsForExtra.get()) {
                        break;
                     }

                     boolean isCraftable = ((RecipeFinderAccessor)customRecipeFinder).invokeIsCraftable(craftingRequirements.get(), 2, null);
                     if (isCraftable) {
                        break;
                     }
                  }
               }
            }
         }

         this.debugLogger.info("Extra item check passed");

         for (Ingredient ingredient : craftingRequirements.get()) {
            for (Holder<Item> entry : ((IngredientAccessor)ingredient).getEntries()) {
               if (inputBlacklist.contains(entry.value())) {
                  continue label91;
               }
            }
         }

         this.debugLogger.info("Blacklist check passed");
         if (inputWhitelist.isEmpty()) {
            break;
         }

         List<Item> whitelistCopy = new ArrayList<>(inputWhitelist);

         for (Ingredient ingredient : craftingRequirements.get()) {
            for (Holder<Item> entryx : ((IngredientAccessor)ingredient).getEntries()) {
               Item item = (Item)entryx.value();
               if (whitelistCopy.contains(item)) {
                  whitelistCopy.remove(item);
                  if ((Boolean)baseAutoCraft.useFirstMatchingVariant.get()) {
                     break;
                  }
               }
            }
         }

         if (whitelistCopy.isEmpty()) {
            break;
         }
      }

      this.debugLogger.info("Whitelist check passed");
      return recipe;
   }
}
