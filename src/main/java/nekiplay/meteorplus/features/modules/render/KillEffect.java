package nekiplay.meteorplus.features.modules.render;

import java.util.ArrayList;
import java.util.Iterator;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

public class KillEffect extends Module {
   public final ArrayList<Entity> entityList = new ArrayList<>();
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<KillEffect.Effects> mode = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("effect")).description("The method of applying fly.")).defaultValue(KillEffect.Effects.Lighting_Bolt))
            .build()
      );

   public KillEffect() {
      super(Categories.Render, "kill-effect", "Render kill effect");
   }

   @EventHandler
   public void onTickEvent(Post event) {
      Iterator var2 = this.mc.level.entitiesForRendering().iterator();

      while (true) {
         Entity entity;
         label39:
         while (true) {
            if (!var2.hasNext()) {
               return;
            }

            entity = (Entity)var2.next();
            if (entity instanceof LivingEntity livingEntity
               && (livingEntity.deathTime > 0 || livingEntity.getHealth() <= 0.0F)
               && !this.entityList.contains(entity)) {
               switch ((KillEffect.Effects)this.mode.get()) {
                  case Lighting_Bolt:
                     LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, this.mc.level);
                     lightning.snapTo(livingEntity.position());
                     this.mc.level.addEntity(lightning);
                     break label39;
                  case Falling_Lava:
                     for (int i = 0; i < entity.getBbHeight() * 10.0F; i++) {
                        for (int j = 0; j < entity.getBbWidth() * 10.0F; j++) {
                           for (int k = 0; k < entity.getBbWidth() * 10.0F; k++) {
                              this.mc
                                 .level
                                 .addParticle(
                                    ParticleTypes.FALLING_LAVA, entity.getX() + j * 0.1, entity.getY() + i * 0.1, entity.getZ() + k * 0.1, 0.0, 0.0, 0.0
                                 );
                           }
                        }
                     }
                  default:
                     break label39;
               }
            }
         }

         this.entityList.add(entity);
      }
   }

   public static enum Effects {
      Lighting_Bolt,
      Falling_Lava;

      @Override
      public String toString() {
         return super.toString().replaceAll("_", " ");
      }
   }
}
