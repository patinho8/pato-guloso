package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.Pool;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** 0.65.5: draws the pool balls (rolling spheres, solids and stripes), the aim line + cue while you hold the cue, and the score text. */
public final class PoolRenderer implements BlockEntityRenderer<Pool.PoolEntity> {
 static final ResourceLocation WHITE=ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"textures/entity/pool_white.png");
 static final int[] COLORS={0xFFF4F1E6,0xFFD21E1E,0xFFD21E1E,0xFFD21E1E,0xFFD21E1E,0xFFD21E1E,0xFFD21E1E,0xFFD21E1E,0xFF111111,
                            0xFF1E4FD8,0xFF1E4FD8,0xFF1E4FD8,0xFF1E4FD8,0xFF1E4FD8,0xFF1E4FD8,0xFF1E4FD8};   // 0.66.0: red team, blue team, the black 8
 static final int LAT=8,LON=12;
 static final float[][] SIN=new float[2][LON+1];
 static{for(int j=0;j<=LON;j++){double a=j*Math.PI*2/LON;SIN[0][j]=(float)Math.cos(a);SIN[1][j]=(float)Math.sin(a);}}
 final Font font;
 public PoolRenderer(BlockEntityRendererProvider.Context c){font=c.getFont();}
 @Override public boolean shouldRenderOffScreen(Pool.PoolEntity e){return true;}
 @Override public AABB getRenderBoundingBox(Pool.PoolEntity e){return new AABB(e.getBlockPos()).inflate(5,3,5);}
 @Override public void render(Pool.PoolEntity e,float pt,PoseStack p,MultiBufferSource buf,int light,int overlay){
  var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
  int lt=LevelRenderer.getLightColor(mc.level,e.getBlockPos().above());
  var vc=buf.getBuffer(RenderType.entityCutoutNoCull(WHITE));
  var f=e.facing;var rt=Pool.right(f);
  p.pushPose();
  p.translate(.5-.5*rt.getStepX()-.5*f.getStepX(),Pool.FELT+Pool.R,.5-.5*rt.getStepZ()-.5*f.getStepZ());
  p.mulPose(Axis.YP.rotationDegrees(-90F*f.get2DDataValue()));
  boolean lerp=e.moving;
  for(int i=0;i<16;i++){
   if(e.in[i])continue;
   float x=lerp?Mth.lerp(pt,e.ox[i],e.x[i]):e.x[i],z=lerp?Mth.lerp(pt,e.oz[i],e.z[i]):e.z[i];
   p.pushPose();p.translate(x,0,z);p.mulPose(e.roll[i]);sphere(p,vc,Pool.R,COLORS[i],false,false,lt);p.popPose();
  }
  // aim line and cue for the local player holding a cue near this table
  var pl=mc.player;var held=pl.getMainHandItem();
  if(held.is(Pool.CUE.get())&&!e.moving&&e.phase!=2&&!e.in[0]&&Pool.nearest(mc.level,pl.position())==e){
   var look=pl.getViewVector(pt);double lx=look.x,lz=look.z,n=Math.sqrt(lx*lx+lz*lz);
   if(n>1e-3){lx/=n;lz/=n;
    float du=(float)(lx*rt.getStepX()+lz*rt.getStepZ()),dv=(float)(lx*f.getStepX()+lz*f.getStepZ());
    aim(e,p,vc,du,dv,lt);
    float charge=pl.isUsingItem()&&pl.getUseItem().is(Pool.CUE.get())?Math.min(1F,pl.getTicksUsingItem()/(float)Pool.FULL_CHARGE):0;
    p.pushPose();p.translate(e.x[0],0,e.z[0]);p.mulPose(Axis.YP.rotation((float)Math.atan2(du,dv)));p.mulPose(Axis.XP.rotationDegrees(7));
    float back=Pool.R+.03F+charge*.32F;
    box(p,vc,-.009F,-.009F,-back-1.45F,.009F,.009F,-back-.35F,0xFF6B3A1E,lt);   // butt
    box(p,vc,-.007F,-.007F,-back-.35F,.007F,.007F,-back-.02F,0xFFD9B27A,lt);    // shaft
    box(p,vc,-.0075F,-.0075F,-back-.02F,.0075F,.0075F,-back,0xFF2F6BD8,lt);     // chalked tip
    p.popPose();
   }
  }
  p.popPose();
 }
 static String grp(int g){return g==1?"lisas":g==2?"listradas":"—";}
 static String left(Pool.PoolEntity e,int who){int g=e.group[who];if(g==0)return "—";int n=0;for(int b=1;b<=15;b++)if((g==1?b<=7:b>=9)&&!e.in[b])n++;return n==0?"falta a 8":n+" bolas";}
 void text(PoseStack p,MultiBufferSource buf,String s,int y){var comp=Component.literal(s);float w=font.width(comp);
  font.drawInBatch(comp,-w/2,y,0xFFFFFFFF,false,p.last().pose(),buf,Font.DisplayMode.NORMAL,0x66000000,0xF000F0);}
 /** dashed line from the white ball to the first thing it would hit, plus the direction the hit ball would go */
 void aim(Pool.PoolEntity e,PoseStack p,VertexConsumer vc,float du,float dv,int lt){
  float sx=e.x[0],sz=e.z[0],R=Pool.R,best=9;int hit=-1;
  for(int i=1;i<16;i++){if(e.in[i])continue;float ex=e.x[i]-sx,ez=e.z[i]-sz,t=ex*du+ez*dv;if(t<=0)continue;
   float perp2=ex*ex+ez*ez-t*t,rr=4*R*R;if(perp2>=rr)continue;float tt=t-(float)Math.sqrt(rr-perp2);if(tt<best){best=tt;hit=i;}}
  float lo=Pool.RAIL+R,hiU=Pool.TW-Pool.RAIL-R,hiV=Pool.TL-Pool.RAIL-R;
  float tw=9;if(du>1e-4F)tw=Math.min(tw,(hiU-sx)/du);if(du<-1e-4F)tw=Math.min(tw,(lo-sx)/du);if(dv>1e-4F)tw=Math.min(tw,(hiV-sz)/dv);if(dv<-1e-4F)tw=Math.min(tw,(lo-sz)/dv);
  if(tw<best){best=Math.max(0,tw);hit=-1;}
  float y=-R+.004F;
  for(float t=R+.02F;t<best;t+=.08F){float t2=Math.min(best,t+.045F);seg(p,vc,sx+du*t,sz+dv*t,sx+du*t2,sz+dv*t2,y,.006F,0xFFFFFFFF,lt);}
  float gx=sx+du*best,gz=sz+dv*best;
  ring(p,vc,gx,gz,y,R,0xFFFFFFFF,lt);
  if(hit>0){float nx=e.x[hit]-gx,nz=e.z[hit]-gz,n=(float)Math.sqrt(nx*nx+nz*nz);if(n>1e-5F){nx/=n;nz/=n;
   seg(p,vc,e.x[hit]+nx*R,e.z[hit]+nz*R,e.x[hit]+nx*(R+.3F),e.z[hit]+nz*(R+.3F),y,.006F,0xFFFFE070,lt);}}
 }
 static void seg(PoseStack p,VertexConsumer vc,float x0,float z0,float x1,float z1,float y,float w,int col,int lt){
  float dx=x1-x0,dz=z1-z0,n=(float)Math.sqrt(dx*dx+dz*dz);if(n<1e-5F)return;float px=-dz/n*w,pz=dx/n*w;
  var ps=p.last();
  v(vc,ps,x0+px,y,z0+pz,col,0,1,0,lt);v(vc,ps,x1+px,y,z1+pz,col,0,1,0,lt);v(vc,ps,x1-px,y,z1-pz,col,0,1,0,lt);v(vc,ps,x0-px,y,z0-pz,col,0,1,0,lt);
 }
 static void ring(PoseStack p,VertexConsumer vc,float cx,float cz,float y,float r,int col,int lt){
  for(int j=0;j<LON;j++)seg(p,vc,cx+SIN[0][j]*r,cz+SIN[1][j]*r,cx+SIN[0][j+1]*r,cz+SIN[1][j+1]*r,y,.004F,col,lt);}
 static void v(VertexConsumer vc,PoseStack.Pose ps,float x,float y,float z,int col,float nx,float ny,float nz,int lt){
  vc.addVertex(ps,x,y,z).setColor(col).setUv(.5F,.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lt).setNormal(ps,nx,ny,nz);}
 /** UV sphere; stripes: only the middle bands get the colour */
 static void sphere(PoseStack p,VertexConsumer vc,float r,int col,boolean stripe,boolean dot,int lt){
  var ps=p.last();
  for(int i=0;i<LAT;i++){
   double a0=-Math.PI/2+Math.PI*i/LAT,a1=-Math.PI/2+Math.PI*(i+1)/LAT;float y0=(float)Math.sin(a0),y1=(float)Math.sin(a1),c0=(float)Math.cos(a0),c1=(float)Math.cos(a1);
   int band=stripe&&(i<=1||i>=LAT-2)?0xFFF4F1E6:col;
   if(dot&&i==LAT-1)band=0xFFF4F1E6;   // the little white number spot on top
   for(int j=0;j<LON;j++){
    float x00=SIN[0][j]*c0,z00=SIN[1][j]*c0,x01=SIN[0][j+1]*c0,z01=SIN[1][j+1]*c0,x10=SIN[0][j]*c1,z10=SIN[1][j]*c1,x11=SIN[0][j+1]*c1,z11=SIN[1][j+1]*c1;
    v(vc,ps,x00*r,y0*r,z00*r,band,x00,y0,z00,lt);v(vc,ps,x10*r,y1*r,z10*r,band,x10,y1,z10,lt);v(vc,ps,x11*r,y1*r,z11*r,band,x11,y1,z11,lt);v(vc,ps,x01*r,y0*r,z01*r,band,x01,y0,z01,lt);
   }
  }
 }
 static void box(PoseStack p,VertexConsumer vc,float x0,float y0,float z0,float x1,float y1,float z1,int col,int lt){
  var ps=p.last();
  v(vc,ps,x0,y1,z0,col,0,1,0,lt);v(vc,ps,x0,y1,z1,col,0,1,0,lt);v(vc,ps,x1,y1,z1,col,0,1,0,lt);v(vc,ps,x1,y1,z0,col,0,1,0,lt);
  v(vc,ps,x0,y0,z0,col,0,-1,0,lt);v(vc,ps,x1,y0,z0,col,0,-1,0,lt);v(vc,ps,x1,y0,z1,col,0,-1,0,lt);v(vc,ps,x0,y0,z1,col,0,-1,0,lt);
  v(vc,ps,x0,y0,z0,col,-1,0,0,lt);v(vc,ps,x0,y0,z1,col,-1,0,0,lt);v(vc,ps,x0,y1,z1,col,-1,0,0,lt);v(vc,ps,x0,y1,z0,col,-1,0,0,lt);
  v(vc,ps,x1,y0,z0,col,1,0,0,lt);v(vc,ps,x1,y1,z0,col,1,0,0,lt);v(vc,ps,x1,y1,z1,col,1,0,0,lt);v(vc,ps,x1,y0,z1,col,1,0,0,lt);
  v(vc,ps,x0,y0,z0,col,0,0,-1,lt);v(vc,ps,x0,y1,z0,col,0,0,-1,lt);v(vc,ps,x1,y1,z0,col,0,0,-1,lt);v(vc,ps,x1,y0,z0,col,0,0,-1,lt);
  v(vc,ps,x0,y0,z1,col,0,0,1,lt);v(vc,ps,x1,y0,z1,col,0,0,1,lt);v(vc,ps,x1,y1,z1,col,0,0,1,lt);v(vc,ps,x0,y1,z1,col,0,0,1,lt);
 }
}
