package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Flashbang: the whole screen goes white, holds, then fades, while the ears ring. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class FlashClient {
 private static long start=-1;private static float strength;
 public static void flash(float s){
  var mc=Minecraft.getInstance();if(mc.level==null)return;
  strength=Math.max(strength*fade(),Math.min(1F,s));start=mc.level.getGameTime();
  mc.getSoundManager().play(SimpleSoundInstance.forUI(MareEletrica.FLASH_RING.get(),1F,.35F+.65F*strength));
 }
 private static float fade(){
  var mc=Minecraft.getInstance();if(start<0||mc.level==null)return 0;
  float t=(mc.level.getGameTime()-start)/20F,hold=.6F+2.2F*strength,out=2.5F+2F*strength;
  return t<hold?1F:Math.max(0,1-(t-hold)/out);
 }
 @SubscribeEvent public static void draw(RenderGuiEvent.Post e){
  float a=strength*fade();if(a<=0){start=-1;return;}
  var g=e.getGuiGraphics();int alpha=(int)(Math.min(1F,a)*255);
  g.fill(0,0,g.guiWidth(),g.guiHeight(),(alpha<<24)|0xFFFFFF);
 }
}
