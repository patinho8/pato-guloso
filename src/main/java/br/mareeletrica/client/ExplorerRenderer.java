package br.mareeletrica.client;

import br.mareeletrica.ExplorerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public final class ExplorerRenderer extends EntityRenderer<ExplorerEntity> {
    public ExplorerRenderer(EntityRendererProvider.Context context){super(context);shadowRadius=.75F;}
    private void model(String name,PoseStack pose,MultiBufferSource buffers,int light) {
        var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)),null,
            mc.getModelManager().getModel(ClientEvents.part(name)),1,1,1,light,OverlayTexture.NO_OVERLAY);
    }
    @Override public void render(ExplorerEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {

        float boat=net.minecraft.util.Mth.lerp(partial,entity.previousBoatFold,entity.boatFold);
        float wings=net.minecraft.util.Mth.lerp(partial,entity.previousWingFold,entity.wingFold);
        pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180-yaw));
        if(entity.mode()==br.mareeletrica.VehicleRules.BOAT || entity.mode()==br.mareeletrica.VehicleRules.PLANE){boolean plane=entity.mode()==br.mareeletrica.VehicleRules.PLANE;if(plane)pose.mulPose(Axis.XP.rotationDegrees(entity.getXRot()*.35F));pose.translate(plane?-1.3:-.9,plane?.1:.18,plane?-1.4:-1.2);pose.scale(plane?2.6F:1.8F,plane?1.2F:1.0F,plane?2.8F:2.4F);model(plane?"explorer_plane":"explorer_boat",pose,buffers,light);pose.popPose();super.render(entity,yaw,partial,pose,buffers,light);return;}

        pose.pushPose();pose.translate(-.65,.12,-.8);pose.scale(1.3F,1.3F,1.6F);model("explorer_body",pose,buffers,light);pose.popPose();
        if(boat>.02F){pose.pushPose();pose.translate(-.95,-.03,-1.05);pose.scale(1.9F,.65F*boat,2.1F);model("explorer_hull",pose,buffers,light);pose.popPose();}
        if(wings>.02F)for(int side:new int[]{-1,1}){
            pose.pushPose();pose.translate(side*.56,.95,-.5);pose.mulPose(Axis.ZP.rotationDegrees(side*(1-wings)*75));
            if(side<0)pose.translate(-1.2*wings,0,0);pose.scale(1.2F*wings,.3F,1.1F);
            model(side<0?"explorer_wing_left":"explorer_wing",pose,buffers,light);pose.popPose();
        }
        for(double x:new double[]{-.69,.69})for(double z:new double[]{-.58,0,.58}) {
            pose.pushPose();pose.translate(x*(1-.35*boat),.31+.33*boat,z);pose.mulPose(Axis.XP.rotationDegrees(entity.wheelAngle+entity.wheelStep*partial));pose.scale(.8F,.8F,.8F);pose.translate(-.5,-.5,-.5);model("explorer_wheel",pose,buffers,light);pose.popPose();
        }
        pose.popPose();super.render(entity,yaw,partial,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(ExplorerEntity entity){return TextureAtlas.LOCATION_BLOCKS;}
}
