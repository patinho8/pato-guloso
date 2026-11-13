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

/** Test only (-Dmareeletrica.maquinas): big machines, compact fallback, giant wheel with crank and cranker. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualMaquinas {
 static int tick;static BlockPos o,wheel,cranker;
 static boolean on(){return Boolean.getBoolean("mareeletrica.maquinas");}
 static void log(String s){System.out.println("[MAQ] "+s);}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"maq_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static String place(Kind k,BlockPos at){var p=sp();var it=new ItemStack(MareEletrica.MACHINES.get(k).get());p.setItemInHand(InteractionHand.MAIN_HAND,it.copy());var below=at.below();
  var r=it.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(below).add(0,.5,0),Direction.UP,below,false)));var st=p.level().getBlockState(at);
  return k+" "+r+" "+st.getBlock()+(st.hasProperty(MachineBlock.EXPANDED)?" expanded="+st.getValue(MachineBlock.EXPANDED)+" facing="+st.getValue(MachineBlock.FACING):"");}
 static MachineEntity m(BlockPos p){return sp().level().getBlockEntity(p) instanceof MachineEntity me?me:null;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=sp();var l=p.serverLevel();p.setGameMode(GameType.CREATIVE);o=p.blockPosition().atY(150);l.setDayTime(6000);l.setWeatherParameters(99999,0,false,false);
   for(int x=-18;x<=18;x++)for(int z=-22;z<=10;z++){l.setBlock(o.offset(x,-1,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);for(int y=0;y<12;y++)l.setBlock(o.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);}
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+8.5,180,0);
   // player looks north => machines face south toward the player? (facing = opposite of look)
   log(place(Kind.CRUSHER,o.offset(-12,0,-4)));
   log(place(Kind.FURNACE,o.offset(-8,0,-4)));
   log(place(Kind.HEALER,o.offset(-5,0,-4)));
   log(place(Kind.WEATHER_CONTROLLER,o.offset(-1,0,-5)));
   log(place(Kind.ARMOR_STATION,o.offset(3,0,-5)));
   // compact fallback: a furnace in a 1-block gap
   for(int y=0;y<3;y++){l.setBlock(o.offset(9,y,-4),Blocks.STONE.defaultBlockState(),2);l.setBlock(o.offset(8,y,-3),Blocks.STONE.defaultBlockState(),2);}
   log("gap: "+place(Kind.FURNACE,o.offset(8,0,-4)));
   // giant water wheel + big cranker beside it
   wheel=o.offset(-3,0,-16);log("wheel: "+place(Kind.GIANT_WHEEL,wheel));
   cranker=o.offset(0,0,-16);log("cranker: "+place(Kind.AUTO_CRANK,cranker));
   p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);});
  if(tick==70)srv.execute(()->{var w=m(wheel);var c=m(cranker);log("wheel be "+(w!=null)+" cranker be "+(c!=null));
   if(w!=null){var p=sp();var it=new ItemStack(CrankBlocks.HAND_CRANK_ITEM.get());p.setItemInHand(InteractionHand.MAIN_HAND,it.copy());
    log("crank: "+it.useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(wheel),Direction.SOUTH,wheel,false)))+" crank="+w.crank);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);}
   if(c!=null){c.clean.add(60000);c.setChanged();}});
  if(tick>60&&tick<400){mc.player.setYRot(180);mc.player.setXRot(8);}
  if(tick==100)mc.options.hideGui=true;
  if(tick==120)shot("frente");
  if(tick==130)srv.execute(()->{var p=sp();p.teleportTo(p.serverLevel(),o.getX()-1.5,o.getY()+3,o.getZ()-6.5,180,20);});
  if(tick>130&&tick<200){mc.player.setYRot(180);mc.player.setXRot(15);}
  if(tick==180){shot("roda");srv.execute(()->{MachineEntity w=m(wheel),c=m(cranker);log("wheel drive "+(w==null?-1:w.drive)+" status "+(w==null?-1:w.status)+" rate "+(w==null?-1:w.lastRate)+" | cranker status "+(c==null?-1:c.status)+" energy "+(c==null?-1:c.clean.stored()));});}
  if(tick==185)srv.execute(()->{var w=m(wheel);log("integrity cranker "+(m(cranker)==null?-1:LargeMachineStructure.integrity(m(cranker)))+" crusher "+LargeMachineStructure.integrity(m(o.offset(-12,0,-4))));});
  if(tick==190){mc.options.hideGui=false;log("DONE");}
 }
}
