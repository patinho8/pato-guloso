package br.mareeletrica.client;
import br.mareeletrica.ExplorerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class ExplorerScreen extends AbstractContainerScreen<ExplorerMenu>{
 public ExplorerScreen(ExplorerMenu m,Inventory inv,Component t){super(m,inv,t);imageWidth=176;imageHeight=181;}
 protected void init(){super.init();addRenderableWidget(Button.builder(Component.literal("Retirar balas do carregador"),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);}).bounds(leftPos+8,topPos+75,160,18).build());}
 protected void renderBg(GuiGraphics g,float d,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF211B38);for(var s:menu.slots){g.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xFF8F6BBC);g.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xFF100E21);}}
 protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,"Explorador · compartimento",8,7,0xEDDAFF,false);g.drawString(font,"Bateria: "+menu.data.get(0)/1000+"% | Balas: "+menu.data.get(1),8,19,0xB1DCFF,false);g.drawString(font,"Gás ativo: "+menu.data.get(2)/20+" s",8,30,0x94F8D3,false);g.drawString(font,"Balas",17,43,0xD9E5F1,false);g.drawString(font,"Gás",51,43,0xD9E5F1,false);g.drawString(font,"Carga livre",87,43,0xD9E5F1,false);}
 public void render(GuiGraphics g,int x,int y,float d){super.render(g,x,y,d);renderTooltip(g,x,y);}
}
