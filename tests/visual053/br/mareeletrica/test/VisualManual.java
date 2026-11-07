package br.mareeletrica.test;

import br.mareeletrica.Batteries;
import br.mareeletrica.Sports;
import br.mareeletrica.SportsMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.manual): guns stay quiet near monsters and fire only when commanded; volleyball lob heights. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualManual {
 static int tick;static BlockPos[] P;static Zombie z;static float zh;static Sports.SportBall ball;static double ballTop=-999,ballStart;
 static boolean on(){return Boolean.getBoolean("mareeletrica.manual");}
 static void log(String s){System.out.println("[MAN] "+s);}
 static String ammo(net.minecraft.server.level.ServerLevel l){var sb=new StringBuilder();for(var b:P)if(l.getBlockEntity(b) instanceof Batteries.Gun g)sb.append(g.kind()).append('=').append(g.ammo).append(' ');return sb.toString();}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==60)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);var b=p.blockPosition().above(40);for(int x=-6;x<=6;x++)for(int zz=-26;zz<=2;zz++)l.setBlockAndUpdate(b.offset(x,-1,zz),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
   P=new BlockPos[]{b.offset(-3,0,-3),b.offset(0,0,-3),b.offset(3,0,-3)};
   l.setBlockAndUpdate(P[0],Batteries.SENTRY.get().defaultBlockState());l.setBlockAndUpdate(P[1],Batteries.GRENADE.get().defaultBlockState());l.setBlockAndUpdate(P[2],Batteries.FLAK.get().defaultBlockState());
   for(var x:P)if(l.getBlockEntity(x) instanceof Batteries.Gun g){g.ammo=1000;g.owner=p.getUUID();g.sync();}
   z=EntityType.ZOMBIE.create(l);z.moveTo(b.getX()-3,b.getY(),b.getZ()-20,0,0);z.setNoAi(true);z.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.LEATHER_HELMET));l.addFreshEntity(z);zh=z.getHealth();
   log("placed; ammo "+ammo(l));});
  if(tick==160)srv.execute(()->{var l=srv.overworld();log("after 100 ticks with a zombie in range (should be untouched): ammo "+ammo(l)+" zombie hp "+z.getHealth()+"/"+zh);
   var zc=z.position().add(0,1,0);
   for(var x:P)if(l.getBlockEntity(x) instanceof Batteries.Gun g){g.aimAt(g.kind()==Batteries.Kind.FLAK?zc.add(0,30,0):g.kind()==Batteries.Kind.GRENADE?z.position():zc);}});
  if(tick==175)srv.execute(()->{var l=srv.overworld();for(var x:P)if(l.getBlockEntity(x) instanceof Batteries.Gun g)g.trigger(false);});
  if(tick==185)Screenshot.grab(mc.gameDirectory,"manual_fire.png",mc.getMainRenderTarget(),c->{});
  if(tick==230)srv.execute(()->{var l=srv.overworld();log("after one click each: ammo "+ammo(l)+" zombie hp "+(z.isAlive()?z.getHealth():0)+"/"+zh);});
  // volleyball: serve 18 blocks and measure how high it goes
  if(tick==240)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);ball=new Sports.SportBall(Sports.BALL.get(),l);ball.setItem(new ItemStack(Sports.BALLS.get(Sports.Kind.VOLLEY).get()));
   Vec3 from=p.position().add(0,2,8);ball.setPos(from.x,from.y,from.z);l.addFreshEntity(ball);ballStart=from.y;ball.kick(SportsMode.lob(from,from.add(18,-2,0),Sports.Kind.VOLLEY.gravity,5.5),null);});
  if(tick>240&&tick<330)srv.execute(()->{if(ball!=null)ballTop=Math.max(ballTop,ball.getY());});
  if(tick==330)srv.execute(()->log("volley serve: rose "+String.format("%.1f",ballTop-ballStart)+" blocks, landed "+String.format("%.1f",ball.getX()-(srv.getPlayerList().getPlayers().get(0).getX()))+" blocks away"));
  if(tick==340){log("DONE");mc.stop();}
 }
}
