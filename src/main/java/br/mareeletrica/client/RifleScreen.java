package br.mareeletrica.client;
import br.mareeletrica.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class RifleScreen extends AbstractContainerScreen<RifleMenu> {
 private static final String[] LABELS={"Mira","Silenc.","Bipé","Adesivo","Pente"};
 public RifleScreen(RifleMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=380;imageHeight=240;}
 @Override protected void init(){super.init();for(int f=0;f<3;f++){final int field=f;int x=leftPos+20+f*78;addRenderableWidget(Button.builder(Component.literal("−"),b->send(field*2)).bounds(x,topPos+113,22,18).build());addRenderableWidget(Button.builder(Component.literal("+"),b->send(field*2+1)).bounds(x+46,topPos+113,22,18).build());}addRenderableWidget(Button.builder(Component.literal("Zerar ajustes"),b->send(6)).bounds(leftPos+253,topPos+213,115,18).build());}
 private void send(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 public net.minecraft.world.item.ItemStack previewStack(){return menu.getSlot(32+menu.heldSlot).getItem();}
 @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF131B23);g.fill(leftPos+3,topPos+3,leftPos+imageWidth-3,topPos+27,0xFF344955);g.fill(leftPos+244,topPos+32,leftPos+245,topPos+233,0xFF4A5C68);for(var s:menu.slots){g.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xFF718391);g.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xFF070D12);}g.pose().pushPose();g.pose().translate(leftPos+263,topPos+41,0);g.pose().scale(5,5,1);g.renderItem(previewStack(),0,0);g.pose().popPose();}
 @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,"OFICINA DA SNIPER",12,10,0xF1F4F6,false);for(int i=0;i<5;i++)g.drawCenteredString(font,LABELS[i],28+i*48,39,0xCEDBE3);g.drawString(font,"CALIBRAÇÃO",20,82,0x8DD8D3,false);String[] names={"Distância","Horizontal","Vertical"};for(int i=0;i<3;i++){g.drawCenteredString(font,names[i],54+i*78,98,0xABBAC6);g.drawCenteredString(font,Integer.toString(menu.calibration.get(i)),54+i*78,119,0xFFFFFF);}g.drawString(font,"Inventário",20,145,0xAEBBC4,false);
  int yy=130;for(String line:new String[]{"M2: mirar · M1: tiro","J: abrir este painel","Shift 2×: deitar","Bipé: deite e pare","Cada mira nova: zero","Distância em blocos","H/V: passos de 0,02°"}){g.drawString(font,line,253,yy,0xB8C7D0,false);yy+=11;}}
 @Override public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
}
