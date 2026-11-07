package br.mareeletrica.client;
import br.mareeletrica.DmtSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
@EventBusSubscriber(modid=br.mareeletrica.MareEletrica.ID,value=Dist.CLIENT)
public final class DmtInventoryView {
 @SubscribeEvent public static void render(ScreenEvent.Render.Post e){
  if(!(e.getScreen() instanceof InventoryScreen||e.getScreen() instanceof CreativeModeInventoryScreen))return;
  var screen=(AbstractContainerScreen<?>)e.getScreen();var g=e.getGuiGraphics();var font=Minecraft.getInstance().font;
  for(var s:screen.getMenu().slots)if(s.container==DmtSlot.MARKER){
   int x=screen.getGuiLeft()+s.x,y=screen.getGuiTop()+s.y;
   g.fill(x-1,y-1,x+17,y,0xFF373737);g.fill(x-1,y,x,y+17,0xFF373737);g.fill(x,y+16,x+17,y+17,0xFFFFFFFF);g.fill(x+16,y,x+17,y+16,0xFFFFFFFF);
   if(!s.hasItem()){g.fill(x,y,x+16,y+16,0xFF8B8B8B);g.drawString(font,"D",x+5,y+4,0xFF56686C,false);}

   if(!s.hasItem()&&e.getMouseX()>=x&&e.getMouseX()<x+16&&e.getMouseY()>=y&&e.getMouseY()<y+16)g.renderTooltip(font,Component.translatable("gui.mareeletrica.dmt_slot"),e.getMouseX(),e.getMouseY());
  }
 }
}
