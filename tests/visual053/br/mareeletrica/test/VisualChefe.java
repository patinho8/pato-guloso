package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.chefe): the Giant Evil Duck and its attacks. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualChefe {
 static int tick;static BlockPos o;static DuckBoss.EvilDuck boss;
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"chefe_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static void look(){srv(()->{var p=sp();if(boss==null)return;var d=boss.position().add(0,2.2,0).subtract(p.getEyePosition());p.teleportTo(p.serverLevel(),p.getX(),p.getY(),p.getZ(),(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90,(float)(-Math.atan2(d.y,Math.hypot(d.x,d.z))*180/Math.PI));});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.chefe"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);o=p.blockPosition().atY(150);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);
   for(int x=-20;x<=20;x++)for(int z=-20;z<=20;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<8;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,20000,4));p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,20000,4));
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+14.5,180,0);
   boss=DuckBoss.BOSS.get().create(l);boss.moveTo(o.getX()+.5,o.getY(),o.getZ()+.5,0,0);l.addFreshEntity(boss);boss.setNoAi(true);});
  if(tick>=62&&tick<300&&tick%10==0)look();
  if(tick==90)shot("frente");
  if(tick==92)srv(()->{boss.setNoAi(false);boss.setTarget(sp());boss.force(DuckBoss.EvilDuck.QUACK);});
  if(tick==112)shot("quack_carga");
  if(tick==122)shot("quack");
  if(tick==140)srv(()->{var p=sp();p.teleportTo(p.serverLevel(),o.getX()+.5,o.getY(),o.getZ()+16.5,180,0);boss.force(DuckBoss.EvilDuck.EGGS);});
  if(tick==170)shot("ovos");
  if(tick==185)shot("ovos2");
  if(tick==230)srv(()->boss.setHealth(150));
  if(tick==240)srv(()->{var p=sp();p.teleportTo(p.serverLevel(),o.getX()+.5,o.getY(),o.getZ()+12.5,180,0);boss.force(DuckBoss.EvilDuck.CHARGE);});
  if(tick==250)shot("furia");
  if(tick==262)shot("investida");
  if(tick==300)srv(()->boss.kill());
  if(tick==340){srv(()->{var p=sp();p.teleportTo(p.serverLevel(),boss.getX(),boss.getY()+4,boss.getZ()+4,180,50);});}
  if(tick==352)shot("drops");
  if(tick==360)System.out.println("[CHEFE] DONE");
 }
}
