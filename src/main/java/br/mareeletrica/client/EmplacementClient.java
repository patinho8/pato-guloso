package br.mareeletrica.client;

import br.mareeletrica.*;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Sitting on a turret or cannon: the attack button fires it. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class EmplacementClient {
 private static boolean held;
 private EmplacementClient(){}
 private static Emplacements.Mounted seat(){var p=Minecraft.getInstance().player;return p!=null&&p.getVehicle() instanceof Emplacements.Mounted m?m:null;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();var m=seat();
  boolean down=m!=null&&mc.screen==null&&mc.options.keyAttack.isDown();
  if(down!=held||down&&mc.player.tickCount%3==0)PacketDistributor.sendToServer(new Emplacements.Net.Trigger(down));
  held=down;
 }
 @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered e){if(seat()!=null){e.setCanceled(true);e.setSwingHand(false);}}
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  var m=seat();var mc=Minecraft.getInstance();if(m==null||mc.options.hideGui)return;var g=e.getGuiGraphics();
  String s=(m.turret?"Metralhadora":"Canhão")+"  •  munição "+m.ammo()+"/"+m.maxAmmo();
  g.drawString(mc.font,s,g.guiWidth()/2-mc.font.width(s)/2,g.guiHeight()-70,m.ammo()>0?0xFFFFE070:0xFFFF5050,true);
 }
}
