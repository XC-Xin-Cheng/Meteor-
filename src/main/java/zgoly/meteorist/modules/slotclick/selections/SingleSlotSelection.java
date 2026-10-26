package zgoly.meteorist.modules.slotclick.selections;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import net.minecraft.world.inventory.ContainerInput;

public class SingleSlotSelection extends DefaultSlotSelection {
   public static final String type = "Single";
   public final SettingGroup sgSingleSlotSelection = this.settings.createGroup("Single Slot Selection");
   public final Setting<Integer> slot = this.sgSingleSlotSelection
      .add(((Builder)((Builder)((Builder)new Builder().name("slot")).description("Slot to select.")).min(0).onChanged(var0 -> reloadParent())).build());
   public final Setting<ContainerInput> action = this.sgSingleSlotSelection
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("action"))
                  .description("Action to perform."))
               .defaultValue(ContainerInput.PICKUP))
            .build()
      );
   public final Setting<Integer> button = this.sgSingleSlotSelection
      .add(((Builder)((Builder)new Builder().name("button")).description("Button to press.")).build());

   @Override
   public String getTypeName() {
      return "Single";
   }

   public SingleSlotSelection copy() {
      return (SingleSlotSelection)new SingleSlotSelection().fromTag(this.toTag());
   }
}
