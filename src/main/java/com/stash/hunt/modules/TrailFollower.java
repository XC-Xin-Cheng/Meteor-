package com.stash.hunt.modules;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.stash.hunt.Addon;
import com.stash.hunt.Utils;
import java.time.Duration;
import java.util.ArrayDeque;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.module.ModuleManager;
import xaeroplus.module.impl.OldChunks;
import xaeroplus.module.impl.PaletteNewChunks;

public class TrailFollower extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   public final Setting<Integer> maxTrailLength = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("max-trail-length"))
                  .description(
                     "The number of trail points to keep for the average. Adjust to change how quickly the average will change. More does not necessarily equal better because if the list is too long it will contain chunks behind you."
                  ))
               .defaultValue(20))
            .sliderRange(1, 100)
            .build()
      );
   public final Setting<Integer> chunksBeforeStarting = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("chunks-before-starting"))
                  .description("Useful for afking looking for a trail. The amount of chunks before it gets detected as a trail."))
               .defaultValue(10))
            .sliderRange(1, 50)
            .build()
      );
   public final Setting<Integer> chunkConsiderationWindow = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("chunk-timeframe"))
                  .description("The amount of time in seconds that the chunks must be found in before starting."))
               .defaultValue(5))
            .sliderRange(1, 20)
            .build()
      );
   public final Setting<TrailFollower.TrailEndBehavior> trailEndBehavior = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("trail-end-behavior"))
                  .description("What to do when the trail ends."))
               .defaultValue(TrailFollower.TrailEndBehavior.DISABLE))
            .build()
      );
   public final Setting<Double> trailEndYaw = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("trail-end-yaw"))
                  .description("The direction to go after the trail is abandoned."))
               .defaultValue(0.0)
               .sliderRange(0.0, 359.9)
               .visible(() -> this.trailEndBehavior.get() == TrailFollower.TrailEndBehavior.FLY_TOWARDS_YAW))
            .build()
      );
   public final Setting<TrailFollower.OverworldFlightMode> overworldFlightMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("overworld-flight-mode"))
                  .description(
                     "Choose how TrailFollower flies in Overworld. If other is selected then nothing will be automatically enabled, instead just your yaw will be changed to point towards the trail."
                  ))
               .defaultValue(TrailFollower.OverworldFlightMode.PITCH40))
            .build()
      );
   public final Setting<TrailFollower.NetherPathMode> netherPathMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("nether-path-mode"))
                  .description(
                     "Choose how TrailFollower does baritone pathing in Nether. If other is selected then nothing will be automatically enabled, instead just your yaw will be changed to point towards the trail."
                  ))
               .defaultValue(TrailFollower.NetherPathMode.AVERAGE))
            .build()
      );
   public final Setting<Boolean> pitch40Firework = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                        .name("auto-firework"))
                     .description("Uses a firework automatically if your velocity is too low."))
                  .defaultValue(true))
               .visible(() -> this.overworldFlightMode.get() == TrailFollower.OverworldFlightMode.PITCH40))
            .build()
      );
   public final Setting<Double> rotateScaling = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("rotate-scaling"))
               .description("Scaling of how fast the yaw changes. 1 = instant, 0 = doesn't change"))
            .defaultValue(0.1)
            .sliderRange(0.0, 1.0)
            .build()
      );
   public final Setting<Boolean> oppositeDimension = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("opposite-dimension"))
                  .description("Follows trails from the opposite dimension (Requires that you've already loaded the other dimension with XP)."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Boolean> autoElytra = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("auto-start-baritone-elytra"))
                  .description("Starts baritone elytra for you."))
               .defaultValue(false))
            .build()
      );
   private final SettingGroup sgAdvanced = this.settings.createGroup("Advanced", false);
   public final Setting<Double> pathDistance = this.sgAdvanced
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("path-distance"))
                  .description("The distance to add trail positions in the direction the player is facing. (Ignored when following overworld from nether)"))
               .defaultValue(500.0)
               .sliderRange(100.0, 2000.0)
               .onChanged(value -> this.pathDistanceActual = value))
            .build()
      );
   public final Setting<TrailFollower.FollowMode> flightMethod = this.sgAdvanced
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("flight-method"))
                  .description("Decided how the goals will be used. Leave this on AUTO unless you want to use yaw lock in the nether for example."))
               .defaultValue(TrailFollower.FollowMode.AUTO))
            .build()
      );
   public final Setting<Double> startDirectionWeighting = this.sgAdvanced
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("start-direction-weight"))
               .description(
                  "The weighting of the direction the player is facing when starting the trail. 0 for no weighting (not recommended) 1 for max weighting (will take a bit for direction to change)"
               ))
            .defaultValue(0.5)
            .min(0.0)
            .sliderMax(1.0)
            .build()
      );
   public final Setting<TrailFollower.DirectionWeighting> directionWeighting = this.sgAdvanced
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                     .name("direction-weighting"))
                  .description(
                     "How the chunks found should be weighted. Useful for path splits. Left will weight chunks to the left of the player higher, right will weigh chunks to the right higher, and none will be in the middle/random. "
                  ))
               .defaultValue(TrailFollower.DirectionWeighting.NONE))
            .build()
      );
   public final Setting<Integer> directionWeightingMultiplier = this.sgAdvanced
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("direction-weighting-multiplier"))
                     .description(
                        "The multiplier for how much weight should be given to chunks in the direction specified. Values are capped to be in the range [2, maxTrailLength]."
                     ))
                  .defaultValue(2))
               .min(2)
               .sliderMax(10)
               .visible(() -> this.directionWeighting.get() != TrailFollower.DirectionWeighting.NONE))
            .build()
      );
   public final Setting<Boolean> only112 = this.sgAdvanced
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("follow-only-1.12"))
                  .description("Will only follow 1.12 chunks and will ignore other ones."))
               .defaultValue(false))
            .build()
      );
   public final Setting<Double> chunkFoundTimeout = this.sgAdvanced
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("chunk-found-timeout"))
               .description("The amount of MS without a chunk found to trigger circling."))
            .defaultValue(5000.0)
            .min(1000.0)
            .sliderMax(10000.0)
            .build()
      );
   public final Setting<Double> circlingDegPerTick = this.sgAdvanced
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("Circling-degrees-per-tick"))
               .description("The amount of degrees to change per tick while circling."))
            .defaultValue(2.0)
            .min(1.0)
            .sliderMax(20.0)
            .build()
      );
   public final Setting<Double> trailTimeout = this.sgAdvanced
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("trail-timeout"))
               .description("The amount of MS without a chunk found to stop following the trail."))
            .defaultValue(30000.0)
            .min(10000.0)
            .sliderMax(60000.0)
            .build()
      );
   public final Setting<Double> maxTrailDeviation = this.sgAdvanced
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                  .name("max-trail-deviation"))
               .description("Maximum allowed angle (in degrees) from the original trail direction. Helps avoid switching to intersecting trails."))
            .defaultValue(180.0)
            .min(1.0)
            .sliderMax(270.0)
            .build()
      );
   public final Setting<Integer> chunkCacheLength = this.sgAdvanced
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("chunk-cache-length"))
                  .description("The amount of chunks to keep in the cache. (Won't be applied until deactivating)"))
               .defaultValue(100000))
            .sliderRange(0, 10000000)
            .build()
      );
   public final Setting<String> webhookLink = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)((meteordevelopment.meteorclient.settings.StringSetting.Builder)new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                     .name("webhook-link"))
                  .description("Will send all updates to the webhook link. Leave blank to disable."))
               .defaultValue(""))
            .build()
      );
   public final Setting<Integer> baritoneUpdateTicks = this.sgAdvanced
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("baritone-path-update-ticks"))
                  .description("The amount of ticks between updates to the baritone goal. Low values may cause high instability."))
               .defaultValue(100))
            .sliderRange(20, 600)
            .build()
      );
   public final Setting<Boolean> debug = this.sgAdvanced
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("debug"))
                  .description("Debug mode."))
               .defaultValue(false))
            .build()
      );
   private boolean oldAutoFireworkValue;
   private TrailFollower.FollowMode followMode;
   private boolean followingTrail = false;
   private ArrayDeque<Vec3> trail = new ArrayDeque<>();
   private ArrayDeque<Vec3> possibleTrail = new ArrayDeque<>();
   private long lastFoundTrailTime;
   private long lastFoundPossibleTrailTime;
   private double pathDistanceActual = (Double)this.pathDistance.get();
   private Cache<Long, Byte> seenChunksCache = Caffeine.newBuilder()
      .maximumSize(((Integer)this.chunkCacheLength.get()).intValue())
      .expireAfterWrite(Duration.ofMinutes(5L))
      .build();
   boolean started = false;
   private double targetYaw;
   private int baritoneSetGoalTicks = 0;
   Vec3 posDebug;

   public TrailFollower() {
      super(Addon.CATEGORY, "TrailFollower", "Automatically follows trails in all dimensions.");
   }

   void resetTrail() {
      this.baritoneSetGoalTicks = 0;
      this.followingTrail = false;
      this.trail = new ArrayDeque<>();
      this.possibleTrail = new ArrayDeque<>();
   }

   public void onActivate() {
      this.resetTrail();
      XaeroPlus.EVENT_BUS.register(this);
      if (this.started) {
         if (this.mc.player != null && this.mc.level != null) {
            ResourceKey<Level> currentDimension = this.mc.level.dimension();
            if ((Boolean)this.oppositeDimension.get()) {
               if (currentDimension.equals(Level.END)) {
                  this.info("There is no opposite dimension to the end. Disabling TrailFollower", new Object[0]);
                  this.toggle();
                  return;
               }

               if (currentDimension.equals(Level.NETHER)) {
                  this.info("Following overworld trails from the nether is not supported yet, sorry. Disabling TrailFollower", new Object[0]);
                  this.toggle();
                  return;
               }
            }

            if (this.flightMethod.get() != TrailFollower.FollowMode.AUTO) {
               this.followMode = (TrailFollower.FollowMode)this.flightMethod.get();
            } else if (!currentDimension.equals(Level.NETHER)) {
               this.followMode = TrailFollower.FollowMode.YAWLOCK;
               this.info("You are in the overworld or end, basic yaw mode will be used.", new Object[0]);
            } else {
               try {
                  Class.forName("baritone.api.BaritoneAPI");
                  this.followMode = TrailFollower.FollowMode.BARITONE;
                  this.info("You are in the nether, baritone mode will be used.", new Object[0]);
               } catch (ClassNotFoundException var5) {
                  this.info("Baritone is required to trail follow in the nether. Disabling TrailFollower", new Object[0]);
                  this.toggle();
                  return;
               }
            }

            if (this.followMode == TrailFollower.FollowMode.YAWLOCK && !this.mc.level.dimension().equals(Level.NETHER)) {
               if (this.overworldFlightMode.get() == TrailFollower.OverworldFlightMode.PITCH40) {
                  Class<? extends Module> pitch40Util = Pitch40Util.class;
                  Module pitch40UtilModule = Modules.get().get(pitch40Util);
                  if (!pitch40UtilModule.isActive()) {
                     pitch40UtilModule.toggle();
                     if ((Boolean)this.pitch40Firework.get()) {
                        Setting<Boolean> setting = pitch40UtilModule.settings.get("auto-firework");
                        this.info(
                           "Auto Firework enabled, if you want to change the velocity threshold or the firework cooldown check the settings under Pitch40Util.",
                           new Object[0]
                        );
                        this.oldAutoFireworkValue = (Boolean)setting.get();
                        setting.set(true);
                     }
                  }
               } else if (this.overworldFlightMode.get() == TrailFollower.OverworldFlightMode.VANILLA) {
                  AFKVanillaFly afkVanillaFly = (AFKVanillaFly)Modules.get().get(AFKVanillaFly.class);
                  if (!afkVanillaFly.isActive()) {
                     afkVanillaFly.toggle();
                  }
               }
            }

            Vec3 offset = new Vec3(Math.sin(-this.mc.player.getYRot() * Math.PI / 180.0), 0.0, Math.cos(-this.mc.player.getYRot() * Math.PI / 180.0))
               .normalize()
               .scale((Double)this.pathDistance.get());
            Vec3 targetPos = this.mc.player.position().add(offset);

            for (int i = 0; i < ((Integer)this.maxTrailLength.get()).intValue() * this.startDirectionWeighting.get(); i++) {
               this.trail.add(targetPos);
            }

            this.targetYaw = this.getActualYaw(this.mc.player.getYRot());
         } else {
            this.toggle();
         }

         this.started = true;
      }
   }

   public void onDeactivate() {
      this.started = false;
      this.seenChunksCache = Caffeine.newBuilder()
         .maximumSize(((Integer)this.chunkCacheLength.get()).intValue())
         .expireAfterWrite(Duration.ofMinutes(5L))
         .build();
      XaeroPlus.EVENT_BUS.unregister(this);
      this.trail.clear();
      if (this.followMode != null) {
         switch (this.followMode) {
            case BARITONE:
               BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("cancel");
               break;
            case YAWLOCK:
               if (this.mc.level == null || this.mc.level.dimension().equals(Level.NETHER)) {
                  return;
               }

               if (this.overworldFlightMode.get() == TrailFollower.OverworldFlightMode.VANILLA) {
                  AFKVanillaFly afkVanillaFly = (AFKVanillaFly)Modules.get().get(AFKVanillaFly.class);
                  if (afkVanillaFly != null) {
                     afkVanillaFly.resetYLock();
                     if (afkVanillaFly.isActive()) {
                        afkVanillaFly.toggle();
                     }
                  }
               } else if (this.overworldFlightMode.get() == TrailFollower.OverworldFlightMode.PITCH40) {
                  Class<? extends Module> pitch40Util = Pitch40Util.class;
                  Module pitch40UtilModule = Modules.get().get(pitch40Util);
                  if (pitch40UtilModule.isActive()) {
                     pitch40UtilModule.toggle();
                  }

                  pitch40UtilModule.settings.get("auto-firework").set(this.oldAutoFireworkValue);
               }
         }
      }
   }

   private void circle() {
      if (this.followMode != TrailFollower.FollowMode.BARITONE) {
         this.mc.player.setYRot(this.getActualYaw((float)(this.mc.player.getYRot() + (Double)this.circlingDegPerTick.get())));
         if (this.mc.player.tickCount % 100 == 0) {
            this.log(
               "Circling to look for new chunks, abandoning trail in "
                  + ((Double)this.trailTimeout.get() - (System.currentTimeMillis() - this.lastFoundTrailTime)) / 1000.0
                  + " seconds."
            );
         }
      }
   }

   @EventHandler
   private void onTick(Post event) {
      if (!this.started) {
         this.started = true;
         this.onActivate();
      }

      if (this.mc.player != null && this.mc.level != null) {
         if (this.followingTrail && System.currentTimeMillis() - this.lastFoundTrailTime > (Double)this.trailTimeout.get()) {
            this.resetTrail();
            this.log("Trail timed out, stopping.");
            switch ((TrailFollower.TrailEndBehavior)this.trailEndBehavior.get()) {
               case DISABLE:
                  this.toggle();
                  break;
               case FLY_TOWARDS_YAW:
                  this.targetYaw = (Double)this.trailEndYaw.get();
                  break;
               case DISCONNECT:
                  this.mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket(Component.literal("[TrailFollower] Trail timed out.")));
            }
         }

         if (this.followingTrail && System.currentTimeMillis() - this.lastFoundTrailTime > (Double)this.chunkFoundTimeout.get()) {
            this.circle();
         } else {
            switch (this.followMode) {
               case BARITONE:
                  if (this.baritoneSetGoalTicks > 0) {
                     this.baritoneSetGoalTicks--;
                  } else if (this.baritoneSetGoalTicks == 0) {
                     this.baritoneSetGoalTicks = (Integer)this.baritoneUpdateTicks.get();
                     if (this.mc.level.dimension().equals(Level.NETHER)) {
                        if (!this.trail.isEmpty()) {
                           Vec3 baritoneTarget;
                           if (this.netherPathMode.get() == TrailFollower.NetherPathMode.AVERAGE) {
                              Vec3 averagePos = this.calculateAveragePosition(this.trail);
                              Vec3 directionVec = averagePos.subtract(this.mc.player.position()).normalize();
                              Vec3 predictedPos = this.mc.player.position().add(directionVec.scale(10.0));
                              this.targetYaw = Rotations.getYaw(predictedPos);
                              baritoneTarget = Utils.positionInDirection(this.mc.player.position(), this.targetYaw, this.pathDistanceActual);
                           } else {
                              Vec3 lastPos = this.trail.getLast();
                              baritoneTarget = lastPos;
                           }

                           BaritoneAPI.getProvider()
                              .getPrimaryBaritone()
                              .getCustomGoalProcess()
                              .setGoalAndPath(new GoalXZ((int)baritoneTarget.x, (int)baritoneTarget.z));
                        }
                     } else {
                        Vec3 targetPos = Utils.positionInDirection(this.mc.player.position(), this.targetYaw, this.pathDistanceActual);
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int)targetPos.x, (int)targetPos.z));
                        this.targetYaw = Rotations.getYaw(targetPos);
                     }

                     if ((Boolean)this.autoElytra.get() && BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().currentDestination() == null) {
                        BaritoneAPI.getSettings().elytraTermsAccepted.value = true;
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("elytra");
                     }
                  }
                  break;
               case YAWLOCK:
                  this.mc.player.setYRot(Utils.smoothRotation(this.getActualYaw(this.mc.player.getYRot()), this.targetYaw, (Double)this.rotateScaling.get()));
            }
         }
      }
   }

   @EventHandler
   private void onRender(Render3DEvent event) {
      if ((Boolean)this.debug.get()) {
         Vec3 targetPos = Utils.positionInDirection(this.mc.player.position(), this.targetYaw, 10.0);
         event.renderer.line(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), targetPos.x, targetPos.y, targetPos.z, new Color(255, 0, 0));
         if (this.posDebug != null) {
            event.renderer
               .line(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.posDebug.x, targetPos.y, this.posDebug.z, new Color(0, 0, 255));
         }
      }
   }

   @net.lenni0451.lambdaevents.EventHandler(
      priority = -1
   )
   public void onChunkData(ChunkDataEvent event) {
      if (!event.seenChunk()) {
         ResourceKey<Level> currentDimension = this.mc.level.dimension();
         LevelChunk chunk = event.chunk();
         ChunkPos chunkPos = chunk.getPos();
         long chunkLong = chunkPos.pack();
         if (this.seenChunksCache.getIfPresent(chunkLong) == null) {
            ChunkPos chunkDelta = new ChunkPos(chunkPos.x() - this.mc.player.chunkPosition().x(), chunkPos.z() - this.mc.player.chunkPosition().z());
            if ((Boolean)this.oppositeDimension.get()) {
               if (currentDimension.equals(Level.OVERWORLD)) {
                  chunkPos = new ChunkPos(this.mc.player.chunkPosition().x() / 8 + chunkDelta.x(), this.mc.player.chunkPosition().z() / 8 + chunkDelta.z());
                  currentDimension = Level.NETHER;
               } else if (currentDimension.equals(Level.NETHER)) {
                  chunkPos = new ChunkPos(this.mc.player.chunkPosition().x() * 8 + chunkDelta.x(), this.mc.player.chunkPosition().z() * 8 + chunkDelta.z());
                  currentDimension = Level.OVERWORLD;
               }
            }

            if (this.isValidChunk(chunkPos, currentDimension)) {
               this.seenChunksCache.put(chunkLong, (byte)127);
               Vec3 pos = chunk.getPos().getMiddleBlockPosition(0).getCenter();
               this.posDebug = pos;
               if (!this.followingTrail) {
                  if (System.currentTimeMillis() - this.lastFoundPossibleTrailTime > (Integer)this.chunkConsiderationWindow.get() * 1000) {
                     this.possibleTrail.clear();
                  }

                  this.possibleTrail.add(pos);
                  this.lastFoundPossibleTrailTime = System.currentTimeMillis();
                  if (this.possibleTrail.size() > (Integer)this.chunksBeforeStarting.get()) {
                     this.log("Trail found, starting to follow.");
                     this.followingTrail = true;
                     this.lastFoundTrailTime = System.currentTimeMillis();
                     this.trail.addAll(this.possibleTrail);
                     this.possibleTrail.clear();
                  }
               } else {
                  double chunkAngle = Rotations.getYaw(pos);
                  double angleDiff = Utils.angleDifference(this.targetYaw, chunkAngle);
                  if (!this.followingTrail || !(Math.abs(angleDiff) > (Double)this.maxTrailDeviation.get())) {
                     this.lastFoundTrailTime = System.currentTimeMillis();

                     while (this.trail.size() >= this.maxTrailLength.get()) {
                        this.trail.pollFirst();
                     }

                     if (angleDiff > 0.0 && angleDiff < 90.0 && this.directionWeighting.get() == TrailFollower.DirectionWeighting.LEFT) {
                        for (int i = 0; i < this.directionWeightingMultiplier.get() - 1; i++) {
                           this.trail.pollFirst();
                           this.trail.add(pos);
                        }

                        this.trail.add(pos);
                     } else if (angleDiff < 0.0 && angleDiff > -90.0 && this.directionWeighting.get() == TrailFollower.DirectionWeighting.RIGHT) {
                        for (int i = 0; i < this.directionWeightingMultiplier.get() - 1; i++) {
                           this.trail.pollFirst();
                           this.trail.add(pos);
                        }

                        this.trail.add(pos);
                     } else {
                        this.trail.add(pos);
                     }

                     if (!this.trail.isEmpty()) {
                        if (this.followMode == TrailFollower.FollowMode.YAWLOCK) {
                           Vec3 averagePos = this.calculateAveragePosition(this.trail);
                           Vec3 positionVec = averagePos.subtract(this.mc.player.position()).normalize();
                           Vec3 targetPos = this.mc.player.position().add(positionVec.scale(10.0));
                           this.targetYaw = Rotations.getYaw(targetPos);
                        } else {
                           Vec3 lastTrailPoint = this.trail.getLast();
                           this.targetYaw = Rotations.getYaw(lastTrailPoint);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isValidChunk(ChunkPos chunkPos, ResourceKey<Level> currentDimension) {
      PaletteNewChunks paletteNewChunks = (PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class);
      boolean is119NewChunk = paletteNewChunks.isNewChunk(chunkPos.x(), chunkPos.z(), currentDimension);
      boolean is112OldChunk = ((OldChunks)ModuleManager.getModule(OldChunks.class)).isOldChunk(chunkPos.x(), chunkPos.z(), currentDimension);
      boolean isHighlighted = is119NewChunk || paletteNewChunks.isInverseNewChunk(chunkPos.x(), chunkPos.z(), currentDimension);
      return isHighlighted && (!is119NewChunk && !(Boolean)this.only112.get() || is112OldChunk);
   }

   private Vec3 calculateAveragePosition(ArrayDeque<Vec3> positions) {
      double sumX = 0.0;
      double sumZ = 0.0;

      for (Vec3 pos : positions) {
         sumX += pos.x;
         sumZ += pos.z;
      }

      return new Vec3(sumX / positions.size(), 0.0, sumZ / positions.size());
   }

   private float getActualYaw(float yaw) {
      return (yaw % 360.0F + 360.0F) % 360.0F;
   }

   private void log(String message) {
      this.info(message, new Object[0]);
      if (!((String)this.webhookLink.get()).isEmpty()) {
         Utils.sendWebhook((String)this.webhookLink.get(), "TrailFollower", message, null, this.mc.player.getGameProfile().name());
      }
   }

   public static enum DirectionWeighting {
      LEFT,
      NONE,
      RIGHT;
   }

   public static enum FollowMode {
      AUTO,
      BARITONE,
      YAWLOCK;
   }

   public static enum NetherPathMode {
      AVERAGE,
      OTHER;
   }

   public static enum OverworldFlightMode {
      VANILLA,
      PITCH40,
      OTHER;
   }

   public static enum TrailEndBehavior {
      DISABLE,
      FLY_TOWARDS_YAW,
      DISCONNECT;
   }
}
