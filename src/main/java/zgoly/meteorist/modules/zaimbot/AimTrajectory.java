package zgoly.meteorist.modules.zaimbot;

import java.util.List;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import zgoly.meteorist.settings.ItemsSetting;

public class AimTrajectory implements ISerializable<AimTrajectory> {
   public final Settings settings = new Settings();
   SettingGroup sgAimTrajectory = this.settings.createGroup("Aim Trajectory");
   public final Setting<List<Item>> items = this.sgAimTrajectory
      .add(
         ((ItemsSetting.Builder)((ItemsSetting.Builder)((ItemsSetting.Builder)new ItemsSetting.Builder().name("items"))
                  .description("The projectile items this trajectory applies to."))
               .defaultValue(List.of(Items.SNOWBALL, Items.EGG, Items.BLUE_EGG, Items.BROWN_EGG)))
            .filter(item -> true)
            .build()
      );
   public final Setting<Double> gravity = this.sgAimTrajectory
      .add(
         ((Builder)((Builder)new Builder().name("gravity")).description("Gravity multiplier for this item's trajectory."))
            .defaultValue(1.0)
            .min(0.0)
            .sliderMax(5.0)
            .build()
      );
   public final Setting<Double> velocityScale = this.sgAimTrajectory
      .add(
         ((Builder)((Builder)new Builder().name("velocity-scale")).description("Base velocity scale for this item."))
            .defaultValue(0.6)
            .min(0.1)
            .sliderMax(10.0)
            .build()
      );

   public AimTrajectory() {
   }

   public AimTrajectory(List<Item> items, double gravity, double velocityScale) {
      this.items.set(items);
      this.gravity.set(gravity);
      this.velocityScale.set(velocityScale);
   }

   public CompoundTag toTag() {
      return this.settings.toTag();
   }

   public AimTrajectory fromTag(CompoundTag tag) {
      this.settings.fromTag(tag);
      return this;
   }

   public AimTrajectory copy() {
      return new AimTrajectory().fromTag(this.toTag());
   }
}
