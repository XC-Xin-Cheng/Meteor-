package anticope.rejects.gui.screens;

import anticope.rejects.mixin.EntityAccessor;
import anticope.rejects.modules.InteractionMenu;
import com.mojang.blaze3d.platform.InputConstants;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.PeekScreen;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector2f;
import org.meteordev.starscript.Script;
import org.meteordev.starscript.Section;
import org.meteordev.starscript.compiler.Compiler;
import org.meteordev.starscript.compiler.Parser;
import org.meteordev.starscript.compiler.Parser.Result;
import org.meteordev.starscript.utils.Error;
import org.meteordev.starscript.utils.StarscriptError;

public class InteractionScreen extends Screen {
   public static Entity interactionMenuEntity;
   private final Entity entity;
   private String focusedString = null;
   private int crosshairX;
   private int crosshairY;
   private int focusedDot = -1;
   private float yaw;
   private float pitch;
   private final Map<String, Consumer<Entity>> functions;
   private final Map<String, String> msgs;
   private final Identifier GUI_ICONS_TEXTURE = Identifier.parse("textures/gui/icons.png");
   private final InteractionScreen.StaticListener shiftListener = new InteractionScreen.StaticListener();
   private final int selectedDotColor;
   private final int dotColor;
   private final int backgroundColor;
   private final int borderColor;
   private final int textColor;

   public InteractionScreen(Entity e) {
      this(e, (InteractionMenu)Modules.get().get(InteractionMenu.class));
   }

   public InteractionScreen(Entity entity, InteractionMenu module) {
      super(Component.literal("Menu Screen"));
      if (module == null) {
         this.closeScreen();
      }

      this.selectedDotColor = ((SettingColor)module.selectedDotColor.get()).getPacked();
      this.dotColor = ((SettingColor)module.dotColor.get()).getPacked();
      this.backgroundColor = ((SettingColor)module.backgroundColor.get()).getPacked();
      this.borderColor = ((SettingColor)module.borderColor.get()).getPacked();
      this.textColor = ((SettingColor)module.textColor.get()).getPacked();
      this.entity = entity;
      this.functions = new HashMap<>();
      this.functions.put("Stats", e -> {
         this.closeScreen();
         this.minecraft.setScreen(new StatsScreen(e));
      });
      switch (entity) {
         case Player playerEntity:
            this.functions.put("Open Inventory", e -> {
               this.closeScreen();
               this.minecraft.setScreen(new InventoryScreen((Player)e));
            });
            break;
         case AbstractHorse abstractHorseEntity:
            this.functions.put("Open Inventory", e -> {
               this.closeScreen();
               if (this.minecraft.player.isHandsBusy()) {
                  this.minecraft.player.connection.send(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, true, false)));
               }

               this.minecraft.player.connection.send(new ServerboundInteractPacket(entity.getId(), InteractionHand.MAIN_HAND, entity.position(), true));
               this.minecraft.player.setShiftKeyDown(false);
            });
            break;
         case AbstractMinecartContainer storageMinecartEntity:
            this.functions.put("Open Inventory", e -> {
               this.closeScreen();
               this.minecraft.player.connection.send(new ServerboundInteractPacket(entity.getId(), InteractionHand.MAIN_HAND, entity.position(), true));
            });
            break;
         case null:
         default:
            this.functions.put("Open Inventory", e -> {
               this.closeScreen();
               ItemStack container = new ItemStack(Items.CHEST);
               container.set(DataComponents.CUSTOM_NAME, e.getName());
               this.minecraft.setScreen(new PeekScreen(container, this.getInventory(e)));
            });
      }

      this.functions.put("Spectate", e -> {
         Minecraft.getInstance().setCameraEntity(e);
         this.minecraft.player.sendOverlayMessage(Component.literal("Sneak to un-spectate."));
         MeteorClient.EVENT_BUS.subscribe(this.shiftListener);
         this.closeScreen();
      });
      if (entity.isCurrentlyGlowing()) {
         this.functions.put("Remove glow", e -> {
            e.setGlowingTag(false);
            ((EntityAccessor)e).invokeSetFlag(6, false);
            this.closeScreen();
         });
      } else {
         this.functions.put("Glow", e -> {
            e.setGlowingTag(true);
            ((EntityAccessor)e).invokeSetFlag(6, true);
            this.closeScreen();
         });
      }

      if (entity.noPhysics) {
         this.functions.put("Disable NoClip", e -> {
            entity.noPhysics = false;
            this.closeScreen();
         });
      } else {
         this.functions.put("NoClip", e -> {
            entity.noPhysics = true;
            this.closeScreen();
         });
      }

      this.msgs = (Map<String, String>)((InteractionMenu)Modules.get().get(InteractionMenu.class)).messages.get();
      this.msgs.keySet().forEach(key -> this.functions.put(key, e -> {
         this.closeScreen();
         interactionMenuEntity = e;
         Result result = Parser.parse(this.msgs.get(key));
         if (!result.hasErrors()) {
            Script script = Compiler.compile(result);

            try {
               Section section = MeteorStarscript.ss.run(script);
               this.minecraft.setScreen(new ChatScreen(section.text, false));
            } catch (StarscriptError var6x) {
               MeteorStarscript.printChatError(var6x);
            }
         } else {
            for (Error error : result.errors) {
               MeteorStarscript.printChatError(error);
            }
         }
      }));
      this.functions.put("Cancel", e -> this.closeScreen());
   }

   private ItemStack[] getInventory(Entity e) {
      ItemStack[] stack = new ItemStack[27];
      int[] index = new int[]{0};
      if (e instanceof EnderMan) {
         try {
            stack[index[0]] = ((EnderMan)e).getCarriedBlock().getBlock().asItem().getDefaultInstance();
            index[0]++;
         } catch (NullPointerException var11) {
         }
      }

      LivingEntity a = (LivingEntity)e;
      ItemStack bodyStack = a.getItemBySlot(EquipmentSlot.BODY);
      if (bodyStack != null && !bodyStack.isEmpty()) {
         stack[index[0]] = bodyStack;
         index[0]++;
      }

      for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
         ItemStack itemStack = a.getItemBySlot(slot);
         if (itemStack != null && !itemStack.isEmpty()) {
            stack[index[0]] = itemStack;
            index[0]++;
         }
      }

      for (EquipmentSlot slotx : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
         ItemStack itemStack = a.getItemBySlot(slotx);
         if (itemStack != null && !itemStack.isEmpty()) {
            stack[index[0]] = itemStack;
            index[0]++;
         }
      }

      for (int i = index[0]; i < 27; i++) {
         stack[i] = Items.AIR.getDefaultInstance();
      }

      return stack;
   }

   public void init() {
      super.init();
      this.cursorMode(212994);
      this.yaw = this.minecraft.player.getYRot();
      this.pitch = this.minecraft.player.getXRot();
   }

   private void cursorMode(int mode) {
      KeyMapping.releaseAll();
      double x = this.minecraft.getWindow().getScreenWidth() / 2.0;
      double y = this.minecraft.getWindow().getScreenHeight() / 2.0;
      InputConstants.grabOrReleaseMouse(this.minecraft.getWindow(), mode, x, y);
   }

   public void tick() {
      if (((Keybind)((InteractionMenu)Modules.get().get(InteractionMenu.class)).keybind.get()).isPressed()) {
         this.onClose();
      }
   }

   private void closeScreen() {
      this.minecraft.setScreen(null);
   }

   public void onClose() {
      this.cursorMode(212993);
      if (this.focusedString != null) {
         this.functions.get(this.focusedString).accept(this.entity);
      } else {
         this.minecraft.setScreen(null);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      this.drawDots(context, (int)(Math.min(this.height, this.width) / 2 * 0.75), mouseX, mouseY);
      context.centeredText(this.font, this.entity.getName(), this.width / 2, 12, -1);
      Vector2f mouse = this.getMouseVecs(mouseX, mouseY);
      this.crosshairX = (int)mouse.x + this.width / 2;
      this.crosshairY = (int)mouse.y + this.height / 2;
      this.minecraft.player.setYRot(this.yaw + mouse.x / 3.0F);
      this.minecraft.player.setXRot(Mth.clamp(this.pitch + mouse.y / 3.0F, -90.0F, 90.0F));
      super.extractRenderState(context, mouseX, mouseY, delta);
   }

   private Vector2f getMouseVecs(int mouseX, int mouseY) {
      int scale = (Integer)this.minecraft.options.guiScale().get();
      Vector2f mouse = new Vector2f(mouseX, mouseY);
      Vector2f center = new Vector2f(this.width / 2, this.height / 2);
      mouse.sub(center).normalize();
      if (scale == 0) {
         scale = 4;
      }

      if (Math.hypot(this.width / 2 - mouseX, this.height / 2 - mouseY) < 1.0F / scale * 200.0F) {
         mouse.mul((float)Math.hypot(this.width / 2 - mouseX, this.height / 2 - mouseY));
      } else {
         mouse.mul(1.0F / scale * 200.0F);
      }

      return mouse;
   }

   private void drawDots(GuiGraphicsExtractor context, int radius, int mouseX, int mouseY) {
      ArrayList<Point> pointList = new ArrayList<>();
      String[] cache = new String[this.functions.size()];
      double lowestDistance = Double.MAX_VALUE;
      int i = 0;

      for (String string : this.functions.keySet()) {
         double s = (double)i / this.functions.size() * 2.0 * Math.PI;
         int x = (int)Math.round(radius * Math.cos(s) + this.width / 2);
         int y = (int)Math.round(radius * Math.sin(s) + this.height / 2);
         this.drawTextField(context, x, y, string);
         if (Math.hypot(x - mouseX, y - mouseY) < lowestDistance) {
            lowestDistance = Math.hypot(x - mouseX, y - mouseY);
            this.focusedDot = i;
         }

         cache[i] = string;
         pointList.add(new Point(x, y));
         i++;
      }

      for (int j = 0; j < this.functions.size(); j++) {
         Point point = pointList.get(j);
         if (pointList.get(this.focusedDot) == point) {
            this.drawDot(context, point.x - 4, point.y - 4, this.selectedDotColor);
            this.focusedString = cache[this.focusedDot];
         } else {
            this.drawDot(context, point.x - 4, point.y - 4, this.dotColor);
         }
      }
   }

   private void drawRect(GuiGraphicsExtractor context, int startX, int startY, int width, int height, int colorInner, int colorOuter) {
      context.horizontalLine(startX, startX + width, startY, colorOuter);
      context.horizontalLine(startX, startX + width, startY + height, colorOuter);
      context.verticalLine(startX, startY, startY + height, colorOuter);
      context.verticalLine(startX + width, startY, startY + height, colorOuter);
      context.fill(startX + 1, startY + 1, startX + width, startY + height, colorInner);
   }

   private void drawTextField(GuiGraphicsExtractor context, int x, int y, String key) {
      if (x >= this.width / 2) {
         this.drawRect(context, x + 10, y - 8, this.font.width(key) + 3, 15, this.backgroundColor, this.borderColor);
         context.text(this.font, key, x + 12, y - 4, this.textColor);
      } else {
         this.drawRect(context, x - 14 - this.font.width(key), y - 8, this.font.width(key) + 3, 15, this.backgroundColor, this.borderColor);
         context.text(this.font, key, x - 12 - this.font.width(key), y - 4, this.textColor);
      }
   }

   private void drawDot(GuiGraphicsExtractor context, int startX, int startY, int colorInner) {
      context.horizontalLine(startX + 2, startX + 5, startY, this.borderColor);
      context.horizontalLine(startX + 1, startX + 6, startY + 1, this.borderColor);
      context.horizontalLine(startX + 2, startX + 5, startY + 1, colorInner);
      context.fill(startX, startY + 2, startX + 8, startY + 6, this.borderColor);
      context.fill(startX + 1, startY + 2, startX + 7, startY + 6, colorInner);
      context.horizontalLine(startX + 1, startX + 6, startY + 6, this.borderColor);
      context.horizontalLine(startX + 2, startX + 5, startY + 6, colorInner);
      context.horizontalLine(startX + 2, startX + 5, startY + 7, this.borderColor);
      context.horizontalLine(startX + 2, startX + 3, startY + 1, -2130706433);
      context.horizontalLine(startX + 1, startX + 1, startY + 2, -2130706433);
   }

   private class StaticListener {
      private StaticListener() {
         Objects.requireNonNull(InteractionScreen.this);
         super();
      }

      @EventHandler
      private void onKey(KeyInputEvent event) {
         if (event.key() == InteractionScreen.this.minecraft.options.keyShift.getDefaultKey().getValue()) {
            InteractionScreen.this.minecraft.setCameraEntity(InteractionScreen.this.minecraft.player);
            event.cancel();
            MeteorClient.EVENT_BUS.unsubscribe(this);
         }
      }
   }
}
