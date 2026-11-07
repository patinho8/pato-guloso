package br.mareeletrica.client;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;

/** Flying the fighter jet: mouse = pitch and roll (stick), A/D = rudder, W/S = throttle, click/Space = wing guns, B = bomb. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class JetClient {
 private static int jetId=-1;private static float refYaw,refPitch;private static boolean bombQueued,flareQueued,ejectQueued;
 /** Hold to turn your head inside the cockpit (look at the wings, the nose, behind you) without moving the stick. */
 public static final net.minecraft.client.KeyMapping LOOK=new net.minecraft.client.KeyMapping("key.mareeletrica.jet_look",com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT,"key.categories.mareeletrica");
 private static float headYaw,headPitch;
 static boolean looking(){var mc=Minecraft.getInstance();return mc.screen==null&&LOOK.isDown();}
 public static final net.minecraft.client.KeyMapping FLARE=new net.minecraft.client.KeyMapping("key.mareeletrica.jet_flare",com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,org.lwjgl.glfw.GLFW.GLFW_KEY_G,"key.categories.mareeletrica");
 public static final net.minecraft.client.KeyMapping EJECT=new net.minecraft.client.KeyMapping("key.mareeletrica.jet_eject",com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,org.lwjgl.glfw.GLFW.GLFW_KEY_J,"key.categories.mareeletrica");
 @EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static final class Keys{@SubscribeEvent public static void keys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(FLARE);e.register(LOOK);e.register(EJECT);}}
 private JetClient(){}
 private static Jets.JetEntity piloted(){var mc=Minecraft.getInstance();return mc.player!=null&&mc.player.getVehicle() instanceof Jets.JetEntity j&&j.getControllingPassenger()==mc.player?j:null;}
 private static Jets.JetEntity riding(){var mc=Minecraft.getInstance();return mc.player!=null&&mc.player.getVehicle() instanceof Jets.JetEntity j?j:null;}
 @SubscribeEvent public static void before(ClientTickEvent.Pre e){
  var mc=Minecraft.getInstance();
  if(mc.player!=null&&mc.player.getVehicle() instanceof Parachutes.ParachuteEntity c)c.inForward=mc.screen==null&&mc.options.keyUp.isDown()?1:0;
  var j=piloted();
  if(j==null){jetId=-1;return;}
  var p=mc.player;
  if(j.getId()!=jetId){jetId=j.getId();boardedAt=p.tickCount;refYaw=p.getYRot();refPitch=p.getXRot();mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);}   // cockpit view when you get in
  float dYaw=Mth.wrapDegrees(p.getYRot()-refYaw),dPitch=p.getXRot()-refPitch;
  boolean menu=mc.screen!=null;
  if(looking()){   // free look: the mouse turns the pilot's head, the stick stays centred
   headYaw=Mth.clamp(headYaw+dYaw,-160,160);headPitch=Mth.clamp(headPitch+dPitch,-70,80);dYaw=0;dPitch=0;
  }else{headYaw*=.6F;headPitch*=.6F;if(Math.abs(headYaw)<.3F)headYaw=0;if(Math.abs(headPitch)<.3F)headPitch=0;}
  j.inRoll=menu?0:dYaw*.9F;j.inPitch=menu?0:dPitch*.75F;
  j.inYaw=menu?0:(mc.options.keyLeft.isDown()?1.6F:0)-(mc.options.keyRight.isDown()?1.6F:0);
  j.inThrottle=menu?0:(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0);
  j.inFire=!menu&&(mc.options.keyAttack.isDown()||mc.options.keyJump.isDown());
  while(DroneClient.BOMB.consumeClick())bombQueued=true;
  while(FLARE.consumeClick())flareQueued=true;
  while(EJECT.consumeClick())ejectQueued=true;
  // the mouse moved the player's head: that was stick input; put the head back on the jet's axis
  refYaw=Jets.JetEntity.yaw(j.q);refPitch=0;
  p.setYRot(refYaw);p.yRotO=refYaw;p.setXRot(0);p.xRotO=0;p.setYHeadRot(refYaw);p.yBodyRot=refYaw;
 }
 @SubscribeEvent public static void after(ClientTickEvent.Post e){
  var j=piloted();if(j==null){bombQueued=false;flareQueued=false;ejectQueued=false;return;}
  var v=j.getDeltaMovement();
  PacketDistributor.sendToServer(new JetNet.Input(j.q.x,j.q.y,j.q.z,j.q.w,j.throttle,(float)v.x,(float)v.y,(float)v.z,j.inFire,bombQueued,flareQueued,ejectQueued));
  bombQueued=false;flareQueued=false;ejectQueued=false;
 }
 /** Missile coming: blinking warning, beeps faster as it gets close. */
 public static void missileWarning(GuiGraphics g,net.minecraft.world.entity.Entity self,int cx,int cy){
  var mc=Minecraft.getInstance();
  double best=Double.MAX_VALUE;
  for(var m:mc.level.getEntitiesOfClass(Missiles.MissileEntity.class,self.getBoundingBox().inflate(200),m->m.targetId()==self.getId()))best=Math.min(best,m.distanceTo(self));
  if(best==Double.MAX_VALUE)return;
  int period=best<30?3:best<80?6:10;
  if((self.tickCount/period)%2==0){String w="MÍSSIL! "+Math.round(best)+" m  [G] despistar / manobre";g.drawString(mc.font,w,cx-mc.font.width(w)/2,cy-56,0xFFFF3030,true);}
  if(self.tickCount%period==0&&lastBeep!=self.tickCount){lastBeep=self.tickCount;mc.player.playSound(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BIT.value(),.6F,1.9F);}
 }
 private static int lastBeep;
 private static Quaternionf smooth(Jets.JetEntity j,float pt){return new Quaternionf(j.qO).slerp(j.q,pt);}
 @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
  var j=riding();if(j==null)return;
  var q=smooth(j,(float)e.getPartialTick());
  if(headYaw!=0||headPitch!=0||looking()){
   var p=Minecraft.getInstance().player;float hy=headYaw,hp=headPitch;
   if(looking()&&piloted()!=null){hy=Mth.clamp(hy+Mth.wrapDegrees(p.getYRot()-refYaw),-160,160);hp=Mth.clamp(hp+p.getXRot()-refPitch,-70,80);}   // mouse moved since the last tick
   q=new Quaternionf(q).rotateY((float)Math.toRadians(-hy)).rotateX((float)Math.toRadians(hp));
  }
  e.setYaw(Jets.JetEntity.yaw(q));e.setPitch(Jets.JetEntity.pitch(q));e.setRoll(Jets.JetEntity.roll(q));   // positive camera roll = right bank
 }
 @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered e){if(piloted()!=null){e.setCanceled(true);e.setSwingHand(false);}}
 @SubscribeEvent public static void hand(RenderHandEvent e){if(riding()!=null)e.setCanceled(true);}
 @SubscribeEvent public static void hidePilot(RenderPlayerEvent.Pre e){if(e.getEntity().getVehicle() instanceof Jets.JetEntity)e.setCanceled(true);}
 static int boardedAt;
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  var j=piloted();if(j==null)return;var mc=Minecraft.getInstance();if(mc.options.hideGui)return;
  GuiGraphics g=e.getGuiGraphics();var f=mc.font;int w=g.guiWidth(),h=g.guiHeight(),cx=w/2,cy=h/2;
  int GREEN=0xFF7CFF8A,RED=0xFFFF5050;
  if(Math.abs(headYaw)>4||Math.abs(headPitch)>4){String t="OLHANDO EM VOLTA  •  solte "+LOOK.getTranslatedKeyMessage().getString()+" para voltar";g.drawString(f,t,cx-f.width(t)/2,22,0xCCFFFFFF,true);missileWarning(g,j,cx,cy);return;}
  // gun cross and flight-path marker
  g.fill(cx-7,cy,cx-2,cy+1,GREEN);g.fill(cx+3,cy,cx+8,cy+1,GREEN);g.fill(cx,cy-7,cx+1,cy-2,GREEN);g.fill(cx,cy+3,cx+1,cy+8,GREEN);
  // horizon ladder: a bar that tilts with the bank and moves with the pitch
  float roll=Jets.JetEntity.roll(j.q),pitch=Jets.JetEntity.pitch(j.q);
  g.pose().pushPose();g.pose().translate(cx,cy,0);g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-roll));
  int off=Mth.clamp(Math.round(-pitch*2.2F),-h/2+20,h/2-20);
  g.fill(-70,off,-20,off+1,0xAA7CFF8A);g.fill(20,off,70,off+1,0xAA7CFF8A);
  g.pose().popPose();
  var v=j.getDeltaMovement();double speed=v.length()*20*3.6;
  int lx=14,ly=cy-30;
  g.drawString(f,"VEL  "+Math.round(speed)+" km/h",lx,ly,speed<40&&!j.onGround()?RED:GREEN,true);
  g.drawString(f,"ALT  "+(int)Math.floor(j.getY()),lx,ly+11,GREEN,true);
  g.drawString(f,"V/S  "+String.format("%+.0f m/s",v.y*20),lx,ly+22,GREEN,true);
  g.drawString(f,"ROLL "+Math.round(roll)+"°",lx,ly+33,GREEN,true);
  int rx=w-Math.max(96,f.width("BOMBAS 3/3")+20),ry=cy-30;
  g.drawString(f,"MOTOR "+Math.round(j.throttle*100)+"%",rx,ry,GREEN,true);
  g.fill(rx,ry+11,rx+80,ry+17,0x80000000);g.fill(rx+1,ry+12,rx+1+Math.round(78*j.throttle),ry+16,j.throttle>.9F?0xFFFFB040:GREEN);
  g.drawString(f,"BOMBAS "+j.bombs()+"/"+j.maxBombs(),rx,ry+22,j.hasBomb()?0xFFFFD050:0x88FFFFFF,true);
  if(j.downed()&&(j.tickCount/5)%2==0){String d="ABATIDO - SHIFT PARA SAIR";g.drawString(f,d,cx-f.width(d)/2,cy-40,RED,true);}
  if(!j.onGround()&&speed<40&&(j.tickCount/6)%2==0)g.drawString(f,"STALL",cx-14,cy+30,RED,true);
  missileWarning(g,j,cx,cy);
  String h1="W/S motor (S no 0 = freio)  Mouse: arfagem/rolagem  A/D leme  "+LOOK.getTranslatedKeyMessage().getString()+": olhar em volta",h2=j.bomber()?"Clique/Espaço ou B: 4 bombas  G despistador  Shift sair":"Clique/Espaço tiro  B bomba  G despistador  Shift sair";
  if(mc.player.tickCount-boardedAt<200){g.drawString(f,h1,cx-f.width(h1)/2,h-70,0xCCFFFFFF,true);g.drawString(f,h2,cx-f.width(h2)/2,h-59,0xCCFFFFFF,true);}   // controls only for the first 10 s
 }
}
