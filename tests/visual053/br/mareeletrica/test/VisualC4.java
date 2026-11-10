package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.c4): armed C4 survives a break attempt and an explosion, then gets defused by holding use. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualC4 {
 static int tick;static BlockPos c;
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"c4_"+n+".png",mc.getMainRenderTarget(),x->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 static String st(){var s=sp().level().getBlockState(c);return s.getBlock() instanceof C4.C4Block?(s.getValue(C4.C4Block.ARMED)?"ARMED":"disarmed"):"GONE("+s.getBlock()+")";}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.c4"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(tick==50&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv(()->{var p=sp();var l=p.serverLevel();l.setDayTime(6000);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setInvulnerable(true);
   for(var m:l.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,p.getBoundingBox().inflate(60)))m.discard();
   BlockPos b=new BlockPos(0,200,0);for(int x=-4;x<=4;x++)for(int z=-5;z<=4;z++){l.setBlockAndUpdate(b.offset(x,-1,z),Blocks.STONE.defaultBlockState());for(int y=0;y<4;y++)l.setBlockAndUpdate(b.offset(x,y,z),Blocks.AIR.defaultBlockState());}c=b.offset(0,0,-2);l.setBlockAndUpdate(c.below(),Blocks.STONE.defaultBlockState());
   l.setBlockAndUpdate(c,C4.BLOCK.get().defaultBlockState().setValue(C4.C4Block.FACING,Direction.UP));
   p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+.5,180,32);p.getInventory().setItem(p.getInventory().selected,new ItemStack(Items.SHEARS));
   if(l.getBlockEntity(c) instanceof C4.C4Entity be){be.arm(20*600);l.setBlockAndUpdate(c,l.getBlockState(c).setValue(C4.C4Block.ARMED,true));}});
  if(tick==95){var p=mc.player;p.setYRot(180);p.setXRot(32);}
  if(tick==100)mc.options.keyUse.setDown(true);
  if(tick==140)shot("desarmando");
  if(tick==190)shot("quase");
  if(tick==215){mc.options.keyUse.setDown(false);srv(()->System.out.println("[C4] after defuse "+st()));}
  if(tick==218)shot("desarmada");
  if(tick==120||tick==160){var h=mc.hitResult;System.out.println("[C4] client t"+tick+" hit "+(h==null?null:h.getType())+" "+(h instanceof net.minecraft.world.phys.BlockHitResult b?mc.level.getBlockState(b.getBlockPos()):"")+" use "+mc.options.keyUse.isDown()+" defusing "+br.mareeletrica.client.C4Defuse.active()+" player "+mc.player.position()+" c4 "+c);}
  if(tick==225)srv(()->{var l=sp().serverLevel();l.setBlockAndUpdate(c.below(),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(c,C4.BLOCK.get().defaultBlockState().setValue(C4.C4Block.FACING,Direction.UP));
   if(l.getBlockEntity(c) instanceof C4.C4Entity be){be.arm(20*600);l.setBlockAndUpdate(c,l.getBlockState(c).setValue(C4.C4Block.ARMED,true));}
   boolean broke=sp().gameMode.destroyBlock(c);System.out.println("[C4] break attempt -> "+broke+" state "+st());
   l.explode(null,c.getX()+.5,c.getY()+1.5,c.getZ()+.5,4F,net.minecraft.world.level.Level.ExplosionInteraction.TNT);System.out.println("[C4] after explosion "+st());
   l.setBlockAndUpdate(c.below(),Blocks.AIR.defaultBlockState());System.out.println("[C4] support removed "+st());});
  if(tick==240)System.out.println("[C4] DONE");
 }
}
