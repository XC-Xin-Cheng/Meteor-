package nekiplay.meteorplus.features.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.utils.ElytraUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class EclipCommand extends Command {
   private int ticks = 0;
   private int slot = -1;
   private double blocks = 0.0;

   public EclipCommand() {
      super("eclip", "Elytra clip need elytra bypass most anticheats", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(argument("blocks", DoubleArgumentType.doubleArg()).executes(context -> {
         LocalPlayer player = mc.player;

         assert player != null;

         double blocks2 = (Double)context.getArgument("blocks", Double.class);
         if (this.work()) {
            this.blocks = blocks2;
            MeteorClient.EVENT_BUS.subscribe(this);
         } else {
            this.ticks = 0;
         }

         return 1;
      }));
      builder.then(literal("up").executes(c -> {
         if (this.work()) {
            this.blocks = this.findBlock(true, 15);
            MeteorClient.EVENT_BUS.subscribe(this);
         } else {
            this.ticks = 0;
         }

         return 1;
      }));
      builder.then(literal("down").executes(c -> {
         if (this.work()) {
            this.blocks = this.findBlock(false, 15);
            MeteorClient.EVENT_BUS.subscribe(this);
         } else {
            this.ticks = 0;
         }

         return 1;
      }));
   }

   private boolean work() {
      LocalPlayer player = mc.player;

      assert player != null;

      FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
      if (elytra.found()) {
         this.ticks = 0;
         return true;
      } else {
         this.error(Names.get(Items.ELYTRA) + " not found", new Object[0]);
         return false;
      }
   }

   private Block getBlock(BlockPos pos) {
      return mc.level.getBlockState(pos).getBlock();
   }

   private double findBlock(boolean up, int maximum) {
      if (up) {
         BlockPos pos = mc.player.blockPosition();

         for (int i = maximum; i >= 0; i--) {
            if (this.getBlock(pos.offset(0, i, 0)) == Blocks.AIR
               && this.getBlock(pos.offset(0, i + 1, 0)) == Blocks.AIR
               && this.getBlock(pos.offset(0, i - 1, 0)) != Blocks.AIR) {
               return i;
            }
         }
      } else {
         BlockPos pos = mc.player.blockPosition();

         for (int ix = -maximum; ix <= 0; ix++) {
            if (this.getBlock(pos.offset(0, ix, 0)) == Blocks.AIR
               && this.getBlock(pos.offset(0, ix + 1, 0)) == Blocks.AIR
               && this.getBlock(pos.offset(0, ix - 1, 0)) != Blocks.AIR) {
               return ix;
            }
         }
      }

      return 0.0;
   }

   @EventHandler
   private void onTick(Pre event) {
      this.clip(this.blocks);
   }

   private void clip(double blocks) {
      if (blocks != 0.0) {
         LocalPlayer player = mc.player;

         assert player != null;

         switch (this.ticks) {
            case 0:
               FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
               this.slot = elytra.slot();
               InvUtils.move().from(this.slot).toArmor(2);
               this.ticks++;
            case 1:
               mc.player.connection.send(new StatusOnly(false, mc.player.horizontalCollision));
               this.ticks++;
            case 2:
               mc.player.connection.send(new StatusOnly(false, mc.player.horizontalCollision));
               this.ticks++;
            case 3:
               ElytraUtils.startFly();
               this.ticks++;
            case 4:
               player.setPos(player.getX(), player.getY() + blocks, player.getZ());
               mc.player.connection.send(new Pos(player.getX(), player.getY() + blocks, player.getZ(), false, mc.player.horizontalCollision));
               this.ticks++;
            case 5:
               ElytraUtils.startFly();
               this.ticks++;
            case 6:
               this.ticks = 0;
               InvUtils.move().fromArmor(2).to(this.slot);
               MeteorClient.EVENT_BUS.unsubscribe(this);
         }
      } else {
         MeteorClient.EVENT_BUS.unsubscribe(this);
      }
   }
}
