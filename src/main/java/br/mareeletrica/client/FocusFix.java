package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Turns off "pause on lost focus" once per session, so Win+Shift+S (Snipping Tool) does not open the pause menu or blur the screen.
 *  The option can still be turned back on in the game's settings afterwards. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class FocusFix {
 private static boolean done=false;
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(done)return;
  var mc=Minecraft.getInstance();if(mc.options==null)return;
  mc.options.pauseOnLostFocus=false;mc.options.save();done=true;
 }
}
