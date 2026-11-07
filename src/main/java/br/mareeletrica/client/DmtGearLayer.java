package br.mareeletrica.client;
import br.mareeletrica.DmtItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.*;
public final class DmtGearLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
 public DmtGearLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> p){super(p);}
 public static void model(String name,PoseStack pose,MultiBufferSource buffers,int light){var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)),null,mc.getModelManager().getModel(ClientEvents.part(name)),1,1,1,light,OverlayTexture.NO_OVERLAY);}
 @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,float limbSwing,float limbAmount,float partial,float age,float headYaw,float headPitch){if(!DmtItem.enabled(player)||player.isInvisible())return;pose.pushPose();getParentModel().body.translateAndRotate(pose);pose.translate(-.5,1.35,.25);pose.scale(1,-.75F,-.5F);model("dmt_gear",pose,buffers,light);pose.popPose();
  for(int hand=0;hand<2;hand++){var item=br.mareeletrica.DmtCombat.visual(player,hand);if(item.isEmpty())continue;boolean left=hand==1;pose.pushPose();getParentModel().translateToHand(left?net.minecraft.world.entity.HumanoidArm.LEFT:net.minecraft.world.entity.HumanoidArm.RIGHT,pose);pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));pose.translate((left?-1:1)/16F,.125,-.625);Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(player,item,left?net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND:net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,left,pose,buffers,light);pose.popPose();}
 }
}
