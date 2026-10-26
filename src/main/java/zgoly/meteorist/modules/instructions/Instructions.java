package zgoly.meteorist.modules.instructions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.modules.instructions.instructions.BaseInstruction;
import zgoly.meteorist.modules.instructions.instructions.CommandInstruction;
import zgoly.meteorist.modules.instructions.instructions.DelayInstruction;
import zgoly.meteorist.utils.InstructionUtils;
import zgoly.meteorist.utils.config.MeteoristConfigManager;

public class Instructions extends Module {
   public static List<BaseInstruction> instructions = new ArrayList<>();
   private final InstructionFactory factory = new InstructionFactory();
   private int startTick = -1;

   public Instructions() {
      super(Meteorist.CATEGORY, "instructions", "Runs commands with different delays and number of cycles. Supports Starscript.");
   }

   public CompoundTag toTag() {
      CompoundTag tag = super.toTag();
      ListTag list = new ListTag();

      for (BaseInstruction instruction : instructions) {
         CompoundTag mTag = new CompoundTag();
         mTag.putString("type", instruction.getTypeName());
         mTag.put("instruction", instruction.toTag());
         list.add(mTag);
      }

      tag.put("instructions", list);
      return tag;
   }

   public Module fromTag(CompoundTag tag) {
      super.fromTag(tag);
      instructions.clear();
      instructions = InstructionUtils.readInstructionsFromTag(tag, this.factory);
      return this;
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      this.fillWidget(theme, list);
      return list;
   }

   private void fillWidget(GuiTheme theme, WVerticalList list) {
      list.clear();

      for (BaseInstruction instruction : instructions) {
         list.add(theme.settings(instruction.settings)).expandX();
         WContainer container = (WContainer)list.add(theme.horizontalList()).expandX().widget();
         if (instructions.size() > 1) {
            WContainer moveContainer = (WContainer)container.add(theme.horizontalList()).expandX().widget();
            int index = instructions.indexOf(instruction);
            if (index > 0) {
               WButton moveUp = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_UP)).widget();
               moveUp.tooltip = "Move instruction up.";
               moveUp.action = () -> {
                  instructions.remove(index);
                  instructions.add(index - 1, instruction);
                  this.fillWidget(theme, list);
               };
            }

            if (index < instructions.size() - 1) {
               WButton moveDown = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_DOWN)).widget();
               moveDown.tooltip = "Move instruction down.";
               moveDown.action = () -> {
                  instructions.remove(index);
                  instructions.add(index + 1, instruction);
                  this.fillWidget(theme, list);
               };
            }
         }

         WButton copy = (WButton)container.add(theme.button(GuiRenderer.COPY)).widget();
         copy.tooltip = "Duplicate instruction.";
         copy.action = () -> {
            instructions.add(instructions.indexOf(instruction), instruction.copy());
            this.fillWidget(theme, list);
         };
         WMinus remove = (WMinus)container.add(theme.minus()).widget();
         remove.tooltip = "Remove instruction.";
         remove.action = () -> {
            instructions.remove(instruction);
            this.fillWidget(theme, list);
         };
      }

      if (!instructions.isEmpty()) {
         list.add(theme.horizontalSeparator()).expandX();
      }

      WTable controls = (WTable)list.add(theme.table()).expandX().widget();
      WButton createCommand = (WButton)controls.add(theme.button("New Command")).expandX().widget();
      createCommand.action = () -> {
         CommandInstruction instruction = new CommandInstruction();
         instructions.add(instruction);
         this.fillWidget(theme, list);
      };
      WButton createDelay = (WButton)controls.add(theme.button("New Delay")).expandX().widget();
      createDelay.action = () -> {
         DelayInstruction instruction = new DelayInstruction();
         instructions.add(instruction);
         this.fillWidget(theme, list);
      };
      WButton removeAll = (WButton)controls.add(theme.button("Remove All Instructions")).expandX().widget();
      removeAll.action = () -> {
         instructions.clear();
         this.fillWidget(theme, list);
      };
      MeteoristConfigManager.configManager(theme, list, this);
   }

   public void onActivate() {
      this.startTick = -1;
   }

   @EventHandler
   public void onTick(Post event) {
      int currentTick = (int)this.mc.level.getGameTime();
      if (this.startTick == -1) {
         this.startTick = currentTick;
      }

      Map<Integer, List<String>> map = new HashMap<>();
      int lastTick = InstructionUtils.processInstructions(instructions, map);
      InstructionUtils.executeCommands(map, currentTick, this.startTick);
      if (this.startTick + lastTick <= currentTick) {
         this.startTick = -1;
      }
   }
}
