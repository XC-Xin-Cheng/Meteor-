package nekiplay.meteorplus.features.modules.movement.nofall.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixin.ServerboundMovePlayerPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IServerboundMovePlayerPacket;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallMode;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallModes;
import nekiplay.meteorplus.utils.ElytraUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class Eclip extends NoFallMode {
   private int ticks = 0;
   private int slot = -1;
   private int blocks = 0;
   private boolean cliped = false;
   private boolean groundcheck = false;
   private final int timer = 0;
   private int teleports = 0;

   public Eclip() {
      super(NoFallModes.Elytra_Clip);
   }

   @Override
   public void onTickEventPre(Pre event) {
      FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
      if (!elytra.found()) {
         ChatUtils.error("Elytra not found", new Object[0]);
         this.settings.toggle();
      } else {
         if (this.mc.player.onGround() && this.groundcheck) {
            this.groundcheck = false;
            this.cliped = false;
            ChatUtils.infoPrefix("No Fall Plus", "Grounded in " + this.teleports + " teleports", new Object[0]);
            this.mc.player.fallDistance = 0.0;
            this.teleports = 0;
         } else if (this.mc.player.fallDistance > 3.0) {
            BlockHitResult result = this.mc
               .level
               .clip(new ClipContext(this.mc.player.position(), this.mc.player.position().subtract(0.0, 10.0, 0.0), Block.OUTLINE, Fluid.NONE, this.mc.player));
            if (result != null && result.getType() == Type.BLOCK) {
               this.blocks = result.getBlockPos().offset(0, 1, 0).getY();
               this.cliped = true;
            } else if (result == null || result.getType() == Type.MISS) {
               this.blocks = (int)this.mc.player.position().y - 10;
               this.cliped = true;
            }
         }

         if (this.cliped) {
            this.clip();
         }
      }
   }

   @Override
   public void onSendPacket(Send event) {
      if (this.groundcheck) {
         if (event.packet instanceof ServerboundMovePlayerPacket && ((IServerboundMovePlayerPacket)event.packet).meteor$getTag() != 1337) {
            ((ServerboundMovePlayerPacketAccessor)event.packet).meteor$setOnGround(true);
         }
      }
   }

   private void clip() {
      if (this.blocks != 0) {
         LocalPlayer player = this.mc.player;

         assert player != null;

         switch (this.ticks) {
            case 0:
               FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
               this.slot = elytra.slot();
               InvUtils.move().from(this.slot).toArmor(2);
               this.ticks++;
            case 1:
               this.groundcheck = true;
               this.mc.player.connection.send(new StatusOnly(false, this.mc.player.horizontalCollision));
               this.ticks++;
            case 2:
               this.mc.player.connection.send(new StatusOnly(false, this.mc.player.horizontalCollision));
               this.ticks++;
            case 3:
               ElytraUtils.startFly();
               this.ticks++;
            case 4:
               player.setPos(player.getX(), this.blocks, player.getZ());
               this.mc.player.connection.send(new Pos(player.getX(), this.blocks, player.getZ(), true, this.mc.player.horizontalCollision));
               this.teleports++;
               this.ticks++;
            case 5:
               this.ticks = 0;
               InvUtils.move().fromArmor(2).to(this.slot);
         }
      }
   }
}
