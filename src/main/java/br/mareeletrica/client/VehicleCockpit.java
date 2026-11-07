package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.Vehicles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** First-person cockpit for the helicopter (canopy frame, green helmet HUD, instrument panel, banking camera) and a gunner
 *  sight for the tank. Third person keeps the plain text HUD. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class VehicleCockpit {
 private static float bank,bankO;
 private VehicleCockpit(){}
 static Vehicles.Base inside(){var mc=Minecraft.getInstance();return mc.player!=null&&mc.player.getVehicle() instanceof Vehicles.Base v&&mc.options.getCameraType().isFirstPerson()?v:null;}
 public static boolean active(){return inside()!=null;}
 /** F5 in a plane, helicopter or tank: the camera pulls far back so you see the whole machine and what is around it. */
 @SubscribeEvent public static void distance(net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent e){
  var mc=Minecraft.getInstance();if(mc.player==null)return;var v=mc.player.getVehicle();
  float d=v instanceof br.mareeletrica.Jets.JetEntity j?(j.bomber()?20F:13F):v instanceof Vehicles.Helicopter?14F:v instanceof Vehicles.Tank?11F:0F;
  if(d>0)e.setDistance(Math.max(e.getDistance(),d));
 }
 @SubscribeEvent public static void hand(RenderHandEvent e){if(inside()!=null)e.setCanceled(true);}
 /** In the helicopter, the tank and the jet only their own sight is shown, not the normal crosshair. */
 @SubscribeEvent public static void crosshair(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre e){
  if(!e.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.CROSSHAIR))return;
  var mc=Minecraft.getInstance();
  if(inside()!=null||mc.player!=null&&mc.player.getVehicle() instanceof br.mareeletrica.Jets.JetEntity&&mc.options.getCameraType().isFirstPerson())e.setCanceled(true);
 }
 /** The helicopter banks into turns and dips its nose when flying forward: the view follows a little. */
 @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){
  if(!(inside() instanceof Vehicles.Helicopter h))return;
  float pt=(float)e.getPartialTick();
  e.setRoll(e.getRoll()+Mth.lerp(pt,bankO,bank));
  e.setPitch(e.getPitch()+Mth.clamp((float)h.moved*6F,0,4F));
 }
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){
  bankO=bank;var v=inside();float target=0;
  if(v instanceof Vehicles.Helicopter h&&!h.onGround())target=h.inTurn*9F*h.rotor;
  bank+=(target-bank)*.12F;
 }

 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  var v=inside();var mc=Minecraft.getInstance();if(v==null||mc.options.hideGui)return;
  var g=e.getGuiGraphics();
  if(v instanceof Vehicles.Helicopter h)heli(g,h,mc,e.getPartialTick().getGameTimeDeltaPartialTick(false));
  else tank(g,v,mc);
 }

 static final int GREEN=0xFF6CFF7A,GREEN_DIM=0x996CFF7A,RED=0xFFFF4A4A,AMBER=0xFFFFB040,FRAME=0xFF1C1E21,FRAME_LIT=0xFF34383D;
 static void line(GuiGraphics g,float x0,float y0,float x1,float y1,int w,int c){
  // thick line from small squares (GuiGraphics has no line primitive)
  int n=(int)Math.max(Math.abs(x1-x0),Math.abs(y1-y0));for(int i=0;i<=n;i++){float t=n==0?0:i/(float)n;int x=Math.round(Mth.lerp(t,x0,x1)),y=Math.round(Mth.lerp(t,y0,y1));g.fill(x-w/2,y-w/2,x-w/2+w,y-w/2+w,c);}
 }
 static void ring(GuiGraphics g,int cx,int cy,int r,int c){for(int a=0;a<360;a+=4){double t=Math.toRadians(a);g.fill(cx+(int)Math.round(Math.cos(t)*r),cy+(int)Math.round(Math.sin(t)*r),cx+(int)Math.round(Math.cos(t)*r)+1,cy+(int)Math.round(Math.sin(t)*r)+1,c);}}
 static String range(Minecraft mc){
  var p=mc.player;var eye=p.getEyePosition();var hit=mc.level.clip(new ClipContext(eye,eye.add(p.getLookAngle().scale(300)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
  return hit.getType()==HitResult.Type.MISS?"---":String.valueOf(Math.round(hit.getLocation().distanceTo(eye)));
 }
 static void bar(GuiGraphics g,int x,int y,int w,float f,int c,String label,String value,net.minecraft.client.gui.Font font){
  g.drawString(font,label,x,y,GREEN_DIM,false);g.drawString(font,value,x+w-font.width(value),y,c,false);
  g.fill(x,y+10,x+w,y+14,0x80000000);g.fill(x,y+10,x+Math.round(w*Mth.clamp(f,0,1)),y+14,c);
 }

 static void heli(GuiGraphics g,Vehicles.Helicopter h,Minecraft mc,float pt){
  var f=mc.font;int w=g.guiWidth(),hh=g.guiHeight(),cx=w/2,cy=hh/2;
  // the cabin itself (posts, dashboard, nose bubble) is now drawn in 3D: only the readout strip stays at the bottom
  int panelTop=hh-62;
  g.fill(0,panelTop,w,hh,0xC01C1E21);g.fill(0,panelTop,w,panelTop+2,FRAME_LIT);
  // ---- helmet HUD (green)
  // gun pipper and rocket reticle
  ring(g,cx,cy,9,GREEN);g.fill(cx,cy,cx+1,cy+1,GREEN);
  g.fill(cx-22,cy,cx-14,cy+1,GREEN);g.fill(cx+15,cy,cx+23,cy+1,GREEN);
  String rg="DIST "+range(mc)+" m";g.drawString(f,rg,cx+14,cy+8,GREEN_DIM,false);
  // heading tape
  float yaw=Mth.wrapDegrees(mc.player.getYRot());
  int tapeY=8;g.fill(cx-90,tapeY+10,cx+90,tapeY+11,GREEN_DIM);
  for(int d=-60;d<=60;d+=5){int deg=Math.floorMod(Math.round(yaw/5)*5+d,360);float off=(Math.round(yaw/5)*5+d-yaw)*1.5F;if(Math.abs(off)>90)continue;int x=cx+Math.round(off);
   g.fill(x,tapeY+(deg%15==0?4:7),x+1,tapeY+10,GREEN);
   if(deg%45==0){String n=switch(deg){case 0->"S";case 90->"O";case 180->"N";case 270->"L";default->String.valueOf(deg);};g.drawString(f,n,x-f.width(n)/2,tapeY-5,GREEN,false);}}
  g.fill(cx,tapeY+11,cx+1,tapeY+16,GREEN);
  // artificial horizon: tilts with the bank
  g.pose().pushPose();g.pose().translate(cx,cy,0);g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-Mth.lerp(pt,bankO,bank)));
  int off=Math.round(mc.player.getXRot()*2.2F);
  g.fill(-80,off,-34,off+1,GREEN_DIM);g.fill(34,off,80,off+1,GREEN_DIM);g.fill(-80,off,-79,off+4,GREEN_DIM);g.fill(79,off,80,off+4,GREEN_DIM);
  g.pose().popPose();
  // speed (left) and altitude (right) boxes
  var vel=h.getDeltaMovement();int kmh=(int)Math.round(Math.sqrt(vel.x*vel.x+vel.z*vel.z)*20*3.6);
  int ground=mc.level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,h.getBlockX(),h.getBlockZ());
  int agl=(int)Math.floor(h.getY()-ground);double vs=vel.y*20;
  int bx=cx-120,by=cy-8;g.fill(bx-2,by-3,bx+44,by+11,0x60000000);g.drawString(f,kmh+" km/h",bx,by,GREEN,false);g.drawString(f,"VEL",bx,by-12,GREEN_DIM,false);
  int ax=cx+80;g.fill(ax-2,by-3,ax+46,by+11,0x60000000);g.drawString(f,"ALT "+(int)Math.floor(h.getY()),ax,by,GREEN,false);
  g.drawString(f,"SOLO "+agl+" m",ax,by+14,agl<6&&vs<-3?RED:GREEN_DIM,false);g.drawString(f,String.format("V/S %+.0f",vs),ax,by+25,GREEN_DIM,false);
  // warnings
  boolean blink=(h.tickCount/5)%2==0;
  if(agl<10&&vs<-6&&blink){String s="PUXE PRA CIMA!";g.drawString(f,s,cx-f.width(s)/2,cy-40,RED,true);}
  if(h.rotor<.95F&&!h.onGround()&&blink){String s="ROTOR "+Math.round(h.rotor*100)+"%";g.drawString(f,s,cx-f.width(s)/2,cy+30,AMBER,true);}
  JetClient.missileWarning(g,h,cx,cy);
  // ---- instrument panel
  int px=10,py=panelTop+8,col=(w-20)/4;
  int hp=Math.round(h.health()*100/h.maxHealth()),fuel=h.fuel()*100/h.maxFuel();
  bar(g,px,py,col-14,hp/100F,hp<30?RED:GREEN,"CASCO",hp+"%",f);
  bar(g,px+col,py,col-14,fuel/100F,fuel<15?RED:fuel<35?AMBER:GREEN,"COMB.",fuel+"%",f);
  bar(g,px+col*2,py,col-14,h.rotor,h.rotor<.9F?AMBER:GREEN,"ROTOR",Math.round(h.rotor*100)+"%",f);
  bar(g,px+col*3,py,col-14,h.ammo()/(float)Math.max(1,h.maxAmmo()),h.ammo()==0?RED:GREEN,"FOGUETES",h.ammo()+"/"+h.maxAmmo(),f);
  if(h.tickCount<20*12||mc.player.isShiftKeyDown()){String help="Espaço/Ctrl sobe e desce • W/S • A/D gira • clique: metralhadora • direito: foguete";
   g.drawString(f,help,cx-f.width(help)/2,panelTop-12,0xCCFFFFFF,true);}
  if(h.fuel()==0&&blink){String nf="SEM COMBUSTÍVEL";g.drawString(f,nf,cx-f.width(nf)/2,cy+44,RED,true);}
 }

 static void tank(GuiGraphics g,Vehicles.Base v,Minecraft mc){
  var f=mc.font;int w=g.guiWidth(),hh=g.guiHeight(),cx=w/2,cy=hh/2;
  // gunner sight: dark vignette ring, stadia marks, range
  int r=Math.min(w,hh)/2-10;
  for(int y=0;y<hh;y+=2){int dy=y-cy;int half=dy*dy<r*r?(int)Math.sqrt(r*r-dy*dy):0;g.fill(0,y,cx-half,y+2,0xD0000000);g.fill(cx+half,y,w,y+2,0xD0000000);}
  ring(g,cx,cy,r,0xFF202020);
  g.fill(cx-r+10,cy,cx-12,cy+1,GREEN);g.fill(cx+12,cy,cx+r-10,cy+1,GREEN);g.fill(cx,cy+12,cx+1,cy+r-10,GREEN);
  for(int i=1;i<=4;i++){g.fill(cx-6,cy+12+i*12,cx+7,cy+13+i*12,GREEN_DIM);g.drawString(f,String.valueOf(i*100),cx+10,cy+8+i*12,GREEN_DIM,false);}
  g.fill(cx-1,cy-1,cx+2,cy+2,RED);
  g.drawString(f,"DIST "+range(mc)+" m",cx+r/2,cy-14,GREEN,false);
  // turret position on the hull (the turret turns only 90 degrees each way)
  if(v instanceof Vehicles.Tank t){
   float rel=Mth.wrapDegrees(t.turretYaw()-t.getYRot());int ix=w-70,iy=hh-60;
   g.fill(ix-14,iy-20,ix+14,iy+20,0x80303830);g.fill(ix-15,iy-21,ix+15,iy-20,GREEN_DIM);
   double a=Math.toRadians(rel);line(g,ix,iy,ix+(float)Math.sin(-a)*-22,iy-(float)Math.cos(a)*22,2,Math.abs(rel)>85?AMBER:GREEN);
   g.drawString(f,"TORRE "+Math.round(rel)+"°",ix-24,iy+26,Math.abs(rel)>85?AMBER:GREEN,false);
   int cd=Math.max(0,60-(v.tickCount-v.firedTick()));
   g.drawString(f,cd>0?"CARREGANDO "+(cd/20+1)+"s":"CANHÃO PRONTO",cx-r/2-40,cy-14,cd>0?AMBER:GREEN,false);
  }
  int hp=Math.round(v.health()*100/v.maxHealth()),fuel=v.fuel()*100/v.maxFuel();
  String st="CASCO "+hp+"%   COMB. "+fuel+"%   PROJÉTEIS "+v.ammo()+"/"+v.maxAmmo()+"   "+Math.round(v.moved*20*3.6)+" km/h";
  g.drawString(f,st,cx-f.width(st)/2,12,hp<30||fuel<15?RED:GREEN,true);
  JetClient.missileWarning(g,v,cx,cy);
 }
}
