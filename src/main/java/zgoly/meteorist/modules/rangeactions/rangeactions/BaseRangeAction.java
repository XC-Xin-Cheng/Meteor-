package zgoly.meteorist.modules.rangeactions.rangeactions;

import java.util.Set;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura.EntityAge;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

public class BaseRangeAction implements ISerializable<BaseRangeAction> {
   public final Settings settings = new Settings();
   protected final SettingGroup sgBaseRangeAction = this.settings.createGroup("Base Range Action");
   public final Setting<Double> rangeFrom = this.sgBaseRangeAction
      .add(((Builder)((Builder)new Builder().name("range-from")).description("Minimum range to react.")).defaultValue(0.0).min(0.0).build());
   public final Setting<Double> rangeTo = this.sgBaseRangeAction
      .add(((Builder)((Builder)new Builder().name("range-to")).description("Maximum range to react.")).defaultValue(10.0).min(0.0).build());
   public final Setting<Set<EntityType<?>>> entities = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder)((meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder)new meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder()
                  .name("entities"))
               .description("Entity types to react on."))
            .onlyAttackable()
            .defaultValue(new EntityType[]{EntityType.PLAYER})
            .build()
      );
   public final Setting<EntityAge> mobAgeFilter = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("mob-age-filter"))
                  .description("Determines the age of the mobs to react (baby, adult, or both)."))
               .defaultValue(EntityAge.Adult))
            .build()
      );
   public final Setting<Boolean> ignoreNamed = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-named"))
                  .description("Ignore named mobs."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> ignorePassive = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-passive"))
                  .description("Ignore passive mobs unless they are targeting you."))
               .defaultValue(true))
            .build()
      );
   public final Setting<Boolean> ignoreTamed = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-tamed"))
                  .description("Ignore tamed mobs."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> ignoreCreative = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-creative"))
                  .description("Will avoid reacting to players in creative mode."))
               .defaultValue(true))
            .build()
      );
   public final Setting<Boolean> ignoreFriends = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-friends"))
                  .description("Will avoid reacting to players on your friends list."))
               .defaultValue(true))
            .build()
      );
   public final Setting<Boolean> ignoreShield = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-shield"))
                  .description("Will avoid reacting to players who are using a shield."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> useFovRange = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("use-fov-range"))
                  .description("Restrict reactions to entities within the specified FOV."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Double> fovRange = this.sgBaseRangeAction
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("fov-range")).description("Maximum Field of View (FOV) range for reacting to entities."))
               .sliderRange(0.0, 180.0)
               .defaultValue(90.0)
               .visible(this.useFovRange::get))
            .build()
      );
   public final Setting<Boolean> ignoreWalls = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ignore-walls"))
                  .description("Allow reacting through walls."))
               .defaultValue(false))
            .build()
      );
   public final Setting<SortPriority> priority = this.sgBaseRangeAction
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("priority"))
                  .description("Sorting method to prioritize targets within range."))
               .defaultValue(SortPriority.ClosestAngle))
            .build()
      );

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.put("settings", this.settings.toTag());
      return tag;
   }

   public BaseRangeAction fromTag(CompoundTag tag) {
      CompoundTag settingsTag = (CompoundTag)tag.get("settings");
      if (settingsTag != null) {
         this.settings.fromTag(settingsTag);
      }

      return this;
   }

   public String getTypeName() {
      return null;
   }

   public BaseRangeAction copy() {
      return null;
   }
}
