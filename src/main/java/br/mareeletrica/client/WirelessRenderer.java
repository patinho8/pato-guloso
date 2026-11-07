package br.mareeletrica.client;
import br.mareeletrica.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.MultiBufferSource;
public final class WirelessRenderer {
 private static void orient(PoseStack p,Direction f){switch(f){case SOUTH->{}case NORTH->p.mulPose(Axis.YP.rotationDegrees(180));case WEST->p.mulPose(Axis.YP.rotationDegrees(-90));case EAST->p.mulPose(Axis.YP.rotationDegrees(90));default->{}}}
 public static void render(MachineEntity m,PoseStack pose,MultiBufferSource buffers,int light){if(!WirelessPower.supported(m))return;var face=m.getBlockState().getValue(MachineBlock.FACING);var center=MachinePanel.center(m.kind,m.getBlockState());pose.pushPose();pose.translate(center.x,center.y,center.z);orient(pose,face);pose.scale(.40F,.34F,.6F);pose.translate(-.5,-.25,0);DmtGearLayer.model("wireless_panel",pose,buffers,light);pose.popPose();
  for(int tier=0;tier<3;tier++){var point=WirelessPower.socket(m,tier).add(net.minecraft.world.phys.Vec3.atLowerCornerOf(face.getNormal()).scale(.006));pose.pushPose();pose.translate(point.x,point.y,point.z);orient(pose,face);pose.scale(.1F,.1F,.1F);pose.translate(-.5,-.5,.012);DmtGearLayer.model("wireless_socket",pose,buffers,light);if(WirelessPower.hasTier(m,tier)){pose.translate(0,0,.08);DmtGearLayer.model("wireless_plate_"+tier,pose,buffers,light);}pose.popPose();}
 }
}
