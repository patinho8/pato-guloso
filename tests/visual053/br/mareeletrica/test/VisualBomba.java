package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.bomba): builds the Bomb-mode map, plays a 5v5 with bots, logs rounds, screenshots. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualBomba {
 static int tick;static BlockPos term;
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"bomba_"+n+".png",mc.getMainRenderTarget(),x->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static BombMode.Terminal t(){return sp().level().getBlockEntity(term) instanceof BombMode.Terminal x?x:null;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.bomba"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(tick==50&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60&&!Boolean.getBoolean("mareeletrica.bomba.close"))srv(()->{var p=sp();var l=p.serverLevel();l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);p.setInvulnerable(true);
   long t0=System.currentTimeMillis();term=BombMode.build(l,new BlockPos(3000,120,3000));System.out.println("[BOMBA] built in "+(System.currentTimeMillis()-t0)+" ms, terminal "+term);
   p.teleportTo(l,term.getX()+2.5,term.getY(),term.getZ()+.5,0,0);});
  if(tick==60&&Boolean.getBoolean("mareeletrica.bomba.close"))srv(()->{var p=sp();term=new BlockPos(3026,120,3054);p.serverLevel().setDayTime(6000);p.setInvulnerable(true);p.teleportTo(p.serverLevel(),term.getX()+2.5,term.getY(),term.getZ()+.5,0,0);});
  if(tick==185&&Boolean.getBoolean("mareeletrica.bomba.close"))srv(()->{var x=t();if(x==null)return;var bs=x.bots();for(var b:bs)if(!b.ct()){sp().setGameMode(net.minecraft.world.level.GameType.SPECTATOR);sp().teleportTo(sp().serverLevel(),b.getX()+2.2,b.getEyeY()+.3,b.getZ()-2.2,45,8);break;}});
  if(tick==205&&Boolean.getBoolean("mareeletrica.bomba.close")){shot("perto");}
  if(tick==170&&Boolean.getBoolean("mareeletrica.bomba.close")){shot("kit");srv(()->{var p=sp();for(int i=0;i<9;i++){var it=p.getInventory().getItem(i);if(!it.isEmpty())System.out.println("[BOMBA] slot "+i+" "+it.getHoverName().getString()+" rounds "+Magazines.rounds(it));}});}
  if(tick==120)srv(()->{var x=t();if(x!=null){x.start(sp());System.out.println("[BOMBA] started");}});
  if(tick==140)shot("congelado");
  if(tick==150){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);}
  if(tick==160)shot("terceira");
  if(tick==170)mc.options.setCameraType(CameraType.FIRST_PERSON);
  if(tick%100==0&&tick>120)srv(()->{var x=t();if(x==null)return;var bots=x.bots();int ct=0,tr=0;for(var b:bots)if(b.ct())ct++;else tr++;
   System.out.println("[BOMBA] t"+tick+" running "+x.running()+" bomb "+x.bombPos()+" botsCT "+ct+" botsTR "+tr+" player "+sp().gameMode.getGameModeForPlayer()+" pos "+sp().blockPosition());});
  // watch the fight from above (spectator) for a while
  if(tick==400)srv(()->{var p=sp();p.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);p.teleportTo(p.serverLevel(),3040,175,3030,0,90);});
  if(tick==430)shot("mapa_cima");
  if(tick==520)srv(()->{var p=sp();p.teleportTo(p.serverLevel(),3040,135,3075,180,35);});
  if(tick==540)shot("vista_ct");
  if(tick==700)shot("luta1");
  if(tick==900)srv(()->{var x=t();if(x!=null&&x.bombPos()!=null){var b=x.bombPos();sp().teleportTo(sp().serverLevel(),b.getX()+4,b.getY()+4,b.getZ()+4,135,35);}});
  if(tick==920)shot("bomba_site");
  if(tick==1300)shot("luta2");
  if(tick==3400){System.out.println("[BOMBA] DONE");}
 }
}
