package br.mareeletrica.client;

import br.mareeletrica.Sports;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

/** The 3D ball model, centred on its hitbox, rolling as it moves. */
public final class SportBallRenderer extends EntityRenderer<Sports.SportBall> {
 public SportBallRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.35F;}
 @Override public ResourceLocation getTextureLocation(Sports.SportBall e){return TextureAtlas.LOCATION_BLOCKS;}
 @Override public void render(Sports.SportBall e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
  p.pushPose();p.translate(0,.35,0);p.scale(1.45F,1.45F,1.45F);
  float a=Mth.lerp(pt,e.rollO,e.roll);var ax=e.rollAxis;
  p.mulPose(new Quaternionf().rotationAxis(a,(float)ax.x,(float)ax.y,(float)ax.z));
  Minecraft.getInstance().getItemRenderer().renderStatic(e.getItem(),ItemDisplayContext.GROUND,light,OverlayTexture.NO_OVERLAY,p,b,e.level(),e.getId());
  p.popPose();
  super.render(e,yaw,pt,p,b,light);
 }
}
