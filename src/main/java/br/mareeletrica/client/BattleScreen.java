package br.mareeletrica.client;

import br.mareeletrica.ChickenBattle;
import br.mareeletrica.ChickenTypes.Breed;
import br.mareeletrica.ChickenTypes.Move;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Pokémon-style battle panel: both roosters with health bars, the battle log and your four moves. */
public final class BattleScreen extends Screen {
 private final BlockPos pos;private CompoundTag data;
 public BattleScreen(BlockPos pos,CompoundTag d){super(Component.literal("Batalha de Galos"));this.pos=pos;this.data=d;}
 public BlockPos pos(){return pos;}
 public void update(CompoundTag d){if(d.getInt("Phase")==0){onClose();return;}data=d;rebuild();}
 int left(){return width/2-180;}int top(){return height-150;}
 @Override protected void init(){rebuild();}
 boolean myTurn(){return data.getInt("Phase")==2&&data.contains("Me")&&!data.getCompound("Me").getBoolean("Chosen");}
 void rebuild(){
  clearWidgets();int x=left(),y=top();
  var me=data.getCompound("Me");var moves=me.getList("Moves",8);
  for(int i=0;i<moves.size();i++){
   Move m;try{m=Move.valueOf(moves.getString(i));}catch(IllegalArgumentException ex){continue;}
   final int k=i;var mm=m;
   var b=Button.builder(Component.literal(m.label).withColor(m.type.color),bt->PacketDistributor.sendToServer(new ChickenBattle.Choose(pos,k)))
    .bounds(x+4+(i%2)*124,y+82+(i/2)*24,120,20)
    .tooltip(Tooltip.create(Component.literal(mm.label+"\nTipo: "+mm.type.label+(mm.power>0?"\nPoder: "+mm.power:"\nGolpe de efeito")+"\nPrecisão: "+mm.accuracy+"%\n"+mm.desc))).build();
   b.active=myTurn();addRenderableWidget(b);
  }
  var flee=Button.builder(Component.literal("§cDesistir"),bt->PacketDistributor.sendToServer(new ChickenBattle.Choose(pos,9))).bounds(x+256,y+106,100,20).build();
  flee.active=data.getInt("Phase")==2;addRenderableWidget(flee);
  addRenderableWidget(Button.builder(Component.literal("Fechar"),bt->onClose()).bounds(x+256,y+82,100,20).build());
 }
 void card(GuiGraphics g,CompoundTag f,int x,int y,boolean mine){
  if(f.isEmpty())return;
  var br=Breed.of(f.getInt("Breed"));int w=170;
  g.fill(x,y,x+w,y+40,0xE0101418);g.fill(x,y,x+3,y+40,br.type.color);
  g.drawString(font,br.label+" §7Nv."+f.getInt("Lvl"),x+7,y+4,0xFFFFFFFF,false);
  String tp=br.type.label;g.drawString(font,tp,x+w-6-font.width(tp),y+4,br.type.color,false);
  g.drawString(font,"§7de "+f.getString("Owner"),x+7,y+14,0xFFAAAAAA,false);
  int hp=f.getInt("Hp"),max=Math.max(1,f.getInt("Max"));float fr=hp/(float)max;
  int col=fr>.5F?0xFF40D040:fr>.25F?0xFFE0C020:0xFFE03030;
  g.fill(x+7,y+26,x+w-50,y+32,0xFF303030);g.fill(x+7,y+26,x+7+(int)((w-57)*fr),y+32,col);
  g.drawString(font,hp+"/"+max,x+w-46,y+25,0xFFFFFFFF,false);
  String st=switch(f.getInt("Status")){case 1->"§6QUEIMADO";case 2->"§ePARALISADO";case 3->"§bCONGELADO";default->"";};
  var bf=new StringBuilder();
  for(var e:new String[][]{{"Atk","ATQ"},{"Def","DEF"},{"Spd","VEL"}}){int v=f.getInt(e[0]);if(v!=0)bf.append(v>0?" §a":" §c").append(e[1]).append(v>0?"+":"").append(v);}
  g.drawString(font,st+bf,x+7,y+34-1,0xFFFFFFFF,false);
  if(f.getBoolean("Chosen")&&data.getInt("Phase")==2)g.drawString(font,"§a✔ escolheu",x+w-60,y+14,0xFFFFFFFF,false);
 }
 @Override public void render(GuiGraphics g,int mx,int my,float pt){
  int x=left(),y=top();
  card(g,data.getCompound("Foe"),width/2+10,16,false);
  card(g,data.getCompound("Me"),x,y,true);
  g.fill(x,y+44,x+360,y+78,0xD0000000);
  var log=data.getList("Log",8);int n=log.size();
  for(int i=Math.max(0,n-3);i<n;i++)g.drawString(font,log.getString(i),x+5,y+47+(i-Math.max(0,n-3))*10,0xFFFFFFFF,false);
  String head=switch(data.getInt("Phase")){
   case 1->"Esperando o adversário...";
   case 2->myTurn()?"§eTurno "+data.getInt("Turn")+" — escolha o golpe! §7("+data.getInt("Timer")+"s)":"§7Esperando o adversário escolher... ("+data.getInt("Timer")+"s)";
   case 3->"§fA batalha está rolando...";
   case 4->"§6🏆 "+data.getString("Result");
   default->"";};
  g.drawString(font,head,x+180,y+4,0xFFFFFFFF,true);
  super.render(g,mx,my,pt);
 }
 @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){}
 @Override public boolean isPauseScreen(){return false;}
}
