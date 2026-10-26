package nekiplay.meteorplus.features.modules.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import nekiplay.meteorplus.utils.ColorRemover;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AntiBotPlus extends Module {
   private final SettingGroup sgFilters = this.settings.createGroup("Filters");
   private final Setting<Boolean> tab = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("tab")).description("check tab.")).defaultValue(true)).build());
   private final Setting<AntiBotPlus.TabMode> tabMode = this.sgFilters
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("tab-mode"))
                     .description("check tab mode."))
                  .defaultValue(AntiBotPlus.TabMode.Contains))
               .visible(this.tab::get))
            .build()
      );
   private final Setting<Boolean> entityID = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("EntityID")).description("check entity id.")).defaultValue(true)).build());
   private final Setting<Boolean> color = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("Color")).description("check color.")).defaultValue(false)).build());
   private final Setting<Boolean> ground = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("ground")).description("check ground.")).defaultValue(true)).build());
   private final Setting<Boolean> air = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("air")).description("check air.")).defaultValue(false)).build());
   private final Setting<Boolean> InvalidGround = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("Invalid-Ground")).description("check Invalid Ground.")).defaultValue(true)).build());
   private final Setting<Boolean> swing = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("Swing")).description("check Swing.")).defaultValue(false)).build());
   private final Setting<Boolean> health = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("health")).description("check health.")).defaultValue(false)).build());
   private final Setting<Boolean> derp = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("derp")).description("check derp.")).defaultValue(true)).build());
   private final Setting<Boolean> useHash = this.sgFilters
      .add(((Builder)((Builder)((Builder)new Builder().name("Memorize bots")).description("Prevent attacking and blinking esp.")).defaultValue(true)).build());
   private final ArrayList<UUID> hash = new ArrayList<>();
   private final ArrayList<Integer> swings = new ArrayList<>();
   private final ArrayList<Integer> grounds = new ArrayList<>();
   private final ArrayList<Integer> airs = new ArrayList<>();
   private final Map<Integer, Integer> invalidGrounds = new HashMap<>();

   public AntiBotPlus() {
      super(Categories.Combat, "Anti Bot", "Remove bots.");
   }

   public void onDeactivate() {
      this.hash.clear();
   }

   public boolean isBot(Entity entity) {
      return entity instanceof LivingEntity living ? this.isBot(living) : false;
   }

   public boolean isBot(LivingEntity entity) {
      if (!(entity instanceof Player)) {
         return false;
      } else if (!this.isActive()) {
         return false;
      } else if ((Boolean)this.useHash.get() && this.hash.contains(entity.getUUID())) {
         return true;
      } else if ((Boolean)this.color.get() && entity.getDisplayName().getString().replace("§r", "").contains("§")) {
         if ((Boolean)this.useHash.get()) {
            this.hash.add(entity.getUUID());
         }

         return true;
      } else if ((Boolean)this.ground.get() && !this.grounds.contains(entity.getId())) {
         if ((Boolean)this.useHash.get()) {
            this.hash.add(entity.getUUID());
         }

         return true;
      } else if ((Boolean)this.InvalidGround.get() && this.invalidGrounds.getOrDefault(entity.getId(), 0) >= 10) {
         if ((Boolean)this.useHash.get()) {
            this.hash.add(entity.getUUID());
         }

         return true;
      } else if (!(Boolean)this.entityID.get() || entity.getId() < 1000000000 && entity.getId() > -1) {
         if (!(Boolean)this.derp.get() || !(entity.getXRot() > 90.0F) && !(entity.getXRot() < -90.0F)) {
            if ((Boolean)this.swing.get() && !this.swings.contains(entity.getId())) {
               if ((Boolean)this.useHash.get()) {
                  this.hash.add(entity.getUUID());
               }

               return true;
            } else {
               if ((Boolean)this.tab.get()) {
                  String targetname = ColorRemover.GetVerbatim(entity.getDisplayName().getString());
                  if (this.mc != null && this.mc.getConnection() != null) {
                     for (PlayerInfo info : this.mc.getConnection().getOnlinePlayers()) {
                        if (info.getTabListDisplayName() != null) {
                           String networkName = ColorRemover.GetVerbatim(info.getTabListDisplayName().getString());
                           if (this.tabMode.get() == AntiBotPlus.TabMode.Equals) {
                              if (targetname.equals(networkName)) {
                                 return false;
                              }
                           } else if (this.tabMode.get() == AntiBotPlus.TabMode.Contains_LowerCase) {
                              if (targetname.toLowerCase().contains(networkName.toLowerCase())) {
                                 return false;
                              }
                           } else if (targetname.contains(networkName)) {
                              return false;
                           }
                        }
                     }

                     if ((Boolean)this.useHash.get()) {
                        this.hash.add(entity.getUUID());
                     }

                     return true;
                  }
               }

               return entity.getName().getString().isEmpty() || entity.getName() == this.mc.player.getName();
            }
         } else {
            if ((Boolean)this.useHash.get()) {
               this.hash.add(entity.getUUID());
            }

            return true;
         }
      } else {
         if ((Boolean)this.useHash.get()) {
            this.hash.add(entity.getUUID());
         }

         return true;
      }
   }

   @EventHandler
   private void onEntityAdd(EntityAddedEvent event) {
      this.isBot(event.entity);
   }

   @EventHandler
   private void onEntityRemove(EntityRemovedEvent event) {
      if (this.hash.contains(event.entity.getUUID())) {
         Iterator<UUID> iterator = this.hash.iterator();

         while (iterator.hasNext()) {
            if (iterator.next() == event.entity.getUUID()) {
               iterator.remove();
               return;
            }
         }
      }
   }

   @EventHandler
   private void livingEntityMove(Receive event) {
      if (event.packet instanceof ClientboundTeleportEntityPacket packet) {
         if (this.mc.level != null) {
            Entity entity = this.mc.level.getEntity(packet.id());
            if (entity != null) {
               if (entity.onGround()) {
                  this.grounds.add(entity.getId());
               }

               if (!entity.onGround() && !this.airs.contains(entity.getId())) {
                  this.airs.add(entity.getId());
               }

               if (entity.onGround()) {
                  if (entity.yo != entity.getY()) {
                     this.invalidGrounds.put(entity.getId(), this.invalidGrounds.getOrDefault(entity.getId(), 0) + 1);
                  }
               } else {
                  int currentVL = this.invalidGrounds.getOrDefault(entity.getId(), 0) / 2;
                  if (currentVL <= 0) {
                     this.invalidGrounds.remove(entity.getId());
                  } else {
                     this.invalidGrounds.replace(entity.getId(), currentVL);
                  }
               }
            }
         }
      } else if (event.packet instanceof ClientboundAnimatePacket packetx && this.mc.level != null) {
         Entity entity = this.mc.level.getEntity(packetx.getId());
         if (entity != null && entity instanceof LivingEntity && packetx.getAction() == 0 && !this.swings.contains(entity.getId())) {
            this.swings.add(entity.getId());
         }
      }
   }

   @EventHandler
   public void worldEvent(GameLeftEvent event) {
      this.clearAll();
   }

   private void clearAll() {
      this.swings.clear();
      this.grounds.clear();
      this.invalidGrounds.clear();
   }

   public static enum TabMode {
      Equals,
      Contains,
      Contains_LowerCase;

      @Override
      public String toString() {
         return super.toString().replace('_', ' ');
      }
   }
}
