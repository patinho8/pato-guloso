package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.ombro): two ducks on the shoulders, gliding down a fall, hopping off. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualOmbro {
 static int tick;static BlockPos o;static java.util.List<Pato.Duck> ducks=new java.util.ArrayList<>();static float hp;
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"ombro_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.ombro"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(tick==50&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setInvulnerable(false);p.setHealth(20);o=p.blockPosition().atY(200).offset(40,0,0);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);
   l.getServer().setDifficulty(net.minecraft.world.Difficulty.PEACEFUL,true);
   for(var m:l.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new net.minecraft.world.phys.AABB(o).inflate(80)))m.discard();
   for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<30;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+.5,0,10);
   for(int i=0;i<2;i++){var d=Pato.ENTITY.get().create(l);d.moveTo(o.getX()+1+i,o.getY(),o.getZ()+1,0,0);d.tame(p);d.setMode(Pato.FOLLOW);d.setLook(i,i==0?2:0,0);d.addXp(i==0?240:90);l.addFreshEntity(d);ducks.add(d);}});
  if(tick==80)srv(()->{for(var d:ducks)d.startRiding(sp(),true);});
  if(tick==85){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.options.hideGui=true;mc.gui.getChat().clearMessages(false);}
  if(tick==120){shot("frente");srv(()->{for(var d:ducks)System.out.println("[OMBRO] duck on "+d.getVehicle()+" at "+d.position()+" player "+sp().position());});}
  if(tick==122)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  if(tick==150)shot("costas");
  if(tick==152)mc.options.keyShift.setDown(true);
  if(tick==175)shot("agachado");
  if(tick==177){mc.options.keyShift.setDown(false);mc.options.setCameraType(CameraType.FIRST_PERSON);}
  if(tick==200)shot("primeira");
  if(tick==205){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);srv(()->{var p=sp();hp=p.getHealth();p.teleportTo(p.serverLevel(),o.getX()+.5,o.getY()+22,o.getZ()+.5,0,30);});}
  if(tick==225)shot("planando");
  if(tick==330){srv(()->{var p=sp();System.out.println("[OMBRO] fall: health "+hp+" -> "+p.getHealth()+" onGround "+p.onGround()+" ducks riding "+Pato.Duck.shoulderDucks(p).size());for(var d:ducks)d.hopOff(p);});}
  if(tick==350){shot("desceu");mc.options.hideGui=false;srv(()->System.out.println("[OMBRO] after hop off: "+Pato.Duck.shoulderDucks(sp()).size()+" riding"));}
  if(tick==355){System.out.println("[OMBRO] DONE");srv(()->sp().serverLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true));}
 }
}
