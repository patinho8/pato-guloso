package br.mareeletrica.test;

import br.mareeletrica.FightChickens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.rooster): a rooster hit by a player circles them and never hurts them. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualRooster {
 static int tick;static FightChickens.FightChicken c;static double minD=99,maxD=0;static float angSum;static double lastA=Double.NaN;
 static final int OX=3000,OZ=-3000;
 static boolean on(){return Boolean.getBoolean("mareeletrica.rooster");}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick>=40){mc.player.setXRot(55);}
  if(tick==40)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.SURVIVAL);p.setHealth(20);});
  if(tick==140)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.SURVIVAL);});
  if(tick==150)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);System.out.println("[ROO] player at "+p.position());
   c=FightChickens.ENTITY.get().create(l);c.moveTo(p.getX()+2.5,p.getY(),p.getZ(),0,0);l.addFreshEntity(c);
   c.hurt(l.damageSources().playerAttack(p),1F);c.setTarget(p);System.out.println("[ROO] difficulty "+l.getDifficulty()+" circleNow="+c.circleTicks+" src="+c.getClass().getProtectionDomain().getCodeSource().getLocation()+" spect="+p.isSpectator()+" mode="+p.gameMode.getGameModeForPlayer());System.out.println("[ROO] hit; target after hurt = "+c.getTarget()+" noAi="+c.isNoAi()+" ko="+c.ko()+" lastHurtBy="+c.getLastHurtByMob());});
  if(tick>165&&tick<260)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);if(tick%15==0)System.out.println("[ROO] t"+tick+" circle="+c.circleTicks+" nav="+c.getNavigation().isInProgress()+" ticking="+c.level().shouldTickBlocksAt(c.blockPosition())+" age="+c.tickCount+" c="+c.position()+" p="+p.position());double d=Math.sqrt(c.distanceToSqr(p));minD=Math.min(minD,d);maxD=Math.max(maxD,d);
   double a=Math.atan2(c.getZ()-p.getZ(),c.getX()-p.getX());if(!Double.isNaN(lastA)){double da=a-lastA;while(da>Math.PI)da-=2*Math.PI;while(da<-Math.PI)da+=2*Math.PI;angSum+=da;}lastA=a;});
  if(tick==200)Screenshot.grab(mc.gameDirectory,"rooster_circle.png",mc.getMainRenderTarget(),x->{});
  if(tick==270)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);
   System.out.println("[ROO] player health "+p.getHealth()+" dist "+String.format("%.2f..%.2f",minD,maxD)+" turned "+String.format("%.0f",Math.toDegrees(angSum))+" deg, target "+c.getTarget()+" chicken at "+c.position());});
  if(tick==280){System.out.println("[ROO] DONE");mc.stop();}
 }
}
