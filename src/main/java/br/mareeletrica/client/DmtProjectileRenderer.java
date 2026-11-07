package br.mareeletrica.client;
import br.mareeletrica.DmtProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
public final class DmtProjectileRenderer extends EntityRenderer<DmtProjectileEntity> {
 public DmtProjectileRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.08F;}
 @Override public void render(DmtProjectileEntity e,float yaw,float dt,PoseStack p,MultiBufferSource b,int light){p.pushPose();p.mulPose(Axis.YP.rotationDegrees(e.getYRot()));p.mulPose(Axis.XP.rotationDegrees(-e.getXRot()));float scale=e.isSpear()?1.25F:.25F;p.scale(scale,scale,scale);p.translate(-.5,-.5,-.5);DmtGearLayer.model(e.isSpear()?"thunder_spear_flying":"precision_bullet",p,b,light);p.popPose();super.render(e,yaw,dt,p,b,light);}
 @Override public ResourceLocation getTextureLocation(DmtProjectileEntity e){return TextureAtlas.LOCATION_BLOCKS;}
}
