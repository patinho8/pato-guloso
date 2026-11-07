package br.mareeletrica.client;

import br.mareeletrica.Barrage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/** Barrage launcher: fixed base, turret turning to the azimuth, pod tilting to the elevation, ten tubes that kick back when they fire. */
public final class BarrageRenderer implements BlockEntityRenderer<Barrage.LauncherEntity> {
 public BarrageRenderer(BlockEntityRendererProvider.Context c){}
 @Override public void render(Barrage.LauncherEntity l,float pt,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  DmtGearLayer.model("barrage_base",pose,buffers,light);
  float yaw=l.curYawO+Mth.wrapDegrees(l.curYaw-l.curYawO)*pt,elev=Mth.lerp(pt,l.curElevO,l.curElev);
  pose.pushPose();
  pose.translate(.5,0,.5);
  pose.mulPose(Axis.YP.rotationDegrees(180F-l.facing().toYRot()-yaw));
  pose.translate(-.5,0,-.5);
  DmtGearLayer.model("barrage_turret",pose,buffers,light);
  pose.translate(.5,Barrage.PIVOT_Y,.5);
  pose.mulPose(Axis.XP.rotationDegrees(elev));
  pose.translate(-.5,-Barrage.PIVOT_Y,-.5);
  DmtGearLayer.model("barrage_pod",pose,buffers,light);
  long now=l.getLevel()==null?0:l.getLevel().getGameTime();
  for(int i=0;i<Barrage.TUBES;i++){
   float age=now-l.firedAt[i]+pt,kick=age<0||age>8?0:(age<1.5F?age/1.5F:1-(age-1.5F)/6.5F);
   pose.pushPose();
   pose.translate(Barrage.tubeX(i)/16F,Barrage.tubeY(i)/16F,kick*1.6F/16F);
   DmtGearLayer.model("barrage_tube",pose,buffers,light);
   pose.popPose();
  }
  pose.popPose();
 }
 @Override public AABB getRenderBoundingBox(Barrage.LauncherEntity l){return new AABB(l.getBlockPos()).inflate(1.5);}
}
