package br.mareeletrica.client;

import br.mareeletrica.*;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Vector3f;

/** First-person knife viewmodel: the player's own arm and fist grip the knife; every knife has its own attack and inspect motion. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class KnifeHands {
 private enum Style{FORWARD,SIDE,PHOTO}
 /** grip point in model units (upright model), hold style, resting hand position (camera space). */
 private record Spec(float gx,float gy,Style style,float hx,float hy,float hz){}
 private static final Map<String,Spec> SPECS=Map.of(
  "butterfly_knife",new Spec(7.05F,4.6F,Style.FORWARD,.20F,-.30F,-.54F),
  "karambit",new Spec(8F,6.2F,Style.PHOTO,.20F,-.31F,-.52F),
  "xeno_hunter",new Spec(8.2F,3.6F,Style.FORWARD,.20F,-.30F,-.54F),
  "hrt3_knife",new Spec(8.3F,3.6F,Style.FORWARD,.20F,-.30F,-.54F),
  "banana",new Spec(7.6F,8.2F,Style.SIDE,.20F,-.31F,-.54F));
 // attack keyframes by swing progress: s, dx, dy, dz, yaw, pitch, roll, open
 private static float[] k(float s,float dx,float dy,float dz,float yaw,float pitch,float roll,float open){return new float[]{s,dx,dy,dz,yaw,pitch,roll,open};}
 private static final float[] Z={0,0,0,0,0,0,0,0};
 private static final Map<String,float[][]> ATTACK=Map.of(
  "butterfly_knife",new float[][]{Z,k(.20F,0,.05F,0,0,-25,0,.7F),k(.50F,0,0,-.12F,0,15,30,1),k(.80F,0,0,-.04F,0,0,10,1),k(1,0,0,0,0,0,0,0)},
  "karambit",new float[][]{Z,k(.20F,.07F,-.30F,-.02F,14,-62,-18,0),k(.42F,.01F,-.06F,-.17F,2,4,-2,0),k(.64F,-.08F,.30F,-.19F,-14,72,24,0),k(.84F,-.03F,.10F,-.07F,-5,24,8,0),k(1,0,0,0,0,0,0,0)},   // deep rip: low under, then high over
  "xeno_hunter",new float[][]{Z,k(.20F,0,0,.06F,0,0,0,0),k(.50F,0,0,-.34F,-8,2,0,0),k(1,0,0,0,0,0,0,0)},
  "hrt3_knife",new float[][]{Z,k(.25F,0,.10F,-.05F,0,-55,0,0),k(.55F,0,-.02F,-.22F,0,30,0,0),k(1,0,0,0,0,0,0,0)},
  "banana",new float[][]{Z,k(.25F,0,.12F,0,0,-45,20,0),k(.55F,0,-.08F,-.14F,0,50,-15,0),k(1,0,0,0,0,0,0,0)});

 private static int lastSlot=-1;private static float equipStart=-100F;
 private static boolean suppress=false,wasSwinging=false;private static int lastSwingTime=0;private static float animStart=-1;
 private static final Map<String,Float> ATTACK_TICKS=Map.of("butterfly_knife",8F,"karambit",9F,"xeno_hunter",9F,"hrt3_knife",8F,"banana",9F);
 /** Hits chained within 0.6 s cycle through four different swings (up to the 4th, then back to the first). */
 private static int combo;private static float lastSwingAt=-100F;
 private static float[] combo(float[] a,float s){
  float[] o=a.clone();float bump=Mth.sin(s*Mth.PI);
  switch(combo){
   case 1->{o[0]=-a[0];o[3]=-a[3];o[5]=-a[5];}                                  // backhand: same cut, mirrored
   case 2->{o[2]=a[2]*1.6F-.06F*bump;o[4]=-a[4]*.8F;o[1]=a[1]+.04F*bump;}      // reverse stab, straight forward
   case 3->{for(int i=0;i<6;i++)o[i]=a[i]*1.35F;o[5]+=45F*bump;o[1]-=.03F*bump;} // finisher: wide, rolling cut
   default->{}
  }
  return o;
 }
 private static float[] attack(String id,float s){
  var keys=ATTACK.get(id);if(keys==null||s<=0)return new float[8];
  for(int i=0;i<keys.length-1;i++){var a=keys[i];var b=keys[i+1];
   if(s>=a[0]&&s<=b[0]){float u=(s-a[0])/(b[0]-a[0]);u=u*u*(3-2*u);float[] o=new float[8];for(int n=1;n<8;n++)o[n-1]=Mth.lerp(u,a[n],b[n]);return o;}}
  return new float[8];
 }

 /** Inspection state: hand offset, knife offset from the hand, spin about the pivot (degrees), pivot in model units, orientation blend, butterfly opening (-1 = none). */
 private record Insp(float hx,float hy,float hz,float kx,float ky,float kz,float rx,float ry,float rz,float px,float py,float ori,float open,float lay,float smile,float sc){}
 private static float sm(float x){x=Mth.clamp(x,0,1);return x*x*(3-2*x);}
 private static Insp inspect(String id,Spec sp,float t){
  switch(id){
   case "banana":{ // lies flat with its curve facing up (a smile) and spins like a turntable, endlessly
    float e=sm(t/10F);return new Insp(0,.04F*e,-.06F*e,0,.27F*e,0,0,t*34F,0,Mth.lerp(e,sp.gx(),7.8F),Mth.lerp(e,sp.gy(),8.3F),0,-1,0,e,1);}
   case "karambit":{ // Valorant-style: hand forward, the karambit spins facing the camera around the ring, IN FRONT of the fist (never through it)
    float e=sm(t/12F);return new Insp(-.07F*e,-.02F*e,-.14F*e,0,0,0,0,0,-t*50F,Mth.lerp(e,sp.gx(),8F),Mth.lerp(e,sp.gy(),2F),e,-1,0,0,Mth.lerp(e,1F,.85F));}
   case "xeno_hunter":{ // looks the knife over: brings it up, shows one flat, turns it to the other, tilts it to sight down the edge, puts it back
    float up=sm(t/12F)*(1-sm((t-98F)/12F));
    float ry=-75F*sm((t-14F)/14F)+150F*sm((t-44F)/16F)-75F*sm((t-80F)/14F);
    float rx=-35F*sm((t-64F)/8F)*(1-sm((t-80F)/8F));
    float bob=.01F*Mth.sin(t*.12F);
    return new Insp(-.05F*up,.10F*up+bob*up,-.06F*up,0,0,0,rx*up,ry*up,0,sp.gx(),sp.gy(),0,-1,0,0,1);}
   case "hrt3_knife":{ // lies flat (blade horizontal) above the hand and spins like a rotor
    float e=sm(t/14F)*(1-sm((t-104F)/14F));
    return new Insp(0,.04F*e,-.10F*e,0,.14F*e,-.105F*e,0,t*55F,0,Mth.lerp(e,sp.gx(),8.3F),Mth.lerp(e,sp.gy(),8.4F),0,-1,e,0,1);}
   case "butterfly_knife":{ // smooth tricks: a slow fan around the pin, one clean close-and-open, then a small toss and catch
    float env=sm(t/8F)*(1-sm((t-100F)/10F));
    float open=1F,rz=0F,ry=0F,dy=0F;
    if(t<48F)rz=-360F*sm((t-8F)/40F);                                    // fan: one full turn around the pivot, open
    else if(t<78F){float u=(t-48F)/30F;open=1F-sm(u*2F)*(1-sm(u*2F-1F));ry=40F*Mth.sin(u*Mth.PI);}   // closes, then opens again
    else{float u=Mth.clamp((t-78F)/22F,0,1);dy=.25F*4F*u*(1-u);rz=-360F*sm(u);}                   // small aerial
    return new Insp(0,.04F*env,0,0,dy*env,-.04F*env,0,ry*env,rz*env,Mth.lerp(env,sp.gx(),8F),Mth.lerp(env,sp.gy(),7.8F),0,open,0,0,1);}
   default:return null;
  }
 }

 @SubscribeEvent public static void render(RenderHandEvent e){
  if(e.getHand()!=InteractionHand.MAIN_HAND)return;
  var mc=Minecraft.getInstance();var player=mc.player;if(player==null)return;
  String id=Knives.id(e.getItemStack());if(id==null)return;
  var spec=SPECS.get(id);if(spec==null)return;
  e.setCanceled(true);
  boolean right=player.getMainArm()==HumanoidArm.RIGHT;float side=right?1:-1;
  float partial=e.getPartialTick();
  float now=player.tickCount+partial;
  int sel=player.getInventory().selected;if(sel!=lastSlot){lastSlot=sel;equipStart=now;}
  float equip=1F-sm((now-equipStart)/7F);
  boolean swinging=player.swinging;
  if((swinging&&!wasSwinging)||(swinging&&player.swingTime<lastSwingTime)){suppress=false;combo=now-lastSwingAt<=12F&&combo<3?combo+1:0;lastSwingAt=now;animStart=now;player.playSound(MareEletrica.KNIFE_SWING.get(),.8F,.9F+player.getRandom().nextFloat()*.3F);}
  wasSwinging=swinging;lastSwingTime=player.swingTime;
  float sw=0F;
  if(animStart>=0){sw=(now-animStart)/ATTACK_TICKS.getOrDefault(id,12F);if(sw>=1F){animStart=-1;sw=0F;}}
  float tk=InspectClient.knifeTicks(partial);Insp in=tk>=0?inspect(id,spec,tk):null;
  float[] a=(in!=null||suppress)?new float[8]:combo(attack(id,sw),sw);
  float openAmount=in!=null&&in.open()>=0?in.open():1F;   // the butterfly is always open (it only shuts during the inspection tricks)
  float bob=Mth.sin((player.tickCount+partial)*.09F)*.004F;
  float hx=side*(spec.hx()+a[0]),hy=spec.hy()+a[1]-equip*.55F+bob,hz=spec.hz()+a[2];
  if(in!=null){hx+=side*in.hx();hy+=in.hy();hz+=in.hz();}
  var pose=e.getPoseStack();var buffers=e.getMultiBufferSource();int light=e.getPackedLight();
  if(!player.isInvisible()&&mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer){
   var model=renderer.getModel();var armPart=right?model.rightArm:model.leftArm;var sleeve=right?model.rightSleeve:model.leftSleeve;
   boolean showSleeve=player.isModelPartShown(right?PlayerModelPart.RIGHT_SLEEVE:PlayerModelPart.LEFT_SLEEVE);
   float ax=hx,ay=hy,az=hz;
   if(in!=null&&(id.equals("karambit")||id.equals("xeno_hunter"))){ax+=side*in.kx();ay+=in.ky();az+=in.kz();}   // the arm extends to the knife
   RifleHands.arm(pose,buffers,light,armPart,sleeve,right,showSleeve,new Vector3f(side*.42F,-.59F,.12F),new Vector3f(ax,ay,az),.50F);
  }
  float gx=in!=null?in.px():spec.gx(),gy=in!=null?in.py():spec.gy(),ob=in!=null?in.ori():0F;
  pose.pushPose();
  pose.translate(hx+(in!=null?side*in.kx():0F),hy+(in!=null?in.ky():0F),hz+(in!=null?in.kz():0F));
  pose.mulPose(Axis.YP.rotationDegrees(side*a[3]));pose.mulPose(Axis.XP.rotationDegrees(a[4]));pose.mulPose(Axis.ZP.rotationDegrees(side*a[5]));
  if(in!=null){pose.mulPose(Axis.XP.rotationDegrees(in.rx()));pose.mulPose(Axis.YP.rotationDegrees(in.ry()));pose.mulPose(Axis.ZP.rotationDegrees(in.rz()));}
  float k=1F-ob;
  float lay=in!=null?in.lay():0F,sml=in!=null?in.smile():0F;
  if(spec.style()==Style.FORWARD){pose.mulPose(Axis.XP.rotationDegrees(10*k));pose.mulPose(Axis.ZP.rotationDegrees(-90*side*k*(1F-lay)));pose.mulPose(Axis.XP.rotationDegrees(-90*k));}
  else if(spec.style()==Style.PHOTO){pose.mulPose(Axis.ZP.rotationDegrees(90*k));pose.mulPose(Axis.XP.rotationDegrees(-90*k));}
  else{pose.mulPose(Axis.ZP.rotationDegrees(123.7F*sml));pose.mulPose(Axis.XP.rotationDegrees(-15*(1F-sml)));pose.mulPose(Axis.YP.rotationDegrees(90*(1F-sml)));}
  float sc=(id.equals("karambit")?.46F:id.equals("banana")?.31F:.39F)*(in!=null?in.sc():1F);pose.scale(sc,sc,sc);
  pose.translate(-gx/16F,-gy/16F,-.5F);
  if(id.equals("butterfly_knife")){
   float shut=1F-Mth.clamp(openAmount,0F,1F);
   float bladeAngle=180F*shut,handleAngle=-180F*Math.min(1F,2F*shut);
   DmtGearLayer.model("knife_butterfly_handle_a",pose,buffers,light);
   pose.pushPose();
   pose.translate(7.05F/16F,8.7F/16F,0);pose.mulPose(Axis.ZP.rotationDegrees(bladeAngle));pose.translate(-7.05F/16F,-8.7F/16F,0);
   pose.pushPose();pose.translate(0,9.7F/16F,0);pose.scale(1F,1.22F,1F);pose.translate(0,-9.7F/16F,0);   // a longer blade
   DmtGearLayer.model("knife_butterfly_blade",pose,buffers,light);
   pose.popPose();
   pose.translate(8.95F/16F,8.7F/16F,0);pose.mulPose(Axis.ZP.rotationDegrees(handleAngle));pose.translate(-8.95F/16F,-8.7F/16F,0);
   DmtGearLayer.model("knife_butterfly_handle_b",pose,buffers,light);
   pose.popPose();
  }else DmtGearLayer.model("knife_"+id,pose,buffers,light);
  pose.popPose();
 }
}
