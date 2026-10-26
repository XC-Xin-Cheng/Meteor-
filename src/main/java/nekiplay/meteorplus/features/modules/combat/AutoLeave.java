package nekiplay.meteorplus.features.modules.combat;

import java.util.Objects;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.world.entity.player.Player;

public class AutoLeave extends Module {
   private final SettingGroup ALSettings = this.settings.getDefaultGroup();
   private final Setting<Boolean> visualRangeIgnoreFriends = this.ALSettings
      .add(((Builder)((Builder)((Builder)new Builder().name("ignore-friends")).description("Ignores friends.")).defaultValue(true)).build());
   private final Setting<Boolean> AutoDisable = this.ALSettings
      .add(((Builder)((Builder)((Builder)new Builder().name("auto-disable")).description("Disables function after player detect.")).defaultValue(true)).build());
   private final Setting<Boolean> Command = this.ALSettings
      .add(((Builder)((Builder)((Builder)new Builder().name("command")).description("Send command instead of leave.")).defaultValue(false)).build());
   private final Setting<String> command_str = this.ALSettings
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                        .name("command:"))
                     .description("Send command in chat."))
                  .defaultValue("/spawn"))
               .visible(this.Command::get))
            .build()
      );

   public AutoLeave() {
      super(Categories.Combat, "auto-leave", "Automatically logs out from the server when someone enters your render distance.");
   }

   @EventHandler
   public void onEntityAdded(EntityAddedEvent event) {
      if (this.mc.player != null) {
         if ((Boolean)this.visualRangeIgnoreFriends.get()) {
            if (event.entity.isAlwaysTicking()
               && !Friends.get().isFriend((Player)event.entity)
               && !Objects.equals(event.entity.getName(), this.mc.player.getName())
               && !Objects.equals(event.entity.getName(), "FreeCamera")) {
               if ((Boolean)this.Command.get()) {
                  ChatUtils.sendPlayerMsg((String)this.command_str.get());
                  this.info(String.format("player §c%s§r was detected", event.entity.getName()), new Object[0]);
               } else {
                  assert this.mc.level != null;

                  this.mc.level.disconnect(Component.nullToEmpty(""));
                  this.mc
                     .player
                     .connection
                     .handleDisconnect(
                        new ClientboundDisconnectPacket(Component.literal(String.format("[§dAuto Leave§r] player %s was detected", event.entity.getName())))
                     );
               }

               if ((Boolean)this.AutoDisable.get()) {
                  this.toggle();
               }
            }
         } else if (event.entity.isAlwaysTicking()) {
            this.mc
               .player
               .connection
               .handleDisconnect(
                  new ClientboundDisconnectPacket(Component.literal(String.format("[§dAuto Leave§r] player %s was detected", event.entity.getName())))
               );
            if ((Boolean)this.AutoDisable.get()) {
               this.toggle();
            }
         }
      }
   }
}
