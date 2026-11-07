package br.mareeletrica.test;

import br.mareeletrica.Sports;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.ball): balls dropped and kicked must never go into the floor and must settle. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualBall {
 static int tick;static Sports.SportBall[] balls=new Sports.SportBall[4];static double[] minS={9,9,9,9},minC={9,9,9,9};static int[] bounces=new int[4],restAt={-1,-1,-1,-1};static double[] lastVy=new double[4];
 static final int OX=-3000,OZ=3000;static final double FLOOR=-60;
 static boolean on(){return Boolean.getBoolean("mareeletrica.ball");}
 static void log(String s){System.out.println("[BALL] "+s);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick>=40&&mc.player.distanceToSqr(OX+.5,-59,OZ+10)>4)mc.player.setPos(OX+.5,-59,OZ+10);
  if(tick>=40){mc.player.setYRot(180);mc.player.setXRot(10);}
  if(tick==40)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.CREATIVE);p.teleportTo(l,OX+.5,-59,OZ+10,180,10);
   for(int y=-60;y<-55;y++)for(int x=-3;x<=3;x++)l.setBlockAndUpdate(new BlockPos(OX+x,y,OZ-12),Blocks.STONE.defaultBlockState());});
  if(tick==150)srv.execute(()->{var l=srv.overworld();
   Sports.Kind[] k={Sports.Kind.SOCCER,Sports.Kind.BASKET,Sports.Kind.VOLLEY,Sports.Kind.SOCCER};
   for(int i=0;i<4;i++){var b=new Sports.SportBall(Sports.BALL.get(),l);b.setItem(new ItemStack(Sports.BALLS.get(k[i]).get()));b.setPos(OX-3+i*2,i==3?-60:-48,OZ);l.addFreshEntity(b);balls[i]=b;
    if(i==3)b.kick(new Vec3(0,0,-.9),null);}   // rolling into the stone wall
  });
  if(tick>150&&tick<560){
   srv.execute(()->{for(int i=0;i<4;i++){var b=balls[i];if(b==null)continue;minS[i]=Math.min(minS[i],b.getY()-FLOOR);double vy=b.getDeltaMovement().y;if(lastVy[i]<-.05&&vy>.05)bounces[i]++;lastVy[i]=vy;
    if(restAt[i]<0&&b.getDeltaMovement().lengthSqr()<1e-6&&tick>160)restAt[i]=tick-150;}});
   for(int i=0;i<4;i++){if(balls[i]==null)continue;var c=mc.level.getEntity(balls[i].getId());if(c!=null)minC[i]=Math.min(minC[i],c.getY()-FLOOR);}
  }
  if(tick==175)Screenshot.grab(mc.gameDirectory,"ball_fall.png",mc.getMainRenderTarget(),c->{});
  if(tick==560){for(int i=0;i<4;i++)log("ball "+i+" "+balls[i].kind()+" minY server "+String.format("%.3f",minS[i])+" client "+String.format("%.3f",minC[i])+" bounces "+bounces[i]+" restAfter "+restAt[i]+" ticks pos "+balls[i].position());log("DONE");mc.stop();}
 }
}
