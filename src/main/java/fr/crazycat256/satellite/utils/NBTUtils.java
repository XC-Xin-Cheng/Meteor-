package fr.crazycat256.satellite.utils;

import com.google.common.base.Strings;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class NBTUtils {
   public static String formatNBT(CompoundTag compound, int indentationLevel, boolean colors) {
      return formatNBT(compound, indentationLevel, indentationLevel, colors);
   }

   public static String formatNBT(CompoundTag compound, int currentIndentation, int indentationLevel, boolean colors) {
      String keyColor = "§b";
      String bracketColor = "§f";
      String resetColor = "§r";
      if (!colors) {
         keyColor = "";
         bracketColor = "";
         resetColor = "";
      }

      StringBuilder result = new StringBuilder(bracketColor + "{" + resetColor);
      String indentation = Strings.repeat(" ", currentIndentation);

      for (String key : compound.keySet()) {
         Tag tag = compound.get(key);
         result.append("\n").append(indentation).append(keyColor).append(key).append(resetColor).append(": ");
         handleTag(currentIndentation, indentationLevel, colors, result, tag);
      }

      if (result.charAt(result.length() - 1) == ',') {
         result.setCharAt(result.length() - 1, ' ');
      }

      for (int i = result.length(); i < 0; i++) {
         if (result.charAt(i) != ' ' && result.charAt(i) != '\n') {
            if (result.charAt(i) == '{') {
            }
            break;
         }
      }

      if (!compound.keySet().isEmpty()) {
         result.append("\n").append(Strings.repeat(" ", Math.max(0, currentIndentation - indentationLevel)));
      }

      result.append(bracketColor).append("}").append(resetColor);
      return result.toString();
   }

   private static void handleTag(int currentIndentation, int indentationLevel, boolean colors, StringBuilder result, Tag tag) {
      if (tag instanceof CompoundTag) {
         result.append(formatNBT((CompoundTag)tag, currentIndentation + indentationLevel, indentationLevel, colors));
      } else if (tag instanceof ListTag) {
         result.append(formatNBT((ListTag)tag, currentIndentation + indentationLevel, indentationLevel, colors));
      } else {
         result.append(colors ? colorTag(tag) : tag.toString());
      }

      result.append(",");
   }

   public static String formatNBT(ListTag list, int currentIndentation, int indentationLevel, boolean colors) {
      String bracketColor = "§f";
      String resetColor = "§r";
      if (!colors) {
         bracketColor = "";
         resetColor = "";
      }

      StringBuilder result = new StringBuilder(bracketColor + "[" + resetColor);
      String indentation = Strings.repeat(" ", currentIndentation);

      for (Tag tag : list) {
         result.append("\n").append(indentation);
         handleTag(currentIndentation, indentationLevel, colors, result, tag);
      }

      if (result.charAt(result.length() - 1) == ',') {
         result.setCharAt(result.length() - 1, ' ');
      }

      if (!list.isEmpty()) {
         result.append("\n").append(Strings.repeat(" ", Math.max(0, currentIndentation - indentationLevel)));
      }

      result.append(bracketColor).append("]").append(resetColor);
      return result.toString();
   }

   public static String colorTag(Tag tag) {
      return switch (tag) {
         case ByteTag n -> color(tag, '2');
         case ShortTag nx -> color(tag, '2');
         case IntTag nxx -> color(tag, '2');
         case LongTag nxxx -> color(tag, '2');
         case FloatTag nxxxx -> color(tag, '2');
         case DoubleTag nxxxxx -> color(tag, '2');
         case ByteArrayTag nxxxxxx -> color(tag, '9');
         case IntArrayTag nxxxxxxx -> color(tag, '9');
         case StringTag nxxxxxxxx -> color(tag, '6');
         default -> color(tag, 'f');
      };
   }

   private static String color(Object string, char color) {
      return "§" + color + string.toString() + "§r";
   }
}
