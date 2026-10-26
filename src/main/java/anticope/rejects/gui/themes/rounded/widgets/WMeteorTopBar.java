package anticope.rejects.gui.themes.rounded.widgets;

import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.gui.themes.rounded.MeteorWidget;
import anticope.rejects.utils.gui.GuiUtils;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class WMeteorTopBar extends WTopBar implements MeteorWidget {
   protected Color getButtonColor(boolean pressed, boolean hovered) {
      return this.theme().backgroundColor.get(pressed, hovered);
   }

   protected Color getNameColor() {
      return (Color)this.theme().textColor.get();
   }

   public void init() {
      for (Tab tab : Tabs.get()) {
         this.add(new WMeteorTopBar.WTopBarButton(tab));
      }
   }

   protected int getState(WMeteorTopBar.WTopBarButton btn) {
      int a = 0;
      if (btn.equals(((Cell)this.cells.get(0)).widget())) {
         a |= 1;
      }

      if (btn.equals(((Cell)this.cells.get(this.cells.size() - 1)).widget())) {
         a |= 2;
      }

      return a;
   }

   protected class WTopBarButton extends WPressable {
      private final Tab tab;

      public WTopBarButton(Tab tab) {
         Objects.requireNonNull(WMeteorTopBar.this);
         super();
         this.tab = tab;
      }

      protected void onCalculateSize() {
         double pad = this.pad();
         this.width = pad + this.theme.textWidth(this.tab.name) + pad;
         this.height = pad + this.theme.textHeight() + pad;
      }

      protected void onPressed(int button) {
         Screen screen = MeteorClient.mc.screen;
         if (!(screen instanceof TabScreen) || ((TabScreen)screen).tab != this.tab) {
            double mouseX = MeteorClient.mc.mouseHandler.xpos();
            double mouseY = MeteorClient.mc.mouseHandler.ypos();
            this.tab.openScreen(this.theme);
            GLFW.glfwSetCursorPos(MeteorClient.mc.getWindow().handle(), mouseX, mouseY);
         }
      }

      protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
         double pad = this.pad();
         Color color = WMeteorTopBar.this.getButtonColor(
            this.pressed || MeteorClient.mc.screen instanceof TabScreen && ((TabScreen)MeteorClient.mc.screen).tab == this.tab, this.mouseOver
         );
         switch (WMeteorTopBar.this.getState(this)) {
            case 1:
               GuiUtils.quadRoundedSide(renderer, this, color, ((MeteorRoundedGuiTheme)this.theme).roundAmount(), false);
               break;
            case 2:
               GuiUtils.quadRoundedSide(renderer, this, color, ((MeteorRoundedGuiTheme)this.theme).roundAmount(), true);
               break;
            case 3:
               GuiUtils.quadRounded(renderer, this, color, ((MeteorRoundedGuiTheme)this.theme).roundAmount());
               break;
            default:
               renderer.quad(this, color);
         }

         renderer.text(this.tab.name, this.x + pad, this.y + pad, WMeteorTopBar.this.getNameColor(), false);
      }
   }
}
