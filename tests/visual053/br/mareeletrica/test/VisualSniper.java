package br.mareeletrica.test;

import br.mareeletrica.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.sniper, -Dmareeletrica.sniper_tag=name): sniper model in hand, third person and hotbar. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualSniper {
 static int tick;
 static ItemStack sn(int skin){var s=new ItemStack(MareEletrica.SNIPER.get());var t=RifleLoadout.tag(s);t.putInt("Sticker",skin);s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return s;}
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,System.getProperty("mareeletrica.sniper_tag","x")+"_"+n+".png",mc.getMainRenderTarget(),c->{});}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.sniper"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;var srv=mc.getSingleplayerServer();
  if(mc.screen!=null&&tick>20)mc.setScreen(null);
  if(tick==40&&mc.player.isDeadOrDying())mc.player.respawn();
  if(tick==60)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);var l=p.serverLevel();
   var at=p.blockPosition().atY(150);for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){l.setBlockAndUpdate(at.offset(x,-1,z),net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<3;y++)l.setBlockAndUpdate(at.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
   p.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+.5,0,5);p.getInventory().clearContent();
   int[] sk={0,6,1,2,7,8,11,12,9};for(int i=0;i<9;i++)p.getInventory().setItem(i,sn(sk[i]));
   l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);});
  if(tick>=62&&tick%10==0)srv.execute(()->{var p=srv.getPlayerList().getPlayers().get(0);for(var en:p.serverLevel().getEntities(p,p.getBoundingBox().inflate(40),x->!(x instanceof net.minecraft.world.entity.player.Player)))en.discard();});
  if(tick==85)shot("mao");
  if(tick==88){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.player.setYRot(90);}
  if(tick==100)shot("lado");
  if(tick==102)mc.player.setYRot(30);
  if(tick==112)shot("frente");
  if(tick==114)mc.player.setYRot(150);
  if(tick==124)shot("tras");
  if(tick==126){mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setYRot(0);mc.player.getInventory().selected=1;}
  if(tick==138)shot("cacador_mao");
  if(tick==140){mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.player.setYRot(150);}
  if(tick==152)shot("cacador_tras");
  if(tick==154){mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setYRot(0);mc.player.getInventory().selected=0;}
  if(tick==160)mc.options.keyUse.setDown(true);
  if(tick==185)shot("luneta");
  if(tick==187)mc.options.keyUse.setDown(false);
  if(tick==195){mc.player.getInventory().selected=4;}
  if(tick==205)shot("tigre_mao");
  if(tick==210)System.out.println("[SN] DONE");
 }
}
