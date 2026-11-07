package br.mareeletrica.test;

import br.mareeletrica.Robots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.robot): Robô Catador picks items up, presses junk, glows at night, opens its belly. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualRobot {
 static int tick;static Robots.Catador robot;
 static boolean on(){return Boolean.getBoolean("mareeletrica.robot");}
 static void shot(Minecraft mc,String n){Screenshot.grab(mc.gameDirectory,"robot_"+n+".png",mc.getMainRenderTarget(),c->System.out.println("[ROBOT] "+c.getString()));}
 static void log(String s){System.out.println("[ROBOT] "+s);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(tick==40)srv.execute(()->{var l=srv.overworld();l.setDayTime(6000);var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.SURVIVAL);p.teleportTo(l,.5,-60,4.5,180,25);
   robot=Robots.ENTITY.get().create(l);robot.moveTo(.5,-60,1.5,0,0);robot.yBodyRot=0;robot.tame(p);l.addFreshEntity(robot);});
  if(tick==62)srv.execute(()->{var l=srv.overworld();
   Object[][] drops={{Items.DIRT,10,4,-2},{Items.DIRT,8,-3,-3},{Items.COBBLESTONE,12,2,-6},{Items.DIAMOND,3,-4,-6},{Items.ROTTEN_FLESH,5,0,-8}};
   for(var d:drops){var it=new ItemEntity(l,.5+(int)d[2],-60,1.5+(int)d[3],new ItemStack((net.minecraft.world.item.Item)d[0],(int)d[1]));l.addFreshEntity(it);}});
  if(tick==120)shot(mc,"catando");
  if(tick==400)srv.execute(()->{var b=robot.belly;StringBuilder s=new StringBuilder();for(int i=0;i<b.getContainerSize();i++)if(!b.getItem(i).isEmpty())s.append(b.getItem(i).getCount()).append("x").append(b.getItem(i).getItem()).append(" ");
   log("belly: "+s+" | left on ground: "+robot.level().getEntitiesOfClass(ItemEntity.class,robot.getBoundingBox().inflate(20)).size());});
  if(tick==380)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);robot.setOrderedToSit(true);robot.getNavigation().stop();robot.setYRot(0);robot.yBodyRot=0;robot.yHeadRot=0;p.teleportTo(l,robot.getX()+.5,robot.getY()+.1,robot.getZ()+1.7,165,22);});
  if(tick==396)shot(mc,"frente");
  if(tick==398)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.teleportTo(l,robot.getX()+2.2,robot.getY()+.4,robot.getZ()-.6,70,20);});
  if(tick==404)shot(mc,"lado");
  if(tick==405)srv.execute(()->{var l=srv.overworld();l.setDayTime(18000);var p=srv.getPlayerList().getPlayers().get(0);p.teleportTo(l,robot.getX()+.5,robot.getY(),robot.getZ()+2.6,180,30);robot.setOrderedToSit(true);robot.setYRot(0);robot.yBodyRot=0;});
  if(tick==440)shot(mc,"noite");
  if(tick==445)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);p.setShiftKeyDown(false);robot.interact(p,net.minecraft.world.InteractionHand.MAIN_HAND);});
  if(tick==460)shot(mc,"barriga");
  if(tick==470){log("DONE");mc.stop();}
 }
}
