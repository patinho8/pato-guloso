package br.mareeletrica.client;

import br.mareeletrica.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;

/** Renderers for the base blocks: desk monitors, siren beacon, runway chase lights, lift platform, delivery drone, shield dome. */
public final class BaseRenderers {
 private BaseRenderers(){}
 static final int FULL=LightTexture.FULL_BRIGHT;

 /** Live text on the three desk monitors. */
 public static final class PanelR implements BlockEntityRenderer<BaseControl.Panel> {
  private final Font font;
  public PanelR(BlockEntityRendererProvider.Context c){font=c.getFont();}
  @Override public AABB getRenderBoundingBox(BaseControl.Panel b){return new AABB(b.getBlockPos()).inflate(1.5,1.5,1.5);}
  @Override public void render(BaseControl.Panel b,float pt,PoseStack p,MultiBufferSource buf,int light,int ov){
   var f=b.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
   p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(180F-f.toYRot()));
   String[][] lines={{"RADAR",b.contacts+" no ar"},{"ENERGIA","+"+b.genRate+" / -"+b.useRate},{b.alarm?"⚠ ALARME":"BASE OK",b.problems>0?b.problems+" pane(s)":"sem panes"}};
   int[] col={0xFF60FF80,0xFFFFE070,b.alarm?0xFFFF4040:0xFF60C0FF};
   for(int i=0;i<3;i++){
    p.pushPose();
    // three monitors along the back of the 3-wide desk (centres 14 px apart), facing the player (-z)
    float mx=(i-1)*14/16F;
    p.translate(mx,19/16F,10.8F/16F-.5F);p.mulPose(Axis.YP.rotationDegrees(180));
    int w=Math.max(font.width(lines[i][0]),font.width(lines[i][1]));float s=Math.min(1/80F,.62F/Math.max(1,w));p.scale(s,-s,s);
    for(int k=0;k<2;k++){String t=lines[i][k];font.drawInBatch(t,-font.width(t)/2F,-10+k*11,k==0?col[i]:0xFFDDDDDD,false,p.last().pose(),buf,Font.DisplayMode.POLYGON_OFFSET,0,FULL);}
    p.popPose();
   }
   p.popPose();
  }
 }
 /** Rotating red beacon when the alarm is on. */
 public static final class SirenR implements BlockEntityRenderer<BaseControl.Siren> {
  public SirenR(BlockEntityRendererProvider.Context c){}
  @Override public void render(BaseControl.Siren b,float pt,PoseStack p,MultiBufferSource buf,int light,int ov){
   var s=b.getBlockState();if(!s.getValue(BaseControl.ACTIVE))return;var f=s.getValue(DirectionalBlock.FACING);
   float t=(Minecraft.getInstance().level.getGameTime()+pt)*18F;
   p.pushPose();p.translate(.5,.5,.5);
   switch(f){case DOWN->p.mulPose(Axis.XP.rotationDegrees(180));case NORTH->p.mulPose(Axis.XP.rotationDegrees(-90));case SOUTH->p.mulPose(Axis.XP.rotationDegrees(90));case EAST->p.mulPose(Axis.ZP.rotationDegrees(-90));case WEST->p.mulPose(Axis.ZP.rotationDegrees(90));default->{}}
   p.mulPose(Axis.YP.rotationDegrees(t));p.translate(-.5,-.5,-.5);
   DmtGearLayer.model("siren_beam",p,buf,FULL);
   p.popPose();
  }
 }
 /** Chase lights: one lamp in eight is lit, the lit one moves the way the lights point. */
 public static final class RunwayR implements BlockEntityRenderer<BaseBlocks.Light> {
  public RunwayR(BlockEntityRendererProvider.Context c){}
  @Override public void render(BaseBlocks.Light b,float pt,PoseStack p,MultiBufferSource buf,int light,int ov){
   var f=b.getBlockState().getValue(HorizontalDirectionalBlock.FACING);var pos=b.getBlockPos();
   long t=Minecraft.getInstance().level.getGameTime()/3;
   int along=pos.getX()*f.getStepX()+pos.getZ()*f.getStepZ();
   if(Math.floorMod(t-along,8)!=0)return;
   DmtGearLayer.model("runway_glow",p,buf,FULL);
  }
 }
 public static final class PlatformR extends EntityRenderer<BaseBlocks.Platform> {
  public PlatformR(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
  @Override public ResourceLocation getTextureLocation(BaseBlocks.Platform e){return TextureAtlas.LOCATION_BLOCKS;}
  @Override public void render(BaseBlocks.Platform e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){p.pushPose();p.scale(5/3F,1,5/3F);p.translate(-.5,0,-.5);DmtGearLayer.model("lift_platform",p,b,light);p.popPose();
   for(int i=0;i<4;i++){p.pushPose();p.translate(i%2==0?-2.35:2.35,0,i<2?-2.35:2.35);p.translate(-.5,0,-.5);DmtGearLayer.model("lift_post",p,b,light);p.popPose();}super.render(e,yaw,pt,p,b,light);}
 }
 public static final class CourierR extends EntityRenderer<Delivery.Courier> {
  public CourierR(EntityRendererProvider.Context c){super(c);shadowRadius=.4F;}
  @Override public ResourceLocation getTextureLocation(Delivery.Courier e){return TextureAtlas.LOCATION_BLOCKS;}
  @Override public void render(Delivery.Courier e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   p.pushPose();p.mulPose(Axis.YP.rotationDegrees(180F-Mth.rotLerp(pt,e.yRotO,e.getYRot())));p.translate(-.5,0,-.5);
   DmtGearLayer.model("courier_drone",p,b,light);
   float a=e.parkedSynced()?20:(e.tickCount+pt)*60;
   for(int i=0;i<4;i++){p.pushPose();p.translate((i%2==0?.12:.88),9.6/16F,(i<2?.12:.88));p.mulPose(Axis.YP.rotationDegrees(a+i*45));p.translate(-.5,-.5,-.5);DmtGearLayer.model("courier_rotor",p,b,light);p.popPose();}
   p.popPose();super.render(e,yaw,pt,p,b,light);
  }
 }
 /** Lit LED lamp: soft coloured halo plus coloured light thrown on the blocks around it (Minecraft light is always white,
  *  so the colour is painted on the nearby surfaces, brighter close to the lamp). */
 public static final class LampR implements BlockEntityRenderer<Lamps.Glow> {
  static final int R=6;
  /** Faces around each lamp: x,y,z of 4 corners + strength of each corner (16 floats per face). Rebuilt once a second. */
  static final java.util.Map<net.minecraft.core.BlockPos,float[]> FACES=new java.util.HashMap<>();
  static final java.util.Map<net.minecraft.core.BlockPos,Long> BUILT=new java.util.HashMap<>();
  public LampR(BlockEntityRendererProvider.Context c){}
  @Override public AABB getRenderBoundingBox(Lamps.Glow g){return new AABB(g.getBlockPos()).inflate(R+1);}
  @Override public int getViewDistance(){return 48;}
  static float[] faces(Lamps.Glow g){
   var l=g.getLevel();var o=g.getBlockPos();long t=l.getGameTime();
   Long at=BUILT.get(o);if(at!=null&&t-at<20&&t>=at)return FACES.get(o);
   if(FACES.size()>256){FACES.clear();BUILT.clear();}
   var out=new java.util.ArrayList<Float>();var q=new net.minecraft.core.BlockPos.MutableBlockPos();var n=new net.minecraft.core.BlockPos.MutableBlockPos();
   for(int x=-R;x<=R;x++)for(int y=-R;y<=R;y++)for(int z=-R;z<=R;z++){
    if(x*x+y*y+z*z>(R+.5)*(R+.5)||x==0&&y==0&&z==0)continue;
    q.set(o.getX()+x,o.getY()+y,o.getZ()+z);var st=l.getBlockState(q);
    if(st.isAir()||!st.isSolidRender(l,q))continue;
    for(var d:net.minecraft.core.Direction.values()){
     // only faces turned towards the lamp, with open space in front of them
     if(d.getStepX()*-x+d.getStepY()*-y+d.getStepZ()*-z<=0)continue;
     n.setWithOffset(q,d);if(l.getBlockState(n).isSolidRender(l,n))continue;
     float cx=x+.5F+d.getStepX()*.503F,cy=y+.5F+d.getStepY()*.503F,cz=z+.5F+d.getStepZ()*.503F;
     float[][] c=corners(d,cx,cy,cz);
     for(var v:c){out.add(v[0]);out.add(v[1]);out.add(v[2]);
      double dx=v[0]-.5,dy=v[1]-.5,dz=v[2]-.5;double k=Math.max(0,1-Math.sqrt(dx*dx+dy*dy+dz*dz)/(R+.5));out.add((float)(k*k));}
    }
   }
   var a=new float[out.size()];for(int i=0;i<a.length;i++)a[i]=out.get(i);
   FACES.put(o.immutable(),a);BUILT.put(o.immutable(),t);return a;
  }
  /** The 4 corners of a block face centred at c, in drawing order (counter-clockwise seen from outside). */
  static float[][] corners(net.minecraft.core.Direction d,float x,float y,float z){
   float h=.5F;
   return switch(d){
    case UP->new float[][]{{x-h,y,z-h},{x-h,y,z+h},{x+h,y,z+h},{x+h,y,z-h}};
    case DOWN->new float[][]{{x-h,y,z-h},{x+h,y,z-h},{x+h,y,z+h},{x-h,y,z+h}};
    case NORTH->new float[][]{{x-h,y-h,z},{x-h,y+h,z},{x+h,y+h,z},{x+h,y-h,z}};
    case SOUTH->new float[][]{{x-h,y-h,z},{x+h,y-h,z},{x+h,y+h,z},{x-h,y+h,z}};
    case WEST->new float[][]{{x,y-h,z-h},{x,y-h,z+h},{x,y+h,z+h},{x,y+h,z-h}};
    default->new float[][]{{x,y-h,z-h},{x,y+h,z-h},{x,y+h,z+h},{x,y-h,z+h}};
   };
  }
  @Override public void render(Lamps.Glow g,float pt,PoseStack p,MultiBufferSource buf,int light,int ov){
   var s=g.getBlockState();if(!(s.getBlock() instanceof Lamps.LampBlock lb)||!s.getValue(Lamps.LIT))return;
   int rgb=Lamps.RGB[lb.color];int r=rgb>>16&255,gg=rgb>>8&255,b=rgb&255;
   var m=p.last();
   // coloured light on the surroundings (additive, so it tints instead of covering)
   var f=faces(g);
   if(f!=null&&f.length>0){
    VertexConsumer w=buf.getBuffer(RenderType.lightning());
    // stronger at night, when it matters
    float sky=g.getLevel() instanceof net.minecraft.client.multiplayer.ClientLevel cl?cl.getSkyDarken(pt):1F;float night=Mth.clamp((1F-sky)/.8F,0F,1F);
    float power=.26F+.26F*night;
    for(int i=0;i<f.length;i+=4){float a=f[i+3]*power;w.addVertex(m,f[i],f[i+1],f[i+2]).setColor(r/255F,gg/255F,b/255F,a);}
   }
   VertexConsumer v=buf.getBuffer(RenderType.entityTranslucentEmissive(ResourceLocation.withDefaultNamespace("textures/misc/white.png")));
   for(float grow:new float[]{.12F,.28F}){int a=grow<.2F?70:32;float lo=-grow,hi=1+grow;
    float[][] q={{lo,lo,lo, hi,lo,lo, hi,lo,hi, lo,lo,hi, 0,-1,0},{lo,hi,lo, lo,hi,hi, hi,hi,hi, hi,hi,lo, 0,1,0},{lo,lo,lo, lo,hi,lo, hi,hi,lo, hi,lo,lo, 0,0,-1},
     {lo,lo,hi, hi,lo,hi, hi,hi,hi, lo,hi,hi, 0,0,1},{lo,lo,lo, lo,lo,hi, lo,hi,hi, lo,hi,lo, -1,0,0},{hi,lo,lo, hi,hi,lo, hi,hi,hi, hi,lo,hi, 1,0,0}};
    for(var e:q)for(int i=0;i<4;i++)v.addVertex(m,e[i*3],e[i*3+1],e[i*3+2]).setColor(r,gg,b,a).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL).setNormal(m,e[12],e[13],e[14]);}
  }
 }
 /** Rooster ring: the 3x3 model drawn stretched to the 5x5 floor. */
 public static final class RingR implements BlockEntityRenderer<br.mareeletrica.ChickenBattle.Ring> {
  public RingR(BlockEntityRendererProvider.Context c){}
  @Override public AABB getRenderBoundingBox(br.mareeletrica.ChickenBattle.Ring r){return new AABB(r.getBlockPos()).inflate(3,2,3);}
  @Override public void render(br.mareeletrica.ChickenBattle.Ring r,float pt,PoseStack p,MultiBufferSource buf,int light,int ov){
   var f=r.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
   p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(180F-f.toYRot()));p.scale(5/3F,1,5/3F);p.translate(-.5,0,-.5);
   cutout("rooster_ring",p,buf,light);p.popPose();
  }
 }
 /** Draws a block model with transparent pixels (nets, glass) cut out. */
 public static void cutout(String name,PoseStack p,MultiBufferSource buf,int light){var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(p.last(),buf.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)),null,mc.getModelManager().getModel(ClientEvents.part(name)),1,1,1,light,OverlayTexture.NO_OVERLAY);}
 /** Football goal (7 x 3.5 x 2.5) and basketball hoop (big: rim about 3.6 blocks up, wide board and basket): the models are drawn scaled up. */
 public static final class GoalR implements BlockEntityRenderer<Sports.GoalEntity> {
  public GoalR(BlockEntityRendererProvider.Context c){}
  @Override public AABB getRenderBoundingBox(Sports.GoalEntity g){return new AABB(g.getBlockPos()).inflate(6,7,6);}
  @Override public void render(Sports.GoalEntity g,float pt,PoseStack p,MultiBufferSource buf,int light,int ov){
   var s=g.getBlockState();if(!(s.getBlock() instanceof Sports.GoalBlock gb))return;var f=s.getValue(HorizontalDirectionalBlock.FACING);
   p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(180F-f.toYRot()));p.translate(-.5,0,-.5);
   if(gb.hoop){p.translate(.5,0,15/16F);p.scale(2.5F,2.2F,2.5F);p.translate(-.5,0,-15/16F);cutout("basketball_hoop",p,buf,light);
    // the net swishes after a basket: it stretches down and sways, then settles
    float sw=g.swish+pt,k=sw<24?(float)Math.sin(sw*.55F)*(1-sw/24F):0;
    p.translate(.5,26F/16F,6.75F/16F);p.scale(1-k*.08F,1+Math.abs(k)*.35F,1-k*.08F);p.mulPose(Axis.XP.rotationDegrees(k*9F));p.translate(-.5,-26F/16F,-6.75F/16F);
    cutout("basketball_hoop_net",p,buf,light);}
   else{float k=(float)Sports.GoalEntity.G;p.translate(.5,0,1/16F);p.scale(7/3F*k,7/3F*k,8/3F*k);p.translate(-.5,0,-1/16F);cutout("soccer_goal",p,buf,light);}
   p.popPose();
  }
 }
 /** Translucent shield dome (a lat/long sphere) drawn by the shield generator. */
 public static void dome(PoseStack p,MultiBufferSource buf,float t){
  VertexConsumer v=buf.getBuffer(RenderType.entityTranslucentEmissive(ResourceLocation.withDefaultNamespace("textures/misc/white.png")));
  float r=BaseControl.SHIELD_RADIUS;int seg=32,rings=16;
  var m=p.last();
  for(int i=0;i<rings;i++){double a0=Math.PI*i/rings/2*2-Math.PI/2,a1=Math.PI*(i+1)/rings-Math.PI/2;
   if(a1<-0.05)continue;   // upper half only (and a little below the horizon)
   for(int j=0;j<seg;j++){double b0=2*Math.PI*j/seg,b1=2*Math.PI*(j+1)/seg;
    int alpha=(int)(40+25*Math.sin(t*.05+i*.6+j*.3));
    quad(v,m,r,a0,b0,a1,b1,alpha);}}
 }
 private static void quad(VertexConsumer v,PoseStack.Pose m,float r,double a0,double b0,double a1,double b1,int alpha){
  double[][] pts={{a0,b0},{a0,b1},{a1,b1},{a1,b0}};
  for(double[] q:pts){float x=(float)(Math.cos(q[0])*Math.cos(q[1])),y=(float)Math.sin(q[0]),z=(float)(Math.cos(q[0])*Math.sin(q[1]));
   v.addVertex(m,x*r,y*r,z*r).setColor(90,190,255,alpha).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL).setNormal(m,x,y,z);}
 }
}
