package anticope.rejects.mixin;

import anticope.rejects.modules.SilentDisconnect;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientCommonPacketListenerImpl.class})
public class ClientCommonNetworkHandlerMixin {
   @Inject(
      method = {"onDisconnect"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDisconnected(DisconnectionDetails info, CallbackInfo ci) {
      if (Modules.get().isActive(SilentDisconnect.class) && MeteorClient.mc.level != null && MeteorClient.mc.player != null) {
         ChatUtils.info(Component.translatable("disconnect.lost").getString() + ":", new Object[0]);
         ChatUtils.sendMsg(info.reason());
         ci.cancel();
      }
   }
}
