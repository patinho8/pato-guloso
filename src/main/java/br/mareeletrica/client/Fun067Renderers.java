package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.MiniGolf;
import br.mareeletrica.PingPong;
import br.mareeletrica.Surf;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/** 0.67.0: minigolf windmill sails, ping-pong ball, surfboard, the moving wave mesh, and the surfer poses. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class Fun067Renderers {
 private Fun067Renderers(){}
 public static final String[] PARTS={"minigolf_windmill_blades","surfboard"};

 // ---------------- windmill ----------------
 public static final class WindmillR implements BlockEntityRenderer<MiniGolf.WindmillEntity> {
  public WindmillR(BlockEntityRendererProvider.Context c){}
  @Override public void render(MiniGolf.WindmillEntity e,float pt,PoseStack p,MultiBufferSource b,int light,int overlay){
   var s=e.getBlockState();if(!s.hasProperty(HorizontalDirectionalBlock.FACING)||e.getLevel()==null)return;
   var f=s.getValue(HorizontalDirectionalBlock.FACING);
   p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(180-f.toYRot()));p.translate(-.5,0,-.5);
   p.translate(.5,1,0);p.mulPose(Axis.ZP.rotationDegrees(MiniGolf.sailAngle(e.getLevel().getGameTime(),pt)));p.translate(-.5,-1,0);
   FrogRenderer.part("minigolf_windmill_blades",p,b,light,overlay);
   p.popPose();}
  @Override public AABB getRenderBoundingBox(MiniGolf.WindmillEntity e){return new AABB(e.getBlockPos()).inflate(1.2);}
 }

 // ---------------- ping-pong ball ----------------
 public static final class PingBallR extends EntityRenderer<PingPong.Ball> {
  public PingBallR(EntityRendererProvider.Context c){super(c);shadowRadius=.05F;}
  @Override public ResourceLocation getTextureLocation(PingPong.Ball e){return PoolRenderer.WHITE;}
  @Override public void render(PingPong.Ball e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   p.pushPose();p.translate(0,.06,0);PoolRenderer.sphere(p,b.getBuffer(RenderType.entityCutoutNoCull(PoolRenderer.WHITE)),.06F,0xFFFAF6EE,false,false,light);p.popPose();
   super.render(e,yaw,pt,p,b,light);}
 }

 // ---------------- surfboard ----------------
 public static final class BoardR extends EntityRenderer<Surf.Board> {
  public BoardR(EntityRendererProvider.Context c){super(c);shadowRadius=.4F;}
  @Override public ResourceLocation getTextureLocation(Surf.Board e){return TextureAtlas.LOCATION_BLOCKS;}
  @Override public void render(Surf.Board e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   p.pushPose();
   p.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(pt,e.yRotO,e.getYRot())+Mth.lerp(pt,e.spinO,e.spin)));
   p.translate(0,.03,0);
   p.mulPose(Axis.XP.rotationDegrees(Mth.lerp(pt,e.tiltO,e.tilt)));
   p.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pt,e.leanO,e.lean)));
   p.translate(-.5,0,-.5);
   FrogRenderer.part("surfboard",p,b,light,OverlayTexture.NO_OVERLAY);
   p.popPose();super.render(e,yaw,pt,p,b,light);}
 }

 // ---------------- wave ----------------
 public static final class WaveR extends EntityRenderer<Surf.Wave> {
  public WaveR(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
  @Override public ResourceLocation getTextureLocation(Surf.Wave e){return PoolRenderer.WHITE;}
  @Override public boolean shouldRender(Surf.Wave e,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){return true;}
  @Override public void render(Surf.Wave w,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   Vec3 at=new Vec3(Mth.lerp(pt,w.xo,w.getX()),Mth.lerp(pt,w.yo,w.getY()),Mth.lerp(pt,w.zo,w.getZ()));
   Vec3 c=w.crest(pt);
   p.pushPose();p.translate(c.x-at.x,c.y-at.y+.03,c.z-at.z);
   Vec3 f=w.dir();float fx=(float)f.x,fz=(float)f.z,sx=-fz,sz=fx;
   VertexConsumer vc=b.getBuffer(RenderType.entityTranslucent(PoolRenderer.WHITE));
   var ps=p.last();
   float half=w.width()/2+2.5F;int NA=22,NB=Math.max(6,(int)(half*2/1.2F));
   float a0=-5.2F,a1=2.8F;
   for(int i=0;i<NA;i++)for(int j=0;j<NB;j++){
    float aa=a0+(a1-a0)*i/NA,ab=a0+(a1-a0)*(i+1)/NA,ba=-half+2*half*j/NB,bb=-half+2*half*(j+1)/NB;
    vert(vc,ps,w,aa,ba,fx,fz,sx,sz,light);vert(vc,ps,w,aa,bb,fx,fz,sx,sz,light);vert(vc,ps,w,ab,bb,fx,fz,sx,sz,light);vert(vc,ps,w,ab,ba,fx,fz,sx,sz,light);
   }
   p.popPose();}
  static void vert(VertexConsumer vc,PoseStack.Pose ps,Surf.Wave w,float a,float bb,float fx,float fz,float sx,float sz,int light){
   float h=(float)w.height(a,bb);float k=h/Math.max(.1F,w.height());
   int alpha=(int)(40+150*Math.min(1,k*1.4F));
   int r,g,bl;
   if(a>-.7F&&a<.5F&&k>.6F){r=235;g=248;bl=255;alpha=Math.max(alpha,210);}   // foam on the crest
   else{r=(int)Mth.lerp(k,40,70);g=(int)Mth.lerp(k,100,170);bl=(int)Mth.lerp(k,190,215);}
   int col=alpha<<24|r<<16|g<<8|bl;
   // slope normal (rough)
   float dh=(float)(w.height(a+.2,bb)-w.height(a-.2,bb))/.4F;float nx=-fx*dh,nz=-fz*dh,ny=1;float n=Mth.sqrt(nx*nx+ny*ny+nz*nz);
   vc.addVertex(ps,fx*a+sx*bb,h,fz*a+sz*bb).setColor(col).setUv(.5F,.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ps,nx/n,ny/n,nz/n);
  }
 }

 // ---------------- surfer: lying on the board ----------------
 static boolean pushed;
 @SubscribeEvent public static void lieDown(RenderPlayerEvent.Pre e){
  pushed=false;
  if(!(e.getEntity().getVehicle() instanceof Surf.Board b)||(b.anim()&(Surf.RIDE|Surf.AIR))!=0)return;
  var p=e.getPoseStack();float pt=e.getPartialTick();var pl=e.getEntity();
  float by=Mth.rotLerp(pt,pl.yBodyRotO,pl.yBodyRot);
  p.pushPose();pushed=true;
  p.translate(0,Surf.Board.LIE_DROP+.12,0);
  p.mulPose(Axis.YP.rotationDegrees(180-by));
  p.translate(0,0,.75);
  p.mulPose(Axis.XP.rotationDegrees(-90));
  p.mulPose(Axis.YP.rotationDegrees(-(180-by)));
 }
 @SubscribeEvent public static void standUp(RenderPlayerEvent.Post e){if(pushed){pushed=false;e.getPoseStack().popPose();}}

 /** limbs (called from the player-model mixin) */
 public static void pose(PlayerModel<?> m,LivingEntity e){
  if(!(e.getVehicle() instanceof Surf.Board b))return;
  int f=b.anim();float t=e.tickCount+Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
  m.rightLeg.yRot=0;m.leftLeg.yRot=0;m.rightArm.yRot=0;m.leftArm.yRot=0;m.body.xRot=0;m.body.zRot=0;m.head.zRot=0;
  if((f&(Surf.RIDE|Surf.AIR))==0){   // prone: paddling with crawl strokes, head up looking ahead
   m.head.xRot=-1.05F;
   if((f&Surf.STROKE)!=0){float ph=t*.32F;m.rightArm.xRot=-(ph%Mth.TWO_PI);m.leftArm.xRot=-((ph+Mth.PI)%Mth.TWO_PI);m.rightArm.zRot=.15F;m.leftArm.zRot=-.15F;}
   else{m.rightArm.xRot=-.15F+Mth.sin(t*.08F)*.1F;m.leftArm.xRot=-.15F-Mth.sin(t*.08F)*.1F;m.rightArm.zRot=.35F;m.leftArm.zRot=-.35F;}
   m.rightLeg.xRot=Mth.sin(t*.25F)*.12F;m.leftLeg.xRot=-Mth.sin(t*.25F)*.12F;m.rightLeg.zRot=.08F;m.leftLeg.zRot=-.08F;
  }else{
   float crouch=2.4F,wob=Mth.sin(t*.15F)*.08F;
   m.rightLeg.xRot=0;m.leftLeg.xRot=0;m.rightLeg.zRot=.38F;m.leftLeg.zRot=-.38F;
   m.rightArm.xRot=-.25F;m.leftArm.xRot=-.25F;m.rightArm.zRot=1.05F+wob;m.leftArm.zRot=-1.05F+wob;m.body.xRot=.2F;
   if((f&Surf.AIR)!=0){crouch=3.4F;m.rightLeg.xRot=-.8F;m.leftLeg.xRot=-.8F;m.rightLeg.zRot=.5F;m.leftLeg.zRot=-.5F;
    m.rightArm.xRot=-1.2F;m.rightArm.zRot=.5F;m.leftArm.zRot=-1.7F;}   // grab the rail
   if((f&Surf.LEFT)!=0){m.leftArm.zRot-=.4F;m.rightArm.zRot-=.4F;m.body.zRot=-.12F;}
   if((f&Surf.RIGHT)!=0){m.leftArm.zRot+=.4F;m.rightArm.zRot+=.4F;m.body.zRot=.12F;}
   m.body.y+=crouch;m.head.y+=crouch;m.rightArm.y+=crouch;m.leftArm.y+=crouch;
  }
  m.hat.copyFrom(m.head);m.jacket.copyFrom(m.body);m.leftSleeve.copyFrom(m.leftArm);m.rightSleeve.copyFrom(m.rightArm);m.leftPants.copyFrom(m.leftLeg);m.rightPants.copyFrom(m.rightLeg);
 }
}
