package br.mareeletrica.client;

import br.mareeletrica.Flares;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** The burning flare: a fat, full-bright, flickering core in its colour (always lit, so it shows from far away). */
public final class FlareRenderer extends EntityRenderer<Flares.FlareEntity> {
 public FlareRenderer(EntityRendererProvider.Context c){super(c);}
 @Override public ResourceLocation getTextureLocation(Flares.FlareEntity e){return ResourceLocation.withDefaultNamespace("textures/misc/white.png");}
 @Override public void render(Flares.FlareEntity e,float yaw,float pt,PoseStack pose,MultiBufferSource buffers,int light){
  float t=e.tickCount+pt,flick=1F+.18F*Mth.sin(t*1.7F)+.1F*Mth.sin(t*3.1F);
  // grows a little with distance so it never shrinks to nothing
  double d=Math.sqrt(entityRenderDispatcher.distanceToSqr(e));float s=(float)(flick*Math.max(.7,d/45.0));
  pose.pushPose();
  pose.translate(0,.2,0);pose.mulPose(Axis.YP.rotationDegrees(t*9F));pose.scale(s,s,s);pose.translate(-.5,-.5,-.5);
  DmtGearLayer.model("flare_glow_"+Flares.COLORS[e.color()],pose,buffers,0xF000F0);
  pose.popPose();
  super.render(e,yaw,pt,pose,buffers,0xF000F0);
 }
 @Override public boolean shouldRender(Flares.FlareEntity e,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){return e.shouldRenderAtSqrDistance(e.distanceToSqr(x,y,z));}
}
