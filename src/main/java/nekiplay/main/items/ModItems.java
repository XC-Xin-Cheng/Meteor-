package nekiplay.main.items;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class ModItems {
   public static Item METEOR_PLUS_LOGO_ITEM = null;
   public static Item METEOR_PLUS_LOGO_MODS_ITEM = null;
   public static Item METEOR_PLUS_STAR_ITEM = null;
   public static Item METEOR_PLUS_DIAMOND_ITEM = null;
   public static Item METEOR_PLUS_MONEY_ITEM = null;

   public static Item register(String path, Function<Properties, Item> factory, Properties settings) {
      Identifier id = Identifier.fromNamespaceAndPath("meteorplus", path);
      ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
      return (Item)Registry.register(BuiltInRegistries.ITEM, key, factory.apply(settings.setId(key)));
   }

   public static void initialize() {
      METEOR_PLUS_LOGO_ITEM = register("logo", Item::new, new Properties());
      METEOR_PLUS_LOGO_MODS_ITEM = register("logo_mods", Item::new, new Properties());
      METEOR_PLUS_STAR_ITEM = register("star", Item::new, new Properties());
      METEOR_PLUS_DIAMOND_ITEM = register("diamond", Item::new, new Properties());
      METEOR_PLUS_MONEY_ITEM = register("money", Item::new, new Properties());
   }
}
