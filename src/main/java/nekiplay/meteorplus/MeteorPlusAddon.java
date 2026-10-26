package nekiplay.meteorplus;

import java.util.ArrayList;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import nekiplay.MixinPlugin;
import nekiplay.main.items.ModItems;
import nekiplay.meteorplus.features.commands.ClearInventoryCommand;
import nekiplay.meteorplus.features.commands.EclipCommand;
import nekiplay.meteorplus.features.commands.GotoPlusCommand;
import nekiplay.meteorplus.features.commands.ItemRawIdCommand;
import nekiplay.meteorplus.features.modules.combat.AntiBotPlus;
import nekiplay.meteorplus.features.modules.combat.AutoLeave;
import nekiplay.meteorplus.features.modules.combat.Hunt;
import nekiplay.meteorplus.features.modules.combat.Teams;
import nekiplay.meteorplus.features.modules.combat.TriggerBot;
import nekiplay.meteorplus.features.modules.combat.velocity.VelocityPlus;
import nekiplay.meteorplus.features.modules.integrations.MapIntegration;
import nekiplay.meteorplus.features.modules.integrations.WhereIsIt;
import nekiplay.meteorplus.features.modules.misc.AutoAccept;
import nekiplay.meteorplus.features.modules.misc.ChatPrefix;
import nekiplay.meteorplus.features.modules.misc.CoordinateProtector;
import nekiplay.meteorplus.features.modules.movement.Freeze;
import nekiplay.meteorplus.features.modules.movement.NoJumpDelay;
import nekiplay.meteorplus.features.modules.movement.elytrafly.ElytraFlyPlus;
import nekiplay.meteorplus.features.modules.movement.fastladder.FastLadderPlus;
import nekiplay.meteorplus.features.modules.movement.fly.FlyPlus;
import nekiplay.meteorplus.features.modules.movement.jesus.JesusPlus;
import nekiplay.meteorplus.features.modules.movement.nofall.NoFallPlus;
import nekiplay.meteorplus.features.modules.movement.noslow.NoSlowPlus;
import nekiplay.meteorplus.features.modules.movement.speed.SpeedPlus;
import nekiplay.meteorplus.features.modules.movement.spider.SpiderPlus;
import nekiplay.meteorplus.features.modules.player.AutoDropPlus;
import nekiplay.meteorplus.features.modules.render.EyeFinder;
import nekiplay.meteorplus.features.modules.render.ItemFrameEsp;
import nekiplay.meteorplus.features.modules.render.ItemHighlightPlus;
import nekiplay.meteorplus.features.modules.render.KillEffect;
import nekiplay.meteorplus.features.modules.render.holograms.HologramModule;
import nekiplay.meteorplus.features.modules.world.BedrockStorageBruteforce;
import nekiplay.meteorplus.features.modules.world.GhostBlockFixer;
import nekiplay.meteorplus.features.modules.world.SafeMine;
import nekiplay.meteorplus.features.modules.world.XrayBruteforce;
import nekiplay.meteorplus.features.modules.world.autoobsidianmine.AutoObsidianFarm;
import nekiplay.meteorplus.features.modules.world.customblocks.CustomBlocksModule;
import nekiplay.meteorplus.features.modules.world.timer.TimerPlus;
import nekiplay.meteorplus.settings.ConfigModifier;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeteorPlusAddon extends MeteorAddon {
   public static final Logger LOG = LoggerFactory.getLogger(MeteorPlusAddon.class);
   public static final Category CATEGORYMODS = new Category("Integrations", () -> ModItems.METEOR_PLUS_LOGO_MODS_ITEM.getDefaultInstance());
   public static final String HUD_TITLE = "Meteor+";
   public static final HudGroup HUD_GROUP = new HudGroup("Meteor+");
   private static MeteorPlusAddon instance;
   public static final String METEOR_LOGPREFIX = "[Meteor+]";

   public static MeteorPlusAddon getInstance() {
      return instance;
   }

   public void onInitialize() {
      instance = this;
      LOG.info("[Meteor+] Initializing...");
      ArrayList<String> notFoundIntegrations = new ArrayList<>();
      ArrayList<String> notFoundBaritoneIntegrations = new ArrayList<>();
      ArrayList<String> enabledIntegrations = new ArrayList<>();
      if (MixinPlugin.isXaeroWorldMapPresent) {
         if (!MixinPlugin.isBaritonePresent) {
            notFoundBaritoneIntegrations.add("Xaero's World Map");
         } else {
            enabledIntegrations.add("Xaero's World Map");
         }
      } else {
         notFoundIntegrations.add("Xaero's World Map");
      }

      if (!MixinPlugin.isWhereIsIt) {
         notFoundIntegrations.add("Where is it");
      } else {
         enabledIntegrations.add("Where is it");
      }

      if (!MixinPlugin.isBaritonePresent) {
         notFoundBaritoneIntegrations.add("Hunt");
         notFoundBaritoneIntegrations.add("Freecam");
         notFoundBaritoneIntegrations.add("Waypoints");
         notFoundBaritoneIntegrations.add("Goto+");
      } else {
         enabledIntegrations.add("Hunt");
         enabledIntegrations.add("Freecam");
         enabledIntegrations.add("Waypoints");
         enabledIntegrations.add("Goto+");
      }

      if (!enabledIntegrations.isEmpty()) {
         LOG.info("[Meteor+] Enabling integrations for: " + String.join(", ", enabledIntegrations));
      }

      if (!notFoundBaritoneIntegrations.isEmpty()) {
         LOG.warn("[Meteor+] Not found Baritone for integrations: " + String.join(", ", notFoundBaritoneIntegrations));
      }

      if (!notFoundIntegrations.isEmpty()) {
         LOG.warn("[Meteor+] Not found mods for integrations: " + String.join(", ", notFoundIntegrations));
      }

      MeteorClient.EVENT_BUS.subscribe(new CoordinateProtector());
      ConfigModifier.get();
      LOG.info("[Meteor+] Initializing commands...");
      Commands.add(new ItemRawIdCommand());
      Commands.add(new EclipCommand());
      Commands.add(new ClearInventoryCommand());
      if (MixinPlugin.isBaritonePresent) {
         Commands.add(new GotoPlusCommand());
      }

      LOG.info("[Meteor+] Loaded commands");
      LOG.info("[Meteor+] Initializing better chat custom head...");
      BetterChat.registerCustomHead("[Meteor+]", Identifier.fromNamespaceAndPath("meteorplus", "chat/icon.png"));
      LOG.info("[Meteor+] Loaded better chat");
      LOG.info("[Meteor+] Initializing modules...");
      Modules modules = Modules.get();
      if (MixinPlugin.isBaritonePresent) {
         modules.add(new Hunt());
      }

      modules.add(new CustomBlocksModule());
      modules.add(new Teams());
      modules.add(new HologramModule());
      modules.add(new ChatPrefix());
      modules.add(new ItemHighlightPlus());
      modules.add(new FastLadderPlus());
      modules.add(new TriggerBot());
      modules.add(new EyeFinder());
      modules.add(new AutoDropPlus());
      modules.add(new NoFallPlus());
      modules.add(new TimerPlus());
      modules.add(new SpeedPlus());
      modules.add(new FlyPlus());
      modules.add(new SpiderPlus());
      modules.add(new JesusPlus());
      modules.add(new BedrockStorageBruteforce());
      modules.add(new AutoObsidianFarm());
      modules.add(new XrayBruteforce());
      modules.add(new AutoLeave());
      modules.add(new AutoAccept());
      modules.add(new GhostBlockFixer());
      modules.add(new SafeMine());
      modules.add(new Freeze());
      modules.add(new AntiBotPlus());
      modules.add(new ItemFrameEsp());
      modules.add(new KillEffect());
      modules.add(new ElytraFlyPlus());
      modules.add(new VelocityPlus());
      if (!MixinPlugin.isMeteorRejects) {
         modules.add(new NoJumpDelay());
      } else {
         LOG.warn("[Meteor+] Meteor Rejects detected, removing No Jump Delay");
      }

      modules.add(new NoSlowPlus());
      if (MixinPlugin.isBaritonePresent && MixinPlugin.isXaeroWorldMapPresent) {
         modules.add(new MapIntegration());
      }

      if (MixinPlugin.isWhereIsIt) {
         modules.add(new WhereIsIt());
      }

      LOG.info("[Meteor+] Loaded modules");
      LOG.info("[Meteor+] Initializing hud...");
      LOG.info("[Meteor+] Loaded hud");
      LOG.info("[Meteor+] Full loaded");
   }

   public void onRegisterCategories() {
      LOG.info("[Meteor+] registering categories...");
      if (MixinPlugin.isXaeroWorldMapPresent || MixinPlugin.isWhereIsIt) {
         Modules.registerCategory(CATEGORYMODS);
      }

      LOG.info("[Meteor+] register categories");
   }

   public String getWebsite() {
      return "https://meteorclientplus.github.io";
   }

   public GithubRepo getRepo() {
      return new GithubRepo("MeteorClientPlus", "MeteorPlus", "26.1.2", null);
   }

   public String getCommit() {
      String commit = ((ModContainer)FabricLoader.getInstance().getModContainer("meteorplus").get()).getMetadata().getCustomValue("github:sha").getAsString();
      return commit.isEmpty() ? null : commit.trim();
   }

   public String getPackage() {
      return "nekiplay.meteorplus";
   }
}
