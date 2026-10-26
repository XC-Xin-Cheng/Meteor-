package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoEnchant extends Module {
   public final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("delay")).description("The tick delay between enchanting items.")).defaultValue(50))
            .sliderMax(500)
            .min(0)
            .build()
      );
   private final Setting<Integer> level = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("level")).description("Choose enchantment levels 1-3")).defaultValue(3)).max(3).min(1).build());
   private final Setting<Boolean> drop = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("drop"))
                  .description("Automatically drops enchanted items (useful for when not enough inventory space)"))
               .defaultValue(false))
            .build()
      );
   private final Setting<List<Item>> itemWhitelist = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)new meteordevelopment.meteorclient.settings.ItemListSetting.Builder()
                  .name("item-whitelist"))
               .description("Item that require enchantment."))
            .defaultValue(new Item[0])
            .filter(item -> item.equals(Items.BOOK) || new ItemStack(item).isEnchantable())
            .build()
      );

   public AutoEnchant() {
      super(MeteorRejectsAddon.CATEGORY, "auto-enchant", "Automatically enchanting items.");
   }

   @EventHandler
   private void onOpenScreen(OpenScreenEvent event) {
      if (Objects.requireNonNull(this.mc.player).containerMenu instanceof EnchantmentMenu) {
         MeteorExecutor.execute(this::autoEnchant);
      }
   }

   private void autoEnchant() {
      if (Objects.requireNonNull(this.mc.player).containerMenu instanceof EnchantmentMenu handler) {
         if (this.mc.player.experienceLevel < 30) {
            this.info("You don't have enough experience levels", new Object[0]);
         } else {
            while (this.getEmptySlotCount(handler) > 2 || this.drop.get()) {
               if (!(this.mc.player.containerMenu instanceof EnchantmentMenu)) {
                  this.info("Enchanting table is closed.", new Object[0]);
                  break;
               }

               if (handler.getGoldCount() < (Integer)this.level.get() && !this.fillLapisItem()) {
                  this.info("Lapis lazuli is not found.", new Object[0]);
                  break;
               }

               if (!this.fillCanEnchantItem()) {
                  this.info("No items found to enchant.", new Object[0]);
                  break;
               }

               Objects.requireNonNull(this.mc.gameMode).handleInventoryButtonClick(handler.containerId, (Integer)this.level.get() - 1);
               if (this.getEmptySlotCount(handler) > 2) {
                  InvUtils.shiftClick().slotId(0);
               } else if ((Boolean)this.drop.get() && handler.getSlot(0).hasItem()) {
                  this.mc.execute(() -> InvUtils.drop().slotId(0));
               }

               try {
                  Thread.sleep(((Integer)this.delay.get()).intValue());
               } catch (InterruptedException var3) {
                  throw new RuntimeException(var3);
               }
            }
         }
      }
   }

   private boolean fillCanEnchantItem() {
      FindItemResult res = InvUtils.find(stack -> ((List)this.itemWhitelist.get()).contains(stack.getItem()));
      if (!res.found()) {
         return false;
      } else {
         InvUtils.shiftClick().slot(res.slot());
         return true;
      }
   }

   private boolean fillLapisItem() {
      FindItemResult res = InvUtils.find(new Item[]{Items.LAPIS_LAZULI});
      if (!res.found()) {
         return false;
      } else {
         InvUtils.shiftClick().slot(res.slot());
         return true;
      }
   }

   private int getEmptySlotCount(AbstractContainerMenu handler) {
      int emptySlotCount = 0;

      for (int i = 0; i < handler.slots.size(); i++) {
         if (((Slot)handler.slots.get(i)).getItem().getItem().equals(Items.AIR)) {
            emptySlotCount++;
         }
      }

      return emptySlotCount;
   }
}
