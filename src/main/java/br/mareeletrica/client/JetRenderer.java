package br.mareeletrica.client;

import br.mareeletrica.Jets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

/** The jet model (built nose toward -z at half size) turned by the aircraft's full 3D attitude. */
public final class JetRenderer extends EntityRenderer<Jets.JetEntity> {
 public JetRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=2.4F;}
 @Override public ResourceLocation getTextureLocation(Jets.JetEntity e){return ResourceLocation.withDefaultNamespace("textures/misc/white.png");}
 @Override public void render(Jets.JetEntity e,float yaw,float pt,PoseStack pose,MultiBufferSource buffers,int light){
  pose.pushPose();
  pose.translate(0,Jets.CENTER_Y,0);
  pose.mulPose(new Quaternionf(e.qO).slerp(e.q,pt));
  pose.mulPose(Axis.YP.rotationDegrees(180F));
  var mcv=net.minecraft.client.Minecraft.getInstance();
  boolean inside=e.hasPassenger(mcv.player)&&mcv.options.getCameraType().isFirstPerson();
  if(e.bomber()){
   // B-2: a flying wing drawn bigger than the fighter; the bomb bay doors open while a stick of bombs falls
   float s=Jets.SCALE*1.6F;pose.scale(s,s,s);pose.translate(-.5,-6F/16F,-.5);
   // from the pilot's seat: the wing without the closed hump around him, plus the cockpit (panel, windscreen frame)
   if(inside){DmtGearLayer.model("b2_fp",pose,buffers,light);DmtGearLayer.model("b2_fp_glow",pose,buffers,0xF000F0);}
   else DmtGearLayer.model("b2_bomber",pose,buffers,light);
   pose.popPose();super.render(e,yaw,pt,pose,buffers,light);return;
  }
  pose.scale(Jets.SCALE,Jets.SCALE,Jets.SCALE);
  pose.translate(-.5,-6F/16F,-.5);
  DmtGearLayer.model("fighter_jet",pose,buffers,light);
  // the canopy glass would fill the pilot's own view: he sees the cockpit frame, panel and HUD instead
  if(!inside)DmtGearLayer.model("jet_canopy",pose,buffers,light);
  else{DmtGearLayer.model("jet_cockpit",pose,buffers,light);BaseRenderers.cutout("jet_cockpit_glow",pose,buffers,0xF000F0);}
  int n=e.bombs();
  if(n>=3)DmtGearLayer.model("jet_bomb_part",pose,buffers,light);
  if(n>=2){pose.pushPose();pose.translate(-9.6F/16F,.3F/16F,0);DmtGearLayer.model("jet_bomb_part",pose,buffers,light);pose.popPose();}
  if(n>=1){pose.pushPose();pose.translate(9.6F/16F,.3F/16F,0);DmtGearLayer.model("jet_bomb_part",pose,buffers,light);pose.popPose();}
  pose.popPose();
  super.render(e,yaw,pt,pose,buffers,light);
 }
}
