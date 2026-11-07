package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.club): how the golf club is held (front view, side view, first person). */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualClub {
 static int tick;
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.club"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
   var at=p.blockPosition().above(50);for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){p.serverLevel().setBlockAndUpdate(at.offset(x,-1,z),net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<3;y++)p.serverLevel().setBlockAndUpdate(at.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
   p.teleportTo(p.serverLevel(),at.getX()+.5,at.getY(),at.getZ()+.5,0,10);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Games.GOLF_CLUB.get()));});
  if(tick>=62&&tick%10==0)mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
   for(var en:p.serverLevel().getEntities(p,p.getBoundingBox().inflate(40),x->!(x instanceof net.minecraft.world.entity.player.Player)))en.discard();});
  if(tick==80){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.player.setYRot(90);}
  if(tick==95)Screenshot.grab(mc.gameDirectory,"club_side.png",mc.getMainRenderTarget(),c->{});
  if(tick==97)mc.player.setYRot(0);
  if(tick==107)Screenshot.grab(mc.gameDirectory,"club_front.png",mc.getMainRenderTarget(),c->{});
  if(tick==109)mc.options.setCameraType(CameraType.FIRST_PERSON);
  if(tick==119)Screenshot.grab(mc.gameDirectory,"club_fp.png",mc.getMainRenderTarget(),c->{});
  if(tick==125)System.out.println("[CLUB] DONE");
 }
}
