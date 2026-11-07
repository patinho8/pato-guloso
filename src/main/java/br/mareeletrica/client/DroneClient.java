package br.mareeletrica.client;

import br.mareeletrica.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Flying the drone: the camera becomes the drone, WASD/Space/Shift fly, Ctrl is turbo, B drops a bomb, X leaves. Drone-camera HUD. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class DroneClient {
 public static final KeyMapping BOMB=new KeyMapping("key.mareeletrica.drone_bomb",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_B,"key.categories.mareeletrica");
 public static final KeyMapping EXIT=new KeyMapping("key.mareeletrica.drone_exit",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_X,"key.categories.mareeletrica");
 private static int droneId=-1;private static CameraType previous;
 private static float forward,strafe;private static boolean up,down;private static float roll,rollO,lastYaw;private static int dropFlash;

 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
 public static final class Keys{@SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(BOMB);e.register(EXIT);}}

 /** While you fly, the server only sends the land around the drone and your game forgets the land under your body.
  *  When the flight ends (exit or the drone is shot down) that land takes a moment to come back: until it does,
  *  hold the body still where it was instead of letting it fall through the missing ground into the void. */
 private static net.minecraft.world.phys.Vec3 home;private static int settle;
 private static void holdUntilGround(Minecraft mc){
  if(settle<=0||mc.player==null||mc.level==null)return;settle--;
  var p=mc.player;var at=home!=null?home:p.position();int cx=net.minecraft.util.Mth.floor(at.x)>>4,cz=net.minecraft.util.Mth.floor(at.z)>>4;
  boolean ready=true;for(int dx=-1;dx<=1&&ready;dx++)for(int dz=-1;dz<=1;dz++)if(!mc.level.getChunkSource().hasChunk(cx+dx,cz+dz)){ready=false;break;}
  if(ready){settle=0;return;}
  if(home!=null)p.setPos(home.x,home.y,home.z);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.fallDistance=0;
 }
 public static boolean flying(){return droneId>=0;}
 private static Drones.DroneEntity drone(){var mc=Minecraft.getInstance();return droneId>=0&&mc.level!=null&&mc.level.getEntity(droneId) instanceof Drones.DroneEntity d?d:null;}
 public static void session(int id){
  var mc=Minecraft.getInstance();
  if(id<0){
   Drones.DroneEntity.localPilotedId=-1;
   if(droneId>=0&&mc.player!=null){mc.setCameraEntity(mc.player);if(previous!=null)mc.options.setCameraType(previous);settle=1200;}
   droneId=-1;return;
  }
  if(mc.level==null)return;
  var e=mc.level.getEntity(id);if(e==null){droneId=-1;return;}
  if(mc.player!=null)home=mc.player.position();settle=0;
  droneId=id;previous=mc.options.getCameraType();mc.options.setCameraType(CameraType.FIRST_PERSON);mc.setCameraEntity(e);
 }

 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();
  if(droneId<0){holdUntilGround(mc);return;}
  if(mc.level==null||mc.player==null){droneId=-1;return;}
  var d=drone();
  if(d==null||d.isRemoved()){PacketDistributor.sendToServer(new DroneNet.Action(2));session(-1);return;}   // lost the drone: tell the server so it sends our own land back
  boolean boost=mc.options.keySprint.isDown();
  // feed our own prediction (applied by the drone's client tick) and report where it is
  Drones.DroneEntity.localPilotedId=d.getId();
  d.inForward=forward;d.inStrafe=strafe;d.inUp=up;d.inDown=down;d.inBoost=boost;d.inYaw=mc.player.getYRot();d.inPitch=mc.player.getXRot();
  PacketDistributor.sendToServer(new DroneNet.Input(forward,strafe,up,down,boost,mc.player.getYRot(),mc.player.getXRot(),d.getX(),d.getY(),d.getZ()));
  while(BOMB.consumeClick()){PacketDistributor.sendToServer(new DroneNet.Action(1));if(d.hasBomb()&&!d.courier())dropFlash=10;}
  while(EXIT.consumeClick())PacketDistributor.sendToServer(new DroneNet.Action(2));
  if(dropFlash>0)dropFlash--;
  // camera bank from sideways speed
  double yaw=Math.toRadians(mc.player.getYRot());double vx=d.getX()-d.xo,vz=d.getZ()-d.zo;
  float side=(float)(vx*-Math.cos(yaw)+vz*-Math.sin(yaw));
  rollO=roll;float turn=Mth.wrapDegrees(mc.player.getYRot()-lastYaw);lastYaw=mc.player.getYRot();
  roll+=(Mth.clamp(side*140F+turn*2.2F,-32F,32F)-roll)*.18F;   // banks toward the side it moves / turns to
 }
 /** While flying, the keys fly the drone and the player's own body stays still. */
 @SubscribeEvent public static void move(MovementInputUpdateEvent e){
  if(droneId<0)return;
  var in=e.getInput();
  forward=in.forwardImpulse;strafe=in.leftImpulse;up=in.jumping;down=in.shiftKeyDown;
  in.forwardImpulse=0;in.leftImpulse=0;in.jumping=false;in.shiftKeyDown=false;in.up=false;in.down=false;in.left=false;in.right=false;
 }
 /** Smooth camera: the view follows the mouse every frame instead of waiting for the server. */
 @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
  if(droneId<0)return;
  var mc=Minecraft.getInstance();if(mc.player==null)return;
  float pt=(float)e.getPartialTick();
  e.setYaw(mc.player.getViewYRot(pt));e.setPitch(mc.player.getViewXRot(pt));e.setRoll(Mth.lerp(pt,rollO,roll));
 }
 /** Looking through the drone camera: no arm or held item in the picture. */
 @SubscribeEvent public static void noHand(net.neoforged.neoforge.client.event.RenderHandEvent e){if(droneId>=0)e.setCanceled(true);}
 @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered e){if(droneId>=0){e.setCanceled(true);e.setSwingHand(false);}}
 /** Hide hotbar, hearts, crosshair... while looking through the drone. */
 @SubscribeEvent public static void hideVanilla(RenderGuiLayerEvent.Pre e){if(droneId>=0&&e.getName().getNamespace().equals("minecraft"))e.setCanceled(true);}

 private static final int GREEN=0xFF7CFFB0,DIM=0x997CFFB0,RED=0xFFFF5050,WHITE=0xFFFFFFFF;
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  var d=drone();if(d==null)return;
  var mc=Minecraft.getInstance();var g=e.getGuiGraphics();var f=mc.font;
  int w=g.guiWidth(),h=g.guiHeight(),cx=w/2,cy=h/2;
  JetClient.missileWarning(g,d,cx,cy);
  // vignette + subtle scanlines
  g.fillGradient(0,0,w,h/5,0x70000000,0x00000000);g.fillGradient(0,h-h/5,w,h,0x00000000,0x70000000);
  for(int y=0;y<h;y+=3)g.fill(0,y,w,y+1,0x0C000000);
  // frame corners
  int bw=w/3,bh=h/3;
  corner(g,cx-bw,cy-bh,1,1);corner(g,cx+bw,cy-bh,-1,1);corner(g,cx-bw,cy+bh,1,-1);corner(g,cx+bw,cy+bh,-1,-1);
  // reticle
  g.fill(cx-9,cy,cx-3,cy+1,GREEN);g.fill(cx+4,cy,cx+10,cy+1,GREEN);g.fill(cx,cy-9,cx+1,cy-3,GREEN);g.fill(cx,cy+4,cx+1,cy+10,GREEN);g.fill(cx,cy,cx+1,cy+1,RED);
  // bomb drop line: straight down marker under the reticle
  if(!d.courier()&&d.hasBomb())for(int y=cy+14;y<cy+40;y+=4)g.fill(cx,y,cx+1,y+2,dropFlash>0?RED:DIM);
  // compass
  float heading=Mth.wrapDegrees(mc.player.getYRot()+180F);if(heading<0)heading+=360;
  int top=10;g.fill(cx-92,top+10,cx+92,top+11,DIM);
  for(int deg=0;deg<360;deg+=15){
   float delta=Mth.wrapDegrees(deg-heading);if(Math.abs(delta)>60)continue;
   int x=cx+(int)(delta*1.5F);boolean major=deg%45==0;
   g.fill(x,top+(major?4:7),x+1,top+10,major?GREEN:DIM);
   if(deg%90==0){String l=deg==0?"N":deg==90?"L":deg==180?"S":"O";g.drawString(f,l,x-f.width(l)/2,top-6,deg==0?RED:GREEN,true);}
   else if(deg%45==0){String l=String.valueOf(deg);g.drawString(f,l,x-f.width(l)/2,top-6,DIM,false);}
  }
  g.fill(cx,top+11,cx+1,top+15,WHITE);
  String hd=String.format("%03d°",Math.round(heading)%360);g.drawString(f,hd,cx-f.width(hd)/2,top+16,WHITE,true);
  // left column: altitude, height above ground, speed, distance
  int ground=heightAboveGround(d);
  double speed=Math.sqrt(Math.pow(d.getX()-d.xo,2)+Math.pow(d.getY()-d.yo,2)+Math.pow(d.getZ()-d.zo,2))*20*3.6;
  double dist=mc.player.distanceTo(d);
  int lx=12,ly=cy-34;
  g.drawString(f,"ALT  "+(int)Math.floor(d.getY()),lx,ly,GREEN,true);
  g.drawString(f,"SOLO "+(ground<0?"--":ground+" m"),lx,ly+11,GREEN,true);
  g.drawString(f,"VEL  "+Math.round(speed)+" km/h",lx,ly+22,GREEN,true);
  g.drawString(f,"DIST "+Math.round(dist)+" m",lx,ly+33,GREEN,true);
  if(mc.options.keySprint.isDown())g.drawString(f,"TURBO",lx,ly+48,0xFFFFD050,true);
  // right column: battery, signal, bombs
  int rx=w-92,ry=cy-34;
  int pct=d.battery()*100/Drones.BATTERY_MAX;boolean low=pct<20;
  g.drawString(f,"BATERIA "+pct+"%",rx,ry,low&&(d.tickCount/8)%2==0?RED:GREEN,true);
  g.fill(rx,ry+11,rx+70,ry+17,0x80000000);g.fill(rx+1,ry+12,rx+1+68*pct/100,ry+16,low?RED:GREEN);
  int bars=dist<150?4:dist<400?3:dist<1000?2:1;
  g.drawString(f,"SINAL",rx,ry+22,GREEN,true);
  for(int i=0;i<4;i++)g.fill(rx+36+i*6,ry+30-(i+1)*2,rx+40+i*6,ry+30,i<bars?(bars<=1?RED:GREEN):0x55FFFFFF);
  if(d.courier()){g.drawString(f,"CARGA",rx,ry+36,GREEN,true);
   for(int i=0;i<Drones.DroneEntity.CARGO_MAX;i++){int bx=rx+42+(i%5)*7,by=ry+36+(i/5)*6;g.fill(bx,by,bx+5,by+4,i<d.cargoStacks()?0xFFC08040:0x55FFFFFF);}}
  else{g.drawString(f,"BOMBAS",rx,ry+36,GREEN,true);
  for(int i=0;i<Drones.MAX_BOMBS;i++){int bx=rx+42+(i%5)*7,by=ry+36+(i/5)*6;g.fill(bx,by,bx+5,by+4,i<d.bombs()?0xFFE0C040:0x55FFFFFF);}}
  if(d.hatch>.05F)g.drawString(f,"ESCOTILHA ABERTA",w/2-40,cy+46,0xFFFFD050,true);
  // fall time of a bomb from here
  if(d.hasBomb()&&ground>0)g.drawString(f,"QUEDA "+String.format("%.1f s",fallTime(ground)),rx,ry+50+6,DIM,true);
  // REC
  if((d.tickCount/10)%2==0)g.fill(12,12,18,18,RED);
  g.drawString(f,"REC",22,11,WHITE,true);
  if(d.battery()<=0)g.drawCenteredString(f,"SEM BATERIA — POUSANDO",cx,cy+40,RED);
  // help line
  String help=d.courier()?"WASD voar  Espaço/Shift subir/descer  Ctrl turbo  [B] "+(d.cargoStacks()>0?"entregar/soltar carga":"pegar carga embaixo")+"  [X] sair"
   :"WASD voar  Espaço/Shift subir/descer  Ctrl turbo  [B] bomba  [X] sair";
  g.drawString(f,help,cx-f.width(help)/2,h-14,0xCCFFFFFF,true);
 }
 private static void corner(GuiGraphics g,int x,int y,int sx,int sy){
  int len=14;
  g.fill(Math.min(x,x+sx*len),y,Math.max(x,x+sx*len),y+1,GREEN);
  g.fill(x,Math.min(y,y+sy*len),x+1,Math.max(y,y+sy*len),GREEN);
 }
 private static int heightAboveGround(Drones.DroneEntity d){
  var level=d.level();var p=BlockPos.containing(d.getX(),d.getY(),d.getZ()).mutable();
  for(int i=0;i<96;i++){p.move(0,-1,0);if(!level.getBlockState(p).getCollisionShape(level,p).isEmpty())return i;}
  return -1;
 }
 /** Same gravity and drag as the bomb entity. */
 private static double fallTime(int height){double y=0,v=0;int t=0;while(y<height+.3&&t<400){v=(v+.07)*.99;y+=v;t++;}return t/20.0;}
}
