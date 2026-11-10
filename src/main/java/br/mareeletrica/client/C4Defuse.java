package br.mareeletrica.client;

import br.mareeletrica.C4;
import br.mareeletrica.MareEletrica;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

/** 0.82.0: hold the use button on an armed C4 to defuse it — both hands work on the wires, a progress bar fills, and the last wire is snipped. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class C4Defuse {
 private C4Defuse(){}
 static BlockPos target;static int ticks,need;static int doneFlash;static boolean waitRelease;
 public static boolean active(){return target!=null;}
 static BlockPos looking(){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!(mc.hitResult instanceof BlockHitResult b)||b.getType()!=HitResult.Type.BLOCK)return null;
  var st=mc.level.getBlockState(b.getBlockPos());if(!(st.getBlock() instanceof C4.C4Block)||!st.getValue(C4.C4Block.ARMED))return null;
  return mc.player.distanceToSqr(b.getBlockPos().getCenter())<=25?b.getBlockPos():null;}
 static void stop(boolean tell){if(target!=null&&tell)PacketDistributor.sendToServer(new C4.Defuse(target,1));target=null;ticks=0;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(doneFlash>0)doneFlash--;if(waitRelease&&!mc.options.keyUse.isDown())waitRelease=false;
  if(mc.player==null||mc.screen!=null){stop(true);return;}
  var at=mc.options.keyUse.isDown()&&!waitRelease?looking():null;
  if(at==null){stop(true);return;}
  if(!at.equals(target)){stop(true);target=at;ticks=0;need=C4.defuseTicks(mc.player);PacketDistributor.sendToServer(new C4.Defuse(at,0));}
  ticks++;
  // sounds and sparks while working on the wires
  if(ticks%6==0)mc.player.playSound(SoundEvents.LEVER_CLICK,.35F,1.6F+mc.player.getRandom().nextFloat()*.4F);
  if(ticks%20==0)mc.player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(),.6F,1.2F+ticks/(float)need*.8F);
  if(ticks%9==0)mc.level.addParticle(ParticleTypes.ELECTRIC_SPARK,at.getX()+.5,at.getY()+.4,at.getZ()+.5,(mc.player.getRandom().nextFloat()-.5F)*.1,.05,(mc.player.getRandom().nextFloat()-.5F)*.1);
  if(ticks%10==0)mc.player.swing(InteractionHand.MAIN_HAND);   // others see the arm working
  if(ticks>=need){PacketDistributor.sendToServer(new C4.Defuse(at,2));target=null;ticks=0;doneFlash=30;waitRelease=true;}
 }
 /** after a defuse, keep holding the button does not open the timer screen of the now-safe C4 */
 @SubscribeEvent public static void noUse(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered e){if(e.isUseItem()&&(waitRelease||target!=null)){e.setCanceled(true);e.setSwingHand(false);}}
 /** progress bar under the crosshair */
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();var g=e.getGuiGraphics();int cx=g.guiWidth()/2,cy=g.guiHeight()/2+22;
  if(target!=null){float k=Mth.clamp(ticks/(float)need,0,1);int w=120;
   g.fill(cx-w/2-1,cy-1,cx+w/2+1,cy+7,0xC0000000);g.fill(cx-w/2,cy,cx-w/2+(int)(w*k),cy+6,0xFF3AD06A);
   float left=(need-ticks)/20F;String t="✂ Desarmando C4… "+String.format("%.1f",Math.max(0,left))+" s"+(mc.player.getMainHandItem().is(Items.SHEARS)?"":"  §7(tesoura: 2x mais rápido)");
   g.drawCenteredString(mc.font,t,cx,cy+10,0xFFFFFFFF);}

 }
 /** both hands in front of the camera, working on the wires (right hand with the shears if held) */
 @SubscribeEvent public static void hands(RenderHandEvent e){
  if(target==null)return;var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return;
  e.setCanceled(true);if(e.getHand()!=InteractionHand.MAIN_HAND)return;
  if(!(mc.getEntityRenderDispatcher().getRenderer(p) instanceof PlayerRenderer r))return;
  PlayerModel<AbstractClientPlayer> model=r.getModel();
  float t=ticks+e.getPartialTick(),k=Mth.clamp(t/need,0,1),reach=Math.min(1,t/8F);
  PoseStack pose=e.getPoseStack();var buffers=e.getMultiBufferSource();int light=e.getPackedLight();
  // left hand steadies the charge, right hand picks at the wires; a big snip at the end
  Vector3f left=new Vector3f(-.15F,-.21F+Mth.sin(t*.13F)*.008F,-.62F),right=new Vector3f(.07F+Mth.sin(t*.47F)*.035F,-.15F+Mth.sin(t*.71F)*.025F,-.60F+Mth.cos(t*.39F)*.02F);
  if(k>.9F){float s=(k-.9F)/.1F;right.add(0,-.04F*s,.05F*s);}
  Vector3f restL=new Vector3f(-.45F,-.9F,-.3F),restR=new Vector3f(.45F,-.9F,-.3F);
  left=new Vector3f(restL).lerp(left,reach);right=new Vector3f(restR).lerp(right,reach);
  RifleHands.arm(pose,buffers,light,model.leftArm,model.leftSleeve,false,p.isModelPartShown(PlayerModelPart.LEFT_SLEEVE),new Vector3f(-.35F,-.64F,.04F),left);
  RifleHands.arm(pose,buffers,light,model.rightArm,model.rightSleeve,true,p.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE),new Vector3f(.35F,-.64F,.04F),right);
  var held=p.getMainHandItem();
  if(held.is(Items.SHEARS)){   // the shears between the fingers, opening and closing
   pose.pushPose();pose.translate(right.x-.01F,right.y+.06F,right.z-.04F);pose.mulPose(Axis.YP.rotationDegrees(180));pose.mulPose(Axis.XP.rotationDegrees(-20+Mth.sin(t*.9F)*12));pose.mulPose(Axis.ZP.rotationDegrees(45));pose.scale(.32F,.32F,.32F);
   mc.getItemRenderer().renderStatic(new ItemStack(Items.SHEARS),ItemDisplayContext.FIXED,light,OverlayTexture.NO_OVERLAY,pose,buffers,mc.level,0);pose.popPose();}
 }
}
