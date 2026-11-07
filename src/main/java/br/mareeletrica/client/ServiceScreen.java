package br.mareeletrica.client;
import br.mareeletrica.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class ServiceScreen extends AbstractContainerScreen<ServiceMenu>{
 private Button giant;public ServiceScreen(ServiceMenu m,Inventory inv,Component t){super(m,inv,t);imageWidth=176;imageHeight=232;}
 protected void init(){super.init();addRenderableWidget(Button.builder(Component.literal("Ligar / pausar"),b->send(0)).bounds(leftPos+8,topPos+24,160,18).build());giant=addRenderableWidget(Button.builder(Component.literal("Aranha gigante: 20 gás"),b->send(1)).bounds(leftPos+8,topPos+88,160,18).build());}
 private void send(int id){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 protected void containerTick(){super.containerTick();giant.visible=menu.kind()==Kind.EXTERMINATOR;}
 protected void renderBg(GuiGraphics g,float d,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF102C36);for(var s:menu.slots)if(s.isActive()){g.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xFF63818A);g.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xFF061B22);}}
 protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,8,7,0xD9FFFF,false);g.drawString(font,menu.data.get(0)+" FE",8,16,0x6DF7C1,false);g.drawString(font,"Gás",8,57,0x91D9FF,false);if(menu.kind()==Kind.QUARRY)g.drawString(font,"Leva: "+(menu.data.get(30)-menu.data.get(29))+" / "+menu.data.get(30),8,96,0xDAEDF2,false);g.drawString(font,font.plainSubstrByWidth(Component.translatable("status.mareeletrica."+menu.data.get(7)).getString(),160),8,132,0xB1DFDB,false);}
 public void render(GuiGraphics g,int x,int y,float d){super.render(g,x,y,d);renderTooltip(g,x,y);}
}
