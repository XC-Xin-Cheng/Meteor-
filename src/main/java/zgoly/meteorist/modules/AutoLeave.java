package zgoly.meteorist.modules;

import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import zgoly.meteorist.Meteorist;

public class AutoLeave extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<List<String>> playerNames = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("player-names")).description("Player names to react on.")).defaultValue(new String[]{"Notch"}).build());
   private final Setting<String> message = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                     .name("message"))
                  .description("Message to show after logging out."))
               .defaultValue("[AutoLeave] Player joined: {player}"))
            .build()
      );
   private final Setting<Boolean> ignoreFriends = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-friends"))
                  .description("Don't react to players added as friends."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> toggleOff = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("toggle-off"))
                  .description("Disables Auto Leave after usage."))
               .defaultValue(false))
            .build()
      );

   public AutoLeave() {
      super(Meteorist.CATEGORY, "auto-leave", "Automatically leaves if player with specific name joins the server.");
   }

   @EventHandler
   private void onTick(Post event) {
      this.mc
         .level
         .players()
         .stream()
         .filter(player -> !(Boolean)this.ignoreFriends.get() || !Friends.get().isFriend(player))
         .filter(player -> ((List)this.playerNames.get()).contains(player.getName().getString()))
         .findFirst()
         .ifPresent(
            player -> {
               this.mc
                  .player
                  .connection
                  .handleDisconnect(
                     new ClientboundDisconnectPacket(Component.nullToEmpty(((String)this.message.get()).replace("{player}", player.getName().getString())))
                  );
               if ((Boolean)this.toggleOff.get()) {
                  this.toggle();
               }
            }
         );
   }
}
