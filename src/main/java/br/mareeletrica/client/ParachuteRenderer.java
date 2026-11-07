package br.mareeletrica.client;

import br.mareeletrica.Parachutes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Open parachute canopy over the rider, gently swaying. */
public final class ParachuteRenderer extends EntityRenderer<Parachutes.ParachuteEntity> {
 public ParachuteRenderer(EntityRendererProvider.Context c){super(c);}
 @Override public ResourceLocation getTextureLocation(Parachutes.ParachuteEntity e){return ResourceLocation.withDefaultNamespace("textures/misc/white.png");}
 @Override public void render(Parachutes.ParachuteEntity e,float yaw,float pt,PoseStack pose,MultiBufferSource buffers,int light){
  pose.pushPose();
  pose.translate(0,1.45,0);
  pose.mulPose(Axis.YP.rotationDegrees(180F-Mth.rotLerp(pt,e.yRotO,e.getYRot())));
  pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pt,e.swayO,e.sway)));
  pose.translate(-.5,0,-.5);
  DmtGearLayer.model("parachute_canopy",pose,buffers,light);
  pose.popPose();
  super.render(e,yaw,pt,pose,buffers,light);
 }
 @Override public boolean shouldRender(Parachutes.ParachuteEntity e,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){return true;}
}
