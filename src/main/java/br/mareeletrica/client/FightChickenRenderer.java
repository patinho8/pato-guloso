package br.mareeletrica.client;

import br.mareeletrica.FightChickens;
import br.mareeletrica.FightChickens.FightChicken;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** The fighting rooster, drawn from block-model parts (body, head, wings, legs) and animated by hand. */
public final class FightChickenRenderer extends EntityRenderer<FightChicken> {
 static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/misc/white.png");
 private final Font font;
 public FightChickenRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.4F;font=c.getFont();}
 @Override public ResourceLocation getTextureLocation(FightChicken e){return TextureAtlas.LOCATION_BLOCKS;}

 static void part(String name,PoseStack p,MultiBufferSource b,int light,int overlay,float r,float g,float bl){
  var mc=Minecraft.getInstance();
  mc.getBlockRenderer().getModelRenderer().renderModel(p.last(),b.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)),null,mc.getModelManager().getModel(ClientEvents.part(name)),r,g,bl,light,overlay);
 }

 @Override public void render(FightChicken e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
  float body=Mth.rotLerp(pt,e.yBodyRotO,e.yBodyRot),head=Mth.rotLerp(pt,e.yHeadRotO,e.yHeadRot),pitch=Mth.lerp(pt,e.xRotO,e.getXRot());
  float age=e.tickCount+pt,t=age-e.animStart;int anim=t<30?e.anim:0;
  float walkPos=e.walkAnimation.position(pt),walkSpeed=Math.min(1F,e.walkAnimation.speed(pt));
  boolean ko=e.ko()>0,rage=e.rage();
  int overlay=OverlayTexture.pack(OverlayTexture.u(0),OverlayTexture.v(e.hurtTime>0||e.deathTime>0));
  float r=1,g=rage?.55F+.15F*Mth.sin(age*.6F):1,bl=rage?.5F:1;
  float s=1F+(e.lvl()-1)*.035F;String br="_"+e.breed().id();

  p.pushPose();
  p.mulPose(Axis.YP.rotationDegrees(180F-body));
  p.scale(s,s,s);
  if(ko){p.translate(0,.32,0);p.mulPose(Axis.ZP.rotationDegrees(85));p.translate(0,-.32,0);}
  // flying kick: leans back, legs shoot forward
  float kick=anim==FightChickens.KICK?Mth.clamp(t/4F,0,1)*Mth.clamp((24-t)/6F,0,1):0;
  if(kick>0){p.translate(0,.5,0);p.mulPose(Axis.XP.rotationDegrees(-40*kick));p.translate(0,-.5,0);}
  // wing storm: spins in place
  if(anim==FightChickens.WINGS&&t<24)p.mulPose(Axis.YP.rotationDegrees(t*30F));
  float bob=ko?0:Mth.sin(age*.12F)*.01F+Math.abs(Mth.sin(walkPos*.6662F))*walkSpeed*.04F;
  p.translate(-.5,bob,-.5);

  part("fchicken_body"+br,p,b,light,overlay,r,g,bl);

  // head: looks around, pecks, puffs up in fury
  p.pushPose();p.translate(8/16F,14/16F,4/16F);
  float rel=Mth.wrapDegrees(head-body);
  p.mulPose(Axis.YP.rotationDegrees(-Mth.clamp(rel,-70,70)));
  float peck=anim==FightChickens.PECK||anim==FightChickens.THUNDER?Mth.sin(Mth.clamp(t/8F,0,1)*Mth.PI)*55F:0;
  float scream=anim==FightChickens.FURY&&t<20?Mth.sin(t*.9F)*8:0;
  p.mulPose(Axis.XP.rotationDegrees(-Mth.clamp(pitch,-40,40)*.6F-peck+scream+(ko?20:0)));
  if(peck>0)p.translate(0,0,-peck/55F*.12F);
  p.translate(-8/16F,-14/16F,-4/16F);
  part("fchicken_head"+br,p,b,light,overlay,r,g,bl);
  p.popPose();

  // wings: fold at rest, flap in the air, spread wide for the wing storm
  float fl=Mth.lerp(pt,e.flapO,e.flap);
  float spread=(Mth.sin(fl)+1)*.5F;float air=e.onGround()?0:1;
  float open=Math.max(air*(20+spread*60),anim==FightChickens.WINGS&&t<24?40+spread*50:0);
  open=Math.max(open,kick*80);
  if(anim==FightChickens.FURY&&t<20)open=Math.max(open,70);
  if(ko)open=10;
  p.pushPose();p.translate(4/16F,12.5F/16,5/16F);p.mulPose(Axis.ZP.rotationDegrees(-open));p.translate(-4/16F,-12.5F/16,-5/16F);
  part("fchicken_wing_l"+br,p,b,light,overlay,r,g,bl);p.popPose();
  p.pushPose();p.translate(12/16F,12.5F/16,5/16F);p.mulPose(Axis.ZP.rotationDegrees(open));p.translate(-12/16F,-12.5F/16,-5/16F);
  part("fchicken_wing_r"+br,p,b,light,overlay,r,g,bl);p.popPose();

  // legs: walk cycle, tucked in the air, forward for the kick
  for(int side=-1;side<=1;side+=2){
   float swing=ko?30:Mth.cos(walkPos*.6662F+(side>0?Mth.PI:0))*1.2F*walkSpeed*57.3F*.6F;
   if(air>0&&kick==0)swing=25;
   if(kick>0)swing=Mth.lerp(kick,swing,75+side*8);
   p.pushPose();p.translate(side*1.6F/16,0,0);
   p.translate(8/16F,6/16F,8/16F);p.mulPose(Axis.XP.rotationDegrees(swing));p.translate(-8/16F,-6/16F,-8/16F);
   part("fchicken_leg",p,b,light,overlay,r,g,bl);p.popPose();
  }
  p.popPose();

  // health bar and level over the head while fighting (or knocked out)
  if(e.combat()||ko||e.hurtTime>0||e.battleMax()>0)bar(e,p,b,s);
  super.render(e,yaw,pt,p,b,light);
 }

 void bar(FightChicken e,PoseStack p,MultiBufferSource b,float s){
  p.pushPose();p.translate(0,1.65*s,0);p.mulPose(entityRenderDispatcher.cameraOrientation());p.scale(.025F,-.025F,.025F);
  boolean ring=e.battleMax()>0;float w=24,f=Mth.clamp(ring?e.battleHp()/(float)e.battleMax():e.getHealth()/e.getMaxHealth(),0,1);
  VertexConsumer v=b.getBuffer(RenderType.textBackgroundSeeThrough());
  var m=p.last().pose();
  quad(v,m,-w-1,-1,w+1,4,0xC0000000);
  int col=e.ko()>0?0xFF808080:f>.5F?0xFF40E040:f>.25F?0xFFE0C020:0xFFE03030;
  quad(v,m,-w,0,-w+2*w*f,3,col);
  String txt=e.ko()>0?"§7💫 NOCAUTE":(e.rage()?"§4🔥 ":"§6")+e.breed().label+" Nv."+e.lvl()+(ring?" §f"+e.battleHp()+"/"+e.battleMax():"");
  font.drawInBatch(txt,-font.width(txt)/2F,-11,0xFFFFFFFF,false,m,b,Font.DisplayMode.SEE_THROUGH,0x40000000,0xF000F0);
  p.popPose();
 }
 static void quad(VertexConsumer v,org.joml.Matrix4f m,float x0,float y0,float x1,float y1,int argb){
  int a=argb>>>24,r=argb>>16&255,g=argb>>8&255,bb=argb&255;
  v.addVertex(m,x0,y0,0).setColor(r,g,bb,a).setLight(0xF000F0);v.addVertex(m,x0,y1,0).setColor(r,g,bb,a).setLight(0xF000F0);
  v.addVertex(m,x1,y1,0).setColor(r,g,bb,a).setLight(0xF000F0);v.addVertex(m,x1,y0,0).setColor(r,g,bb,a).setLight(0xF000F0);
 }
}
