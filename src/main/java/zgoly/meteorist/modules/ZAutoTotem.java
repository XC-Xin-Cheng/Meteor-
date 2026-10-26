package zgoly.meteorist.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import zgoly.meteorist.Meteorist;

public class ZAutoTotem extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<ZAutoTotem.Mode> mode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("mode")).description("Determines when to hold a totem, strict will always hold."))
               .defaultValue(ZAutoTotem.Mode.Smart))
            .build()
      );
   private final Setting<Integer> delay = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("delay"))
                  .description("The ticks between slot movements."))
               .defaultValue(0))
            .min(0)
            .build()
      );
   private final Setting<Integer> health = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("health"))
                     .description("The health to hold a totem at."))
                  .defaultValue(10))
               .range(0, 36)
               .sliderMax(36)
               .visible(() -> this.mode.get() == ZAutoTotem.Mode.Smart))
            .build()
      );
   private final Setting<Boolean> elytra = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("elytra"))
                     .description("Will always hold a totem when flying with elytra."))
                  .defaultValue(true))
               .visible(() -> this.mode.get() == ZAutoTotem.Mode.Smart))
            .build()
      );
   private final Setting<Boolean> fall = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("fall"))
                     .description("Will hold a totem when fall damage could kill you."))
                  .defaultValue(true))
               .visible(() -> this.mode.get() == ZAutoTotem.Mode.Smart))
            .build()
      );
   private final Setting<Boolean> explosion = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("explosion"))
                     .description("Will hold a totem when explosion damage could kill you."))
                  .defaultValue(true))
               .visible(() -> this.mode.get() == ZAutoTotem.Mode.Smart))
            .build()
      );
   private final Setting<Boolean> onlyInInventory = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("only-in-inventory"))
                  .description("Only moves the totem if your inventory is open."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> onlyWhenStill = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("only-when-still"))
                  .description("Only moves the totem if you stop moving"))
               .defaultValue(false))
            .build()
      );
   public boolean locked;
   private int totems;
   private int ticks;

   public ZAutoTotem() {
      super(Meteorist.CATEGORY, "z-auto-totem", "Auto totem with some extra options (by Rapou7)");
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(Pre event) {
      FindItemResult result = InvUtils.find(new Item[]{Items.TOTEM_OF_UNDYING});
      this.totems = result.count();
      if (this.totems <= 0) {
         this.locked = false;
      } else if (this.ticks >= (Integer)this.delay.get()) {
         boolean low = this.mc.player.getHealth()
               + this.mc.player.getAbsorptionAmount()
               - PlayerUtils.possibleHealthReductions((Boolean)this.explosion.get(), (Boolean)this.fall.get())
            <= ((Integer)this.health.get()).intValue();
         boolean ely = (Boolean)this.elytra.get()
            && this.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA
            && this.mc.player.isFallFlying();
         this.locked = this.mode.get() == ZAutoTotem.Mode.Strict || this.mode.get() == ZAutoTotem.Mode.Smart && (low || ely);
         if (this.locked && this.mc.player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) {
            if ((Boolean)this.onlyInInventory.get() && this.mc.screen == null) {
               return;
            }

            if ((Boolean)this.onlyWhenStill.get() && this.mc.player.input.hasForwardImpulse()) {
               return;
            }

            InvUtils.move().from(result.slot()).toOffhand();
         }

         this.ticks = 0;
         return;
      }

      this.ticks++;
   }

   @EventHandler(
      priority = 100
   )
   private void onReceivePacket(Receive event) {
      if (event.packet instanceof ClientboundEntityEventPacket p) {
         if (p.getEventId() == 35) {
            Entity entity = p.getEntity(this.mc.level);
            if (entity != null && entity.equals(this.mc.player)) {
               this.ticks = 0;
            }
         }
      }
   }

   public boolean isLocked() {
      return this.isActive() && this.locked;
   }

   public String getInfoString() {
      return String.valueOf(this.totems);
   }

   public static enum Mode {
      Smart,
      Strict;
   }
}
