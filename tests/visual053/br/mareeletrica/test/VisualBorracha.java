package br.mareeletrica.test;

import br.mareeletrica.*;
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

/** Test only (-Dmareeletrica.borracha): rubber ducks floating in the big tub and in a pool, squeaking. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualBorracha {
 static int tick;static BlockPos o;static java.util.List<RubberDuck.Toy> toys=new java.util.ArrayList<>();
 static boolean on(){return Boolean.getBoolean("mareeletrica.borracha");}
 static void log(String s){System.out.println("[RUB] "+s);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"rub_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(GameType.CREATIVE);o=p.blockPosition().atY(220);l.setDayTime(6000);l.setWeatherParameters(99999,0,false,false);
   for(int x=-8;x<=8;x++)for(int z=-10;z<=6;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.SMOOTH_QUARTZ.defaultBlockState());for(int y=0;y<5;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   for(int x=2;x<=6;x++)for(int z=-7;z<=-3;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.WATER.defaultBlockState());}   // small pool
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+2.5,200,40);
   var floor=o.offset(-2,-1,-3);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Bath.BIG_ITEM.get()));new ItemStack(Bath.BIG_ITEM.get()).useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(floor).add(0,.5,0),Direction.UP,floor,false)));
   p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var c=o.offset(-2,0,-4);log("tub "+l.getBlockState(c));l.getBlockState(c).useWithoutItem(l,p,new BlockHitResult(Vec3.atCenterOf(c),Direction.UP,c,false));
  });
  if(tick==62)srv.execute(()->{var l=sp().serverLevel();
   double[][] at={{-2.6,.6,-4.4},{-1.4,.6,-3.6},{-2.2,.6,-3.1},{4.5,-.2,-5},{3.2,-.2,-4},{5.3,-.2,-6.2},{0,0,-1}};
   for(int i=0;i<at.length;i++){var d=RubberDuck.ENTITY.get().create(l);d.setVariant(i%4);d.moveTo(o.getX()+.5+at[i][0],o.getY()+at[i][1],o.getZ()+.5+at[i][2],i*50,0);l.addFreshEntity(d);toys.add(d);}
   var duck=Pato.ENTITY.get().create(l);duck.moveTo(o.getX()+1.5,o.getY(),o.getZ()-1,150,0);duck.tame(sp());l.addFreshEntity(duck);});
  if(tick>62&&tick<180){mc.player.setYRot(200);mc.player.setXRot(40);}
  if(tick==100)srv.execute(()->log("toys: "+toys.stream().map(t->String.format("%d y=%.2f float=%s",t.variant(),t.getY()-o.getY(),t.floating())).toList()));
  if(tick==110){mc.options.hideGui=true;}
  if(tick==120)shot("banheira");
  if(tick==125)srv.execute(()->{for(var t:toys)t.squeak(1F);});
  if(tick==126)shot("squeak");
  if(tick==160)srv.execute(()->log("after 100 ticks: "+toys.stream().map(t->String.format("y=%.2f",t.getY()-o.getY())).toList()));
  if(tick==165)log("DONE");
 }
 static net.minecraft.nbt.CompoundTag tagFor(int v){var t=new net.minecraft.nbt.CompoundTag();t.putInt("Variant",v);return t;}
}
