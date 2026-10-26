package zgoly.meteorist.modules.rangeactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura.EntityAge;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.modules.rangeactions.rangeactions.BaseRangeAction;
import zgoly.meteorist.modules.rangeactions.rangeactions.CommandsRangeAction;
import zgoly.meteorist.modules.rangeactions.rangeactions.DespawnerRangeAction;
import zgoly.meteorist.modules.rangeactions.rangeactions.InteractionRangeAction;
import zgoly.meteorist.utils.MeteoristUtils;
import zgoly.meteorist.utils.config.MeteoristConfigManager;

public class RangeActions extends Module {
   public static List<BaseRangeAction> rangeActions = new ArrayList<>();
   private final RangeActionFactory factory = new RangeActionFactory();
   private final Map<CommandsRangeAction, Integer> commandDelayTimers = new HashMap<>();
   private final Map<CommandsRangeAction, Integer> commandIndex = new HashMap<>();
   private boolean ignoreStartBreakingBlock;
   private boolean ignoreInteractBlock;
   private boolean ignoreAttackEntity;
   private boolean ignoreInteractEntity;

   public RangeActions() {
      super(Meteorist.CATEGORY, "range-actions", "Combined functionality of different range actions.");
   }

   public CompoundTag toTag() {
      CompoundTag tag = super.toTag();
      ListTag list = new ListTag();

      for (BaseRangeAction rangeAction : rangeActions) {
         CompoundTag mTag = new CompoundTag();
         mTag.putString("type", rangeAction.getTypeName());
         mTag.put("rangeAction", rangeAction.toTag());
         list.add(mTag);
      }

      tag.put("rangeActions", list);
      return tag;
   }

   public Module fromTag(CompoundTag tag) {
      super.fromTag(tag);
      rangeActions.clear();

      for (Tag tagII : tag.getListOrEmpty("rangeActions")) {
         CompoundTag tagI = (CompoundTag)tagII;
         String type = tagI.getStringOr("type", "");
         BaseRangeAction rangeAction = this.factory.createRangeAction(type);
         if (rangeAction != null) {
            CompoundTag rangeActionTag = (CompoundTag)tagI.get("rangeAction");
            if (rangeActionTag != null) {
               rangeAction.fromTag(rangeActionTag);
            }

            rangeActions.add(rangeAction);
         }
      }

      return this;
   }

   public WWidget getWidget(GuiTheme theme) {
      WVerticalList list = theme.verticalList();
      this.fillWidget(theme, list);
      return list;
   }

   private void fillWidget(GuiTheme theme, WVerticalList list) {
      list.clear();

      for (BaseRangeAction rangeAction : rangeActions) {
         list.add(theme.settings(rangeAction.settings)).expandX();
         WContainer container = (WContainer)list.add(theme.horizontalList()).expandX().widget();
         if (rangeActions.size() > 1) {
            WContainer moveContainer = (WContainer)container.add(theme.horizontalList()).expandX().widget();
            int index = rangeActions.indexOf(rangeAction);
            if (index > 0) {
               WButton moveUp = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_UP)).widget();
               moveUp.tooltip = "Move range action up.";
               moveUp.action = () -> {
                  rangeActions.remove(index);
                  rangeActions.add(index - 1, rangeAction);
                  this.fillWidget(theme, list);
               };
            }

            if (index < rangeActions.size() - 1) {
               WButton moveDown = (WButton)moveContainer.add(theme.button(Meteorist.ARROW_DOWN)).widget();
               moveDown.tooltip = "Move range action down.";
               moveDown.action = () -> {
                  rangeActions.remove(index);
                  rangeActions.add(index + 1, rangeAction);
                  this.fillWidget(theme, list);
               };
            }
         }

         WButton copy = (WButton)container.add(theme.button(GuiRenderer.COPY)).widget();
         copy.tooltip = "Duplicate range action.";
         copy.action = () -> {
            rangeActions.add(rangeActions.indexOf(rangeAction), rangeAction.copy());
            this.fillWidget(theme, list);
         };
         WMinus remove = (WMinus)container.add(theme.minus()).widget();
         remove.tooltip = "Remove range action.";
         remove.action = () -> {
            rangeActions.remove(rangeAction);
            this.fillWidget(theme, list);
         };
      }

      if (!rangeActions.isEmpty()) {
         list.add(theme.horizontalSeparator()).expandX();
      }

      WTable controls = (WTable)list.add(theme.table()).expandX().widget();
      WButton createInteraction = (WButton)controls.add(theme.button("New Interaction")).expandX().widget();
      createInteraction.action = () -> {
         InteractionRangeAction interactionAction = new InteractionRangeAction();
         rangeActions.add(interactionAction);
         this.fillWidget(theme, list);
      };
      WButton createDespawner = (WButton)controls.add(theme.button("New Despawner")).expandX().widget();
      createDespawner.action = () -> {
         DespawnerRangeAction rangeAction = new DespawnerRangeAction();
         rangeActions.add(rangeAction);
         this.fillWidget(theme, list);
      };
      WButton createCommands = (WButton)controls.add(theme.button("New Commands")).expandX().widget();
      createCommands.action = () -> {
         CommandsRangeAction rangeAction = new CommandsRangeAction();
         rangeActions.add(rangeAction);
         this.fillWidget(theme, list);
      };
      WButton removeAll = (WButton)controls.add(theme.button("Remove All Range Actions")).expandX().widget();
      removeAll.action = () -> {
         rangeActions.clear();
         this.fillWidget(theme, list);
      };
      MeteoristConfigManager.configManager(theme, list, this);
   }

   @EventHandler
   private void onTick(Post event) {
      this.ignoreStartBreakingBlock = this.ignoreInteractBlock = this.ignoreAttackEntity = this.ignoreInteractEntity = false;
      boolean shouldSneak = false;

      for (BaseRangeAction rangeAction : rangeActions) {
         Entity entity = TargetUtils.get(target -> this.entityCheck(target, rangeAction), (SortPriority)rangeAction.priority.get());
         if (entity != null) {
            switch (rangeAction) {
               case InteractionRangeAction interactionAction:
                  if ((Boolean)interactionAction.ignoreStartBreakingBlock.get()) {
                     this.ignoreStartBreakingBlock = true;
                  }

                  if ((Boolean)interactionAction.ignoreInteractBlock.get()) {
                     this.ignoreInteractBlock = true;
                  }

                  if ((Boolean)interactionAction.ignoreAttackEntity.get()) {
                     this.ignoreAttackEntity = true;
                  }

                  if ((Boolean)interactionAction.ignoreInteractEntity.get()) {
                     this.ignoreInteractEntity = true;
                  }

                  if ((Boolean)interactionAction.enableSneak.get()) {
                     shouldSneak = true;
                  }
                  break;
               case DespawnerRangeAction despawnerAction:
                  if (!(Boolean)despawnerAction.checkRoof.get() || this.mc.level.canSeeSky(this.mc.player.blockPosition().above())) {
                     this.mc.player.setDeltaMovement(this.mc.player.getDeltaMovement().add(0.0, (Double)despawnerAction.upVelocity.get(), 0.0));
                  }
                  break;
               case CommandsRangeAction var16:
                  CommandsRangeAction commandsAction = (CommandsRangeAction)rangeAction;
                  int delay = (Integer)commandsAction.delay.get();
                  int commandsPerTick = (Integer)commandsAction.commandsPerTick.get();
                  this.commandDelayTimers.putIfAbsent(commandsAction, 0);
                  this.commandIndex.putIfAbsent(commandsAction, 0);
                  int timer = this.commandDelayTimers.get(commandsAction);
                  int index = this.commandIndex.get(commandsAction);
                  if (timer < delay) {
                     this.commandDelayTimers.put(commandsAction, timer + 1);
                  } else {
                     for (int i = 0; i < commandsPerTick && index < ((List)commandsAction.commands.get()).size(); i++) {
                        ChatUtils.sendPlayerMsg((String)((List)commandsAction.commands.get()).get(index));
                        index++;
                     }

                     this.commandIndex.put(commandsAction, index);
                     this.commandDelayTimers.put(commandsAction, 0);
                  }

                  if (index >= ((List)commandsAction.commands.get()).size()) {
                     this.commandIndex.put(commandsAction, 0);
                  }
                  continue;
               default:
            }
         }
      }

      if (shouldSneak) {
         this.mc.options.keyShift.setDown(true);
      }
   }

   private boolean entityCheck(Entity entity, BaseRangeAction rangeAction) {
      if (!entity.equals(this.mc.player) && !entity.equals(this.mc.getCameraEntity())) {
         if (entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying() || !entity.isAlive()) {
            return false;
         } else if (PlayerUtils.isWithin(entity, (Double)rangeAction.rangeFrom.get()) || !PlayerUtils.isWithin(entity, (Double)rangeAction.rangeTo.get())) {
            return false;
         } else if (!((Set)rangeAction.entities.get()).contains(entity.getType())) {
            return false;
         } else if ((Boolean)rangeAction.ignoreNamed.get() && entity.hasCustomName()) {
            return false;
         } else if ((Boolean)rangeAction.ignoreTamed.get()
            && entity instanceof OwnableEntity tameable
            && tameable.getOwner() != null
            && tameable.getOwner().equals(this.mc.player)) {
            return false;
         } else {
            if ((Boolean)rangeAction.ignorePassive.get()) {
               Objects.requireNonNull(entity);
               switch (entity) {
                  case EnderMan enderman when !enderman.isCreepy():
                     return false;
                  case ZombifiedPiglin piglin when !piglin.isAggressive():
                     return false;
                  case Wolf wolf when !wolf.isAggressive():
                     return false;
                  default:
               }
            }

            if (entity instanceof Player player) {
               if ((Boolean)rangeAction.ignoreCreative.get() && player.isCreative()) {
                  return false;
               }

               if ((Boolean)rangeAction.ignoreFriends.get() && !Friends.get().shouldAttack(player)) {
                  return false;
               }

               if ((Boolean)rangeAction.ignoreShield.get() && player.isBlocking()) {
                  return false;
               }
            }

            if (entity instanceof Animal animal) {
               return switch ((EntityAge)rangeAction.mobAgeFilter.get()) {
                  case Baby -> animal.isBaby();
                  case Adult -> !animal.isBaby();
                  case Both -> true;
                  default -> throw new MatchException(null, null);
               };
            } else {
               return rangeAction.useFovRange.get() && MeteoristUtils.calculateFov(this.mc.player, entity) > rangeAction.fovRange.get()
                  ? false
                  : (Boolean)rangeAction.ignoreWalls.get() || PlayerUtils.canSeeEntity(entity);
            }
         }
      } else {
         return false;
      }
   }

   @EventHandler(
      priority = 100
   )
   private void onStartBreakingBlockEvent(StartBreakingBlockEvent event) {
      if (this.ignoreStartBreakingBlock) {
         event.cancel();
      }
   }

   @EventHandler
   private void onInteractBlock(InteractBlockEvent event) {
      if (this.ignoreInteractBlock) {
         event.cancel();
      }
   }

   @EventHandler(
      priority = 100
   )
   private void onAttackEntity(AttackEntityEvent event) {
      if (this.ignoreAttackEntity) {
         event.cancel();
      }
   }

   @EventHandler
   private void onInteractEntity(InteractEntityEvent event) {
      if (this.ignoreInteractEntity) {
         event.cancel();
      }
   }
}
