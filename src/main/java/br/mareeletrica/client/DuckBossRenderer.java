package br.mareeletrica.client;

import br.mareeletrica.DuckBoss.EvilDuck;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** 0.71.0: the Giant Evil Duck — the duck parts at 6x, a normal white duck with a red crown (slightly red when enraged). */
public final class DuckBossRenderer extends EntityRenderer<EvilDuck> {
 public static final String[] PARTS={"duck_crown_red"};
 static final float S=6F;
 public DuckBossRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=1.8F;}
 @Override public ResourceLocation getTextureLocation(EvilDuck e){return TextureAtlas.LOCATION_BLOCKS;}
 static void pivot(PoseStack p,float x,float y,float z){p.translate(x/16,y/16,z/16);}
 @Override public void render(EvilDuck e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
  float age=e.tickCount+pt,body=Mth.rotLerp(pt,e.yBodyRotO,e.yBodyRot),head=Mth.rotLerp(pt,e.yHeadRotO,e.yHeadRot)-body,pitch=Mth.lerp(pt,e.xRotO,e.getXRot());
  int overlay=OverlayTexture.pack(OverlayTexture.u(0),OverlayTexture.v(e.hurtTime>0||e.deathTime>0));
  boolean rage=e.rage();int atk=e.attack();
  float r=1,g=rage?.78F:1,bl=rage?.78F:1,hr=r,hg=g,hb=bl;   // a normal white duck; a little red when enraged
  float walkPos=e.walkAnimation.position(pt),walkSpd=Math.min(1,e.walkAnimation.speed(pt));
  p.pushPose();
  p.mulPose(Axis.YP.rotationDegrees(180-body));
  if(e.deathTime>0){float d=Math.min(1,(e.deathTime+pt)/20F);p.mulPose(Axis.ZP.rotationDegrees(d*80));}
  p.scale(S,S,S);
  float waddle=Mth.sin(walkPos*.6F)*walkSpd*7F;p.mulPose(Axis.ZP.rotationDegrees(waddle));
  if(atk==EvilDuck.CHARGE){p.mulPose(Axis.XP.rotationDegrees(-14));}
  if(!e.onGround()){float vy=(float)e.getDeltaMovement().y;p.mulPose(Axis.XP.rotationDegrees(atk==EvilDuck.DIVE&&vy<-.5F?-40:Mth.clamp(-vy*12,-15,15)));}
  if(atk==EvilDuck.FEATHERS){float t=Math.min(1,(e.attackTime()+pt)/8F);p.mulPose(Axis.XP.rotationDegrees(t*12));}
  if(atk==EvilDuck.QUACK){float t=Math.min(1,(e.attackTime()+pt)/28F);p.translate(0,0,t*.04);p.mulPose(Axis.XP.rotationDegrees(t*18-(e.attackTime()>28?30:0)));}
  p.translate(-.5,0,-.5);
  float sw=Mth.cos(walkPos*.6F)*walkSpd*35F;
  p.pushPose();pivot(p,9.4F,3.4F,8.4F);p.mulPose(Axis.XP.rotationDegrees(sw));pivot(p,-9.4F,-3.4F,-8.4F);DuckRenderer.part("duck_leg_l",p,b,1,1,1,light,overlay);p.popPose();
  p.pushPose();pivot(p,6.6F,3.4F,8.4F);p.mulPose(Axis.XP.rotationDegrees(-sw));pivot(p,-6.6F,-3.4F,-8.4F);DuckRenderer.part("duck_leg_r",p,b,1,1,1,light,overlay);p.popPose();
  DuckRenderer.part("duck_body",p,b,r,g,bl,light,overlay);
  float flap=Mth.lerp(pt,e.flapO,e.flap),wing=flap>.05F?Mth.sin(flap*1.6F)*60F+30F:Mth.sin(age*.05F)*4F;
  p.pushPose();pivot(p,11F,7.6F,6F);p.mulPose(Axis.ZP.rotationDegrees(-wing));pivot(p,-11F,-7.6F,-6F);DuckRenderer.part("duck_wing_l",p,b,r,g,bl,light,overlay);p.popPose();
  p.pushPose();pivot(p,5F,7.6F,6F);p.mulPose(Axis.ZP.rotationDegrees(wing));pivot(p,-5F,-7.6F,-6F);DuckRenderer.part("duck_wing_r",p,b,r,g,bl,light,overlay);p.popPose();
  DuckRenderer.part("duck_neck",p,b,hr,hg,hb,light,overlay);
  p.pushPose();pivot(p,8F,8.5F,4.5F);
  p.mulPose(Axis.YP.rotationDegrees(-Mth.clamp(head,-50,50)));p.mulPose(Axis.XP.rotationDegrees(-pitch*.5F));
  float peck=e.tickCount-e.peckAt<10?Mth.sin((e.tickCount-e.peckAt+pt)/10F*Mth.PI)*45:0;p.mulPose(Axis.XP.rotationDegrees(-peck));
  float q=e.tickCount-e.quackAt<14?Mth.sin((e.tickCount-e.quackAt+pt)/14F*Mth.PI)*22:0;p.mulPose(Axis.XP.rotationDegrees(q));
  pivot(p,-8F,-8.5F,-4.5F);
  DuckRenderer.part("duck_head",p,b,hr,hg,hb,light,overlay);
  DuckRenderer.part("duck_beak",p,b,1,1,1,light,overlay);
    DuckRenderer.part("duck_eyes",p,b,1,1,1,light,overlay);
  DuckRenderer.part("duck_crown_red",p,b,1,1,1,light,overlay);
  p.popPose();
  p.popPose();
  super.render(e,yaw,pt,p,b,light);
 }
}
