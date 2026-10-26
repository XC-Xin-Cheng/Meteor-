package nekiplay.meteorplus.mixin.meteorclient.modules;

import java.util.concurrent.atomic.AtomicReference;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.WaypointsModule;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import nekiplay.meteorplus.mixinclasses.EditWaypointScreen;
import nekiplay.meteorplus.mixinclasses.WIcon;
import nekiplay.meteorplus.mixinclasses.WaypointsModuleModes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({WaypointsModule.class})
public class WaypointsModuleMixin extends Module {
   @Unique
   public final AtomicReference<GuiTheme> themeRef = new AtomicReference<>();
   @Unique
   public final AtomicReference<WTable> tableRef = new AtomicReference<>();
   @Unique
   private final SettingGroup meteorPlusTab = this.settings.createGroup("Meteor+");
   @Unique
   private final Setting<Boolean> showDistance = this.meteorPlusTab
      .add(((Builder)((Builder)((Builder)((Builder)new Builder().name("show-distance")).description("Show distance in this gui.")).onChanged(a -> {
         GuiTheme t = this.themeRef.get();
         WTable tab = this.tableRef.get();
         if (t != null && tab != null) {
            this.initTable(t, tab);
         }
      })).defaultValue(true)).build());
   @Unique
   private final Setting<Boolean> showCompactDistance = this.meteorPlusTab
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("show-compact-distance")).description("Show compact distance in this gui."))
                     .onChanged(a -> {
                        GuiTheme t = this.themeRef.get();
                        WTable tab = this.tableRef.get();
                        if (t != null && tab != null) {
                           this.initTable(t, tab);
                        }
                     }))
                  .visible(this.showDistance::get))
               .defaultValue(true))
            .build()
      );
   @Unique
   private final Setting<WaypointsModuleModes.SortMode> sortMode = this.meteorPlusTab
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("sort-mode"))
                     .description("Sorting waypoints mode."))
                  .defaultValue(WaypointsModuleModes.SortMode.Distance))
               .onChanged(a -> {
                  GuiTheme t = this.themeRef.get();
                  WTable tab = this.tableRef.get();
                  if (t != null && tab != null) {
                     this.initTable(t, tab);
                  }
               }))
            .build()
      );
   @Unique
   private final Setting<String> search = this.meteorPlusTab
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("search"))
                     .description("Search waypoint by text"))
                  .defaultValue(""))
               .onChanged(a -> {
                  GuiTheme t = this.themeRef.get();
                  WTable tab = this.tableRef.get();
                  if (t != null && tab != null) {
                     this.initTable(t, tab);
                  }
               }))
            .build()
      );

   public WaypointsModuleMixin(Category category, String name, String description) {
      super(category, name, description);
   }

   @Inject(
      method = {"getWidget"},
      at = {@At("HEAD")},
      remap = false,
      cancellable = true
   )
   private void getWidget(GuiTheme theme, CallbackInfoReturnable<WWidget> cir) {
      if (!Utils.canUpdate()) {
         cir.setReturnValue(theme.label("You need to be in a world."));
      }

      WTable table = theme.table();
      this.initTable(theme, table);
      this.themeRef.set(theme);
      this.tableRef.set(table);
      cir.setReturnValue(table);
   }

   @Unique
   private void initTable(GuiTheme theme, WTable table) {
      table.clear();

      for (Waypoint waypoint : Waypoints.get()) {
         boolean validDim = Waypoints.checkDimension(waypoint);
         table.add(new WIcon(waypoint));
         WLabel name = (WLabel)table.add(theme.label((String)waypoint.name.get())).expandCellX().widget();
         if (!validDim) {
            name.color = Color.GRAY;
         }

         WCheckbox visible = (WCheckbox)table.add(theme.checkbox((Boolean)waypoint.visible.get())).widget();
         visible.action = () -> {
            waypoint.visible.set(visible.checked);
            Waypoints.get().save();
         };
         WButton edit = (WButton)table.add(theme.button(GuiRenderer.EDIT)).widget();
         edit.action = () -> this.mc.setScreen(new EditWaypointScreen(theme, waypoint, () -> this.initTable(theme, table)));
         if (validDim) {
            WButton gotoB = (WButton)table.add(theme.button("Goto")).widget();
            gotoB.action = () -> {
               if (PathManagers.get().isPathing()) {
                  PathManagers.get().stop();
               }

               PathManagers.get().moveTo(waypoint.getPos());
            };
         }

         WMinus remove = (WMinus)table.add(theme.minus()).widget();
         remove.action = () -> {
            Waypoints.get().remove(waypoint);
            this.initTable(theme, table);
         };
         table.row();
      }

      table.add(theme.horizontalSeparator()).expandX();
      table.row();
      WButton create = (WButton)table.add(theme.button("Create")).expandX().widget();
      create.action = () -> {
         if (Utils.canUpdate()) {
            this.mc.setScreen(new EditWaypointScreen(theme, null, () -> this.initTable(theme, table)));
         }
      };
   }
}
