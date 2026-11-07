package br.mareeletrica.client;
import br.mareeletrica.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class GunScreen extends AbstractContainerScreen<GunMenu> {
 public GunScreen(GunMenu m,Inventory i,Component t){super(m,i,t);imageWidth=374;imageHeight=208;}
 public net.minecraft.world.item.ItemStack previewStack(){return menu.getSlot(31+menu.heldSlot).getItem();}
 @Override protected void renderBg(GuiGraphics g,float dt,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF111A20);g.fill(leftPos+3,topPos+3,leftPos+imageWidth-3,topPos+27,0xFF294B50);g.fill(leftPos+223,topPos+34,leftPos+224,topPos+196,0xFF46676C);for(var s:menu.slots){g.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xFF658A90);g.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xFF070D12);}g.enableScissor(leftPos+226,topPos+30,leftPos+imageWidth-4,topPos+126);g.pose().pushPose();g.pose().translate(leftPos+243,topPos+40,0);g.pose().scale(5,5,1);g.renderItem(previewStack(),0,0);g.pose().popPose();g.disableScissor();}
 @Override protected void renderLabels(GuiGraphics g,int x,int y){var p=WeaponProfile.of(previewStack());if(p==null)return;g.drawString(font,"OFICINA · "+p.title,12,10,0xF2FFFF,false);String[] names={"Mira","Silenc.","Pintura","Pente"};for(int i=0;i<4;i++)g.drawCenteredString(font,names[i],28+i*54,38,0xDBEEF0);g.drawString(font,"Carregador: "+GunLoadout.rounds(previewStack())+"/"+p.capacity,20,85,0x9FE5D7,false);g.drawString(font,"R troca • M2 no pente: abastecer",20,99,0xAFC5CF,false);g.drawString(font,"Inventário",20,111,0xAFC5CF,false);int yy=129;for(String line:new String[]{"M1: "+(p.automatic?"rajada":"disparar"),"M2 segurar: mirar","R: recarregar","J: acessórios",p.pellets>1?"8 projéteis / cartucho":p.damage+" dano / tiro"}){g.drawString(font,line,234,yy,0xBCD9DC,false);yy+=12;}}
 @Override public void render(GuiGraphics g,int x,int y,float dt){super.render(g,x,y,dt);renderTooltip(g,x,y);}
}
