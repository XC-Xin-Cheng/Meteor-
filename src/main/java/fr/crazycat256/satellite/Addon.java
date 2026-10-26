package fr.crazycat256.satellite;

import com.mojang.logging.LogUtils;
import fr.crazycat256.satellite.commands.FrameDrop;
import fr.crazycat256.satellite.commands.TPCamCommand;
import fr.crazycat256.satellite.commands.TPCommand;
import fr.crazycat256.satellite.modules.AutoFrameDupe;
import fr.crazycat256.satellite.modules.EventlessFly;
import fr.crazycat256.satellite.modules.InfiniteClickTP;
import fr.crazycat256.satellite.modules.InfiniteInteract;
import fr.crazycat256.satellite.modules.KnockbackTweaks;
import fr.crazycat256.satellite.modules.NBTTooltip;
import fr.crazycat256.satellite.modules.Phase;
import fr.crazycat256.satellite.modules.SpeedBypass;
import fr.crazycat256.satellite.modules.UiUtils;
import fr.crazycat256.satellite.modules.VecFly;
import fr.crazycat256.satellite.modules.seedmap.SeedMap;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class Addon extends MeteorAddon {
   public static final Logger LOG = LogUtils.getLogger();
   public static final Category CATEGORY = new Category("Satellite");

   public void onInitialize() {
      LOG.info("Initializing Satellite");
      Modules.get().add(new EventlessFly());
      Modules.get().add(new Phase());
      Modules.get().add(new InfiniteClickTP());
      Modules.get().add(new InfiniteInteract());
      Modules.get().add(new KnockbackTweaks());
      Modules.get().add(new SeedMap());
      Modules.get().add(new AutoFrameDupe());
      Modules.get().add(new SpeedBypass());
      Modules.get().add(new NBTTooltip());
      Modules.get().add(new VecFly());
      if (UiUtils.uiUtilsClass != null) {
         Modules.get().add(new UiUtils());
      }

      Commands.add(new FrameDrop());
      Commands.add(new TPCamCommand());
      Commands.add(new TPCommand());
   }

   public void onRegisterCategories() {
      Modules.registerCategory(CATEGORY);
   }

   public String getPackage() {
      return "fr.crazycat256.satellite";
   }
}
