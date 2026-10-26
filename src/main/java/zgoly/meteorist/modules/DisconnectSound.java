package zgoly.meteorist.modules;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.events.DisconnectedScreenEvent;

public class DisconnectSound extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<List<SoundEvent>> sound = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("sound")).description("Sound to play."))
               .defaultValue(List.of((SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value())))
            .build()
      );
   private final Setting<Double> soundPitch = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("sound-pitch"))
               .description("Pitch of the sound."))
            .defaultValue(0.5)
            .min(0.0)
            .sliderRange(0.5, 2.0)
            .build()
      );
   private final Setting<Double> soundVolume = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("sound-volume"))
               .description("Volume of the sound."))
            .defaultValue(1.0)
            .min(0.0)
            .sliderRange(0.0, 1.0)
            .build()
      );

   public DisconnectSound() {
      super(Meteorist.CATEGORY, "disconnect-sound", "Plays a sound when the Disconnected Screen appears (e.g., when kicked).");
      MeteorClient.EVENT_BUS.subscribe(this);
   }

   private void playSound() {
      this.mc
         .getSoundManager()
         .play(
            SimpleSoundInstance.forUI(
               (SoundEvent)((List)this.sound.get()).getFirst(), ((Double)this.soundPitch.get()).floatValue(), ((Double)this.soundVolume.get()).floatValue()
            )
         );
   }

   public WWidget getWidget(GuiTheme theme) {
      WButton button = theme.button("Preview");
      button.action = this::playSound;
      return button;
   }

   @EventHandler
   private void onDisconnectedScreen(DisconnectedScreenEvent event) {
      if (this.isActive()) {
         this.playSound();
      }
   }
}
