package br.mareeletrica.test;

import br.mareeletrica.Drones;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.drone): fly a drone 300 blocks away, shoot it down, the pilot's body must not fall into the void. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualDrone {
 static int tick;static Drones.DroneEntity d;static double y0,minY=1e9;
 static boolean on(){return Boolean.getBoolean("mareeletrica.drone");}
 static void log(String s){System.out.println("[DRN] "+s);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.SURVIVAL);});
  if(tick==80)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);
   d=new Drones.DroneEntity(Drones.DRONE_ENTITY.get(),l);d.setPos(p.getX()+2,p.getY()+1,p.getZ());d.setOwner(p.getUUID());l.addFreshEntity(d);});
  if(tick==95)srv.execute(()->Drones.start(srv.getPlayerList().getPlayers().get(0),d));
  if(tick>=110&&tick<190){var cd=mc.level.getEntity(d.getId());if(cd!=null)cd.setPos(cd.getX()+4,-40,cd.getZ());srv.execute(()->d.setPos(d.getX()+4,-40,d.getZ()));}
  if(tick==150)log("mid flight: camera "+mc.getCameraEntity().getClass().getSimpleName()+" drone x "+d.getX());
  if(tick==200){y0=mc.player.getY();log("before crash: player y "+y0+" chunk loaded "+mc.level.getChunkSource().hasChunk(mc.player.getBlockX()>>4,mc.player.getBlockZ()>>4)+" camera "+mc.getCameraEntity());}
  if(tick==201)srv.execute(()->d.hurt(d.damageSources().generic(),100F));
  if(tick>201&&tick<420)minY=Math.min(minY,mc.player.getY());
  if(tick%20==0&&tick>=120&&tick<=420)log("t"+tick+" cam="+mc.getCameraEntity().getClass().getSimpleName()+" homeChunk="+mc.level.getChunkSource().hasChunk(mc.player.getBlockX()>>4,mc.player.getBlockZ()>>4)+" loadedChunks="+mc.level.getChunkSource().getLoadedChunksCount()+" serverViewing="+Drones.viewing(srv.getPlayerList().getPlayers().get(0))+" y="+mc.player.getY());
  if(tick==420){log("after crash: start y "+y0+" lowest "+minY+" now "+mc.player.getY()+" camera "+mc.getCameraEntity().getClass().getSimpleName()+" alive "+mc.player.isAlive());log("DONE");mc.stop();}
 }
}
