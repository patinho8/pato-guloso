package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.pool): pool table (placing, break shot, potting, the 8, breaking the table) and the Pato Guloso maze game. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualPool {
 static int tick,stage,wait;static BlockPos plat,origin;
 static boolean on(){return Boolean.getBoolean("mareeletrica.pool");}
 static void log(String s){System.out.println("[POOL] "+s);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static Pool.PoolEntity table(){return sp().serverLevel().getBlockEntity(origin) instanceof Pool.PoolEntity e?e:null;}
 static void stand(double x,double z,float yaw,float pitch){var p=sp();p.setDeltaMovement(0,0,0);p.teleportTo(p.serverLevel(),x,plat.getY(),z,yaw,pitch);}
 static void shoot(int ticks){var p=sp();var s=p.getMainHandItem();s.getItem().releaseUsing(s,p.level(),p,72000-ticks);}
 static String state(Pool.PoolEntity e){int n=0;for(int i=1;i<16;i++)if(e.in[i])n++;return "moving="+e.moving+" potted="+n+" white in="+e.in[0]+" phase="+e.phase+" turn="+e.turn+" groups="+e.group[0]+"/"+e.group[1]+" winner='"+e.winner+"'";}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20&&!(mc.screen instanceof br.mareeletrica.client.ArcadeScreen))mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
   plat=p.blockPosition().above(40);for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++){l.setBlockAndUpdate(plat.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(plat.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   origin=plat.offset(-1,0,-2);
   stand(origin.getX()+1,origin.getZ()-1.5,0,0);
   var item=new ItemStack(Pool.TABLE_ITEM.get());p.setItemInHand(InteractionHand.MAIN_HAND,item);
   var hit=new net.minecraft.world.phys.BlockHitResult(origin.below().getCenter().add(0,.5,0),Direction.UP,origin.below(),false);
   var r=item.useOn(new net.minecraft.world.item.context.UseOnContext(l,p,InteractionHand.MAIN_HAND,item,hit));
   int n=0;for(int x=-2;x<=3;x++)for(int z=-3;z<=5;z++)if(l.getBlockState(origin.offset(x,0,z)).is(Pool.TABLE.get()))n++;
   log("placed: "+r+" pieces="+n+" main="+(table()!=null)+(table()!=null?" facing="+table().facing:""));
   p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Pool.CUE.get()));
   stand(origin.getX()+1,origin.getZ()-1.4,0,38);
  });
  if(tick==85)Screenshot.grab(mc.gameDirectory,"pool_aim.png",mc.getMainRenderTarget(),c->{});
  if(tick==90)srv.execute(()->{shoot(30);log("break shot: "+state(table()));});
  if(tick==96)Screenshot.grab(mc.gameDirectory,"pool_break.png",mc.getMainRenderTarget(),c->{});
  if(tick>96&&stage==0&&tick%10==0)srv.execute(()->{var t=table();if(!t.moving&&stage==0){stage=1;log("after break (t"+tick+"): "+state(t));}});
  // stage 1: set up a sure pot — ball 1 near the bottom-left corner pocket, white behind it on the diagonal
  if(stage==1){stage=2;srv.execute(()->{var t=table();
   for(int i=2;i<16;i++)if(i!=8&&!t.in[i]){t.x[i]=1.6F;t.z[i]=1.2F+i*.16F;}t.x[8]=1.7F;t.z[8]=3.6F;
   t.in[1]=false;t.x[1]=.45F;t.z[1]=.45F;t.in[0]=false;t.x[0]=.85F;t.z[0]=.85F;
   // look from behind the white towards the pocket: local dir (-1,-1) = world (-x,-z) → yaw 135
   stand(origin.getX()+1.5,origin.getZ()+1.5,135,40);wait=tick;});}
  if(stage==2&&tick==wait+12){stage=3;srv.execute(()->{shoot(12);log("pot shot fired: "+state(table()));});}
  if(stage==3&&tick%10==0)srv.execute(()->{var t=table();if(!t.moving&&stage==3){stage=4;log("pot result: ball1 in="+t.in[1]+" "+state(t));}});
  // stage 4: put the 8 in early → the game is lost
  if(stage==4){stage=5;srv.execute(()->{var t=table();for(int i=1;i<16;i++)if(i!=8&&!t.in[i]){t.x[i]=.35F;t.z[i]=1.0F+i*.15F;}t.in[8]=false;t.x[8]=1.55F;t.z[8]=3.55F;t.x[0]=1.15F;t.z[0]=3.15F;t.in[0]=false;
   stand(origin.getX()+.2,origin.getZ()+2.2,-45,40);wait=tick;});}   // local dir (+1,+1) = world (+x,+z) → yaw -45
  if(stage==5&&tick==wait+12){stage=6;srv.execute(()->{shoot(10);});}
  if(stage==6&&tick%10==0)srv.execute(()->{var t=table();if(!t.moving&&stage==6){stage=7;log("eight early: 8 in="+t.in[8]+" "+state(t));stand(origin.getX()+1,origin.getZ()-1.6,0,30);wait=tick;}});
  if(stage==7&&tick==wait+10){stage=8;Screenshot.grab(mc.gameDirectory,"pool_end.png",mc.getMainRenderTarget(),c->{});}
  // stage 8: breaking one piece removes the whole table
  if(stage==8&&tick==wait+14){stage=9;srv.execute(()->{var l=sp().serverLevel();l.destroyBlock(origin.offset(1,0,2),false);
   int n=0;for(int x=-2;x<=3;x++)for(int z=-3;z<=5;z++)if(l.getBlockState(origin.offset(x,0,z)).is(Pool.TABLE.get()))n++;log("after breaking one piece: pieces left="+n);
   l.setBlockAndUpdate(plat.offset(3,0,3),Fun.ARCADE.get().defaultBlockState());stand(plat.getX()+3.5,plat.getZ()+1.5,0,0);});wait=tick;}
  // maze game
  if(stage==9&&tick==wait+10){var a=new br.mareeletrica.client.ArcadeScreen(plat.offset(3,0,3));mc.setScreen(a);a.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_3,0,0);a.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE,0,0);}
  if(stage==9&&tick>wait+10&&tick<wait+420&&mc.screen instanceof br.mareeletrica.client.ArcadeScreen a){
   srv.execute(()->{var p=sp();p.setDeltaMovement(0,0,0);p.teleportTo(plat.getX()+3.5,plat.getY(),plat.getZ()+1.5);});
   int k=(tick-wait)/25%4;int[] keys={org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT,org.lwjgl.glfw.GLFW.GLFW_KEY_UP,org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT,org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN};
   if((tick-wait)%25==0)a.keyPressed(keys[k],0,0);
   if((tick-wait)%30==0)log("maze t"+(tick-wait)+": "+a.mazeDebug());
   if(tick==wait+60||tick==wait+150){Screenshot.grab(mc.gameDirectory,"maze"+(tick-wait)+".png",mc.getMainRenderTarget(),c->{});}
  }
  if(stage==9&&tick==wait+430){log("DONE");}
 }
}
