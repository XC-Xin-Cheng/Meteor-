package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoSoup extends Module {
   private static final String desc = "Automatically eats soup when your health is low on some servers.";
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<Double> health = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("health")).description("Eats a soup when your health reaches this value or falls below it."))
            .defaultValue(6.5)
            .min(0.5)
            .sliderMin(0.5)
            .sliderMax(9.5)
            .build()
      );
   private int oldSlot = -1;

   public AutoSoup() {
      super(MeteorRejectsAddon.CATEGORY, "auto-soup", "Automatically eats soup when your health is low on some servers.");
   }

   public void onDeactivate() {
      this.stopIfEating();
   }

   @EventHandler
   private void onTick(Post event) {
      for (int i = 0; i < 36; i++) {
         ItemStack stack = this.mc.player.getInventory().getItem(i);
         if (stack != null && stack.getItem() == Items.BOWL && i != 9) {
            ItemStack emptyBowlStack = this.mc.player.getInventory().getItem(9);
            boolean swap = !emptyBowlStack.isEmpty() && emptyBowlStack.getItem() != Items.BOWL;
            InvUtils.click().slot(i < 9 ? 36 + i : i);
            InvUtils.click().slot(9);
            if (swap) {
               InvUtils.click().slot(i < 9 ? 36 + i : i);
            }
         }
      }

      int soupInHotbar = this.findSoup(0, 9);
      if (soupInHotbar == -1) {
         this.stopIfEating();
         int soupInInventory = this.findSoup(9, 36);
         if (soupInInventory != -1) {
            InvUtils.quickSwap().slot(soupInInventory);
         }
      } else if (!this.shouldEatSoup()) {
         this.stopIfEating();
      } else {
         if (this.oldSlot == -1) {
            this.oldSlot = this.mc.player.getInventory().getSelectedSlot();
         }

         this.mc.player.getInventory().setSelectedSlot(soupInHotbar);
         this.mc.options.keyUse.setDown(true);
         this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND);
      }
   }

   private int findSoup(int startSlot, int endSlot) {
      List<Item> stews = List.of(Items.RABBIT_STEW, Items.MUSHROOM_STEW, Items.BEETROOT_SOUP);

      for (int i = startSlot; i < endSlot; i++) {
         ItemStack stack = this.mc.player.getInventory().getItem(i);
         if (stack != null && stews.contains(stack.getItem())) {
            return i;
         }
      }

      return -1;
   }

   private boolean shouldEatSoup() {
      return this.mc.player.getHealth() > this.health.get() * 2.0 ? false : !this.isClickable(this.mc.hitResult);
   }

   private boolean isClickable(HitResult hitResult) {
      switch (hitResult) {
         case null:
            return false;
         case EntityHitResult var9:
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            Entity entity = ((EntityHitResult)this.mc.hitResult).getEntity();
            return entity instanceof Villager || entity instanceof TamableAnimal;
         case BlockHitResult blockHitResult:
            BlockPos pos = ((BlockHitResult)this.mc.hitResult).getBlockPos();
            if (pos == null) {
               return false;
            }

            Block block = this.mc.level.getBlockState(pos).getBlock();
            return block instanceof BaseEntityBlock || block instanceof CraftingTableBlock;
         default:
            return false;
      }
   }

   private void stopIfEating() {
      if (this.oldSlot != -1) {
         this.mc.options.keyUse.setDown(false);
         this.mc.player.getInventory().setSelectedSlot(this.oldSlot);
         this.oldSlot = -1;
      }
   }
}
