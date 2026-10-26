package com.stash.hunt.modules;

import com.stash.hunt.Addon;
import com.stash.hunt.Utils;
import java.util.LinkedList;
import java.util.Queue;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import xaeroplus.module.ModuleManager;
import xaeroplus.module.impl.Drawing;

public class TrailMaker extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> recording = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("toggle-recording")).description("Enabled = recording, Disabled = not recording."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Double> rotationScaling = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("rotation-scaling"))
               .description("Scaling of how fast the yaw changes. 1 = instant, 0 = doesn't change"))
            .defaultValue(0.1)
            .sliderRange(0.0, 1.0)
            .build()
      );
   public ResourceKey<Level> dimension;
   public final Queue<ChunkPos> points = new LinkedList<>();
   private boolean following = false;

   public TrailMaker() {
      super(Addon.CATEGORY, "trail-maker", "Allows you to plot xaero chunk highlights on the map and then follow them in order.");
   }

   public void onActivate() {
      if (this.points.isEmpty()) {
         this.info("You haven't added any points, toggle the recording setting and add chunk highlights on the xaero map.", new Object[0]);
      }
   }

   public void onDeactivate() {
      this.following = false;
      this.recording.set(false);
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      WTable buttonTable = (WTable)list.add(theme.table()).widget();
      WButton startFollowing = (WButton)buttonTable.add(theme.button(this.following ? "Stop Following" : "Start Following")).widget();
      startFollowing.action = () -> {
         this.following = !this.following;
         this.recording.set(false);
      };
      WButton clearPoints = (WButton)buttonTable.add(theme.button("Clear Points")).widget();
      clearPoints.action = () -> {
         this.points.clear();
         this.dimension = null;
      };
      return list;
   }

   public boolean isRecording() {
      return this.isActive() && (Boolean)this.recording.get();
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null) {
         if (this.following) {
            if (this.points.isEmpty()) {
               this.info("Finished following the points! Disabling.", new Object[0]);
               this.toggle();
               return;
            }

            ChunkPos goal = this.points.peek();
            Vec3 centerBlockPos = goal.getMiddleBlockPosition((int)this.mc.player.getY()).getCenter();
            if (this.dimension.equals(Level.NETHER) && !this.mc.level.dimension().equals(Level.NETHER)) {
               centerBlockPos = centerBlockPos.multiply(8.0, 1.0, 8.0);
            } else if (this.mc.level.dimension().equals(Level.NETHER) && !this.dimension.equals(Level.NETHER)) {
               centerBlockPos = centerBlockPos.multiply(0.125, 1.0, 0.125);
            }

            float targetYaw = (float)Rotations.getYaw(centerBlockPos);
            this.mc.player.setYRot(Utils.smoothRotation(this.mc.player.getYRot(), targetYaw, (Double)this.rotationScaling.get()));
            if (this.mc.player.position().distanceToSqr(centerBlockPos) < 256.0) {
               ChunkPos point = this.points.poll();
               ((Drawing)ModuleManager.getModule(Drawing.class)).drawingCache.removeHighlight(point.x(), point.z(), this.dimension);
            }
         }
      }
   }
}
