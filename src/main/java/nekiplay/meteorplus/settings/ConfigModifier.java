package nekiplay.meteorplus.settings;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.MultiPlayerGameModeAccessor;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.HorizontalDirection;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import nekiplay.meteorplus.features.modules.world.timer.TimerPlus;
import nekiplay.meteorplus.mixinclasses.SpoofMode;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.meteordev.starscript.Starscript;
import org.meteordev.starscript.value.Value;
import org.meteordev.starscript.value.ValueMap;

public class ConfigModifier {
   private static ConfigModifier INSTANCE;
   public static final SettingGroup sgMeteorPlus = Config.get().settings.createGroup("Meteor+");
   public final Setting<Boolean> positionProtection = sgMeteorPlus.add(
      ((Builder)((Builder)((Builder)((Builder)new Builder().name("position-protection")).description("Set fake position in F3 and in mods."))
               .defaultValue(false))
            .onChanged(a -> this.changedProtection()))
         .build()
   );
   public final Setting<SpoofMode> spoofMode = sgMeteorPlus.add(
      ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("protection-mode"))
                  .defaultValue(SpoofMode.Sensor))
               .visible(this.positionProtection::get))
            .onChanged(a -> this.changedProtection()))
         .build()
   );
   public final Setting<Integer> x_spoof = sgMeteorPlus.add(
      ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                  .name("x-add"))
               .defaultValue(100000))
            .visible(() -> (Boolean)this.positionProtection.get() && this.spoofMode.get() == SpoofMode.Fake))
         .build()
   );
   public final Setting<Integer> z_spoof = sgMeteorPlus.add(
      ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                  .name("z-add"))
               .defaultValue(100000))
            .visible(() -> (Boolean)this.positionProtection.get() && this.spoofMode.get() == SpoofMode.Fake))
         .build()
   );
   private static final MutableBlockPos BP = new MutableBlockPos();
   private static long lastRequestedStatsTime = 0L;

   public static ConfigModifier get() {
      if (INSTANCE == null) {
         INSTANCE = new ConfigModifier();
      }

      return INSTANCE;
   }

   public ConfigModifier() {
      Starscript ss = MeteorStarscript.ss;
      ss.set(
         "meteorplus",
         new ValueMap().set("modules", new ValueMap().set("timerplus", new ValueMap().set("charge", () -> Value.number(TimerPlus.getPercentage()))))
      );
   }

   private void changedProtection() {
      Starscript ss = MeteorStarscript.ss;
      if ((Boolean)this.positionProtection.get()) {
         if (this.spoofMode.get() == SpoofMode.Fake) {
            ss.set(
               "camera",
               new ValueMap()
                  .set(
                     "pos",
                     new ValueMap()
                        .set("_toString", () -> this.posString(false, true))
                        .set("x", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().x + ((Integer)get().x_spoof.get()).intValue()))
                        .set("y", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().y))
                        .set("z", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().z + ((Integer)get().z_spoof.get()).intValue()))
                  )
                  .set(
                     "opposite_dim_pos",
                     new ValueMap()
                        .set("_toString", () -> this.posString(true, true))
                        .set("x", () -> this.oppositeX(true))
                        .set("y", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().y))
                        .set("z", () -> this.oppositeZ(true))
                  )
                  .set("yaw", () -> yaw(true))
                  .set("pitch", () -> pitch(true))
                  .set("direction", () -> direction(true))
            );
            ss.set(
               "player",
               new ValueMap()
                  .set("_toString", () -> Value.string(MeteorClient.mc.getUser().getName()))
                  .set("health", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getHealth() : 0.0))
                  .set("absorption", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getAbsorptionAmount() : 0.0))
                  .set("hunger", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getFoodData().getFoodLevel() : 0.0))
                  .set("speed", () -> Value.number(Utils.getPlayerSpeed().horizontalDistance()))
                  .set(
                     "speed_all",
                     new ValueMap()
                        .set("_toString", () -> Value.string(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().toString() : ""))
                        .set("x", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().x : 0.0))
                        .set("y", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().y : 0.0))
                        .set("z", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().z : 0.0))
                  )
                  .set(
                     "breaking_progress",
                     () -> Value.number(
                        MeteorClient.mc.gameMode != null ? ((MultiPlayerGameModeAccessor)MeteorClient.mc.gameMode).meteor$getBreakingProgress() : 0.0
                     )
                  )
                  .set("biome", ConfigModifier::biome)
                  .set("dimension", () -> Value.string(PlayerUtils.getDimension().name()))
                  .set("opposite_dimension", () -> Value.string(PlayerUtils.getDimension().opposite().name()))
                  .set(
                     "pos",
                     new ValueMap()
                        .set("_toString", () -> this.posString(false, false))
                        .set(
                           "x",
                           () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getX() + ((Integer)get().x_spoof.get()).intValue() : 0.0)
                        )
                        .set("y", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getY() : 0.0))
                        .set(
                           "z",
                           () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getZ() + ((Integer)get().z_spoof.get()).intValue() : 0.0)
                        )
                  )
                  .set(
                     "opposite_dim_pos",
                     new ValueMap()
                        .set("_toString", () -> this.posString(true, false))
                        .set("x", () -> this.oppositeX(false))
                        .set("y", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getY() : 0.0))
                        .set("z", () -> this.oppositeZ(false))
                  )
                  .set("yaw", () -> yaw(false))
                  .set("pitch", () -> pitch(false))
                  .set("direction", () -> direction(false))
                  .set("hand", () -> MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getMainHandItem()) : Value.null_())
                  .set("offhand", () -> MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getOffhandItem()) : Value.null_())
                  .set("hand_or_offhand", ConfigModifier::handOrOffhand)
                  .set("get_item", ConfigModifier::getItem)
                  .set("count_items", ConfigModifier::countItems)
                  .set(
                     "xp",
                     new ValueMap()
                        .set("level", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.experienceLevel : 0.0))
                        .set("progress", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.experienceProgress : 0.0))
                        .set("total", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.totalExperience : 0.0))
                  )
                  .set("has_potion_effect", ConfigModifier::hasPotionEffect)
                  .set("get_potion_effect", ConfigModifier::getPotionEffect)
                  .set("get_stat", ConfigModifier::getStat)
            );
         } else {
            ss.set(
               "camera",
               new ValueMap()
                  .set(
                     "pos",
                     new ValueMap()
                        .set("_toString", () -> this.posString(false, true))
                        .set("x", () -> Value.number(0.0))
                        .set("y", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().y))
                        .set("z", () -> Value.number(0.0))
                  )
                  .set(
                     "opposite_dim_pos",
                     new ValueMap()
                        .set("_toString", () -> this.posString(true, true))
                        .set("x", () -> Value.number(0.0))
                        .set("y", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().y))
                        .set("z", () -> Value.number(0.0))
                  )
                  .set("yaw", () -> yaw(true))
                  .set("pitch", () -> pitch(true))
                  .set("direction", () -> direction(true))
            );
            ss.set(
               "player",
               new ValueMap()
                  .set("_toString", () -> Value.string(MeteorClient.mc.getUser().getName()))
                  .set("health", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getHealth() : 0.0))
                  .set("absorption", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getAbsorptionAmount() : 0.0))
                  .set("hunger", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getFoodData().getFoodLevel() : 0.0))
                  .set("speed", () -> Value.number(Utils.getPlayerSpeed().horizontalDistance()))
                  .set(
                     "speed_all",
                     new ValueMap()
                        .set("_toString", () -> Value.string(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().toString() : ""))
                        .set("x", () -> Value.number(0.0))
                        .set("y", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().y : 0.0))
                        .set("z", () -> Value.number(0.0))
                  )
                  .set(
                     "breaking_progress",
                     () -> Value.number(
                        MeteorClient.mc.gameMode != null ? ((MultiPlayerGameModeAccessor)MeteorClient.mc.gameMode).meteor$getBreakingProgress() : 0.0
                     )
                  )
                  .set("biome", ConfigModifier::biome)
                  .set("dimension", () -> Value.string(PlayerUtils.getDimension().name()))
                  .set("opposite_dimension", () -> Value.string(PlayerUtils.getDimension().opposite().name()))
                  .set(
                     "pos",
                     new ValueMap()
                        .set("_toString", () -> this.posString(false, false))
                        .set("x", () -> Value.number(0.0))
                        .set("y", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getY() : 0.0))
                        .set("z", () -> Value.number(0.0))
                  )
                  .set(
                     "opposite_dim_pos",
                     new ValueMap()
                        .set("_toString", () -> this.posString(true, false))
                        .set("x", () -> Value.number(0.0))
                        .set("y", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getY() : 0.0))
                        .set("z", () -> Value.number(0.0))
                  )
                  .set("yaw", () -> yaw(false))
                  .set("pitch", () -> pitch(false))
                  .set("direction", () -> direction(false))
                  .set("hand", () -> MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getMainHandItem()) : Value.null_())
                  .set("offhand", () -> MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getOffhandItem()) : Value.null_())
                  .set("hand_or_offhand", ConfigModifier::handOrOffhand)
                  .set("get_item", ConfigModifier::getItem)
                  .set("count_items", ConfigModifier::countItems)
                  .set(
                     "xp",
                     new ValueMap()
                        .set("level", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.experienceLevel : 0.0))
                        .set("progress", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.experienceProgress : 0.0))
                        .set("total", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.totalExperience : 0.0))
                  )
                  .set("has_potion_effect", ConfigModifier::hasPotionEffect)
                  .set("get_potion_effect", ConfigModifier::getPotionEffect)
                  .set("get_stat", ConfigModifier::getStat)
            );
         }
      } else {
         ss.set(
            "camera",
            new ValueMap()
               .set(
                  "pos",
                  new ValueMap()
                     .set("_toString", () -> this.posString(false, true))
                     .set("x", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().x))
                     .set("y", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().y))
                     .set("z", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().z))
               )
               .set(
                  "opposite_dim_pos",
                  new ValueMap()
                     .set("_toString", () -> this.posString(true, true))
                     .set("x", () -> this.oppositeX(true))
                     .set("y", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().y))
                     .set("z", () -> this.oppositeZ(true))
               )
               .set("yaw", () -> yaw(true))
               .set("pitch", () -> pitch(true))
               .set("direction", () -> direction(true))
         );
         ss.set(
            "player",
            new ValueMap()
               .set("_toString", () -> Value.string(MeteorClient.mc.getUser().getName()))
               .set("health", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getHealth() : 0.0))
               .set("absorption", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getAbsorptionAmount() : 0.0))
               .set("hunger", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getFoodData().getFoodLevel() : 0.0))
               .set("speed", () -> Value.number(Utils.getPlayerSpeed().horizontalDistance()))
               .set(
                  "speed_all",
                  new ValueMap()
                     .set("_toString", () -> Value.string(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().toString() : ""))
                     .set("x", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().x : 0.0))
                     .set("y", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().y : 0.0))
                     .set("z", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().z : 0.0))
               )
               .set(
                  "breaking_progress",
                  () -> Value.number(
                     MeteorClient.mc.gameMode != null ? ((MultiPlayerGameModeAccessor)MeteorClient.mc.gameMode).meteor$getBreakingProgress() : 0.0
                  )
               )
               .set("biome", ConfigModifier::biome)
               .set("dimension", () -> Value.string(PlayerUtils.getDimension().name()))
               .set("opposite_dimension", () -> Value.string(PlayerUtils.getDimension().opposite().name()))
               .set(
                  "pos",
                  new ValueMap()
                     .set("_toString", () -> this.posString(false, false))
                     .set("x", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getX() : 0.0))
                     .set("y", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getY() : 0.0))
                     .set("z", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getZ() : 0.0))
               )
               .set(
                  "opposite_dim_pos",
                  new ValueMap()
                     .set("_toString", () -> this.posString(true, false))
                     .set("x", () -> this.oppositeX(false))
                     .set("y", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getY() : 0.0))
                     .set("z", () -> this.oppositeZ(false))
               )
               .set("yaw", () -> yaw(false))
               .set("pitch", () -> pitch(false))
               .set("direction", () -> direction(false))
               .set("hand", () -> MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getMainHandItem()) : Value.null_())
               .set("offhand", () -> MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getOffhandItem()) : Value.null_())
               .set("hand_or_offhand", ConfigModifier::handOrOffhand)
               .set("get_item", ConfigModifier::getItem)
               .set("count_items", ConfigModifier::countItems)
               .set(
                  "xp",
                  new ValueMap()
                     .set("level", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.experienceLevel : 0.0))
                     .set("progress", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.experienceProgress : 0.0))
                     .set("total", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.totalExperience : 0.0))
               )
               .set("has_potion_effect", ConfigModifier::hasPotionEffect)
               .set("get_potion_effect", ConfigModifier::getPotionEffect)
               .set("get_stat", ConfigModifier::getStat)
         );
      }
   }

   private Value oppositeX(boolean camera) {
      double x = 0.0;
      if ((Boolean)this.positionProtection.get()) {
         x = camera
            ? MeteorClient.mc.gameRenderer.getMainCamera().position().x + ((Integer)get().x_spoof.get()).intValue()
            : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getX() + ((Integer)get().x_spoof.get()).intValue() : 0.0);
      } else {
         x = camera ? MeteorClient.mc.gameRenderer.getMainCamera().position().x : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getX() : 0.0);
      }

      Dimension dimension = PlayerUtils.getDimension();
      if (dimension == Dimension.Overworld) {
         x /= 8.0;
      } else if (dimension == Dimension.Nether) {
         x *= 8.0;
      }

      return Value.number(x);
   }

   private Value oppositeZ(boolean camera) {
      double z = 0.0;
      if ((Boolean)this.positionProtection.get()) {
         z = camera
            ? MeteorClient.mc.gameRenderer.getMainCamera().position().z + ((Integer)get().z_spoof.get()).intValue()
            : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getZ() + ((Integer)get().z_spoof.get()).intValue() : 0.0);
      } else {
         z = camera ? MeteorClient.mc.gameRenderer.getMainCamera().position().z : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getZ() : 0.0);
      }

      Dimension dimension = PlayerUtils.getDimension();
      if (dimension == Dimension.Overworld) {
         z /= 8.0;
      } else if (dimension == Dimension.Nether) {
         z *= 8.0;
      }

      return Value.number(z);
   }

   private static Value yaw(boolean camera) {
      float yaw;
      if (camera) {
         yaw = MeteorClient.mc.gameRenderer.getMainCamera().yRot();
      } else {
         yaw = MeteorClient.mc.player != null ? MeteorClient.mc.player.getYRot() : 0.0F;
      }

      yaw %= 360.0F;
      if (yaw < 0.0F) {
         yaw += 360.0F;
      }

      if (yaw > 180.0F) {
         yaw -= 360.0F;
      }

      return Value.number(yaw);
   }

   private static Value pitch(boolean camera) {
      float pitch;
      if (camera) {
         pitch = MeteorClient.mc.gameRenderer.getMainCamera().xRot();
      } else {
         pitch = MeteorClient.mc.player != null ? MeteorClient.mc.player.getXRot() : 0.0F;
      }

      pitch %= 360.0F;
      if (pitch < 0.0F) {
         pitch += 360.0F;
      }

      if (pitch > 180.0F) {
         pitch -= 360.0F;
      }

      return Value.number(pitch);
   }

   private static Value direction(boolean camera) {
      float yaw;
      if (camera) {
         yaw = MeteorClient.mc.gameRenderer.getMainCamera().yRot();
      } else {
         yaw = MeteorClient.mc.player != null ? MeteorClient.mc.player.getYRot() : 0.0F;
      }

      return MeteorStarscript.wrap(HorizontalDirection.get(yaw));
   }

   private static Value biome() {
      if (MeteorClient.mc.player != null && MeteorClient.mc.level != null) {
         BP.set(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ());
         Identifier id = ((Registry)MeteorClient.mc.level.registryAccess().lookup(Registries.BIOME).get())
            .getKey((Biome)MeteorClient.mc.level.getBiome(BP).value());
         return id == null
            ? Value.string("Unknown")
            : Value.string(Arrays.stream(id.getPath().split("_")).<CharSequence>map(StringUtils::capitalize).collect(Collectors.joining(" ")));
      } else {
         return Value.string("");
      }
   }

   private static Value handOrOffhand() {
      if (MeteorClient.mc.player == null) {
         return Value.null_();
      } else {
         ItemStack itemStack = MeteorClient.mc.player.getMainHandItem();
         if (itemStack.isEmpty()) {
            itemStack = MeteorClient.mc.player.getOffhandItem();
         }

         return itemStack != null ? MeteorStarscript.wrap(itemStack) : Value.null_();
      }
   }

   private static Value ping() {
      if (MeteorClient.mc.getConnection() != null && MeteorClient.mc.player != null) {
         PlayerInfo playerListEntry = MeteorClient.mc.getConnection().getPlayerInfo(MeteorClient.mc.player.getUUID());
         return Value.number(playerListEntry != null ? playerListEntry.getLatency() : 0.0);
      } else {
         return Value.number(0.0);
      }
   }

   private Value posString(boolean opposite, boolean camera) {
      Vec3 pos;
      if (camera) {
         pos = MeteorClient.mc.gameRenderer.getMainCamera().position();
      } else {
         pos = MeteorClient.mc.player != null ? MeteorClient.mc.player.position() : Vec3.ZERO;
      }

      double x = pos.x;
      double z = pos.z;
      if (opposite) {
         Dimension dimension = PlayerUtils.getDimension();
         if (dimension == Dimension.Overworld) {
            x /= 8.0;
            z /= 8.0;
         } else if (dimension == Dimension.Nether) {
            x *= 8.0;
            z *= 8.0;
         }
      }

      return this.posString(x, pos.y, z);
   }

   private Value posString(double x, double y, double z) {
      if ((Boolean)this.positionProtection.get()) {
         return this.spoofMode.get() == SpoofMode.Fake
            ? Value.string(
               String.format("X: %.0f Y: %.0f Z: %.0f", x + ((Integer)get().x_spoof.get()).intValue(), y, z + ((Integer)get().z_spoof.get()).intValue())
            )
            : Value.string(String.format("X: 0 Y: %.0f Z: 0", y));
      } else {
         return Value.string(String.format("X: %.0f Y: %.0f Z: %.0f", x, y, z));
      }
   }

   private static Value getItem(Starscript ss, int argCount) {
      if (argCount != 1) {
         ss.error("player.get_item() requires 1 argument, got %d.", new Object[]{argCount});
      }

      int i = (int)ss.popNumber("First argument to player.get_item() needs to be a number.");
      return MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getInventory().getItem(i)) : Value.null_();
   }

   private static Value countItems(Starscript ss, int argCount) {
      if (argCount != 1) {
         ss.error("player.count_items() requires 1 argument, got %d.", new Object[]{argCount});
      }

      String idRaw = ss.popString("First argument to player.count_items() needs to be a string.");
      Identifier id = Identifier.tryParse(idRaw);
      if (id == null) {
         return Value.number(0.0);
      } else {
         Item item = (Item)BuiltInRegistries.ITEM.getValue(id);
         if (item != Items.AIR && MeteorClient.mc.player != null) {
            int count = 0;

            for (int i = 0; i < MeteorClient.mc.player.getInventory().getContainerSize(); i++) {
               ItemStack itemStack = MeteorClient.mc.player.getInventory().getItem(i);
               if (itemStack.getItem() == item) {
                  count += itemStack.getCount();
               }
            }

            return Value.number(count);
         } else {
            return Value.number(0.0);
         }
      }
   }

   private static Value hasPotionEffect(Starscript ss, int argCount) {
      if (argCount < 1) {
         ss.error("player.has_potion_effect() requires 1 argument, got %d.", new Object[]{argCount});
      }

      if (MeteorClient.mc.player == null) {
         return Value.bool(false);
      } else {
         Identifier name = MeteorStarscript.popIdentifier(ss, "First argument to player.has_potion_effect() needs to a string.");
         Optional<Reference<MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.get(name);
         if (effect.isEmpty()) {
            return Value.null_();
         } else {
            MobEffectInstance effectInstance = MeteorClient.mc.player.getEffect((Holder)effect.get());
            return Value.bool(effectInstance != null);
         }
      }
   }

   private static Value getPotionEffect(Starscript ss, int argCount) {
      if (argCount < 1) {
         ss.error("player.get_potion_effect() requires 1 argument, got %d.", new Object[]{argCount});
      }

      if (MeteorClient.mc.player == null) {
         return Value.null_();
      } else {
         Identifier name = MeteorStarscript.popIdentifier(ss, "First argument to player.get_potion_effect() needs to a string.");
         Optional<Reference<MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.get(name);
         if (effect.isEmpty()) {
            return Value.null_();
         } else {
            MobEffectInstance effectInstance = MeteorClient.mc.player.getEffect((Holder)effect.get());
            return effectInstance == null ? Value.null_() : MeteorStarscript.wrap(effectInstance);
         }
      }
   }

   private static Value getStat(Starscript ss, int argCount) {
      if (argCount < 1) {
         ss.error("player.get_stat() requires 1 argument, got %d.", new Object[]{argCount});
      }

      if (MeteorClient.mc.player == null) {
         return Value.number(0.0);
      } else {
         long time = System.currentTimeMillis();
         if ((time - lastRequestedStatsTime) / 1000.0 >= 1.0 && MeteorClient.mc.getConnection() != null) {
            MeteorClient.mc.getConnection().send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
            lastRequestedStatsTime = time;
         }

         String type = argCount > 1 ? ss.popString("First argument to player.get_stat() needs to be a string.") : "custom";
         Identifier name = MeteorStarscript.popIdentifier(ss, (argCount > 1 ? "Second" : "First") + " argument to player.get_stat() needs to be a string.");

         Stat<?> stat = switch (type) {
            case "mined" -> Stats.BLOCK_MINED.get((Block)BuiltInRegistries.BLOCK.getValue(name));
            case "crafted" -> Stats.ITEM_CRAFTED.get((Item)BuiltInRegistries.ITEM.getValue(name));
            case "used" -> Stats.ITEM_USED.get((Item)BuiltInRegistries.ITEM.getValue(name));
            case "broken" -> Stats.ITEM_BROKEN.get((Item)BuiltInRegistries.ITEM.getValue(name));
            case "picked_up" -> Stats.ITEM_PICKED_UP.get((Item)BuiltInRegistries.ITEM.getValue(name));
            case "dropped" -> Stats.ITEM_DROPPED.get((Item)BuiltInRegistries.ITEM.getValue(name));
            case "killed" -> Stats.ENTITY_KILLED.get((EntityType)BuiltInRegistries.ENTITY_TYPE.getValue(name));
            case "killed_by" -> Stats.ENTITY_KILLED_BY.get((EntityType)BuiltInRegistries.ENTITY_TYPE.getValue(name));
            case "custom" -> {
               name = (Identifier)BuiltInRegistries.CUSTOM_STAT.getValue(name);
               yield name != null ? Stats.CUSTOM.get(name) : null;
            }
            default -> null;
         };
         return Value.number(stat != null ? MeteorClient.mc.player.getStats().getValue(stat) : 0.0);
      }
   }
}
