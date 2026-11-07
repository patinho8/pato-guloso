package br.mareeletrica.client;

import br.mareeletrica.Batteries;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/** Sentry gun (spinning barrel cluster), flak battery (twin barrels that recoil in turn) and auto grenade launcher (drum + kicking barrel).
 *  Every model points -z; the turret turns around the block centre and the gun tilts around (8, pivotY, 8). */
public final class BatteryRenderer implements BlockEntityRenderer<Batteries.Gun> {
 public BatteryRenderer(BlockEntityRendererProvider.Context c){}
 @Override public void render(Batteries.Gun g,float pt,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  var k=g.kind();String n=k.id;float py=k.pivotY;
  long now=g.getLevel()==null?0:g.getLevel().getGameTime();
  DmtGearLayer.model(n+"_base",pose,buffers,light);
  float yaw=g.yawO+Mth.wrapDegrees(g.yaw-g.yawO)*pt,pitch=Mth.lerp(pt,g.pitchO,g.pitch);
  pose.pushPose();
  pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(180F-yaw));pose.translate(-.5,0,-.5);
  DmtGearLayer.model(n+"_turret",pose,buffers,light);
  pose.translate(.5,py,.5);pose.mulPose(Axis.XP.rotationDegrees(pitch));pose.translate(-.5,-py,-.5);
  DmtGearLayer.model(n+"_gun",pose,buffers,light);
  switch(k){
   case SENTRY->{   // gatling cluster spins up while it fires
    float spin=Mth.lerp(pt,g.spinO,g.spin);
    pose.pushPose();pose.translate(.5,py,.5);pose.mulPose(Axis.ZP.rotationDegrees(spin));pose.translate(-.5,-py,-.5);
    DmtGearLayer.model(n+"_barrel",pose,buffers,light);pose.popPose();
   }
   case FLAK->{
    for(int i=0;i<2;i++){
     pose.pushPose();pose.translate((i==0?-3.2F:3.2F)/16F,0,kick(now-g.firedAt[i]+pt)*3F/16F);
     DmtGearLayer.model(n+"_barrel",pose,buffers,light);pose.popPose();
    }
   }
   case GRENADE->{
    pose.pushPose();pose.translate(0,0,kick(now-g.firedAt[0]+pt)*1.8F/16F);
    DmtGearLayer.model(n+"_barrel",pose,buffers,light);pose.popPose();
   }
  }
  pose.popPose();
 }
 static float kick(float age){return age<0||age>9?0:(age<1.5F?age/1.5F:1-(age-1.5F)/7.5F);}
 @Override public AABB getRenderBoundingBox(Batteries.Gun g){return new AABB(g.getBlockPos()).inflate(1.5);}
}
