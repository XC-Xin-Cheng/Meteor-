package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.BreezeDebugRenderer;
import net.minecraft.client.renderer.debug.ChunkCullingDebugRenderer;
import net.minecraft.client.renderer.debug.ChunkDebugRenderer;
import net.minecraft.client.renderer.debug.CollisionBoxRenderer;
import net.minecraft.client.renderer.debug.EntityBlockIntersectionDebugRenderer;
import net.minecraft.client.renderer.debug.GameEventListenerRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.HeightMapRenderer;
import net.minecraft.client.renderer.debug.LightDebugRenderer;
import net.minecraft.client.renderer.debug.LightSectionDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.OctreeDebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.client.renderer.debug.PoiDebugRenderer;
import net.minecraft.client.renderer.debug.RaidDebugRenderer;
import net.minecraft.client.renderer.debug.RedstoneWireOrientationsRenderer;
import net.minecraft.client.renderer.debug.SolidFaceRenderer;
import net.minecraft.client.renderer.debug.StructureRenderer;
import net.minecraft.client.renderer.debug.SupportBlockRenderer;
import net.minecraft.client.renderer.debug.VillageSectionsDebugRenderer;
import net.minecraft.client.renderer.debug.WaterDebugRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer.SimpleDebugRenderer;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.level.LightLayer;

public class DebugRender extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Map<Setting<Boolean>, SimpleDebugRenderer> renderers = new HashMap<>();

   public DebugRender() {
      super(MeteorRejectsAddon.CATEGORY, "debug-renders", "Render useful debug information.");
      BrainDebugRenderer brainRenderer = new BrainDebugRenderer(this.mc);
      this.addRenderer("bee-brain-&-hive", new BeeDebugRenderer(this.mc));
      this.addRenderer("breeze-brain", new BreezeDebugRenderer(this.mc));
      this.addRenderer("chunk-culling", new ChunkCullingDebugRenderer(this.mc));
      this.addRenderer("chunk-debug", new ChunkDebugRenderer(this.mc));
      this.addRenderer("collision", new CollisionBoxRenderer(this.mc));
      this.addRenderer("entity-block-intersection", new EntityBlockIntersectionDebugRenderer());
      this.addRenderer("game-event", new GameEventListenerRenderer());
      this.addRenderer("mob-goals", new GoalSelectorDebugRenderer(this.mc));
      this.addRenderer("heightmap", new HeightMapRenderer(this.mc));
      this.addRenderer("block-light", new LightDebugRenderer(this.mc, true, false));
      this.addRenderer("sky-light", new LightDebugRenderer(this.mc, false, true));
      this.addRenderer("light-sections-sky", new LightSectionDebugRenderer(this.mc, LightLayer.SKY));
      this.addRenderer("light-sections-block", new LightSectionDebugRenderer(this.mc, LightLayer.BLOCK));
      this.addRenderer("neighbor-updates", new NeighborsUpdateRenderer());
      this.addRenderer("octree", new OctreeDebugRenderer(this.mc));
      this.addRenderer("pathfinding-debug", new PathfindingRenderer());
      this.addRenderer("raid-center", new RaidDebugRenderer(this.mc));
      this.addRenderer("redstone-wire-orientations", new RedstoneWireOrientationsRenderer());
      this.addRenderer("solid-faces", new SolidFaceRenderer(this.mc));
      this.addRenderer("structure-outlines", new StructureRenderer());
      this.addRenderer("support-blocks", new SupportBlockRenderer(this.mc));
      this.addRenderer("brain-debug", brainRenderer);
      this.addRenderer("poi-debug", new PoiDebugRenderer(brainRenderer));
      this.addRenderer("village-sections", new VillageSectionsDebugRenderer());
      this.addRenderer("water", new WaterDebugRenderer(this.mc));
   }

   private void addRenderer(String name, SimpleDebugRenderer renderer) {
      Setting<Boolean> setting = this.sgGeneral.add(((Builder)((Builder)new Builder().name(name)).defaultValue(false)).build());
      this.renderers.put(setting, renderer);
   }

   public void render(Frustum frustum, double x, double y, double z, float partialTick) {
      if (this.mc.getConnection() != null) {
         DebugValueAccess debugValueAccess = this.mc.getConnection().createDebugValueAccess();

         for (Entry<Setting<Boolean>, SimpleDebugRenderer> entry : this.renderers.entrySet()) {
            if ((Boolean)entry.getKey().get()) {
               entry.getValue().emitGizmos(x, y, z, debugValueAccess, frustum, partialTick);
            }
         }
      }
   }
}
