package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.Vec3;

public class BlockIn extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> multiPlace = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("multi-place")).description("Whether to place all blocks in a single tick")).defaultValue(false))
            .build()
      );
   private final Setting<Boolean> center = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("center")).description("Whether to center to avoid obstructing placement")).defaultValue(true))
            .build()
      );
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("rotate")).description("Whether to rotate towards block placements.")).defaultValue(true)).build());
   private final Setting<Boolean> turnOff = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("turn-off")).description("Whether to turn off after finished placing.")).defaultValue(true)).build()
      );
   private final Setting<Boolean> onlyOnGround = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("only-on-ground")).description("Only places when you are standing on blocks (not in midair)."))
               .defaultValue(true))
            .build()
      );
   private final MutableBlockPos bp = new MutableBlockPos();
   private boolean return_;
   private double sY;

   public BlockIn() {
      super(MeteorRejectsAddon.CATEGORY, "block-in", "Block yourself in using any block.");
   }

   public void onActivate() {
      this.sY = this.mc.player.getY();
   }

   @EventHandler
   private void onPreTick(Pre event) {
      if (this.mc.player.onGround() && this.mc.player.getY() > Math.floor(this.mc.player.getY() + 0.2)) {
         this.mc.options.keyShift.setDown(true);
      }

      if (this.return_ && this.mc.options.keyShift.isDown()) {
         this.mc.options.keyShift.setDown(false);
      }

      if ((Boolean)this.center.get()) {
         if (!(Boolean)this.onlyOnGround.get()) {
            this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
            this.mc.player.move(MoverType.SELF, new Vec3(0.0, -(this.sY - Math.floor(this.sY)), 0.0));
         }

         PlayerUtils.centerPlayer();
      }

      if (!(Boolean)this.onlyOnGround.get() || this.mc.player.onGround()) {
         this.return_ = false;
         if ((Boolean)this.multiPlace.get()) {
            boolean p1 = this.place(0, -1, 0);
            boolean p2 = this.place(1, 0, 0);
            boolean p3 = this.place(-1, 0, 0);
            boolean p4 = this.place(0, 0, 1);
            boolean p5 = this.place(0, 0, -1);
            boolean p6 = this.place(1, 1, 0);
            boolean p7 = this.place(-1, 1, 0);
            boolean p8 = this.place(0, 1, 1);
            boolean p9 = this.place(0, 1, -1);
            boolean p10 = this.place(0, 2, 0);
            if ((Boolean)this.turnOff.get() && p1 && p2 && p3 && p4 && p5 && p6 && p7 && p8 && p9 && p10) {
               this.toggle();
            }
         } else {
            boolean p1 = this.place(0, -1, 0);
            if (this.return_) {
               return;
            }

            boolean p2 = this.place(1, 0, 0);
            if (this.return_) {
               return;
            }

            boolean p3 = this.place(-1, 0, 0);
            if (this.return_) {
               return;
            }

            boolean p4 = this.place(0, 0, 1);
            if (this.return_) {
               return;
            }

            boolean p5 = this.place(0, 0, -1);
            if (this.return_) {
               return;
            }

            boolean p6 = this.place(1, 1, 0);
            if (this.return_) {
               return;
            }

            boolean p7 = this.place(-1, 1, 0);
            if (this.return_) {
               return;
            }

            boolean p8 = this.place(0, 1, 1);
            if (this.return_) {
               return;
            }

            boolean p9 = this.place(0, 1, -1);
            if (this.return_) {
               return;
            }

            boolean p10 = this.place(0, 2, 0);
            if ((Boolean)this.turnOff.get() && p1 && p2 && p3 && p4 && p5 && p6 && p7 && p8 && p9 && p10) {
               this.toggle();
            }
         }
      }
   }

   private boolean place(int x, int y, int z) {
      this.setBlockPos(x, y, z);
      FindItemResult findItemResult = InvUtils.findInHotbar(itemStack -> this.validItem(itemStack, this.bp));
      if (!BlockUtils.canPlace(this.bp)) {
         return true;
      } else {
         if (BlockUtils.place(this.bp, findItemResult, (Boolean)this.rotate.get(), 100, true)) {
            this.return_ = true;
         }

         return false;
      }
   }

   private void setBlockPos(int x, int y, int z) {
      this.bp.set(this.mc.player.getX() + x, this.mc.player.getY() + y, this.mc.player.getZ() + z);
   }

   private boolean validItem(ItemStack itemStack, BlockPos pos) {
      if (!(itemStack.getItem() instanceof BlockItem)) {
         return false;
      } else {
         Block block = ((BlockItem)itemStack.getItem()).getBlock();
         return !Block.isShapeFullBlock(block.defaultBlockState().getCollisionShape(this.mc.level, pos))
            ? false
            : !(block instanceof FallingBlock) || !FallingBlock.isFree(this.mc.level.getBlockState(pos));
      }
   }
}
