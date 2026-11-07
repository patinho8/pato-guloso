package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.Vehicles;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Driving the helicopter and the tank: keys go into the vehicle before it ticks; the trigger state goes to the server. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class VehicleClient {
 private static boolean lastFire,lastAlt;
 private VehicleClient(){}
 private static Vehicles.Base driven(){var mc=Minecraft.getInstance();return mc.player!=null&&mc.player.getVehicle() instanceof Vehicles.Base b&&b.getControllingPassenger()==mc.player?b:null;}
 @SubscribeEvent public static void before(ClientTickEvent.Pre e){
  var v=driven();if(v==null)return;var mc=Minecraft.getInstance();var o=mc.options;boolean menu=mc.screen!=null;
  v.inForward=menu?0:(o.keyUp.isDown()?1:0)-(o.keyDown.isDown()?1:0);
  v.inTurn=menu?0:(o.keyRight.isDown()?1:0)-(o.keyLeft.isDown()?1:0);
  v.inLift=menu?0:(o.keyJump.isDown()?1:0)-(o.keySprint.isDown()?1:0);
  v.inFire=!menu&&o.keyAttack.isDown();v.inAlt=!menu&&o.keyUse.isDown();
 }
 @SubscribeEvent public static void after(ClientTickEvent.Post e){
  var v=driven();
  if(v==null){lastFire=lastAlt=false;return;}
  if(v.inFire!=lastFire||v.inAlt!=lastAlt||v.tickCount%4==0)PacketDistributor.sendToServer(new Vehicles.Input(v.inFire,v.inAlt));
  lastFire=v.inFire;lastAlt=v.inAlt;
 }
 @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered e){if(driven()!=null){e.setCanceled(true);e.setSwingHand(false);}}
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  var v=driven();var mc=Minecraft.getInstance();if(v==null||mc.options.hideGui||VehicleCockpit.active())return;   // first person: the cockpit draws its own HUD
  var g=e.getGuiGraphics();var f=mc.font;int w=g.guiWidth(),h=g.guiHeight();
  boolean heli=v instanceof Vehicles.Helicopter;
  int hp=Math.round(v.health()*100/v.maxHealth());
  String a=(heli?"Helicóptero":"Tanque")+"  •  casco "+hp+"%  •  "+(heli?"foguetes ":"projéteis ")+v.ammo()+"/"+v.maxAmmo();
  if(heli)a+="  •  ALT "+(int)Math.floor(v.getY())+"  •  rotor "+Math.round(((Vehicles.Helicopter)v).rotor*100)+"%";
  else a+="  •  "+Math.round(v.moved*20*3.6)+" km/h";
  int fuel=v.fuel()*100/v.maxFuel();a+="  •  comb. "+fuel+"%";
  g.drawString(f,a,w/2-f.width(a)/2,h-72,hp<30||fuel<15?0xFFFF5050:0xFFFFE070,true);
  if(fuel==0){String nf="SEM COMBUSTÍVEL — use um galão";g.drawString(f,nf,w/2-f.width(nf)/2,h/2+26,0xFFFF4040,true);}
  String help=heli?"Espaço sobe • Ctrl desce • W/S • A/D gira • clique: metralhadora • direito: foguete":"W/S anda • A/D vira • mouse mira • clique: metralhadora • direito: canhão";
  g.drawString(f,help,w/2-f.width(help)/2,h-60,0xCCFFFFFF,true);
  if(v instanceof Vehicles.Tank t){int cd=Math.max(0,60-(v.tickCount-v.firedTick()));if(cd>0){String r="recarregando canhão "+(cd/20+1)+" s";g.drawString(f,r,w/2-f.width(r)/2,h/2+14,0xFFFFA040,true);}}
  JetClient.missileWarning(g,v,w/2,h/2);
 }
}
