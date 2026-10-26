package fr.crazycat256.satellite.modules;

import com.mojang.serialization.DataResult;
import fr.crazycat256.satellite.Addon;
import fr.crazycat256.satellite.utils.NBTUtils;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;

public class NBTTooltip extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> indentationLevel = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("indentation-level")).description("The level of indentation for NBT data."))
               .min(0)
               .sliderMax(4)
               .defaultValue(2))
            .build()
      );
   private final Setting<Boolean> color = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("color"))
                  .description("Colorize NBT data."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> onlyOnKey = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("only-on-key"))
                  .description("Only show NBT data when CTRL is held."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Keybind> displayKey = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)new meteordevelopment.meteorclient.settings.KeybindSetting.Builder()
                        .name("display-key"))
                     .description("The key to press to display the nbt tooltip."))
                  .defaultValue(Keybind.fromKey(341)))
               .visible(this.onlyOnKey::get))
            .build()
      );
   private final Setting<Keybind> copyKey = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)((meteordevelopment.meteorclient.settings.KeybindSetting.Builder)new meteordevelopment.meteorclient.settings.KeybindSetting.Builder()
                     .name("copy-key"))
                  .description("The key to press to copy the NBT into the clipboard."))
               .defaultValue(Keybind.fromKeys(67, 2)))
            .build()
      );

   public NBTTooltip() {
      super(Addon.CATEGORY, "nbt-tooltip", "Shows NBT data in item tooltips.");
   }

   public String getTooltip(ItemStack stack, TooltipContext ctx) {
      if (this.isActive() && (!(Boolean)this.onlyOnKey.get() || ((Keybind)this.displayKey.get()).isPressed())) {
         DataResult<Tag> result = DataComponentPatch.CODEC
            .encodeStart(ctx.registries().createSerializationContext(NbtOps.INSTANCE), stack.getComponentsPatch());
         result.ifError(e -> {});
         Tag nbtElement = (Tag)result.getOrThrow();
         CompoundTag compound = (CompoundTag)nbtElement;
         if (((Keybind)this.copyKey.get()).isPressed()) {
            this.mc.keyboardHandler.setClipboard(NBTUtils.formatNBT(compound, (Integer)this.indentationLevel.get(), false));
         }

         return NBTUtils.formatNBT(compound, (Integer)this.indentationLevel.get(), (Boolean)this.color.get());
      } else {
         return null;
      }
   }
}
