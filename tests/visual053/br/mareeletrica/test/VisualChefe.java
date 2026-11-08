package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
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

/** Test only (-Dmareeletrica.chefe): the reworked duck boss fight, then a dark room full of LED lamps. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualChefe {
 static int tick;static BlockPos o;static DuckBoss.EvilDuck boss;
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"chefe_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static void cam(double x,double y,double z){srv(()->{var p=sp();if(boss==null)return;var d=boss.position().add(0,2,0).subtract(new net.minecraft.world.phys.Vec3(x,y+1.6,z));p.teleportTo(p.serverLevel(),x,y,z,(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90,(float)(-Math.atan2(d.y,Math.hypot(d.x,d.z))*180/Math.PI));});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.chefe"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);o=p.blockPosition().atY(150);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);
   for(int x=-24;x<=24;x++)for(int z=-24;z<=24;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<12;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,20000,4));p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,20000,4));
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+14.5,180,0);
   boss=DuckBoss.BOSS.get().create(l);boss.moveTo(o.getX()+.5,o.getY(),o.getZ()+.5,0,0);l.addFreshEntity(boss);boss.setNoAi(true);});
  if(tick==90){mc.options.setCameraType(CameraType.FIRST_PERSON);cam(o.getX()+.5,o.getY(),o.getZ()+16.5);}
  if(tick==96)shot("frente");
  // each attack, watched from the side
  if(tick==100)srv(()->{boss.setNoAi(false);boss.setTarget(sp());boss.force(DuckBoss.EvilDuck.FEATHERS);});
  if(tick==112)cam(o.getX()+14.5,o.getY()+2,o.getZ()+8.5);
  if(tick==118)shot("penas");
  if(tick==140)srv(()->{boss.moveTo(o.getX()+.5,o.getY(),o.getZ()+.5);boss.force(DuckBoss.EvilDuck.EGGS);});
  if(tick==150)cam(o.getX()+16.5,o.getY()+3,o.getZ()+10.5);
  if(tick==166)shot("ovos");
  if(tick==200)srv(()->{boss.moveTo(o.getX()+.5,o.getY(),o.getZ()+.5);boss.force(DuckBoss.EvilDuck.QUACK);});
  if(tick==201)cam(o.getX()+12.5,o.getY()+5,o.getZ()+14.5);
  if(tick==234)shot("onda");
  if(tick==242)shot("onda2");
  if(tick==260)srv(()->{boss.moveTo(o.getX()+.5,o.getY(),o.getZ()+.5);boss.force(DuckBoss.EvilDuck.DIVE);});
  if(tick==261)cam(o.getX()+15.5,o.getY()+4,o.getZ()+16.5);
  if(tick==282)shot("mergulho");
  if(tick==298)shot("mergulho2");
  if(tick==320)srv(()->{boss.setHealth(150);});
  if(tick==330)cam(o.getX()+10.5,o.getY()+3,o.getZ()+10.5);
  if(tick==352)shot("patinhos");
  // let it fight on its own for a while (it should circle and keep its distance)
  if(tick==360)srv(()->{var p=sp();p.teleportTo(p.serverLevel(),o.getX()+.5,o.getY(),o.getZ()+8.5,180,10);});
  if(tick==420)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  if(tick==430)shot("luta");
  if(tick==470)shot("luta2");
  if(tick==480){mc.options.setCameraType(CameraType.FIRST_PERSON);srv(()->{boss.discard();for(var d:sp().serverLevel().getEntitiesOfClass(DuckBoss.Duckling.class,sp().getBoundingBox().inflate(60)))d.discard();});}
  // LED room at night
  if(tick==490)srv(()->{var p=sp();var l=p.serverLevel();l.setDayTime(18000);var r=o.offset(40,0,0);
   for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){l.setBlockAndUpdate(r.offset(x,-1,z),Blocks.SMOOTH_QUARTZ.defaultBlockState());l.setBlockAndUpdate(r.offset(x,5,z),Blocks.SMOOTH_QUARTZ.defaultBlockState());
    for(int y=0;y<5;y++)l.setBlockAndUpdate(r.offset(x,y,z),(Math.abs(x)==6||Math.abs(z)==6)?Blocks.WHITE_CONCRETE.defaultBlockState():Blocks.AIR.defaultBlockState());}
   int[][] at={{-3,4,-3},{-2,4,-3},{-1,4,-3},{0,4,-3},{1,4,-3},{2,4,-3},{3,4,-3},{-5,0,-5},{5,0,-5},{-5,1,-5},{0,0,-5},{1,0,-5},{-5,0,2},{5,0,2},{5,1,2},{0,4,2}};
   for(int i=0;i<at.length;i++)l.setBlockAndUpdate(r.offset(at[i][0],at[i][1],at[i][2]),Lamps.BLOCKS.get((i+1)%Lamps.BLOCKS.size()).get().defaultBlockState());
   p.teleportTo(l,r.getX()+.5,r.getY(),r.getZ()+5.0,180,-5);});
  if(tick==520)shot("led");
  if(tick==540)srv(()->{var p=sp();p.teleportTo(p.serverLevel(),p.getX()-3,p.getY(),p.getZ(),150,-15);});
  if(tick==556)shot("led2");
  if(tick==560)System.out.println("[CHEFE] DONE");
 }
}
