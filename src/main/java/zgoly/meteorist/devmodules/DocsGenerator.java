package zgoly.meteorist.devmodules;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.sounds.SoundEvent;
import org.apache.commons.lang3.StringUtils;
import zgoly.meteorist.Meteorist;

public class DocsGenerator extends Module {
   private final SettingGroup sgWiki = this.settings.createGroup("Wiki");
   private final SettingGroup sgSource = this.settings.createGroup("Source");
   public final Setting<String> modulesPageLink = this.sgWiki
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("modules-page-link"))
                  .description("Link to the modules page on the GitHub wiki. Keep empty to disable."))
               .defaultValue("../../wiki/modules"))
            .build()
      );
   public final Setting<String> commandsPageLink = this.sgWiki
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("commands-page-link"))
                  .description("Link to the commands page on the GitHub wiki. Keep empty to disable."))
               .defaultValue("../../wiki/commands"))
            .build()
      );
   public final Setting<String> presetsPageLink = this.sgWiki
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("presets-page-link"))
                  .description("Link to the presets page on the GitHub wiki. Keep empty to disable."))
               .defaultValue("../../wiki/presets"))
            .build()
      );
   private final Setting<String> sourceLink = this.sgSource
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("source-link")).description("Link to the java source directory. Keep empty to disable."))
               .defaultValue("../blob/main/src/main/java"))
            .build()
      );
   private final Setting<String> sourceFileExtension = this.sgSource
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("source-file-extension")).description("File extension of the java source files."))
               .defaultValue(".java"))
            .build()
      );
   public List<DocsGenerator.PackageInfo> packages = new ArrayList<>();

   public DocsGenerator() {
      super(Meteorist.DEV_CATEGORY, "generator", "Generate documentation for Meteor and its add-ons.");
   }

   public void fillPackages() {
      Modules.get()
         .getAll()
         .forEach(
            module -> {
               String packageName = getPackageName(module.getClass());
               DocsGenerator.PackageInfo packageInfo = this.getPackageInfo(packageName);
               DocsGenerator.PackageInfo.CategoryWrapper existingCategory = packageInfo.categoryWrappers
                  .stream()
                  .filter(wrapper -> wrapper.category.equals(module.category))
                  .findFirst()
                  .orElse(null);
               if (existingCategory != null) {
                  existingCategory.modules.add(module);
               } else {
                  DocsGenerator.PackageInfo.CategoryWrapper categoryWrapper = new DocsGenerator.PackageInfo.CategoryWrapper();
                  categoryWrapper.category = module.category;
                  categoryWrapper.modules = new ArrayList<>(List.of(module));
                  packageInfo.categoryWrappers.add(categoryWrapper);
               }
            }
         );
      Commands.COMMANDS.forEach(command -> {
         String packageName = getPackageName(command.getClass());
         DocsGenerator.PackageInfo packageInfo = this.getPackageInfo(packageName);
         packageInfo.commands.add(command);
      });
      Hud.get()
         .infos
         .forEach(
            (name, info) -> info.presets
               .forEach(
                  preset -> {
                     HudElement element = preset.info.create();
                     Consumer<HudElement> consumer = preset.callback;
                     consumer.accept(element);
                     if (element instanceof TextHud textHud) {
                        String packageName = getPackageName(preset.callback.getClass());
                        DocsGenerator.PackageInfo packageInfo = this.getPackageInfo(packageName);
                        DocsGenerator.PackageInfo.HudElementInfoWrapper existingHudElementInfo = packageInfo.hudElementInfoWrappers
                           .stream()
                           .filter(wrapper -> wrapper.hudElementInfo.equals(info))
                           .findFirst()
                           .orElse(null);
                        if (existingHudElementInfo != null) {
                           DocsGenerator.PackageInfo.HudElementInfoWrapper.TextHudWrapper textHudWrapper = new DocsGenerator.PackageInfo.HudElementInfoWrapper.TextHudWrapper();
                           textHudWrapper.preset = preset;
                           textHudWrapper.textHud = textHud;
                           existingHudElementInfo.textHudWrappers.add(textHudWrapper);
                        } else {
                           DocsGenerator.PackageInfo.HudElementInfoWrapper hudElementInfoWrapper = new DocsGenerator.PackageInfo.HudElementInfoWrapper();
                           hudElementInfoWrapper.hudElementInfo = info;
                           DocsGenerator.PackageInfo.HudElementInfoWrapper.TextHudWrapper textHudWrapper = new DocsGenerator.PackageInfo.HudElementInfoWrapper.TextHudWrapper();
                           textHudWrapper.preset = preset;
                           textHudWrapper.textHud = textHud;
                           hudElementInfoWrapper.textHudWrappers = new ArrayList<>(List.of(textHudWrapper));
                           packageInfo.hudElementInfoWrappers.add(hudElementInfoWrapper);
                        }
                     }
                  }
               )
         );
   }

   public DocsGenerator.PackageInfo getPackageInfo(String packageName) {
      for (DocsGenerator.PackageInfo packageInfo : this.packages) {
         if (packageInfo.name.equals(packageName)) {
            return packageInfo;
         }
      }

      DocsGenerator.PackageInfo newPackageInfo = new DocsGenerator.PackageInfo(packageName);
      newPackageInfo.title = getNameFromPackageName(packageName);
      this.packages.add(newPackageInfo);
      return newPackageInfo;
   }

   public static String getPackageName(Class<?> clazz) {
      String[] split = clazz.getPackage().getName().split("\\.");
      return split[0] + "." + split[1];
   }

   public static String getNameFromPackageName(String packageName) {
      List<String> parts = List.of(packageName.split("\\."));
      return Utils.nameToTitle(parts.getLast());
   }

   public WWidget getWidget(GuiTheme theme) {
      if (this.packages.isEmpty()) {
         this.fillPackages();
      }

      WVerticalList list = theme.verticalList();
      WTable table = theme.table();
      this.packages.forEach(packageInfo -> {
         WLabel label = (WLabel)table.add(theme.label(packageInfo.title)).expandX().widget();
         label.tooltip = packageInfo.name;
         WCheckbox checkbox = (WCheckbox)table.add(theme.checkbox(packageInfo.enabled)).widget();
         checkbox.action = () -> packageInfo.enabled = checkbox.checked;
         table.row();
      });
      list.add(table);
      WHorizontalList actions = theme.horizontalList();
      WButton copyReadme = (WButton)actions.add(theme.button("Copy Readme")).expandX().widget();
      WButton copyWiki = (WButton)actions.add(theme.button("Copy Wiki")).expandX().widget();
      list.add(actions).expandX();
      copyReadme.action = () -> {
         String readme = this.generateReadme();
         this.mc.keyboardHandler.setClipboard(readme);
      };
      copyWiki.action = () -> {
         String wiki = this.generateWiki();
         this.mc.keyboardHandler.setClipboard(wiki);
      };
      return list;
   }

   public String generateReadme() {
      StringBuilder infosBuilder = new StringBuilder();
      this.packages
         .forEach(
            packageInfo -> {
               if (packageInfo.enabled) {
                  StringBuilder infoBuilder = new StringBuilder();
                  if (!packageInfo.categoryWrappers.isEmpty()) {
                     StringBuilder modulesText = new StringBuilder();
                     modulesText.append("## Modules").append("\n");
                     packageInfo.categoryWrappers
                        .forEach(
                           categoryWrapper -> {
                              if (!categoryWrapper.modules.isEmpty()) {
                                 modulesText.append("### ").append(categoryWrapper.category.name).append("\n");
                                 modulesText.append("| № | Module | Description |").append("\n");
                                 modulesText.append("|---|---|---|").append("\n");
                                 int ix = 0;

                                 for (Module module : categoryWrapper.modules) {
                                    ix++;
                                    String moduleName = module.title;
                                    if (!((String)this.modulesPageLink.get()).isEmpty()) {
                                       moduleName = "[" + moduleName + "](" + (String)this.modulesPageLink.get() + "#" + module.name + ")";
                                    }

                                    modulesText.append("| ")
                                       .append(ix)
                                       .append(" | ")
                                       .append(moduleName)
                                       .append(" | ")
                                       .append(module.description)
                                       .append(" |")
                                       .append("\n");
                                 }

                                 modulesText.append("\n");
                              }
                           }
                        );
                     infoBuilder.append((CharSequence)modulesText);
                  }

                  if (!packageInfo.commands.isEmpty()) {
                     StringBuilder commandsText = new StringBuilder();
                     commandsText.append("## Commands").append("\n");
                     commandsText.append("| № | Command | Description |").append("\n");
                     commandsText.append("|---|---|---|").append("\n");
                     int i = 0;

                     for (Command command : packageInfo.commands) {
                        i++;
                        String commandName = "`" + command.getName() + "`";
                        if (!((String)this.commandsPageLink.get()).isEmpty()) {
                           commandName = "[" + commandName + "](" + (String)this.commandsPageLink.get() + "#" + command.getName() + ")";
                        }

                        commandsText.append("| ")
                           .append(i)
                           .append(" | ")
                           .append(commandName)
                           .append(" | ")
                           .append(command.getDescription())
                           .append(" |")
                           .append("\n");
                     }

                     commandsText.append("\n");
                     infoBuilder.append((CharSequence)commandsText);
                  }

                  if (!packageInfo.hudElementInfoWrappers.isEmpty()) {
                     StringBuilder presetsText = new StringBuilder();
                     presetsText.append("## Presets").append("\n");

                     for (DocsGenerator.PackageInfo.HudElementInfoWrapper hudElementInfoWrapper : packageInfo.hudElementInfoWrappers) {
                        if (!hudElementInfoWrapper.textHudWrappers.isEmpty()) {
                           presetsText.append("### ").append(hudElementInfoWrapper.hudElementInfo.title).append("\n");
                           presetsText.append(hudElementInfoWrapper.hudElementInfo.description).append("\n");
                           presetsText.append("| № | Title | Text |").append("\n");
                           presetsText.append("|---|---|---|").append("\n");
                           int i = 0;

                           for (DocsGenerator.PackageInfo.HudElementInfoWrapper.TextHudWrapper textHudWrapper : hudElementInfoWrapper.textHudWrappers) {
                              i++;
                              String presetName = textHudWrapper.preset.title;
                              if (!((String)this.presetsPageLink.get()).isEmpty()) {
                                 presetName = "["
                                    + presetName
                                    + "]("
                                    + (String)this.presetsPageLink.get()
                                    + "#"
                                    + Utils.titleToName(textHudWrapper.preset.title)
                                    + ")";
                              }

                              presetsText.append("| ")
                                 .append(i)
                                 .append(" | ")
                                 .append(presetName)
                                 .append(" | ")
                                 .append("`")
                                 .append(textHudWrapper.textHud.text)
                                 .append("`")
                                 .append(" |")
                                 .append("\n");
                           }

                           presetsText.append("\n");
                        }
                     }

                     infoBuilder.append((CharSequence)presetsText);
                  }

                  infosBuilder.append("# ").append(packageInfo.title).append("\n").append((CharSequence)infoBuilder);
               }
            }
         );
      return infosBuilder.toString();
   }

   public String getClassLink(Class<?> clazz) {
      String filePath = clazz.getName();
      if (filePath.contains("$$Lambda")) {
         filePath = filePath.substring(0, filePath.indexOf("$$Lambda"));
      }

      return (String)this.sourceLink.get() + "/" + filePath.replace(".", "/") + (String)this.sourceFileExtension.get();
   }

   public static String toTitleCase(String str) {
      return StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(str), " "));
   }

   public String generateWiki() {
      StringBuilder infos = new StringBuilder();
      List<DocsGenerator.PackageInfo> filteredPackages = this.packages.stream().filter(packageInfo -> packageInfo.enabled).toList();
      filteredPackages.forEach(
         packageInfo -> {
            infos.append("# ").append(packageInfo.title).append("\n");
            boolean hasModules = !packageInfo.categoryWrappers.isEmpty();
            boolean hasCommands = !packageInfo.commands.isEmpty();
            boolean hasPresets = !packageInfo.hudElementInfoWrappers.isEmpty();
            if (hasModules) {
               infos.append("# Modules\n");
               packageInfo.categoryWrappers
                  .forEach(
                     categoryWrapper -> {
                        if (!categoryWrapper.modules.isEmpty()) {
                           infos.append("## ").append(categoryWrapper.category.name).append("\n");
                           categoryWrapper.modules
                              .forEach(
                                 module -> {
                                    infos.append("### ").append(module.title).append("\n");
                                    if (!((String)this.sourceLink.get()).isEmpty()) {
                                       infos.append("[[\ud83d\udcd6 Source code]](").append(this.getClassLink(module.getClass())).append(")").append("\n\n");
                                    }

                                    infos.append(module.description).append("\n");
                                    if (module.getWidget(new MeteorGuiTheme()) != null) {
                                       infos.append("\n")
                                          .append("> [!NOTE]")
                                          .append("\n")
                                          .append("> This module has additional functionality that cannot be displayed on this page.")
                                          .append("\n");
                                    }

                                    if (!module.settings.groups.isEmpty()) {
                                       module.settings.forEach(settingGroup -> settingGroup.forEach(setting -> {
                                          infos.append("\n");
                                          infos.append("+ #### ").append(setting.title).append("\n");
                                          infos.append("  ").append(setting.description).append("\n");
                                          infos.append("  + Type: `").append(setting.getClass().getSimpleName()).append("`").append("\n");
                                          infos.append("  + **Default**: ").append(this.getValueName(setting.getDefaultValue())).append("\n");
                                          Class<?> clazzx = setting.getClass();
                                          Field[] fieldsx = clazzx.getDeclaredFields();

                                          for (Field fieldx : fieldsx) {
                                             fieldx.setAccessible(true);

                                             try {
                                                Object valuex = this.getValueName(fieldx.get(setting));
                                                if (valuex != null) {
                                                   infos.append("  + ").append(toTitleCase(fieldx.getName())).append(": ").append(valuex).append("\n");
                                                }
                                             } catch (Exception var10x) {
                                             }
                                          }
                                       }));
                                    }

                                    if (!infos.isEmpty() && categoryWrapper.modules.getLast() != module) {
                                       infos.append("\n---\n\n");
                                    }
                                 }
                              );
                        }
                     }
                  );
               if (hasCommands && hasPresets) {
                  infos.append("\n---\n\n");
               }
            }

            if (hasCommands) {
               infos.append("# Commands\n");
               packageInfo.commands.forEach(command -> {
                  infos.append("## `").append(command.getName()).append("`").append("\n");
                  if (!((String)this.sourceLink.get()).isEmpty()) {
                     infos.append("[[\ud83d\udcd6 Source code]](").append(this.getClassLink(command.getClass())).append(")").append("\n\n");
                  }

                  infos.append(command.getDescription()).append("\n");
                  if (!command.getAliases().isEmpty()) {
                     infos.append("+ Aliases: `").append(String.join("`, `", command.getAliases())).append("`").append("\n");
                  }

                  if (!infos.isEmpty() && packageInfo.commands.getLast() != command) {
                     infos.append("\n---\n\n");
                  }
               });
               if (hasPresets) {
                  infos.append("\n---\n\n");
               }
            }

            if (hasPresets) {
               infos.append("# Presets\n");

               for (DocsGenerator.PackageInfo.HudElementInfoWrapper hudElementInfoWrapper : packageInfo.hudElementInfoWrappers) {
                  if (!hudElementInfoWrapper.textHudWrappers.isEmpty()) {
                     infos.append("## ").append(hudElementInfoWrapper.hudElementInfo.title).append("\n");
                     infos.append(hudElementInfoWrapper.hudElementInfo.description).append("\n");

                     for (DocsGenerator.PackageInfo.HudElementInfoWrapper.TextHudWrapper textHudWrapper : hudElementInfoWrapper.textHudWrappers) {
                        infos.append("\n").append("+ ### ").append(textHudWrapper.preset.title).append("\n");
                        if (!((String)this.sourceLink.get()).isEmpty()) {
                           infos.append("  [[\ud83d\udcd6 Source code]](")
                              .append(this.getClassLink(textHudWrapper.preset.callback.getClass()))
                              .append(")")
                              .append("\n");
                        }

                        Class<?> clazz = textHudWrapper.textHud.getClass();
                        Field[] fields = clazz.getDeclaredFields();

                        for (Field field : fields) {
                           field.setAccessible(true);

                           try {
                              Object value = this.getValueName(field.get(textHudWrapper.textHud));
                              if (value != null) {
                                 infos.append("  + ").append(toTitleCase(field.getName())).append(": ").append(value).append("\n");
                              }
                           } catch (Exception var18) {
                           }
                        }
                     }
                  }

                  if (!infos.isEmpty() && packageInfo.hudElementInfoWrappers.getLast() != hudElementInfoWrapper) {
                     infos.append("\n---\n\n");
                  }
               }
            }

            if (!infos.isEmpty() && filteredPackages.getLast() != packageInfo) {
               infos.append("\n---\n\n");
            }
         }
      );
      return infos.toString();
   }

   public String getValueName(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof List<?> list) {
         return list.stream().map(this::getValueName).collect(Collectors.joining(", "));
      } else if (value.getClass().isArray()) {
         return Arrays.<Object>stream((Object[])value).map(this::getValueName).collect(Collectors.joining(", "));
      } else if (value.getClass().getName().contains("$$Lambda")) {
         return "`Lambda expression (cannot be displayed)`";
      } else if (value instanceof SoundEvent soundEvent) {
         return "`" + soundEvent.location().toLanguageKey() + "`";
      } else {
         String stringValue = String.valueOf(value);
         if (stringValue.equals(value.getClass().getName() + "@" + Integer.toHexString(value.hashCode()))) {
            return null;
         } else {
            return stringValue.isEmpty() ? null : "`" + stringValue + "`";
         }
      }
   }

   public static class PackageInfo {
      public String name;
      public String title;
      public List<DocsGenerator.PackageInfo.CategoryWrapper> categoryWrappers = new ArrayList<>();
      public List<Command> commands = new ArrayList<>();
      public List<DocsGenerator.PackageInfo.HudElementInfoWrapper> hudElementInfoWrappers = new ArrayList<>();
      public boolean enabled = true;

      public PackageInfo(String name) {
         this.name = name;
      }

      public static class CategoryWrapper {
         public Category category;
         public List<Module> modules;
      }

      public static class HudElementInfoWrapper {
         public HudElementInfo<?> hudElementInfo;
         public List<DocsGenerator.PackageInfo.HudElementInfoWrapper.TextHudWrapper> textHudWrappers;

         public static class TextHudWrapper {
            public HudElementInfo<?>.Preset preset;
            public TextHud textHud;
         }
      }
   }
}
