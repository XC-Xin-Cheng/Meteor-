package meteoraddonppp;

import cn.blockforge.meteorzhcn.modules.Translation;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;

public final class MeteorAddonPPPPostInit {
    private static final String READY_KEY = "key.meteor-client.open-gui";
    private static boolean applied;

    private MeteorAddonPPPPostInit() {
    }

    @PostInit
    public static void init() {
        if (applied) return;
        applied = true;

        try {
            Config config = Config.get();
            if (config != null) {
                config.customFont.set(false);
                MeteorClient.LOG.info("[{}] Custom font disabled (custom-font={}).", MeteorAddonPPP.DISPLAY_NAME, config.customFont.get());
            }
        } catch (Throwable throwable) {
            MeteorClient.LOG.warn("[{}] Could not disable the custom font.", MeteorAddonPPP.DISPLAY_NAME, throwable);
        }

        // 应用保存的组件显示开关
        ComponentVisibility.apply();

        MeteorClient.EVENT_BUS.subscribe(new TranslationEnabler());
    }

    private static final class TranslationEnabler {
        private int ticks;

        @EventHandler
        private void onTick(Post event) {
            ticks++;
            String sample = Component.translatable(READY_KEY).getString();
            if (READY_KEY.equals(sample)) {
                if (ticks > 3600) {
                    MeteorClient.LOG.warn("[{}] Language table did not load, giving up on the Chinese defaults.", MeteorAddonPPP.DISPLAY_NAME);
                    MeteorClient.EVENT_BUS.unsubscribe(this);
                }
                return;
            }

            MeteorClient.EVENT_BUS.unsubscribe(this);

            try {
                Translation translation = (Translation) Modules.get().get(Translation.class);
                if (translation != null) {
                    translation.bSetAutoTranslation.set(true);
                    if (!translation.isActive()) {
                        Compat.enableModule(translation);
                    }
                    translation.tran();

                    String recheck = Component.translatable(READY_KEY).getString();
                    Module sampleModule = Modules.get().get("aim-assist");
                    if (sampleModule == null) sampleModule = Modules.get().get("auto-portal");

                    MeteorClient.LOG.info(
                        "[{}] Chinese translation enabled (active={}, language-ready={}, sample-title={}).",
                        MeteorAddonPPP.DISPLAY_NAME, translation.isActive(), !READY_KEY.equals(recheck),
                        sampleModule != null ? sampleModule.title : "n/a"
                    );
                }
            } catch (Throwable throwable) {
                MeteorClient.LOG.warn("[{}] Could not enable the Chinese translation.", MeteorAddonPPP.DISPLAY_NAME, throwable);
            }
        }
    }
}
