package br.mareeletrica.client;

import br.mareeletrica.Tracers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** A bright stretched bullet with a tracer streak, drawn along its flight direction at full brightness. */
public final class TracerRenderer extends EntityRenderer<Tracers.Round> {
 public TracerRenderer(EntityRendererProvider.Context c){super(c);}
 @Override public void render(Tracers.Round e,float yaw,float dt,PoseStack p,MultiBufferSource b,int light){
  if(e.tickCount<1&&dt<.5F)return;   // skip the very first frame inside the muzzle
  p.pushPose();
  p.mulPose(Axis.YP.rotationDegrees(Mth.lerp(dt,e.yRotO,e.getYRot())));
  p.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(dt,e.xRotO,e.getXRot())));
  p.scale(.5F,.5F,2.2F);   // long streak trailing behind the round
  p.translate(-.5,-.5,-1);
  DmtGearLayer.model("tracer_round",p,b,0xF000F0);
  p.popPose();
  super.render(e,yaw,dt,p,b,light);
 }
 @Override public ResourceLocation getTextureLocation(Tracers.Round e){return TextureAtlas.LOCATION_BLOCKS;}
}
