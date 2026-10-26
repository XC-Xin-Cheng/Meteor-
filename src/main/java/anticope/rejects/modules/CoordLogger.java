package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.UUID;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;

public class CoordLogger extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgTeleports = this.settings.createGroup("Teleports");
   private final SettingGroup sgWorldEvents = this.settings.createGroup("World Events");
   private final Setting<Double> minDistance = this.sgGeneral
      .add(
         ((Builder)((Builder)new Builder().name("minimum-distance")).description("Minimum distance to log event."))
            .min(5.0)
            .max(100.0)
            .sliderMin(5.0)
            .sliderMax(100.0)
            .defaultValue(10.0)
            .build()
      );
   private final Setting<Boolean> players = this.sgTeleports
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("players"))
                  .description("Logs player teleports."))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> wolves = this.sgTeleports
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("wolves"))
                  .description("Logs wolf teleports."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> enderDragons = this.sgWorldEvents
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("ender-dragons"))
                  .description("Logs killed ender dragons."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> endPortals = this.sgWorldEvents
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("end-portals"))
                  .description("Logs opened end portals."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> withers = this.sgWorldEvents
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("withers"))
                  .description("Logs wither spawns."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> otherEvents = this.sgWorldEvents
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("other-global-events"))
                  .description("Logs other global events."))
               .defaultValue(false))
            .build()
      );

   public CoordLogger() {
      super(MeteorRejectsAddon.CATEGORY, "coord-logger", "Logs coordinates of various events. Might not work on Spigot/Paper servers.");
   }

   @EventHandler
   private void onPacketReceive(Receive event) {
      if (event.packet instanceof ClientboundTeleportEntityPacket packet) {
         try {
            Entity entity = this.mc.level.getEntity(packet.id());
            if (entity.getType().equals(EntityType.PLAYER) && (Boolean)this.players.get()) {
               Vec3 packetPosition = packet.change().position();
               Vec3 playerPosition = new Vec3(entity.getX(), entity.getY(), entity.getZ());
               if (playerPosition.distanceTo(packetPosition) >= (Double)this.minDistance.get()) {
                  this.info(this.formatMessage("Player '" + entity.getScoreboardName() + "' has teleported to ", packetPosition));
               }
            } else if (entity.getType().equals(EntityType.WOLF) && (Boolean)this.wolves.get()) {
               Vec3 packetPosition = packet.change().position();
               Vec3 wolfPosition = new Vec3(entity.getX(), entity.getY(), entity.getZ());
               UUID ownerUuid = ((TamableAnimal)entity).getOwner() != null ? ((TamableAnimal)entity).getOwner().getUUID() : null;
               if (ownerUuid != null && wolfPosition.distanceTo(packetPosition) >= (Double)this.minDistance.get()) {
                  this.info(this.formatMessage("Wolf has teleported to ", packetPosition));
               }
            }
         } catch (NullPointerException var7) {
         }
      } else if (event.packet instanceof ClientboundLevelEventPacket worldEventS2CPacket && worldEventS2CPacket.isGlobalEvent()) {
         if (PlayerUtils.distanceTo(worldEventS2CPacket.getPos()) <= (Double)this.minDistance.get()) {
            return;
         }

         switch (worldEventS2CPacket.getType()) {
            case 1023:
               if ((Boolean)this.withers.get()) {
                  this.info(this.formatMessage("Wither spawned at ", worldEventS2CPacket.getPos()));
               }
               break;
            case 1028:
               if ((Boolean)this.enderDragons.get()) {
                  this.info(this.formatMessage("Ender dragon killed at ", worldEventS2CPacket.getPos()));
               }
               break;
            case 1038:
               if ((Boolean)this.endPortals.get()) {
                  this.info(this.formatMessage("End portal opened at ", worldEventS2CPacket.getPos()));
               }
               break;
            default:
               if ((Boolean)this.otherEvents.get()) {
                  this.info(this.formatMessage("Unknown global event at ", worldEventS2CPacket.getPos()));
               }
         }
      }
   }

   public MutableComponent formatMessage(String message, Vec3 coords) {
      MutableComponent text = Component.literal(message);
      text.append(ChatUtils.formatCoords(coords));
      text.append(ChatFormatting.GRAY + ".");
      return text;
   }

   public MutableComponent formatMessage(String message, BlockPos coords) {
      return this.formatMessage(message, new Vec3(coords.getX(), coords.getY(), coords.getZ()));
   }
}
