package nekiplay.meteorplus.features.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class BlockRawIdCommand extends Command {
   public BlockRawIdCommand() {
      super("rawblockid", "Get raw block id under mouse", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.executes(context -> {
         if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK) {
            BlockPos pos = ((BlockHitResult)mc.hitResult).getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            int raw_id = Block.getId(state);
            this.info(String.valueOf(raw_id), new Object[0]);
         }

         return 1;
      });
   }
}
