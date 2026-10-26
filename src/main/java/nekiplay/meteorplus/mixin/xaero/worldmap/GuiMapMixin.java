package nekiplay.meteorplus.mixin.xaero.worldmap;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import nekiplay.MixinPlugin;
import nekiplay.meteorplus.features.modules.integrations.MapIntegration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.misc.Misc;
import xaero.map.region.MapBlock;
import xaero.map.region.MapRegion;
import xaero.map.region.MapTile;
import xaero.map.region.MapTileChunk;

@Mixin({GuiMap.class})
public abstract class GuiMapMixin {
   @Unique
   private final GuiMap guiMap = (GuiMap)this;
   @Shadow(
      remap = false
   )
   private double scale;
   @Shadow(
      remap = false
   )
   private int rightClickX;
   @Shadow(
      remap = false
   )
   private int rightClickY;
   @Shadow(
      remap = false
   )
   private int rightClickZ;
   @Shadow(
      remap = false
   )
   private MapProcessor mapProcessor;
   @Shadow(
      remap = false
   )
   private double cameraX = 0.0;
   @Shadow(
      remap = false
   )
   private double cameraZ = 0.0;

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void onInit(CallbackInfo info) {
      this.cameraX = (float)MeteorClient.mc.player.getX();
      this.cameraZ = (float)MeteorClient.mc.player.getZ();
   }

   @Inject(
      method = {"getRightClickOptions"},
      at = {@At("RETURN")},
      remap = false
   )
   private void rightClickOptions(CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
      Modules modules = Modules.get();
      if (modules != null) {
         MapIntegration mapIntegration = (MapIntegration)modules.get(MapIntegration.class);
         if (mapIntegration != null && mapIntegration.isActive()) {
            ArrayList<RightClickOption> options = (ArrayList<RightClickOption>)cir.getReturnValue();
            int mouseXPos = (int)Misc.getMouseX(MeteorClient.mc, false);
            int mouseYPos = (int)Misc.getMouseY(MeteorClient.mc, false);
            int mouseFromCentreX = mouseXPos - MeteorClient.mc.getWindow().getWidth() / 2;
            int mouseFromCentreY = mouseYPos - MeteorClient.mc.getWindow().getHeight() / 2;
            double mousePosX = mouseFromCentreX / this.scale + this.cameraX;
            double mousePosZ = mouseFromCentreY / this.scale + this.cameraZ;
            int mouseBlockPosX = (int)Math.floor(mousePosX);
            int mouseBlockPosZ = (int)Math.floor(mousePosZ);
            int renderedCaveLayer = this.mapProcessor.getCurrentCaveLayer();
            MapRegion leafRegion = this.mapProcessor.getLeafMapRegion(renderedCaveLayer, mouseBlockPosX >> 9, mouseBlockPosZ >> 9, false);
            MapTileChunk chunk = leafRegion == null ? null : leafRegion.getChunk(mouseBlockPosX >> 6 & 7, mouseBlockPosZ >> 6 & 7);
            MapTile mouseTile = chunk == null ? null : chunk.getTile(mouseBlockPosX >> 4 & 3, mouseBlockPosZ >> 4 & 3);
            if (mapIntegration.showBlock.get() != null && mouseTile != null) {
               MapBlock block = mouseTile.getBlock(mouseBlockPosX & 15, mouseBlockPosZ & 15);
               MapPixelAccessor pixel = (MapPixelAccessor)block;
               options.addAll(2, List.of(new RightClickOption(Names.get(pixel.getBlockState().getBlock()), options.size(), this.guiMap) {
                  {
                     Objects.requireNonNull(GuiMapMixin.this);
                  }

                  public void onAction(Screen screen) {
                  }
               }));
            }

            if (!MixinPlugin.isXaeroPlusMapPresent) {
               if ((Boolean)mapIntegration.baritoneGoto.get()) {
                  options.addAll(
                     3,
                     List.of(
                        new RightClickOption(I18n.get("gui.world_map.baritone_goal_here", new Object[0]), options.size(), this.guiMap) {
                           {
                              Objects.requireNonNull(GuiMapMixin.this);
                           }

                           public void onAction(Screen screen) {
                              GoalBlock goal = new GoalBlock(
                                 new BlockPos(GuiMapMixin.this.rightClickX, GuiMapMixin.this.rightClickY, GuiMapMixin.this.rightClickZ).above()
                              );
                              BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(goal);
                           }
                        },
                        new RightClickOption(I18n.get("gui.world_map.baritone_path_here", new Object[0]), options.size(), this.guiMap) {
                           {
                              Objects.requireNonNull(GuiMapMixin.this);
                           }

                           public void onAction(Screen screen) {
                              GoalBlock goal = new GoalBlock(
                                 new BlockPos(GuiMapMixin.this.rightClickX, GuiMapMixin.this.rightClickY, GuiMapMixin.this.rightClickZ).above()
                              );
                              BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
                           }
                        }
                     )
                  );
                  if ((Boolean)mapIntegration.baritoneElytra.get()
                     && (Boolean)mapIntegration.baritoneGoto.get()
                     && PlayerUtils.getDimension() == Dimension.Nether
                     && this.rightClickY - 1 > 0
                     && this.rightClickY < 128) {
                     options.addAll(
                        3,
                        List.of(
                           new RightClickOption(I18n.get("gui.world_map.baritone_elytra_here", new Object[0]), options.size(), this.guiMap) {
                              {
                                 Objects.requireNonNull(GuiMapMixin.this);
                              }

                              public void onAction(Screen screen) {
                                 GoalBlock goal = new GoalBlock(
                                    new BlockPos(GuiMapMixin.this.rightClickX, GuiMapMixin.this.rightClickY, GuiMapMixin.this.rightClickZ).above()
                                 );
                                 BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(goal);
                                 BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("elytra");
                              }
                           }
                        )
                     );
                  }
               }
            } else if ((Boolean)mapIntegration.baritoneElytra.get()
               && (Boolean)mapIntegration.baritoneGoto.get()
               && PlayerUtils.getDimension() == Dimension.Nether
               && this.rightClickY - 1 > 0
               && this.rightClickY < 128) {
               options.addAll(
                  3,
                  List.of(
                     new RightClickOption(I18n.get("gui.world_map.baritone_elytra_here", new Object[0]), options.size(), this.guiMap) {
                        {
                           Objects.requireNonNull(GuiMapMixin.this);
                        }

                        public void onAction(Screen screen) {
                           GoalBlock goal = new GoalBlock(
                              new BlockPos(GuiMapMixin.this.rightClickX, GuiMapMixin.this.rightClickY, GuiMapMixin.this.rightClickZ).above()
                           );
                           BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal(goal);
                           BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("elytra");
                        }
                     }
                  )
               );
            }
         }
      }
   }
}
