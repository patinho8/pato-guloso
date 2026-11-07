package br.mareeletrica.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** Draws a block model (nose toward -z, centred on 8,8,8) pointed along the entity's flight path. */
public final class ModelProjectileRenderer<T extends Entity> extends EntityRenderer<T> {
 private final String model;private final float scale,centerY;private final boolean noseDown;
 public ModelProjectileRenderer(EntityRendererProvider.Context c,String model,float scale){this(c,model,scale,8F,false);}
 /** centerY: model pixel height of the projectile's axis; noseDown: tips over and falls nose first (bombs). */
 public ModelProjectileRenderer(EntityRendererProvider.Context c,String model,float scale,float centerY,boolean noseDown){super(c);this.model=model;this.scale=scale;this.centerY=centerY;this.noseDown=noseDown;}
 @Override public ResourceLocation getTextureLocation(T e){return ResourceLocation.withDefaultNamespace("textures/misc/white.png");}
 @Override public void render(T e,float yaw,float pt,PoseStack pose,MultiBufferSource buffers,int light){
  var v=e.getDeltaMovement();
  float y=(float)Math.toDegrees(Mth.atan2(v.x,v.z)),p=(float)Math.toDegrees(Mth.atan2(v.y,v.horizontalDistance()));
  pose.pushPose();
  if(noseDown)p=Math.max(-90F,Math.min(p,-25F-(e.tickCount+pt)*3.5F));
  pose.translate(0,e.getBbHeight()/2,0);
  pose.mulPose(Axis.YP.rotationDegrees(y+180F));   // model nose (-z) along the velocity
  pose.mulPose(Axis.XP.rotationDegrees(p));
  pose.scale(scale,scale,scale);
  pose.translate(-.5,-centerY/16F,-.5);
  DmtGearLayer.model(model,pose,buffers,light);
  pose.popPose();
  super.render(e,yaw,pt,pose,buffers,light);
 }
}
