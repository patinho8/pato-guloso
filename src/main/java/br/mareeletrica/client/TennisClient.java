package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.TennisBall;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.network.PacketDistributor;

/** Tennis on the client: a racket swing tells the server which ball it met; the first-person racket does a real forehand. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class TennisClient {
 private TennisClient(){}
 @SubscribeEvent public static void swing(InputEvent.InteractionKeyMappingTriggered e){
  var mc=Minecraft.getInstance();var p=mc.player;
  if(!e.isAttack()||p==null||mc.level==null||!p.getMainHandItem().is(TennisBall.RACKET.get()))return;
  Vec3 eye=p.getEyePosition(),look=p.getLookAngle();
  TennisBall.Ball best=null;double score=1e9;
  double reach=br.mareeletrica.SportsMode.mode(p)==br.mareeletrica.SportsMode.Mode.TENNIS?7.0:5.8;
  for(var b:mc.level.getEntitiesOfClass(TennisBall.Ball.class,p.getBoundingBox().inflate(reach))){
   Vec3 to=b.position().subtract(eye);double d=b.position().distanceTo(p.position().add(0,1,0));
   if(d>reach||to.normalize().dot(look)<-.45)continue;
   double s=Math.abs(d-2.2);if(s<score){score=s;best=b;}
  }
  if(best!=null)PacketDistributor.sendToServer(new TennisBall.HitBall(best.getId()));
 }
 /** First-person forehand: racket back on the right, sweeps across in front and finishes over the other shoulder. */
 public static final IClientItemExtensions RACKET=new IClientItemExtensions(){
  @Override public boolean applyForgeHandTransform(PoseStack pose,LocalPlayer player,HumanoidArm arm,ItemStack stack,float partialTick,float equip,float swing){
   int side=arm==HumanoidArm.RIGHT?1:-1;
   pose.translate(side*.56F,-.52F-equip*.6F,-.72F);
   if(swing>0){
    float e=1-(1-swing)*(1-swing);   // fast through contact, slower follow-through
    float arc=Mth.sin(swing*Mth.PI);
    pose.translate(-side*(e*.75F-.2F),arc*.12F,-arc*.3F);
    pose.mulPose(Axis.YP.rotationDegrees(side*Mth.lerp(e,35F,-70F)));
    pose.mulPose(Axis.ZP.rotationDegrees(-side*arc*35F));
    pose.mulPose(Axis.XP.rotationDegrees(-arc*15F));
   }
   return true;
  }
 };
}
