package com.stash.hunt;

import com.mojang.logging.LogUtils;
import com.stash.hunt.hud.EntityList;
import com.stash.hunt.hud.Weather;
import com.stash.hunt.modules.AFKVanillaFly;
import com.stash.hunt.modules.AutoEXPPlus;
import com.stash.hunt.modules.AutoLogPlus;
import com.stash.hunt.modules.AutoPortal;
import com.stash.hunt.modules.BetterStashFinder;
import com.stash.hunt.modules.ChestIndex;
import com.stash.hunt.modules.DiscordNotifs;
import com.stash.hunt.modules.ElytraFlyPlusPlus;
import com.stash.hunt.modules.GotoPosition;
import com.stash.hunt.modules.GrimAirPlace;
import com.stash.hunt.modules.HighlightOldLava;
import com.stash.hunt.modules.NoJumpDelay;
import com.stash.hunt.modules.OldChunkNotifier;
import com.stash.hunt.modules.Pitch40Util;
import com.stash.hunt.modules.TrailFollower;
import com.stash.hunt.modules.TrailMaker;
import com.stash.hunt.modules.VanityESP;
import com.stash.hunt.modules.searcharea.SearchArea;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

public class Addon extends MeteorAddon {
   public static final Logger LOG = LogUtils.getLogger();
   public static final Category CATEGORY = new Category("Jefff Mod");
   public static final HudGroup HUD_GROUP = new HudGroup("Jefff Mod");
   public final Settings settings = new Settings();

   public void onInitialize() {
      LOG.info("Initializing Jefff Mod");
      Modules.get().add(new SearchArea());
      Modules.get().add(new AutoLogPlus());
      Modules.get().add(new GotoPosition());
      Modules.get().add(new ChestIndex());
      Modules.get().add(new HighlightOldLava());
      Modules.get().add(new VanityESP());
      Modules.get().add(new AFKVanillaFly());
      Modules.get().add(new AutoPortal());
      Modules.get().add(new Pitch40Util());
      Modules.get().add(new NoJumpDelay());
      Modules.get().add(new GrimAirPlace());
      Modules.get().add(new DiscordNotifs());
      Modules.get().add(new AutoEXPPlus());
      boolean baritoneLoaded = this.checkModLoaded("baritone", "baritone-meteor");
      boolean xaeroWorldMapLoaded = this.checkModLoaded("xaeroworldmap");
      boolean xaeroMinimapLoaded = this.checkModLoaded("xaerominimap");
      boolean xaeroPlusLoaded = this.checkModLoaded("xaeroplus");
      if (xaeroWorldMapLoaded && xaeroPlusLoaded) {
         if (xaeroMinimapLoaded) {
            Modules.get().add(new BetterStashFinder());
            Modules.get().add(new OldChunkNotifier());
            Modules.get().add(new TrailMaker());
         }

         if (baritoneLoaded) {
            Modules.get().add(new TrailFollower());
         }
      }

      if (baritoneLoaded) {
         Modules.get().add(new ElytraFlyPlusPlus());
      }

      Hud.get().register(Weather.INFO);
      Hud.get().register(EntityList.INFO);
   }

   public void onRegisterCategories() {
      Modules.registerCategory(CATEGORY);
   }

   public String getPackage() {
      return "com.stash.hunt";
   }

   private boolean checkModLoaded(String... modIds) {
      boolean loaded = false;

      for (String id : modIds) {
         if (FabricLoader.getInstance().isModLoaded(id)) {
            loaded = true;
            break;
         }
      }

      if (!loaded) {
         LOG.error("{} not found, disabling modules that require it.", modIds[0]);
      }

      return loaded;
   }
}
