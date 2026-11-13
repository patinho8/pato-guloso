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

/** Test only (-Dmareeletrica.cano): a small water pipe network. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualCano {
 static int tick;static BlockPos o;
 static boolean on(){return Boolean.getBoolean("mareeletrica.cano");}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"cano_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static void put(net.minecraft.server.level.ServerPlayer p,BlockPos at){var l=p.serverLevel();
  var below=at.below();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MareEletrica.WATER_DUCT_ITEM.get()));
  new ItemStack(MareEletrica.WATER_DUCT_ITEM.get()).useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(below).add(0,.5,0),Direction.UP,below,false)));}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(GameType.CREATIVE);o=p.blockPosition().atY(200);l.setDayTime(6000);l.setWeatherParameters(99999,0,false,false);
   for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState());for(int y=0;y<8;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   var s=MareEletrica.WATER_DUCT.get().defaultBlockState();
   // a run along x, a corner, a T, a vertical riser and a lone piece
   for(int x=-4;x<=2;x++)l.setBlock(o.offset(x,0,-3),s,3);
   for(int z=-3;z<=1;z++)l.setBlock(o.offset(2,0,z),s,3);
   l.setBlock(o.offset(-1,0,-2),s,3);l.setBlock(o.offset(-1,0,-1),s,3);
   for(int y=1;y<=3;y++)l.setBlock(o.offset(-4,y,-3),s,3);l.setBlock(o.offset(-3,3,-3),s,3);
   l.setBlock(o.offset(4,0,-1),s,3);
   p.teleportTo(l,o.getX()+.5,o.getY()+2,o.getZ()+4.5,180,25);});
  if(tick>60&&tick<130){mc.player.setYRot(180);mc.player.setXRot(28);}
  if(tick==90)mc.options.hideGui=true;
  if(tick==110)shot("rede");
  if(tick==115){mc.options.hideGui=false;}
  if(tick==125)shot("item");
  if(tick==128)System.out.println("[CANO] DONE");
 }
}
