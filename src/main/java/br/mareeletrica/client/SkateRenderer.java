package br.mareeletrica.client;

import br.mareeletrica.Skate;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** 0.65.6: the skateboard, with its trick animation (kickflip roll, shove-it spin, nose up on the ollie). */
public final class SkateRenderer extends EntityRenderer<Skate.Board> {
 public SkateRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.35F;}
 @Override public ResourceLocation getTextureLocation(Skate.Board e){return TextureAtlas.LOCATION_BLOCKS;}
 @Override public void render(Skate.Board e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
  p.pushPose();
  p.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(pt,e.yRotO,e.getYRot())+Mth.lerp(pt,e.shoveO,e.shove)));
  p.translate(0,.22*Skate.Board.SCALE,0);
  p.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(pt,e.noseO,e.nose)));
  p.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pt,e.flipO,e.flip)+Mth.lerp(pt,e.leanO,e.lean)));
  p.scale(Skate.Board.SCALE,Skate.Board.SCALE,Skate.Board.SCALE);p.translate(-.5,-.22,-.5);
  FrogRenderer.part("skateboard",p,b,light,OverlayTexture.NO_OVERLAY);
  p.popPose();super.render(e,yaw,pt,p,b,light);
 }
}
