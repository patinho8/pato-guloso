package br.mareeletrica.client;

import br.mareeletrica.Robots.Catador;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Robô Catador: shell, spinning wheels, a scoop that lifts when it grabs something, a glowing visor (red when full) and an antenna lamp at night. */
public final class CatadorRenderer extends EntityRenderer<Catador> {
 static final float S=.8F;
 public CatadorRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.45F;}
 @Override public ResourceLocation getTextureLocation(Catador e){return TextureAtlas.LOCATION_BLOCKS;}
 @Override public void render(Catador e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
  float body=Mth.rotLerp(pt,e.yBodyRotO,e.yBodyRot),age=e.tickCount+pt;
  int overlay=OverlayTexture.pack(OverlayTexture.u(0),OverlayTexture.v(e.hurtTime>0||e.deathTime>0));
  float wheel=Mth.lerp(pt,e.wheelO,e.wheel);
  float scoop=age-e.scoopAt()<12?Mth.sin(Mth.clamp((age-e.scoopAt())/12F,0,1)*Mth.PI):0;
  float shake=e.pressing()?Mth.sin(age*2.2F)*.012F:0;
  float bob=Mth.sin(age*.25F)*.004F;
  p.pushPose();
  p.mulPose(Axis.YP.rotationDegrees(180F-body));
  if(e.deathTime>0){float d=Math.min(1,(e.deathTime+pt)/12F);p.mulPose(Axis.ZP.rotationDegrees(90*d));}
  p.scale(S,S,S);p.translate(-.5,0,-.5);
  // wheels: two at the back, one small caster at the front
  for(int side=-1;side<=1;side+=2){p.pushPose();p.translate(side*6.5F/16F,0,3F/16F);spin(p,8,2,8,wheel);FrogRenderer.part("robot_wheel",p,b,light,overlay);p.popPose();}
  p.pushPose();spin(p,8,1.2F,4,wheel*1.6F);FrogRenderer.part("robot_caster",p,b,light,overlay);p.popPose();
  // body (shakes while the press works, does a little happy hop when it grabs something)
  float hop=scoop*.05F;
  p.pushPose();p.translate(shake,bob+hop,0);
  FrogRenderer.part("robot_body",p,b,light,overlay);
  // head: turns toward what it looks at (you!) and tilts its head a little now and then, like it's curious
  float look=Mth.clamp(Mth.wrapDegrees(Mth.rotLerp(pt,e.yHeadRotO,e.yHeadRot)-body),-45,45);
  float tilt=Mth.sin(age*.05F+e.getId())*Mth.sin(age*.013F)*9F+(e.full()?-6F:0);
  p.pushPose();p.translate(8/16F,9/16F,8/16F);p.mulPose(Axis.YP.rotationDegrees(-look));p.mulPose(Axis.ZP.rotationDegrees(tilt));
  if(e.full())p.mulPose(Axis.XP.rotationDegrees(10));   // full: hangs its head a little
  p.translate(-8/16F,-9/16F,-8/16F);
  FrogRenderer.part("robot_head",p,b,light,overlay);
  boolean blink=((e.tickCount+e.getId()*17)%80)<3;
  String face=e.full()?"sad":age-e.scoopAt()<24?"happy":blink?"blink":"open";
  FrogRenderer.part("robot_face_"+face,p,b,0xF000F0,OverlayTexture.NO_OVERLAY);
  // antenna bulb on a springy stalk
  p.pushPose();p.translate(8/16F,15.1/16F,8/16F);p.mulPose(Axis.ZP.rotationDegrees(Mth.sin(age*.31F)*5F-tilt*.6F));p.mulPose(Axis.XP.rotationDegrees(Mth.cos(age*.27F)*4F+scoop*12F));p.translate(-8/16F,-15.1/16F,-8/16F);
  boolean flash=e.full()?(e.tickCount/6)%2==0:true;
  FrogRenderer.part("robot_lamp",p,b,e.lampOn()&&flash?0xF000F0:light,OverlayTexture.NO_OVERLAY);
  p.popPose();
  p.popPose();
  p.popPose();
  // front scoop: lifts the item into the hatch
  p.pushPose();p.translate(8/16F,6/16F,3/16F);p.mulPose(Axis.XP.rotationDegrees(-scoop*55F));p.translate(-8/16F,-6/16F,-3/16F);
  FrogRenderer.part("robot_scoop",p,b,light,overlay);p.popPose();
  p.popPose();
  super.render(e,yaw,pt,p,b,light);
 }
 static void spin(PoseStack p,float x,float y,float z,float deg){p.translate(x/16F,y/16F,z/16F);p.mulPose(Axis.XP.rotationDegrees(-deg));p.translate(-x/16F,-y/16F,-z/16F);}
}
