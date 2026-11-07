package br.mareeletrica.client;

import br.mareeletrica.GroundGuns;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

/** A gun lying on its side on the ground. */
public final class GroundGunRenderer extends EntityRenderer<GroundGuns.GroundGun> {
 public GroundGunRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.3F;}
 @Override public ResourceLocation getTextureLocation(GroundGuns.GroundGun e){return ResourceLocation.withDefaultNamespace("textures/misc/white.png");}
 @Override public void render(GroundGuns.GroundGun e,float yaw,float pt,PoseStack pose,MultiBufferSource buffers,int light){
  var s=e.stack();if(s.isEmpty())return;
  pose.pushPose();
  pose.translate(0,.08,0);pose.mulPose(Axis.YP.rotationDegrees(-e.getYRot()));pose.mulPose(Axis.XP.rotationDegrees(90));pose.scale(1.4F,1.4F,1.4F);
  Minecraft.getInstance().getItemRenderer().renderStatic(s,ItemDisplayContext.FIXED,light,OverlayTexture.NO_OVERLAY,pose,buffers,e.level(),e.getId());
  pose.popPose();
  super.render(e,yaw,pt,pose,buffers,light);
 }
}
