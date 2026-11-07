package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.fun067): minigolf kit + ball physics, ping-pong serve/hit, surfing a machine wave. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualFun067 {
 static int tick;static BlockPos plat;static float fwd,left,jump;static Games.GolfBall g1,g2;static PingPong.Ball pb;static Surf.Board board;static int rs=-1;
 static boolean on(){return Boolean.getBoolean("mareeletrica.fun067");}
 static void log(String s){System.out.println("[F67] "+s);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,n+".png",mc.getMainRenderTarget(),c->{});}
 static void tp(double x,double y,double z,float yaw,float pitch){var p=sp();p.stopRiding();p.setDeltaMovement(0,0,0);p.teleportTo(p.serverLevel(),x,y,z,yaw,pitch);}
 static Object field(Object o,String n){try{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}catch(Exception e){return "?";}}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60){Skate.input=()->new float[]{fwd,left,jump,0};
   srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);plat=p.blockPosition().atY(150);log("plat "+plat);
    l.setDayTime(6000);
    FieldKits.buildAt(l,plat,Direction.SOUTH,FieldKits.Kind.MINIGOLFE);
    tp(plat.getX()+.5,plat.getY()+9,plat.getZ()-5,0,42);});}
  if(tick>=62&&tick%10==0)srv.execute(()->{var p=sp();
   for(var en:p.serverLevel().getEntities(p,p.getBoundingBox().inflate(60),x->x instanceof net.minecraft.world.entity.animal.Animal||x instanceof net.minecraft.world.entity.item.ItemEntity))en.discard();});
  if(tick==80)shot("f67_minigolf");
  // golf balls: lane 2 (ramp) and lane 1 (windmill), putted straight down the lane
  if(tick==85)srv.execute(()->{var l=sp().serverLevel();
   g1=Games.GOLF_BALL.get().create(l);g1.setPos(plat.getX()+.5,plat.getY()+1.05,plat.getZ()+1.5);g1.setDeltaMovement(0,0,.6);l.addFreshEntity(g1);
   g2=Games.GOLF_BALL.get().create(l);g2.setPos(plat.getX()+5.5,plat.getY()+1.05,plat.getZ()+1.5);g2.setDeltaMovement(0,0,.55);l.addFreshEntity(g2);});
  if(tick>85&&tick<200&&(tick<104||tick%5==0))srv.execute(()->log(String.format("golf t%d ramp z=%.2f y=%.2f %s | mill z=%.2f y=%.2f %s",tick,g1.getZ()-plat.getZ(),g1.getY()-plat.getY(),g1.isRemoved()?"IN":"",g2.getZ()-plat.getZ(),g2.getY()-plat.getY(),g2.isRemoved()?"IN":"")));
  if(tick==92){srv.execute(()->tp(plat.getX()-3.5,plat.getY()+3.5,plat.getZ()+8.5,-90,28));}
  if(tick==100)shot("f67_windmill");if(tick==102)srv.execute(()->tp(plat.getX()+.5,plat.getY()+4,plat.getZ()+1.5,0,30));if(tick==110)shot("f67_ramp");
  // ---- ping-pong ----
  if(tick==200)srv.execute(()->{var p=sp();var l=p.serverLevel();var o=plat.offset(-30,0,0);
   for(int x=-4;x<=4;x++)for(int z=-4;z<=8;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.SPRUCE_PLANKS.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   tp(o.getX()+.5,o.getY(),o.getZ()-1.5,0,30);
   p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PingPong.TABLE_ITEM.get()));
   var ctx=new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(o.below()).add(0,.5,0),Direction.UP,o.below(),false));
   log("table place: "+p.getMainHandItem().useOn(ctx));
   p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PingPong.PADDLE.get()));});
  if(tick==205)srv.execute(()->{var p=sp();tp(p.getX()-.5,p.getY(),p.getZ(),0,35);});
  if(tick==210)srv.execute(()->{var p=sp();log("serve: "+p.getMainHandItem().use(p.level(),p,InteractionHand.MAIN_HAND).getResult());
   pb=p.level().getEntitiesOfClass(PingPong.Ball.class,p.getBoundingBox().inflate(4)).stream().findFirst().orElse(null);log("ball="+pb);});
  if(tick==217)srv.execute(()->{var p=sp();p.setXRot(35);log("hit: "+p.getMainHandItem().use(p.level(),p,InteractionHand.MAIN_HAND).getResult());});
  if(tick>217&&tick<250&&pb!=null)srv.execute(()->{var o=plat.offset(-30,0,0);log(String.format("pp t%d x=%.2f y=%.2f z=%.2f",tick,pb.getX()-o.getX(),pb.getY()-o.getY(),pb.getZ()-o.getZ()));});
  if(tick==221){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);}
  if(tick==224)shot("f67_pingpong");
  if(tick==250)srv.execute(()->{var o=plat.offset(-30,0,0);var t=PingPong.nearest(sp().level(),Vec3.atCenterOf(o),6);log("table status: "+(t==null?"none":t.status()));});
  if(tick==255){mc.options.setCameraType(CameraType.FIRST_PERSON);srv.execute(()->{var o=plat.offset(-30,0,0);tp(o.getX()+3.5,o.getY()+1.5,o.getZ()+5.5,135,25);});}
  if(tick==265)shot("f67_table");
  // ---- surf: a pool 20 x 50, 3 deep, wave machine on the north end ----
  if(tick==270)srv.execute(()->{var p=sp();var l=p.serverLevel();var o=plat.offset(0,0,40);
   for(int x=-11;x<=11;x++)for(int z=-2;z<=52;z++){boolean wall=Math.abs(x)==11||z==-2||z==52;
    l.setBlockAndUpdate(o.offset(x,-4,z),Blocks.SMOOTH_STONE.defaultBlockState());
    for(int y=-3;y<=-1;y++)l.setBlockAndUpdate(o.offset(x,y,z),wall?Blocks.SMOOTH_SANDSTONE.defaultBlockState():Blocks.WATER.defaultBlockState());
    for(int y=0;y<6;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   l.setBlockAndUpdate(o.offset(0,-1,-2),Surf.MACHINE.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,Direction.SOUTH));
   board=Surf.BOARD.get().create(l);board.moveTo(o.getX()+.5,o.getY()-.2,o.getZ()+12.5,0,0);l.addFreshEntity(board);
   tp(o.getX()+.5,o.getY(),o.getZ()+12.5,0,15);p.startRiding(board);});
  if(tick==275)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  if(tick==300)shot("f67_surf_lying");
  if(tick==302){fwd=1;mc.player.setYRot(0);}
  if(tick==312)shot("f67_surf_paddle");
  if(tick==305)srv.execute(()->{var o=plat.offset(0,0,40);double s=Surf.surface(sp().level(),o.getX()+.5,o.getY()-1,o.getZ()+8.5);Surf.Wave.spawn(sp().serverLevel(),new Vec3(o.getX()+.5,s,o.getZ()+7.5),0F,14F,1.8F);});
  var bb0=mc.player.getVehicle() instanceof Surf.Board bx?bx:null;
  if(rs<0&&bb0!=null&&bb0.state==1&&tick>305){rs=tick;log("CAUGHT at t"+tick);}
  if(tick>305&&tick<460&&tick%3==0){if(bb0!=null)log(String.format("surf t%d z=%.2f x=%.2f y=%.2f state=%d speed=%.3f ride=%.3f yaw=%.0f",tick,bb0.getZ()-plat.getZ()-40,bb0.getX()-plat.getX(),bb0.getY()-plat.getY(),bb0.state,bb0.speed,bb0.ride,bb0.getYRot()));else log("surf t"+tick+" not riding");}
  if(rs>0){int k=tick-rs;
   if(k==5){mc.player.setYRot(-60);}
   if(k==8)shot("f67_surf_ride_side");
   if(k==10){left=1;fwd=0;}
   if(k==26){left=0;}
   if(k==31)shot("f67_surf_carve");
   if(k==34)jump=1;
   if(k==35)jump=0;
   if(k==40)shot("f67_surf_air");
   if(k==48)shot("f67_surf_after");if(k==50){left=-1;}if(k==86)left=0;}
  if(tick==470)System.out.println("[F67] DONE");
 }
}
