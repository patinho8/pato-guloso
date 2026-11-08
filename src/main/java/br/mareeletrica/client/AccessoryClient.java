package br.mareeletrica.client;

import br.mareeletrica.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/** 0.74.0: the laser line and dot, and the flashlight spot, for every player holding a gun with them. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class AccessoryClient {
 private AccessoryClient(){}
 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();if(mc.level==null)return;
  float pt=e.getPartialTick().getGameTimeDeltaPartialTick(false);Vec3 cam=e.getCamera().getPosition();var buffers=mc.renderBuffers().bufferSource();boolean any=false;
  PoseStack p=e.getPoseStack();p.pushPose();p.translate(-cam.x,-cam.y,-cam.z);
  for(Player pl:mc.level.players()){
   var gun=pl.getMainHandItem();var f=WeaponFamily.of(gun);if(f==null)continue;var k=Accessory.beam(gun);if(k==null||pl.isSpectator())continue;
   Vec3 eye=pl.getEyePosition(pt),look=pl.getViewVector(pt);var hit=AccessoryLight.ray(pl,eye,look,80);Vec3 end=hit.getLocation();boolean landed=hit.getType()!=HitResult.Type.MISS;
   Vec3 right=look.cross(new Vec3(0,1,0));if(right.lengthSqr()<1e-4)right=new Vec3(1,0,0);right=right.normalize();Vec3 up=right.cross(look).normalize();
   boolean me=pl==mc.player&&mc.options.getCameraType().isFirstPerson();float ads=me?SniperClient.aimAmount(pt):0;
   Vec3 start=me?eye.add(look.scale(.55)).add(right.scale(.2*(1-ads)+.03)).add(up.scale(-.16+.08*ads)):eye.add(look.scale(.7)).add(right.scale(.32)).add(up.scale(-.38));
   var vc=buffers.getBuffer(GlowTypes.HALO);any=true;Matrix4f m=p.last().pose();
   if(k.light()&&landed){double d=end.distanceTo(eye);float r=(float)Math.min(2.6,.35+d*.09);Vec3 n=end.subtract(look.scale(.05));billboard(vc,m,n,cam,r,255,246,215,(int)Math.max(50,170-d*3));billboard(vc,m,n,cam,r*.45F,255,250,230,(int)Math.max(40,150-d*2.5));
    cone(vc,m,start,end,look,cam,Math.min(r,1.2F),255,245,215,me?10:22);}
   if(k.laser()){int r=k==Accessory.Kind.LASER_GREEN?60:255,g=k==Accessory.Kind.LASER_GREEN?255:30,b=k==Accessory.Kind.LASER_GREEN?70:25;
    beam(vc,m,start,end,cam,me?.006F:.012F,r,g,b,me?120:160);
    if(landed){Vec3 n=end.subtract(look.scale(.03));double d=end.distanceTo(eye);float s=(float)(.035+d*.004);billboard(vc,m,n,cam,s,r,g,b,255);billboard(vc,m,n,cam,s*.5F,255,255,255,200);billboard(vc,m,n,cam,s*3,r,g,b,70);}}
  }
  p.popPose();if(any)buffers.endBatch(GlowTypes.HALO);
 }
 static void quad(VertexConsumer vc,Matrix4f m,Vec3 a,Vec3 b,Vec3 c,Vec3 d,int r,int g,int bl,int al){for(Vec3 v:new Vec3[]{a,b,c,d})vc.addVertex(m,(float)v.x,(float)v.y,(float)v.z).setColor(r,g,bl,al);}
 /** a round soft glow facing the camera: bright centre fading to nothing at the edge */
 static void billboard(VertexConsumer vc,Matrix4f m,Vec3 c,Vec3 cam,float s,int r,int g,int b,int a){Vec3 to=cam.subtract(c).normalize();Vec3 u=to.cross(new Vec3(0,1,0));if(u.lengthSqr()<1e-4)u=new Vec3(1,0,0);u=u.normalize();Vec3 v=u.cross(to).normalize();
  int n=16;for(int i=0;i<n;i++){double a0=i*Math.PI*2/n,a1=(i+1)*Math.PI*2/n;Vec3 p0=c.add(u.scale(Math.cos(a0)*s)).add(v.scale(Math.sin(a0)*s)),p1=c.add(u.scale(Math.cos(a1)*s)).add(v.scale(Math.sin(a1)*s));
   vc.addVertex(m,(float)c.x,(float)c.y,(float)c.z).setColor(r,g,b,a);vc.addVertex(m,(float)c.x,(float)c.y,(float)c.z).setColor(r,g,b,a);vc.addVertex(m,(float)p1.x,(float)p1.y,(float)p1.z).setColor(r,g,b,0);vc.addVertex(m,(float)p0.x,(float)p0.y,(float)p0.z).setColor(r,g,b,0);}}
 static void beam(VertexConsumer vc,Matrix4f m,Vec3 a,Vec3 b,Vec3 cam,float w,int r,int g,int bl,int al){Vec3 dir=b.subtract(a).normalize();Vec3 to=cam.subtract(a.add(b).scale(.5)).normalize();Vec3 s=dir.cross(to);if(s.lengthSqr()<1e-6)return;s=s.normalize().scale(w);quad(vc,m,a.add(s),a.subtract(s),b.subtract(s.scale(2)),b.add(s.scale(2)),r,g,bl,al);}
 static void cone(VertexConsumer vc,Matrix4f m,Vec3 a,Vec3 b,Vec3 look,Vec3 cam,float rad,int r,int g,int bl,int al){Vec3 to=cam.subtract(a.add(b).scale(.5)).normalize();Vec3 s=look.cross(to);if(s.lengthSqr()<1e-6)return;s=s.normalize();quad(vc,m,a.add(s.scale(.04)),a.subtract(s.scale(.04)),b.subtract(s.scale(rad)),b.add(s.scale(rad)),r,g,bl,al);}
}
