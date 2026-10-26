package nekiplay.meteorplus.features.modules.combat;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class Teams extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> scoreBoardTeam = this.sgGeneral.add(((Builder)((Builder)new Builder().name("Scoreboard-Team")).defaultValue(true)).build());
   private final Setting<Boolean> colorTeam = this.sgGeneral.add(((Builder)((Builder)new Builder().name("color")).defaultValue(true)).build());
   private final Setting<Boolean> gommeSkyWars = this.sgGeneral.add(((Builder)((Builder)new Builder().name("GommeHD-SkyWars")).defaultValue(false)).build());

   public Teams() {
      super(Categories.Combat, "teams", "Check if [entity] is in your own team using scoreboard, name color or team prefix.");
   }

   public boolean isInYourTeam(Entity entity) {
      return entity instanceof LivingEntity ? this.isInYourTeam((LivingEntity)entity) : false;
   }

   public boolean isInYourTeam(LivingEntity entity) {
      LocalPlayer player = this.mc.player;
      if (player == null) {
         return false;
      } else if (!this.isActive()) {
         return false;
      } else if ((Boolean)this.scoreBoardTeam.get() && player.getTeam() != null && entity.getTeam() != null && player.isAlliedTo(entity.getTeam())) {
         return true;
      } else {
         Component displayName = player.getDisplayName();
         if ((Boolean)this.gommeSkyWars.get() && displayName != null && entity.getDisplayName() != null) {
            String targetName = entity.getDisplayName().getString().replaceAll("§r", "");
            String clientName = displayName.getString().replaceAll("§r", "");
            if (targetName.startsWith("T") && clientName.startsWith("T") && Character.isDigit(targetName.charAt(1)) && Character.isDigit(clientName.charAt(1))) {
               return targetName.charAt(1) == clientName.charAt(1);
            }
         }

         if ((Boolean)this.colorTeam.get() && displayName != null && entity.getDisplayName() != null) {
            String targetName = entity.getDisplayName().getString().replaceAll("§r", "");
            String clientName = displayName.getString().replaceAll("§r", "");
            return targetName.startsWith("§" + clientName.charAt(1));
         } else {
            return false;
         }
      }
   }
}
