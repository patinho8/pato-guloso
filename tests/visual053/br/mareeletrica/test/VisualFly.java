package br.mareeletrica.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.fly): first-person views from the helicopter, the fighter jet and the B-2 (ahead, to the side, down). */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualFly {
 static int tick;
 static boolean on(){return Boolean.getBoolean("mareeletrica.fly");}
 static void shot(Minecraft mc,String n){Screenshot.grab(mc.gameDirectory,"fly_"+n+".png",mc.getMainRenderTarget(),c->System.out.println("[FLY] "+c.getString()));}
 static void look(Minecraft mc,float yaw,float pitch){mc.player.setYRot(yaw);mc.player.setXRot(pitch);mc.player.yRotO=yaw;mc.player.xRotO=pitch;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  String[] kinds={"heli","jet","b2"};
  for(int k=0;k<3;k++){int base=40+k*160;final int kk=k;
   if(tick==base)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.CREATIVE);p.stopRiding();
    net.minecraft.world.entity.Entity v=switch(kk){case 0->br.mareeletrica.Vehicles.HELI.get().create(l);case 1->br.mareeletrica.Jets.JET.get().create(l);default->br.mareeletrica.Jets.B2.get().create(l);};
    v.moveTo(10.5+kk*30,-60,10.5,180,0);if(v instanceof br.mareeletrica.Jets.JetEntity j)j.setHeading(180);l.addFreshEntity(v);p.startRiding(v,true);});
   if(tick==base+60)look(mc,180,8);
   if(tick==base+80)shot(mc,kinds[k]+"_frente");
   if(tick==base+85){if(k>0){br.mareeletrica.client.JetClient.LOOK.setDown(true);mc.player.setYRot(mc.player.getYRot()-80);mc.player.setXRot(15);}else look(mc,180-75,12);}
   if(tick==base+105)shot(mc,kinds[k]+"_lado");
   if(tick==base+110){if(k>0){mc.player.setYRot(mc.player.getYRot()+80);mc.player.setXRot(mc.player.getXRot()+50);}else look(mc,180,55);}
   if(tick==base+135)br.mareeletrica.client.JetClient.LOOK.setDown(false);
   if(tick==base+130)shot(mc,kinds[k]+"_baixo");
  }
  if(tick==40+3*160){System.out.println("[FLY] DONE");mc.stop();}
 }
}
