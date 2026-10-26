package zgoly.meteorist.modules.slotclick.selections;

import java.util.Arrays;
import java.util.List;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.MenuType;
import zgoly.meteorist.settings.StringPairSetting;

public class DefaultSlotSelection extends BaseSlotSelection {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<Boolean> checkContainerType = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("check-container-type")).description("Checks the container type.")).defaultValue(false)).build());
   public final Setting<List<MenuType<?>>> containerType = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.ScreenHandlerListSetting.Builder)((meteordevelopment.meteorclient.settings.ScreenHandlerListSetting.Builder)((meteordevelopment.meteorclient.settings.ScreenHandlerListSetting.Builder)((meteordevelopment.meteorclient.settings.ScreenHandlerListSetting.Builder)new meteordevelopment.meteorclient.settings.ScreenHandlerListSetting.Builder()
                        .name("container-type"))
                     .description("Determines the type of containers that can be interacted with."))
                  .defaultValue(Arrays.asList(MenuType.GENERIC_9x3, MenuType.GENERIC_9x6)))
               .visible(this.checkContainerType::get))
            .build()
      );
   public final Setting<DefaultSlotSelection.ContainerTypeMode> containerTypeMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("container-type-mode"))
                     .description("Determines how the container types are handled."))
                  .defaultValue(DefaultSlotSelection.ContainerTypeMode.Whitelist))
               .visible(this.checkContainerType::get))
            .build()
      );
   public final Setting<Boolean> ignoreMenuTypeMismatch = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("ignore-menu-type-mismatch")).description("Ignores menu type mismatches."))
                  .defaultValue(true))
               .visible(this.checkContainerType::get))
            .build()
      );
   public final Setting<Boolean> checkScreenName = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("check-screen-name")).description("Checks the screen name.")).defaultValue(false)).build());
   public final Setting<String> screenName = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                     .name("screen-name"))
                  .description("Determines the name of the screen. Uses Regular Expressions (RegEx)."))
               .visible(this.checkScreenName::get))
            .build()
      );
   public final Setting<Boolean> checkSlotItemData = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("check-slot-item")).description("Checks the slot item.")).defaultValue(false)).build());
   public final Setting<List<Tuple<String, String>>> slotItemData = this.sgGeneral
      .add(
         ((StringPairSetting.Builder)((StringPairSetting.Builder)((StringPairSetting.Builder)new StringPairSetting.Builder().name("slot-item-data"))
                  .description("Checks data of the slot item."))
               .placeholder(new Tuple("Path", "RegEx"))
               .defaultValue(List.of(new Tuple("", "")))
               .visible(this.checkSlotItemData::get))
            .build()
      );
   public final Setting<DefaultSlotSelection.SlotItemMatchMode> slotItemMatchMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("slot-item-match-mode"))
                     .description("Determines how the slot item data should be matched."))
                  .defaultValue(DefaultSlotSelection.SlotItemMatchMode.All))
               .visible(this.checkSlotItemData::get))
            .build()
      );

   public static enum ContainerTypeMode {
      Whitelist,
      Blacklist;
   }

   public static enum SlotItemMatchMode {
      All,
      Any;
   }
}
