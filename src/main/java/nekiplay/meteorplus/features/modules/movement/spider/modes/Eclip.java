package nekiplay.meteorplus.features.modules.movement.spider.modes;

import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import nekiplay.meteorplus.features.modules.movement.spider.SpiderMode;
import nekiplay.meteorplus.features.modules.movement.spider.SpiderModes;
import nekiplay.meteorplus.utils.ElytraUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class Eclip extends SpiderMode {
   private int ticks = 0;
   private int slot = -1;
   private double blocks = 0.0;

   public Eclip() {
      super(SpiderModes.Elytra_clip);
   }

   @Override
   public void onActivate() {
      FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
      if (!elytra.found()) {
         this.settings.error(Names.get(Items.ELYTRA) + " not found", new Object[0]);
         this.settings.toggle();
      }
   }

   @Override
   public void onTickEventPre(Pre event) {
      if (this.work() && this.mc.player.horizontalCollision) {
         this.blocks = ((Integer)this.settings.Blocks.get()).intValue();
         this.clip();
      } else {
         this.ticks = 0;
      }
   }

   private boolean work() {
      LocalPlayer player = this.mc.player;

      assert player != null;

      FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
      return elytra.found();
   }

   private void clip() {
      if (this.blocks != 0.0) {
         LocalPlayer player = this.mc.player;

         assert player != null;

         switch (this.ticks) {
            case 0:
               FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
               this.slot = elytra.slot();
               InvUtils.move().from(this.slot).toArmor(2);
               this.ticks++;
            case 1:
               this.mc.player.connection.send(new StatusOnly(false, this.mc.player.horizontalCollision));
               this.ticks++;
            case 2:
               this.mc.player.connection.send(new StatusOnly(false, this.mc.player.horizontalCollision));
               this.ticks++;
            case 3:
               ElytraUtils.startFly();
               this.ticks++;
            case 4:
               player.setPos(player.getX(), player.getY() + this.blocks, player.getZ());
               this.mc.player.connection.send(new Pos(player.getX(), player.getY() + this.blocks, player.getZ(), false, this.mc.player.horizontalCollision));
               this.ticks++;
            case 5:
               ElytraUtils.startFly();
               this.ticks++;
            case 6:
               this.ticks = 0;
               this.blocks = 0.0;
               InvUtils.move().fromArmor(2).to(this.slot);
         }
      }
   }
}
