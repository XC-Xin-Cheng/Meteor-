package zgoly.meteorist.modules;

import java.util.Objects;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import zgoly.meteorist.Meteorist;

public class AutoSleep extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Integer> bedSearchRadius = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("bed-search-radius")).description("Radius to search for beds.")).defaultValue(3))
            .min(1)
            .sliderRange(1, 4)
            .build()
      );
   private final Setting<AutoSleep.SleepMode> sleepMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("sleep-mode"))
                  .description("'Default' - sleep every amount of ticks; 'WhenPlayerLiesOnBed' - sleep when other player lies down on bed."))
               .defaultValue(AutoSleep.SleepMode.Default))
            .build()
      );
   private final Setting<Integer> sleepDelay = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("sleep-delay")).description("Delay between tries to sleep in ticks (20 ticks = 1 sec)."))
                  .defaultValue(200))
               .min(1)
               .sliderRange(1, 1200)
               .visible(() -> this.sleepMode.get() == AutoSleep.SleepMode.Default))
            .build()
      );
   private final Setting<Boolean> atNight = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("at-night"))
                     .description("Sleep only at night."))
                  .defaultValue(true))
               .visible(() -> this.sleepMode.get() == AutoSleep.SleepMode.Default))
            .build()
      );
   private final Setting<Boolean> atThunderstorm = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("at-thunderstorm"))
                     .description("Sleep only at thunderstorm."))
                  .defaultValue(true))
               .visible(() -> this.sleepMode.get() == AutoSleep.SleepMode.Default))
            .build()
      );
   private final Setting<Boolean> useMaxSleepTime = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("use-max-sleep-time"))
                  .description("Use maximum sleep time."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Integer> maxSleepTime = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("max-sleep-time")).description("Maximum time to sleep in ticks (20 ticks = 1 sec)."))
                  .defaultValue(200))
               .min(1)
               .sliderRange(1, 1200)
               .visible(this.useMaxSleepTime::get))
            .build()
      );
   private final Setting<Boolean> dimensionRestrict = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("dimension-restrict"))
                  .description("Don't go to bed if you're in a dimension like nether/end."))
               .defaultValue(true))
            .build()
      );
   private int sleepDelayTimer;
   private int sleepTimer;

   public AutoSleep() {
      super(Meteorist.CATEGORY, "auto-sleep", "Gets into bed automatically for you.");
   }

   public void onActivate() {
      this.sleepDelayTimer = (Integer)this.sleepDelay.get();
      this.sleepTimer = 0;
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.mc.level != null && (!(Boolean)this.dimensionRestrict.get() || this.mc.level.dimensionType().hasSkyLight())) {
         if (this.mc.player.isSleeping()) {
            if ((Boolean)this.useMaxSleepTime.get() && ++this.sleepTimer >= (Integer)this.maxSleepTime.get()) {
               this.sleepTimer = 0;
               this.mc.getConnection().send(new ServerboundPlayerCommandPacket(this.mc.player, Action.STOP_SLEEPING));
            }
         } else if (this.sleepMode.get() == AutoSleep.SleepMode.Default) {
            if (++this.sleepDelayTimer < (Integer)this.sleepDelay.get()) {
               return;
            }

            this.sleepDelayTimer = (Integer)this.sleepDelay.get();
            boolean shouldWait = false;
            if ((Boolean)this.atNight.get() && (Boolean)this.atThunderstorm.get()) {
               shouldWait = this.mc.level.isBrightOutside() && !this.mc.level.isThundering();
            } else if ((Boolean)this.atNight.get()) {
               shouldWait = this.mc.level.isBrightOutside();
            } else if ((Boolean)this.atThunderstorm.get()) {
               shouldWait = !this.mc.level.isThundering();
            }

            if (shouldWait) {
               return;
            }

            if (this.sleepInNearestBed((Integer)this.bedSearchRadius.get())) {
               this.sleepDelayTimer = 0;
            }
         }
      }
   }

   private boolean sleepInNearestBed(int radius) {
      for (BlockPos blockPos : BlockPos.withinManhattan(this.mc.player.blockPosition(), radius, radius, radius)) {
         if (this.mc.level.getBlockState(blockPos).getBlock() instanceof BedBlock) {
            this.sleepTimer = 0;
            BlockUtils.interact(new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, true), InteractionHand.MAIN_HAND, false);
            return true;
         }
      }

      return false;
   }

   @EventHandler
   public void onReceiveMessage(Receive event) {
      if (this.sleepMode.get() == AutoSleep.SleepMode.WhenPlayerLiesOnBed
         && event.packet instanceof ClientboundSystemChatPacket packet
         && packet.content().getContents() instanceof TranslatableContents translatableText
         && Objects.equals(translatableText.getKey(), "sleep.players_sleeping")) {
         this.sleepInNearestBed((Integer)this.bedSearchRadius.get());
      }
   }

   public static enum SleepMode {
      Default,
      WhenPlayerLiesOnBed;
   }
}
