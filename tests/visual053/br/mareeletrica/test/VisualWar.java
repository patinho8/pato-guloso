package br.mareeletrica.test;

import br.mareeletrica.WarPlus;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Visual check (test only, -Dmareeletrica.war): RPG firing while aiming, B-2 cockpit, far F5 camera on the jets. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualWar {
 static int tick=-1,gy=-60;
 static boolean on(){return Boolean.getBoolean("mareeletrica.war");}
 static void shot(Minecraft mc,String n){Screenshot.grab(mc.gameDirectory,"war_"+n+".png",mc.getMainRenderTarget(),c->System.out.println("[WAR] "+c.getString()));}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(tick==40)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(GameType.CREATIVE);
   p.teleportTo(l,20.5,gy,0.5,0,-4);p.getInventory().selected=0;p.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(WarPlus.LAUNCHER.get()));});
  if(tick==100)mc.options.keyUse.setDown(true);
  if(tick==120)KeyMapping.click(mc.options.keyAttack.getKey());
  if(tick==119||tick==122||tick==130)System.out.println("[WAR] pitch "+tick+" "+mc.player.getXRot()+" pos "+mc.player.position());
  if(tick==121)shot(mc,"rpg_recoil");
  if(tick==124)shot(mc,"rpg_fire_aiming");
  if(tick==134)shot(mc,"rpg_fire_aiming2");
  if(tick==140){mc.options.keyUse.setDown(false);srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);
   var b2=br.mareeletrica.Jets.B2.get().create(l);b2.setPos(40.5,gy,10.5);b2.setHeading(0);l.addFreshEntity(b2);p.startRiding(b2,true);});}
  if(tick==200)shot(mc,"b2_cockpit");
  if(tick==205)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
  if(tick==240)shot(mc,"b2_f5");
  if(tick==245)srv.execute(()->{var l=srv.overworld();var p=srv.getPlayerList().getPlayers().get(0);p.stopRiding();
   var j=br.mareeletrica.Jets.JET.get().create(l);j.setPos(70.5,gy,10.5);j.setHeading(0);l.addFreshEntity(j);p.startRiding(j,true);});
  if(tick==290)shot(mc,"jet_f5");
  if(tick==300){System.out.println("[WAR] DONE");mc.stop();}
 }
}
