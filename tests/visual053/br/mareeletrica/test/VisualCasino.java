package br.mareeletrica.test;

import br.mareeletrica.Casino;
import br.mareeletrica.FieldKits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Visual check (test only, -Dmareeletrica.casino): casino machines, a few games and two ready-made fields. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualCasino {
 static int tick=-1;static int gy;
 static boolean on(){return Boolean.getBoolean("mareeletrica.casino");}
 static void shot(Minecraft mc,String n){Screenshot.grab(mc.gameDirectory,"casino_"+n+".png",mc.getMainRenderTarget(),c->System.out.println("[CASINO] "+c.getString()));}
 static void tp(Minecraft mc,double x,double y,double z,float yaw,float pitch){var srv=mc.getSingleplayerServer();srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);p.teleportTo(srv.overworld(),x,gy+y,z,yaw,pitch);});}
 static void open(Minecraft mc,Casino.Game g,int i){var srv=mc.getSingleplayerServer();srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);var pos=new BlockPos(i*2,gy,40);
  l.getBlockState(pos).useWithoutItem(l,p,new BlockHitResult(Vec3.atCenterOf(pos),Direction.SOUTH,pos,false));});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  var srv=mc.getSingleplayerServer();
  if(tick==40)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.CREATIVE);
   gy=l.getHeight(Heightmap.Types.MOTION_BLOCKING,0,40);
   var games=Casino.Game.values();
   for(int i=0;i<games.length;i++){var pos=new BlockPos(i*2,gy,40);var st=Casino.BLOCKS.get(games[i]).get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,Direction.SOUTH);
    l.setBlock(pos,st,3);br.mareeletrica.Multiblock.place(l,pos,st);}
   p.getInventory().add(new net.minecraft.world.item.ItemStack(Casino.CHIP.get(),64));p.getInventory().add(new net.minecraft.world.item.ItemStack(Casino.CHIP.get(),64));
   
   FieldKits.buildAt(l,new BlockPos(0,gy-1,60),Direction.SOUTH,FieldKits.Kind.TENIS);
   FieldKits.buildAt(l,new BlockPos(90,gy-1,60),Direction.SOUTH,FieldKits.Kind.VOLEI);});
  if(tick==60){tick=419;}
  if(tick==120)shot(mc,"machines");
  if(tick==130){open(mc,Casino.Game.ROLETA,1);}
  if(tick==150)PacketDistributor.sendToServer(new Casino.Action(1,1,0));
  if(tick==210)shot(mc,"roleta");
  if(tick==220){mc.setScreen(null);open(mc,Casino.Game.MINES,4);}
  if(tick==240)PacketDistributor.sendToServer(new Casino.Action(1,3,0));
  if(tick==250)PacketDistributor.sendToServer(new Casino.Action(2,12,0));
  if(tick==270)shot(mc,"mines");
  if(tick==280){mc.setScreen(null);open(mc,Casino.Game.PLINKO,3);}
  if(tick==300)PacketDistributor.sendToServer(new Casino.Action(1,2,0));
  if(tick==330)shot(mc,"plinko");
  if(tick==340){mc.setScreen(null);open(mc,Casino.Game.BLACKJACK,2);}
  if(tick==360)PacketDistributor.sendToServer(new Casino.Action(1,0,0));
  if(tick==380)shot(mc,"blackjack");


  if(tick==420){mc.setScreen(null);srv.execute(()->srv.getPlayerList().getPlayers().get(0).setGameMode(GameType.SPECTATOR));tp(mc,0,52,90,0,90);}
  if(tick==520)shot(mc,"tenis");
  if(tick==530)tp(mc,90,80,100,0,90);
  if(tick==610)shot(mc,"basquete_volei");
  if(tick==630){System.out.println("[CASINO] DONE");mc.stop();}
 }
}
