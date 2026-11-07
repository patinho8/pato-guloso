package br.mareeletrica.client;
import br.mareeletrica.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class DmtHands {
 @SubscribeEvent public static void hand(RenderHandEvent e){var mc=Minecraft.getInstance();if(mc.player==null||RemoteClient.active()||AimRemoteItem.active(mc.player.getMainHandItem())||DmtCombat.mode(mc.player)==0)return;boolean left=e.getHand()==InteractionHand.OFF_HAND;var item=DmtCombat.visual(mc.player,left?1:0);e.setCanceled(true);if(item.isEmpty())return;var pose=e.getPoseStack();pose.pushPose();double swing=Math.sin(Math.sqrt(mc.player.getAttackAnim(e.getPartialTick()))*Math.PI);pose.translate(left?-.48:.48,-.44-swing*.08,-.72+swing*.12);pose.mulPose(Axis.XP.rotationDegrees((float)(-swing*45)));pose.mulPose(Axis.YP.rotationDegrees((float)((left?-1:1)*swing*20)));mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(mc.player,item,left?ItemDisplayContext.FIRST_PERSON_LEFT_HAND:ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,left,pose,e.getMultiBufferSource(),e.getPackedLight());pose.popPose();}
}
