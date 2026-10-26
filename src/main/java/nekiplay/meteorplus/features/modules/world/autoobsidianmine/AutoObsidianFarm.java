package nekiplay.meteorplus.features.modules.world.autoobsidianmine;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.MixinPlugin;
import nekiplay.meteorplus.features.modules.world.autoobsidianmine.modes.Cauldrons;
import nekiplay.meteorplus.features.modules.world.autoobsidianmine.modes.Portals;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class AutoObsidianFarm extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<AutoObsidianFarmModes> workingMode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)((Builder)new Builder().name("mode")).description("Working mode."))
                     .defaultValue(AutoObsidianFarmModes.Portals_Vanilla))
                  .onModuleActivated(modesSetting -> this.onModeChanged((AutoObsidianFarmModes)modesSetting.get())))
               .onChanged(this::onModeChanged))
            .build()
      );
   public final Setting<BlockPos> mainPortalPosition = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                     .name("portal location 1"))
                  .description("the position of the portal to hell"))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Portals_Vanilla))
            .build()
      );
   public final Setting<BlockPos> twoPortalPosition = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                     .name("portal location 2"))
                  .description("portal position in hell for new portal generations"))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Portals_Vanilla))
            .build()
      );
   public final Setting<String> command = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("command"))
                     .description("Send command."))
                  .defaultValue("/home"))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Portal_Homes))
            .build()
      );
   public final Setting<BlockPos> lavaPlaceLocation = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                     .name("lava-place-location"))
                  .description("the position placing lava"))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Cauldrons))
            .build()
      );
   public final Setting<Integer> delayCommand = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("command-delay"))
                     .description("Ticks delay."))
                  .defaultValue(700))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Portal_Homes))
            .build()
      );
   public final Setting<Integer> delay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("mining-delay"))
                  .description("Mining delay."))
               .defaultValue(3))
            .build()
      );
   public final Setting<Integer> collectDelay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("collect-delay"))
                  .description("Cauldron collecting lava delay."))
               .defaultValue(8))
            .build()
      );
   public final Setting<Integer> lavaPlaceDelay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("lava-place-delay"))
                  .description("Delay for placing lava."))
               .defaultValue(8))
            .build()
      );
   public final Setting<Integer> range = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("range"))
                     .description("Cauldron range's."))
                  .defaultValue(5))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Cauldrons))
            .build()
      );
   public final Setting<Boolean> solidCauldrons = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("solid-cauldrons"))
                     .description("Solid cauldrons."))
                  .defaultValue(false))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Cauldrons))
            .build()
      );
   public final Setting<Boolean> bypassSneak = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("bypass-sneak"))
                     .description("Bypass sneak interact."))
                  .defaultValue(false))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Cauldrons))
            .build()
      );
   public final Setting<Boolean> tpsCheck = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("no-work-if-server-lag"))
                     .description("Stop working if server lagging."))
                  .defaultValue(false))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Cauldrons))
            .build()
      );
   public final Setting<Boolean> noBaritoneBreaking = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("disable-baritone-breaking-if-not-mine-portal"))
                     .description("No break blocks if is not mining portal."))
                  .defaultValue(true))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Portals_Vanilla))
            .build()
      );
   public final Setting<Boolean> noBaritonePlacing = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("disable-baritone-place"))
                     .description("No place blocks."))
                  .defaultValue(true))
               .visible(() -> this.workingMode.get() == AutoObsidianFarmModes.Portals_Vanilla))
            .build()
      );
   public final Setting<Boolean> rotate = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("rotate"))
                     .description("Rotate to breaking block."))
                  .defaultValue(false))
               .visible(() -> this.workingMode.get() != AutoObsidianFarmModes.Cauldrons))
            .build()
      );
   public final Setting<Boolean> swingHand = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("swing-hand"))
                     .description("Swing hand client side."))
                  .defaultValue(true))
               .visible(() -> this.workingMode.get() != AutoObsidianFarmModes.Cauldrons))
            .build()
      );
   public final Setting<Boolean> pauseOnEat = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("pause-on-eat"))
                  .description("Pause farming on eat or auto eat."))
               .defaultValue(true))
            .build()
      );
   private AutoObsidianFarmMode currentMode;

   public AutoObsidianFarm() {
      super(Categories.World, "Auto-obsidian-farm", "Automatically farm obsidian in AFK.");
   }

   private void onModeChanged(AutoObsidianFarmModes mode) {
      switch (mode) {
         case Portal_Homes:
         case Portals_Vanilla:
            if (MixinPlugin.isBaritonePresent) {
               this.currentMode = new Portals();
            } else {
               this.error("This mode need Baritone API (Fabric)", new Object[0]);
            }
            break;
         case Cauldrons:
            this.currentMode = new Cauldrons();
      }
   }

   public String getInfoString() {
      return this.currentMode == null ? "" : this.currentMode.getInfoString();
   }

   public void onActivate() {
      if (this.currentMode != null) {
         this.currentMode.onActivate();
      }
   }

   public void onDeactivate() {
      if (this.currentMode != null) {
         this.currentMode.onDeactivate();
      }
   }

   @EventHandler
   private void onPreTickPost(Post event) {
      if (this.currentMode != null) {
         this.currentMode.onTickEventPost(event);
      }
   }

   @EventHandler
   private void onPreTick(Pre event) {
      if (this.currentMode != null) {
         this.currentMode.onTickEventPre(event);
      }
   }

   @EventHandler
   private void onCollisionShape(CollisionShapeEvent event) {
      if (this.currentMode != null) {
         this.currentMode.onCollisionShape(event);
      }
   }

   @EventHandler
   private void onMovePacket(Send event) {
      if (event.packet instanceof ServerboundMovePlayerPacket playerMove && this.currentMode != null) {
         this.currentMode.onMovePacket(playerMove);
      }
   }
}
