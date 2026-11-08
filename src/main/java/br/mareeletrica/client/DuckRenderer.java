package br.mareeletrica.client;

import br.mareeletrica.Pato;
import br.mareeletrica.Pato.Duck;
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

/** 0.68.0: the duck — tinted feathers (white, brown with a green head, black, golden, rainbow; yellow duckling),
 *  waddling legs, flapping wings, bobbing head, sleeping, swimming, diving, hats, the King's cape and the soldier's mini gun. */
public final class DuckRenderer extends EntityRenderer<Duck> {
 public static final String[] PARTS={"duck_body","duck_neck","duck_head","duck_beak","duck_eyes","duck_eyes_closed","duck_wing_l","duck_wing_r","duck_leg_l","duck_leg_r",
  "duck_hat_cap","duck_hat_tophat","duck_hat_crown","duck_hat_sunglasses","duck_hat_helmet","duck_hat_scope","duck_cape","duck_minigun"};
 static final String[] HAT_PARTS={"","duck_hat_cap","duck_hat_tophat","duck_hat_crown","duck_hat_sunglasses","duck_hat_helmet","duck_hat_scope"};
 public DuckRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.3F;}
 @Override public ResourceLocation getTextureLocation(Duck e){return TextureAtlas.LOCATION_BLOCKS;}
 static void part(String n,PoseStack p,MultiBufferSource b,float r,float g,float bl,int light,int overlay){
  var mc=Minecraft.getInstance();
  mc.getBlockRenderer().getModelRenderer().renderModel(p.last(),b.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)),null,mc.getModelManager().getModel(ClientEvents.part(n)),r,g,bl,light,overlay);}
 static void pivot(PoseStack p,float x,float y,float z){p.translate(x/16,y/16,z/16);}
 @Override public void render(Duck e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
  var mc=Minecraft.getInstance();
  if(e.getVehicle()==mc.player&&mc.options.getCameraType().isFirstPerson()){super.render(e,yaw,pt,p,b,light);return;}
  float age=e.tickCount+pt,body=Mth.rotLerp(pt,e.yBodyRotO,e.yBodyRot),head=Mth.rotLerp(pt,e.yHeadRotO,e.yHeadRot)-body,pitch=Mth.lerp(pt,e.xRotO,e.getXRot());
  int overlay=OverlayTexture.pack(OverlayTexture.u(0),OverlayTexture.v(e.hurtTime>0||e.deathTime>0));
  // colour
  float r=1,g=1,bl=1,hr,hg,hb;
  switch(e.color()){case 1->{r=.62F;g=.43F;bl=.27F;}case 2->{r=.2F;g=.2F;bl=.22F;}case 3->{r=1F;g=.8F;bl=.3F;}case 4->{int c=java.awt.Color.HSBtoRGB((age*.012F)%1F,.55F,1F);r=(c>>16&255)/255F;g=(c>>8&255)/255F;bl=(c&255)/255F;}default->{}}
  hr=r;hg=g;hb=bl;if(e.color()==1){hr=.2F;hg=.5F;hb=.3F;}   // brown ones have the green mallard head
  if(e.isBaby()){r=hr=1F;g=hg=.88F;bl=hb=.35F;}
  boolean sleep=e.flag(Duck.SLEEP),sit=e.isInSittingPose()||sleep||e.isPassenger(),swim=(e.isInWater()||e.fakeSwim)&&!e.flag(Duck.DIVE),dive=e.flag(Duck.DIVE),board=e.flag(Duck.BOARD),racer=e.racer();
  float walkPos=e.walkAnimation.position(pt),walkSpd=Math.min(1,e.walkAnimation.speed(pt));
  if(racer){walkPos=age*1.6F;walkSpd=1;}
  if(e.fakeWalkPos>=0){walkPos=e.fakeWalkPos;walkSpd=e.fakeWalkSpd;}
  p.pushPose();
  float s=e.getScale();
  p.mulPose(Axis.YP.rotationDegrees(180-body));
  if(e.isPassenger()&&e.getVehicle()!=null&&!(e.getVehicle() instanceof net.minecraft.world.entity.player.Player))p.translate(0,0,0);
  p.scale(s,s,s);
  // waddle: the whole duck rocks side to side while walking
  float waddle=Mth.sin(walkPos*.9F)*walkSpd*8F;
  if(!sit&&!swim)p.mulPose(Axis.ZP.rotationDegrees(waddle));
  if(board)p.mulPose(Axis.ZP.rotationDegrees(Mth.sin(age*.2F)*6F));
  if(sit)p.translate(0,-2.6/16,0);
  if(swim)p.translate(0,-2.4/16+Mth.sin(age*.15F)*.01,0);
  if(dive){p.translate(0,-.1,0);p.mulPose(Axis.XP.rotationDegrees(-60));}
  if(e.isBaby())p.scale(1,1,1);
  p.translate(-.5,0,-.5);
  // legs
  if(!sit&&!swim){float sw=Mth.cos(walkPos*.9F)*walkSpd*40F;
   p.pushPose();pivot(p,9.4F,3.4F,8.4F);p.mulPose(Axis.XP.rotationDegrees(sw));pivot(p,-9.4F,-3.4F,-8.4F);part("duck_leg_l",p,b,1,1,1,light,overlay);p.popPose();
   p.pushPose();pivot(p,6.6F,3.4F,8.4F);p.mulPose(Axis.XP.rotationDegrees(-sw));pivot(p,-6.6F,-3.4F,-8.4F);part("duck_leg_r",p,b,1,1,1,light,overlay);p.popPose();}
  else if(swim){float pad=Mth.sin(age*.5F)*25;
   p.pushPose();pivot(p,9.4F,3.4F,8.4F);p.mulPose(Axis.XP.rotationDegrees(40+pad));pivot(p,-9.4F,-3.4F,-8.4F);part("duck_leg_l",p,b,1,1,1,light,overlay);p.popPose();
   p.pushPose();pivot(p,6.6F,3.4F,8.4F);p.mulPose(Axis.XP.rotationDegrees(40-pad));pivot(p,-6.6F,-3.4F,-8.4F);part("duck_leg_r",p,b,1,1,1,light,overlay);p.popPose();}
  part("duck_body",p,b,r,g,bl,light,overlay);
  // wings: flap when flying/excited, a little twitch otherwise
  float flap=Mth.lerp(pt,e.flapO,e.flap),wing=flap>.05F?Mth.sin(flap*2.2F)*55F+25F:Mth.sin(age*.07F)*3F+(e.tickCount-e.quackAt<12?18:0);
  if(racer)wing=Mth.sin(age*1.4F)*35F+20F;
  p.pushPose();pivot(p,11F,7.6F,6F);p.mulPose(Axis.ZP.rotationDegrees(-wing));pivot(p,-11F,-7.6F,-6F);part("duck_wing_l",p,b,r,g,bl,light,overlay);p.popPose();
  p.pushPose();pivot(p,5F,7.6F,6F);p.mulPose(Axis.ZP.rotationDegrees(wing));pivot(p,-5F,-7.6F,-6F);part("duck_wing_r",p,b,r,g,bl,light,overlay);p.popPose();
  if(e.king())part("duck_cape",p,b,1,1,1,light,overlay);
  if(e.hat()==5)part("duck_minigun",p,b,1,1,1,light,overlay);
  part("duck_neck",p,b,hr,hg,hb,light,overlay);
  // head: looks around, bobs while walking, tucks in when asleep, pecks
  p.pushPose();pivot(p,8F,8.5F,4.5F);
  if(e.isBaby())p.scale(1.25F,1.25F,1.25F);
  if(sleep){p.translate(0,-1.4/16,1.5/16);p.mulPose(Axis.XP.rotationDegrees(25));}
  else{p.mulPose(Axis.YP.rotationDegrees(-Mth.clamp(head,-60,60)));p.mulPose(Axis.XP.rotationDegrees(-pitch*.6F));
   float bob=Mth.sin(walkPos*1.8F)*walkSpd*2.2F;p.translate(0,0,-bob/16);
   float peck=e.tickCount-e.peckAt<10?Mth.sin((e.tickCount-e.peckAt+pt)/10F*Mth.PI)*40:0;p.mulPose(Axis.XP.rotationDegrees(-peck));
   float q=e.tickCount-e.quackAt<8?Mth.sin((e.tickCount-e.quackAt+pt)/8F*Mth.PI)*10:0;p.mulPose(Axis.XP.rotationDegrees(q));}
  pivot(p,-8F,-8.5F,-4.5F);
  part("duck_head",p,b,hr,hg,hb,light,overlay);part("duck_beak",p,b,1,1,1,light,overlay);part(sleep?"duck_eyes_closed":"duck_eyes",p,b,1,1,1,light,overlay);
  int hat=e.hat();if(hat==0&&e.king())hat=3;if(hat>0&&hat<HAT_PARTS.length)part(HAT_PARTS[hat],p,b,1,1,1,light,overlay);
  if(e.king()&&e.hat()!=0&&e.hat()!=3&&e.hat()!=4){p.translate(0,(e.hat()==2?4.6:1.6)/16,0);part("duck_hat_crown",p,b,1,1,1,light,overlay);}   // the King keeps a crown on top of any hat
  p.popPose();
  p.popPose();
  super.render(e,yaw,pt,p,b,light);
 }
}
