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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.roda): the Ferris wheel by day, by night and riding it. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualRoda {
 static int tick;static BlockPos o,booth;
 static boolean on(){return Boolean.getBoolean("mareeletrica.roda");}
 static void log(String s){System.out.println("[RODA] "+s);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"roda_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(GameType.CREATIVE);o=p.blockPosition().atY(200);l.setDayTime(6000);l.setWeatherParameters(99999,0,false,false);
   for(int x=-14;x<=14;x++)for(int z=-14;z<=24;z++){l.setBlockAndUpdate(o.offset(x,-1,z),(x+z)%7==0?Blocks.GRASS_BLOCK.defaultBlockState():Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<20;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   booth=o;p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+20.5,180,-8);
   p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(FerrisWheel.ITEM.get()));
   var floor=o.below();var r=new ItemStack(FerrisWheel.ITEM.get()).useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(floor).add(0,.5,0),Direction.UP,floor,false)));
   p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);log("placed "+r+" "+l.getBlockState(o));});
  if(tick>60&&tick<200){mc.player.setYRot(180);mc.player.setXRot(-12);}
  if(tick==90)mc.options.hideGui=true;
  if(tick==110)shot("dia");if(tick==112)srv.execute(()->{var p=sp();p.teleportTo(p.serverLevel(),o.getX()+12.5,o.getY()+2,o.getZ()+14.5,140,-10);});if(tick>112&&tick<140){mc.player.setYRot(140);mc.player.setXRot(-14);}if(tick==135)shot("lado");
  if(tick==115)srv.execute(()->sp().serverLevel().setDayTime(18000));
  if(tick==150)shot("noite");
  if(tick==155)srv.execute(()->{var p=sp();var l=p.serverLevel();p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+2.5,180,0);});
  if(tick==165)srv.execute(()->{var p=sp();var l=p.serverLevel();l.getBlockState(booth).useWithoutItem(l,p,new BlockHitResult(Vec3.atCenterOf(booth),Direction.UP,booth,false));log("riding "+p.getVehicle());l.setDayTime(12500);});
  if(tick==175)log("seat at "+mc.player.position()+" booth "+booth);
  if(tick>175&&tick<520){mc.player.setYRot(((tick/80)%2==0?0:150));mc.player.setXRot(15);}
  if(tick==200)shot("dentro_baixo");
  if(tick==320){shot("dentro_subindo");log("pos "+mc.player.position());}
  if(tick==330)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  if(tick==345)shot("terceira");
  if(tick==480){mc.options.setCameraType(CameraType.FIRST_PERSON);shot("topo");log("pos top "+mc.player.position());}
  if(tick==520){srv.execute(()->{var p=sp();p.stopRiding();log("after dismount "+p.position());});}
  if(tick==530)log("DONE");
 }
}
