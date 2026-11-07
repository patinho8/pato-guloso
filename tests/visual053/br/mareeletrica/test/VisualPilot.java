package br.mareeletrica.test;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** 0.61 checks (test only, -Dmareeletrica.pilot): cockpits in first person, tiny frogs, volleyball arms, football control, ball bounces, courier drone. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualPilot {
 static int tick=-1;static int gy;static br.mareeletrica.Sports.SportBall ball;
 static boolean on(){return Boolean.getBoolean("mareeletrica.pilot");}
 static void shot(Minecraft mc,String n){Screenshot.grab(mc.gameDirectory,"pilot_"+n+".png",mc.getMainRenderTarget(),c->System.out.println("[PILOT] "+c.getString()));}
 static net.minecraft.server.level.ServerPlayer pl(net.minecraft.server.MinecraftServer s){return s.getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  var srv=mc.getSingleplayerServer();
  if(tick==40)srv.execute(()->{var l=srv.overworld();var p=pl(srv);p.setGameMode(GameType.CREATIVE);gy=l.getHeight(Heightmap.Types.MOTION_BLOCKING,0,0);
   l.setDayTime(6000);
   var h=br.mareeletrica.Vehicles.HELI.get().create(l);h.moveTo(0.5,gy+10,0.5,180,0);l.addFreshEntity(h);p.startRiding(h,true);p.setYRot(180);p.setXRot(12);});
  if(tick>=45&&tick<=60&&mc.player.getVehicle()!=null){mc.player.setYRot(mc.player.getVehicle().getYRot());mc.player.setXRot(12);}
  if(tick==118){var cam=mc.gameRenderer.getMainCamera();var v=mc.player.getVehicle();System.out.println("[PILOT] cam "+cam.getPosition()+" yaw "+cam.getYRot()+" veh "+(v==null?"none":v.position()+" yaw "+v.getYRot())+" player "+mc.player.position()+" eye "+mc.player.getEyeY());mc.options.hideGui=true;}
  if(tick==120)shot(mc,"heli");
  if(tick==121)mc.options.hideGui=false;
  if(tick==125)srv.execute(()->pl(srv).setXRot(40));
  if(tick==135)shot(mc,"heli_down");
  if(tick==140)srv.execute(()->{var l=srv.overworld();var p=pl(srv);p.stopRiding();
   var j=br.mareeletrica.Jets.JET.get().create(l);j.setPos(30.5,gy,0.5);j.setHeading(0);l.addFreshEntity(j);p.startRiding(j,true);});
  if(tick==150)srv.execute(()->pl(srv).setXRot(8));
  if(tick==190)shot(mc,"jet");
  if(tick==195)srv.execute(()->{var l=srv.overworld();var p=pl(srv);p.stopRiding();
   var b=br.mareeletrica.Jets.B2.get().create(l);b.setPos(60.5,gy,0.5);b.setHeading(0);l.addFreshEntity(b);p.startRiding(b,true);});
  if(tick==205)srv.execute(()->pl(srv).setXRot(10));
  if(tick==245)shot(mc,"b2");
  // tiny frogs
  if(tick==250)srv.execute(()->{var l=srv.overworld();var p=pl(srv);p.stopRiding();p.teleportTo(l,-20.5,gy,-20.5,0,35);
   for(int i=0;i<3;i++){var f=br.mareeletrica.Frogs.ENTITY.get().create(l);f.setVariant(i);f.moveTo(-21.2+i*.7,gy,-19.2,180+i*20,0);f.setNoAi(true);l.addFreshEntity(f);}
   var hop=br.mareeletrica.Frogs.ENTITY.get().create(l);hop.moveTo(-18,gy,-17,0,0);l.addFreshEntity(hop);});
  if(tick==262)srv.execute(()->{var l=srv.overworld();for(var f:l.getEntitiesOfClass(br.mareeletrica.Frogs.TinyFrog.class,pl(srv).getBoundingBox().inflate(6)))if(f.variant()==1)l.broadcastEntityEvent(f,(byte)70);else if(f.variant()==2)l.broadcastEntityEvent(f,(byte)71);});
  if(tick==268)shot(mc,"frogs");
  if(tick==280)srv.execute(()->{var l=srv.overworld();var f=l.getEntitiesOfClass(br.mareeletrica.Frogs.TinyFrog.class,pl(srv).getBoundingBox().inflate(8),x->!x.isNoAi()).stream().findFirst().orElse(null);
   System.out.println("[PILOT] free frog at "+(f==null?"none":f.position()));});
  if(tick==400)srv.execute(()->{var l=srv.overworld();var f=l.getEntitiesOfClass(br.mareeletrica.Frogs.TinyFrog.class,pl(srv).getBoundingBox().inflate(16),x->!x.isNoAi()).stream().findFirst().orElse(null);
   System.out.println("[PILOT] free frog after 6s at "+(f==null?"none":f.position()));});
  // volleyball: block, first person and from the front
  if(tick==410)srv.execute(()->{var l=srv.overworld();var p=pl(srv);p.teleportTo(l,-40.5,gy,-40.5,0,-10);
   p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,new net.minecraft.world.item.ItemStack(br.mareeletrica.SportsMode.ITEMS.get(br.mareeletrica.SportsMode.Mode.VOLLEY.ordinal()*3).get()));});
  if(tick==430)srv.execute(()->br.mareeletrica.SportsMode.action(pl(srv),5));
  if(tick==436)shot(mc,"volei_bloqueio_1p");
  if(tick==450)srv.execute(()->br.mareeletrica.SportsMode.action(pl(srv),0));
  if(tick==452){int[] ty={0};System.out.println("[PILOT] anim progress "+br.mareeletrica.client.SportsClient.progress(mc.player,0,ty)+" type "+ty[0]);}
  if(tick==453)shot(mc,"volei_manchete_1p");
  if(tick==470)mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
  if(tick==480)srv.execute(()->br.mareeletrica.SportsMode.action(pl(srv),5));
  if(tick==480)srv.execute(()->br.mareeletrica.SportsMode.READY.clear());
  if(tick==481)srv.execute(()->br.mareeletrica.SportsMode.action(pl(srv),5));
  if(tick==487)shot(mc,"volei_bloqueio_3p");
  if(tick==520){srv.execute(()->br.mareeletrica.SportsMode.READY.clear());srv.execute(()->br.mareeletrica.SportsMode.action(pl(srv),4));}
  if(tick==525)shot(mc,"volei_cortada_3p");
  // cooldown: spamming a special does nothing until it is ready
  if(tick==530)srv.execute(()->{var p=pl(srv);br.mareeletrica.SportsMode.READY.clear();var m=br.mareeletrica.SportsMode.Mode.VOLLEY;
   System.out.println("[PILOT] cooldown first="+br.mareeletrica.SportsMode.readyForTest(p,m,4)+" spam="+br.mareeletrica.SportsMode.readyForTest(p,m,4));});
  // football: dominate the ball and run with it
  if(tick==540){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);srv.execute(()->{var l=srv.overworld();var p=pl(srv);p.teleportTo(l,-60.5,gy,-60.5,0,20);
   p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,new net.minecraft.world.item.ItemStack(br.mareeletrica.SportsMode.ITEMS.get(br.mareeletrica.SportsMode.Mode.SOCCER.ordinal()*3).get()));
   ball=br.mareeletrica.Sports.BALL.get().create(l);ball.setItem(new net.minecraft.world.item.ItemStack(br.mareeletrica.Sports.BALLS.get(br.mareeletrica.Sports.Kind.SOCCER).get()));ball.moveTo(-60.5,gy,-59.3,0,0);l.addFreshEntity(ball);});}
  if(tick==560)srv.execute(()->{br.mareeletrica.SportsMode.READY.clear();br.mareeletrica.SportsMode.action(pl(srv),6);System.out.println("[PILOT] held="+(ball.holderId()==pl(srv).getId()));});
  if(tick>=565&&tick<600)mc.options.keyUp.setDown(true);
  if(tick==590)shot(mc,"futebol_conduzindo");
  if(tick==600){mc.options.keyUp.setDown(false);srv.execute(()->{var p=pl(srv);System.out.println("[PILOT] after run: player "+p.position()+" ball "+ball.position()+" held="+(ball.holderId()==p.getId()));});}
  if(tick==605)srv.execute(()->{br.mareeletrica.SportsMode.READY.clear();br.mareeletrica.SportsMode.action(pl(srv),4);});   // dribble keeps the ball
  if(tick==620)srv.execute(()->{var p=pl(srv);System.out.println("[PILOT] after dribble: dist "+String.format("%.2f",p.distanceTo(ball))+" held="+(ball.holderId()==p.getId()));});
  // bounce: drop a free ball from 6 blocks and count the hops
  if(tick==630)srv.execute(()->{var l=srv.overworld();var b=br.mareeletrica.Sports.BALL.get().create(l);b.setItem(new net.minecraft.world.item.ItemStack(br.mareeletrica.Sports.BALLS.get(br.mareeletrica.Sports.Kind.SOCCER).get()));b.moveTo(-70.5,gy+6,-70.5,0,0);b.setDeltaMovement(.12,0,0);l.addFreshEntity(b);ball=b;});
  if(tick>630&&tick<760&&tick%3==0)srv.execute(()->System.out.println("[PILOT] bounce "+tick+" y="+String.format("%.3f",ball.getY()-gy)+" vy="+String.format("%.3f",ball.getDeltaMovement().y)));
  // courier drone: placed on the ground, flown with the remote, picks up items below
  if(tick==770)srv.execute(()->{var l=srv.overworld();var p=pl(srv);p.teleportTo(l,-80.5,gy,-80.5,180,10);
   var d=new br.mareeletrica.Drones.DroneEntity(br.mareeletrica.Drones.DRONE_ENTITY.get(),l);d.setCourier(true);d.setPos(-80.5,gy+1.5,-82.5);d.setOwner(p.getUUID());l.addFreshEntity(d);
   for(int i=0;i<3;i++)l.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(l,-80.5+i*.4,gy,-82.5,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,3)));
   var r=new net.minecraft.world.item.ItemStack(br.mareeletrica.Drones.REMOTE.get());br.mareeletrica.Drones.DroneRemoteItem.link(r,d);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,r);});
  if(tick==800)srv.execute(()->{var p=pl(srv);p.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND);});
  if(tick==840)srv.execute(()->{var p=pl(srv);var d=br.mareeletrica.Drones.droneOf(p);if(d!=null){d.cargoAction(p);System.out.println("[PILOT] courier cargo="+d.cargoStacks());}else System.out.println("[PILOT] no courier session");});
  if(tick==860)shot(mc,"courier_hud");
  if(tick==870){System.out.println("[PILOT] DONE");mc.stop();}
 }
}
