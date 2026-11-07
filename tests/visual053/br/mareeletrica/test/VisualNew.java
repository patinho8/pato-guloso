package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.newstuff): frog hp, rare egg, ejection, thermal, radio, golf, bowling, pong. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualNew {
 static int tick;static BlockPos plat;static RareEgg egg;static Jets.JetEntity jet;static Games.GolfBall golf;static BlockPos setter,hole;static net.minecraft.world.entity.monster.Zombie zom;static Support.Crate crate;
 static boolean on(){return Boolean.getBoolean("mareeletrica.newstuff");}
 static void log(String s){System.out.println("[NEW] "+s);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20&&!(mc.screen instanceof br.mareeletrica.client.ArcadeScreen))mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
   plat=p.blockPosition().above(30);for(int x=-10;x<=10;x++)for(int z=-14;z<=14;z++)l.setBlockAndUpdate(plat.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());
   // frog
   var f=Frogs.ENTITY.get().create(l);f.moveTo(plat.getX()+3,plat.getY(),plat.getZ()-8,0,0);l.addFreshEntity(f);log("frog max health "+f.getMaxHealth());
   // rare egg via the item
   var item=RareEgg.item();var ctx=new net.minecraft.world.item.context.UseOnContext(l,p,InteractionHand.MAIN_HAND,item,new net.minecraft.world.phys.BlockHitResult(plat.offset(-3,-1,-8).getCenter().add(0,.5,0),Direction.UP,plat.offset(-3,-1,-8),false));
   p.setItemInHand(InteractionHand.MAIN_HAND,item);item.getItem().useOn(ctx);
   egg=l.getEntitiesOfClass(RareEgg.class,new AABB(plat).inflate(15)).stream().findFirst().orElse(null);log("rare egg placed: "+(egg!=null));if(egg!=null)egg.age=RareEgg.HATCH-30;
   // golf: hole 1.2 blocks away from the ball, putt toward it
   hole=plat.offset(0,-1,-4);l.setBlockAndUpdate(hole,Games.GOLF_HOLE.get().defaultBlockState());
   golf=Games.GOLF_BALL.get().create(l);golf.setPos(plat.getX()+.5,plat.getY(),plat.getZ()-1.3);l.addFreshEntity(golf);
   // bowling: pinsetter at the far end facing the player (south)
   setter=plat.offset(0,0,10);l.setBlockAndUpdate(setter,Games.PINSETTER.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));
   if(l.getBlockEntity(setter) instanceof Games.PinsetterEntity pe)pe.setPins();
   log("pins set: "+l.getEntitiesOfClass(Games.Pin.class,new AABB(setter).inflate(8)).size());
  });
  if(tick==70)srv.execute(()->{var p=sp();var l=p.serverLevel();
   // putt: stand behind the ball, look north (toward the hole), crouch
   p.teleportTo(golf.getX(),golf.getY(),golf.getZ()+1.2);p.setYRot(180);p.setXRot(10);p.setShiftKeyDown(true);
   var club=new ItemStack(Games.GOLF_CLUB.get());club.getItem().releaseUsing(club,l,p,72000-10);p.setShiftKeyDown(false);
   log("golf ball after putt: vel "+golf.getDeltaMovement()+" strokes "+golf.strokes);});
  if(tick==100)srv.execute(()->{var l=sp().serverLevel();log("t100 egg removed="+(egg==null||egg.isRemoved())+" age="+(egg!=null?egg.age:-1)+" chickens near: "+l.getEntitiesOfClass(FightChickens.FightChicken.class,new AABB(plat).inflate(40)).stream().map(c->c.breed()+" tame="+c.isTame()+" at "+c.blockPosition()).toList());});
  if(tick==130)srv.execute(()->log("golf: ball removed (in the hole)="+golf.isRemoved()+" pos "+golf.position()));
  if(tick==135)srv.execute(()->{var p=sp();var l=p.serverLevel();
   // bowling: from 7 blocks in front of the pins, look south
   p.teleportTo(setter.getX()+.5,setter.getY(),setter.getZ()-12+.5);p.setYRot(0);p.setXRot(10);
   var b=new ItemStack(Games.BOWLING_BALL_ITEM.get());b.getItem().releaseUsing(b,l,p,72000-30);log("bowling ball rolled");});
  if(tick==215)srv.execute(()->{var l=sp().serverLevel();long down=l.getEntitiesOfClass(Games.Pin.class,new AABB(setter).inflate(8)).stream().filter(Games.Pin::down).count();log("bowling: pins down = "+down);});
  if(tick==216)Screenshot.grab(mc.gameDirectory,"new_bowling.png",mc.getMainRenderTarget(),c->{});
  if(tick==222)srv.execute(()->{log("chickens at 222: "+sp().serverLevel().getEntitiesOfClass(FightChickens.FightChicken.class,new AABB(plat).inflate(40)).stream().map(c->c.breed()+" tame="+c.isTame()+" rm="+c.isRemoved()+" at "+c.blockPosition()).toList());log("rare egg hatched: egg gone="+(egg==null||egg.isRemoved())+" golden rooster="+sp().serverLevel().getEntitiesOfClass(FightChickens.FightChicken.class,new AABB(plat).inflate(20),c->c.breed()==ChickenTypes.Breed.DOURADO&&c.isTame()).size());});
  // ejection
  if(tick==225)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
   jet=Jets.JET.get().create(l);jet.moveTo(plat.getX(),plat.getY()+25,plat.getZ(),0,0);l.addFreshEntity(jet);p.startRiding(jet,true);log("in jet: "+(p.getVehicle()==jet));});
  if(tick==232)srv.execute(()->{var l=sp().serverLevel();log("before eject: jetY="+jet.getY()+" ground="+jet.onGround()+" hmap="+l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,jet.getBlockX(),jet.getBlockZ())+" riding="+(sp().getVehicle()==jet));jet.eject(sp());log("ejected: riding="+sp().getVehicle()+" jet downed="+jet.downed());});
  if(tick==260)srv.execute(()->log("after eject: on parachute="+(sp().getVehicle() instanceof Parachutes.ParachuteEntity)+" hp="+sp().getHealth()));
  if(tick==262)srv.execute(()->{sp().stopRiding();sp().setGameMode(net.minecraft.world.level.GameType.CREATIVE);sp().teleportTo(plat.getX()+.5,plat.getY(),plat.getZ()+.5);});
  // thermal goggles + zombie behind a wall
  if(tick==270)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Gadgets.THERMAL_GOGGLES.get()));p.setYRot(180);p.setXRot(5);
   for(int y=0;y<4;y++)for(int x=-3;x<=3;x++)l.setBlockAndUpdate(plat.offset(x,y,-4),Blocks.STONE.defaultBlockState());
   zom=EntityType.ZOMBIE.create(l);zom.moveTo(plat.getX()+.5,plat.getY(),plat.getZ()-7,0,0);zom.setNoAi(true);l.addFreshEntity(zom);});
  if(tick>=271&&tick<300){mc.player.setYRot(180);mc.player.setXRot(5);}
  if(tick==290){var cz=mc.level.getEntity(zom.getId());log("thermal: active="+br.mareeletrica.client.ThermalClient.active()+" zombie glows="+(cz!=null&&mc.shouldEntityAppearGlowing(cz))+" color="+(cz!=null?Integer.toHexString(cz.getTeamColor()):"?"));}
  if(tick==292)Screenshot.grab(mc.gameDirectory,"new_thermal.png",mc.getMainRenderTarget(),c->{});
  // radio: supply crate where we look (down at the platform) and an air strike far away
  if(tick==300)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);p.setXRot(70);p.setYRot(0);
   var r=new ItemStack(Support.RADIO.get());p.setItemInHand(InteractionHand.MAIN_HAND,r);r.getItem().use(l,p,InteractionHand.MAIN_HAND);
   crate=l.getEntitiesOfClass(Support.Crate.class,new AABB(plat).inflate(10,120,10)).stream().findFirst().orElse(null);log("supply crate in the sky: "+(crate!=null)+(crate!=null?" y="+crate.getY():""));
   if(crate!=null)crate.setPos(crate.getX(),plat.getY()+4,crate.getZ());
   p.setShiftKeyDown(true);p.setXRot(40);r.getItem().use(l,p,InteractionHand.MAIN_HAND);p.setShiftKeyDown(false);});
  if(tick==360)srv.execute(()->{var l=sp().serverLevel();int chests=0,items=0;
   for(var bp:BlockPos.betweenClosed(plat.offset(-6,-1,-6),plat.offset(6,3,12)))if(l.getBlockEntity(bp) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity c){chests++;for(int i=0;i<27;i++)if(!c.getItem(i).isEmpty())items++;}
   log("supply crate landed: chests="+chests+" stacks inside="+items+" crate entity gone="+(crate==null||crate.isRemoved()));});
  if(tick==460)srv.execute(()->{var l=sp().serverLevel();int air=0;for(var bp:BlockPos.betweenClosed(plat.offset(-10,-1,-14),plat.offset(10,-1,14)))if(l.getBlockState(bp).isAir())air++;log("air strike: platform blocks destroyed = "+air);});
  // pong vs the computer
  if(tick>=470&&tick<=566&&plat!=null)srv.execute(()->{var p=sp();p.setDeltaMovement(0,0,0);p.fallDistance=0;p.teleportTo(plat.getX()+3.5,plat.getY(),plat.getZ()+1.5);});
  if(tick==470){srv.execute(()->{var l=sp().serverLevel();l.setBlockAndUpdate(plat.offset(3,0,3),Fun.ARCADE.get().defaultBlockState());var p=sp();p.stopRiding();p.teleportTo(plat.getX()+3.5,plat.getY(),plat.getZ()+1.5);});}
  if(tick==480){var a=new br.mareeletrica.client.ArcadeScreen(plat.offset(3,0,3));mc.setScreen(a);a.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_2,0,0);}
  if(tick==490&&mc.screen instanceof br.mareeletrica.client.ArcadeScreen a)a.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE,0,0);
  if(tick==560){log("pong screen open="+(mc.screen instanceof br.mareeletrica.client.ArcadeScreen)+" client state="+br.mareeletrica.client.ArcadeScreen.debugState());Screenshot.grab(mc.gameDirectory,"new_pong.png",mc.getMainRenderTarget(),c->{});}
  if(tick==565)srv.execute(()->{var l=sp().serverLevel();if(l.getBlockEntity(plat.offset(3,0,3)) instanceof Fun.ArcadeEntity a)log("pong server: running="+(a.pong!=null));});
  if(tick==570){mc.setScreen(null);log("DONE");mc.stop();}
 }
}
