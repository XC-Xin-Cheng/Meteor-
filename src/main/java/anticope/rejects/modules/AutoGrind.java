package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class AutoGrind extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("delay")).description("The tick delay between grinding items.")).defaultValue(50))
            .sliderMax(500)
            .min(0)
            .build()
      );
   private final Setting<List<Item>> itemBlacklist = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)new meteordevelopment.meteorclient.settings.ItemListSetting.Builder()
                  .name("item-blacklist"))
               .description("Items that should be ignored."))
            .defaultValue(new Item[0])
            .filter(Item -> Item.components().get(DataComponents.DAMAGE) != null)
            .build()
      );
   private final Setting<Set<ResourceKey<Enchantment>>> enchantmentBlacklist = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnchantmentListSetting.Builder)((meteordevelopment.meteorclient.settings.EnchantmentListSetting.Builder)new meteordevelopment.meteorclient.settings.EnchantmentListSetting.Builder()
                  .name("enchantment-blacklist"))
               .description("Enchantments that should be ignored."))
            .defaultValue(new ResourceKey[0])
            .build()
      );

   public AutoGrind() {
      super(MeteorRejectsAddon.CATEGORY, "auto-grind", "Automatically disenchants items.");
   }

   @EventHandler
   private void onOpenScreen(OpenScreenEvent event) {
      if (this.mc.player.containerMenu instanceof GrindstoneMenu) {
         MeteorExecutor.execute(() -> {
            for (int i = 0; i <= this.mc.player.getInventory().getContainerSize(); i++) {
               if (this.canGrind(this.mc.player.getInventory().getItem(i))) {
                  try {
                     Thread.sleep(((Integer)this.delay.get()).intValue());
                  } catch (InterruptedException var3) {
                     var3.printStackTrace();
                  }

                  if (this.mc.screen == null) {
                     break;
                  }

                  InvUtils.shiftClick().slot(i);
                  InvUtils.move().fromId(2).to(i);
               }
            }
         });
      }
   }

   private boolean canGrind(ItemStack stack) {
      if (((List)this.itemBlacklist.get()).contains(stack.getItem())) {
         return false;
      } else {
         ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
         int availEnchs = 0;

         for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (!enchantment.is(EnchantmentTags.CURSE)) {
               availEnchs++;
               if (enchantment.unwrapKey().map(((Set)this.enchantmentBlacklist.get())::contains).orElse(false)) {
                  return false;
               }
            }
         }

         return !enchantments.isEmpty() && availEnchs > 0;
      }
   }
}
