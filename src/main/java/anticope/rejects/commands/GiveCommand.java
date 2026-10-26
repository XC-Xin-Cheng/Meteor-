package anticope.rejects.commands;

import anticope.rejects.arguments.ClientPosArgumentType;
import anticope.rejects.arguments.EnumStringArgumentType;
import anticope.rejects.utils.GiveUtils;
import anticope.rejects.utils.accounts.GetPlayerUUID;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.phys.Vec3;

public class GiveCommand extends Command {
   private final Collection<String> PRESETS = GiveUtils.PRESETS.keySet();

   public GiveCommand() {
      super("give", "Gives items in creative", new String[]{"item", "kit"});
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(
         literal("egg")
            .executes(
               ctx -> {
                  ItemStack inHand = mc.player.getMainHandItem();
                  ItemStack item = new ItemStack(Items.STRIDER_SPAWN_EGG);
                  CompoundTag ct = new CompoundTag();
                  if (inHand.getItem() instanceof BlockItem) {
                     ct.putInt("Time", 1);
                     ct.putString("id", "minecraft:falling_block");
                     ct.put("BlockState", new CompoundTag());
                     ct.getCompound("BlockState")
                        .ifPresent(compound -> compound.put("Name", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(inHand.getItem()).toString())));
                  } else {
                     ct.putString("id", "minecraft:item");
                     CompoundTag itemTag = new CompoundTag();
                     itemTag.putString("id", BuiltInRegistries.ITEM.getKey(inHand.getItem()).toString());
                     itemTag.putInt("Count", inHand.getCount());
                     ct.put("Item", itemTag);
                  }

                  CompoundTag t = new CompoundTag();
                  t.put("EntityTag", ct);
                  DataComponentPatch changes = DataComponentPatch.builder()
                     .set(DataComponents.CUSTOM_NAME, inHand.getHoverName())
                     .set(DataComponents.CUSTOM_DATA, CustomData.of(t))
                     .build();
                  item.applyComponentsAndValidate(changes);
                  GiveUtils.giveItem(item);
                  return 1;
               }
            )
      );
      builder.then(((LiteralArgumentBuilder)literal("holo").then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
         String message = ((String)ctx.getArgument("message", String.class)).replace("&", "§");
         return this.createHolo(message, mc.player.getX(), mc.player.getY(), mc.player.getZ());
      }))).then(argument("pos", ClientPosArgumentType.pos()).then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
         String message = ((String)ctx.getArgument("message", String.class)).replace("&", "§");
         Vec3 pos = ClientPosArgumentType.getPos(ctx, "pos");
         return this.createHolo(message, pos.x, pos.y, pos.z);
      }))));
      builder.then(
         literal("bossbar")
            .then(
               argument("message", StringArgumentType.greedyString())
                  .executes(
                     ctx -> {
                        String message = ((String)ctx.getArgument("message", String.class)).replace("&", "§");
                        ItemStack stack = new ItemStack(Items.BAT_SPAWN_EGG);
                        CompoundTag tag = new CompoundTag();
                        tag.putBoolean("NoAI", true);
                        tag.putBoolean("Silent", true);
                        tag.putBoolean("PersistenceRequired", true);
                        tag.putBoolean("Invulnerable", true);
                        ListTag effects = new ListTag();
                        CompoundTag invisEffect = new CompoundTag();
                        invisEffect.putString("id", "minecraft:invisibility");
                        invisEffect.putInt("amplifier", 0);
                        invisEffect.putInt("duration", Integer.MAX_VALUE);
                        invisEffect.putBoolean("ambient", false);
                        invisEffect.putBoolean("show_particles", false);
                        effects.add(invisEffect);
                        tag.put("active_effects", effects);
                        DataComponentPatch changes = DataComponentPatch.builder()
                           .set(DataComponents.CUSTOM_NAME, Component.literal(message))
                           .set(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.WITHER, tag))
                           .build();
                        stack.applyComponentsAndValidate(changes);
                        GiveUtils.giveItem(stack);
                        return 1;
                     }
                  )
            )
      );
      builder.then(
         literal("head")
            .then(
               argument("owner", StringArgumentType.greedyString())
                  .executes(
                     ctx -> {
                        String playerName = (String)ctx.getArgument("owner", String.class);
                        ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
                        DataComponentPatch changes = DataComponentPatch.builder()
                           .set(DataComponents.PROFILE, ResolvableProfile.createResolved(new GameProfile(GetPlayerUUID.getUUID(playerName), playerName)))
                           .build();
                        itemStack.applyComponentsAndValidate(changes);
                        GiveUtils.giveItem(itemStack);
                        return 1;
                     }
                  )
            )
      );
      builder.then(literal("preset").then(argument("name", new EnumStringArgumentType(this.PRESETS)).executes(context -> {
         String name = (String)context.getArgument("name", String.class);
         GiveUtils.giveItem(GiveUtils.getPreset(name));
         return 1;
      })));
   }

   private int createHolo(String message, double x, double y, double z) throws CommandSyntaxException {
      ItemStack stack = new ItemStack(Items.STRIDER_SPAWN_EGG);
      CompoundTag tag = new CompoundTag();
      ListTag pos = new ListTag();
      pos.add(DoubleTag.valueOf(x));
      pos.add(DoubleTag.valueOf(y));
      pos.add(DoubleTag.valueOf(z));
      tag.put("Pos", pos);
      tag.putBoolean("Invisible", true);
      tag.putBoolean("Invulnerable", true);
      tag.putBoolean("NoGravity", true);
      tag.putBoolean("CustomNameVisible", true);
      DataComponentPatch changes = DataComponentPatch.builder()
         .set(DataComponents.CUSTOM_NAME, Component.literal(message))
         .set(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityType.ARMOR_STAND, tag))
         .build();
      stack.applyComponentsAndValidate(changes);
      GiveUtils.giveItem(stack);
      return 1;
   }
}
