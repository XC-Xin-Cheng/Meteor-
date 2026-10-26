package com.stash.hunt.modules.searcharea;

import com.stash.hunt.Addon;
import com.stash.hunt.modules.searcharea.modes.Rectangle;
import com.stash.hunt.modules.searcharea.modes.Spiral;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;

public class SearchArea extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<SearchAreaModes> chunkLoadMode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("The mode chunks are loaded."))
                     .defaultValue(SearchAreaModes.Rectangle))
                  .onModuleActivated(chunkMode -> this.onModeChanged((SearchAreaModes)chunkMode.get())))
               .onChanged(this::onModeChanged))
            .build()
      );
   public final Setting<BlockPos> startPos = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                        .name("start-position"))
                     .description("The coordinates to start the rectangle at. Y Pos is ignored"))
                  .defaultValue(new BlockPos(0, 0, 0)))
               .visible(() -> this.chunkLoadMode.get() == SearchAreaModes.Rectangle))
            .build()
      );
   public final Setting<BlockPos> targetPos = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                        .name("end-position"))
                     .description("The coordinates to end the rectangle at. Y Pos is ignored"))
                  .defaultValue(new BlockPos(0, 0, 0)))
               .visible(() -> this.chunkLoadMode.get() == SearchAreaModes.Rectangle))
            .build()
      );
   public final Setting<Integer> rowGap = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("path-gap"))
                  .description("The amount of chunks to space between each chunk path."))
               .defaultValue(12))
            .min(1)
            .sliderRange(0, 32)
            .build()
      );
   public final Setting<String> saveLocation = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                     .name("save-name"))
                  .description("The name to use for the folder that saves data, if you leave it blank, no data will be saved."))
               .defaultValue(""))
            .build()
      );
   public final Setting<Boolean> disconnectOnCompletion = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("disconnect-on-completion"))
                     .description("Whether to disconnect after the path is complete. This will turn autoreconnect off when disconnecting."))
                  .defaultValue(false))
               .visible(() -> this.chunkLoadMode.get() == SearchAreaModes.Rectangle))
            .build()
      );
   private SearchAreaMode currentMode = new Rectangle();

   public SearchArea() {
      super(
         Addon.CATEGORY,
         "search-area",
         "Either loads chunks in a rectangle to a certain point from you, or spirals endlessly from you. Useful with Stash Finder or other map saving mods."
      );
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      WButton clear = (WButton)list.add(theme.button("Clear Currently Selected")).widget();
      clear.action = () -> this.currentMode.clear();
      WButton clearAll = (WButton)list.add(theme.button("Clear All")).widget();
      clearAll.action = () -> this.currentMode.clearAll();
      return list;
   }

   public void onActivate() {
      this.currentMode.onActivate();
   }

   public void onDeactivate() {
      this.currentMode.onDeactivate();
   }

   @EventHandler
   private void onTick(Post event) {
      this.currentMode.onTick();
   }

   private void onModeChanged(SearchAreaModes mode) {
      switch (mode) {
         case Rectangle:
            this.currentMode = new Rectangle();
            break;
         case Spiral:
            this.currentMode = new Spiral();
      }
   }

   public static enum WebhookSettings {
      Off,
      LogChat,
      LogStashes,
      LogBoth;
   }
}
