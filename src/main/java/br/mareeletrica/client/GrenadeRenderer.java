package br.mareeletrica.client;

import br.mareeletrica.Grenades;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Draws the grenade's own 3D model, tumbling in the air and rolling on its side on the ground. */
public final class GrenadeRenderer extends EntityRenderer<Grenades.GrenadeEntity> {
 public GrenadeRenderer(EntityRendererProvider.Context c){super(c);}
 @Override public ResourceLocation getTextureLocation(Grenades.GrenadeEntity e){return ResourceLocation.withDefaultNamespace("textures/misc/white.png");}
 @Override public void render(Grenades.GrenadeEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
  float spin=Mth.lerp(partial,e.spinO,e.spin);
  pose.pushPose();
  pose.translate(0,.125,0);
  pose.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(partial,e.yRotO,e.getYRot())));
  if(e.onGround()&&e.getDeltaMovement().horizontalDistanceSqr()<1e-5){pose.mulPose(Axis.ZP.rotationDegrees(90));}   // resting on its side
  else{pose.mulPose(Axis.XP.rotationDegrees(spin));pose.mulPose(Axis.ZP.rotationDegrees(90));}
  pose.scale(.42F,.42F,.42F);
  pose.translate(-.5,-.5,-.5);
  DmtGearLayer.model("grenade_"+e.kind().id,pose,buffers,light);
  pose.popPose();
 }
}
