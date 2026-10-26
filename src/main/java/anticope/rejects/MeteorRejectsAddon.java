package anticope.rejects;

import anticope.rejects.commands.CenterCommand;
import anticope.rejects.commands.ClearChatCommand;
import anticope.rejects.commands.FillCommand;
import anticope.rejects.commands.GhostCommand;
import anticope.rejects.commands.GiveCommand;
import anticope.rejects.commands.HeadsCommand;
import anticope.rejects.commands.KickCommand;
import anticope.rejects.commands.LocateCommand;
import anticope.rejects.commands.PanicCommand;
import anticope.rejects.commands.ReconnectCommand;
import anticope.rejects.commands.SaveSkinCommand;
import anticope.rejects.commands.SeedCommand;
import anticope.rejects.commands.ServerCommand;
import anticope.rejects.commands.SetBlockCommand;
import anticope.rejects.commands.SetVelocityCommand;
import anticope.rejects.commands.TeleportCommand;
import anticope.rejects.commands.TerrainExport;
import anticope.rejects.gui.hud.RadarHud;
import anticope.rejects.gui.themes.rounded.MeteorRoundedGuiTheme;
import anticope.rejects.modules.AimAssist;
import anticope.rejects.modules.AntiBot;
import anticope.rejects.modules.AntiCrash;
import anticope.rejects.modules.AntiSpawnpoint;
import anticope.rejects.modules.AntiVanish;
import anticope.rejects.modules.ArrowDmg;
import anticope.rejects.modules.AutoBedTrap;
import anticope.rejects.modules.AutoCraft;
import anticope.rejects.modules.AutoDrop;
import anticope.rejects.modules.AutoEnchant;
import anticope.rejects.modules.AutoExtinguish;
import anticope.rejects.modules.AutoFarm;
import anticope.rejects.modules.AutoGrind;
import anticope.rejects.modules.AutoLogin;
import anticope.rejects.modules.AutoPot;
import anticope.rejects.modules.AutoRename;
import anticope.rejects.modules.AutoSoup;
import anticope.rejects.modules.AutoTNT;
import anticope.rejects.modules.AutoWither;
import anticope.rejects.modules.BlockIn;
import anticope.rejects.modules.BoatGlitch;
import anticope.rejects.modules.BoatPhase;
import anticope.rejects.modules.Boost;
import anticope.rejects.modules.BungeeCordSpoof;
import anticope.rejects.modules.ChatBot;
import anticope.rejects.modules.ChestAura;
import anticope.rejects.modules.ChorusExploit;
import anticope.rejects.modules.ColorSigns;
import anticope.rejects.modules.Confuse;
import anticope.rejects.modules.CoordLogger;
import anticope.rejects.modules.CustomPackets;
import anticope.rejects.modules.DebugRender;
import anticope.rejects.modules.ExtraElytra;
import anticope.rejects.modules.FullFlight;
import anticope.rejects.modules.GamemodeNotifier;
import anticope.rejects.modules.GhostMode;
import anticope.rejects.modules.Glide;
import anticope.rejects.modules.InteractionMenu;
import anticope.rejects.modules.ItemGenerator;
import anticope.rejects.modules.Jetpack;
import anticope.rejects.modules.KnockbackPlus;
import anticope.rejects.modules.Lavacast;
import anticope.rejects.modules.LawnBot;
import anticope.rejects.modules.MossBot;
import anticope.rejects.modules.NewChunks;
import anticope.rejects.modules.NoJumpDelay;
import anticope.rejects.modules.ObsidianFarm;
import anticope.rejects.modules.OreSim;
import anticope.rejects.modules.PacketFly;
import anticope.rejects.modules.Painter;
import anticope.rejects.modules.Rendering;
import anticope.rejects.modules.RoboWalk;
import anticope.rejects.modules.ShieldBypass;
import anticope.rejects.modules.SilentDisconnect;
import anticope.rejects.modules.SkeletonESP;
import anticope.rejects.modules.SoundLocator;
import anticope.rejects.modules.TreeAura;
import anticope.rejects.modules.VehicleOneHit;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeteorRejectsAddon extends MeteorAddon {
   public static final Logger LOG = LoggerFactory.getLogger("Rejects");
   public static final Category CATEGORY = new Category("Rejects", () -> Items.BARRIER.getDefaultInstance());
   public static final HudGroup HUD_GROUP = new HudGroup("Rejects");

   public void onInitialize() {
      LOG.info("Initializing Meteor Rejects Addon");
      Modules modules = Modules.get();
      modules.add(new AimAssist());
      modules.add(new AntiBot());
      modules.add(new AntiCrash());
      modules.add(new AntiSpawnpoint());
      modules.add(new AntiVanish());
      modules.add(new ArrowDmg());
      modules.add(new AutoDrop());
      modules.add(new AutoBedTrap());
      modules.add(new AutoCraft());
      modules.add(new AutoExtinguish());
      modules.add(new AutoFarm());
      modules.add(new AutoGrind());
      modules.add(new AutoLogin());
      modules.add(new AutoPot());
      modules.add(new AutoSoup());
      modules.add(new AutoTNT());
      modules.add(new AutoWither());
      modules.add(new BoatGlitch());
      modules.add(new BlockIn());
      modules.add(new BoatPhase());
      modules.add(new Boost());
      modules.add(new BungeeCordSpoof());
      modules.add(new ChatBot());
      modules.add(new ChestAura());
      modules.add(new ChorusExploit());
      modules.add(new ColorSigns());
      modules.add(new Confuse());
      modules.add(new CoordLogger());
      modules.add(new CustomPackets());
      modules.add(new DebugRender());
      modules.add(new ExtraElytra());
      modules.add(new FullFlight());
      modules.add(new GamemodeNotifier());
      modules.add(new GhostMode());
      modules.add(new Glide());
      modules.add(new ItemGenerator());
      modules.add(new InteractionMenu());
      modules.add(new Jetpack());
      modules.add(new KnockbackPlus());
      modules.add(new LawnBot());
      modules.add(new Lavacast());
      modules.add(new MossBot());
      modules.add(new NewChunks());
      modules.add(new NoJumpDelay());
      modules.add(new ObsidianFarm());
      modules.add(new OreSim());
      modules.add(new PacketFly());
      modules.add(new Painter());
      modules.add(new Rendering());
      modules.add(new RoboWalk());
      modules.add(new ShieldBypass());
      modules.add(new SilentDisconnect());
      modules.add(new SkeletonESP());
      modules.add(new SoundLocator());
      modules.add(new TreeAura());
      modules.add(new VehicleOneHit());
      modules.add(new AutoEnchant());
      modules.add(new AutoRename());
      Commands.add(new CenterCommand());
      Commands.add(new ClearChatCommand());
      Commands.add(new FillCommand());
      Commands.add(new GhostCommand());
      Commands.add(new GiveCommand());
      Commands.add(new HeadsCommand());
      Commands.add(new KickCommand());
      Commands.add(new LocateCommand());
      Commands.add(new PanicCommand());
      Commands.add(new ReconnectCommand());
      Commands.add(new ServerCommand());
      Commands.add(new SaveSkinCommand());
      Commands.add(new SeedCommand());
      Commands.add(new SetBlockCommand());
      Commands.add(new SetVelocityCommand());
      Commands.add(new TeleportCommand());
      Commands.add(new TerrainExport());
      Hud hud = (Hud)Systems.get(Hud.class);
      hud.register(RadarHud.INFO);
      GuiThemes.add(new MeteorRoundedGuiTheme());
   }

   public void onRegisterCategories() {
      Modules.registerCategory(CATEGORY);
   }

   public String getWebsite() {
      return "https://github.com/AntiCope/meteor-rejects";
   }

   public GithubRepo getRepo() {
      return new GithubRepo("AntiCope", "meteor-rejects");
   }

   public String getCommit() {
      String commit = ((ModContainer)FabricLoader.getInstance().getModContainer("meteor-rejects").get())
         .getMetadata()
         .getCustomValue("github:sha")
         .getAsString();
      LOG.info("Rejects version: {}", commit);
      return commit.isEmpty() ? null : commit.trim();
   }

   public String getPackage() {
      return "anticope.rejects";
   }
}
