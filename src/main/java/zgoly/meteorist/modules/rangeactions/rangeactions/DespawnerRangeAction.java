package zgoly.meteorist.modules.rangeactions.rangeactions;

import java.util.HashSet;
import java.util.Set;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import net.minecraft.world.entity.EntityType;

public class DespawnerRangeAction extends BaseRangeAction {
   public static final String type = "Despawner";
   protected final SettingGroup sgDespawnerRangeAction = this.settings.createGroup("Despawner Range Action");
   public final Setting<Boolean> checkRoof = this.sgDespawnerRangeAction
      .add(((Builder)((Builder)((Builder)new Builder().name("check-roof")).description("Check if the player is under a roof.")).defaultValue(false)).build());
   public final Setting<Double> upVelocity = this.sgDespawnerRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("up-velocity"))
               .description("Up velocity to apply to the player."))
            .defaultValue(8.0)
            .min(0.0)
            .build()
      );

   public DespawnerRangeAction() {
      Set<EntityType<?>> mutableEntities = new HashSet<>();
      mutableEntities.add(EntityType.PHANTOM);
      this.entities.set(mutableEntities);
   }

   @Override
   public String getTypeName() {
      return "Despawner";
   }

   public DespawnerRangeAction copy() {
      return (DespawnerRangeAction)new DespawnerRangeAction().fromTag(this.toTag());
   }
}
