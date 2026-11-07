package br.mareeletrica.client;
import br.mareeletrica.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
public final class HydroRenderer {
 private HydroRenderer(){}
 public static void render(MachineEntity m,float partial,PoseStack pose,MultiBufferSource buffers,int light){pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-PotabilizerStructure.turn(m.getBlockState().getValue(MachineBlock.FACING))*90));float angle=m.angle+(m.getBlockState().getValue(MachineBlock.ACTIVE)?partial*HydroGenerators.speed(m.kind):0);
  if(m.kind==Kind.GIANT_WHEEL){int w=WheelConnections.width(m);for(boolean front:new boolean[]{true,false})if(WheelConnections.connected(m,front)){pose.pushPose();pose.translate(w/2.0-1,w/2.0-.5,w==7?(front?.5:4.5):(front?-.5:w-1.5));DmtGearLayer.model(w==7?(front?"wheel_large_connector_front":"wheel_large_connector_back"):(front?"wheel_connector_front":"wheel_connector_back"),pose,buffers,light);pose.popPose();}pose.translate((w-1)/2.0,w/2.0,(w-1)/2.0);pose.mulPose(Axis.ZP.rotationDegrees(angle));if(m.crank){pose.pushPose();pose.translate(-.5,-.5,(w==7?-1.3:-.55)-1);DmtGearLayer.model("wheel_crank",pose,buffers,light);pose.popPose();}pose.scale(w-.1F,w-.1F,w==7?2.6F:1.1F);}
  else if(m.kind==Kind.CURRENT_TURBINE){pose.translate(1,1.6,.20);pose.mulPose(Axis.ZP.rotationDegrees(angle));pose.scale(2.2F,2.2F,.9F);}
  else {pose.translate(1,m.kind==Kind.FALL_TURBINE?1.45:1.55,1);pose.mulPose(Axis.YP.rotationDegrees(angle));pose.scale(m.kind==Kind.FALL_TURBINE?2.3F:1.7F,m.kind==Kind.FALL_TURBINE?1.7F:2.4F,m.kind==Kind.FALL_TURBINE?2.3F:1.7F);}
  pose.translate(-.5,-.5,-.5);DmtGearLayer.model(m.kind.id+"_rotor",pose,buffers,light);pose.popPose();
 }
}
