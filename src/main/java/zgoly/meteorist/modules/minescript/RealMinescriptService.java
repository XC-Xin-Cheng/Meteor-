package zgoly.meteorist.modules.minescript;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import net.minescript.common.JobControl;
import net.minescript.common.JobState;
import net.minescript.common.Minescript;
import zgoly.meteorist.mixin.MinescriptAccessor;
import zgoly.meteorist.utils.MeteoristUtils;
import zgoly.meteorist.utils.misc.DebugLogger;

public class RealMinescriptService implements MinescriptService {
   private final Setting<Boolean> censorJWord;
   private final DebugLogger debugLogger;
   private final List<String> scriptStates = new ArrayList<>();
   private final Map<Integer, Integer> jobTickCounts = new HashMap<>();
   private GuiTheme currentTheme;
   private WVerticalList rootList;
   private final List<WLabel> currentJobLabels = new ArrayList<>();
   private boolean isEnabled = false;
   private Set<Entry<Integer, JobState>> lastJobStateSnapshot = Collections.emptySet();
   public static Object jobs;

   public RealMinescriptService(MinescriptIntegration module) {
      SettingGroup sgGeneral = module.settings.getDefaultGroup();
      this.censorJWord = sgGeneral.add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("censor-j-word")).description("Censors the J-word as 'J*b'.")).defaultValue(true))
               .onChanged(v -> this.onCensorSettingChanged()))
            .build()
      );
      this.debugLogger = new DebugLogger(module, module.settings);
   }

   public void onCensorSettingChanged() {
      if (this.currentTheme != null && this.rootList != null) {
         this.refreshWidget();
      }
   }

   @Override
   public WWidget createWidget(GuiTheme theme) {
      this.currentTheme = theme;
      this.rootList = theme.verticalList();
      this.refreshWidget();
      return this.rootList;
   }

   private void refreshWidget() {
      if (this.currentTheme != null && this.rootList != null) {
         this.rootList.clear();
         this.currentJobLabels.clear();
         this.refreshScripts();
         WTable table = (WTable)this.rootList.add(this.currentTheme.table()).expandX().widget();
         if (this.scriptStates.isEmpty()) {
            table.add(this.currentTheme.label("No custom scripts found in minescript/")).expandX();
         } else {
            table.add(this.currentTheme.horizontalSeparator("Custom scripts")).expandX();
            table.row();

            for (String script : this.scriptStates) {
               table.add(this.currentTheme.label(script)).expandX();
               WButton btn = (WButton)table.add(this.currentTheme.button("Run")).widget();
               btn.action = () -> MinescriptAccessor.runMinescriptCommand(script);
               table.row();
            }

            WTable jobsTable = (WTable)table.add(this.currentTheme.table()).expandX().widget();
            Map<Integer, JobControl> activeJobs = getJobMap();
            this.lastJobStateSnapshot = activeJobs.entrySet()
               .stream()
               .map(e -> new SimpleEntry<>(e.getKey(), e.getValue().state()))
               .collect(Collectors.toSet());
            if (!this.isEnabled) {
               jobsTable.add(this.currentTheme.horizontalSeparator()).expandX();
               jobsTable.row();
               jobsTable.add(this.currentTheme.label(this.censored("Module inactive. Activate the module in-world to track job activity.")))
                  .center()
                  .alignWidget();
            } else {
               if (!activeJobs.isEmpty()) {
                  jobsTable.add(this.currentTheme.horizontalSeparator(this.censored("Jobs"))).expandX();
                  jobsTable.row();
               }

               activeJobs.forEach(
                  (id, job) -> {
                     if (job.state() == JobState.KILLED) {
                        this.jobTickCounts.remove(id);
                        this.debugLogger.info(this.censored("Removed killed job ID: (highlight)%d(default)"), id);
                     } else {
                        jobsTable.add(this.currentTheme.label(job.jobSummary())).expandX();
                        this.currentJobLabels.add((WLabel)jobsTable.add(this.currentTheme.label("")).expandX().widget());
                        WButton suspendBtn = (WButton)jobsTable.add(this.currentTheme.button(job.state() == JobState.SUSPENDED ? "Resume" : "Suspend"))
                           .expandX()
                           .widget();
                        suspendBtn.action = () -> {
                           if (job.state() == JobState.SUSPENDED) {
                              job.resume();
                              suspendBtn.set("Resume");
                           } else {
                              job.suspend();
                              suspendBtn.set("Suspend");
                           }

                           this.debugLogger
                              .info(this.censored("%s job ID: (highlight)%d(default)"), job.state() == JobState.SUSPENDED ? "Resumed" : "Suspended", id);
                           this.refreshWidget();
                        };
                        WButton stopBtn = (WButton)jobsTable.add(this.currentTheme.button("Stop")).expandX().widget();
                        stopBtn.action = () -> {
                           job.requestKill();
                           this.jobTickCounts.remove(id);
                           this.debugLogger.info(this.censored("Requested kill for job ID: (highlight)%d(default)"), id);
                           this.refreshWidget();
                        };
                        jobsTable.row();
                     }
                  }
               );
            }
         }
      }
   }

   @Override
   public void refreshScripts() {
      this.scriptStates.clear();

      for (String script : Minescript.config.scriptConfig().findCommandPrefixMatches("")) {
         if (!MinescriptAccessor.getBuiltinCommands().contains(script)) {
            this.scriptStates.add(script);
         }
      }
   }

   @Override
   public void refreshJobsIfNeeded() {
      Map<Integer, JobControl> currentJobs = getJobMap();
      Set<Entry<Integer, JobState>> currentSnapshot = currentJobs.entrySet()
         .stream()
         .map(e -> new SimpleEntry<>(e.getKey(), e.getValue().state()))
         .collect(Collectors.toSet());
      if (!currentSnapshot.equals(this.lastJobStateSnapshot)) {
         for (Integer id : currentJobs.keySet()) {
            this.jobTickCounts.putIfAbsent(id, 0);
         }

         this.jobTickCounts.keySet().removeIf(idx -> !currentJobs.containsKey(idx));
         this.refreshWidget();
      }
   }

   @Override
   public void onTick() {
      this.refreshJobsIfNeeded();
      Map<Integer, JobControl> activeJobs = getJobMap();
      int index = 0;

      for (Entry<Integer, JobControl> entry : activeJobs.entrySet()) {
         Integer id = entry.getKey();
         JobControl job = entry.getValue();
         if (job.state() != JobState.SUSPENDED && job.state() != JobState.KILLED) {
            this.jobTickCounts.merge(id, 1, Integer::sum);
         }

         if (index < this.currentJobLabels.size()) {
            int ticks = this.jobTickCounts.getOrDefault(id, 0);
            String timeStr = MeteoristUtils.ticksToTime(ticks, true, false);
            this.currentJobLabels.get(index).set(timeStr);
         }

         index++;
      }
   }

   private String censored(String input) {
      return !this.censorJWord.get() ? input : input.replaceAll("(?i)(j)o(b)", "$1*$2");
   }

   @Override
   public void onActivate() {
      this.isEnabled = true;
      this.refreshWidget();
   }

   @Override
   public void onDeactivate() {
      this.jobTickCounts.clear();
      this.isEnabled = false;
      this.refreshWidget();
   }

   public static Map<Integer, JobControl> getJobMap() {
      try {
         Field jobManagerField = Minescript.class.getDeclaredField("jobs");
         jobManagerField.setAccessible(true);
         Object jobManager = jobManagerField.get(null);
         Field jobMapField = jobManager.getClass().getDeclaredField("jobMap");
         jobMapField.setAccessible(true);
         return (Map<Integer, JobControl>)jobMapField.get(jobManager);
      } catch (Exception var3) {
         return Collections.emptyMap();
      }
   }
}
