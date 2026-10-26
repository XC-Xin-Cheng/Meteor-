package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SoundLocator extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgRender = this.settings.createGroup("Render");
   private final Setting<Boolean> whitelist = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("whitelist")).description("Enable sounds filter whitelist.")).defaultValue(false)).build());
   private final Setting<List<SoundEvent>> sounds = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)new meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder()
                        .name("sounds"))
                     .description("Sounds to find."))
                  .defaultValue(new ArrayList(0)))
               .visible(this.whitelist::get))
            .build()
      );
   private final Setting<Boolean> chatActive = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("log-chat")).description("Send the position of the sound in the chat.")).defaultValue(false)).build()
      );
   private final Setting<Integer> timeS = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("time"))
                  .description("The time between the sounds verification."))
               .defaultValue(60))
            .build()
      );
   private final Setting<Boolean> renderActive = this.sgRender
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("render-positions")).description("Renders boxes where the sound was emitted.")).defaultValue(true))
            .build()
      );
   private final Setting<ShapeMode> shapeMode = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("shape-mode"))
                  .description("How the shapes are rendered."))
               .defaultValue(ShapeMode.Both))
            .build()
      );
   private final Setting<SettingColor> sideColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("side-color"))
               .description("The side color of the target sound rendering."))
            .defaultValue(new SettingColor(255, 0, 0, 70))
            .build()
      );
   private final Setting<SettingColor> lineColor = this.sgRender
      .add(
         ((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                  .name("line-color"))
               .description("The line color of the target sound rendering."))
            .defaultValue(new SettingColor(255, 0, 0))
            .build()
      );
   private List<Vec3> renderPos = new ArrayList<>();
   private List<Integer> delay = new ArrayList<>();

   public SoundLocator() {
      super(MeteorRejectsAddon.CATEGORY, "sound-locator", "Prints locations of sound events.");
   }

   @EventHandler
   private void onTick(Pre event) {
      Iterator<Integer> iterator = this.delay.iterator();

      while (iterator.hasNext()) {
         int time = iterator.next();
         if (time <= 0) {
            iterator.remove();
            this.renderPos.remove(0);
         } else {
            this.delay.set(this.delay.indexOf(time), time - 1);
         }
      }
   }

   @EventHandler
   private void onPlaySound(PlaySoundEvent event) {
      if ((Boolean)this.whitelist.get()) {
         for (SoundEvent sound : (List)this.sounds.get()) {
            if (sound.location().equals(event.sound.getIdentifier())) {
               this.printSound(event.sound);
               break;
            }
         }
      } else {
         this.printSound(event.sound);
      }
   }

   private void printSound(SoundInstance sound) {
      WeighedSoundEvents soundSet = this.mc.getSoundManager().getSoundEvent(sound.getIdentifier());
      Vec3 pos = new Vec3(sound.getX() - 0.5, sound.getY() - 0.5, sound.getZ() - 0.5);
      if (!this.renderPos.contains(pos)) {
         this.renderPos.add(pos);
         this.delay.add((Integer)this.timeS.get());
         if ((Boolean)this.chatActive.get()) {
            MutableComponent text;
            if (soundSet != null && soundSet.getSubtitle() != null) {
               text = soundSet.getSubtitle().copy();
            } else {
               text = Component.literal(sound.getIdentifier().toString());
            }

            text.append(String.format("%s at ", ChatFormatting.RESET));
            text.append(ChatUtils.formatCoords(pos));
            text.append(String.format("%s.", ChatFormatting.RESET));
            this.info(text);
         }
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if ((Boolean)this.renderActive.get()) {
         this.renderPos
            .forEach(
               pos -> event.renderer
                  .box(AABB.unitCubeFromLowerCorner(pos), (Color)this.sideColor.get(), (Color)this.lineColor.get(), (ShapeMode)this.shapeMode.get(), 0)
            );
      }
   }
}
