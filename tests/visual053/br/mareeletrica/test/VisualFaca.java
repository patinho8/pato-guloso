package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.faca): every knife swings 3 times in a row (1st, 2nd, finisher), screenshot mid-swing. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualFaca {
 static int tick;
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"faca_"+n+".png",mc.getMainRenderTarget(),c->{});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.faca"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(tick==50&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60){mc.gui.getChat().clearMessages(false);mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);p.serverLevel().setDayTime(6000);for(var m:p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,p.getBoundingBox().inflate(60)))m.discard();p.setXRot(10);});}
  String[] ids={"karambit","xeno_hunter","hrt3_knife","butterfly_knife","banana"};
  int t=tick-80;if(t<0)return;int k=t/60,r=t%60;if(k>=ids.length){if(r==0)System.out.println("[FACA] DONE");return;}
  String id=ids[k];
  if(r==0){var it=switch(id){case "karambit"->Knives.KARAMBIT.get();case "xeno_hunter"->Knives.XENO.get();case "hrt3_knife"->Knives.HRT3.get();case "butterfly_knife"->Knives.BUTTERFLY.get();default->Knives.BANANA.get();};
   mc.player.getInventory().setItem(mc.player.getInventory().selected,new ItemStack(it));}
  for(int h=0;h<3;h++){if(r==20+h*12)mc.player.swing(InteractionHand.MAIN_HAND);if(r==20+h*12+4)shot(id+"_"+(h+1));}
 }
}
