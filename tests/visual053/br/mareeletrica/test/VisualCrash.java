package br.mareeletrica.test;

import br.mareeletrica.Jets;
import br.mareeletrica.Vehicles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.crash): a shot-down jet spirals down and crashes; a knocked-out tank burns, can't move, and blows up at 3 min. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualCrash {
 static int tick;static Jets.JetEntity jet;static Vehicles.Tank tank;static double jetY0;static Vec3 tank0;
 static boolean on(){return Boolean.getBoolean("mareeletrica.crash");}
 static void log(String s){System.out.println("[CRS] "+s);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying()){mc.player.respawn();}
  if(tick>=60){mc.player.setYRot(0);mc.player.setXRot(-25);}
  if(tick==60)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);var l=p.serverLevel();
   p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
   jet=Jets.JET.get().create(l);jet.moveTo(p.getX(),p.getY()+40,p.getZ()+45,0,0);jet.setDeltaMovement(0,0,1.2);l.addFreshEntity(jet);jetY0=jet.getY();
   tank=Vehicles.TANK.get().create(l);tank.moveTo(p.getX()+6,-60,p.getZ()+10,0,0);l.addFreshEntity(tank);tank0=tank.position();});
  if(tick==70)srv.execute(()->{var l=srv.overworld();jet.hurt(l.damageSources().source(DamageTypes.ARROW),9999F);log("jet downed="+jet.downed()+" y="+jet.getY());
   tank.hurt(l.damageSources().source(DamageTypes.ARROW),9999F);log("tank wrecked="+tank.wrecked()+" alive="+tank.isAlive()+" health="+tank.health());});
  if(tick==90||tick==110)Screenshot.grab(mc.gameDirectory,"crash_"+tick+".png",mc.getMainRenderTarget(),c->{});
  if(tick%20==0&&tick>70&&tick<300)srv.execute(()->log("t"+tick+" jet alive="+jet.isAlive()+" y="+String.format("%.1f",jet.getY())+" | tank moved "+String.format("%.2f",tank.position().distanceTo(tank0))+" pos "+tank.position()+" vel "+tank.getDeltaMovement()+" removed "+tank.getRemovalReason()+" wreck "+tank.wrecked()));
  if(tick==140)srv.execute(()->tank.hurt(srv.overworld().damageSources().source(DamageTypes.ARROW),50F));   // more hits do nothing
  if(tick==160)srv.execute(()->{var t=new net.minecraft.nbt.CompoundTag();tank.saveWithoutId(t);t.putInt("Wreck",Vehicles.Base.WRECK_TICKS-15);tank.load(t);log("fast-forward wreck timer; tank still alive="+tank.isAlive());});
  if(tick==170)Screenshot.grab(mc.gameDirectory,"crash_tank.png",mc.getMainRenderTarget(),c->{});
  if(tick==200)srv.execute(()->log("after 3 min: tank alive="+tank.isAlive()));
  if(tick==310){log("DONE");mc.stop();}
 }
}
