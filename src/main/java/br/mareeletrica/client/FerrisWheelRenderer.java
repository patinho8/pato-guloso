package br.mareeletrica.client;

import br.mareeletrica.FerrisWheel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/** 0.89.0: draws the whole Ferris wheel — A-frame legs, axle, double rim with spokes and bracing, chasing light bulbs
 *  and 8 rainbow gondolas that stay level. */
public final class FerrisWheelRenderer implements BlockEntityRenderer<FerrisWheel.Wheel> {
 static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/misc/white.png");
 static final int[] CAB={0xFFE84545,0xFFFF9A2E,0xFFFFD83A,0xFF5BD15B,0xFF3EC9E6,0xFF4A6BFF,0xFFB25BFF,0xFFFF6FB7};
 public FerrisWheelRenderer(BlockEntityRendererProvider.Context c){}
 @Override public boolean shouldRenderOffScreen(FerrisWheel.Wheel w){return true;}
 @Override public int getViewDistance(){return 160;}
 @Override public AABB getRenderBoundingBox(FerrisWheel.Wheel w){return new AABB(w.getBlockPos()).inflate(FerrisWheel.R+3,0,FerrisWheel.R+3).expandTowards(0,FerrisWheel.HUB+FerrisWheel.R+2,0);}

 VertexConsumer vc;PoseStack.Pose last;int light;
 void quad(float[][] v,float nx,float ny,float nz,int c,int L){
  int a=c>>>24,r=c>>16&255,g=c>>8&255,b=c&255;
  for(int i=0;i<4;i++)vc.addVertex(last,v[i][0],v[i][1],v[i][2]).setColor(r,g,b,a).setUv(i==1||i==2?1:0,i>=2?1:0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(L).setNormal(last,nx,ny,nz);}
 /** axis-aligned box in the current pose */
 void box(PoseStack pose,float x0,float y0,float z0,float x1,float y1,float z1,int c){box(pose,x0,y0,z0,x1,y1,z1,c,light);}
 void box(PoseStack pose,float x0,float y0,float z0,float x1,float y1,float z1,int c,int L){
  last=pose.last();
  int top=c,side=shade(c,.85F),front=shade(c,.92F),bot=shade(c,.6F);
  quad(new float[][]{{x0,y1,z0},{x0,y1,z1},{x1,y1,z1},{x1,y1,z0}},0,1,0,top,L);
  quad(new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y0,z1},{x0,y0,z1}},0,-1,0,bot,L);
  quad(new float[][]{{x0,y0,z0},{x0,y1,z0},{x1,y1,z0},{x1,y0,z0}},0,0,-1,front,L);
  quad(new float[][]{{x1,y0,z1},{x1,y1,z1},{x0,y1,z1},{x0,y0,z1}},0,0,1,front,L);
  quad(new float[][]{{x0,y0,z1},{x0,y1,z1},{x0,y1,z0},{x0,y0,z0}},-1,0,0,side,L);
  quad(new float[][]{{x1,y0,z0},{x1,y1,z0},{x1,y1,z1},{x1,y0,z1}},1,0,0,side,L);}
 static int shade(int c,float k){return c&0xFF000000|(int)((c>>16&255)*k)<<16|(int)((c>>8&255)*k)<<8|(int)((c&255)*k);}
 /** a beam from (x0,y0) to (x1,y1) in the XY plane, thickness t, depth z0..z1 */
 void beam(PoseStack pose,float x0,float y0,float x1,float y1,float t,float z0,float z1,int c){
  float dx=x1-x0,dy=y1-y0,len=Mth.sqrt(dx*dx+dy*dy);
  pose.pushPose();pose.translate(x0,y0,0);pose.mulPose(Axis.ZP.rotation((float)Math.atan2(dy,dx)));
  box(pose,0,-t/2,z0,len,t/2,z1,c);pose.popPose();}

 @Override public void render(FerrisWheel.Wheel w,float pt,PoseStack pose,MultiBufferSource buf,int packedLight,int overlay){
  if(w.getLevel()==null)return;
  BlockPos hubPos=BlockPos.containing(FerrisWheel.hub(w.getBlockPos()));
  light=LevelRenderer.getLightColor(w.getLevel(),hubPos);
  vc=buf.getBuffer(RenderType.entityCutoutNoCull(WHITE));
  float a=w.angle(pt),T=w.getLevel().getGameTime()+pt;
  float R=(float)FerrisWheel.R,H=(float)FerrisWheel.HUB;
  int ang=switch(w.facing()){case EAST->90;case SOUTH->180;case WEST->270;default->0;};
  pose.pushPose();
  pose.translate(.5,H,.5);pose.mulPose(Axis.YP.rotationDegrees(-ang));
  int steel=0xFFE8E8EE,dark=0xFF6A6F7A,red=0xFFD83A3A;
  // ---- static frame: two A-frames, axle and ground feet
  for(float z:new float[]{-1.25F,1.25F}){
   beam(pose,-3.4F,-H+.15F,0,0,.32F,z-.14F,z+.14F,red);beam(pose,3.4F,-H+.15F,0,0,.32F,z-.14F,z+.14F,red);
   beam(pose,-2.3F,-H*.62F,2.3F,-H*.62F,.18F,z-.08F,z+.08F,red);
   beam(pose,-1.2F,-H*.3F,1.2F,-H*.3F,.14F,z-.07F,z+.07F,red);
   box(pose,-3.9F,-H,z-.5F,-2.9F,-H+.25F,z+.5F,dark);box(pose,2.9F,-H,z-.5F,3.9F,-H+.25F,z+.5F,dark);}
  box(pose,-.22F,-.22F,-1.45F,.22F,.22F,1.45F,dark);
  // ---- rotating wheel
  pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(a));
  box(pose,-.55F,-.55F,-.7F,.55F,.55F,.7F,steel);box(pose,-.3F,-.3F,-.85F,.3F,.3F,.85F,red);
  int N=36;float seg=(float)(2*Math.PI*R/N)*1.04F,segI=(float)(2*Math.PI*R*.62F/N)*1.06F;
  for(int i=0;i<N;i++){
   float t=(float)(i*2*Math.PI/N);
   for(float z:new float[]{-.48F,.48F}){
    pose.pushPose();pose.mulPose(Axis.ZP.rotation(t));
    box(pose,R-.1F,-seg/2,z-.09F,R+.1F,seg/2,z+.09F,steel);                    // outer rim
    box(pose,R*.62F-.06F,-segI/2,z-.06F,R*.62F+.06F,segI/2,z+.06F,steel);         // inner ring
    pose.popPose();}
   // light bulbs chasing around the rim
   boolean on=((i+(int)(T/3))%4)!=0;int bc=on?CAB[(i+(int)(T/12))%CAB.length]|0xFF000000:0xFF404048;
   pose.pushPose();pose.mulPose(Axis.ZP.rotation(t));
   for(float z:new float[]{-.62F,.62F})box(pose,R-.07F,-.07F,z-.07F,R+.07F,.07F,z+.07F,bc,on?LightTexture.FULL_BRIGHT:light);
   pose.popPose();}
  // spokes (two sides) and zig-zag bracing
  for(int i=0;i<16;i++){float t=(float)(i*2*Math.PI/16),t2=(float)((i+1)*2*Math.PI/16);
   for(float z:new float[]{-.48F,.48F}){
    beam(pose,0,0,Mth.cos(t)*R,Mth.sin(t)*R,.09F,z-.05F,z+.05F,steel);
    beam(pose,Mth.cos(t)*R*.62F,Mth.sin(t)*R*.62F,Mth.cos(t2)*R,Mth.sin(t2)*R,.06F,z-.04F,z+.04F,steel);}}
  // cross bars where the gondolas hang
  for(int i=0;i<FerrisWheel.CABINS;i++){float t=(float)(i*2*Math.PI/FerrisWheel.CABINS);
   pose.pushPose();pose.translate(Mth.cos(t)*R,Mth.sin(t)*R,0);box(pose,-.09F,-.09F,-.62F,.09F,.09F,.62F,dark);pose.popPose();}
  pose.popPose();
  // ---- gondolas (always level)
  for(int i=0;i<FerrisWheel.CABINS;i++){
   double t=Math.toRadians(a+i*360.0/FerrisWheel.CABINS);float px=(float)(Math.cos(t)*R),py=(float)(Math.sin(t)*R);
   float sway=Mth.sin(T*.05F+i)*2.5F;
   pose.pushPose();pose.translate(px,py,0);pose.mulPose(Axis.ZP.rotationDegrees(sway));
   int c=CAB[i];
   box(pose,-.05F,-.45F,-.05F,.05F,0,.05F,dark);                                         // hanger
   box(pose,-.8F,-.6F,-.7F,.8F,-.45F,.7F,c);                                              // roof
   box(pose,-.6F,-.45F,-.5F,.6F,-.35F,.5F,shade(c,.8F));
   box(pose,-.8F,-.66F,-.7F,.8F,-.6F,.7F,0xFFFFFFFF);                                     // white trim under the roof
   for(float sx:new float[]{-.72F,.64F})for(float sz:new float[]{-.62F,.54F})box(pose,sx,-3.05F,sz,sx+.08F,-.6F,sz+.08F,steel);   // corner posts
   box(pose,-.75F,-3.15F,-.65F,.75F,-3.03F,.65F,shade(c,.7F));                            // floor
   box(pose,-.75F,-3.03F,-.65F,.75F,-2.35F,-.55F,c);box(pose,-.75F,-3.03F,.55F,.75F,-2.35F,.65F,c);  // low walls
   box(pose,-.75F,-3.03F,-.65F,-.65F,-2.35F,.65F,c);box(pose,.65F,-3.03F,-.65F,.75F,-2.35F,.65F,c);
   box(pose,-.65F,-3.0F,.18F,.65F,-2.72F,.52F,0xFFF2E6D0);box(pose,-.65F,-2.72F,.44F,.65F,-2.3F,.52F,0xFFF2E6D0);   // bench + back
   box(pose,-.75F,-2.35F,-.67F,.75F,-2.29F,.67F,0xFFFFFFFF);                              // rail
   pose.popPose();}
  pose.popPose();
 }
}
