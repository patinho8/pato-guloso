package br.mareeletrica.client;

import br.mareeletrica.BaseControl;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.network.PacketDistributor;

/** Command panel: tabs for overview/energy, radar, base map, cameras, defence and members. Refreshes every second. */
public final class PanelScreen extends Screen {
 private static final String[] TABS={"Resumo","Radar","Mapa","Câmeras","Defesa","Membros"};
 private static int tab;
 private final BlockPos pos;private CompoundTag data;private CompoundTag cams;private int refresh;
 private EditBox name;
 private int[] mapColors;private int mapTick=-1000;
 public PanelScreen(BlockPos pos,CompoundTag data){super(Component.literal("Painel de comando"));this.pos=pos;this.data=data;this.cams=data;}
 public BlockPos pos(){return pos;}
 private String key(){return data.getBoolean("Armed")+","+data.getBoolean("Alarm")+","+data.getInt("Turrets")+","+data.getBoolean("Shield")+","+data.getInt("Doors")+","+data.getList("Members",8);}
 public void update(CompoundTag d,boolean open){String before=key();data=d;if(open)cams=d;if(open||!before.equals(key())){String typed=name==null?"":name.getValue();rebuild();if(name!=null)name.setValue(typed);}}
 private int left(){return width/2-170;}private int top(){return height/2-110;}
 private void send(int a,String arg){PacketDistributor.sendToServer(new BaseControl.PanelAction(pos,a,arg));}
 @Override protected void init(){rebuild();}
 private void rebuild(){
  clearWidgets();int x=left(),y=top();
  for(int i=0;i<TABS.length;i++){final int t=i;var b=Button.builder(Component.literal((tab==i?"§e":"")+TABS[i]),bt->{tab=t;rebuild();}).bounds(x+i*57,y-22,55,18).build();addRenderableWidget(b);}
  int bx=x+12,by=y+30;
  if(tab==3){
   var list=cams.getList("Cameras",10);var c=new ArrayList<CameraView.Cam>();
   for(int i=0;i<list.size();i++){var t=list.getCompound(i);c.add(new CameraView.Cam(BlockPos.of(t.getLong("Pos")),Direction.from3DDataValue(t.getInt("Facing")),"Câmera "+(i+1)));}
   for(int i=0;i<c.size();i++){final int k=i;addRenderableWidget(Button.builder(Component.literal("Ver câmera "+(i+1)),b->{onClose();CameraView.start(c,k);}).bounds(bx+(i%3)*108,by+(i/3)*24,104,20).build());}
  }
  if(tab==4){
   addRenderableWidget(Button.builder(Component.literal("Alarme: "+(data.getBoolean("Armed")?"§aARMADO":"§7desarmado")),b->send(1,"")).bounds(bx,by,160,20).build());
   if(data.getBoolean("Alarm"))addRenderableWidget(Button.builder(Component.literal("§cSilenciar alarme"),b->send(9,"")).bounds(bx+168,by,150,20).build());
   String[] tm={"§7desligadas","só monstros","§cmonstros + intrusos"};
   addRenderableWidget(Button.builder(Component.literal("Torretas: "+tm[data.getInt("Turrets")]),b->send(2,"")).bounds(bx,by+26,160,20).build());
   addRenderableWidget(Button.builder(Component.literal("Escudo: "+(data.getBoolean("Shield")?"§bligado":"§7desligado")),b->send(3,"")).bounds(bx+168,by+26,150,20).build());
   addRenderableWidget(Button.builder(Component.literal("Abrir portões ("+data.getInt("Doors")+")"),b->send(4,"")).bounds(bx,by+52,160,20).build());
   addRenderableWidget(Button.builder(Component.literal("Fechar portões"),b->send(5,"")).bounds(bx+168,by+52,150,20).build());
   addRenderableWidget(Button.builder(Component.literal("Chamar helicóptero ao heliponto"),b->send(6,"")).bounds(bx,by+78,318,20).build());
  }
  if(tab==5){
   name=new EditBox(font,bx,by,200,18,Component.literal("Nome"));name.setMaxLength(16);name.setHint(Component.literal("nome do jogador"));addRenderableWidget(name);
   addRenderableWidget(Button.builder(Component.literal("Adicionar"),b->{if(!name.getValue().isBlank()){send(7,name.getValue());name.setValue("");}}).bounds(bx+206,by-1,90,20).build());
   var m=data.getList("Members",8);
   for(int i=0;i<m.size()&&i<12;i++){final String n=m.getString(i);addRenderableWidget(Button.builder(Component.literal("✕"),b->send(8,n)).bounds(bx+150+(i%2)*160,by+28+(i/2)*20,18,16).build());}
  }
 }
 @Override public void tick(){if(++refresh%20==0)send(0,"");}
 private static String fe(long v){return v>=1_000_000?String.format("%.1fM",v/1e6):v>=1000?String.format("%.1fk",v/1e3):Long.toString(v);}
 /** 1.21 blurs the background inside super.render(), i.e. on top of what was already drawn: that made this screen blurry. Plain dim instead. */
 @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){}
 @Override public void render(GuiGraphics g,int mx,int my,float pt){
  g.fill(0,0,width,height,0x90000000);
  int x=left(),y=top(),w=340,h=230;
  g.fill(x-2,y-2,x+w+2,y+h+2,0xFF2A3640);g.fill(x,y,x+w,y+h,0xF0101820);
  g.fill(x,y,x+w,y+20,0xFF1C3A4A);
  boolean alarm=data.getBoolean("Alarm");
  g.drawString(font,"PAINEL DE COMANDO  •  "+TABS[tab],x+8,y+6,0xFF9CE8FF,false);
  if(alarm&&(Minecraft.getInstance().level.getGameTime()/8)%2==0)g.drawString(font,"⚠ ALARME",x+w-60,y+6,0xFFFF4040,false);
  int tx=x+12,ty=y+30;
  switch(tab){
   case 0->{
    long st=data.getLong("Stored"),max=Math.max(1,data.getLong("Max"));
    g.drawString(font,"Energia guardada nos núcleos: §f"+fe(st)+" / "+fe(max)+" FE",tx,ty,0xFFB0FFB0,false);
    g.fill(tx,ty+12,tx+300,ty+20,0xFF203020);g.fill(tx,ty+12,tx+(int)(300*Math.min(1,(double)st/max)),ty+20,0xFF40D060);
    g.drawString(font,"Geração: §a+"+fe(data.getInt("Gen"))+" FE/s",tx,ty+28,0xFFDDDDDD,false);
    g.drawString(font,"Consumo: §c-"+fe(data.getInt("Use"))+" FE/s",tx+160,ty+28,0xFFDDDDDD,false);
    g.drawString(font,"Máquinas na base: §f"+data.getInt("Machines"),tx,ty+44,0xFFDDDDDD,false);
    g.drawString(font,"Contatos aéreos: §f"+data.getList("Contacts",10).stream().filter(t->((CompoundTag)t).getInt("Kind")==1).count(),tx+160,ty+44,0xFFDDDDDD,false);
    g.drawString(font,"Escudos: §f"+data.getInt("Shields")+"   Helipontos: §f"+data.getInt("Helipads")+"   Portões: §f"+data.getInt("Doors"),tx,ty+60,0xFFDDDDDD,false);
    var bad=data.getList("Bad",10);
    g.drawString(font,bad.isEmpty()?"§aNenhuma máquina com problema":"§cMáquinas com problema ("+(data.getInt("Burnt"))+" pifadas, "+data.getInt("Over")+" em sobretensão):",tx,ty+80,0xFFFFFFFF,false);
    for(int i=0;i<bad.size();i++){var b=bad.getCompound(i);var p=BlockPos.of(b.getLong("Pos"));g.drawString(font,(b.getBoolean("Burnt")?"§c✖ PIFOU ":"§e⚡ sobretensão ")+"§f"+b.getString("Name")+" §7("+p.toShortString()+")",tx+6,ty+94+i*11,0xFFFFFFFF,false);}
   }
   case 1->{
    int cx=x+110,cy=y+125,r=90;
    for(int rr=r;rr>0;rr-=30)ring(g,cx,cy,rr,0xFF1E6A30);
    g.fill(cx-r,cy,cx+r,cy+1,0xFF1E6A30);g.fill(cx,cy-r,cx+1,cy+r,0xFF1E6A30);
    double sweep=(Minecraft.getInstance().level.getGameTime()+pt)*0.08;
    for(int i=0;i<r;i++)g.fill(cx+(int)(Math.sin(sweep)*i),cy-(int)(Math.cos(sweep)*i),cx+(int)(Math.sin(sweep)*i)+1,cy-(int)(Math.cos(sweep)*i)+1,0xFF60FF80);
    g.fill(cx-1,cy-1,cx+2,cy+2,0xFFFFFFFF);
    var list=data.getList("Contacts",10);int row=0;
    for(int i=0;i<list.size();i++){var c=list.getCompound(i);double dx=c.getDouble("DX"),dz=c.getDouble("DZ");double s=r/160.0;int px=cx+(int)(dx*s),py=cy+(int)(dz*s);
     boolean friend=c.getBoolean("Friend");int col=c.getInt("Kind")==1?(friend?0xFF60C0FF:0xFFFF4040):(friend?0xFF60FF60:0xFFFFE040);
     if(Math.abs(px-cx)<r&&Math.abs(py-cy)<r)g.fill(px-2,py-2,px+2,py+2,col);
     if(row<16){g.drawString(font,"■",x+212,y+30+row*11,col,false);g.drawString(font,c.getString("Name")+" "+Math.round(Math.sqrt(dx*dx+dz*dz))+"m",x+222,y+30+row*11,0xFFDDDDDD,false);row++;}}
    if(list.isEmpty())g.drawString(font,"Nenhum contato",x+214,y+30,0xFF808080,false);
   }
   case 2->map(g,x+20,y+26,200);
   case 3->{if(cams.getList("Cameras",10).isEmpty())g.drawString(font,"Nenhuma câmera de segurança a até 40 blocos do painel.",tx,ty,0xFF909090,false);}
   case 4->{g.drawString(font,"O alarme dispara com aeronaves de fora, intrusos (não membros) ou monstros perto:",tx,ty+110,0xFF909090,false);
    g.drawString(font,"sirenes tocam e as portas blindadas e portões trancam.",tx,ty+121,0xFF909090,false);}
   case 5->{var m=data.getList("Members",8);g.drawString(font,"Membros (abrem o painel, não disparam o alarme):",tx,ty+18,0xFF909090,false);
    for(int i=0;i<m.size()&&i<12;i++)g.drawString(font,m.getString(i),tx+(i%2)*160,ty+32+(i/2)*20,0xFFFFFFFF,false);}
   default->{}
  }
  super.render(g,mx,my,pt);
 }
 private static void ring(GuiGraphics g,int cx,int cy,int r,int col){for(int a=0;a<360;a+=3){double t=Math.toRadians(a);g.fill(cx+(int)(Math.cos(t)*r),cy+(int)(Math.sin(t)*r),cx+(int)(Math.cos(t)*r)+1,cy+(int)(Math.sin(t)*r)+1,col);}}
 /** Top-down map of 100x100 blocks around the panel (map colours), with markers. */
 private void map(GuiGraphics g,int x,int y,int size){
  var mc=Minecraft.getInstance();var l=mc.level;int n=100;
  if(mapColors==null||mc.player.tickCount-mapTick>100){
   mapTick=mc.player.tickCount;mapColors=new int[n*n];
   for(int i=0;i<n;i++)for(int j=0;j<n;j++){
    var top=l.getHeightmapPos(Heightmap.Types.WORLD_SURFACE,pos.offset(i-n/2,0,j-n/2)).below();
    var st=l.getBlockState(top);int c=st.getMapColor(l,top).col;
    int shade=(top.getY()&1)==0?0:-12;
    int r=Math.max(0,(c>>16&255)+shade),gg=Math.max(0,(c>>8&255)+shade),b=Math.max(0,(c&255)+shade);
    mapColors[i+j*n]=0xFF000000|r<<16|gg<<8|b;
   }
  }
  float px=size/(float)n;
  for(int i=0;i<n;i++)for(int j=0;j<n;j++)g.fill(x+(int)(i*px),y+(int)(j*px),x+(int)((i+1)*px),y+(int)((j+1)*px),mapColors[i+j*n]);
  g.fill(x+size/2-2,y+size/2-2,x+size/2+2,y+size/2+2,0xFF00E0FF);
  var list=data.getList("Contacts",10);
  for(int i=0;i<list.size();i++){var c=list.getCompound(i);int mx=(int)(x+size/2+c.getDouble("DX")*px),my=(int)(y+size/2+c.getDouble("DZ")*px);
   if(mx>x&&mx<x+size&&my>y&&my<y+size)g.fill(mx-2,my-2,mx+2,my+2,c.getInt("Kind")==1?0xFFFF4040:c.getBoolean("Friend")?0xFF60FF60:0xFFFFE040);}
  int lx=x+size+12;
  g.drawString(font,"■ painel",lx,y,0xFF00E0FF,false);g.drawString(font,"■ aeronaves",lx,y+12,0xFFFF4040,false);g.drawString(font,"■ amigos",lx,y+24,0xFF60FF60,false);g.drawString(font,"■ outros",lx,y+36,0xFFFFE040,false);
  g.drawString(font,"N ↑   100 x 100 blocos",lx,y+56,0xFFB0B0B0,false);
 }
 @Override public boolean isPauseScreen(){return false;}
}
