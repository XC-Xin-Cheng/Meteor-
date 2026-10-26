package nekiplay.meteorplus.features.modules.misc;

import java.util.List;
import java.util.Locale;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.Reach;
import meteordevelopment.orbit.EventHandler;
import nekiplay.main.events.hud.DebugDrawTextEvent;
import nekiplay.meteorplus.mixinclasses.SpoofMode;
import nekiplay.meteorplus.settings.ConfigModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class CoordinateProtector {
   @EventHandler
   private void onDebugF3RenderText(DebugDrawTextEvent event) {
      List<String> lines = event.getLines();
      if ((Boolean)ConfigModifier.get().positionProtection.get()) {
         int index = 0;

         for (Object obj : lines.toArray()) {
            String str = obj.toString();
            if (str.startsWith("XYZ:")) {
               String xyz = String.format(
                  Locale.ROOT,
                  "XYZ: %.3f / %.5f / %.3f",
                  MeteorClient.mc.getCameraEntity().getX() + ((Integer)ConfigModifier.get().x_spoof.get()).intValue(),
                  MeteorClient.mc.getCameraEntity().getY(),
                  MeteorClient.mc.getCameraEntity().getZ() + ((Integer)ConfigModifier.get().z_spoof.get()).intValue()
               );
               if (ConfigModifier.get().spoofMode.get() == SpoofMode.Fake) {
                  lines.set(index, xyz);
               } else if (ConfigModifier.get().spoofMode.get() == SpoofMode.Sensor) {
                  lines.set(index, "XYZ: *** / *** / ***");
               }
            } else if (str.startsWith("Block: ")) {
               BlockPos blockPos = MeteorClient.mc.getCameraEntity().blockPosition();
               blockPos = blockPos.offset((Integer)ConfigModifier.get().x_spoof.get(), 0, (Integer)ConfigModifier.get().z_spoof.get());
               String block = String.format(Locale.ROOT, "Block: %d %d %d", blockPos.getX(), blockPos.getY(), blockPos.getZ());
               if (ConfigModifier.get().spoofMode.get() == SpoofMode.Fake) {
                  lines.set(index, block);
               } else if (ConfigModifier.get().spoofMode.get() == SpoofMode.Sensor) {
                  lines.set(index, "Block: *** *** ***");
               }
            } else if (str.startsWith("Chunk:")) {
               BlockPos blockPos = MeteorClient.mc.getCameraEntity().blockPosition();
               blockPos = blockPos.offset((Integer)ConfigModifier.get().x_spoof.get(), 0, (Integer)ConfigModifier.get().z_spoof.get());
               ChunkPos chunkPos = ChunkPos.containing(blockPos);
               if (ConfigModifier.get().spoofMode.get() == SpoofMode.Fake) {
                  String chunk = String.format(
                     Locale.ROOT,
                     "Chunk: %d %d %d [%d %d in r.%d.%d.mca]",
                     chunkPos.x(),
                     SectionPos.blockToSectionCoord(blockPos.getY()),
                     chunkPos.z(),
                     chunkPos.getRegionLocalX(),
                     chunkPos.getRegionLocalZ(),
                     chunkPos.getRegionX(),
                     chunkPos.getRegionZ()
                  );
                  lines.set(index, chunk);
               } else if (ConfigModifier.get().spoofMode.get() == SpoofMode.Sensor) {
                  lines.set(index, "Chunk: *** *** *** [*** *** in ***.***.mca]");
               }
            } else if (str.contains("Targeted Block:")) {
               HitResult blockHitResult = MeteorClient.mc.player.pick(((Reach)Modules.get().get(Reach.class)).blockReach(), 1.0F, false);
               if (blockHitResult != null && blockHitResult.getType() == Type.BLOCK) {
                  ChatFormatting var10001 = ChatFormatting.UNDERLINE;
                  BlockPos blockPos = ((BlockHitResult)blockHitResult).getBlockPos();
                  blockPos = blockPos.offset((Integer)ConfigModifier.get().x_spoof.get(), 0, (Integer)ConfigModifier.get().z_spoof.get());
                  if (ConfigModifier.get().spoofMode.get() == SpoofMode.Fake) {
                     lines.set(index, var10001 + "Targeted Block: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
                  } else if (ConfigModifier.get().spoofMode.get() == SpoofMode.Sensor) {
                     lines.set(index, var10001 + "Targeted Block: *** *** ***");
                  }
               }
            } else if (str.contains("Targeted Fluid:")) {
               HitResult blockHitResult = MeteorClient.mc.player.pick(((Reach)Modules.get().get(Reach.class)).blockReach(), 1.0F, true);
               if (blockHitResult != null && blockHitResult.getType() == Type.BLOCK) {
                  ChatFormatting var10001 = ChatFormatting.UNDERLINE;
                  BlockPos blockPos = ((BlockHitResult)blockHitResult).getBlockPos();
                  blockPos = blockPos.offset((Integer)ConfigModifier.get().x_spoof.get(), 0, (Integer)ConfigModifier.get().z_spoof.get());
                  if (ConfigModifier.get().spoofMode.get() == SpoofMode.Fake) {
                     lines.set(index, var10001 + "Targeted Fluid: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
                  } else if (ConfigModifier.get().spoofMode.get() == SpoofMode.Sensor) {
                     lines.set(index, var10001 + "Targeted Fluid: *** *** ***");
                  }
               }
            }

            index++;
         }
      }
   }
}
