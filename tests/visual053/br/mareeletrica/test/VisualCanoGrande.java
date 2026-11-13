package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.canogrande): pump + big pipes pouring water out of the open ends. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualCanoGrande {
 static int tick;static BlockPos o;
 static boolean on(){return Boolean.getBoolean("mareeletrica.canogrande");}
 static void log(String s){System.out.println("[CG] "+s);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"cg_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void pipe(net.minecraft.server.level.ServerLevel l,BlockPos p){var b=BigPipe.PIPE.get();l.setBlock(p,b.connect(b.defaultBlockState(),l,p),3);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(GameType.CREATIVE);o=p.blockPosition().atY(170);l.setDayTime(6000);l.setWeatherParameters(99999,0,false,false);
   for(int x=-10;x<=10;x++)for(int z=-12;z<=6;z++){l.setBlock(o.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);for(int y=0;y<10;y++)l.setBlock(o.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);}
   // a water pool (sources) on the left
   for(int x=-8;x<=-5;x++)for(int z=-6;z<=-3;z++){l.setBlock(o.offset(x,-1,z),Blocks.WATER.defaultBlockState(),3);}
   // pump right next to the pool, sucking west
   BlockPos pump=o.offset(-4,-1,-4);l.setBlock(pump.below(),Blocks.SMOOTH_STONE.defaultBlockState(),2);l.setBlock(pump,BigPipe.PUMP.get().defaultBlockState().setValue(BigPipe.PumpBlock.FACING,Direction.WEST),3);
   // pipe: up 3, then east 6, then a T going north, open ends pour
   for(int y=0;y<=3;y++)pipe(l,pump.above(y+1));
   for(int x=1;x<=7;x++)pipe(l,pump.above(4).east(x));
   for(int z=1;z<=2;z++)pipe(l,pump.above(4).east(4).north(z));
   pipe(l,pump.above(4).east(7).below());
   for(int pass=0;pass<1;pass++){for(int y=0;y<=3;y++)pipe(l,pump.above(y+1));for(int x=1;x<=7;x++)pipe(l,pump.above(4).east(x));for(int z=1;z<=2;z++)pipe(l,pump.above(4).east(4).north(z));pipe(l,pump.above(4).east(7).below());}
   p.teleportTo(l,o.getX()+.5,o.getY()+1,o.getZ()+4.5,180,0);log("pump "+l.getBlockState(pump)+" | h "+l.getBlockState(pump.above(4).east(2))+" | v "+l.getBlockState(pump.above(2)));});
  if(tick>60&&tick<300){mc.player.setYRot(180);mc.player.setXRot(-8);}
  if(tick==90)mc.options.hideGui=true;
  if(tick==100)shot("inicio");if(tick==200){shot("rede");srv.execute(()->{var l=sp().serverLevel();int water=0;for(BlockPos q:BlockPos.betweenClosed(o.offset(-10,-1,-12),o.offset(10,8,6)))if(l.getFluidState(q).is(net.minecraft.tags.FluidTags.WATER))water++;log("water blocks "+water);});}
  if(tick==210){mc.options.hideGui=false;log("DONE");}
 }
}
