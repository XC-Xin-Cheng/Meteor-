package zgoly.meteorist.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class DataCommand extends Command {
   public DataCommand() {
      super("data", "Gets NBT data of entities or blocks.", new String[]{"target-nbt"});
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.executes(var1x -> this.getDataOrStates(false));
      this.buildCommand(builder, "get", false);
      this.buildCommand(builder, "copy", true);
   }

   private void buildCommand(LiteralArgumentBuilder<ClientSuggestionProvider> builder, String commandName, boolean copy) {
      builder.then(
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)literal(commandName).executes(var2x -> this.getDataOrStates(copy)))
               .then(
                  ((LiteralArgumentBuilder)literal("player").executes(var2x -> this.getEntityData(mc.player, copy)))
                     .then(argument("player", PlayerArgumentType.create()).executes(context -> this.getEntityData(PlayerArgumentType.get(context), copy)))
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)literal("target").executes(var2x -> this.getDataOrStates(copy)))
                     .then(literal("data").executes(var2x -> this.getFullData(copy))))
                  .then(literal("states").executes(var2x -> this.getFullStates(copy)))
            )
      );
   }

   public int getEntityData(Entity entity, boolean copy) {
      CompoundTag nbt = NbtPredicate.getEntityTagToCompare(entity);
      if (copy) {
         mc.keyboardHandler.setClipboard(nbt.asString().orElse(""));
         this.info("Entity data was copied to your clipboard", new Object[0]);
      } else {
         this.info(Component.literal("Entity data: ").append(NbtUtils.toPrettyComponent(nbt)));
      }

      return 1;
   }

   public int getDataOrStates(boolean copy) {
      DataCommand.WarningType dataWarning = this.getData(copy);
      DataCommand.WarningType statesWarning = this.getStates(copy);
      if (dataWarning != DataCommand.WarningType.NO_WARNING && statesWarning != DataCommand.WarningType.NO_WARNING) {
         this.warningMessage(DataCommand.WarningType.NO_TARGET);
      }

      return 1;
   }

   public int getFullData(boolean copy) {
      this.warningMessage(this.getData(copy));
      return 1;
   }

   public int getFullStates(boolean copy) {
      this.warningMessage(this.getStates(copy));
      return 1;
   }

   public DataCommand.WarningType getData(boolean copy) {
      if (mc.hitResult.getType() == Type.ENTITY) {
         this.getEntityData(((EntityHitResult)mc.hitResult).getEntity(), copy);
      } else {
         if (mc.hitResult.getType() != Type.BLOCK) {
            return DataCommand.WarningType.NO_TARGET;
         }

         BlockPos blockPos = ((BlockHitResult)mc.hitResult).getBlockPos();
         BlockEntity blockEntity = mc.level.getBlockEntity(blockPos);
         if (blockEntity == null) {
            return DataCommand.WarningType.NOT_A_BLOCK_ENTITY;
         }

         CompoundTag nbt = blockEntity.saveWithFullMetadata(mc.level.registryAccess());
         if (copy) {
            mc.keyboardHandler.setClipboard(nbt.asString().orElse(""));
            this.info("Block data was copied to your clipboard", new Object[0]);
         } else {
            this.info(Component.literal("Block data: ").append(NbtUtils.toPrettyComponent(nbt)));
         }
      }

      return DataCommand.WarningType.NO_WARNING;
   }

   public DataCommand.WarningType getStates(boolean copy) {
      if (mc.hitResult.getType() == Type.BLOCK) {
         BlockPos blockPos = ((BlockHitResult)mc.hitResult).getBlockPos();
         BlockState blockState = mc.level.getBlockState(blockPos);
         CompoundTag nbt = NbtUtils.writeBlockState(blockState);
         if (copy) {
            mc.keyboardHandler.setClipboard(nbt.asString().orElse(""));
            this.info("Block states were copied to your clipboard", new Object[0]);
         } else {
            this.info(Component.literal("Block states: ").append(NbtUtils.toPrettyComponent(nbt)));
         }

         return DataCommand.WarningType.NO_WARNING;
      } else {
         return mc.hitResult.getType() == Type.ENTITY ? DataCommand.WarningType.NOT_A_BLOCK : DataCommand.WarningType.NO_TARGET;
      }
   }

   private void warningMessage(DataCommand.WarningType warning) {
      switch (warning) {
         case NO_WARNING:
         default:
            break;
         case NOT_A_BLOCK_ENTITY:
            this.warning("Target block is not a block entity", new Object[0]);
            break;
         case NOT_A_BLOCK:
            this.warning("Target is not a block", new Object[0]);
            break;
         case NO_TARGET:
            this.warning("There is no target for your cursor", new Object[0]);
      }
   }

   public static enum WarningType {
      NO_WARNING,
      NOT_A_BLOCK_ENTITY,
      NOT_A_BLOCK,
      NO_TARGET;
   }
}
