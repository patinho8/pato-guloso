package br.mareeletrica.client;

import br.mareeletrica.DuckBoss;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** 0.72.0: 3D models for the duck boss fight — spinning eggs, feather darts, the ground shockwave ring / dive marker and the kamikaze ducklings. */
public final class BossFx {
 private BossFx(){}
 public static final String[] PARTS={"boss_egg","boss_feather"};
 static abstract class Base<T extends Entity> extends EntityRenderer<T>{Base(EntityRendererProvider.Context c){super(c);}@Override public ResourceLocation getTextureLocation(T e){return TextureAtlas.LOCATION_BLOCKS;}}
 public static final class EggR extends Base<DuckBoss.EvilEgg>{
  public EggR(EntityRendererProvider.Context c){super(c);shadowRadius=.3F;}
  @Override public void render(DuckBoss.EvilEgg e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   float s=e.small()?.55F:1.1F,age=e.tickCount+pt;
   p.pushPose();p.translate(0,.3*s,0);p.mulPose(Axis.YP.rotationDegrees(e.spin+age*9));p.mulPose(Axis.XP.rotationDegrees(age*14));p.scale(s,s,s);p.translate(-.5,-.5,-.5);
   DuckRenderer.part("boss_egg",p,b,1,1,1,light,OverlayTexture.NO_OVERLAY);p.popPose();super.render(e,yaw,pt,p,b,light);}
 }
 public static final class FeatherR extends Base<DuckBoss.Feather>{
  public FeatherR(EntityRendererProvider.Context c){super(c);}
  @Override public void render(DuckBoss.Feather e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   var v=e.getDeltaMovement();float ry=(float)(Mth.atan2(v.x,v.z)*180/Math.PI),rx=(float)(Mth.atan2(v.y,Math.sqrt(v.x*v.x+v.z*v.z))*180/Math.PI);
   p.pushPose();p.mulPose(Axis.YP.rotationDegrees(ry));p.mulPose(Axis.XP.rotationDegrees(-rx));p.mulPose(Axis.ZP.rotationDegrees((e.tickCount+pt)*25));p.translate(-.5,-.5,-.5);
   DuckRenderer.part("boss_feather",p,b,1,1,1,light,OverlayTexture.NO_OVERLAY);p.popPose();super.render(e,yaw,pt,p,b,light);}
 }
 /** the shockwave: a glowing band around the duck that grows along the ground; as a marker, a pulsing red circle */
 public static final class WaveR extends Base<DuckBoss.Shockwave>{
  public WaveR(EntityRendererProvider.Context c){super(c);}
  @Override public boolean shouldRender(DuckBoss.Shockwave e,net.minecraft.client.renderer.culling.Frustum f,double x,double y,double z){return true;}
  @Override public void render(DuckBoss.Shockwave e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   var vc=b.getBuffer(GlowTypes.HALO);var m=p.last();boolean mark=e.isMarker();
   float r=e.radius(pt),age=e.tickCount+pt;int seg=64;
   float cr=mark?1F:.75F,cg=mark?.15F:.9F,cb=mark?.1F:1F;
   float a=mark?.35F+.3F*Mth.sin(age*.8F):Mth.clamp(1-r/DuckBoss.Shockwave.MAX,0,1)*.85F;
   float h=mark?.08F:1.0F,in=mark?r-.45F:r-1.1F;
   for(int i=0;i<seg;i++){double a0=i*Math.PI*2/seg,a1=(i+1)*Math.PI*2/seg;float c0=(float)Math.cos(a0),s0=(float)Math.sin(a0),c1=(float)Math.cos(a1),s1=(float)Math.sin(a1);
    // outer wall (bright at the bottom, fading up)
    vc.addVertex(m,c0*r,0,s0*r).setColor(cr,cg,cb,a);vc.addVertex(m,c1*r,0,s1*r).setColor(cr,cg,cb,a);vc.addVertex(m,c1*r,h,s1*r).setColor(cr,cg,cb,0);vc.addVertex(m,c0*r,h,s0*r).setColor(cr,cg,cb,0);
    // ground band
    vc.addVertex(m,c0*in,.02F,s0*in).setColor(cr,cg,cb,0);vc.addVertex(m,c1*in,.02F,s1*in).setColor(cr,cg,cb,0);vc.addVertex(m,c1*r,.02F,s1*r).setColor(cr,cg,cb,a*.8F);vc.addVertex(m,c0*r,.02F,s0*r).setColor(cr,cg,cb,a*.8F);
   }
   if(mark){float rr=r*(.4F+.6F*((age*.05F)%1F));   // shrinking inner ring: "it's coming down here"
    for(int i=0;i<seg;i++){double a0=i*Math.PI*2/seg,a1=(i+1)*Math.PI*2/seg;float c0=(float)Math.cos(a0),s0=(float)Math.sin(a0),c1=(float)Math.cos(a1),s1=(float)Math.sin(a1);
     vc.addVertex(m,c0*(rr-.25F),.03F,s0*(rr-.25F)).setColor(1F,.3F,.1F,0);vc.addVertex(m,c1*(rr-.25F),.03F,s1*(rr-.25F)).setColor(1F,.3F,.1F,0);vc.addVertex(m,c1*rr,.03F,s1*rr).setColor(1F,.3F,.1F,.7F);vc.addVertex(m,c0*rr,.03F,s0*rr).setColor(1F,.3F,.1F,.7F);}}
  }
 }
 /** the kamikaze duckling: a yellow duckling with an angry red headband, swelling and flashing before it pops */
 public static final class DucklingR extends Base<DuckBoss.Duckling>{
  public DucklingR(EntityRendererProvider.Context c){super(c);shadowRadius=.25F;}
  @Override public void render(DuckBoss.Duckling e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   float body=Mth.rotLerp(pt,e.yBodyRotO,e.yBodyRot),walk=e.walkAnimation.position(pt),spd=Math.min(1,e.walkAnimation.speed(pt));int f=e.fuse();
   float swell=f>0?1+f*.025F:1,flash=f>0&&(f/2)%2==0?1:0;int ov=OverlayTexture.pack(OverlayTexture.u(flash),OverlayTexture.v(e.hurtTime>0));
   p.pushPose();p.mulPose(Axis.YP.rotationDegrees(180-body));p.scale(.8F*swell,.8F*swell,.8F*swell);p.mulPose(Axis.ZP.rotationDegrees(Mth.sin(walk*.9F)*spd*10));p.translate(-.5,0,-.5);
   float sw=Mth.cos(walk*.9F)*spd*45;
   p.pushPose();p.translate(9.4/16,3.4/16,8.4/16);p.mulPose(Axis.XP.rotationDegrees(sw));p.translate(-9.4/16,-3.4/16,-8.4/16);DuckRenderer.part("duck_leg_l",p,b,1,1,1,light,ov);p.popPose();
   p.pushPose();p.translate(6.6/16,3.4/16,8.4/16);p.mulPose(Axis.XP.rotationDegrees(-sw));p.translate(-6.6/16,-3.4/16,-8.4/16);DuckRenderer.part("duck_leg_r",p,b,1,1,1,light,ov);p.popPose();
   float y1=1F,y2=.86F,y3=.3F;
   DuckRenderer.part("duck_body",p,b,y1,y2,y3,light,ov);DuckRenderer.part("duck_wing_l",p,b,y1,y2,y3,light,ov);DuckRenderer.part("duck_wing_r",p,b,y1,y2,y3,light,ov);DuckRenderer.part("duck_neck",p,b,y1,y2,y3,light,ov);
   p.pushPose();p.translate(8/16F,8.5/16F,4.5/16F);p.scale(1.3F,1.3F,1.3F);p.translate(-8/16F,-8.5/16F,-4.5/16F);
   DuckRenderer.part("duck_head",p,b,y1,y2,y3,light,ov);DuckRenderer.part("duck_beak",p,b,1,1,1,light,ov);DuckRenderer.part("duck_eyes",p,b,1,1,1,light,ov);DuckRenderer.part("duck_hat_cap",p,b,1,.2F,.2F,light,ov);p.popPose();
   p.popPose();super.render(e,yaw,pt,p,b,light);}
 }
}
