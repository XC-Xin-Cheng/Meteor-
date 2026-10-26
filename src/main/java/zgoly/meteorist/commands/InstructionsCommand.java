package zgoly.meteorist.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import zgoly.meteorist.commands.arguments.InstructionArgumentType;
import zgoly.meteorist.modules.instructions.InstructionFactory;
import zgoly.meteorist.modules.instructions.Instructions;
import zgoly.meteorist.modules.instructions.instructions.BaseInstruction;
import zgoly.meteorist.utils.InstructionUtils;
import zgoly.meteorist.utils.MeteoristUtils;
import zgoly.meteorist.utils.misc.DebugLogger;

public class InstructionsCommand extends Command {
   public Instructions instructionsModule = (Instructions)Modules.get().get(Instructions.class);
   public Map<Integer, List<String>> map = new HashMap<>();
   private final DebugLogger debugLogger;

   public InstructionsCommand() {
      super("instructions", "Runs saved instructions from the \"Instructions\" module.", new String[0]);
      MeteorClient.EVENT_BUS.subscribe(this);
      this.debugLogger = new DebugLogger(this.instructionsModule, this.instructionsModule.settings);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(
         literal("run")
            .then(
               ((RequiredArgumentBuilder)argument("instruction", InstructionArgumentType.instruction())
                     .then(argument("count", IntegerArgumentType.integer(1)).executes(context -> {
                        String instructionName = (String)context.getArgument("instruction", String.class);
                        File instructionFile = InstructionArgumentType.instruction().getInstructionFile(instructionName);
                        int count = IntegerArgumentType.getInteger(context, "count");
                        return this.handleInstruction(instructionFile, count);
                     })))
                  .executes(context -> {
                     String instructionName = (String)context.getArgument("instruction", String.class);
                     File instructionFile = InstructionArgumentType.instruction().getInstructionFile(instructionName);
                     return this.handleInstruction(instructionFile, 1);
                  })
            )
      );
      builder.then(literal("debug").executes(var1x -> {
         boolean value = this.debugLogger.isDebugEnabled();
         this.debugLogger.toggleDebug();
         this.info("Debug Info was (highlight)%s", new Object[]{value ? "disabled" : "enabled"});
         return 1;
      }));
      builder.then(literal("stop").executes(var1x -> {
         if (!this.map.isEmpty()) {
            this.info("Stopping (highlight)%d(default) instructions", new Object[]{this.map.size()});
            this.map.clear();
         } else {
            this.error("Nothing to stop", new Object[0]);
         }

         return 1;
      }));
   }

   public int handleInstruction(File file, int runs) {
      InstructionFactory factory = new InstructionFactory();

      try {
         List<BaseInstruction> instructions;
         try (InputStream inputStream = new FileInputStream(file)) {
            CompoundTag tag = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
            instructions = InstructionUtils.readInstructionsFromTag(tag, factory);
         }

         int worldTime = (int)mc.level.getGameTime() + 1;
         int var13 = worldTime;

         for (int runsMessage = 0; runsMessage < runs; runsMessage++) {
            int lastTick = InstructionUtils.processInstructions(instructions, this.map, var13);
            this.debugLogger
               .info("Iteration (highlight)%d(default) will run from (highlight)%d(default) to (highlight)%d(default) ticks", runsMessage + 1, var13, lastTick);
            var13 = lastTick;
         }

         String runsMessage = runs > 1 ? " for (highlight)" + runs + "(default) runs" : "";
         this.info("Loaded (highlight)%d(default) instructions from (highlight)%s(default)%s", new Object[]{instructions.size(), file.getName(), runsMessage});
         int totalTicks = var13 - worldTime;
         this.info(
            "Total ticks to run: (highlight)%d(default) (approximately %s)", new Object[]{totalTicks, MeteoristUtils.ticksToTime(totalTicks, false, true)}
         );
      } catch (Exception var11) {
         this.error("Error loading instructions: (highlight)%d(default)", new Object[]{var11.getMessage()});
      }

      return 1;
   }

   @EventHandler
   public void onTick(Post event) {
      if (mc.level != null) {
         int worldTime = (int)mc.level.getGameTime();
         if (!this.map.isEmpty()) {
            this.debugLogger.info("Running (highlight)%d(default) instructions", this.map.size());
         }

         this.map.entrySet().removeIf(entry -> entry.getKey() < worldTime);
         InstructionUtils.executeCommands(this.map, worldTime);
      }
   }

   @EventHandler
   private void onGameLeft(GameLeftEvent event) {
      this.map.clear();
   }
}
