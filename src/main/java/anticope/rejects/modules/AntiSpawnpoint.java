package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;

public class AntiSpawnpoint extends Module {
   private SettingGroup sgDefault = this.settings.getDefaultGroup();
   private Setting<Boolean> fakeUse = this.sgDefault
      .add(((Builder)((Builder)((Builder)new Builder().name("fake-use")).description("Fake using the bed or anchor.")).defaultValue(true)).build());

   public AntiSpawnpoint() {
      super(MeteorRejectsAddon.CATEGORY, "anti-spawnpoint", "Protects the player from losing the respawn point.");
   }

   @EventHandler
   private void onSendPacket(Send event) {
      if (this.mc.level != null) {
         if (event.packet instanceof ServerboundUseItemOnPacket) {
            BlockPos blockPos = ((ServerboundUseItemOnPacket)event.packet).getHitResult().getBlockPos();
            boolean IsOverWorld = this.mc.level.dimension() == Level.OVERWORLD;
            boolean IsNetherWorld = this.mc.level.dimension() == Level.NETHER;
            boolean BlockIsBed = this.mc.level.getBlockState(blockPos).getBlock() instanceof BedBlock;
            boolean BlockIsAnchor = this.mc.level.getBlockState(blockPos).getBlock().equals(Blocks.RESPAWN_ANCHOR);

            assert this.mc.player != null;

            if ((Boolean)this.fakeUse.get()) {
               if (BlockIsBed && IsOverWorld) {
                  this.mc.player.swing(InteractionHand.MAIN_HAND);
                  this.mc.player.absSnapTo(blockPos.getX(), blockPos.above().getY(), blockPos.getZ());
               } else if (BlockIsAnchor && IsNetherWorld) {
                  this.mc.player.swing(InteractionHand.MAIN_HAND);
               }
            }

            if (BlockIsBed && IsOverWorld || BlockIsAnchor && IsNetherWorld) {
               event.cancel();
            }
         }
      }
   }
}
