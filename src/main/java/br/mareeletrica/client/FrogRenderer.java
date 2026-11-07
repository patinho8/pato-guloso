package br.mareeletrica.client;

import br.mareeletrica.Frogs;
import br.mareeletrica.Frogs.TinyFrog;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** The tiny frog: body, two back legs that kick out while it hops, a throat that puffs when it croaks and a tongue that flicks out. */
public final class FrogRenderer extends EntityRenderer<TinyFrog> {
 /** Model pixels are drawn at this scale: the whole frog is about a fifth of a block. */
 static final float S=.42F;
 public FrogRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.14F;}
 @Override public ResourceLocation getTextureLocation(TinyFrog e){return TextureAtlas.LOCATION_BLOCKS;}
 static void part(String n,PoseStack p,MultiBufferSource b,int light,int overlay){
  var mc=Minecraft.getInstance();
  mc.getBlockRenderer().getModelRenderer().renderModel(p.last(),b.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)),null,mc.getModelManager().getModel(ClientEvents.part(n)),1,1,1,light,overlay);
 }
 @Override public void render(TinyFrog e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
  var mc=net.minecraft.client.Minecraft.getInstance();
  if(e.getVehicle()==mc.player&&mc.options.getCameraType().isFirstPerson())return;   // on your own head: it would sit in front of the camera
  float body=Mth.rotLerp(pt,e.yBodyRotO,e.yBodyRot),age=e.tickCount+pt;
  int overlay=OverlayTexture.pack(OverlayTexture.u(0),OverlayTexture.v(e.hurtTime>0||e.deathTime>0));
  String v="_"+Frogs.VARIANTS[e.variant()];
  // in the air the body tilts with the jump and the back legs stretch out; on the ground they are folded
  double vy=Mth.lerp(pt,e.yo,e.getY())-e.yo;boolean air=!e.onGround();
  float stretch=air?Mth.clamp((float)(.6+vy*6),0,1):0;
  float pitch=air?Mth.clamp((float)(-vy*160),-30,25):0;
  float breathe=Mth.sin(age*.12F)*.015F;
  float croak=age-e.croakAt<24?Mth.sin(Mth.clamp((age-e.croakAt)/24F,0,1)*Mth.PI*3):0;croak=Math.abs(croak);
  float tongue=age-e.tongueAt<8?Mth.sin((age-e.tongueAt)/8F*Mth.PI):0;
  float s=S*(e.isBaby()?.55F:1F);
  p.pushPose();
  p.mulPose(Axis.YP.rotationDegrees(180F-body));
  if(e.deathTime>0){float d=Math.min(1,(e.deathTime+pt)/10F);p.translate(0,.06*d,0);p.mulPose(Axis.ZP.rotationDegrees(180*d));}
  p.scale(s,s,s);
  p.translate(0,0,0);
  p.translate(0,2/16F,8/16F);p.mulPose(Axis.XP.rotationDegrees(pitch));p.translate(0,-2/16F,-8/16F);
  p.pushPose();p.translate(-.5,0,-.5);
  p.pushPose();p.translate(.5,breathe,.5);p.scale(1,1+breathe*4,1);p.translate(-.5,0,-.5);part("frog_body"+v,p,b,light,overlay);p.popPose();
  // back legs: pivot at the hips, swing back and down while stretched
  for(String side:new String[]{"l","r"}){
   p.pushPose();float hx=side.equals("l")?10.2F:5.8F;
   p.translate(hx/16F,1.6/16F,10/16F);p.mulPose(Axis.XP.rotationDegrees(-stretch*70));p.translate(-hx/16F,-1.6/16F,-10/16F);
   p.translate(0,0,stretch*1.5F/16F);
   part("frog_leg_"+side+v,p,b,light,overlay);p.popPose();
  }
  // throat sac: a pale bubble under the chin that inflates when croaking
  if(croak>.05F){p.pushPose();p.translate(8/16F,1.2/16F,3.2/16F);float q=.4F+croak*1.1F;p.scale(q,q,q);p.translate(-.5,0,-.5);part("frog_throat",p,b,light,OverlayTexture.NO_OVERLAY);p.popPose();}
  // tongue: shoots straight out of the mouth
  if(tongue>.02F){p.pushPose();p.translate(8/16F,2.1/16F,2/16F);p.scale(1,1,tongue*5);p.translate(-.5,0,-.5);part("frog_tongue",p,b,light,OverlayTexture.NO_OVERLAY);p.popPose();}
  p.popPose();
  p.popPose();
  super.render(e,yaw,pt,p,b,light);
 }
}
