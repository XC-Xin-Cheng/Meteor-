package com.seedfinding.mcfeature.loot.item;

import java.util.Objects;

public class ItemStack {
   public static final ItemStack EMPTY = new ItemStack(null);
   private final Item item;
   private int count;

   public ItemStack(Item item) {
      this(item, 1);
   }

   public ItemStack(Item item, int count) {
      this.item = item;
      this.count = count;
   }

   public boolean isEmpty() {
      if (this == EMPTY) {
         return true;
      } else {
         return this.getItem() != null && this.getItem() != Items.AIR ? this.count <= 0 : true;
      }
   }

   public Item getItem() {
      return this.item;
   }

   public int getCount() {
      return this.count;
   }

   public void setCount(int count) {
      this.count = count;
   }

   public ItemStack split(int count) {
      int splitCount = Math.min(count, this.count);
      ItemStack itemstack = new ItemStack(this.item, this.count);
      itemstack.setCount(splitCount);
      this.setCount(this.count - splitCount);
      return itemstack;
   }

   @Override
   public String toString() {
      return "ItemStack{item=" + this.item + ", count=" + this.count + '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof ItemStack)) {
         return false;
      } else {
         ItemStack itemStack = (ItemStack)o;
         return this.count == itemStack.count && Objects.equals(this.item, itemStack.item);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.item, this.count);
   }
}
