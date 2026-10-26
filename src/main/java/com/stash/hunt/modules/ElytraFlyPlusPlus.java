package com.stash.hunt.modules;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import com.stash.hunt.Addon;
import com.stash.hunt.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class ElytraFlyPlusPlus extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final SettingGroup sgObstaclePasser = this.settings.createGroup("Obstacle Passer");
   private final Setting<Boolean> bounce = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("bounce")).description("Automatically does bounce efly.")).defaultValue(false)).build());
   private final Setting<Boolean> motionYBoost = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("motion-y-boost")).description("Greatly increases speed by cancelling Y momentum."))
                  .defaultValue(false))
               .visible(this.bounce::get))
            .build()
      );
   private final Setting<Boolean> onlyWhileColliding = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("only-while-colliding"))
                     .description("Only enables motion y boost if colliding with a wall."))
                  .defaultValue(true))
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.motionYBoost.get()))
            .build()
      );
   private final Setting<Boolean> tunnelBounce = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("tunnel-bounce"))
                     .description("Allows you to bounce in 1x2 tunnels. This should not be on if you are not in a tunnel."))
                  .defaultValue(false))
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.motionYBoost.get()))
            .build()
      );
   private final Setting<Double> speed = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("speed"))
                  .description("The speed in blocks per second to keep you at."))
               .defaultValue(100.0)
               .sliderRange(20.0, 250.0)
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.motionYBoost.get()))
            .build()
      );
   private final Setting<Boolean> lockPitch = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("lock-pitch")).description("Whether to lock your pitch when bounce is enabled."))
                  .defaultValue(true))
               .visible(this.bounce::get))
            .build()
      );
   private final Setting<Double> pitch = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("pitch"))
                  .description("The pitch to set when bounce is enabled."))
               .defaultValue(90.0)
               .sliderRange(-90.0, 90.0)
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.lockPitch.get()))
            .build()
      );
   private final Setting<Boolean> lockYaw = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("lock-yaw")).description("Whether to lock your yaw when bounce is enabled."))
                  .defaultValue(false))
               .visible(this.bounce::get))
            .build()
      );
   private final Setting<Boolean> useCustomYaw = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("use-custom-yaw"))
                     .description(
                        "Enable this if you want to use a yaw that isn't a factor of 45. WARNING: This effects the baritone goal for obstacle passer, use the default Rotations module if you only want a different yawlock."
                     ))
                  .defaultValue(false))
               .visible(this.bounce::get))
            .build()
      );
   private final Setting<Double> yaw = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("yaw"))
                  .description(
                     "The yaw to set when bounce is enabled. This is auto set to the closest 45 deg angle to you unless Use Custom Yaw is enabled. WARNING: This effects the baritone goal for obstacle passer, use the default Rotations module if you only want a different yawlock."
                  ))
               .defaultValue(0.0)
               .sliderRange(0.0, 359.0)
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.useCustomYaw.get()))
            .build()
      );
   private final Setting<Boolean> highwayObstaclePasser = this.sgObstaclePasser
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("highway-obstacle-passer")).description("Uses baritone to pass obstacles."))
                  .defaultValue(false))
               .visible(this.bounce::get))
            .build()
      );
   private final Setting<Boolean> useCustomStartPos = this.sgObstaclePasser
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("use-custom-start-position"))
                     .description(
                        "Enable and set this ONLY if you are on a ringroad or don't want to be locked to a highway. Otherwise (0, 0) is the start position and will be automatically used."
                     ))
                  .defaultValue(false))
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.highwayObstaclePasser.get()))
            .build()
      );
   private final Setting<BlockPos> startPos = this.sgObstaclePasser
      .add(
         ((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)((meteordevelopment.meteorclient.settings.BlockPosSetting.Builder)new meteordevelopment.meteorclient.settings.BlockPosSetting.Builder()
                        .name("start-position"))
                     .description("The start position to use when using a custom start position."))
                  .defaultValue(new BlockPos(0, 0, 0)))
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.highwayObstaclePasser.get() && (Boolean)this.useCustomStartPos.get()))
            .build()
      );
   private final Setting<Boolean> awayFromStartPos = this.sgObstaclePasser
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("away-from-start-position"))
                     .description(
                        "If true, will go away from the start position instead of towards it. The start pos is (0,0) if it is not set to a custom start pos."
                     ))
                  .defaultValue(true))
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.highwayObstaclePasser.get()))
            .build()
      );
   private final Setting<Double> distance = this.sgObstaclePasser
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("distance"))
                  .description("The distance to set the baritone goal for path realignment."))
               .defaultValue(10.0)
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.highwayObstaclePasser.get()))
            .build()
      );
   private final Setting<Integer> targetY = this.sgObstaclePasser
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("y-level"))
                     .description("The Y level to bounce at. This must be correct or bounce will not start properly."))
                  .defaultValue(120))
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.highwayObstaclePasser.get()))
            .build()
      );
   private final Setting<Boolean> avoidPortalTraps = this.sgObstaclePasser
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("avoid-portal-traps"))
                     .description("Will attempt to detect portal traps on chunk load and avoid them."))
                  .defaultValue(false))
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.highwayObstaclePasser.get()))
            .build()
      );
   private final Setting<Double> portalAvoidDistance = this.sgObstaclePasser
      .add(
         ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)((meteordevelopment.meteorclient.settings.DoubleSetting.Builder)new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                     .name("portal-avoid-distance"))
                  .description("The distance to a portal trap where the obstacle passer will takeover and go around it."))
               .defaultValue(20.0)
               .min(0.0)
               .sliderMax(50.0)
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.highwayObstaclePasser.get() && (Boolean)this.avoidPortalTraps.get()))
            .build()
      );
   private final Setting<Integer> portalScanWidth = this.sgObstaclePasser
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                        .name("portal-scan-width"))
                     .description("The width on the axis of the highway that will be scanned for portal traps."))
                  .defaultValue(5))
               .min(3)
               .sliderMax(10)
               .visible(() -> (Boolean)this.bounce.get() && (Boolean)this.highwayObstaclePasser.get() && (Boolean)this.avoidPortalTraps.get()))
            .build()
      );
   private final Setting<Boolean> fakeFly = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("chestplate-fakefly"))
                  .description("Lets you fly using a chestplate to use almost 0 elytra durability. Must have elytra in hotbar."))
               .defaultValue(false))
            .build()
      );
   private final Setting<Boolean> toggleElytra = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("toggle-elytra"))
                     .description("Equips an elytra on activate, and a chestplate on deactivate."))
                  .defaultValue(false))
               .visible(() -> !(Boolean)this.fakeFly.get()))
            .build()
      );
   private boolean startSprinting;
   private BlockPos portalTrap = null;
   private boolean paused = false;
   private boolean elytraToggled = false;
   private Vec3 lastUnstuckPos;
   private int stuckTimer = 0;
   private Vec3 lastPos;
   private final double maxDistance = 80.0;
   private BlockPos tempPath = null;
   private boolean waitingForChunksToLoad;

   public ElytraFlyPlusPlus() {
      super(Addon.CATEGORY, "ElytraFlyPlusPlus", "Elytra fly with some more features.");
   }

   @EventHandler
   private void onReceivePacket(Receive event) {
      if (!(event.packet instanceof ClientboundPlayerPositionPacket packet) && event.packet instanceof ClientboundContainerClosePacket) {
         event.cancel();
      }
   }

   public void onActivate() {
      if (this.mc.player != null && !this.mc.player.getAbilities().mayfly) {
         this.startSprinting = this.mc.player.isSprinting();
         this.tempPath = null;
         this.portalTrap = null;
         this.paused = false;
         this.waitingForChunksToLoad = false;
         this.elytraToggled = false;
         this.lastPos = this.mc.player.position();
         this.lastUnstuckPos = this.mc.player.position();
         this.stuckTimer = 0;
         if ((Boolean)this.bounce.get() && this.mc.player.position().multiply(1.0, 0.0, 1.0).length() >= 100.0) {
            if (BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().currentDestination() == null) {
               BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null);
            }

            if (!(Boolean)this.useCustomStartPos.get()) {
               this.startPos.set(new BlockPos(0, 0, 0));
            }

            if (!(Boolean)this.useCustomYaw.get()) {
               if (!(this.mc.player.blockPosition().distSqr((Vec3i)this.startPos.get()) < 10000.0) && (Boolean)this.highwayObstaclePasser.get()) {
                  BlockPos directionVec = this.mc.player.blockPosition().subtract((Vec3i)this.startPos.get());
                  double angle = Math.toDegrees(Math.atan2(-directionVec.getX(), directionVec.getZ()));
                  double angleNormalized = Utils.angleOnAxis(angle);
                  if (!(Boolean)this.awayFromStartPos.get()) {
                     angleNormalized += 180.0;
                  }

                  this.yaw.set(angleNormalized);
               } else {
                  double playerAngleNormalized = Utils.angleOnAxis(this.mc.player.getYRot());
                  this.yaw.set(playerAngleNormalized);
               }
            }
         }
      }
   }

   @EventHandler
   private void onPlayerMove(PlayerMoveEvent event) {
      if (this.mc.player != null && event.type == MoverType.SELF && this.enabled() && (Boolean)this.motionYBoost.get() && (Boolean)this.bounce.get()) {
         if (!(Boolean)this.onlyWhileColliding.get() || this.mc.player.horizontalCollision) {
            if (this.lastPos != null) {
               double speedBps = this.mc.player.position().subtract(this.lastPos).multiply(20.0, 0.0, 20.0).length();
               Timer timer = (Timer)Modules.get().get(Timer.class);
               if (timer.isActive()) {
                  speedBps *= timer.getMultiplier();
               }

               if (this.mc.player.onGround() && this.mc.player.isSprinting() && speedBps < (Double)this.speed.get()) {
                  if (speedBps > 20.0 || (Boolean)this.tunnelBounce.get()) {
                     ((IVec3)event.movement).meteor$setY(0.0);
                  }

                  this.mc.player.setDeltaMovement(this.mc.player.getDeltaMovement().x, 0.0, this.mc.player.getDeltaMovement().z);
               }
            }

            this.lastPos = this.mc.player.position();
         }
      }
   }

   public void onDeactivate() {
      if (this.mc.player != null) {
         if ((Boolean)this.bounce.get() && BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().currentDestination() == null) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null);
         }

         this.mc.player.setSprinting(this.startSprinting);
         if ((Boolean)this.toggleElytra.get()
            && !(Boolean)this.fakeFly.get()
            && !this.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem().toString().contains("chestplate")) {
            ((ChestSwap)Modules.get().get(ChestSwap.class)).swap();
         }
      }
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null && !this.mc.player.getAbilities().mayfly) {
         if ((Boolean)this.toggleElytra.get() && !(Boolean)this.fakeFly.get() && !this.elytraToggled) {
            if (!this.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
               ((ChestSwap)Modules.get().get(ChestSwap.class)).swap();
            } else {
               this.elytraToggled = true;
            }
         }

         if (this.enabled()) {
            this.mc.player.setSprinting(true);
         }

         if ((Boolean)this.bounce.get()) {
            if (this.tempPath != null && this.mc.player.blockPosition().distSqr(this.tempPath) < 500.0) {
               this.tempPath = null;
               BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(null);
            } else if (this.tempPath != null) {
               BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(this.tempPath));
               return;
            }

            if ((Boolean)this.highwayObstaclePasser.get() && BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal() != null) {
               return;
            }

            if (this.mc.player.distanceToSqr(this.lastUnstuckPos) < 25.0) {
               this.stuckTimer++;
            } else {
               this.stuckTimer = 0;
               this.lastUnstuckPos = this.mc.player.position();
            }

            if ((Boolean)this.highwayObstaclePasser.get()
               && this.mc.player.position().length() > 100.0
               && (
                  this.mc.player.getY() < ((Integer)this.targetY.get()).intValue()
                     || this.mc.player.getY() > (Integer)this.targetY.get() + 2
                     || this.mc.player.horizontalCollision && !this.mc.player.minorHorizontalCollision
                     || this.portalTrap != null
                        && this.portalTrap.distSqr(this.mc.player.blockPosition())
                           < (Double)this.portalAvoidDistance.get() * (Double)this.portalAvoidDistance.get()
                     || this.waitingForChunksToLoad
                     || this.stuckTimer > 50
               )) {
               this.waitingForChunksToLoad = false;
               this.paused = true;
               BlockPos goal = this.mc.player.blockPosition();
               double currDistance = (Double)this.distance.get();
               if (this.portalTrap != null) {
                  currDistance += this.mc.player.position().distanceTo(this.portalTrap.getCenter());
                  this.portalTrap = null;
                  this.info("Pathing around portal.", new Object[0]);
               }

               do {
                  if (currDistance > 80.0) {
                     this.tempPath = goal;
                     BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(goal));
                     return;
                  }

                  Vec3 unitYawVec = Utils.yawToDirection((Double)this.yaw.get());
                  Vec3 travelVec = this.mc.player.position().subtract(((BlockPos)this.startPos.get()).getCenter());
                  double parallelCurrPosDot = travelVec.multiply(new Vec3(1.0, 0.0, 1.0)).dot(unitYawVec);
                  Vec3 parallelCurrPosComponent = unitYawVec.scale(parallelCurrPosDot);
                  Vec3 pos = ((BlockPos)this.startPos.get()).getCenter().add(parallelCurrPosComponent);
                  pos = Utils.positionInDirection(pos, (Double)this.yaw.get(), currDistance);
                  goal = new BlockPos((int)Math.floor(pos.x), (Integer)this.targetY.get(), (int)Math.floor(pos.z));
                  currDistance++;
                  if (this.mc.level.getBlockState(goal).getBlock() == Blocks.VOID_AIR) {
                     this.waitingForChunksToLoad = true;
                     return;
                  }
               } while (
                  !this.mc.level.getBlockState(goal.below()).isRedstoneConductor(this.mc.level, goal.below())
                     || this.mc.level.getBlockState(goal).getBlock() == Blocks.NETHER_PORTAL
                     || !this.mc.level.getBlockState(goal).isAir()
               );

               BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(goal));
            } else {
               this.paused = false;
               if (!this.enabled()) {
                  return;
               }

               if (!(Boolean)this.fakeFly.get()
                  && this.mc.player.onGround()
                  && (
                     !(Boolean)this.motionYBoost.get()
                        || meteordevelopment.meteorclient.utils.Utils.getPlayerSpeed().multiply(1.0, 0.0, 1.0).length() < (Double)this.speed.get()
                  )) {
                  this.mc.player.jumpFromGround();
               }

               if ((Boolean)this.lockYaw.get()) {
                  this.mc.player.setYRot(((Double)this.yaw.get()).floatValue());
               }

               if ((Boolean)this.lockPitch.get()) {
                  this.mc.player.setXRot(((Double)this.pitch.get()).floatValue());
               }
            }
         }

         if (this.enabled()) {
            if ((Boolean)this.fakeFly.get()) {
               this.doGrimEflyStuff();
            } else {
               this.sendStartFlyingPacket();
            }
         }
      }
   }

   public boolean enabled() {
      return this.isActive()
         && !this.paused
         && this.mc.player != null
         && ((Boolean)this.fakeFly.get() || this.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA));
   }

   private void doGrimEflyStuff() {
      FindItemResult itemResult = InvUtils.findInHotbar(new Item[]{Items.ELYTRA});
      if (itemResult.found()) {
         this.swapToItem(itemResult.slot());
         this.sendStartFlyingPacket();
         if ((Boolean)this.bounce.get()
            && this.mc.player.onGround()
            && (
               !(Boolean)this.motionYBoost.get()
                  || meteordevelopment.meteorclient.utils.Utils.getPlayerSpeed().multiply(1.0, 0.0, 1.0).length() < (Double)this.speed.get()
            )) {
            this.mc.player.jumpFromGround();
         }

         this.swapToItem(itemResult.slot());
      }
   }

   @EventHandler
   private void onPlaySound(PlaySoundEvent event) {
      if ((Boolean)this.fakeFly.get()) {
         for (Identifier identifier : List.of(
            Identifier.parse("minecraft:item.armor.equip_generic"),
            Identifier.parse("minecraft:item.armor.equip_netherite"),
            Identifier.parse("minecraft:item.armor.equip_elytra"),
            Identifier.parse("minecraft:item.armor.equip_diamond"),
            Identifier.parse("minecraft:item.armor.equip_gold"),
            Identifier.parse("minecraft:item.armor.equip_iron"),
            Identifier.parse("minecraft:item.armor.equip_chain"),
            Identifier.parse("minecraft:item.armor.equip_leather"),
            Identifier.parse("minecraft:item.elytra.flying")
         )) {
            if (identifier.equals(event.sound.getIdentifier())) {
               event.cancel();
               break;
            }
         }
      }
   }

   private void swapToItem(int slot) {
      ItemStack chestItem = this.mc.player.getInventory().getItem(38);
      ItemStack hotbarSwapItem = this.mc.player.getInventory().getItem(slot);
      Int2ObjectMap<HashedStack> changedSlots = new Int2ObjectOpenHashMap();
      changedSlots.put(6, HashedStack.create(hotbarSwapItem, this.mc.getConnection().decoratedHashOpsGenenerator()));
      changedSlots.put(slot + 36, HashedStack.create(chestItem, this.mc.getConnection().decoratedHashOpsGenenerator()));
      this.sendSwapPacket(changedSlots, (byte)slot);
   }

   private void sendStartFlyingPacket() {
      if (this.mc.player != null) {
         this.mc.player.connection.send(new ServerboundPlayerCommandPacket(this.mc.player, Action.START_FALL_FLYING));
      }
   }

   private void sendSwapPacket(Int2ObjectMap<HashedStack> changedSlots, byte buttonNum) {
      int syncId = this.mc.player.containerMenu.containerId;
      int stateId = this.mc.player.containerMenu.getStateId();
      this.mc
         .player
         .connection
         .send(new ServerboundContainerClickPacket(syncId, stateId, (short)6, buttonNum, ContainerInput.SWAP, changedSlots, HashedStack.EMPTY));
   }

   @EventHandler
   private void onChunkData(ChunkDataEvent event) {
      if ((Boolean)this.avoidPortalTraps.get() && (Boolean)this.highwayObstaclePasser.get()) {
         ChunkPos pos = event.chunk().getPos();
         BlockPos centerPos = pos.getMiddleBlockPosition((Integer)this.targetY.get());
         Vec3 moveDir = Utils.yawToDirection((Double)this.yaw.get());
         double distanceToHighway = Utils.distancePointToDirection(Vec3.atLowerCornerOf(centerPos), moveDir, this.mc.player.position());
         if (!(distanceToHighway > 21.0)) {
            for (int x = 0; x < 16; x++) {
               for (int z = 0; z < 16; z++) {
                  for (int y = (Integer)this.targetY.get(); y < this.targetY.get() + 3; y++) {
                     BlockPos position = new BlockPos(pos.x() * 16 + x, y, pos.z() * 16 + z);
                     if (!(
                           Utils.distancePointToDirection(Vec3.atLowerCornerOf(position), moveDir, this.mc.player.position())
                              > ((Integer)this.portalScanWidth.get()).intValue()
                        )
                        && this.mc.level.getBlockState(position).getBlock().equals(Blocks.NETHER_PORTAL)) {
                        BlockPos posBehind = new BlockPos(
                           (int)Math.floor(position.getX() + moveDir.x), position.getY(), (int)Math.floor(position.getZ() + moveDir.z)
                        );
                        if ((
                              this.mc.level.getBlockState(posBehind).isRedstoneConductor(this.mc.level, posBehind)
                                 || this.mc.level.getBlockState(posBehind).getBlock() == Blocks.NETHER_PORTAL
                           )
                           && (
                              this.portalTrap == null
                                 || this.portalTrap.distSqr(posBehind) > 100.0
                                    && this.mc.player.blockPosition().distSqr(posBehind) < this.mc.player.blockPosition().distSqr(this.portalTrap)
                           )) {
                           this.portalTrap = posBehind;
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
