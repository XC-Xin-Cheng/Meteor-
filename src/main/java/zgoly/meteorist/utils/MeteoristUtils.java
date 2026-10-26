package zgoly.meteorist.utils;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MeteoristUtils {
   public static boolean isCollidesEntity(AABB box) {
      if (MeteorClient.mc.level == null) {
         return false;
      } else {
         for (Entity entity : MeteorClient.mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity)) {
               return false;
            }

            if (box.intersects(entity.getBoundingBox())) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean isCollidesEntity(BlockPos blockPos) {
      return isCollidesEntity(new AABB(blockPos));
   }

   public static HitResult getCrosshairTarget(Entity entity, double range, boolean ignoreBlocks, Predicate<Entity> filter) {
      if (entity != null && MeteorClient.mc.level != null) {
         Vec3 vec3d = entity.getEyePosition(1.0F);
         Vec3 vec3d2 = entity.getViewVector(1.0F);
         Vec3 vec3d3 = vec3d.add(vec3d2.scale(range));
         AABB box = entity.getBoundingBox().expandTowards(vec3d2.scale(range)).inflate(1.0);
         ClipContext raycastContext = new ClipContext(vec3d, vec3d3, Block.COLLIDER, Fluid.NONE, entity);
         HitResult hitResult = MeteorClient.mc.level.clip(raycastContext);
         double e = range * range;
         if (!ignoreBlocks) {
            e = hitResult.getLocation().distanceToSqr(vec3d);
         }

         EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
            entity, vec3d, vec3d3, box, filter.and(targetEntity -> !targetEntity.isSpectator()), e
         );
         if (entityHitResult != null) {
            return entityHitResult;
         } else {
            return !ignoreBlocks ? hitResult : null;
         }
      } else {
         return null;
      }
   }

   public static String removeInvalidChars(String text) {
      try {
         Paths.get(text);
         return text;
      } catch (InvalidPathException var3) {
         if (var3.getInput() != null && !var3.getInput().isEmpty() && var3.getIndex() >= 0) {
            StringBuilder stringBuilder = new StringBuilder(var3.getInput());
            stringBuilder.deleteCharAt(var3.getIndex());
            return removeInvalidChars(stringBuilder.toString());
         } else {
            throw var3;
         }
      }
   }

   public static String ticksToTime(int ticks, boolean shortFormat, boolean formatMsg) {
      int ticksPerSecond = 20;
      int ticksPerMinute = ticksPerSecond * 60;
      int ticksPerHour = ticksPerMinute * 60;
      int hours = ticks / ticksPerHour;
      int minutes = ticks % ticksPerHour / ticksPerMinute;
      int seconds = ticks % ticksPerMinute / ticksPerSecond;
      if (hours == 0 && minutes == 0 && seconds == 0) {
         String highlight = formatMsg ? "(highlight)" : "";
         String def = formatMsg ? "(default)" : "";
         return shortFormat ? highlight + "0" + def + "s" : highlight + "0" + def + " seconds";
      } else {
         StringBuilder result = new StringBuilder();
         String highlight = formatMsg ? "(highlight)" : "";
         String def = formatMsg ? "(default)" : "";
         if (shortFormat) {
            if (hours > 0) {
               result.append(highlight).append(hours).append(def).append("h");
               if (minutes > 0 || seconds > 0) {
                  result.append(", ");
               }
            }

            if (minutes > 0) {
               result.append(highlight).append(minutes).append(def).append("m");
               if (seconds > 0) {
                  result.append(", ");
               }
            }

            if (seconds > 0) {
               result.append(highlight).append(seconds).append(def).append("s");
            }
         } else {
            if (hours > 0) {
               result.append(highlight).append(hours).append(def).append(" hour").append(hours > 1 ? "s" : "");
               if (minutes > 0 || seconds > 0) {
                  result.append(", ");
               }
            }

            if (minutes > 0) {
               result.append(highlight).append(minutes).append(def).append(" minute").append(minutes > 1 ? "s" : "");
               if (seconds > 0) {
                  result.append(", ");
               }
            }

            if (seconds > 0) {
               result.append(highlight).append(seconds).append(def).append(" second").append(seconds > 1 ? "s" : "");
            }
         }

         return result.toString();
      }
   }

   public static float calculateFov(LivingEntity player, Entity target) {
      Vec3 lookDirection = player.getViewVector(1.0F);
      Vec3 targetDirection = target.position().subtract(player.position()).normalize();
      return (float)Math.toDegrees(Math.acos(lookDirection.dot(targetDirection)));
   }

   public static boolean canDisplayRecipe(AbstractCraftingMenu screenHandler, RecipeDisplay display) {
      int width = screenHandler.getGridWidth();
      int height = screenHandler.getGridHeight();
      if (!(display instanceof ShapedCraftingRecipeDisplay shaped)) {
         return display instanceof ShapelessCraftingRecipeDisplay shapeless ? width * height >= shapeless.ingredients().size() : false;
      } else {
         return width >= shaped.width() && height >= shaped.height();
      }
   }
}
