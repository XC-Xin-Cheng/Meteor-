package zgoly.meteorist.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import zgoly.meteorist.Meteorist;

public class EntityInteract extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Boolean> oneInteractionPerTick = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("one-interaction-per-tick")).description("One interaction per tick.")).defaultValue(true)).build());
   private final Setting<Set<EntityType<?>>> entities = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder)((meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder)new meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder()
                  .name("entities"))
               .description("Entities to interact with."))
            .defaultValue(new EntityType[]{EntityType.SHEEP})
            .onlyAttackable()
            .build()
      );
   private final Setting<Double> range = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("range"))
               .description("Interact range."))
            .min(0.0)
            .defaultValue(4.0)
            .build()
      );
   private final Setting<InteractionHand> hand = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("hand"))
                  .description("The hand to use when interacting."))
               .defaultValue(InteractionHand.MAIN_HAND))
            .build()
      );
   private final Setting<Boolean> swingHand = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("swing-hand")).description("Swing hand client-side.")).defaultValue(true)).build());
   private final Setting<Boolean> ignoreBabies = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("ignore-babies")).description("Ignore baby entities.")).defaultValue(true)).build());
   private final Setting<Boolean> oneTime = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("one-time")).description("Interact with each entity only one time.")).defaultValue(true)).build());
   private final Setting<Boolean> rotate = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("rotate")).description("Sends rotation packets to the server when interacting with entity."))
               .defaultValue(true))
            .build()
      );
   private final List<Entity> used = new ArrayList<>();

   public EntityInteract() {
      super(Meteorist.CATEGORY, "entity-interact", "Automatically interacts with entities in range.");
   }

   public void onActivate() {
      this.used.clear();
   }

   @EventHandler
   private void onTick(Pre event) {
      for (Entity entity : this.mc.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity
            && ((Set)this.entities.get()).contains(entity.getType())
            && !this.mc.player.getMainHandItem().isEmpty()
            && (!(Boolean)this.oneTime.get() || !this.used.contains(entity))
            && !(this.mc.player.distanceTo(entity) > (Double)this.range.get())
            && (!(Boolean)this.ignoreBabies.get() || !((LivingEntity)entity).isBaby())) {
            if ((Boolean)this.rotate.get()) {
               Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), 10, null);
            }

            if ((Boolean)this.swingHand.get()) {
               this.mc.player.swing((InteractionHand)this.hand.get());
            }

            if ((Boolean)this.oneTime.get()) {
               this.used.add(entity);
            }

            EntityHitResult location = new EntityHitResult(entity, entity.getBoundingBox().getCenter());
            this.mc.gameMode.interact(this.mc.player, entity, location, (InteractionHand)this.hand.get());
            if ((Boolean)this.oneInteractionPerTick.get()) {
               break;
            }
         }
      }
   }
}
