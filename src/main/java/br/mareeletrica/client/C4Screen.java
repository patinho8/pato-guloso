package br.mareeletrica.client;

import br.mareeletrica.C4;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Timer programming screen for the C4. */
public final class C4Screen extends Screen {
 private static final String[] UNITS={"Segundos","Minutos (reais)","Horas (reais)","Dias (do Minecraft)"};
 private static final int[] CODES={3,0,1,2};
 private final BlockPos pos;private int unit=0;private EditBox amount;private Button unitButton;
 public C4Screen(BlockPos pos){super(Component.literal("C4 — programar explosão"));this.pos=pos;}
 @Override protected void init(){
  int cx=width/2,cy=height/2;
  amount=new EditBox(font,cx-40,cy-22,80,20,Component.literal("Tempo"));amount.setMaxLength(4);amount.setFilter(s->s.matches("\\d{0,4}"));amount.setValue("30");addRenderableWidget(amount);setInitialFocus(amount);
  unitButton=addRenderableWidget(Button.builder(Component.literal("Unidade: "+UNITS[unit]),b->{unit=(unit+1)%UNITS.length;b.setMessage(Component.literal("Unidade: "+UNITS[unit]));}).bounds(cx-80,cy+4,160,20).build());
  addRenderableWidget(Button.builder(Component.literal("ARMAR C4"),b->{int n=amount.getValue().isEmpty()?0:Integer.parseInt(amount.getValue());if(n>0){PacketDistributor.sendToServer(new C4.Arm(pos,n,CODES[unit]));onClose();}}).bounds(cx-80,cy+30,78,20).build());
  addRenderableWidget(Button.builder(Component.literal("Cancelar"),b->onClose()).bounds(cx+2,cy+30,78,20).build());
 }
 @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
  super.render(g,mouseX,mouseY,partial);
  g.drawCenteredString(font,title,width/2,height/2-52,0xFF5BE05B);
  g.drawCenteredString(font,Component.literal("Explodir daqui a:"),width/2,height/2-36,0xFFFFFFFF);
 }
 @Override public boolean isPauseScreen(){return false;}
}
