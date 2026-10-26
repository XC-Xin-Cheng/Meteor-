package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.StopUsingItemEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ArrowDmg extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<Double> strength = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("strength")).description("More strength = higher damage.")).defaultValue(5.0).min(0.1).sliderMax(25.0).build()
      );
   public final Setting<Boolean> tridents = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("tridents"))
                  .description(
                     "When enabled, tridents fly much further. Doesn't seem to affect damage or Riptide. WARNING: You can easily lose your trident by enabling this option!"
                  ))
               .defaultValue(false))
            .build()
      );

   public ArrowDmg() {
      super(
         MeteorRejectsAddon.CATEGORY,
         "arrow-damage",
         "Massively increases arrow damage, but reduces accuracy and consume more hunger. Does not work with crossbows and is patched on Paper servers."
      );
   }

   @EventHandler
   private void onStopUsingItem(StopUsingItemEvent event) {
      if (this.isValidItem(event.itemStack.getItem())) {
         LocalPlayer p = this.mc.player;
         p.connection.send(new ServerboundPlayerCommandPacket(p, Action.START_SPRINTING));
         double x = p.getX();
         double y = p.getY();
         double z = p.getZ();
         double adjustedStrength = (Double)this.strength.get() / 10.0 * Math.sqrt(500.0);
         Vec3 lookVec = p.getViewVector(1.0F).scale(adjustedStrength);

         for (int i = 0; i < 4; i++) {
            this.sendPos(x, y, z, true);
         }

         this.sendPos(x - lookVec.x, y, z - lookVec.z, true);
         this.sendPos(x, y, z, false);
      }
   }

   private void sendPos(double x, double y, double z, boolean onGround) {
      ClientPacketListener clientPacketListener = this.mc.player.connection;
      clientPacketListener.send(new Pos(x, y, z, onGround, this.mc.player.horizontalCollision));
   }

   private boolean isValidItem(Item item) {
      return (Boolean)this.tridents.get() && item == Items.TRIDENT || item == Items.BOW;
   }
}
