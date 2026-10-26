package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.ItemListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection.CraftableStatus;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public class AutoCraft extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<List<Item>> items = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("items")).description("Items you want to get crafted.")).defaultValue(List.of())).build());
   private final Setting<Boolean> antiDesync = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("anti-desync"))
                  .description("Try to prevent inventory desync."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> craftAll = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("craft-all"))
                  .description("Crafts maximum possible amount amount per craft (shift-clicking)"))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> drop = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("drop"))
                  .description("Automatically drops crafted items (useful for when not enough inventory space)"))
               .defaultValue(false))
            .build()
      );

   public AutoCraft() {
      super(MeteorRejectsAddon.CATEGORY, "auto-craft", "Automatically crafts items.");
   }

   @EventHandler
   private void onTick(Post event) {
      if (Utils.canUpdate() && this.mc.gameMode != null) {
         if (!((List)this.items.get()).isEmpty()) {
            if (this.mc.player.containerMenu instanceof CraftingMenu) {
               if ((Boolean)this.antiDesync.get()) {
                  this.mc.player.getInventory().tick();
               }

               CraftingMenu currentScreenHandler = (CraftingMenu)this.mc.player.containerMenu;
               List<Item> itemList = (List<Item>)this.items.get();

               for (RecipeCollection recipeResultCollection : this.mc.player.getRecipeBook().getCollections()) {
                  for (RecipeDisplayEntry recipe : recipeResultCollection.getSelectedRecipes(CraftableStatus.CRAFTABLE)) {
                     RecipeDisplay recipeDisplay = recipe.display();

                     for (ItemStack resultStack : recipeDisplay.result().resolveForStacks(SlotDisplayContext.fromLevel(this.mc.level))) {
                        if (itemList.contains(resultStack.getItem())) {
                           this.mc.gameMode.handlePlaceRecipe(currentScreenHandler.containerId, recipe.id(), (Boolean)this.craftAll.get());
                           this.mc
                              .gameMode
                              .handleContainerInput(
                                 currentScreenHandler.containerId, 0, 1, this.drop.get() ? ContainerInput.THROW : ContainerInput.QUICK_MOVE, this.mc.player
                              );
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
