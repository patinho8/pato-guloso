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

/** Test only (-Dmareeletrica.skate): ride the skateboard, push, kickflip, grind a rail, roll down a slope. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualSkate {
 static int tick,t0=-1;static BlockPos plat;static float fwd,left,jump;static boolean sawFlip,sawGrind;static float maxSpeed,maxAir;
 static boolean on(){return Boolean.getBoolean("mareeletrica.skate");}
 static void log(String s){System.out.println("[SKATE] "+s);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60){Skate.input=()->new float[]{fwd,left,jump};mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
   srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
   plat=p.blockPosition().above(40);
   for(int x=-6;x<=6;x++)for(int z=-4;z<=70;z++){l.setBlockAndUpdate(plat.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<5;y++)l.setBlockAndUpdate(plat.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   for(int z=22;z<=30;z++)l.setBlockAndUpdate(plat.offset(1,0,z),Blocks.RAIL.defaultBlockState());          // grind rail
   for(int z=40;z<=70;z++){int drop=(z-40)/3;for(int y=-1;y>=-1-drop;y--)l.setBlockAndUpdate(plat.offset(0,y,z),Blocks.AIR.defaultBlockState());
    for(int x=-6;x<=6;x++){for(int y=-1;y>-1-drop;y--)l.setBlockAndUpdate(plat.offset(x,y,z),Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(plat.offset(x,-2-drop,z),Blocks.SMOOTH_STONE.defaultBlockState());}}   // stairs down = slope
   var b=new Skate.Board(Skate.ENTITY.get(),l);b.moveTo(plat.getX()+.5,plat.getY(),plat.getZ()+.5,0,0);l.addFreshEntity(b);
   p.teleportTo(plat.getX()+.5,plat.getY(),plat.getZ()+.5);p.startRiding(b);log("riding="+(p.getVehicle()==b));});}
  if(tick==80)t0=tick;
  if(t0<0)return;int k=tick-t0;
  var board=mc.player.getVehicle() instanceof Skate.Board b?b:null;
  fwd=k<30?1:k>=70&&k<90?1:k>=120&&k<140?1:0;left=k>=40&&k<52?1:0;jump=k==40||k==41?1:0;
  if(board!=null){maxSpeed=Math.max(maxSpeed,board.speed);maxAir=Math.max(maxAir,board.air);if(board.flip>0)sawFlip=true;if(board.grinding)sawGrind=true;
   if(k%10==0||k>=80&&k<=92)log("k"+k+" x="+String.format("%.2f",board.getX()-plat.getX())+" z="+String.format("%.1f",board.getZ()-plat.getZ())+" y="+String.format("%.2f",board.getY()-plat.getY())+" speed="+String.format("%.3f",board.speed)+" air="+board.air+" flip="+board.flip+" grind="+board.grinding+" ground="+board.onGround());}
  else if(k%10==0||k<95){log("k"+k+" not riding: player at "+mc.player.position().subtract(plat.getX(),plat.getY(),plat.getZ())+" hp="+mc.player.getHealth()+" dead="+mc.player.isDeadOrDying());if(k<95)srv.execute(()->{var bs=sp().serverLevel().getEntitiesOfClass(Skate.Board.class,new net.minecraft.world.phys.AABB(plat).inflate(80));log("  server boards: "+bs.stream().map(x->String.format("(%.1f,%.1f,%.1f) removed=%s",x.getX()-plat.getX(),x.getY()-plat.getY(),x.getZ()-plat.getZ(),x.isRemoved())).toList()+" player vehicle="+sp().getVehicle());});}
  boolean side=k>=24&&k<=28||k>=86&&k<=92;   // look sideways (where the body faces) so the front camera shows the whole pose
  if(side&&board!=null){mc.player.setYRot(board.getYRot()+90);mc.player.setXRot(10);}else if(board!=null&&k<160){mc.player.setYRot(board.getYRot());mc.player.setXRot(20);}
  mc.options.setCameraType(side||k>=15&&k<=25||k>=42&&k<=48||k>=64&&k<=80?CameraType.THIRD_PERSON_FRONT:k>=160?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_BACK);
  if(k==27)Screenshot.grab(mc.gameDirectory,"skate_side_push.png",mc.getMainRenderTarget(),c->{});
  if(k==91)Screenshot.grab(mc.gameDirectory,"skate_side_coast.png",mc.getMainRenderTarget(),c->{});
  if(k==150)srv.execute(()->{var l=sp().serverLevel();var b2=new Skate.Board(Skate.ENTITY.get(),l);b2.moveTo(plat.getX()-2.5,plat.getY(),plat.getZ()+5.5,30,0);l.addFreshEntity(b2);
   sp().stopRiding();sp().teleportTo(l,plat.getX()-1.2,plat.getY(),plat.getZ()+4.3,125,52);});
  if(k==175)Screenshot.grab(mc.gameDirectory,"skate_board.png",mc.getMainRenderTarget(),c->{});
  if(k==20){Screenshot.grab(mc.gameDirectory,"skate_ride.png",mc.getMainRenderTarget(),c->{});if(board!=null)log("feet y - deck y = "+String.format("%.3f",mc.player.getY()-(board.getY()+Skate.Board.DECK)));}
  if(k==47)Screenshot.grab(mc.gameDirectory,"skate_flip_front.png",mc.getMainRenderTarget(),c->{});
  if(k==70)Screenshot.grab(mc.gameDirectory,"skate_grind_front.png",mc.getMainRenderTarget(),c->{});
  if(k==100)Screenshot.grab(mc.gameDirectory,"skate_coast.png",mc.getMainRenderTarget(),c->{});
  if(k==12||k==44||k==70)log("anim flags k"+k+" = "+(board==null?-1:board.anim())+" server="+(board==null?-1:board.getEntityData().get(Skate.Board.ANIM)));
  if(k==45)Screenshot.grab(mc.gameDirectory,"skate_flip.png",mc.getMainRenderTarget(),c->{});
  if(board!=null&&board.grinding&&!sawGrindShot){sawGrindShot=true;Screenshot.grab(mc.gameDirectory,"skate_grind.png",mc.getMainRenderTarget(),c->{});}
  if(k==60)srv.execute(()->{var sb=sp().getVehicle();log("server board z="+(sb==null?"none":String.format("%.1f",sb.getZ()-plat.getZ())));});
  if(k==100)return;if(k==180){log("summary: maxSpeed="+maxSpeed+" maxAir="+maxAir+" kickflip seen="+sawFlip+" grind seen="+sawGrind);log("DONE");}
 }
 static boolean sawGrindShot;
 @SubscribeEvent public static void mount(net.neoforged.neoforge.event.entity.EntityMountEvent e){
  if(!on()||!e.isDismounting()||!(e.getEntityBeingMounted() instanceof Skate.Board))return;
  log("DISMOUNT on "+(e.getLevel().isClientSide?"client":"server")+" at "+e.getEntityMounting().position());
  var st=Thread.currentThread().getStackTrace();for(int i=2;i<Math.min(st.length,26);i++)log("   at "+st[i]);}
}
