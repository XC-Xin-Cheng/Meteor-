package com.seedfinding.mcfeature.loot.function;

import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mcfeature.loot.LootContext;
import com.seedfinding.mcfeature.loot.enchantment.Enchantment;
import com.seedfinding.mcfeature.loot.enchantment.Enchantments;
import com.seedfinding.mcfeature.loot.item.Item;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EnchantRandomlyFunction extends EnchantmentFunction {
   private List<Enchantment> applicableEnchantments = new ArrayList<>();

   public EnchantRandomlyFunction(Item item) {
      super(item);
   }

   public EnchantRandomlyFunction(Item item, boolean isTreasure) {
      super(item, isTreasure);
   }

   public EnchantRandomlyFunction(Item item, boolean isTreasure, boolean isDiscoverable) {
      super(item, isTreasure, isDiscoverable);
   }

   @Override
   public EnchantmentFunction applyEnchantment(List<Enchantment> enchantments) {
      HashSet<HashSet<String>> applicableCategories = Enchantments.getCategories(new ItemStack(this.item, 1));
      this.applicableEnchantments = Enchantments.getApplicableEnchantments(enchantments, applicableCategories, this.isTreasure, this.isDiscoverable);
      return this;
   }

   @Override
   public ItemStack process(ItemStack baseStack, LootContext context) {
      Item newItem = baseStack.getItem();
      if (this.applicableEnchantments.isEmpty()) {
         return baseStack;
      } else {
         int enchantNr = context.nextInt(this.applicableEnchantments.size());
         Enchantment enchantment = this.applicableEnchantments.get(enchantNr);
         int level = 1;
         if (!Enchantments.SingleEnchants.contains(enchantment.getName())) {
            level = context.nextInt(enchantment.getMaxLevel()) + 1;
         }

         newItem.addEnchantment(new Pair<>(enchantment.getName(), level));
         return new ItemStack(newItem, baseStack.getCount());
      }
   }
}
