package br.mareeletrica.client;

import br.mareeletrica.Jets;
import br.mareeletrica.WarPlus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** The B-2 that crosses the sky during an airstrike. */
public final class PlaneRenderer extends EntityRenderer<WarPlus.Plane> {
 public PlaneRenderer(EntityRendererProvider.Context c){super(c);}
 @Override public ResourceLocation getTextureLocation(WarPlus.Plane e){return ResourceLocation.withDefaultNamespace("textures/misc/white.png");}
 @Override public void render(WarPlus.Plane e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
  var v=e.getDeltaMovement();float y=(float)Math.toDegrees(Math.atan2(v.x,v.z));
  p.pushPose();p.mulPose(Axis.YP.rotationDegrees(y+180F));
  float s=Jets.SCALE*1.6F;p.scale(s,s,s);p.translate(-.5,-6F/16F,-.5);
  DmtGearLayer.model("b2_bomber",p,b,0xF000F0);
  p.popPose();
 }
}
