package com.seedfinding.mcfeature.loot.enchantment;

import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Enchantments {
   private static final HashSet<String> ARMOR_TYPES = new HashSet<>(Arrays.asList("NETHERITE", "DIAMOND", "GOLDEN", "IRON", "LEATHER", "CHAINMAIL"));
   private static final HashSet<String> TOOL_TYPES = new HashSet<>(Arrays.asList("NETHERITE", "DIAMOND", "GOLDEN", "IRON", "STONE", "WOODEN"));
   private static final HashSet<String> BOOKS = new HashSet<>(Arrays.asList("ENCHANTED_BOOK", "BOOK"));
   public static final HashSet<String> ARMOR_HEAD = new HashSet<String>() {
      {
         for (String type : Enchantments.ARMOR_TYPES) {
            this.add(type + "_HELMET");
         }

         this.addAll(Enchantments.BOOKS);
         this.add("TURTLE_HELMET");
      }
   };
   public static final HashSet<String> ARMOR_CHEST = new HashSet<String>() {
      {
         for (String type : Enchantments.ARMOR_TYPES) {
            this.add(type + "_CHESTPLATE");
         }

         this.addAll(Enchantments.BOOKS);
         this.add("ELYTRA");
      }
   };
   public static final HashSet<String> ARMOR_LEGGINGS = new HashSet<String>() {
      {
         for (String type : Enchantments.ARMOR_TYPES) {
            this.add(type + "_LEGGINGS");
         }

         this.addAll(Enchantments.BOOKS);
      }
   };
   public static final HashSet<String> ARMOR_FEET = new HashSet<String>() {
      {
         for (String type : Enchantments.ARMOR_TYPES) {
            this.add(type + "_BOOTS");
         }

         this.addAll(Enchantments.BOOKS);
      }
   };
   public static final HashSet<String> ARMOR = new HashSet<String>() {
      {
         this.addAll(Enchantments.ARMOR_HEAD);
         this.addAll(Enchantments.ARMOR_CHEST);
         this.addAll(Enchantments.ARMOR_LEGGINGS);
         this.addAll(Enchantments.ARMOR_FEET);
      }
   };
   public static final HashSet<String> SWORDS = new HashSet<String>() {
      {
         for (String type : Enchantments.TOOL_TYPES) {
            this.add(type + "_SWORD");
         }
      }
   };
   public static final HashSet<String> AXES = new HashSet<String>() {
      {
         for (String type : Enchantments.TOOL_TYPES) {
            this.add(type + "_AXE");
         }
      }
   };
   public static final HashSet<String> HOES = new HashSet<String>() {
      {
         for (String type : Enchantments.TOOL_TYPES) {
            this.add(type + "_HOE");
         }
      }
   };
   public static final HashSet<String> PICKAXES = new HashSet<String>() {
      {
         for (String type : Enchantments.TOOL_TYPES) {
            this.add(type + "_PICKAXE");
         }
      }
   };
   public static final HashSet<String> SHOVELS = new HashSet<String>() {
      {
         for (String type : Enchantments.TOOL_TYPES) {
            this.add(type + "_SHOVEL");
         }
      }
   };
   public static final HashSet<String> BOW = new HashSet<String>() {
      {
         this.add("BOW");
         this.addAll(Enchantments.BOOKS);
      }
   };
   public static final HashSet<String> CROSSBOW = new HashSet<String>() {
      {
         this.add("CROSSBOW");
         this.addAll(Enchantments.BOOKS);
      }
   };
   public static final HashSet<String> FISHING_ROD = new HashSet<String>() {
      {
         this.add("FISHING_ROD");
         this.addAll(Enchantments.BOOKS);
      }
   };
   public static final HashSet<String> TRIDENT = new HashSet<String>() {
      {
         this.add("TRIDENT");
         this.addAll(Enchantments.BOOKS);
      }
   };
   public static final HashSet<String> BREAKABLE = new HashSet<String>() {
      {
         this.addAll(Enchantments.CROSSBOW);
         this.addAll(Enchantments.BOW);
         this.addAll(Enchantments.TRIDENT);
         this.addAll(Enchantments.FISHING_ROD);
         this.addAll(Enchantments.ARMOR);
         this.addAll(Enchantments.SWORDS);
         this.addAll(Enchantments.AXES);
         this.addAll(Enchantments.HOES);
         this.addAll(Enchantments.PICKAXES);
         this.addAll(Enchantments.SHOVELS);
      }
   };
   public static final HashSet<String> DIGGER = new HashSet<String>() {
      {
         this.addAll(Enchantments.HOES);
         this.addAll(Enchantments.PICKAXES);
         this.addAll(Enchantments.AXES);
         this.addAll(Enchantments.SHOVELS);
         this.addAll(Enchantments.BOOKS);
      }
   };
   public static final HashSet<String> WEAPON = new HashSet<String>() {
      {
         this.addAll(Enchantments.SWORDS);
         this.addAll(Enchantments.BOOKS);
      }
   };
   public static final HashSet<String> DAMAGE = new HashSet<String>() {
      {
         this.addAll(Enchantments.SWORDS);
         this.addAll(Enchantments.AXES);
         this.addAll(Enchantments.BOOKS);
      }
   };
   public static final HashSet<String> THORNS = new HashSet<String>() {
      {
         this.addAll(Enchantments.ARMOR_CHEST);
         this.remove("ELYTRA");
      }
   };
   public static final HashSet<String> VANISHABLE = new HashSet<String>() {
      {
         this.addAll(Enchantments.BREAKABLE);
      }
   };
   public static final HashSet<String> SingleEnchants = new HashSet<>(
      Arrays.asList("aqua_affinity", "binding_curse", "channeling", "silk_touch", "flame", "infinity", "multishot", "mending", "vanishing_curse")
   );
   public static final List<HashSet<String>> allCategories = new ArrayList<>(
      Arrays.asList(ARMOR, ARMOR_HEAD, ARMOR_CHEST, ARMOR_FEET, BOW, BREAKABLE, CROSSBOW, DIGGER, DAMAGE, FISHING_ROD, TRIDENT, WEAPON, VANISHABLE, THORNS)
   );
   private static final Integer COMMON = 10;
   private static final Integer UNCOMMON = 5;
   private static final Integer RARE = 2;
   private static final Integer VERY_RARE = 1;
   public static final ConcurrentHashMap<MCVersion, List<Enchantment>> CACHE_ENCHANTMENT_REGISTRY = new ConcurrentHashMap<>();

   public static boolean canApply(Enchantment enchantment, ItemStack item) {
      return enchantment.getCategory().contains(item.getItem().getName().toUpperCase());
   }

   public static List<Enchantment> removeAllNull(List<Enchantment> list) {
      list.removeIf(Objects::isNull);
      return list;
   }

   public static List<Enchantment> getFor(MCVersion version) {
      return CACHE_ENCHANTMENT_REGISTRY.computeIfAbsent(version, key -> apply(new ArrayList<>(), version));
   }

   public static List<Enchantment> apply(List<Enchantment> enchantments, MCVersion version) {
      enchantments.clear();
      enchantments.add(
         new Enchantment(
            "protection",
            COMMON,
            ARMOR,
            1,
            4,
            (i, n) -> n < 1 + (i - 1) * 11,
            (i, n) -> n > 1 + (i - 1) * 11 + 11,
            new HashSet<>(Arrays.asList("protection", "fire_protection", "projectile_protection", "blast_protection"))
         )
      );
      enchantments.add(
         new Enchantment(
            "fire_protection",
            UNCOMMON,
            ARMOR,
            1,
            4,
            (i, n) -> n < 10 + (i - 1) * 8,
            (i, n) -> n > 10 + (i - 1) * 8 + 8,
            new HashSet<>(Arrays.asList("protection", "fire_protection", "projectile_protection", "blast_protection"))
         )
      );
      enchantments.add(
         new Enchantment(
            "feather_falling",
            UNCOMMON,
            ARMOR_FEET,
            1,
            4,
            (i, n) -> n < 5 + (i - 1) * 6,
            (i, n) -> n > 5 + (i - 1) * 6 + 6,
            new HashSet<>(Collections.singletonList("feather_falling"))
         )
      );
      enchantments.add(
         new Enchantment(
            "blast_protection",
            RARE,
            ARMOR,
            1,
            4,
            (i, n) -> n < 5 + (i - 1) * 8,
            (i, n) -> n > 5 + (i - 1) * 8 + 8,
            new HashSet<>(Arrays.asList("protection", "fire_protection", "projectile_protection", "blast_protection"))
         )
      );
      enchantments.add(
         new Enchantment(
            "projectile_protection",
            UNCOMMON,
            ARMOR,
            1,
            4,
            (i, n) -> n < 3 + (i - 1) * 6,
            (i, n) -> n > 3 + (i - 1) * 6 + 6,
            new HashSet<>(Arrays.asList("protection", "fire_protection", "projectile_protection", "blast_protection"))
         )
      );
      enchantments.add(
         new Enchantment(
            "respiration", RARE, ARMOR_HEAD, 1, 3, (i, n) -> n < 10 * i, (i, n) -> n > 10 * i + 30, new HashSet<>(Collections.singletonList("respiration"))
         )
      );
      enchantments.add(
         new Enchantment("aqua_affinity", RARE, ARMOR_HEAD, 1, 1, (i, n) -> n < 1, (i, n) -> n > 41, new HashSet<>(Collections.singletonList("aqua_affinity")))
      );
      enchantments.add(
         new Enchantment(
            "thorns",
            VERY_RARE,
            THORNS,
            1,
            3,
            (i, n) -> n < 10 + 20 * (i - 1),
            (i, n) -> n > 10 + 20 * (i - 1) + 50,
            new HashSet<>(Collections.singletonList("thorns"))
         )
      );
      if (version.isNewerOrEqualTo(MCVersion.v1_8)) {
         enchantments.add(
            new Enchantment(
               "depth_strider",
               RARE,
               ARMOR_FEET,
               1,
               3,
               (i, n) -> n < i * 10,
               (i, n) -> n > i * 10 + 15,
               new HashSet<>(Arrays.asList("frost_walker", "depth_strider"))
            )
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_9)) {
         enchantments.add(
            new Enchantment(
               "frost_walker",
               RARE,
               ARMOR_FEET,
               1,
               2,
               (i, n) -> n < i * 10,
               (i, n) -> n > i * 10 + 15,
               new HashSet<>(Arrays.asList("frost_walker", "depth_strider")),
               true
            )
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_11)) {
         enchantments.add(
            new Enchantment(
               "binding_curse", VERY_RARE, ARMOR, 1, 1, (i, n) -> n < 25, (i, n) -> n > 50, new HashSet<>(Collections.singletonList("binding_curse")), true
            )
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_16)) {
         enchantments.add(
            new Enchantment(
               "soul_speed",
               VERY_RARE,
               ARMOR_FEET,
               1,
               3,
               (i, n) -> n < i * 10,
               (i, n) -> n > i * 10 + 15,
               new HashSet<>(Collections.singletonList("soul_speed")),
               true,
               false
            )
         );
      }

      enchantments.add(
         new Enchantment(
            "sharpness",
            COMMON,
            DAMAGE,
            1,
            5,
            (i, n) -> n < 1 + (i - 1) * 11,
            (i, n) -> n > 1 + (i - 1) * 11 + 20,
            new HashSet<>(Arrays.asList("sharpness", "smite", "bane_of_arthropods"))
         )
      );
      enchantments.add(
         new Enchantment(
            "smite",
            UNCOMMON,
            DAMAGE,
            1,
            5,
            (i, n) -> n < 5 + (i - 1) * 8,
            (i, n) -> n > 5 + (i - 1) * 8 + 20,
            new HashSet<>(Arrays.asList("sharpness", "smite", "bane_of_arthropods"))
         )
      );
      enchantments.add(
         new Enchantment(
            "bane_of_arthropods",
            UNCOMMON,
            DAMAGE,
            1,
            5,
            (i, n) -> n < 5 + (i - 1) * 8,
            (i, n) -> n > 5 + (i - 1) * 8 + 20,
            new HashSet<>(Arrays.asList("sharpness", "smite", "bane_of_arthropods"))
         )
      );
      enchantments.add(
         new Enchantment(
            "knockback",
            UNCOMMON,
            WEAPON,
            1,
            2,
            (i, n) -> n < 5 + 20 * (i - 1),
            (i, n) -> n > 1 + i * 10 + 50,
            new HashSet<>(Collections.singletonList("knockback"))
         )
      );
      enchantments.add(
         new Enchantment(
            "fire_aspect",
            RARE,
            WEAPON,
            1,
            2,
            (i, n) -> n < 10 + 20 * (i - 1),
            (i, n) -> n > 1 + i * 10 + 50,
            new HashSet<>(Collections.singletonList("fire_aspect"))
         )
      );
      enchantments.add(
         new Enchantment(
            "looting", RARE, WEAPON, 1, 3, (i, n) -> n < 15 + (i - 1) * 9, (i, n) -> n > 1 + i * 10 + 50, new HashSet<>(Arrays.asList("looting", "silk_touch"))
         )
      );
      if (version.isNewerOrEqualTo(MCVersion.v1_11_1)) {
         enchantments.add(
            new Enchantment(
               "sweeping",
               RARE,
               WEAPON,
               1,
               3,
               (i, n) -> n < 5 + (i - 1) * 9,
               (i, n) -> n > 5 + (i - 1) * 9 + 15,
               new HashSet<>(Collections.singletonList("sweeping"))
            )
         );
      }

      enchantments.add(
         new Enchantment(
            "efficiency",
            COMMON,
            DIGGER,
            1,
            5,
            (i, n) -> n < 1 + 10 * (i - 1),
            (i, n) -> n > 1 + i * 10 + 50,
            new HashSet<>(Collections.singletonList("efficiency"))
         )
      );
      enchantments.add(
         new Enchantment(
            "silk_touch", VERY_RARE, DIGGER, 1, 1, (i, n) -> n < 15, (i, n) -> n > 1 + i * 10 + 50, new HashSet<>(Arrays.asList("fortune", "silk_touch"))
         )
      );
      enchantments.add(
         new Enchantment(
            "unbreaking",
            UNCOMMON,
            BREAKABLE,
            1,
            3,
            (i, n) -> n < 5 + (i - 1) * 8,
            (i, n) -> n > 1 + i * 10 + 50,
            new HashSet<>(Collections.singletonList("unbreaking"))
         )
      );
      enchantments.add(
         new Enchantment(
            "fortune", RARE, DIGGER, 1, 3, (i, n) -> n < 15 + (i - 1) * 9, (i, n) -> n > 1 + i * 10 + 50, new HashSet<>(Arrays.asList("fortune", "silk_touch"))
         )
      );
      enchantments.add(
         new Enchantment(
            "power", COMMON, BOW, 1, 5, (i, n) -> n < 1 + (i - 1) * 10, (i, n) -> n > 1 + (i - 1) * 10 + 15, new HashSet<>(Collections.singletonList("power"))
         )
      );
      enchantments.add(
         new Enchantment(
            "punch", RARE, BOW, 1, 2, (i, n) -> n < 12 + (i - 1) * 20, (i, n) -> n > 12 + (i - 1) * 20 + 25, new HashSet<>(Collections.singletonList("punch"))
         )
      );
      enchantments.add(new Enchantment("flame", RARE, BOW, 1, 1, (i, n) -> n < 20, (i, n) -> n > 50, new HashSet<>(Collections.singletonList("flame"))));
      enchantments.add(
         new Enchantment("infinity", VERY_RARE, BOW, 1, 1, (i, n) -> n < 20, (i, n) -> n > 50, new HashSet<>(Arrays.asList("mending", "infinity")))
      );
      if (version.isNewerOrEqualTo(MCVersion.v1_7_2)) {
         enchantments.add(
            new Enchantment(
               "luck_of_the_sea",
               RARE,
               FISHING_ROD,
               1,
               3,
               (i, n) -> n < 15 + (i - 1) * 9,
               (i, n) -> n > 1 + i * 10 + 50,
               new HashSet<>(Arrays.asList("luck_of_the_sea", "silk_touch"))
            )
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_7_2)) {
         enchantments.add(
            new Enchantment(
               "lure", RARE, FISHING_ROD, 1, 3, (i, n) -> n < 15 + (i - 1) * 9, (i, n) -> n > 1 + i * 10 + 50, new HashSet<>(Collections.singletonList("lure"))
            )
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_13)) {
         enchantments.add(
            new Enchantment("loyalty", UNCOMMON, TRIDENT, 1, 3, (i, n) -> n < 5 + i * 7, (i, n) -> n > 50, new HashSet<>(Arrays.asList("loyalty", "riptide")))
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_13)) {
         enchantments.add(
            new Enchantment(
               "impaling",
               RARE,
               TRIDENT,
               1,
               5,
               (i, n) -> n < 1 + (i - 1) * 8,
               (i, n) -> n > 1 + (i - 1) * 8 + 20,
               new HashSet<>(Collections.singletonList("impaling"))
            )
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_13)) {
         enchantments.add(
            new Enchantment(
               "riptide", RARE, TRIDENT, 1, 3, (i, n) -> n < 10 + i * 7, (i, n) -> n > 50, new HashSet<>(Arrays.asList("riptide", "loyalty", "channeling"))
            )
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_13)) {
         enchantments.add(
            new Enchantment("channeling", VERY_RARE, TRIDENT, 1, 1, (i, n) -> n < 25, (i, n) -> n > 50, new HashSet<>(Arrays.asList("channeling", "riptide")))
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_14)) {
         enchantments.add(
            new Enchantment("multishot", RARE, CROSSBOW, 1, 1, (i, n) -> n < 20, (i, n) -> n > 50, new HashSet<>(Arrays.asList("multishot", "piercing")))
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_14)) {
         enchantments.add(
            new Enchantment(
               "quick_charge",
               UNCOMMON,
               CROSSBOW,
               1,
               3,
               (i, n) -> n < 12 + (i - 1) * 20,
               (i, n) -> n > 50,
               new HashSet<>(Collections.singletonList("quick_charge"))
            )
         );
      }

      if (version.isNewerOrEqualTo(MCVersion.v1_14)) {
         enchantments.add(
            new Enchantment(
               "piercing", COMMON, CROSSBOW, 1, 4, (i, n) -> n < 1 + (i - 1) * 10, (i, n) -> n > 50, new HashSet<>(Arrays.asList("multishot", "piercing"))
            )
         );
      }

      enchantments.add(
         new Enchantment(
            "mending", RARE, BREAKABLE, 1, 1, (i, n) -> n < i * 25, (i, n) -> n > i * 25 + 50, new HashSet<>(Arrays.asList("mending", "infinity")), true
         )
      );
      enchantments.add(
         new Enchantment(
            "vanishing_curse",
            VERY_RARE,
            VANISHABLE,
            1,
            1,
            (i, n) -> n < 25,
            (i, n) -> n > 50,
            new HashSet<>(Collections.singletonList("vanishing_curse")),
            true
         )
      );
      return enchantments;
   }

   public static HashSet<HashSet<String>> getCategories(ItemStack baseStack) {
      HashSet<HashSet<String>> applicableCategories = new HashSet<>();

      for (HashSet<String> category : allCategories) {
         if (category.contains(baseStack.getItem().getName().toUpperCase())) {
            applicableCategories.add(category);
         }
      }

      return applicableCategories;
   }

   public static List<Enchantment> getApplicableEnchantments(List<Enchantment> enchantments, HashSet<HashSet<String>> applicableCategories) {
      return getApplicableEnchantments(enchantments, applicableCategories, false, true);
   }

   public static List<Enchantment> getApplicableEnchantments(List<Enchantment> enchantments, HashSet<HashSet<String>> applicableCategories, boolean isTreasure) {
      return getApplicableEnchantments(enchantments, applicableCategories, isTreasure, true);
   }

   public static List<Enchantment> getApplicableEnchantments(
      List<Enchantment> enchantments, HashSet<HashSet<String>> applicableCategories, boolean isTreasure, boolean isDiscoverable
   ) {
      List<Enchantment> applicableEnchantments = new ArrayList<>();
      List<String> applicableEnchantmentNames = new ArrayList<>();

      for (Enchantment currentEnchantment : enchantments) {
         if ((!currentEnchantment.isTreasure() || isTreasure)
            && currentEnchantment.isDiscoverable() == isDiscoverable
            && applicableCategories.contains(currentEnchantment.getCategory())
            && !applicableEnchantmentNames.contains(currentEnchantment.getName())) {
            applicableEnchantments.add(currentEnchantment);
            applicableEnchantmentNames.add(currentEnchantment.getName());
         }
      }

      return applicableEnchantments;
   }

   public static void filterCompatibleEnchantments(ArrayList<EnchantmentInstance> list, EnchantmentInstance instance) {
      list.removeIf(e -> e.getIncompatible().contains(instance.getName()) || instance.getIncompatible().contains(e.getName()));
   }

   public Enchantment getEnchantment(List<Enchantment> enchantments, String name) {
      for (Enchantment enchantment : enchantments) {
         if (enchantment.getName().equals(name)) {
            return enchantment;
         }
      }

      return null;
   }
}
