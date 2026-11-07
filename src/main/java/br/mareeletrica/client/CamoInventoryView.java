package br.mareeletrica.client;

import br.mareeletrica.CamoSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Draws the four camouflage slots as a column on the right edge of the inventory. */
@EventBusSubscriber(modid=br.mareeletrica.MareEletrica.ID,value=Dist.CLIENT)
public final class CamoInventoryView {
 private static final String[] LETTERS={"C","J","P","B"};
 private static final String[] NAMES={"Capuz camuflado","Jaqueta camuflada","Calça camuflada","Botas camufladas"};
 @SubscribeEvent public static void render(ScreenEvent.Render.Pre e){
  if(!(e.getScreen() instanceof InventoryScreen||e.getScreen() instanceof CreativeModeInventoryScreen))return;
  var screen=(AbstractContainerScreen<?>)e.getScreen();var g=e.getGuiGraphics();
  for(var s:screen.getMenu().slots)for(int i=0;i<4;i++)if(s.container==CamoSlot.MARKERS[i]&&s.x>=0){
   int x=screen.getGuiLeft()+s.x,y=screen.getGuiTop()+s.y;
   g.fill(x-2,y-2,x+18,y+18,0xFFC6C6C6);
   g.fill(x-1,y-1,x+17,y,0xFF373737);g.fill(x-1,y,x,y+17,0xFF373737);g.fill(x,y+16,x+17,y+17,0xFFFFFFFF);g.fill(x+16,y,x+17,y+16,0xFFFFFFFF);
   g.fill(x,y,x+16,y+16,0xFF8B8B8B);
  }
 }
 @SubscribeEvent public static void labels(ScreenEvent.Render.Post e){
  if(!(e.getScreen() instanceof InventoryScreen||e.getScreen() instanceof CreativeModeInventoryScreen))return;
  var screen=(AbstractContainerScreen<?>)e.getScreen();var g=e.getGuiGraphics();var font=Minecraft.getInstance().font;
  for(var s:screen.getMenu().slots)for(int i=0;i<4;i++)if(s.container==CamoSlot.MARKERS[i]&&s.x>=0){
   int x=screen.getGuiLeft()+s.x,y=screen.getGuiTop()+s.y;
   if(!s.hasItem())g.drawString(font,LETTERS[i],x+5,y+4,0xFF56686C,false);
   if(!s.hasItem()&&e.getMouseX()>=x&&e.getMouseX()<x+16&&e.getMouseY()>=y&&e.getMouseY()<y+16)g.renderTooltip(font,Component.literal(NAMES[i]),e.getMouseX(),e.getMouseY());
  }
 }
}
