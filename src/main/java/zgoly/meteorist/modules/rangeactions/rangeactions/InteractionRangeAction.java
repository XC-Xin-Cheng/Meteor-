package zgoly.meteorist.modules.rangeactions.rangeactions;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;

public class InteractionRangeAction extends BaseRangeAction {
   public static final String type = "Interaction";
   protected final SettingGroup sgInteractionRangeAction = this.settings.createGroup("Interaction Range Action");
   public final Setting<Boolean> ignoreStartBreakingBlock = this.sgInteractionRangeAction
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("ignore-start-breaking-block")).description("Ignore actions when starting to break a block."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> ignoreInteractBlock = this.sgInteractionRangeAction
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("ignore-interact-block")).description("Ignore actions when interacting with a block."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> ignoreAttackEntity = this.sgInteractionRangeAction
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("ignore-attack-entity")).description("Ignore actions when attacking an entity.")).defaultValue(false))
            .build()
      );
   public final Setting<Boolean> ignoreInteractEntity = this.sgInteractionRangeAction
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("ignore-interact-entity")).description("Ignore actions when interacting with an entity."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> enableSneak = this.sgInteractionRangeAction
      .add(((Builder)((Builder)((Builder)new Builder().name("enable-sneak")).description("Enable sneak action.")).defaultValue(false)).build());

   @Override
   public String getTypeName() {
      return "Interaction";
   }

   public InteractionRangeAction copy() {
      return (InteractionRangeAction)new InteractionRangeAction().fromTag(this.toTag());
   }
}
