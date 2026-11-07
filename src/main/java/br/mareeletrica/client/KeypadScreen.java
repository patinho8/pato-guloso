package br.mareeletrica.client;

import br.mareeletrica.BunkerDoor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Bunker door keypad: 4 to 8 digits, OK sends, C clears. Number keys and Enter work too. */
public final class KeypadScreen extends Screen {
 private final BlockPos pos;private String typed="";
 public KeypadScreen(BlockPos pos){super(Component.literal("Teclado da porta"));this.pos=pos;}
 @Override protected void init(){
  int cx=width/2,top=height/2-50;String[] keys={"1","2","3","4","5","6","7","8","9","C","0","OK"};
  for(int i=0;i<12;i++){String k=keys[i];int x=cx-49+(i%3)*34,y=top+(i/3)*24;addRenderableWidget(Button.builder(Component.literal(k),b->press(k)).bounds(x,y,30,20).build());}
 }
 private void press(String k){
  if(k.equals("C"))typed="";
  else if(k.equals("OK")){if(typed.length()>=4){PacketDistributor.sendToServer(new BunkerDoor.Code(pos,typed));onClose();}}
  else if(typed.length()<8)typed+=k;
 }
 @Override public boolean keyPressed(int key,int scan,int mods){
  if(key>=48&&key<=57){press(String.valueOf(key-48));return true;}
  if(key>=320&&key<=329){press(String.valueOf(key-320));return true;}
  if(key==257||key==335){press("OK");return true;}
  if(key==259){if(!typed.isEmpty())typed=typed.substring(0,typed.length()-1);return true;}
  return super.keyPressed(key,scan,mods);
 }
 @Override public void render(GuiGraphics g,int mx,int my,float pt){
  super.render(g,mx,my,pt);
  int cx=width/2,top=height/2-50;
  g.fill(cx-52,top-34,cx+52,top-8,0xE0101418);g.fill(cx-50,top-32,cx+50,top-10,0xFF0A2A12);
  String shown="*".repeat(typed.length());if(shown.isEmpty())shown="_ _ _ _";
  g.drawCenteredString(font,shown,cx,top-25,0xFF60FF80);
  g.drawCenteredString(font,"Porta blindada — digite a senha (4 a 8 números)",cx,top-48,0xFFFFFFFF);
 }
 @Override public boolean isPauseScreen(){return false;}
}
