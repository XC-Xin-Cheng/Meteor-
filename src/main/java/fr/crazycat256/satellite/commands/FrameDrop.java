package fr.crazycat256.satellite.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;

public class FrameDrop extends Command {
   public FrameDrop() {
      super("framedrop", "Drops all items from item frames around you.", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.executes(context -> {
         if (mc.player != null && mc.level != null && mc.gameMode != null) {
            List<ItemFrame> itemFrames = new ArrayList<>();

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof ItemFrame itemFrame && itemFrame.distanceToSqr(mc.player) <= 16.0 && itemFrame.getItem().getItem() != Items.AIR) {
                  itemFrames.add(itemFrame);
               }
            }

            for (ItemFrame itemFrame : itemFrames) {
               mc.gameMode.attack(mc.player, itemFrame);
            }

            this.info("Dropped " + itemFrames.size() + " items.", new Object[0]);
            return 1;
         } else {
            return 0;
         }
      });
   }
}
