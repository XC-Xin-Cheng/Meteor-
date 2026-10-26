package zgoly.meteorist.utils.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.render.prompts.YesNoPrompt;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.utils.MeteoristUtils;

public class MeteoristConfigManager {
   public static void reload(boolean fromPrompt) {
      if (MeteorClient.mc.screen instanceof WidgetScreen screen) {
         if (fromPrompt) {
            if (screen.parent instanceof WidgetScreen screen1) {
               screen1.reload();
            }
         } else {
            screen.reload();
         }
      }
   }

   public static File getFolderPath(Module module) {
      return new File(
         Paths.get(FabricLoader.getInstance().getGameDir().toString(), Meteorist.MOD_ID, MeteoristUtils.removeInvalidChars(module.name)).toString()
      );
   }

   public static void configManager(GuiTheme theme, WVerticalList list, Module module) {
      File folderPath = getFolderPath(module);
      list.add(theme.horizontalSeparator()).expandX();
      WSection configSection = (WSection)list.add(theme.section("Config Manager")).expandX().widget();
      WTable control = (WTable)configSection.add(theme.table()).expandX().widget();
      WTable configTable = (WTable)control.add(theme.table()).expandX().widget();
      fillConfigTable(theme, module, folderPath, configTable);
      control.row();
      WHorizontalList buttons = (WHorizontalList)control.add(theme.horizontalList()).expandX().widget();
      WButton reloadButton = (WButton)buttons.add(theme.button("Reload")).expandX().widget();
      reloadButton.action = () -> {
         configTable.clear();
         fillConfigTable(theme, module, folderPath, configTable);
      };
      WButton openFolder = (WButton)buttons.add(theme.button("Open Folder")).expandX().widget();
      openFolder.action = () -> {
         if (folderPath.exists() || folderPath.mkdirs()) {
            Util.getPlatform().openFile(folderPath);
         }
      };
      control.row();
      control.add(theme.horizontalSeparator()).expandX();
      control.row();
      WTextBox textBox = (WTextBox)control.add(theme.textBox("", "Config Name")).expandX().widget();
      WButton save = (WButton)control.add(theme.button("Save")).widget();
      save.action = () -> {
         if (folderPath.exists() || folderPath.mkdirs()) {
            File file = new File(folderPath, textBox.get() + ".nbt");
            save(module, file);
         }
      };
   }

   public static CompoundTag toTag(Module module) {
      CompoundTag nbtCompound = module.toTag();
      nbtCompound.remove("name");
      nbtCompound.remove("keybind");
      nbtCompound.remove("toggleOnKeyRelease");
      nbtCompound.remove("chatFeedback");
      nbtCompound.remove("favorite");
      nbtCompound.remove("active");
      return nbtCompound;
   }

   public static void fromTag(Module module, CompoundTag nbtCompound) {
      Keybind keybind = module.keybind.copy();
      boolean toggleOnBindRelease = module.toggleOnBindRelease;
      boolean chatFeedback = module.chatFeedback;
      boolean favorite = module.favorite;
      boolean isActive = module.isActive();
      module.fromTag(nbtCompound);
      if (module.keybind != keybind) {
         module.keybind.set(keybind);
      }

      if (module.toggleOnBindRelease != toggleOnBindRelease) {
         module.toggleOnBindRelease = toggleOnBindRelease;
      }

      if (module.chatFeedback != chatFeedback) {
         module.chatFeedback = chatFeedback;
      }

      if (module.favorite != favorite) {
         module.favorite = favorite;
      }

      if (module.isActive() != isActive) {
         module.toggle();
      }
   }

   private static void fillConfigTable(GuiTheme theme, Module module, File folderPath, WTable configTable) {
      if (folderPath.isDirectory()) {
         File[] files = folderPath.listFiles();
         if (files != null) {
            Arrays.stream(files)
               .filter(file -> file.getName().endsWith(".nbt"))
               .forEach(
                  file -> {
                     configTable.add(theme.label(file.getName().replace(".nbt", ""))).expandX();
                     WButton save = (WButton)configTable.add(theme.button("Save")).widget();
                     save.action = () -> save(module, file);
                     WButton load = (WButton)configTable.add(theme.button("Load")).widget();
                     load.action = () -> {
                        try (InputStream inputStream = new FileInputStream(file)) {
                           CompoundTag nbtCompound = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
                           fromTag(module, nbtCompound);
                           MeteorClient.mc
                              .getToastManager()
                              .addToast(
                                 new SystemToast(
                                    SystemToastId.PERIODIC_NOTIFICATION,
                                    Component.nullToEmpty("Config successfully loaded"),
                                    Component.nullToEmpty("Loaded as \"" + file.getName() + "\" with " + nbtCompound.size() + " entries.")
                                 )
                              );
                        } catch (FileNotFoundException var7) {
                           MeteorClient.mc
                              .getToastManager()
                              .addToast(
                                 new SystemToast(
                                    SystemToastId.PERIODIC_NOTIFICATION,
                                    Component.nullToEmpty("Failed to load config"),
                                    Component.nullToEmpty("File not found. Did you delete/rename it?")
                                 )
                              );
                        } catch (Exception var8) {
                           MeteorClient.mc
                              .getToastManager()
                              .addToast(
                                 new SystemToast(
                                    SystemToastId.PERIODIC_NOTIFICATION,
                                    Component.nullToEmpty("Failed to load config"),
                                    Component.nullToEmpty(var8.getMessage())
                                 )
                              );
                        }

                        reload(false);
                     };
                     WMinus remove = (WMinus)configTable.add(theme.minus()).widget();
                     remove.action = () -> ((YesNoPrompt)((YesNoPrompt)((YesNoPrompt)YesNoPrompt.create().title("Delete Config"))
                              .message("Are you sure you want to delete \"" + file.getName() + "\"? This cannot be undone."))
                           .onYes(() -> {
                              if (file.delete()) {
                                 reload(true);
                              }
                           })
                           .dontShowAgainCheckboxVisible(false))
                        .show();
                     configTable.row();
                  }
               );
         }
      }
   }

   private static void save(Module module, File file) {
      save(module, file, false);
   }

   private static void save(Module module, File file, boolean overwrite) {
      if (file.exists() && !overwrite) {
         ((YesNoPrompt)((YesNoPrompt)((YesNoPrompt)YesNoPrompt.create().title("Overwrite Config"))
                  .message("Are you sure you want to overwrite \"" + file.getName() + "\"? This cannot be undone."))
               .onYes(() -> save(module, file, true))
               .dontShowAgainCheckboxVisible(false))
            .show();
      } else {
         try {
            CompoundTag nbtCompound = toTag(module);
            FileOutputStream outputStream = new FileOutputStream(file);
            NbtIo.writeCompressed(nbtCompound, outputStream);
            outputStream.close();
            MeteorClient.mc
               .getToastManager()
               .addToast(
                  new SystemToast(
                     SystemToastId.PERIODIC_NOTIFICATION,
                     Component.nullToEmpty("Config successfully saved"),
                     Component.nullToEmpty("Saved as \"" + file.getName() + "\" with " + nbtCompound.size() + " entries.")
                  )
               );
         } catch (Exception var5) {
            MeteorClient.mc
               .getToastManager()
               .addToast(
                  new SystemToast(SystemToastId.PERIODIC_NOTIFICATION, Component.nullToEmpty("Failed to save config"), Component.nullToEmpty(var5.getMessage()))
               );
         }

         reload(false);
      }
   }
}
