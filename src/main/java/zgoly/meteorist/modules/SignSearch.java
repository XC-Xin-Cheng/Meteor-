package zgoly.meteorist.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.utils.misc.DebugLogger;

public class SignSearch extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<SignSearch.SearchSide> searchSide = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("search-side")).description("Which side(s) of the sign to search."))
               .defaultValue(SignSearch.SearchSide.Both))
            .build()
      );
   private final Setting<Boolean> showResults = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("show-results"))
                  .description("Show found signs in GUI."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> updateResultsEveryTick = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("update-every-tick"))
                  .description("Re-run the search every tick (useful for dynamic sign changes). May impact performance."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> renderSigns = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("render-signs"))
                  .description("Render found signs in world."))
               .defaultValue(true))
            .build()
      );
   private final Setting<SettingColor> sideColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("side-color"))
                  .description("Color of the side of the sign render box."))
               .defaultValue(new SettingColor(255, 255, 0, 50))
               .visible(this.renderSigns::get))
            .build()
      );
   private final Setting<SettingColor> lineColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                     .name("line-color"))
                  .description("Color of the line of the sign render box."))
               .defaultValue(new SettingColor(255, 255, 0, 255))
               .visible(this.renderSigns::get))
            .build()
      );
   private final Setting<ShapeMode> shapeMode = this.sgRender
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("shape-mode")).description("How to render the sign boxes.")).defaultValue(ShapeMode.Both))
               .visible(this.renderSigns::get))
            .build()
      );
   private final List<SignBlockEntity> foundSigns = new ArrayList<>();
   private String searchBarText = "";
   private GuiTheme currentTheme;
   private WVerticalList currentResultsList;
   private final DebugLogger debugLogger = new DebugLogger(this, this.settings);

   public SignSearch() {
      super(Meteorist.CATEGORY, "sign-search", "Searches sign text using regex and highlights matching signs.");
   }

   public WWidget getWidget(GuiTheme theme) {
      this.currentTheme = theme;
      WVerticalList list = theme.verticalList();
      WTextBox searchBar = (WTextBox)list.add(theme.textBox(this.searchBarText, "Enter your search query. Regex is supported.")).expandX().widget();
      this.currentResultsList = theme.verticalList();
      list.add(this.currentResultsList).expandX();
      searchBar.action = () -> {
         this.searchBarText = searchBar.get();
         this.refreshGui();
      };
      this.refreshGui();
      return list;
   }

   private void refreshGui() {
      if (this.currentTheme != null && this.currentResultsList != null) {
         this.currentResultsList.clear();
         this.performSearch();
         WTable table = (WTable)this.currentResultsList.add(this.currentTheme.table()).expandX().widget();
         table.horizontalSpacing = 12.0;
         if ((Boolean)this.showResults.get()) {
            if (!this.foundSigns.isEmpty()) {
               table.add(this.currentTheme.label("Item")).expandX();
               table.add(this.currentTheme.label("Name")).expandX();
               table.add(this.currentTheme.label("Pos")).expandX();
               table.add(this.currentTheme.label("Front Text")).expandX();
               table.add(this.currentTheme.label("Back Text")).expandX();
               table.add(this.currentTheme.label("Goto")).expandX();
               table.add(this.currentTheme.label("Look at")).expandX();
            }

            for (SignBlockEntity signBlockEntity : this.foundSigns) {
               table.row();
               BlockPos pos = signBlockEntity.getBlockPos();
               Block sign = this.mc.level.getBlockState(pos).getBlock();
               Item signItem = sign.asItem();
               table.add(this.currentTheme.item(signItem.getDefaultInstance())).expandX();
               table.add(this.currentTheme.label(sign.getName().getString())).expandX();
               table.add(this.currentTheme.label(String.format(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()))).expandX();
               WVerticalList frontTextList = (WVerticalList)table.add(this.currentTheme.verticalList()).expandX().widget();

               for (Component message : signBlockEntity.getFrontText().getMessages(false)) {
                  frontTextList.add(this.currentTheme.label(message.getString())).expandX();
               }

               WVerticalList backTextList = (WVerticalList)table.add(this.currentTheme.verticalList()).expandX().widget();

               for (Component message : signBlockEntity.getBackText().getMessages(false)) {
                  backTextList.add(this.currentTheme.label(message.getString())).expandX();
               }

               WButton gotoBtn = (WButton)table.add(this.currentTheme.button("Goto")).expandX().widget();
               gotoBtn.action = () -> PathManagers.get().moveTo(pos);
               WButton lookAtBtn = (WButton)table.add(this.currentTheme.button("Look at")).expandX().widget();
               lookAtBtn.action = () -> {
                  if (this.mc.player != null) {
                     this.mc.player.setYRot((float)Rotations.getYaw(pos));
                     this.mc.player.setXRot((float)Rotations.getPitch(pos));
                  }
               };
            }
         }
      }
   }

   private void performSearch() {
      this.foundSigns.clear();
      if (this.mc.level != null && !this.searchBarText.isEmpty()) {
         Pattern pattern;
         try {
            pattern = Pattern.compile(this.searchBarText, 2);
         } catch (PatternSyntaxException var6) {
            this.debugLogger.error(var6.getMessage());
            return;
         }

         for (BlockEntity blockEntity : Utils.blockEntities()) {
            if (blockEntity instanceof SignBlockEntity sign) {
               boolean matches = false;
               switch ((SignSearch.SearchSide)this.searchSide.get()) {
                  case Front:
                     matches = this.matchesText(pattern, sign.getFrontText());
                     break;
                  case Back:
                     matches = this.matchesText(pattern, sign.getBackText());
                     break;
                  case Both:
                     matches = this.matchesText(pattern, sign.getFrontText()) || this.matchesText(pattern, sign.getBackText());
               }

               if (matches) {
                  this.foundSigns.add(sign);
               }
            }
         }
      }
   }

   private boolean matchesText(Pattern pattern, SignText signText) {
      if (signText == null) {
         return false;
      } else {
         for (int i = 0; i < 4; i++) {
            String line = signText.getMessage(i, false).getString();
            if (pattern.matcher(line).find()) {
               return true;
            }
         }

         return false;
      }
   }

   @EventHandler
   private void onTick(Post event) {
      if ((Boolean)this.updateResultsEveryTick.get()) {
         this.refreshGui();
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if ((Boolean)this.renderSigns.get() && this.isActive()) {
         for (SignBlockEntity signBlockEntity : this.foundSigns) {
            event.renderer.box(signBlockEntity.getBlockPos(), (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0);
         }
      }
   }

   public String getInfoString() {
      return String.valueOf(this.foundSigns.size());
   }

   public static enum SearchSide {
      Front,
      Back,
      Both;
   }
}
