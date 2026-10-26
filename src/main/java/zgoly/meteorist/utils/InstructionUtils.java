package zgoly.meteorist.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.meteordev.starscript.Script;
import zgoly.meteorist.modules.instructions.InstructionFactory;
import zgoly.meteorist.modules.instructions.instructions.BaseInstruction;
import zgoly.meteorist.modules.instructions.instructions.CommandInstruction;
import zgoly.meteorist.modules.instructions.instructions.DelayInstruction;

public class InstructionUtils {
   public static List<BaseInstruction> readInstructionsFromTag(CompoundTag tag, InstructionFactory factory) {
      List<BaseInstruction> instructions = new ArrayList<>();

      for (Tag tagII : tag.getListOrEmpty("instructions")) {
         CompoundTag tagI = (CompoundTag)tagII;
         String type = tagI.getStringOr("type", "");
         BaseInstruction instruction = factory.createInstruction(type);
         if (instruction != null) {
            CompoundTag instructionTag = (CompoundTag)tagI.get("instruction");
            if (instructionTag != null) {
               instruction.fromTag(instructionTag);
            }

            instructions.add(instruction);
         }
      }

      return instructions;
   }

   public static int processInstructions(List<BaseInstruction> instructions, Map<Integer, List<String>> map) {
      return processInstructions(instructions, map, 0);
   }

   public static int processInstructions(List<BaseInstruction> instructions, Map<Integer, List<String>> map, int tick) {
      for (BaseInstruction instruction : instructions) {
         if (instruction instanceof DelayInstruction delayInstruction) {
            tick += delayInstruction.delay.get();
         } else if (instruction instanceof CommandInstruction commandInstruction) {
            for (int i = 0; i < commandInstruction.runCount.get(); i++) {
               map.computeIfAbsent(tick, ArrayList::new).add((String)commandInstruction.command.get());
               if (i < (Integer)commandInstruction.runCount.get() - 1) {
                  tick += commandInstruction.delayBetweenRuns.get();
               }
            }
         }
      }

      return tick;
   }

   public static void executeCommands(Map<Integer, List<String>> map, int currentTick) {
      executeCommands(map, currentTick, 0);
   }

   public static void executeCommands(Map<Integer, List<String>> map, int currentTick, int startTick) {
      for (Entry<Integer, List<String>> entry : new ArrayList<>(map.entrySet())) {
         if (startTick + entry.getKey() == currentTick) {
            for (String command : entry.getValue()) {
               Script script = MeteorStarscript.compile(command);
               if (script != null) {
                  ChatUtils.sendPlayerMsg(MeteorStarscript.run(script));
               }
            }
         }
      }
   }
}
