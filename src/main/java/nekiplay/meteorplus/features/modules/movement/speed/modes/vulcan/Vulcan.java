package nekiplay.meteorplus.features.modules.movement.speed.modes.vulcan;

import java.util.Objects;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedMode;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedModes;
import nekiplay.meteorplus.utils.CustomSpeedUtils;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class Vulcan extends SpeedMode {
   public Item chestPlate;

   public Vulcan() {
      super(SpeedModes.Vulcan);
   }

   @Override
   public void onDeactivate() {
      FindItemResult chest = InvUtils.find(new Item[]{this.chestPlate});
      if (chest.found() && this.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA && (Boolean)this.settings.autoSwapVulcan.get()) {
         InvUtils.move().from(chest.slot()).toArmor(2);
      }
   }

   @Override
   public void onActivate() {
      FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
      if (!elytra.found()) {
         this.settings.error("Elytra not found", new Object[0]);
         this.settings.toggle();
      } else if (!SlotUtils.isArmor(elytra.slot())
         && (Boolean)this.settings.autoSwapVulcan.get()
         && this.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
         this.chestPlate = this.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem();
         InvUtils.move().from(elytra.slot()).toArmor(2);
      }
   }

   @Override
   public void onPlayerMoveEvent(PlayerMoveEvent event) {
      if (this.mc.player != null && this.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
         if (this.mc.player.hasEffect(MobEffects.SPEED) && this.mc.player.getEffect(MobEffects.SPEED) != null) {
            if (Objects.requireNonNull(this.mc.player.getEffect(MobEffects.SPEED)).getAmplifier() == 1) {
               CustomSpeedUtils.applySpeed(event, (Double)this.settings.speedVulcanef2.get());
            } else if (Objects.requireNonNull(this.mc.player.getEffect(MobEffects.SPEED)).getAmplifier() == 0) {
               CustomSpeedUtils.applySpeed(event, (Double)this.settings.speedVulcanef1.get());
            }
         } else {
            CustomSpeedUtils.applySpeed(event, (Double)this.settings.speedVulcanef0.get());
         }
      }
   }
}
