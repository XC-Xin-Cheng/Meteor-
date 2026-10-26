package nekiplay.meteorplus.features.modules.movement.speed;

import meteordevelopment.meteorclient.events.entity.player.JumpVelocityMultiplierEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;

public class SpeedMode {
   protected final Minecraft mc;
   protected final SpeedPlus settings = (SpeedPlus)Modules.get().get(SpeedPlus.class);
   private final SpeedModes type;

   public SpeedMode(SpeedModes type) {
      this.mc = Minecraft.getInstance();
      this.type = type;
   }

   public void onReceivePacket(Receive event) {
   }

   public void onSendPacket(Send event) {
   }

   public void onSentPacket(Sent event) {
   }

   public void onPlayerMoveEvent(PlayerMoveEvent event) {
   }

   public void onTickEventPre(Pre event) {
   }

   public void onTickEventPost(Post event) {
   }

   public void onJump(JumpVelocityMultiplierEvent event) {
   }

   public void onActivate() {
   }

   public void onDeactivate() {
   }

   protected double getDefaultSpeed() {
      double defaultSpeed = 0.2873;
      if (this.mc.player.hasEffect(MobEffects.SPEED)) {
         int amplifier = this.mc.player.getEffect(MobEffects.SPEED).getAmplifier();
         defaultSpeed *= 1.0 + 0.2 * (amplifier + 1);
      }

      if (this.mc.player.hasEffect(MobEffects.SLOWNESS)) {
         int amplifier = this.mc.player.getEffect(MobEffects.SLOWNESS).getAmplifier();
         defaultSpeed /= 1.0 + 0.2 * (amplifier + 1);
      }

      return defaultSpeed;
   }
}
