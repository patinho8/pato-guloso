package br.mareeletrica.client;

import br.mareeletrica.Garage;
import br.mareeletrica.Vehicles;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Garage console: stored vehicles (take out), vehicles parked on the pad (store, paint). */
public final class GarageScreen extends Screen {
 private static final int[] SWATCH={0xFF5C6838,0xFFC8A870,0xFF3A5A2A,0xFFE8ECEE,0xFF26282C,0xFF808890,0xFF2C3E62};
 private final BlockPos pos;private CompoundTag data;
 public GarageScreen(BlockPos pos,CompoundTag d){super(Component.literal("Garagem"));this.pos=pos;this.data=d;}
 public BlockPos pos(){return pos;}
 public void update(CompoundTag d){data=d;rebuild();}
 private int left(){return width/2-160;}private int top(){return height/2-100;}
 private void send(int a,int i,int arg){PacketDistributor.sendToServer(new Garage.Action(pos,a,i,arg));}
 @Override protected void init(){rebuild();}
 private void rebuild(){
  clearWidgets();int x=left(),y=top();
  var stored=data.getList("Stored",10);
  for(int i=0;i<stored.size();i++){final int k=i;addRenderableWidget(Button.builder(Component.literal("Tirar"),b->send(2,k,0)).bounds(x+100,y+30+i*22,50,18).build());}
  var parked=data.getList("Parked",10);
  for(int i=0;i<parked.size()&&i<3;i++){var v=parked.getCompound(i);final int id=v.getInt("Id");int ry=y+30+i*62;
   addRenderableWidget(Button.builder(Component.literal("Guardar"),b->send(1,id,0)).bounds(x+256,ry,60,18).build());
   for(int c=0;c<Vehicles.Base.PAINTS.length;c++){final int cc=c;addRenderableWidget(Button.builder(Component.literal(c==v.getInt("Paint")?"●":""),b->send(3,id,cc)).bounds(x+172+c*20,ry+40,18,14).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(Vehicles.Base.PAINTS[cc]))).build());}
  }
 }
 /** 1.21 blurs the background inside super.render(), i.e. on top of what was already drawn: that made this screen blurry. Plain dim instead. */
 @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){}
 @Override public void render(GuiGraphics g,int mx,int my,float pt){
  g.fill(0,0,width,height,0x90000000);
  int x=left(),y=top(),w=320,h=200;
  g.fill(x-2,y-2,x+w+2,y+h+2,0xFF3A3020);g.fill(x,y,x+w,y+h,0xF0141210);
  g.fill(x,y,x+w,y+20,0xFF4A3A18);g.drawString(font,"GARAGEM DE VEÍCULOS",x+8,y+6,0xFFFFD070,false);
  g.drawString(font,"Guardados ("+data.getList("Stored",10).size()+"/"+Garage.SLOTS+")",x+8,y+20+2,0xFFB0B0B0,false);
  var stored=data.getList("Stored",10);
  for(int i=0;i<stored.size();i++){var v=stored.getCompound(i);g.drawString(font,v.getString("Name"),x+8,y+31+i*22,0xFFFFFFFF,false);g.drawString(font,"casco "+v.getInt("Health")+"% • comb. "+v.getInt("Fuel")+"%",x+8,y+41+i*22,0xFF909090,false);}
  if(stored.isEmpty())g.drawString(font,"vazia",x+8,y+32,0xFF707070,false);
  g.fill(x+160,y+22,x+161,y+h-4,0xFF3A3020);
  g.drawString(font,"No pátio (em frente)",x+168,y+20+2,0xFFB0B0B0,false);
  var parked=data.getList("Parked",10);
  for(int i=0;i<parked.size()&&i<3;i++){var v=parked.getCompound(i);int ry=y+30+i*62;
   g.drawString(font,v.getString("Name"),x+168,ry+4,0xFFFFFFFF,false);
   g.drawString(font,"casco "+v.getInt("Health")+"% • comb. "+v.getInt("Fuel")+"%",x+168,ry+16,0xFFB0B0B0,false);
   g.drawString(font,"blind. "+v.getInt("A")+" • motor "+v.getInt("E")+" • munição "+v.getInt("M"),x+168,ry+27,0xFF90C0FF,false);
   for(int c=0;c<SWATCH.length;c++)g.fill(x+174+c*20,ry+52,x+188+c*20,ry+55,SWATCH[c]);
  }
  if(parked.isEmpty()){g.drawString(font,"Estacione um helicóptero ou",x+168,y+34,0xFF707070,false);g.drawString(font,"tanque vazio no pátio 7x7",x+168,y+45,0xFF707070,false);g.drawString(font,"à frente da garagem.",x+168,y+56,0xFF707070,false);}
  g.drawString(font,"Kits de melhoria e galão: clique no veículo com eles.",x+8,y+h-12,0xFF808080,false);
  super.render(g,mx,my,pt);
 }
 @Override public boolean isPauseScreen(){return false;}
}
