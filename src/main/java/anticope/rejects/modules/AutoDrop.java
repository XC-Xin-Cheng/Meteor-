package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.ItemListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AutoDrop extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<List<Item>> items = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("items")).description("Items to automatically drop from your inventory.")).defaultValue(List.of()))
            .build()
      );
   private final Setting<Boolean> dropHotbar = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("drop-hotbar"))
                  .description("Also drop matching items from your hotbar."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay"))
                  .description("Delay in ticks between each drop check."))
               .defaultValue(10))
            .min(1)
            .sliderMax(40)
            .build()
      );
   private int timer = 0;

   public AutoDrop() {
      super(MeteorRejectsAddon.CATEGORY, "auto-drop", "Automatically drops selected items from your inventory.");
   }

   public void onActivate() {
      this.timer = 0;
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null) {
         if (this.timer > 0) {
            this.timer--;
         } else {
            this.timer = (Integer)this.delay.get();
            int startSlot = this.dropHotbar.get() ? 0 : 9;

            for (int i = startSlot; i < 36; i++) {
               ItemStack stack = this.mc.player.getInventory().getItem(i);
               if (stack != null && !stack.isEmpty() && ((List)this.items.get()).contains(stack.getItem())) {
                  InvUtils.drop().slot(i);
                  return;
               }
            }
         }
      }
   }
}
