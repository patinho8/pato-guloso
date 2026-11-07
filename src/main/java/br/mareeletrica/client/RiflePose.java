package br.mareeletrica.client;
import br.mareeletrica.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
/** Override vanilla swimming arms after animation; use both hands to shoulder the rifle. Also military crawl and peeking. */
public final class RiflePose {
 public static void apply(PlayerModel<?> model,LivingEntity entity,float yaw,float pitch,float swing,float amount){
  if(!(entity instanceof Player p))return;
  boolean prone=ProneSystem.prone(p);
  if(!SniperItem.firearm(p.getMainHandItem())){
   if(prone)crawl(model,p,yaw,pitch,swing,amount,false);
   lean(model,p);
   copyOverlays(model);
   return;
  }
  boolean aim=SniperItem.scoped(p),right=p.getMainArm()==HumanoidArm.RIGHT;
  float angle=Mth.clamp(pitch,-65,65)*Mth.DEG_TO_RAD,turn=Mth.clamp(yaw,-40,40)*Mth.DEG_TO_RAD;
  var main=right?model.rightArm:model.leftArm;var support=right?model.leftArm:model.rightArm;
  float side=right?1:-1,raise=prone?-Mth.PI:-Mth.HALF_PI;
  main.xRot=raise+angle+(aim||prone?.02F:.35F);main.yRot=turn-side*.14F;main.zRot=0;
  support.xRot=raise+angle+(aim||prone?.08F:.42F);
  support.yRot=turn+side*(p.getMainHandItem().getItem() instanceof GunItem g&&g.profile.pistol?.75F:.56F);support.zRot=side*.06F;
  if(prone){
   main.yRot=support.yRot=0;main.zRot=-turn;support.zRot=-side*.8F-turn;
   model.head.xRot=-Mth.HALF_PI+angle;model.head.yRot=turn;model.hat.copyFrom(model.head);
   crawl(model,p,yaw,pitch,swing,amount,true);
  }
  lean(model,p);
  copyOverlays(model);
 }
 /** Military crawl: body twists slowly, legs splay and drag alternately, arms pull when unarmed. */
 private static void crawl(PlayerModel<?> model,Player p,float yaw,float pitch,float swing,float amount,boolean armed){
  float move=Mth.clamp(amount*5F,0F,1F),phase=swing*.55F,wave=Mth.cos(phase),sway=Mth.sin(phase),turn=Mth.clamp(yaw,-40,40)*Mth.DEG_TO_RAD,angle=Mth.clamp(pitch,-65,65)*Mth.DEG_TO_RAD;
  // low crawl: shoulders twist toward the pulling elbow, the opposite knee drives and drags
  model.body.yRot=sway*.34F*move;model.body.zRot=sway*.08F*move;
  model.rightLeg.xRot=Math.max(0,wave)*.70F*move;model.leftLeg.xRot=Math.max(0,-wave)*.70F*move;
  model.rightLeg.yRot=Math.max(0,wave)*.22F*move;model.leftLeg.yRot=-Math.max(0,-wave)*.22F*move;
  model.rightLeg.zRot=.14F+Math.max(0,wave)*.50F*move;model.leftLeg.zRot=-.14F-Math.max(0,-wave)*.50F*move;
  model.head.zRot=-sway*.10F*move;
  if(!armed){
   model.head.xRot=-Mth.HALF_PI+angle;model.head.yRot=turn;model.hat.copyFrom(model.head);
   model.rightArm.xRot=-Mth.PI+.20F-wave*.60F*move;model.leftArm.xRot=-Mth.PI+.20F+wave*.60F*move;
   model.rightArm.yRot=-.45F+Math.max(0,wave)*.35F*move;model.leftArm.yRot=.45F-Math.max(0,-wave)*.35F*move;
   model.rightArm.zRot=-.35F-Math.max(0,wave)*.40F*move;model.leftArm.zRot=.35F+Math.max(0,-wave)*.40F*move;
  }
 }
 private static void lean(PlayerModel<?> model,Player p){
  float lean=LeanClient.of(p);
  if(lean==0){model.body.x=0;model.head.x=0;model.hat.copyFrom(model.head);return;}
  float t=-lean*.30F,s=Mth.sin(t),c=Mth.cos(t),dx=12F*s,dy=12F*(1F-c);
  model.body.zRot+=t;model.body.x=dx;model.body.y+=dy;
  model.head.zRot+=t;model.head.x=dx;model.head.y+=dy;model.hat.copyFrom(model.head);
  for(var arm:new net.minecraft.client.model.geom.ModelPart[]{model.rightArm,model.leftArm}){float ax=arm.x,ay=arm.y;arm.x=dx+ax*c-ay*s;arm.y=dy+ax*s+ay*c;arm.zRot+=t;}
 }
 private static void copyOverlays(PlayerModel<?> model){
  model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);
  model.rightPants.copyFrom(model.rightLeg);model.leftPants.copyFrom(model.leftLeg);model.jacket.copyFrom(model.body);
 }
}
