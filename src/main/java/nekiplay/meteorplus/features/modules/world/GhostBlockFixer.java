package nekiplay.meteorplus.features.modules.world;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayDeque;
import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.level.block.state.BlockState;

public class GhostBlockFixer extends Module {
   private final SettingGroup GBSettings = this.settings.getDefaultGroup();
   private final Setting<Integer> delay = this.GBSettings
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("Fixer delay")).description("Delay for block fixing.")).defaultValue(50))
            .range(1, 250)
            .sliderRange(1, 250)
            .build()
      );
   private final Setting<Integer> range = this.GBSettings
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("Fixer range")).description("Range to block.")).defaultValue(6))
            .range(1, 80)
            .sliderRange(1, 80)
            .build()
      );
   private final ArrayDeque<BlockPos> blocks = new ArrayDeque<>();
   private long millis = 0L;

   public GhostBlockFixer() {
      super(Categories.World, "auto-ghost-block-fixer", "Automatically fix ghost blocks.");
   }

   @EventHandler
   public void onBlockBreak(BreakBlockEvent block) {
      this.blocks.add(block.blockPos);
   }

   public void onActivate() {
      this.millis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
   }

   public void onDeactivate() {
      this.blocks.clear();
   }

   @EventHandler
   public void onLeave(GameLeftEvent event) {
      this.blocks.clear();
   }

   @EventHandler
   public void onTick(Post event) {
      if (!this.blocks.isEmpty()) {
         ClientPacketListener conn = this.mc.getConnection();
         LocalPlayer player = this.mc.player;
         if (conn != null && player != null && LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() >= this.millis) {
            BlockPos block = this.blocks.peek();

            assert block != null;

            assert this.mc.level != null;

            double distance = this.mc.player.distanceToSqr(block.getX(), block.getY(), block.getZ());
            BlockState state = this.mc.level.getBlockState(block);
            if (distance <= ((Integer)this.range.get()).intValue() && state.isAir()) {
               this.millis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + ((Integer)this.delay.get()).intValue();
               ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, block, Direction.UP, 0);
               conn.send(packet);
               this.blocks.remove();
            } else if (!state.isAir()) {
               this.blocks.remove();
            }
         }
      }
   }
}
