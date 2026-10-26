package nekiplay.meteorplus.features.modules.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class Hunt extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Set<EntityType<?>>> entities = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("entities")).description("Entities to attack.")).onlyAttackable().build());
   private final Setting<Boolean> onGround = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("On ground only"))
                  .description("Attack entities on ground only"))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> customCheck = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("Custom on ground check"))
                  .description("Use a custom on ground check instead of the usual entity.isOnGround. Useful on some servers"))
               .defaultValue(false))
            .build()
      );
   private final ArrayList<Entity> targets = new ArrayList<>();

   public Hunt() {
      super(Categories.Combat, "Hunt", "Automatic walk to selected entities");
   }

   private boolean entityCheck(Entity entity) {
      if (!entity.equals(this.mc.player) && !entity.equals(this.mc.getCameraEntity())) {
         if ((!(entity instanceof LivingEntity) || !((LivingEntity)entity).isDeadOrDying()) && entity.isAlive()) {
            if (!((Set)this.entities.get()).contains(entity.getType())) {
               return false;
            } else if (entity instanceof OwnableEntity tameable
               && tameable.getOwner().getUUID() != null
               && tameable.getOwner().getUUID().equals(this.mc.player.getUUID())) {
               return false;
            } else {
               if (entity instanceof Player player) {
                  if (player.isCreative()) {
                     return false;
                  }

                  if (!Friends.get().shouldAttack(player)) {
                     return false;
                  }

                  AntiBotPlus antiBotPlus = (AntiBotPlus)Modules.get().get(AntiBotPlus.class);
                  Teams teams = (Teams)Modules.get().get(Teams.class);
                  if (antiBotPlus != null && antiBotPlus.isBot(player)) {
                     return false;
                  }

                  if (teams != null && teams.isInYourTeam(player)) {
                     return false;
                  }
               }

               if (!(Boolean)this.onGround.get()) {
                  return true;
               } else if (!(Boolean)this.customCheck.get()) {
                  return entity.onGround();
               } else {
                  Level world = entity.level();
                  Vec3 entityPos = entity.position();
                  BlockPos posBelow = new BlockPos((int)entityPos.x, (int)(entityPos.y - 1.0), (int)entityPos.z);
                  Block blockBelow = world.getBlockState(posBelow).getBlock();
                  return blockBelow != Blocks.AIR && blockBelow != Blocks.WATER && blockBelow != Blocks.LAVA;
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void onDeactivate() {
      this.targets.clear();
      PathManagers.get().stop();
   }

   @EventHandler
   private void onTickEvent(Pre event) {
      if (this.mc.level != null) {
         TargetUtils.getList(this.targets, this::entityCheck, SortPriority.LowestDistance, 25);
         Iterator var2 = this.targets.iterator();
         if (var2.hasNext()) {
            Entity entity = (Entity)var2.next();
            PathManagers.get().moveTo(entity.blockPosition());
            return;
         }
      }
   }
}
