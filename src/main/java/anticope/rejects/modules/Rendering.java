package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;

public class Rendering extends Module {
   private final SettingGroup sgInvisible = this.settings.createGroup("Invisible");
   private final SettingGroup sgFun = this.settings.createGroup("Fun");
   private final Setting<Boolean> structureVoid = this.sgInvisible
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("structure-void")).description("Render structure void blocks.")).defaultValue(true))
               .onChanged(onChanged -> {
                  if (this.isActive()) {
                     this.mc.levelRenderer.allChanged();
                  }
               }))
            .build()
      );
   private final Setting<Rendering.Shader> shaderEnum = this.sgFun
      .add(
         ((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)((meteordevelopment.meteorclient.settings.EnumSetting.Builder)new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                        .name("shader"))
                     .description("Select which shader to use"))
                  .defaultValue(Rendering.Shader.None))
               .onChanged(this::onChanged))
            .build()
      );
   private final Setting<Boolean> dinnerbone = this.sgFun
      .add(
         ((Builder)((Builder)((Builder)new Builder().name("dinnerbone")).description("Apply dinnerbone effects to all entities")).defaultValue(false)).build()
      );
   private final Setting<Boolean> deadmau5Ears = this.sgFun
      .add(((Builder)((Builder)((Builder)new Builder().name("deadmau5-ears")).description("Add deadmau5 ears to all players")).defaultValue(false)).build());
   private final Setting<Boolean> christmas = this.sgFun
      .add(((Builder)((Builder)((Builder)new Builder().name("christmas")).description("Chistmas chest anytime")).defaultValue(false)).build());
   private PostChain shader = null;

   public Rendering() {
      super(MeteorRejectsAddon.CATEGORY, "rendering", "Various Render Tweaks");
   }

   public void onActivate() {
      this.mc.levelRenderer.allChanged();
   }

   public void onDeactivate() {
      this.mc.levelRenderer.allChanged();
   }

   public void onChanged(Rendering.Shader s) {
      if (this.mc.level != null) {
         String name = s.toString().toLowerCase();
         if (name.equals("none")) {
            this.shader = null;
         } else {
            Identifier shaderID = Identifier.withDefaultNamespace(name);
            this.shader = this.mc.getShaderManager().getPostChain(shaderID, LevelTargetBundle.MAIN_TARGETS);
         }
      }
   }

   public boolean renderStructureVoid() {
      return this.isActive() && (Boolean)this.structureVoid.get();
   }

   public PostChain getShaderEffect() {
      return !this.isActive() ? null : this.shader;
   }

   public boolean dinnerboneEnabled() {
      return !this.isActive() ? false : (Boolean)this.dinnerbone.get();
   }

   public boolean deadmau5EarsEnabled() {
      return !this.isActive() ? false : (Boolean)this.deadmau5Ears.get();
   }

   public boolean chistmas() {
      return !this.isActive() ? false : (Boolean)this.christmas.get();
   }

   public static enum Shader {
      None,
      Blur,
      Creeper,
      Invert,
      Spider;
   }
}
