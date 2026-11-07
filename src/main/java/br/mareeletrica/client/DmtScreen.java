package br.mareeletrica.client;
import br.mareeletrica.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class DmtScreen extends AbstractContainerScreen<DmtMenu>{
 private static final String[] LABELS={"Lâminas","Arma","Lanças","Gás","Gancho"};
 public DmtScreen(DmtMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=370;imageHeight=220;}
 @Override protected void init(){super.init();for(int i=0;i<3;i++){final int mode=i+1;addRenderableWidget(Button.builder(Component.literal("Sacar"),b->send(mode)).bounds(leftPos+14+i*60,topPos+78,56,18).build());}addRenderableWidget(Button.builder(Component.literal("Guardar"),b->send(0)).bounds(leftPos+14,topPos+102,86,18).build());addRenderableWidget(Button.builder(Component.literal("Recarregar gás"),b->send(4)).bounds(leftPos+104,topPos+102,92,18).build());}
 private void send(int id){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 @Override protected void renderBg(GuiGraphics g,float partial,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF1C242A);g.fill(leftPos+212,topPos+30,leftPos+213,topPos+imageHeight-5,0xFF47616B);g.fill(leftPos+3,topPos+3,leftPos+imageWidth-3,topPos+27,0xFF6E4B31);for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF9AABAD);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xFF0D1419);}}
 @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY){g.drawString(font,"EQUIPAMENTO DE MANOBRA",13,10,0xFFF1D2,false);for(int i=0;i<5;i++)g.drawCenteredString(font,LABELS[i],32+i*36,34,0xDBE3E5);g.drawString(font,"Gás: "+DmtItem.fuel(DmtItem.equipped(minecraft.player))/20+" s · "+DmtCombat.name(DmtCombat.mode(minecraft.player)),14,66,0xA7DACE,false);g.drawString(font,"Inventário",24,126,0xABB9BB,false);
  int x=218,y=36;g.drawString(font,"COMO USAR",x,y,0xFFE3A6,false);y+=17;
  String[] help={"M2: gancho direito",DmtClient.LEFT.getTranslatedKeyMessage().getString()+" / "+DmtClient.RIGHT.getTranslatedKeyMessage().getString()+": ganchos", "Scroll: regular 0,25 m", "Recolha para puxar mobs leves com acessório.",DmtClient.WEAPON.getTranslatedKeyMessage().getString()+": sacar / guardar", "M1: atacar com as duas lâminas ou disparar", "Espaço: impulso preso a blocos",DmtClient.RELEASE.getTranslatedKeyMessage().getString()+": soltar cabos",DmtClient.REFILL.getTranslatedKeyMessage().getString()+": gás",DmtClient.TOGGLE.getTranslatedKeyMessage().getString()+": ativar / desativar"};
  for(String line:help){g.drawWordWrap(font,Component.literal(line),x,y,140,0xCCD8DC);y+=font.split(Component.literal(line),140).size()*9+4;}
}
 @Override public void render(GuiGraphics g,int x,int y,float partial){super.render(g,x,y,partial);renderTooltip(g,x,y);}
}
