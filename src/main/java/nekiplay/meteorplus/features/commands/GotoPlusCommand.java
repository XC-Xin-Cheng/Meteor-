package nekiplay.meteorplus.features.commands;

import baritone.api.BaritoneAPI;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class GotoPlusCommand extends Command {
   private final Pool<GotoPlusCommand.Cross> crossPool = new Pool(() -> new GotoPlusCommand.Cross());
   private final List<GotoPlusCommand.Cross> crosses = new ArrayList<>();

   public GotoPlusCommand() {
      super("gotoplus", "Baritone goto to selected type", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(literal("nolight").executes(c -> {
         MeteorExecutor.execute(() -> {
            for (GotoPlusCommand.Cross cross : this.crosses) {
               this.crossPool.free(cross);
            }

            this.crosses.clear();
            BlockIterator.register(64, mc.level.getHeight() / 2, (blockPos, blockState) -> {
               switch (BlockUtils.isValidMobSpawn(blockPos, mc.level.getBlockState(blockPos), 0)) {
                  case Potential:
                  case Always:
                     this.crosses.add(((GotoPlusCommand.Cross)this.crossPool.get()).set(blockPos));
                  case Never:
               }
            });
            BlockIterator.after(() -> {
               BlockPos near = null;
               double dst = Double.MAX_VALUE;

               for (GotoPlusCommand.Cross crossxx : this.crosses) {
                  BlockState ground = mc.level.getBlockState(new BlockPos(crossxx.x, crossxx.y - 1, crossxx.z));
                  Block ground_block = ground.getBlock();
                  if (!(ground_block instanceof LeavesBlock) && ground_block != Blocks.CHORUS_FLOWER) {
                     double dist = mc.player.distanceToSqr(new Vec3(crossxx.x, crossxx.y, crossxx.z));
                     if (dist < dst) {
                        dst = dist;
                        near = new BlockPos(crossxx.x, crossxx.y, crossxx.z);
                     }
                  }
               }

               for (GotoPlusCommand.Cross crossx : this.crosses) {
                  this.crossPool.free(crossx);
               }

               this.crosses.clear();
               if (near != null) {
                  BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("goto " + near.getX() + " " + near.getY() + " " + near.getZ());
               } else {
                  ChatUtils.sendMsg(Component.nullToEmpty("Not founded near light"));
               }
            });
         });
         return 1;
      }));
   }

   private class Cross {
      private int x;
      private int y;
      private int z;

      private Cross() {
         Objects.requireNonNull(GotoPlusCommand.this);
         super();
      }

      public GotoPlusCommand.Cross set(BlockPos blockPos) {
         this.x = blockPos.getX();
         this.y = blockPos.getY();
         this.z = blockPos.getZ();
         return this;
      }
   }

   public static enum Spawn {
      Never,
      Potential,
      Always;
   }
}
