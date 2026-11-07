package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.pato): ducks (colours, hats, King, duckling, nest, swimming), duck on the head, duck race, the new wave. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualPato {
 static int tick,rs=-1;static BlockPos plat;static float fwd,left,jump;
 static void log(String s){System.out.println("[PATO] "+s);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"pato_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void tp(double x,double y,double z,float yaw,float pitch){var p=sp();p.stopRiding();p.setDeltaMovement(0,0,0);p.teleportTo(p.serverLevel(),x,y,z,yaw,pitch);}
 static Pato.Duck duck(double x,double y,double z,int color,int hat,int fed,boolean tame){var l=sp().serverLevel();var d=Pato.ENTITY.get().create(l);d.moveTo(x,y,z,180,0);d.setLook(color,hat,fed);
  if(tame){d.tame(sp());d.setMode(Pato.SIT);}l.addFreshEntity(d);return d;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.pato"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60){Skate.input=()->new float[]{fwd,left,jump,0};srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);plat=p.blockPosition().atY(150);
   l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);
   for(int x=-12;x<=12;x++)for(int z=-6;z<=24;z++){l.setBlockAndUpdate(plat.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(plat.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   for(int x=4;x<=8;x++)for(int z=-4;z<=-1;z++)l.setBlockAndUpdate(plat.offset(x,-1,z),Blocks.WATER.defaultBlockState());
   l.setBlockAndUpdate(plat.offset(-4,0,-2),Pato.NEST.get().defaultBlockState());
   // line-up: 5 colours with the 6 hats, the King, a duckling, a sleeper on the nest, a swimmer
   for(int i=0;i<5;i++)duck(plat.getX()-3.5+i*1.4,plat.getY(),plat.getZ()+.5,i,i+1,0,true);
   duck(plat.getX()+3.6,plat.getY(),plat.getZ()+.5,0,6,0,true);
   duck(plat.getX()-1,plat.getY(),plat.getZ()-2.5,3,0,45,true);
   var baby=duck(plat.getX()+1.5,plat.getY(),plat.getZ()-2,0,0,0,true);baby.setAge(-24000);
   var sl=duck(plat.getX()-3.5,plat.getY()+.25,plat.getZ()-1.5,1,0,0,true);sl.setSleeping(true);
   duck(plat.getX()+6.5,plat.getY()-.4,plat.getZ()-2.5,2,4,0,false);
   tp(plat.getX()+.5,plat.getY(),plat.getZ()+5.5,180,22);});}
  if(tick>=62&&tick<300&&tick%10==0)srv.execute(()->{var p=sp();for(var en:p.serverLevel().getEntities(p,p.getBoundingBox().inflate(60),x->x instanceof net.minecraft.world.entity.animal.Animal&&!(x instanceof Pato.Duck)||x instanceof net.minecraft.world.entity.item.ItemEntity))en.discard();});
  if(tick==90)shot("fila");
  if(tick==92)srv.execute(()->tp(plat.getX()+.5,plat.getY()+1.6,plat.getZ()+2.6,200,40));
  if(tick==100)shot("perto");
  // duck on the head
  if(tick==104)srv.execute(()->{var p=sp();tp(plat.getX()+.5,plat.getY(),plat.getZ()+8.5,0,0);var d=duck(p.getX(),p.getY(),p.getZ(),3,1,10,true);d.startRiding(p,true);});
  if(tick==110){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);}
  if(tick==122)shot("cabeca");
  if(tick==124){mc.options.setCameraType(CameraType.FIRST_PERSON);srv.execute(()->{for(var p2:sp().getPassengers())p2.stopRiding();});}
  // duck race
  if(tick==130)srv.execute(()->{var p=sp();var l=p.serverLevel();var at=plat.offset(-10,0,2);l.setBlockAndUpdate(at,Pato.RACE.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,Direction.SOUTH));
   tp(at.getX()+.5,at.getY(),at.getZ()-1.5,0,30);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Casino.CHIP.get(),10));
   var be=l.getBlockEntity(at);log("race be="+be);
   l.getBlockState(at).useItemOn(p.getMainHandItem(),l,p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false));});
  if(tick==140)srv.execute(()->tp(plat.getX()-9.5,plat.getY()+3.5,plat.getZ()+.5,0,32));
  if(tick==250)shot("corrida");
  if(tick==300)srv.execute(()->log("chips after race: "+Casino.chips(sp())));
  // the wave
  if(tick==310)srv.execute(()->{var p=sp();var l=p.serverLevel();var o=plat.offset(0,0,60);
   for(int x=-14;x<=14;x++)for(int z=-2;z<=50;z++){boolean wall=Math.abs(x)==14||z==-2||z==50;l.setBlockAndUpdate(o.offset(x,-4,z),Blocks.SMOOTH_STONE.defaultBlockState());
    for(int y=-3;y<=-1;y++)l.setBlockAndUpdate(o.offset(x,y,z),wall?Blocks.SMOOTH_SANDSTONE.defaultBlockState():Blocks.WATER.defaultBlockState());for(int y=0;y<6;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   var b=Surf.BOARD.get().create(l);b.moveTo(o.getX()+.5,o.getY()-.2,o.getZ()+12.5,0,0);l.addFreshEntity(b);tp(o.getX()+.5,o.getY(),o.getZ()+12.5,0,15);p.startRiding(b);
   var d=duck(o.getX()+1.5,o.getY(),o.getZ()+12,4,4,0,true);d.setMode(Pato.FOLLOW);});
  if(tick==316)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  if(tick==320){fwd=1;mc.player.setYRot(0);}
  if(tick==322)srv.execute(()->{var o=plat.offset(0,0,60);double s=Surf.surface(sp().level(),o.getX()+.5,o.getY()-1,o.getZ()+8.5);Surf.Wave.spawn(sp().serverLevel(),new Vec3(o.getX()+.5,s,o.getZ()+7.5),0F,16F,2.1F);});
  var bb=mc.player.getVehicle() instanceof Surf.Board b0?b0:null;
  if(rs<0&&bb!=null&&bb.state==1&&tick>322){rs=tick;log("CAUGHT t"+tick);}
  if(tick>322&&tick<470&&tick%4==0&&bb!=null)log(String.format("surf t%d z=%.1f x=%.1f y=%.2f st=%d",tick,bb.getZ()-plat.getZ()-60,bb.getX()-plat.getX(),bb.getY()-plat.getY(),bb.state));
  if(rs>0){int k=tick-rs;if(k==4){left=-1;fwd=0;}if(k==20)left=0;if(k==22)mc.player.setYRot(-120);if(k==28)shot("onda_lado");if(k==30)mc.player.setYRot(-20);if(k==36)shot("onda_tras");if(k==38){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);mc.player.setYRot(0);mc.player.setXRot(70);}if(k==44)shot("onda_cima");}
  if(tick==330)shot("onda_chegando");
  if(tick==480)System.out.println("[PATO] DONE");
 }
}
