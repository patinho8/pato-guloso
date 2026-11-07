package br.mareeletrica.client;

import br.mareeletrica.MareEletrica;
import br.mareeletrica.PowerArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Charge of the worn power suit, bottom-left. */
@EventBusSubscriber(modid=MareEletrica.ID,value=Dist.CLIENT)
public final class PowerArmorHud {
 private PowerArmorHud(){}
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.options.hideGui)return;
  var suit=PowerArmor.fullSet(p);if(suit==null)return;
  int pct=PowerArmor.charge(p.getItemBySlot(EquipmentSlot.CHEST))*100/PowerArmor.MAX_CHARGE;
  var g=e.getGuiGraphics();int x=8,y=g.guiHeight()-30;int col=pct>20?0xFF3FD0FF:(p.tickCount/8)%2==0?0xFFFF5050:0xFF802020;
  g.drawString(mc.font,suit.title+"  "+pct+"%",x,y,col,true);
  g.fill(x,y+10,x+80,y+14,0x90000000);g.fill(x+1,y+11,x+1+78*pct/100,y+13,col);
 }
}
