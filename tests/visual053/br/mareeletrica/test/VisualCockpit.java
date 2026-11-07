package br.mareeletrica.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Visual check (test only, -Dmareeletrica.cockpit): sits in the helicopter and the tank and screenshots the first-person view. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualCockpit {
 static int tick=-1;
 static boolean on(){return Boolean.getBoolean("mareeletrica.cockpit");}
 static void shot(Minecraft mc,String n){Screenshot.grab(mc.gameDirectory,"cockpit_"+n+".png",mc.getMainRenderTarget(),c->System.out.println("[COCKPIT] "+c.getString()));}
 static void sport(net.minecraft.server.MinecraftServer srv,br.mareeletrica.SportsMode.Mode m,br.mareeletrica.Sports.Kind k){srv.execute(()->{
  var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.stopRiding();
  int gy=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,(int)p.getX()-12,(int)p.getZ());p.teleportTo(p.getX()-12,gy,p.getZ());p.setYRot(180);p.setXRot(20);
  p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,new net.minecraft.world.item.ItemStack(br.mareeletrica.SportsMode.ITEMS.get(m.ordinal()*3).get()));
  var b=br.mareeletrica.Sports.BALL.get().create(l);b.setItem(new net.minecraft.world.item.ItemStack(br.mareeletrica.Sports.BALLS.get(k).get()));
  var f=p.getLookAngle().multiply(1,0,1).normalize();b.moveTo(p.getX()+f.x*1.2,gy+(k==br.mareeletrica.Sports.Kind.VOLLEY?1.6:0),p.getZ()+f.z*1.2,0,0);l.addFreshEntity(b);});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  var srv=mc.getSingleplayerServer();
  if(tick==60)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.CREATIVE);
   var h=br.mareeletrica.Vehicles.HELI.get().create(l);h.moveTo(p.getX(),p.getY()+12,p.getZ(),180,0);l.addFreshEntity(h);p.startRiding(h,true);
   p.setYRot(180);p.setXRot(15);});
  if(tick==200)shot(mc,"heli");
  if(tick==210)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.stopRiding();
   var t=br.mareeletrica.Vehicles.TANK.get().create(l);int gy=l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,(int)p.getX()+8,(int)p.getZ());t.moveTo(p.getX()+8,gy,p.getZ(),180,0);l.addFreshEntity(t);p.startRiding(t,true);p.setYRot(180);p.setXRot(5);});
  if(tick==330)shot(mc,"tank");
  if(tick==340)sport(srv,br.mareeletrica.SportsMode.Mode.VOLLEY,br.mareeletrica.Sports.Kind.VOLLEY);
  if(tick==370)srv.execute(()->br.mareeletrica.SportsMode.action(srv.getPlayerList().getPlayers().get(0),3));
  if(tick==380)shot(mc,"volei");
  if(tick==400)sport(srv,br.mareeletrica.SportsMode.Mode.SOCCER,br.mareeletrica.Sports.Kind.SOCCER);
  if(tick==430)srv.execute(()->br.mareeletrica.SportsMode.action(srv.getPlayerList().getPlayers().get(0),3));
  if(tick==442)shot(mc,"futebol");
  if(tick==460){System.out.println("[COCKPIT] DONE");mc.stop();}
 }
}
