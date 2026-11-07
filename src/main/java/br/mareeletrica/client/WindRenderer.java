package br.mareeletrica.client;

import br.mareeletrica.Kind;
import br.mareeletrica.MachineBlock;
import br.mareeletrica.MachineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;

/** Moving parts of the wind machines: rotors (3 blades or 4 sails), the vertical rooftop rotor, the giant tower and the anemometer cups. */
public final class WindRenderer {
 private WindRenderer(){}
 public static void render(MachineEntity m,float pt,PoseStack pose,MultiBufferSource b,int light,int overlay){
  boolean active=m.getBlockState().getValue(MachineBlock.ACTIVE);
  float angle=m.angle+(active?br.mareeletrica.WindPower.spin(m.kind)*pt:0);
  var facing=m.getBlockState().getValue(MachineBlock.FACING);
  pose.pushPose();
  pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(180F-facing.toYRot()));
  switch(m.kind){
   case WIND_SMALL->rotor(pose,b,light,1.13,-.42,.62F,3,angle,"wind_blade");
   case WIND_MEDIUM->rotor(pose,b,light,1.88,-.5,1.5F,3,angle,"wind_blade");
   case WIND_GIANT->{
    for(int i=0;i<4;i++){pose.pushPose();pose.translate(-.5,2+i*2,-.5);DmtGearLayer.model("wind_tower_segment",pose,b,light);pose.popPose();}
    pose.pushPose();pose.translate(-.5,9.9,-.5);DmtGearLayer.model("wind_nacelle",pose,b,light);pose.popPose();
    rotor(pose,b,light,10.15,-1.2,3.6F,3,angle,"wind_blade");
   }
   case WINDMILL->rotor(pose,b,light,1.7,-.4,1.25F,4,angle,"windmill_sail");
   case WIND_ROOF->{pose.pushPose();pose.translate(0,.25,0);pose.mulPose(Axis.YP.rotationDegrees(angle));pose.translate(-.5,0,-.5);DmtGearLayer.model("wind_savonius",pose,b,light);pose.popPose();}
   case WIND_MAST->{pose.pushPose();pose.translate(0,1.8,0);pose.mulPose(Axis.YP.rotationDegrees(angle*3));pose.translate(-.5,0,-.5);DmtGearLayer.model("anemometer_cups",pose,b,light);pose.popPose();}
   default->{}
  }
  pose.popPose();
 }
 /** Rotor in the block's local frame (facing -z after the yaw), hub at (0,y,z). Each blade model points up from (8,8,8). */
 private static void rotor(PoseStack pose,MultiBufferSource b,int light,double y,double z,float scale,int blades,float angle,String blade){
  pose.pushPose();pose.translate(0,y,z);pose.mulPose(Axis.ZP.rotationDegrees(angle));pose.scale(scale,scale,scale);
  pose.pushPose();pose.translate(-.5,-.5,-.5);DmtGearLayer.model("wind_hub",pose,b,light);pose.popPose();
  for(int i=0;i<blades;i++){pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(i*360F/blades));pose.translate(-.5,-.5,-.5);DmtGearLayer.model(blade,pose,b,light);pose.popPose();}
  pose.popPose();
 }
}
