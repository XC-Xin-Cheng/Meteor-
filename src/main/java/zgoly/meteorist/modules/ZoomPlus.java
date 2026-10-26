package zgoly.meteorist.modules;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorclient.events.render.GetFovEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.events.HandRenderEvent;
import zgoly.meteorist.events.MouseSensitivityEvent;
import zgoly.meteorist.events.SmoothCameraEnabledEvent;
import zgoly.meteorist.events.TweenEndEvent;
import zgoly.meteorist.events.UpdateCameraSmoothingEvent;
import zgoly.meteorist.utils.misc.TweenHandler;

public class ZoomPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgZoomIn = this.settings.createGroup("Zoom In");
   private final SettingGroup sgZoomOut = this.settings.createGroup("Zoom Out");
   private final SettingGroup sgScrollIn = this.settings.createGroup("Scroll In");
   private final SettingGroup sgScrollOut = this.settings.createGroup("Scroll Out");
   private final Setting<Double> zoomFactor = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("zoom-factor")).description("The factor by which to zoom.")).defaultValue(6.0).min(2.0).build());
   private final Setting<Double> scrollStep = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("scroll-step")).description("The step size for scrolling.")).defaultValue(1.0).min(0.0).build());
   private final Setting<Boolean> saveScrollStep = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("save-scroll-step"))
                  .description("Whether to save the scroll step value between sessions."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> cinematic = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("cinematic"))
                  .description("Enables cinematic mode when zooming."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Double> cinematicFactor = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("cinematic-factor")).description("The factor for cinematic mode."))
               .defaultValue(1.0)
               .min(0.1)
               .visible(this.cinematic::get))
            .build()
      );
   private final Setting<Boolean> normalizeCinematic = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("normalize-cinematic"))
                     .description("Whether to normalize cinematic mode."))
                  .defaultValue(true))
               .visible(this.cinematic::get))
            .build()
      );
   private final Setting<Boolean> normalizeSensitivity = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("normalize-sensitivity"))
                  .description("Whether to normalize sensitivity."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Double> sensitivityNormalizationFactor = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("sensitivity-normalization-factor")).description("The factor for normalized sensitivity."))
               .defaultValue(0.5)
               .min(0.0)
               .visible(this.normalizeSensitivity::get))
            .build()
      );
   private final Setting<Boolean> normalizeZoom = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("normalize-zoom"))
                  .description("Whether to normalize zoom."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Double> zoomNormalizationFactor = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("zoom-normalization-factor")).description("The factor for normalized zoom."))
               .defaultValue(0.25)
               .min(0.0)
               .visible(this.normalizeZoom::get))
            .build()
      );
   private final Setting<Boolean> hideHud = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("hide-HUD"))
                  .description("Whether to hide the HUD."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> renderHands = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("render-hands"))
                     .description("Whether to render hands."))
                  .defaultValue(true))
               .visible(() -> !(Boolean)this.hideHud.get()))
            .build()
      );
   private final Setting<Boolean> scroll = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("allow-scroll"))
                  .description("Whether to allow zoom scrolling."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> instantZoomIn = this.sgZoomIn
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("instant-zoom-in"))
                  .description("Whether to zoom in instantly."))
               .defaultValue(false))
            .build()
      );
   private final Setting<TweenHandler.EasingStyle> zoomInEasingStyle = this.sgZoomIn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("easing-style"))
                     .description("The easing style for zooming in."))
                  .defaultValue(TweenHandler.EasingStyle.Quint))
               .visible(() -> !(Boolean)this.instantZoomIn.get()))
            .build()
      );
   private final Setting<TweenHandler.EasingDirection> zoomInEasingDirection = this.sgZoomIn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("easing-direction"))
                     .description("The direction of the easing for zooming in."))
                  .defaultValue(TweenHandler.EasingDirection.Out))
               .visible(() -> !(Boolean)this.instantZoomIn.get()))
            .build()
      );
   private final Setting<Double> zoomInEasingDuration = this.sgZoomIn
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("easing-duration")).description("The duration of the easing for zooming in."))
               .defaultValue(1.0)
               .min(0.1)
               .visible(() -> !(Boolean)this.instantZoomIn.get()))
            .build()
      );
   private final Setting<Boolean> zoomInPlaySound = this.sgZoomIn
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("play-sound"))
                  .description("Whether to play a sound when zooming in."))
               .defaultValue(true))
            .build()
      );
   private final Setting<List<SoundEvent>> zoomInSound = this.sgZoomIn
      .add(
         ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)new meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder()
                        .name("sound"))
                     .description("Sound to play."))
                  .defaultValue(List.of(SoundEvents.UI_TOAST_IN)))
               .visible(this.zoomInPlaySound::get))
            .build()
      );
   private final Setting<Double> zoomInSoundPitch = this.sgZoomIn
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("sound-pitch")).description("Pitch of the sound."))
               .defaultValue(1.0)
               .min(0.0)
               .sliderRange(0.5, 2.0)
               .visible(this.zoomInPlaySound::get))
            .build()
      );
   private final Setting<Double> zoomInSoundVolume = this.sgZoomIn
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("sound-volume")).description("Volume of the sound."))
               .defaultValue(0.25)
               .min(0.0)
               .sliderRange(0.0, 1.0)
               .visible(this.zoomInPlaySound::get))
            .build()
      );
   private final Setting<Boolean> instantZoomOut = this.sgZoomOut
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("instant-zoom-out"))
                  .description("Whether to zoom out instantly."))
               .defaultValue(false))
            .build()
      );
   private final Setting<TweenHandler.EasingStyle> zoomOutEasingStyle = this.sgZoomOut
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("easing-style"))
                     .description("The easing style for zooming out."))
                  .defaultValue(TweenHandler.EasingStyle.Quint))
               .visible(() -> !(Boolean)this.instantZoomOut.get()))
            .build()
      );
   private final Setting<TweenHandler.EasingDirection> zoomOutEasingDirection = this.sgZoomOut
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("easing-direction"))
                     .description("The direction of the easing for zooming out."))
                  .defaultValue(TweenHandler.EasingDirection.Out))
               .visible(() -> !(Boolean)this.instantZoomOut.get()))
            .build()
      );
   private final Setting<Double> zoomOutEasingDuration = this.sgZoomOut
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("easing-duration")).description("The duration of the easing for zooming out."))
               .defaultValue(1.0)
               .min(0.1)
               .visible(() -> !(Boolean)this.instantZoomOut.get()))
            .build()
      );
   private final Setting<Boolean> zoomOutPlaySound = this.sgZoomOut
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("play-sound"))
                  .description("Whether to play a sound when zooming out."))
               .defaultValue(true))
            .build()
      );
   private final Setting<List<SoundEvent>> zoomOutSound = this.sgZoomOut
      .add(
         ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)new meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder()
                        .name("sound"))
                     .description("Sound to play."))
                  .defaultValue(List.of(SoundEvents.UI_TOAST_OUT)))
               .visible(this.zoomOutPlaySound::get))
            .build()
      );
   private final Setting<Double> zoomOutSoundPitch = this.sgZoomOut
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("sound-pitch")).description("Pitch of the sound."))
               .defaultValue(1.0)
               .min(0.0)
               .sliderRange(0.5, 2.0)
               .visible(this.zoomOutPlaySound::get))
            .build()
      );
   private final Setting<Double> zoomOutSoundVolume = this.sgZoomOut
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("sound-volume")).description("Volume of the sound."))
               .defaultValue(0.25)
               .min(0.0)
               .sliderRange(0.0, 1.0)
               .visible(this.zoomOutPlaySound::get))
            .build()
      );
   private final Setting<Boolean> instantScrollIn = this.sgScrollIn
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("instant-scroll-in"))
                  .description("Whether to scroll in instantly."))
               .defaultValue(false))
            .build()
      );
   private final Setting<TweenHandler.EasingStyle> scrollInEasingStyle = this.sgScrollIn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("easing-style"))
                     .description("The easing style for scrolling in."))
                  .defaultValue(TweenHandler.EasingStyle.Quint))
               .visible(() -> !(Boolean)this.instantScrollIn.get()))
            .build()
      );
   private final Setting<TweenHandler.EasingDirection> scrollInEasingDirection = this.sgScrollIn
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("easing-direction"))
                     .description("The direction of the easing for scrolling in."))
                  .defaultValue(TweenHandler.EasingDirection.Out))
               .visible(() -> !(Boolean)this.instantScrollIn.get()))
            .build()
      );
   private final Setting<Double> scrollInEasingDuration = this.sgScrollIn
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("easing-duration")).description("The duration of the easing for scrolling in."))
               .defaultValue(0.5)
               .min(0.1)
               .visible(() -> !(Boolean)this.instantScrollIn.get()))
            .build()
      );
   private final Setting<Boolean> scrollInPlaySound = this.sgScrollIn
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("play-sound"))
                  .description("Whether to play a sound when scrolling in."))
               .defaultValue(true))
            .build()
      );
   private final Setting<List<SoundEvent>> scrollInSound = this.sgScrollIn
      .add(
         ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)new meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder()
                        .name("sound"))
                     .description("Sound to play."))
                  .defaultValue(List.of(SoundEvents.SPYGLASS_USE)))
               .visible(this.scrollInPlaySound::get))
            .build()
      );
   private final Setting<Double> scrollInSoundPitch = this.sgScrollIn
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("sound-pitch")).description("Pitch of the sound."))
               .defaultValue(1.0)
               .min(0.0)
               .sliderRange(0.5, 2.0)
               .visible(this.scrollInPlaySound::get))
            .build()
      );
   private final Setting<Double> scrollInSoundVolume = this.sgScrollIn
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("sound-volume")).description("Volume of the sound."))
               .defaultValue(0.5)
               .min(0.0)
               .sliderRange(0.0, 1.0)
               .visible(this.scrollInPlaySound::get))
            .build()
      );
   private final Setting<Boolean> instantScrollOut = this.sgScrollOut
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("instant-scroll-out"))
                  .description("Whether to scroll out instantly."))
               .defaultValue(false))
            .build()
      );
   private final Setting<TweenHandler.EasingStyle> scrollOutEasingStyle = this.sgScrollOut
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("easing-style"))
                     .description("The easing style for scrolling out."))
                  .defaultValue(TweenHandler.EasingStyle.Quint))
               .visible(() -> !(Boolean)this.instantScrollOut.get()))
            .build()
      );
   private final Setting<TweenHandler.EasingDirection> scrollOutEasingDirection = this.sgScrollOut
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("easing-direction"))
                     .description("The direction of the easing for scrolling out."))
                  .defaultValue(TweenHandler.EasingDirection.Out))
               .visible(() -> !(Boolean)this.instantScrollOut.get()))
            .build()
      );
   private final Setting<Double> scrollOutEasingDuration = this.sgScrollOut
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("easing-duration")).description("The duration of the easing for scrolling out."))
               .defaultValue(0.5)
               .min(0.1)
               .visible(() -> !(Boolean)this.instantScrollOut.get()))
            .build()
      );
   private final Setting<Boolean> scrollOutPlaySound = this.sgScrollOut
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("play-sound"))
                  .description("Whether to play a sound when scrolling out."))
               .defaultValue(true))
            .build()
      );
   private final Setting<List<SoundEvent>> scrollOutSound = this.sgScrollOut
      .add(
         ((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)((meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder)new meteordevelopment.meteorclient.settings.SoundEventListSetting.Builder()
                        .name("sound"))
                     .description("Sound to play."))
                  .defaultValue(List.of(SoundEvents.SPYGLASS_STOP_USING)))
               .visible(this.scrollOutPlaySound::get))
            .build()
      );
   private final Setting<Double> scrollOutSoundPitch = this.sgScrollOut
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("sound-pitch")).description("Pitch of the sound."))
               .defaultValue(1.0)
               .min(0.0)
               .sliderRange(0.5, 2.0)
               .visible(this.scrollOutPlaySound::get))
            .build()
      );
   private final Setting<Double> scrollOutSoundVolume = this.sgScrollOut
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("sound-volume")).description("Volume of the sound."))
               .defaultValue(0.5)
               .min(0.0)
               .sliderRange(0.0, 1.0)
               .visible(this.scrollOutPlaySound::get))
            .build()
      );
   private final TweenHandler tweenHandler = new TweenHandler();
   private final AtomicDouble currentZoomFactor = new AtomicDouble(1.0);
   private final AtomicDouble currentScrollStep = new AtomicDouble(0.0);
   private double targetScrollStep = 0.0;
   private boolean isSubscribed = false;
   private boolean deactivated = false;

   public ZoomPlus() {
      super(Meteorist.CATEGORY, "zoom-plus", "Advanced Zoom module with more customizable settings.");
      this.autoSubscribe = false;
   }

   @EventHandler
   public void onActivate() {
      this.deactivated = false;
      if (!this.isSubscribed) {
         if ((Boolean)this.hideHud.get()) {
            this.mc.options.hideGui = true;
         }

         MeteorClient.EVENT_BUS.subscribe(this);
         MeteorClient.EVENT_BUS.subscribe(this.tweenHandler);
         this.isSubscribed = true;
      }

      if ((Boolean)this.zoomInPlaySound.get()) {
         this.mc
            .getSoundManager()
            .play(
               SimpleSoundInstance.forUI(
                  (SoundEvent)((List)this.zoomInSound.get()).getFirst(),
                  ((Double)this.zoomInSoundPitch.get()).floatValue(),
                  ((Double)this.zoomInSoundVolume.get()).floatValue()
               )
            );
      }

      this.tweenHandler
         .play(
            (Boolean)this.instantZoomIn.get(),
            this.currentZoomFactor,
            (Double)this.zoomFactor.get(),
            (Double)this.zoomInEasingDuration.get(),
            (TweenHandler.EasingStyle)this.zoomInEasingStyle.get(),
            (TweenHandler.EasingDirection)this.zoomInEasingDirection.get()
         );
      if ((Boolean)this.scroll.get()) {
         if ((Boolean)this.saveScrollStep.get()) {
            this.tweenHandler
               .play(
                  (Boolean)this.instantZoomIn.get(),
                  this.currentScrollStep,
                  this.targetScrollStep,
                  (Double)this.zoomInEasingDuration.get(),
                  (TweenHandler.EasingStyle)this.zoomInEasingStyle.get(),
                  (TweenHandler.EasingDirection)this.zoomInEasingDirection.get()
               );
         } else {
            this.targetScrollStep = 0.0;
         }
      }
   }

   @EventHandler
   public void onDeactivate() {
      this.deactivated = true;
      if ((Boolean)this.zoomOutPlaySound.get()) {
         this.mc
            .getSoundManager()
            .play(
               SimpleSoundInstance.forUI(
                  (SoundEvent)((List)this.zoomOutSound.get()).getFirst(),
                  ((Double)this.zoomOutSoundPitch.get()).floatValue(),
                  ((Double)this.zoomOutSoundVolume.get()).floatValue()
               )
            );
      }

      this.tweenHandler
         .play(
            (Boolean)this.instantZoomOut.get(),
            this.currentZoomFactor,
            1.0,
            (Double)this.zoomOutEasingDuration.get(),
            (TweenHandler.EasingStyle)this.zoomOutEasingStyle.get(),
            (TweenHandler.EasingDirection)this.zoomOutEasingDirection.get()
         );
      this.tweenHandler
         .play(
            (Boolean)this.instantZoomOut.get(),
            this.currentScrollStep,
            0.0,
            (Double)this.zoomOutEasingDuration.get(),
            (TweenHandler.EasingStyle)this.zoomOutEasingStyle.get(),
            (TweenHandler.EasingDirection)this.zoomOutEasingDirection.get()
         );
   }

   @EventHandler
   private void onMouseScroll(MouseScrollEvent event) {
      if (!this.deactivated && (Boolean)this.scroll.get()) {
         this.targetScrollStep = Math.max(0.0, event.value * (Double)this.scrollStep.get() + this.targetScrollStep);
         boolean scrollIn = event.value > 0.0;
         boolean playSound = scrollIn ? (Boolean)this.scrollInPlaySound.get() : (Boolean)this.scrollOutPlaySound.get();
         if (playSound) {
            SoundEvent sound = scrollIn ? (SoundEvent)((List)this.scrollInSound.get()).getFirst() : (SoundEvent)((List)this.scrollOutSound.get()).getFirst();
            float soundPitch = scrollIn ? ((Double)this.scrollInSoundPitch.get()).floatValue() : ((Double)this.scrollOutSoundPitch.get()).floatValue();
            float soundVolume = scrollIn ? ((Double)this.scrollInSoundVolume.get()).floatValue() : ((Double)this.scrollOutSoundVolume.get()).floatValue();
            this.mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, soundPitch, soundVolume));
         }

         boolean instantScroll = scrollIn ? (Boolean)this.instantScrollIn.get() : (Boolean)this.instantScrollOut.get();
         double scrollEasingDuration = scrollIn ? (Double)this.scrollInEasingDuration.get() : (Double)this.scrollOutEasingDuration.get();
         TweenHandler.EasingStyle scrollEasingStyle = scrollIn
            ? (TweenHandler.EasingStyle)this.scrollInEasingStyle.get()
            : (TweenHandler.EasingStyle)this.scrollOutEasingStyle.get();
         TweenHandler.EasingDirection scrollEasingDirection = scrollIn
            ? (TweenHandler.EasingDirection)this.scrollInEasingDirection.get()
            : (TweenHandler.EasingDirection)this.scrollOutEasingDirection.get();
         this.tweenHandler.play(instantScroll, this.currentScrollStep, this.targetScrollStep, scrollEasingDuration, scrollEasingStyle, scrollEasingDirection);
         event.cancel();
      }
   }

   @EventHandler
   private void onGetFov(GetFovEvent event) {
      event.fov = event.fov / (float)this.getScaling();
   }

   @EventHandler
   private void onHandRender(HandRenderEvent event) {
      if (!(Boolean)this.renderHands.get()) {
         event.renderHand = false;
      }
   }

   @EventHandler
   private void onGetMouseSensitivity(MouseSensitivityEvent event) {
      if ((Boolean)this.normalizeSensitivity.get()) {
         event.sensitivity = event.sensitivity / (1.0 + (this.getScaling() - 1.0) * (Double)this.sensitivityNormalizationFactor.get());
      }
   }

   @EventHandler
   private void onUpdateCameraSmoothing(UpdateCameraSmoothingEvent event) {
      if ((Boolean)this.cinematic.get()) {
         double normalizedCinematic = this.normalizeCinematic.get() ? this.getScaling() : 1.0;
         event.timeDelta = event.timeDelta * ((Double)this.cinematicFactor.get() * normalizedCinematic);
      }
   }

   @EventHandler
   public void onTweenEnd(TweenEndEvent event) {
      if (this.currentZoomFactor.get() == 1.0) {
         if ((Boolean)this.hideHud.get()) {
            this.mc.options.hideGui = false;
         }

         MeteorClient.EVENT_BUS.unsubscribe(this);
         MeteorClient.EVENT_BUS.unsubscribe(this.tweenHandler);
         this.isSubscribed = false;
      }
   }

   @EventHandler
   public void onSmoothCameraEnabled(SmoothCameraEnabledEvent event) {
      if (!this.deactivated) {
         event.enabled = (Boolean)this.cinematic.get();
      }
   }

   private double getScaling() {
      double finalZoomFactor = this.currentZoomFactor.get() + this.currentScrollStep.get();
      return this.normalizeZoom.get() ? Math.exp((Double)this.zoomNormalizationFactor.get() * (finalZoomFactor - 1.0)) : finalZoomFactor;
   }
}
