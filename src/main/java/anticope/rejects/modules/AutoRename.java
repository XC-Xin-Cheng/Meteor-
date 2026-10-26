package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

public class AutoRename extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRename = this.settings.createGroup("Rename");
   private final SettingGroup sgLabelContainers = this.settings.createGroup("Label Containers");
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("delay")).description("How many ticks to wait between actions.")).defaultValue(2))
            .min(0)
            .sliderMax(40)
            .build()
      );
   private final Setting<List<Item>> items = this.sgRename
      .add(
         ((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)((meteordevelopment.meteorclient.settings.ItemListSetting.Builder)new meteordevelopment.meteorclient.settings.ItemListSetting.Builder()
                     .name("items"))
                  .description("Items to rename."))
               .defaultValue(List.of()))
            .build()
      );
   private final Setting<String> name = this.sgRename
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                     .name("name"))
                  .description("Name to apply to items. Leave blank to keep the default name."))
               .defaultValue(""))
            .build()
      );
   private final Setting<AutoRename.ContainerType> containerType = this.sgLabelContainers
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("container-type"))
                  .description("Which container types to rename based on the first item inside them."))
               .defaultValue(AutoRename.ContainerType.NONE))
            .build()
      );
   private int delayLeft = 0;

   public AutoRename() {
      super(MeteorRejectsAddon.CATEGORY, "auto-rename", "Automatically renames items at an anvil. Can also label shulkers and bundles based on their contents.");
   }

   @EventHandler
   private void onTick(Post ignoredEvent) {
      if (this.mc.gameMode != null) {
         if (!((List)this.items.get()).isEmpty() || this.containerType.get() != AutoRename.ContainerType.NONE) {
            if (this.mc.player.containerMenu instanceof AnvilMenu) {
               if (this.delayLeft > 0) {
                  this.delayLeft--;
               } else {
                  this.delayLeft = (Integer)this.delay.get();
                  Slot slot0 = this.mc.player.containerMenu.getSlot(0);
                  Slot slot1 = this.mc.player.containerMenu.getSlot(1);
                  Slot slot2 = this.mc.player.containerMenu.getSlot(2);
                  if (!slot1.hasItem()) {
                     if (slot2.hasItem()) {
                        if (this.mc.player.experienceLevel >= 1) {
                           this.extractNamed();
                        }
                     } else if (slot0.hasItem()) {
                        this.renameItem(slot0.getItem());
                     } else {
                        this.populateAnvil();
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isContainerTarget(ItemStack st) {
      return switch ((AutoRename.ContainerType)this.containerType.get()) {
         case BOTH -> st.has(DataComponents.CONTAINER) || st.has(DataComponents.BUNDLE_CONTENTS);
         case BUNDLES -> st.has(DataComponents.BUNDLE_CONTENTS);
         case SHULKERS -> st.has(DataComponents.CONTAINER);
         case NONE -> false;
      };
   }

   private void renameItem(ItemStack s) {
      String setname = this.isContainerTarget(s) ? this.getFirstItemName(s) : (String)this.name.get();
      if (!(this.mc.screen instanceof AnvilScreen)) {
         this.error("Not anvil screen", new Object[0]);
         this.toggle();
      } else {
         EditBox input = (EditBox)this.mc.screen.children().get(0);
         input.setValue(setname);
      }
   }

   private String getFirstItemName(ItemStack stack) {
      ItemContainerContents container = (ItemContainerContents)stack.get(DataComponents.CONTAINER);
      if (container != null) {
         Iterator bundle = container.nonEmptyItemCopyStream().toList().iterator();
         if (bundle.hasNext()) {
            ItemStack item = (ItemStack)bundle.next();
            return item.getHoverName().getString();
         }
      }

      BundleContents bundle = (BundleContents)stack.get(DataComponents.BUNDLE_CONTENTS);
      if (bundle != null) {
         Iterator item = bundle.itemCopyStream().toList().iterator();
         if (item.hasNext()) {
            ItemStack itemx = (ItemStack)item.next();
            return itemx.getHoverName().getString();
         }
      }

      return "";
   }

   private void extractNamed() {
      AbstractContainerMenu inv = this.mc.player.containerMenu;

      for (int i = 3; i < 38; i++) {
         if (inv.getSlot(i).hasItem()) {
            InvUtils.shiftClick().fromId(2).toId(i);
            return;
         }
      }
   }

   private void populateAnvil() {
      AbstractContainerMenu inv = this.mc.player.containerMenu;

      for (int i = 3; i < 38; i++) {
         Slot sl = inv.getSlot(i);
         if (sl.hasItem()) {
            ItemStack st = sl.getItem();
            boolean hasCustomName = st.getComponents().has(DataComponents.CUSTOM_NAME);
            boolean isRenameItem = ((List)this.items.get()).contains(st.getItem()) && (((String)this.name.get()).isEmpty() ? hasCustomName : !hasCustomName);
            boolean isContainerItem = this.isContainerTarget(st) && !this.getFirstItemName(st).isEmpty();
            if (isRenameItem || isContainerItem && !hasCustomName) {
               InvUtils.shiftClick().fromId(i).toId(0);
               return;
            }
         }
      }
   }

   public static enum ContainerType {
      BOTH,
      BUNDLES,
      SHULKERS,
      NONE;

      @Override
      public String toString() {
         return switch (this) {
            case BOTH -> "Both";
            case BUNDLES -> "Bundles";
            case SHULKERS -> "Shulkers";
            case NONE -> "None";
         };
      }
   }
}
