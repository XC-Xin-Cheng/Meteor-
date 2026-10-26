package cn.blockforge.meteorzhcn.util.online;

import cn.blockforge.meteorzhcn.MeteorZhCn;
import cn.blockforge.meteorzhcn.mixin.ModuleAccessor;
import cn.blockforge.meteorzhcn.mixin.SettingAccessor;
import cn.blockforge.meteorzhcn.mixin.SettingGroupAccessor;
import cn.blockforge.meteorzhcn.modules.Translation;
import cn.blockforge.meteorzhcn.util.ChatPrefix;
import cn.blockforge.meteorzhcn.util.TransUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

public final class OnlineTranslator {
   private static final OnlineTranslator INSTANCE = new OnlineTranslator();
   private final TransCache cache = new TransCache(cacheFile());
   private final AtomicBoolean running = new AtomicBoolean(false);
   private final Map<Module, String> moduleDescriptions = Collections.synchronizedMap(new IdentityHashMap<>());
   private final Map<Setting<?>, String> settingDescriptions = Collections.synchronizedMap(new IdentityHashMap<>());

   private OnlineTranslator() {
   }

   public static OnlineTranslator get() {
      return INSTANCE;
   }

   public int cacheSize() {
      return this.cache.size();
   }

   private static File cacheFile() {
      File folder = MeteorClient.FOLDER != null ? MeteorClient.FOLDER : new File(".");
      return new File(folder, "meteor-zh-cn-cache.properties");
   }

   public void start(Translation module, TransMode mode) {
      if (mode.isOnline()) {
         ChatPrefix.install();
         if (!this.running.compareAndSet(false, true)) {
            ChatUtils.warning("联网翻译正在进行中，请稍候。", new Object[0]);
         } else {
            TargetLang target = (TargetLang)module.targetLanguage.get();

            Translator provider;
            String namespace;
            try {
               if (mode == TransMode.COLLOQUIAL) {
                  if (((String)module.sSetApiKey.get()).isBlank()) {
                     this.running.set(false);
                     ChatUtils.warning("口语化翻译需要先在 api-key 里填写密钥。", new Object[0]);
                     return;
                  }

                  provider = new ColloquialTranslator(
                     (String)module.sSetApiUrl.get(),
                     (String)module.sSetApiKey.get(),
                     (String)module.sSetModel.get(),
                     target,
                     (Integer)module.iSetTimeout.get(),
                     (Integer)module.iSetBatchSize.get()
                  );
                  namespace = mode.name() + ":" + target.name();
               } else {
                  MachineProvider machineProvider = (MachineProvider)module.machineProvider.get();
                  if (!target.supportedBy(machineProvider)) {
                     this.running.set(false);
                     ChatUtils.warning(
                        "%s 不支持%s，可以改用：%s；或把 translation-mode 换成联网口语化翻译。", new Object[]{machineProvider.shortName(), target, target.providerSuggestions()}
                     );
                     return;
                  }

                  if (machineProvider.requiresAppId() && ((String)module.sSetMachineAppId.get()).isBlank()) {
                     this.running.set(false);
                     ChatUtils.warning("%s 需要先在 machine-appid 里填写应用标识。", new Object[]{machineProvider});
                     return;
                  }

                  if (machineProvider.requiresKey() && ((String)module.sSetMachineKey.get()).isBlank()) {
                     this.running.set(false);
                     ChatUtils.warning("%s 需要先在 machine-key 里填写密钥。", new Object[]{machineProvider});
                     return;
                  }

                  if (machineProvider.needsCustomUrl() && ((String)module.sSetMachineUrl.get()).isBlank()) {
                     this.running.set(false);
                     ChatUtils.warning("%s 没有内置接口地址，请先在 machine-api-url 里填写地址模板。", new Object[]{machineProvider});
                     return;
                  }

                  provider = new MachineTranslator(
                     machineProvider,
                     target,
                     (String)module.sSetMachineUrl.get(),
                     (String)module.sSetMachineAppId.get(),
                     (String)module.sSetMachineKey.get(),
                     (Integer)module.iSetTimeout.get()
                  );
                  namespace = mode.name() + ":" + machineProvider.name() + ":" + target.name();
               }
            } catch (Throwable var15) {
               this.running.set(false);
               MeteorZhCn.LOG.error("创建联网翻译器失败", var15);
               ChatUtils.error("联网翻译启动失败：" + var15, new Object[0]);
               return;
            }

            Map<String, List<Consumer<String>>> entries;
            try {
               entries = this.collect(module);
            } catch (Throwable var14) {
               this.running.set(false);
               MeteorZhCn.LOG.error("收集待翻译文本失败", var14);
               ChatUtils.error("联网翻译启动失败：" + var14, new Object[0]);
               return;
            }

            List<String> pending = new ArrayList<>();
            int cached = 0;

            for (Entry<String, List<Consumer<String>>> entry : entries.entrySet()) {
               String translation = this.cache.get(namespace, entry.getKey());
               if (translation != null) {
                  for (Consumer<String> apply : entry.getValue()) {
                     apply.accept(translation);
                  }

                  cached++;
               } else {
                  pending.add(entry.getKey());
               }
            }

            if (pending.isEmpty()) {
               this.running.set(false);
               ChatUtils.info("联网翻译完成：全部命中缓存（%d 条）。", new Object[]{cached});
            } else {
               int threads = Math.max(1, Math.min(8, (Integer)module.iSetThreads.get()));
               ChatUtils.info("联网翻译开始：待翻译 %d 条，命中缓存 %d 条，模式 %s，目标语言 %s。", new Object[]{pending.size(), cached, mode, target});
               Thread worker = new Thread(() -> {
                  try {
                     this.run(provider, namespace, entries, pending, threads);
                  } catch (Throwable var7x) {
                     MeteorZhCn.LOG.error("联网翻译失败", var7x);
                     this.finish(namespace, entries, 0, pending.size(), var7x, null);
                  }
               }, "meteor-zh-cn-online");
               worker.setDaemon(true);
               worker.start();
            }
         }
      }
   }

   public void clearCache() {
      this.cache.clear();
      this.cache.save();
      this.onClient(() -> {
         this.reset();
         ChatUtils.info("已清空联网翻译缓存，并把界面文字还原成原文。", new Object[0]);
      });
   }

   private void run(Translator provider, String namespace, Map<String, List<Consumer<String>>> entries, List<String> pending, int threads) {
      List<List<String>> batches = partition(pending, provider.batchSize(), provider.maxCharsPerBatch());
      ExecutorService pool = Executors.newFixedThreadPool(threads, task -> {
         Thread thread = new Thread(task, "meteor-zh-cn-online");
         thread.setDaemon(true);
         return thread;
      });

      try {
         List<Future<List<String>>> futures = new ArrayList<>(batches.size());

         for (List<String> batch : batches) {
            futures.add(pool.submit(() -> provider.translate(batch)));
         }

         int ok = 0;
         int fail = 0;
         int done = 0;
         int reported = 0;
         String firstError = null;

         for (int i = 0; i < batches.size(); i++) {
            List<String> batch = batches.get(i);

            try {
               List<String> raw = futures.get(i).get();
               List<String> result = raw == null ? List.of() : raw;
               if (result.size() < batch.size()) {
                  if (firstError == null) {
                     firstError = "接口只返回 " + result.size() + "/" + batch.size() + " 条译文";
                  }

                  fail += batch.size() - result.size();
               }

               for (int j = 0; j < batch.size(); j++) {
                  String source = batch.get(j);
                  String translation = j < result.size() ? result.get(j) : null;
                  if (translation != null && !translation.isBlank() && !translation.equals(source)) {
                     this.cache.put(namespace, source, translation);
                     ok++;
                  }
               }
            } catch (Exception var24) {
               fail += batch.size();
               if (firstError == null) {
                  Throwable cause = (Throwable)(var24.getCause() != null ? var24.getCause() : var24);
                  firstError = cause.getMessage() == null ? cause.toString() : cause.getMessage();
               }
            }

            done += batch.size();
            int percent = done * 100 / pending.size();
            if (percent - reported >= 10) {
               reported = percent;
               this.onClient(() -> ChatUtils.info("联网翻译进度：%d%%", new Object[]{percent}));
            }
         }

         this.cache.save();
         this.finish(namespace, entries, ok, fail, null, firstError);
      } finally {
         pool.shutdown();
      }
   }

   private void finish(String namespace, Map<String, List<Consumer<String>>> entries, int ok, int fail, Throwable error, String failReason) {
      this.onClient(() -> {
         this.apply(entries, namespace);
         this.running.set(false);
         String message;
         if (error != null) {
            message = "联网翻译中断：" + error.getMessage();
         } else {
            message = "联网翻译完成：新增 " + ok + " 条，失败 " + fail + " 条。";
            if (fail > 0 && failReason != null) {
               message = message + "首个失败原因：" + failReason;
            }
         }

         ChatUtils.sendMsg(error == null ? ChatFormatting.GRAY : ChatFormatting.RED, message, new Object[0]);
      });
   }

   private void apply(Map<String, List<Consumer<String>>> entries, String namespace) {
      for (Entry<String, List<Consumer<String>>> entry : entries.entrySet()) {
         String translation = this.cache.get(namespace, entry.getKey());
         if (translation != null) {
            for (Consumer<String> apply : entry.getValue()) {
               apply.accept(translation);
            }
         }
      }
   }

   private Map<String, List<Consumer<String>>> collect(Translation module) {
      OnlineScope scope = (OnlineScope)module.onlineScope.get();
      TargetLang target = (TargetLang)module.targetLanguage.get();
      Set<String> selected = (Set<String>)module.translationModules.get();
      Map<String, List<Consumer<String>>> entries = new LinkedHashMap<>();

      for (Module moduleI : Modules.get().getAll()) {
         if (selected.contains(TransUtil.getAddonName(moduleI))) {
            this.add(entries, target, moduleI.name, value -> ((ModuleAccessor)moduleI).setTitle(value));
            if (scope != OnlineScope.NAMES) {
               this.moduleDescriptions.putIfAbsent(moduleI, moduleI.description);
               this.add(entries, target, moduleI.description, value -> ((ModuleAccessor)moduleI).setDescription(value));
            }

            if (scope != OnlineScope.MODULES) {
               Settings settings = moduleI.settings;

               for (SettingGroup group : settings.groups) {
                  for (Setting<?> setting : ((SettingGroupAccessor)group).getSettings()) {
                     this.add(entries, target, setting.name, value -> ((SettingAccessor)setting).setTitle(value));
                     if (scope == OnlineScope.ALL) {
                        this.settingDescriptions.putIfAbsent(setting, setting.description);
                        this.add(entries, target, setting.description, value -> ((SettingAccessor)setting).setDescription(value));
                     }
                  }
               }
            }
         }
      }

      return entries;
   }

   private void add(Map<String, List<Consumer<String>>> entries, TargetLang target, String source, Consumer<String> apply) {
      if (isTranslatable(source, target)) {
         entries.computeIfAbsent(source, key -> new ArrayList<>()).add(apply);
      }
   }

   private void reset() {
      for (Module module : Modules.get().getAll()) {
         ((ModuleAccessor)module).setTitle(Utils.nameToTitle(module.name));
         ((ModuleAccessor)module).setDescription(this.moduleDescriptions.getOrDefault(module, module.description));

         for (SettingGroup group : module.settings.groups) {
            for (Setting<?> setting : ((SettingGroupAccessor)group).getSettings()) {
               ((SettingAccessor)setting).setTitle(Utils.nameToTitle(setting.name));
               ((SettingAccessor)setting).setDescription(this.settingDescriptions.getOrDefault(setting, setting.description));
            }
         }
      }
   }

   private void onClient(Runnable task) {
      Minecraft client = Minecraft.getInstance();
      if (client == null) {
         task.run();
      } else {
         client.execute(task);
      }
   }

   private static List<List<String>> partition(List<String> sources, int maxCount, int maxChars) {
      List<List<String>> batches = new ArrayList<>();
      List<String> current = new ArrayList<>();
      int chars = 0;

      for (String source : sources) {
         if (!current.isEmpty() && (current.size() >= maxCount || (long)chars + source.length() > maxChars)) {
            batches.add(current);
            current = new ArrayList<>();
            chars = 0;
         }

         current.add(source);
         chars += source.length();
      }

      if (!current.isEmpty()) {
         batches.add(current);
      }

      return batches;
   }

   private static boolean isTranslatable(String source, TargetLang target) {
      if (source == null) {
         return false;
      } else {
         String text = source.trim();
         if (!text.isEmpty() && !target.alreadyInTargetScript(text)) {
            boolean hasLatin = false;
            boolean hasHan = false;

            for (int i = 0; i < text.length(); i++) {
               char c = text.charAt(i);
               if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                  hasLatin = true;
                  break;
               }

               if (c >= 19968 && c <= '鿿') {
                  hasHan = true;
               }
            }

            return hasLatin || hasHan && target.needsTranslationFromChinese();
         } else {
            return false;
         }
      }
   }
}
