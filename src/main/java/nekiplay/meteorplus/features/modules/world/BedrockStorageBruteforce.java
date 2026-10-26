package nekiplay.meteorplus.features.modules.world;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BedrockStorageBruteforce extends Module {
   public ArrayList<BlockPos> scanned = new ArrayList<>();
   private final SettingGroup SCSettings = this.settings.createGroup("Scanner Settings");
   public final Setting<Boolean> clear_cache_blocks = this.SCSettings
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("clear-cache")).description("clear-saved-cache.")).defaultValue(false))
               .onChanged(a -> this.scanned.clear()))
            .build()
      );
   private final Setting<Integer> range = this.SCSettings
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("Scan range"))
                  .description("Bruteforce delay min ."))
               .defaultValue(8))
            .min(3)
            .sliderRange(8, 64)
            .build()
      );
   private final Setting<Integer> delaymin = this.SCSettings
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("Scan delay min"))
                  .description("Bruteforce delay min ."))
               .defaultValue(24))
            .min(0)
            .sliderRange(0, 80)
            .build()
      );
   private final Setting<Integer> delaymax = this.SCSettings
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("Scan delay max"))
                  .description("Bruteforce delay max ."))
               .defaultValue(25))
            .min(0)
            .sliderRange(0, 80)
            .build()
      );
   Thread clickerThread = null;
   private boolean scan = true;
   Dimension dim;
   BlockPos last = null;
   long millis = 0L;

   public BedrockStorageBruteforce() {
      super(Categories.World, "bedrock-storage-BF", "Scan storage's in bedrock from -1 to -4 height for 1.12.2.");
   }

   private void stop() {
      if (this.clickerThread != null && this.clickerThread.isAlive()) {
      }

      this.scan = false;
   }

   public void onDeactivate() {
      this.stop();
   }

   private boolean isAllowScan(BlockPos pos) {
      if (this.mc.level != null && !this.scanned.contains(pos)) {
         BlockState state = this.mc.level.getBlockState(pos);
         Block block = state.getBlock();
         return block != Blocks.AIR && block != Blocks.BEDROCK && block != Blocks.LAVA && block != Blocks.WATER;
      } else {
         return false;
      }
   }

   public void onActivate() {
      this.dim = PlayerUtils.getDimension();
      this.scan = true;
      this.clickerThread = new Thread(
         () -> {
            while (this.scan) {
               assert this.mc.player != null;

               BlockPos playerPos = this.mc.player.blockPosition();
               int ranger = (Integer)this.range.get();
               int x = Utils.random(playerPos.getX() - ranger, playerPos.getX() + ranger);
               int y = Utils.random(1, 4);
               int z = Utils.random(playerPos.getZ() - ranger, playerPos.getZ() + ranger);
               BlockPos possible = new BlockPos(x, y, z);
               if (LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() >= this.millis && this.isAllowScan(possible)) {
                  ClientPacketListener conn = this.mc.getConnection();
                  if (conn != null) {
                     this.last = possible;
                     this.scanned.add(possible);
                     ServerboundPlayerActionPacket abortPacket = new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, possible, Direction.UP, 0);
                     conn.send(abortPacket);
                     ServerboundPlayerActionPacket abortPacket2 = new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, possible, Direction.UP, 0);
                     conn.send(abortPacket2);
                     this.millis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        + Utils.random((Integer)this.delaymin.get(), (Integer)this.delaymax.get());
                  }
               }
            }
         }
      );
      this.clickerThread.start();
      this.millis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if (this.last != null) {
         BlockPos bp = this.last;

         assert this.mc.level != null;

         BlockState state = this.mc.level.getBlockState(bp);
         VoxelShape shape = state.getShape(this.mc.level, bp);
         SettingColor color = new SettingColor(255, 255, 255);
         if (shape.isEmpty()) {
            return;
         }

         for (AABB b : shape.toAabbs()) {
            event.renderer
               .box(
                  bp.getX() + b.minX,
                  bp.getY() + b.minY,
                  bp.getZ() + b.minZ,
                  bp.getX() + b.maxX,
                  bp.getY() + b.maxY,
                  bp.getZ() + b.maxZ,
                  new SettingColor(255, 255, 255, 255),
                  color,
                  ShapeMode.Lines,
                  0
               );
         }

         event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, bp.getX(), bp.getY(), bp.getZ(), color);
      }
   }

   @EventHandler
   private void onGameLeft(GameLeftEvent event) {
      this.scanned.clear();
      this.stop();
   }

   @EventHandler
   private void onTickPre(Pre event) {
      if (PlayerUtils.getDimension() != this.dim) {
         this.dim = PlayerUtils.getDimension();
         this.scanned.clear();
      }
   }
}
