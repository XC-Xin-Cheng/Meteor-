package nekiplay.meteorplus.features.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.ItemListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AutoDropPlus extends Module {
   private final SettingGroup defaultGroup = this.settings.getDefaultGroup();
   private final Setting<List<Item>> items = this.defaultGroup
      .add(((Builder)((Builder)new Builder().name("drop-items")).description("Items to dropping.")).build());
   private final Setting<Integer> delay;
   private final Setting<Boolean> workInstant;
   private final Setting<Boolean> removeContainersItems;
   private final Setting<Boolean> autoDropExcludeHotbar;
   private final Setting<Boolean> removeItems;
   private int tick;

   public AutoDropPlus() {
      super(Categories.Player, "auto-drop", "Auto drop items in inventory.");
      Setting<Boolean>[] workInstantRef = new Setting[1];
      this.delay = this.defaultGroup
         .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                           .name("delay"))
                        .description("drop delay."))
                     .defaultValue(5))
                  .min(0)
                  .onChanged(value -> {
                     if (value != 0 && workInstantRef[0] != null && (Boolean)workInstantRef[0].get()) {
                        workInstantRef[0].set(false);
                     }
                  }))
               .build()
         );
      workInstantRef[0] = this.defaultGroup
         .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                           .name("instant-work"))
                        .description("Drop or remove items instant."))
                     .defaultValue(false))
                  .visible(() -> (Integer)this.delay.get() == 0))
               .build()
         );
      this.workInstant = workInstantRef[0];
      this.removeContainersItems = this.defaultGroup
         .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("work-in-containers"))
                     .description("Work in chests?."))
                  .defaultValue(true))
               .build()
         );
      this.autoDropExcludeHotbar = this.defaultGroup
         .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("work-in-hotbar"))
                     .description("Work in hotbar?."))
                  .defaultValue(true))
               .build()
         );
      this.removeItems = this.defaultGroup
         .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("remover"))
                     .description("Remove items?."))
                  .defaultValue(false))
               .build()
         );
      this.tick = 0;
   }

   @EventHandler
   public void onTickPost(Pre event) {
      int sync = this.mc.player.containerMenu.containerId;

      for (int i = this.autoDropExcludeHotbar.get() ? 0 : 9; i < this.mc.player.getInventory().getContainerSize(); i++) {
         ItemStack itemStack = this.mc.player.getInventory().getItem(i);
         if (((List)this.items.get()).contains(itemStack.getItem().asItem())) {
            if (this.tick != 0) {
               this.tick--;
            } else {
               if ((Boolean)this.removeItems.get() && sync != -1) {
                  this.mc.gameMode.handleContainerInput(sync, invIndexToSlotId(i), 300, ContainerInput.SWAP, this.mc.player);
               } else if (!(Boolean)this.removeItems.get()) {
                  InvUtils.drop().slot(i);
               }

               if (!(Boolean)this.workInstant.get()) {
                  this.tick = (Integer)this.delay.get();
               }
            }

            if (!(Boolean)this.workInstant.get()) {
               break;
            }
         }
      }

      if ((Boolean)this.removeContainersItems.get()) {
         for (int ix = 0; ix < SlotUtils.indexToId(9); ix++) {
            AbstractContainerMenu handler = this.mc.player.containerMenu;
            if (handler.getSlot(ix).hasItem()) {
               Item item = handler.getSlot(ix).getItem().getItem();
               if (((List)this.items.get()).contains(item.asItem())) {
                  if (this.tick == 0) {
                     if ((Boolean)this.removeItems.get()) {
                        this.mc.gameMode.handleContainerInput(handler.containerId, getIndexToSlotId(handler, ix), 300, ContainerInput.SWAP, this.mc.player);
                     } else {
                        InvUtils.drop().slotId(ix);
                     }

                     if (!(Boolean)this.workInstant.get()) {
                        this.tick = (Integer)this.delay.get();
                     }
                  } else {
                     this.tick--;
                  }

                  if (!(Boolean)this.workInstant.get()) {
                     break;
                  }
               }
            }
         }
      }
   }

   public static int invIndexToSlotId(int invIndex) {
      return invIndex < 9 && invIndex != -1 ? 44 - (8 - invIndex) : invIndex;
   }

   public static int getIndexToSlotId(AbstractContainerMenu handler, int invIndex) {
      if (handler instanceof ChestMenu genericContainerScreenHandler) {
         int count = genericContainerScreenHandler.slots.size();
         return invIndex < 0 && invIndex != -1 ? count - (-1 - invIndex) : invIndex;
      } else if (handler instanceof ShulkerBoxMenu genericContainerScreenHandler) {
         int count = genericContainerScreenHandler.slots.size();
         return invIndex < 0 && invIndex != -1 ? count - (-1 - invIndex) : invIndex;
      } else {
         return invIndex < 9 && invIndex != -1 ? 44 - (8 - invIndex) : invIndex;
      }
   }
}
