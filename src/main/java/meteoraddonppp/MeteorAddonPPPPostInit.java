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
   private static final String SAMPLE_KEY = "meteor.jefff's_mods.meteor+++.auto_portal.name";
   private static boolean applied;

   private MeteorAddonPPPPostInit() {
   }

   @PostInit
   public static void init() {
      if (!applied) {
         applied = true;

         try {
            Config var0 = Config.get();
            if (var0 != null) {
               var0.customFont.set(false);
               MeteorClient.LOG.info("[{}] Custom font disabled (custom-font={}).", "Meteor Addon+++", var0.customFont.get());
            }
         } catch (Throwable var1) {
            MeteorClient.LOG.warn("[{}] Could not disable the custom font.", "Meteor Addon+++", var1);
         }

         MeteorClient.EVENT_BUS.subscribe(new MeteorAddonPPPPostInit.TranslationEnabler());
      }
   }

   private static final class TranslationEnabler {
      private int ticks;

      @EventHandler
      private void onTick(Post var1) {
         this.ticks++;
         String var2 = Component.translatable("key.meteor-client.open-gui").getString();
         if ("key.meteor-client.open-gui".equals(var2)) {
            if (this.ticks > 3600) {
               MeteorClient.LOG.warn("[{}] Language table did not load, giving up on the Chinese defaults.", "Meteor Addon+++");
               MeteorClient.EVENT_BUS.unsubscribe(this);
            }
         } else {
            MeteorClient.EVENT_BUS.unsubscribe(this);

            try {
               Translation var3 = (Translation)Modules.get().get(Translation.class);
               if (var3 != null) {
                  var3.bSetAutoTranslation.set(true);
                  if (!var3.isActive()) {
                     var3.enable();
                  }

                  var3.tran();
                  String var4 = Component.translatable("key.meteor-client.open-gui").getString();
                  Module var5 = Modules.get().get("aim-assist");
                  if (var5 == null) {
                     var5 = Modules.get().get("auto-portal");
                  }

                  MeteorClient.LOG
                     .info(
                        "[{}] Chinese translation enabled (active={}, language-ready={}, sample-title={}).",
                        new Object[]{"Meteor Addon+++", var3.isActive(), !"key.meteor-client.open-gui".equals(var4), var5 != null ? var5.title : "n/a"}
                     );
               }
            } catch (Throwable var6) {
               MeteorClient.LOG.warn("[{}] Could not enable the Chinese translation.", "Meteor Addon+++", var6);
            }
         }
      }
   }
}
