package br.mareeletrica.client;

import br.mareeletrica.Drones;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Draws the drone from block models: body, four spinning rotors and the bomb-bay doors. */
public final class DroneRenderer extends EntityRenderer<Drones.DroneEntity> {
 private static final float[][] MOTORS={{2F,2F},{14F,2F},{2F,14F},{14F,14F}};
 public DroneRenderer(EntityRendererProvider.Context context){super(context);}
 @Override public ResourceLocation getTextureLocation(Drones.DroneEntity entity){return ResourceLocation.withDefaultNamespace("textures/misc/white.png");}
 @Override public void render(Drones.DroneEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
  float y=Mth.rotLerp(partial,e.yRotO,e.getYRot());
  double vx=e.getX()-e.xo,vz=e.getZ()-e.zo,rad=Math.toRadians(y);
  float fwd=(float)(vx*-Math.sin(rad)+vz*Math.cos(rad)),side=(float)(vx*-Math.cos(rad)+vz*-Math.sin(rad));
  pose.pushPose();
  pose.translate(0,-1F/16F,0);
  pose.mulPose(Axis.YP.rotationDegrees(180F-y));
  pose.mulPose(Axis.XP.rotationDegrees(Mth.clamp(fwd*70F,-18F,18F)));
  pose.mulPose(Axis.ZP.rotationDegrees(Mth.clamp(side*70F,-18F,18F)));
  pose.translate(-.5,0,-.5);
  if(e.courier()){   // delivery drone: its own body and four rotors; a cargo crate hangs under it while it carries something
   DmtGearLayer.model("courier_drone",pose,buffers,light);
   float a=(e.tickCount+partial)*(e.crashing()?20F:e.powered()?60F:8F);
   for(int i=0;i<4;i++){pose.pushPose();pose.translate((i%2==0?.12:.88),9.6/16F,(i<2?.12:.88));pose.mulPose(Axis.YP.rotationDegrees(a+i*45));pose.translate(-.5,-.5,-.5);DmtGearLayer.model("courier_rotor",pose,buffers,light);pose.popPose();}
   if(e.cargoStacks()>0){pose.pushPose();pose.translate(.5,-.05,.5);float c=.26F+e.cargoStacks()*.018F;pose.scale(c,c,c);pose.translate(-.5,-1,-.5);
    net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState(),pose,buffers,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);pose.popPose();}
   pose.popPose();return;
  }
  DmtGearLayer.model("drone_body",pose,buffers,light);
  float spin=(e.tickCount+partial)*(e.crashing()?20F:e.powered()?95F:12F);
  for(int i=0;i<MOTORS.length;i++){
   pose.pushPose();
   pose.translate(MOTORS[i][0]/16F,7.85F/16F,MOTORS[i][1]/16F);
   pose.mulPose(Axis.YP.rotationDegrees(i%2==0?spin:-spin));
   pose.translate(-.5,0,-.5);
   DmtGearLayer.model("drone_rotor",pose,buffers,light);
   pose.popPose();
  }
  // bombs ride inside the bay; the doors swing open only while dropping
  float open=Mth.lerp(partial,e.hatchO,e.hatch)*80F;
  pose.pushPose();pose.translate(5.7F/16F,3.4F/16F,0);pose.mulPose(Axis.ZP.rotationDegrees(-open));pose.translate(-5.7F/16F,-3.4F/16F,0);DmtGearLayer.model("drone_hatch_l",pose,buffers,light);pose.popPose();
  pose.pushPose();pose.translate(10.3F/16F,3.4F/16F,0);pose.mulPose(Axis.ZP.rotationDegrees(open));pose.translate(-10.3F/16F,-3.4F/16F,0);DmtGearLayer.model("drone_hatch_r",pose,buffers,light);pose.popPose();
  pose.popPose();
 }
}
