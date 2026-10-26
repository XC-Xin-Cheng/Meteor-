package anticope.rejects.gui.themes.rounded;

import anticope.rejects.gui.themes.rounded.widgets.WMeteorAccount;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorHorizontalSeparator;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorLabel;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorModule;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorMultiLabel;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorQuad;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorSection;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorTooltip;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorTopBar;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorVerticalSeparator;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorView;
import anticope.rejects.gui.themes.rounded.widgets.WMeteorWindow;
import anticope.rejects.gui.themes.rounded.widgets.input.WMeteorDropdown;
import anticope.rejects.gui.themes.rounded.widgets.input.WMeteorSlider;
import anticope.rejects.gui.themes.rounded.widgets.input.WMeteorTextBox;
import anticope.rejects.gui.themes.rounded.widgets.pressable.WMeteorButton;
import anticope.rejects.gui.themes.rounded.widgets.pressable.WMeteorCheckbox;
import anticope.rejects.gui.themes.rounded.widgets.pressable.WMeteorFavorite;
import anticope.rejects.gui.themes.rounded.widgets.pressable.WMeteorMinus;
import anticope.rejects.gui.themes.rounded.widgets.pressable.WMeteorPlus;
import anticope.rejects.gui.themes.rounded.widgets.pressable.WMeteorTriangle;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.WAccount;
import meteordevelopment.meteorclient.gui.widgets.WHorizontalSeparator;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WQuad;
import meteordevelopment.meteorclient.gui.widgets.WTooltip;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.gui.widgets.WVerticalSeparator;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox.Renderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WConfirmedButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WConfirmedMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class MeteorRoundedGuiTheme extends GuiTheme {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgColors = this.settings.createGroup("Colors");
   private final SettingGroup sgTextColors = this.settings.createGroup("Text");
   private final SettingGroup sgBackgroundColors = this.settings.createGroup("Background");
   private final SettingGroup sgOutline = this.settings.createGroup("Outline");
   private final SettingGroup sgSeparator = this.settings.createGroup("Separator");
   private final SettingGroup sgScrollbar = this.settings.createGroup("Scrollbar");
   private final SettingGroup sgSlider = this.settings.createGroup("Slider");
   private final SettingGroup sgStarscript = this.settings.createGroup("Starscript");
   public final Setting<Double> scale = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("scale")).description("Scale of the GUI."))
               .defaultValue(1.0)
               .min(0.75)
               .sliderMin(0.75)
               .sliderMax(4.0)
               .onSliderRelease()
               .onChanged(aDouble -> {
                  if (MeteorClient.mc.screen instanceof WidgetScreen) {
                     ((WidgetScreen)MeteorClient.mc.screen).invalidate();
                  }
               }))
            .build()
      );
   public final Setting<AlignmentX> moduleAlignment = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("module-alignment"))
                  .description("How module titles are aligned."))
               .defaultValue(AlignmentX.Center))
            .build()
      );
   public final Setting<Boolean> categoryIcons = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("category-icons"))
                  .description("Adds item icons to module categories."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> hideHUD = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("hide-HUD"))
                     .description("Hide HUD when in GUI."))
                  .defaultValue(false))
               .onChanged(v -> {
                  if (MeteorClient.mc.screen instanceof WidgetScreen) {
                     MeteorClient.mc.options.hideGui = v;
                  }
               }))
            .build()
      );
   public final Setting<Integer> round = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("round"))
                  .description("How much windows should be rounded"))
               .defaultValue(0))
            .min(0)
            .max(20)
            .sliderMin(0)
            .sliderMax(15)
            .build()
      );
   public final Setting<SettingColor> accentColor = this.color("accent", "Main color of the GUI.", new SettingColor(135, 0, 255));
   public final Setting<SettingColor> checkboxColor = this.color("checkbox", "Color of checkbox.", new SettingColor(135, 0, 255));
   public final Setting<SettingColor> plusColor = this.color("plus", "Color of plus button.", new SettingColor(255, 255, 255));
   public final Setting<SettingColor> minusColor = this.color("minus", "Color of minus button.", new SettingColor(255, 255, 255));
   public final Setting<SettingColor> favoriteColor = this.color("favorite", "Color of checked favorite button.", new SettingColor(255, 255, 0));
   public final Setting<SettingColor> textColor = this.color(this.sgTextColors, "text", "Color of text.", new SettingColor(255, 255, 255));
   public final Setting<SettingColor> textSecondaryColor = this.color(
      this.sgTextColors, "text-secondary-text", "Color of secondary text.", new SettingColor(150, 150, 150)
   );
   public final Setting<SettingColor> textHighlightColor = this.color(
      this.sgTextColors, "text-highlight", "Color of text highlighting.", new SettingColor(45, 125, 245, 100)
   );
   public final Setting<SettingColor> titleTextColor = this.color(this.sgTextColors, "title-text", "Color of title text.", new SettingColor(255, 255, 255));
   public final Setting<SettingColor> loggedInColor = this.color(
      this.sgTextColors, "logged-in-text", "Color of logged in account name.", new SettingColor(45, 225, 45)
   );
   public final Setting<SettingColor> placeholderColor = this.color(
      this.sgTextColors, "placeholder", "Color of placeholder text.", new SettingColor(255, 255, 255, 20)
   );
   public final MeteorRoundedGuiTheme.ThreeStateColorSetting backgroundColor = new MeteorRoundedGuiTheme.ThreeStateColorSetting(
      this.sgBackgroundColors, "background", new SettingColor(20, 20, 20, 200), new SettingColor(30, 30, 30, 200), new SettingColor(40, 40, 40, 200)
   );
   public final Setting<SettingColor> moduleBackground = this.color(
      this.sgBackgroundColors, "module-background", "Color of module background when active.", new SettingColor(50, 50, 50)
   );
   public final MeteorRoundedGuiTheme.ThreeStateColorSetting outlineColor = new MeteorRoundedGuiTheme.ThreeStateColorSetting(
      this.sgOutline, "outline", new SettingColor(0, 0, 0), new SettingColor(10, 10, 10), new SettingColor(20, 20, 20)
   );
   public final Setting<SettingColor> separatorText = this.color(this.sgSeparator, "separator-text", "Color of separator text", new SettingColor(255, 255, 255));
   public final Setting<SettingColor> separatorCenter = this.color(
      this.sgSeparator, "separator-center", "Center color of separators.", new SettingColor(255, 255, 255)
   );
   public final Setting<SettingColor> separatorEdges = this.color(
      this.sgSeparator, "separator-edges", "Color of separator edges.", new SettingColor(225, 225, 225, 150)
   );
   public final MeteorRoundedGuiTheme.ThreeStateColorSetting scrollbarColor = new MeteorRoundedGuiTheme.ThreeStateColorSetting(
      this.sgScrollbar, "Scrollbar", new SettingColor(30, 30, 30, 200), new SettingColor(40, 40, 40, 200), new SettingColor(50, 50, 50, 200)
   );
   public final MeteorRoundedGuiTheme.ThreeStateColorSetting sliderHandle = new MeteorRoundedGuiTheme.ThreeStateColorSetting(
      this.sgSlider, "slider-handle", new SettingColor(0, 255, 180), new SettingColor(0, 240, 165), new SettingColor(0, 225, 150)
   );
   public final Setting<SettingColor> sliderLeft = this.color(this.sgSlider, "slider-left", "Color of slider left part.", new SettingColor(0, 150, 80));
   public final Setting<SettingColor> sliderRight = this.color(this.sgSlider, "slider-right", "Color of slider right part.", new SettingColor(50, 50, 50));
   private final Setting<SettingColor> starscriptText = this.color(
      this.sgStarscript, "starscript-text", "Color of text in Starscript code.", new SettingColor(169, 183, 198)
   );
   private final Setting<SettingColor> starscriptBraces = this.color(
      this.sgStarscript, "starscript-braces", "Color of braces in Starscript code.", new SettingColor(150, 150, 150)
   );
   private final Setting<SettingColor> starscriptParenthesis = this.color(
      this.sgStarscript, "starscript-parenthesis", "Color of parenthesis in Starscript code.", new SettingColor(169, 183, 198)
   );
   private final Setting<SettingColor> starscriptDots = this.color(
      this.sgStarscript, "starscript-dots", "Color of dots in starscript code.", new SettingColor(169, 183, 198)
   );
   private final Setting<SettingColor> starscriptCommas = this.color(
      this.sgStarscript, "starscript-commas", "Color of commas in starscript code.", new SettingColor(169, 183, 198)
   );
   private final Setting<SettingColor> starscriptOperators = this.color(
      this.sgStarscript, "starscript-operators", "Color of operators in Starscript code.", new SettingColor(169, 183, 198)
   );
   private final Setting<SettingColor> starscriptStrings = this.color(
      this.sgStarscript, "starscript-strings", "Color of strings in Starscript code.", new SettingColor(106, 135, 89)
   );
   private final Setting<SettingColor> starscriptNumbers = this.color(
      this.sgStarscript, "starscript-numbers", "Color of numbers in Starscript code.", new SettingColor(104, 141, 187)
   );
   private final Setting<SettingColor> starscriptKeywords = this.color(
      this.sgStarscript, "starscript-keywords", "Color of keywords in Starscript code.", new SettingColor(204, 120, 50)
   );
   private final Setting<SettingColor> starscriptAccessedObjects = this.color(
      this.sgStarscript, "starscript-accessed-objects", "Color of accessed objects (before a dot) in Starscript code.", new SettingColor(152, 118, 170)
   );

   public MeteorRoundedGuiTheme() {
      super("Meteor Rounded");
      this.settingsFactory = new DefaultSettingsWidgetFactory(this);
   }

   private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color) {
      return group.add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name(name + "-color"))
               .description(description))
            .defaultValue(color)
            .build()
      );
   }

   private Setting<SettingColor> color(String name, String description, SettingColor color) {
      return this.color(this.sgColors, name, description, color);
   }

   public WWindow window(WWidget icon, String title) {
      return (WWindow)this.w(new WMeteorWindow(icon, title));
   }

   public WLabel label(String text, boolean title, double maxWidth) {
      return maxWidth == 0.0 ? (WLabel)this.w(new WMeteorLabel(text, title)) : (WLabel)this.w(new WMeteorMultiLabel(text, title, maxWidth));
   }

   public WHorizontalSeparator horizontalSeparator(String text) {
      return (WHorizontalSeparator)this.w(new WMeteorHorizontalSeparator(text));
   }

   public WVerticalSeparator verticalSeparator() {
      return (WVerticalSeparator)this.w(new WMeteorVerticalSeparator());
   }

   protected WButton button(String text, GuiTexture texture) {
      return (WButton)this.w(new WMeteorButton(text, texture));
   }

   protected WConfirmedButton confirmedButton(String s, String s1, GuiTexture guiTexture) {
      return null;
   }

   public WMinus minus() {
      return (WMinus)this.w(new WMeteorMinus());
   }

   public WConfirmedMinus confirmedMinus() {
      return null;
   }

   public WPlus plus() {
      return (WPlus)this.w(new WMeteorPlus());
   }

   public WCheckbox checkbox(boolean checked) {
      return (WCheckbox)this.w(new WMeteorCheckbox(checked));
   }

   public WSlider slider(double value, double min, double max) {
      return (WSlider)this.w(new WMeteorSlider(value, min, max));
   }

   public WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends Renderer> renderer) {
      return (WTextBox)this.w(new WMeteorTextBox(text, placeholder, filter, renderer));
   }

   public <T> WDropdown<T> dropdown(T[] values, T value) {
      return (WDropdown<T>)this.w(new WMeteorDropdown(values, value));
   }

   public WTriangle triangle() {
      return (WTriangle)this.w(new WMeteorTriangle());
   }

   public WTooltip tooltip(String text) {
      return (WTooltip)this.w(new WMeteorTooltip(text));
   }

   public WView view() {
      return (WView)this.w(new WMeteorView());
   }

   public WSection section(String title, boolean expanded, WWidget headerWidget) {
      return (WSection)this.w(new WMeteorSection(title, expanded, headerWidget));
   }

   public WAccount account(WidgetScreen screen, Account<?> account) {
      return (WAccount)this.w(new WMeteorAccount(screen, account));
   }

   public WWidget module(Module module) {
      return this.w(new WMeteorModule(module, module.title));
   }

   public WWidget module(Module module, String title) {
      return this.w(new WMeteorModule(module, title));
   }

   public WQuad quad(Color color) {
      return (WQuad)this.w(new WMeteorQuad(color));
   }

   public WTopBar topBar() {
      return (WTopBar)this.w(new WMeteorTopBar());
   }

   public WFavorite favorite(boolean checked) {
      return (WFavorite)this.w(new WMeteorFavorite(checked));
   }

   public Color textColor() {
      return (Color)this.textColor.get();
   }

   public Color textSecondaryColor() {
      return (Color)this.textSecondaryColor.get();
   }

   public Color starscriptTextColor() {
      return (Color)this.starscriptText.get();
   }

   public Color starscriptBraceColor() {
      return (Color)this.starscriptBraces.get();
   }

   public Color starscriptParenthesisColor() {
      return (Color)this.starscriptParenthesis.get();
   }

   public Color starscriptDotColor() {
      return (Color)this.starscriptDots.get();
   }

   public Color starscriptCommaColor() {
      return (Color)this.starscriptCommas.get();
   }

   public Color starscriptOperatorColor() {
      return (Color)this.starscriptOperators.get();
   }

   public Color starscriptStringColor() {
      return (Color)this.starscriptStrings.get();
   }

   public Color starscriptNumberColor() {
      return (Color)this.starscriptNumbers.get();
   }

   public Color starscriptKeywordColor() {
      return (Color)this.starscriptKeywords.get();
   }

   public Color starscriptAccessedObjectColor() {
      return (Color)this.starscriptAccessedObjects.get();
   }

   public TextRenderer textRenderer() {
      return TextRenderer.get();
   }

   public double scale(double value) {
      return value * (Double)this.scale.get();
   }

   public boolean categoryIcons() {
      return (Boolean)this.categoryIcons.get();
   }

   public boolean hideHUD() {
      return (Boolean)this.hideHUD.get();
   }

   public int roundAmount() {
      return (Integer)this.round.get();
   }

   public class ThreeStateColorSetting {
      private final Setting<SettingColor> normal;
      private final Setting<SettingColor> hovered;
      private final Setting<SettingColor> pressed;

      public ThreeStateColorSetting(SettingGroup group, String name, SettingColor c1, SettingColor c2, SettingColor c3) {
         Objects.requireNonNull(MeteorRoundedGuiTheme.this);
         super();
         this.normal = MeteorRoundedGuiTheme.this.color(group, name, "Color of " + name + ".", c1);
         this.hovered = MeteorRoundedGuiTheme.this.color(group, "hovered-" + name, "Color of " + name + " when hovered.", c2);
         this.pressed = MeteorRoundedGuiTheme.this.color(group, "pressed-" + name, "Color of " + name + " when pressed.", c3);
      }

      public SettingColor get() {
         return (SettingColor)this.normal.get();
      }

      public SettingColor get(boolean pressed, boolean hovered, boolean bypassDisableHoverColor) {
         if (pressed) {
            return (SettingColor)this.pressed.get();
         } else {
            return !hovered || !bypassDisableHoverColor && MeteorRoundedGuiTheme.this.disableHoverColor
               ? (SettingColor)this.normal.get()
               : (SettingColor)this.hovered.get();
         }
      }

      public SettingColor get(boolean pressed, boolean hovered) {
         return this.get(pressed, hovered, false);
      }
   }
}
