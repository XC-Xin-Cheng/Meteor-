package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;

public class AntiBot extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgFilters = this.settings.createGroup("Filters");
   private final Setting<Boolean> removeInvisible = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("remove-invisible")).description("Removes bot only if they are invisible.")).defaultValue(true))
            .build()
      );
   private final Setting<Boolean> gameMode = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("null-gamemode")).description("Removes players without a gamemode")).defaultValue(true)).build());
   private final Setting<Boolean> api = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("null-entry")).description("Removes players without a player entry")).defaultValue(true)).build());
   private final Setting<Boolean> profile = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("null-profile")).description("Removes players without a game profile")).defaultValue(true)).build());
   private final Setting<Boolean> latency = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("ping-check")).description("Removes players using ping check")).defaultValue(false)).build());
   private final Setting<Boolean> nullException = this.sgFilters
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("null-exception")).description("Removes players if a NullPointerException occurred"))
               .defaultValue(false))
            .build()
      );

   public AntiBot() {
      super(MeteorRejectsAddon.CATEGORY, "anti-bot", "Detects and removes bots.");
   }

   @EventHandler
   public void onTick(Post tickEvent) {
      for (Entity entity : this.mc.level.entitiesForRendering()) {
         if (entity instanceof Player playerEntity && (!(Boolean)this.removeInvisible.get() || entity.isInvisible()) && this.isBot(playerEntity)) {
            entity.remove(RemovalReason.DISCARDED);
         }
      }
   }

   private boolean isBot(Player entity) {
      try {
         if ((Boolean)this.gameMode.get() && EntityUtils.getGameMode(entity) == null) {
            return true;
         }

         if ((Boolean)this.api.get() && this.mc.getConnection().getPlayerInfo(entity.getUUID()) == null) {
            return true;
         }

         if ((Boolean)this.profile.get() && this.mc.getConnection().getPlayerInfo(entity.getUUID()).getProfile() == null) {
            return true;
         }

         if ((Boolean)this.latency.get() && this.mc.getConnection().getPlayerInfo(entity.getUUID()).getLatency() > 1) {
            return true;
         }
      } catch (NullPointerException var3) {
         if ((Boolean)this.nullException.get()) {
            return true;
         }
      }

      return false;
   }
}
