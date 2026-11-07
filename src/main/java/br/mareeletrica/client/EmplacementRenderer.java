package br.mareeletrica.client;

import br.mareeletrica.Emplacements;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Machine-gun turret (fixed tripod, gun traverses) and field cannon (the whole gun traverses, barrel elevates and recoils). */
public final class EmplacementRenderer extends EntityRenderer<Emplacements.Mounted> {
 public EmplacementRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=1.4F;}
 @Override public ResourceLocation getTextureLocation(Emplacements.Mounted e){return ResourceLocation.withDefaultNamespace("textures/misc/white.png");}
 @Override public void render(Emplacements.Mounted e,float yaw,float pt,PoseStack pose,MultiBufferSource buffers,int light){
  float ay=e.yawO+Mth.wrapDegrees(e.yawR-e.yawO)*pt,ap=Mth.lerp(pt,e.pitchO,e.pitchR);
  float age=e.tickCount+pt-e.firedTick(),kick=age>=0&&age<10?1-age/10F:0;
  pose.pushPose();
  if(e.turret){
   pose.scale(1.5F,1.5F,1.5F);
   pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180F-e.getYRot()));pose.translate(-.5,0,-.5);DmtGearLayer.model("turret_base",pose,buffers,light);pose.popPose();
   pose.translate(0,12F/16F,0);pose.mulPose(Axis.YP.rotationDegrees(180F-ay));pose.mulPose(Axis.XP.rotationDegrees(-ap));pose.translate(-.5,-12F/16F,-.5+kick*.04F);
   DmtGearLayer.model("turret_gun",pose,buffers,light);
  }else{
   pose.scale(1.5F,1.5F,1.5F);
   // fixed concrete mount; the gun turns on top of it
   pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180F-e.getYRot()));pose.translate(-.5,0,-.5);DmtGearLayer.model("cannon_mount",pose,buffers,light);pose.popPose();
   pose.mulPose(Axis.YP.rotationDegrees(180F-ay));pose.translate(-.5,0,-.5);
   DmtGearLayer.model("cannon_carriage",pose,buffers,light);
   pose.translate(.5,16F/16F,.5);pose.mulPose(Axis.XP.rotationDegrees(-ap));pose.translate(-.5,-16F/16F,-.5+kick*.3F);
   DmtGearLayer.model("cannon_barrel",pose,buffers,light);
  }
  pose.popPose();
  super.render(e,yaw,pt,pose,buffers,light);
 }
}
