package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.big066): the 0.66.0 batch — jet spin, helicopter fall, cockpit, football goal / keeper / curve,
 *  basketball hoop, 2-block arcade, bowling alley kit (hook + pin reset), golf course kit, pool colours, skatepark. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualBig {
 static int tick;static BlockPos plat;static Jets.JetEntity jet;static Vehicles.Helicopter heli;static Sports.SportBall ball;static Games.BowlingBall bowl;static Skate.Board board;
 static float fwd,left,jump,sprint;static int t0;static Vec3 curveStart;static double maxSide;
 static boolean on(){return Boolean.getBoolean("mareeletrica.big066");}
 static void log(String s){System.out.println("[BIG] "+s);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,n+".png",mc.getMainRenderTarget(),c->{});}
 static void tp(double x,double y,double z,float yaw,float pitch){var p=sp();p.stopRiding();p.setDeltaMovement(0,0,0);p.teleportTo(p.serverLevel(),x,y,z,yaw,pitch);}
 static void floor(int x0,int z0,int x1,int z1){var l=sp().serverLevel();for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){l.setBlockAndUpdate(plat.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<6;y++)l.setBlockAndUpdate(plat.offset(x,y,z),Blocks.AIR.defaultBlockState());}}
 static ItemStack outfit(SportsMode.Mode m){for(var i:SportsMode.ITEMS)if(i.get().mode==m&&i.get().getType()==net.minecraft.world.item.ArmorItem.Type.CHESTPLATE)return new ItemStack(i.get());return ItemStack.EMPTY;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20&&!(mc.screen instanceof br.mareeletrica.client.ArcadeScreen))mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  // ---- setup ----
  if(tick==60){Skate.input=()->new float[]{fwd,left,jump,sprint};
   srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);plat=p.blockPosition().above(60);
    FieldKits.buildAt(l,plat,Direction.SOUTH,FieldKits.Kind.BOLICHE);
    FieldKits.buildAt(l,plat.offset(45,0,0),Direction.SOUTH,FieldKits.Kind.GOLFE);
    floor(-70,-10,-20,40);floor(80,-10,100,60);
    log("kits built; pinsetter="+(l.getBlockState(plat.offset(0,1,28)).is(Games.PINSETTER.get())));
    tp(plat.getX()+.5,plat.getY()+1,plat.getZ()+.5,0,15);});}
  if(tick==90)shot("big_bowling_alley");
  // ---- bowling: hooked throw from the approach ----
  if(tick==95)srv.execute(()->{var p=sp();tp(plat.getX()+.5,plat.getY()+1,plat.getZ()+3.5,0,10);
   var s=new ItemStack(Games.BOWLING_BALL_ITEM.get());p.setItemInHand(InteractionHand.MAIN_HAND,s);s.getItem().releaseUsing(s,p.level(),p,72000-20);
   bowl=p.level().getEntitiesOfClass(Games.BowlingBall.class,p.getBoundingBox().inflate(3)).stream().findFirst().orElse(null);log("bowling ball thrown: "+(bowl!=null)+" hook="+(bowl==null?0:bowl.hook)+" color="+(bowl==null?0:Integer.toHexString(bowl.color())));});
  if(tick>95&&tick<180&&tick%10==0&&bowl!=null)srv.execute(()->log(String.format("bowl t%d x=%.2f z=%.2f v=%.2f",tick,bowl.getX()-plat.getX()-.5,bowl.getZ()-plat.getZ(),bowl.getDeltaMovement().horizontalDistance())));
  if(tick==118)shot("big_bowling_roll");
  if(tick==200)srv.execute(()->{int down=0,total=0;for(var pin:sp().serverLevel().getEntitiesOfClass(Games.Pin.class,new net.minecraft.world.phys.AABB(plat).inflate(40))){total++;if(pin.down())down++;}log("pins down after throw: "+down+"/"+total);});
  if(tick==290)srv.execute(()->{int down=0,total=0;for(var pin:sp().serverLevel().getEntitiesOfClass(Games.Pin.class,new net.minecraft.world.phys.AABB(plat).inflate(40))){total++;if(pin.down())down++;}log("pins after reset wait: down="+down+" total="+total);
   var p=sp();tp(plat.getX()+1.5,plat.getY()+1,plat.getZ()+3.5,0,10);p.setShiftKeyDown(true);
   var s=new ItemStack(Games.BOWLING_BALL_ITEM.get());p.setItemInHand(InteractionHand.MAIN_HAND,s);s.getItem().releaseUsing(s,p.level(),p,72000-20);p.setShiftKeyDown(false);
   bowl=p.level().getEntitiesOfClass(Games.BowlingBall.class,p.getBoundingBox().inflate(3)).stream().findFirst().orElse(null);});
  if(tick>290&&tick<330&&tick%4==0&&bowl!=null)srv.execute(()->log(String.format("hook t%d x=%.2f z=%.2f",tick,bowl.getX()-plat.getX()-.5,bowl.getZ()-plat.getZ())));
  if(tick==335)srv.execute(()->{int down=0;for(var pin:sp().serverLevel().getEntitiesOfClass(Games.Pin.class,new net.minecraft.world.phys.AABB(plat).inflate(40)))if(pin.down())down++;log("hook throw pins down: "+down);});
  // ---- golf course ----
  if(tick==340)srv.execute(()->tp(plat.getX()+45.5,plat.getY()+6,plat.getZ()-3,0,25));
  if(tick==360)shot("big_golf");
  // ---- football: goal, ball into the goal, keeper reflex, curve ----
  if(tick==370)srv.execute(()->{var p=sp();var l=p.serverLevel();var g=plat.offset(-45,0,20);
   var st=Sports.GOAL.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,Direction.NORTH);l.setBlockAndUpdate(g,st);Multiblock.place(l,g,st);
   p.getInventory().clearContent();
   ball=Sports.BALL.get().create(l);ball.setItem(new ItemStack(Sports.BALLS.get(Sports.Kind.SOCCER).get()));ball.setPos(g.getX()+3.5,g.getY()+.5,g.getZ()-4);ball.kick(new Vec3(0,.05,.9),null);l.addFreshEntity(ball);
   tp(g.getX()+.5,g.getY(),g.getZ()-12,0,5);});
  if(tick==376)shot("big_goal");
  if(tick==400)srv.execute(()->{var p=sp();int n=0;for(var s:p.getInventory().items)if(s.getItem() instanceof Sports.BallItem)n+=s.getCount();log("goal: ball removed="+ball.isRemoved()+" balls in inventory="+n);});
  if(tick==410)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setItemSlot(EquipmentSlot.CHEST,outfit(SportsMode.Mode.KEEPER));var g=plat.offset(-45,0,20);
   tp(g.getX()+.5,g.getY(),g.getZ()+.5,180,0);
   ball=Sports.BALL.get().create(l);ball.setItem(new ItemStack(Sports.BALLS.get(Sports.Kind.SOCCER).get()));ball.setPos(g.getX()+.5,g.getY()+.6,g.getZ()-8);ball.kick(new Vec3(0,.12,1.2),null);l.addFreshEntity(ball);});
  if(tick>410&&tick<=432&&tick%2==0)srv.execute(()->log(String.format("keeper t%d ball z=%.2f y=%.2f held=%s",tick,ball.getZ()-plat.getZ()-20,ball.getY()-plat.getY(),ball.holderId()==sp().getId())));
  if(tick==440)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setItemSlot(EquipmentSlot.CHEST,outfit(SportsMode.Mode.SOCCER));ball.hold(null);ball.discard();
   tp(plat.getX()-45.5,plat.getY(),plat.getZ()-8,0,0);
   ball=Sports.BALL.get().create(l);ball.setItem(new ItemStack(Sports.BALLS.get(Sports.Kind.SOCCER).get()));ball.setPos(plat.getX()-45.5,plat.getY()+.05,plat.getZ()-7.2);l.addFreshEntity(ball);});
  if(tick==446)srv.execute(()->{var p=sp();SportsMode.READY.clear();SportsMode.action(p,3);curveStart=ball.position();maxSide=0;t0=tick;});
  if(tick>446&&tick<480)srv.execute(()->{double side=ball.getX()-curveStart.x;if(Math.abs(side)>Math.abs(maxSide))maxSide=side;});
  if(tick==480)srv.execute(()->log(String.format("curve kick: forward=%.1f maxSide=%.2f endSide=%.2f",ball.getZ()-curveStart.z,maxSide,ball.getX()-curveStart.x)));
  // ---- basketball hoop: shot from 13 blocks ----
  if(tick==490)srv.execute(()->{var p=sp();var l=p.serverLevel();var h=plat.offset(-60,0,30);
   var st=Sports.HOOP.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,Direction.NORTH);l.setBlockAndUpdate(h,st);Multiblock.place(l,h,st);
   p.setItemSlot(EquipmentSlot.CHEST,outfit(SportsMode.Mode.BASKET));tp(h.getX()+.5,h.getY(),h.getZ()-14,0,-8);
   ball=Sports.BALL.get().create(l);ball.setItem(new ItemStack(Sports.BALLS.get(Sports.Kind.BASKET).get()));ball.setPos(p.getX(),p.getY()+1,p.getZ()+.6);l.addFreshEntity(ball);ball.hold(p);});
  if(tick==496)srv.execute(()->{SportsMode.READY.clear();SportsMode.action(sp(),0);log("basket shot fired");});
  if(tick==503)shot("big_hoop_flight");
  if(tick==540)srv.execute(()->{var b=Scoreboard.nearest(sp().level(),plat.offset(-60,0,30),64);log("after basket: ball y="+String.format("%.1f",ball.getY()-plat.getY())+" swish client="+(Minecraft.getInstance().level.getBlockEntity(plat.offset(-60,0,30)) instanceof Sports.GoalEntity g?g.swish:-1));});
  if(tick==545)srv.execute(()->tp(plat.getX()-62.5,plat.getY()+1,plat.getZ()+22,20,-18));
  if(tick==555)shot("big_hoop");
  // ---- arcade (2 blocks) + pool ----
  if(tick==560)srv.execute(()->{var p=sp();var l=p.serverLevel();var a=plat.offset(-30,0,5);p.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);
   var it=new ItemStack(Fun.ARCADE.get());p.setItemInHand(InteractionHand.MAIN_HAND,it);
   tp(a.getX()+.5,a.getY(),a.getZ()-3.5,0,0);
   var hit=new net.minecraft.world.phys.BlockHitResult(a.below().getCenter().add(0,.5,0),Direction.UP,a.below(),false);
   var r=it.useOn(new net.minecraft.world.item.context.UseOnContext(l,p,InteractionHand.MAIN_HAND,it,hit));
   log("arcade placed: "+r+" lower="+l.getBlockState(a)+" upper="+l.getBlockState(a.above()));
   var tbl=new ItemStack(Pool.TABLE_ITEM.get());p.setItemInHand(InteractionHand.MAIN_HAND,tbl);var o=plat.offset(-38,0,5);
   tp(o.getX()+.5,o.getY(),o.getZ()-1.5,0,0);
   tbl.useOn(new net.minecraft.world.item.context.UseOnContext(l,p,InteractionHand.MAIN_HAND,tbl,new net.minecraft.world.phys.BlockHitResult(o.below().getCenter().add(0,.5,0),Direction.UP,o.below(),false)));
   tp(a.getX()-3.5,a.getY()+1.6,a.getZ()-3.5,-60,25);});
  if(tick==580)shot("big_arcade_pool");
  // ---- jet: shot down, spins nose-down ----
  if(tick==590)srv.execute(()->{var l=sp().serverLevel();jet=Jets.JET.get().create(l);jet.moveTo(plat.getX()+20.5,plat.getY()+60,plat.getZ()-40.5,0,0);jet.setDeltaMovement(0,0,1);l.addFreshEntity(jet);
   jet.hurt(l.damageSources().explosion(null,null),9999);tp(plat.getX()+20.5,plat.getY()+55,plat.getZ()-62,0,-5);});
  if(tick>590&&tick<=640&&tick%10==0)srv.execute(()->{if(jet.isRemoved()){log("jet removed");return;}var f=jet.forward();log(String.format("jet t%d downed=%s fwd.y=%.2f vy=%.2f",tick-590,jet.downed(),f.y,jet.getDeltaMovement().y));});
  if(tick==615)shot("big_jet_spin");
  // ---- cockpit (first person in a healthy jet on the ground) ----
  if(tick==645)srv.execute(()->{var p=sp();var l=p.serverLevel();if(!jet.isRemoved())jet.discard();var j=Jets.JET.get().create(l);j.moveTo(plat.getX()+20.5,plat.getY(),plat.getZ()+50.5,0,0);l.addFreshEntity(j);floor(10,40,30,70);p.startRiding(j,true);});
  if(tick==650)mc.options.setCameraType(CameraType.FIRST_PERSON);
  if(tick==670)shot("big_cockpit");
  // ---- helicopter: shot down, crew bails out, it falls ----
  if(tick==680)srv.execute(()->{var p=sp();p.stopRiding();var l=p.serverLevel();floor(-70,-60,-40,-40);
   heli=Vehicles.HELI.get().create(l);heli.moveTo(plat.getX()-55.5,plat.getY()+25,plat.getZ()-50.5,0,0);l.addFreshEntity(heli);
   tp(plat.getX()-55.5,plat.getY()+25.6,plat.getZ()-50,0,0);p.startRiding(heli,true);});
  if(tick==690)srv.execute(()->{heli.hurt(sp().serverLevel().damageSources().explosion(null,null),9999);log("heli hit: wrecked="+heli.wrecked()+" player riding="+(sp().getVehicle()!=null));});
  if(tick>690&&tick<=780&&tick%10==0)srv.execute(()->log(String.format("heli t%d removed=%s y=%.1f player y=%.1f",tick-690,heli.isRemoved(),heli.getY()-plat.getY(),sp().getY()-plat.getY())));
  if(tick==700){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);}
  if(tick==705)shot("big_heli_fall");
  // ---- skatepark: ramp launch and the push animation ----
  if(tick==790)srv.execute(()->{var p=sp();var l=p.serverLevel();
   for(int z=30;z<=30;z++)for(int x=88;x<=91;x++)l.setBlockAndUpdate(plat.offset(x,0,z),Skate.RAMP.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,Direction.SOUTH));
   for(int z=10;z<=18;z++)l.setBlockAndUpdate(plat.offset(84,0,z),Skate.RAIL.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,Direction.SOUTH));
   board=new Skate.Board(Skate.ENTITY.get(),l);board.moveTo(plat.getX()+89.5,plat.getY(),plat.getZ()+2.5,0,0);l.addFreshEntity(board);
   tp(plat.getX()+89.5,plat.getY(),plat.getZ()+2.5,0,0);p.startRiding(board,true);});
  if(tick>=795&&tick<900){int k=tick-795;fwd=k<40?1:0;sprint=k>=40&&k<55?1:0;jump=0;
   var b=mc.player.getVehicle() instanceof Skate.Board bb?bb:null;
   if(k==20){mc.player.setYRot(90);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);}
   if(k==22)shot("big_skate_push");
   if(k==24){mc.player.setYRot(0);mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);}
   if(k==48&&b!=null)log("manual flag="+((b.anim()&Skate.MANUAL)!=0));
   if(b!=null&&k%5==0)log(String.format("skate k%d z=%.1f y=%.2f speed=%.2f air=%d trick=%d",k,b.getZ()-plat.getZ(),b.getY()-plat.getY(),b.speed,b.air,b.trick));
   if(b!=null&&b.trick==5&&b.air==5){mc.player.setYRot(90);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);shot("big_skate_air");}
  }
  if(tick==905){mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);srv.execute(()->{tp(plat.getX()+86.5,plat.getY()+1.2,plat.getZ()+8,30,20);});}
  if(tick==915)shot("big_skatepark");
  if(tick==920)log("DONE");
 }
}
