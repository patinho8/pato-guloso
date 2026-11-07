package br.mareeletrica.client;

import br.mareeletrica.Casino;
import br.mareeletrica.Casino.Game;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** One screen for all casino machines: bet controls at the bottom, the game in the middle. Results come from the server;
 *  the screen only animates them (roulette spin, plinko ball, wheel). */
public final class CasinoScreen extends Screen {
 static final int W=360,H=232;
 private final BlockPos pos;private CompoundTag d;private Game game;
 // local choices
 private static int rouType=1,rouNum=17,mines=3,diceChance=50,diceOver=0,limbo=200,risk=1,auto=0;private static long keno=0;
 static final int[] AUTO={0,150,200,300,500,1000};
 static final String[] ROU={"Número","Vermelho","Preto","Par","Ímpar","1-18","19-36","Dúzia"};
 // animations
 private int animStart=-1000,lastSpin=-1,lastDrop=-1;private int age;
 public CasinoScreen(BlockPos pos,CompoundTag d){super(Component.literal("Cassino"));this.pos=pos;this.d=d;game=Game.valueOf(d.getString("Game"));lastSpin=spinCount();lastDrop=d.getInt("Drop");}
 public BlockPos pos(){return pos;}
 int spinCount(){return d.getInt("Spin")+d.getInt("Rolls")+d.getInt("Draws")+d.getInt("Deals");}
 public void update(CompoundTag n){
  d=n;
  if(spinCount()!=lastSpin||n.getInt("Drop")!=lastDrop){lastSpin=spinCount();lastDrop=n.getInt("Drop");animStart=age;}
  rebuild();
 }
 int L(){return width/2-W/2;}int T(){return height/2-H/2;}
 int phase(){return d.getInt("Phase");}
 void send(int a,int x,int y){PacketDistributor.sendToServer(new Casino.Action(a,x,y));}
 Button btn(String t,int x,int y,int w,Runnable r){var b=Button.builder(Component.literal(t),bt->r.run()).bounds(x,y,w,18).build();addRenderableWidget(b);return b;}
 boolean busy(){return phase()==1;}
 @Override protected void init(){rebuild();}
 @Override public void tick(){age++;}
 void rebuild(){
  clearWidgets();int x=L(),y=T(),by=y+H-24;
  {
   int bet=d.getInt("Bet");
   btn("½",x+6,by,18,()->send(0,Math.max(1,bet/2),0));btn("-10",x+26,by,24,()->send(0,bet-10,0));btn("-1",x+52,by,20,()->send(0,bet-1,0));
   btn("+1",x+132,by,20,()->send(0,bet+1,0));btn("+10",x+154,by,24,()->send(0,bet+10,0));btn("x2",x+180,by,20,()->send(0,bet*2,0));
   btn("Máx",x+202,by,28,()->send(0,Math.min(Casino.MAX_BET,d.getInt("Chips")),0));
   for(var w:children())if(w instanceof Button b)b.active=!busy();
  }
  int gx=x+236,gw=W-242;
  switch(game){
   case CRASH->{
    btn("Auto: "+(AUTO[auto]==0?"não":AUTO[auto]/100.0+"x"),gx,y+150,gw,()->{auto=(auto+1)%AUTO.length;rebuild();}).active=!busy();
    btn(busy()?"§aRETIRAR":"Apostar",gx,by,gw,()->send(busy()?4:1,AUTO[auto],0));
   }
   case ROLETA->{
    btn(ROU[rouType],gx,y+128,gw,()->{rouType=(rouType+1)%ROU.length;rebuild();});
    if(rouType==0||rouType==7){int max=rouType==0?36:3;btn("-",gx,y+150,18,()->{rouNum=Math.floorMod(rouNum-1-(rouType==7?1:0),max+(rouType==0?1:0))+(rouType==7?1:0);rebuild();});
     btn("+",gx+gw-18,y+150,18,()->{rouNum=rouType==7?rouNum%3+1:(rouNum+1)%37;rebuild();});}
    btn("Girar",gx,by,gw,()->send(1,rouType,rouType==7?Mth.clamp(rouNum,1,3):rouNum));
   }
   case BLACKJACK->{
    if(busy()){btn("Pedir",gx,by-44,gw,()->send(2,0,0));btn("Parar",gx,by-22,gw,()->send(3,0,0));btn("Dobrar",gx,by,gw,()->send(4,0,0)).active=d.getIntArray("P").length==2;}
    else btn("Dar cartas",gx,by,gw,()->send(1,0,0));
   }
   case PLINKO->{btn("Risco: "+new String[]{"baixo","médio","alto"}[risk],gx,y+150,gw,()->{risk=(risk+1)%3;rebuild();});btn("Soltar bola",gx,by,gw,()->send(1,risk,0));}
   case MINES->{
    if(busy()){btn("§aRETIRAR",gx,by,gw,()->send(4,0,0)).active=d.getDouble("Mult")>1.0001;}
    else{btn("-",gx,y+150,18,()->{mines=Math.max(1,mines-1);rebuild();});btn("+",gx+gw-18,y+150,18,()->{mines=Math.min(24,mines+1);rebuild();});btn("Começar",gx,by,gw,()->send(1,mines,0));}
    var board=d.getIntArray("Board");
    for(int i=0;i<25;i++){final int k=i;int bx=x+20+(i%5)*40,byy=y+28+(i/5)*30;
     int st=board.length==25?board[i]:0;String t=st==1?"§b§l◆":st==2?"§c§l✖":st==3?"§4✖":"";
     var b=Button.builder(Component.literal(t),bt->send(2,k,0)).bounds(bx,byy,36,26).build();b.active=busy()&&st==0;addRenderableWidget(b);}
   }
   case DICE->{
    btn("-10",gx,y+128,26,()->{diceChance=Math.max(2,diceChance-10);rebuild();});btn("-1",gx+28,y+128,20,()->{diceChance=Math.max(2,diceChance-1);rebuild();});
    btn("+1",gx+50,y+128,20,()->{diceChance=Math.min(95,diceChance+1);rebuild();});btn("+10",gx+72,y+128,26,()->{diceChance=Math.min(95,diceChance+10);rebuild();});
    btn(diceOver==0?"Rolar ABAIXO":"Rolar ACIMA",gx,y+150,gw,()->{diceOver=1-diceOver;rebuild();});
    btn("Rolar",gx,by,gw,()->send(1,diceChance,diceOver));
   }
   case LIMBO->{
    int[] pre={150,200,300,500,1000,10000};
    for(int i=0;i<pre.length;i++){final int v=pre[i];btn((v/100.0)+"x",gx+(i%3)*36,y+108+(i/3)*20,34,()->{limbo=v;rebuild();});}
    btn("-",gx,y+150,18,()->{limbo=Math.max(101,limbo-(limbo>1000?100:10));rebuild();});btn("+",gx+gw-18,y+150,18,()->{limbo=Math.min(100000,limbo+(limbo>=1000?100:10));rebuild();});
    btn("Jogar",gx,by,gw,()->send(1,limbo,0));
   }
   case KENO->{
    var drawn=d.getIntArray("Drawn");var set=new java.util.HashSet<Integer>();for(int v:drawn)set.add(v);
    for(int i=0;i<40;i++){final int k=i;boolean pick=(keno>>i&1)!=0,hit=set.contains(i);
     String t=(pick&&hit?"§a§l":pick?"§e":hit?"§b":"§7")+(i+1);
     var b=Button.builder(Component.literal(t),bt->{if((keno>>k&1)!=0)keno&=~(1L<<k);else if(Long.bitCount(keno)<10)keno|=1L<<k;rebuild();}).bounds(x+8+(i%8)*28,y+26+(i/8)*24,26,20).build();addRenderableWidget(b);}
    btn("Limpar",gx,y+150,gw,()->{keno=0;rebuild();});
    btn("Sortear",gx,by,gw,()->send(1,(int)(keno&0xFFFFFFFFL),(int)(keno>>32)));
   }
   case BACCARAT->{btn("Jogador (2x)",gx,by-44,gw,()->send(1,0,0));btn("Banca (1,95x)",gx,by-22,gw,()->send(1,1,0));btn("Empate (9x)",gx,by,gw,()->send(1,2,0));}
   case WHEEL->{btn("Risco: "+new String[]{"baixo","médio","alto"}[risk],gx,y+150,gw,()->{risk=(risk+1)%3;rebuild();});btn("Girar",gx,by,gw,()->send(1,risk,0));}
   case HILO->{
    if(busy()){btn("▲ Maior",gx,by-44,gw,()->send(2,0,0));btn("▼ Menor",gx,by-22,gw,()->send(3,0,0));btn("§aRETIRAR",gx,by,gw,()->send(4,0,0)).active=d.getInt("Steps")>0;}
    else btn("Começar",gx,by,gw,()->send(1,0,0));
   }
   case ANDAR_BAHAR->{btn("Andar (1,9x)",gx,by-22,gw,()->send(1,0,0));btn("Bahar (2x)",gx,by,gw,()->send(1,1,0));}
  }
  btn("Sair",x+W-40,y+4,34,this::onClose);
 }
 @Override public void onClose(){send(9,0,0);super.onClose();}
 @Override public boolean isPauseScreen(){return false;}

 static String suit(int c){return switch(c/13){case 0->"♠";case 1->"♥";case 2->"♦";default->"♣";};}
 static String rank(int c){int r=c%13;return switch(r){case 0->"A";case 10->"J";case 11->"Q";case 12->"K";default->String.valueOf(r+1);};}
 void card(GuiGraphics g,int x,int y,int c,boolean hidden){
  g.fill(x,y,x+24,y+34,0xFF202020);g.fill(x+1,y+1,x+23,y+33,hidden?0xFF2A3A8A:0xFFF4F4F0);
  if(hidden){g.fill(x+4,y+4,x+20,y+30,0xFF4060C0);return;}
  int col=(c/13==1||c/13==2)?0xFFD02020:0xFF101010;
  g.drawString(font,rank(c),x+3,y+3,col,false);g.drawString(font,suit(c),x+12-font.width(suit(c))/2,y+15,col,false);
 }
 void cards(GuiGraphics g,int x,int y,int[] cs,int hiddenFrom){for(int i=0;i<cs.length;i++)card(g,x+i*27,y,cs[i],false);if(hiddenFrom>0)card(g,x+cs.length*27,y,0,true);}

 @Override public void render(GuiGraphics g,int mx,int my,float pt){
  int x=L(),y=T();float t=age+pt-animStart;
  g.fill(x-3,y-3,x+W+3,y+H+3,game.color);g.fill(x,y,x+W,y+H,0xF0101216);
  g.fill(x,y,x+W,y+20,0xFF1C1F26);
  g.drawString(font,"§l"+game.label.toUpperCase(),x+8,y+6,game.color|0xFF000000,false);
  String chips="● "+d.getInt("Chips")+" fichas";g.drawString(font,chips,x+W-48-font.width(chips),y+6,0xFFFFD050,false);
  {int by=y+H-24;String bet="Aposta: "+d.getInt("Bet");g.drawString(font,bet,x+102-font.width(bet)/2,by+5,0xFFFFFFFF,false);}
  int gx=x+236;
  switch(game){
   case CRASH->{
    int ax=x+10,ay=y+28,aw=218,ah=150;g.fill(ax,ay,ax+aw,ay+ah,0xFF0A0C10);
    double m=d.getDouble("Mult");int ticks=d.getInt("Ticks");
    int n=Math.max(1,ticks);double maxM=Math.max(2,m*1.15);
    int prevX=ax,prevY=ay+ah;
    for(int i=1;i<=n;i+=Math.max(1,n/120)){double mm=Math.exp(.0035*i*1.6);int px=ax+(int)(aw*i/(double)Math.max(n,60)),py=ay+ah-(int)((mm-1)/(maxM-1)*(ah-10));
     g.fill(Math.min(prevX,px),Math.min(prevY,py),Math.max(prevX,px)+2,Math.max(prevY,py)+2,phase()==2&&d.getInt("Win")==0?0xFFE03030:0xFF40E070);prevX=px;prevY=py;}
    g.drawString(font,"🚀",prevX-4,prevY-10,0xFFFFFFFF,false);
    String big=String.format(java.util.Locale.ROOT,"%.2fx",m);g.pose().pushPose();g.pose().translate(ax+aw/2F,ay+40,0);g.pose().scale(2.5F,2.5F,1);
    g.drawString(font,big,-font.width(big)/2,0,phase()==2&&d.getInt("Win")==0?0xFFFF4040:0xFFFFFFFF,true);g.pose().popPose();
    var h=d.getIntArray("History");for(int i=0;i<h.length&&i<8;i++){String s=String.format(java.util.Locale.ROOT,"%.2f",h[i]/100.0);g.drawString(font,s,gx,y+28+i*11,h[i]>=200?0xFF40E070:0xFFE07070,false);}
   }
   case ROLETA->{
    int cx=x+118,cy=y+100,r=62;
    int res=d.getInt("Result");boolean spinning=t<40;
    float rot=spinning?t*t*.4F:0;
    int[] order={0,32,15,19,4,21,2,25,17,34,6,27,13,36,11,30,8,23,10,5,24,16,33,1,20,14,31,9,22,18,29,7,28,12,35,3,26};
    for(int i=0;i<37;i++){double a=Math.toRadians(i*360/37.0+rot);int n=order[i];int col=n==0?0xFF20A040:Casino.Session.RED.contains(n)?0xFFC02020:0xFF202020;
     int px=cx+(int)(Math.cos(a)*r),py=cy+(int)(Math.sin(a)*r);g.fill(px-7,py-5,px+8,py+6,col);g.drawString(font,String.valueOf(n),px-font.width(String.valueOf(n))/2,py-3,0xFFFFFFFF,false);}
    String s=spinning?String.valueOf((int)(t*7)%37):String.valueOf(res);
    g.pose().pushPose();g.pose().translate(cx,cy-8,0);g.pose().scale(2.5F,2.5F,1);g.drawString(font,s,-font.width(s)/2,0,0xFFFFFFFF,true);g.pose().popPose();
    g.drawString(font,"Aposta em:",gx,y+116,0xFFAAAAAA,false);
    if(rouType==0||rouType==7)g.drawString(font,rouType==0?"nº "+rouNum:rouNum+"ª dúzia",gx+22,y+155,0xFFFFFFFF,false);
    g.drawString(font,rouType==0?"paga 36x":rouType==7?"paga 3x":"paga 2x",gx,y+176,0xFF90FF90,false);
   }
   case BLACKJACK->{
    g.fill(x+8,y+26,x+228,y+190,0xFF0E4A24);
    var dh=d.getIntArray("D");var ph=d.getIntArray("P");
    g.drawString(font,"Banca: "+d.getInt("DV"),x+14,y+30,0xFFFFFFFF,false);cards(g,x+14,y+42,dh,phase()==1?1:0);
    g.drawString(font,"Você: "+d.getInt("PV"),x+14,y+110,0xFFFFFFFF,false);cards(g,x+14,y+122,ph,0);
    g.drawString(font,"Blackjack paga 2,5x",gx,y+30,0xFF90FF90,false);g.drawString(font,"Banca para no 17",gx,y+42,0xFFAAAAAA,false);
   }
   case PLINKO->{
    var mults=Casino.Session.PLINKO[d.contains("Risk")?d.getInt("Risk"):risk];
    int top=y+30,cx=x+118;
    for(int row=0;row<12;row++)for(int i=0;i<=row+2;i++){int px=cx+(int)((i-(row+2)/2.0)*14),py=top+row*11;g.fill(px,py,px+2,py+2,0xFFDDDDDD);}
    for(int s=0;s<13;s++){int sx=cx+(int)((s-6)*14)-7;double m=mults[s];int col=m>=10?0xFFE02020:m>=2?0xFFE08020:m>=1?0xFFE0C020:0xFF8080FF;
     g.fill(sx,top+134,sx+13,top+146,col);String ms=m>=10?String.valueOf((int)m):String.valueOf(m);g.pose().pushPose();g.pose().translate(sx+6.5F,top+137,0);g.pose().scale(.7F,.7F,1);g.drawString(font,ms,-font.width(ms)/2,0,0xFF000000,false);g.pose().popPose();}
    if(d.contains("Path")&&t<60){int path=d.getInt("Path");float prog=Math.min(12,t/4F);int step=(int)prog;float frac=prog-step;
     double pos=0;for(int i=0;i<step;i++)pos+=((path>>i&1)!=0)?.5:-.5;double nxt=pos+(step<12?(((path>>step&1)!=0)?.5:-.5):0);
     double bx=cx+Mth.lerp(frac,pos,nxt)*14+1,byy=top-6+(step+frac)*11;g.fill((int)bx-3,(int)byy-3,(int)bx+4,(int)byy+4,0xFFFF60A0);}
   }
   case MINES->{
    g.drawString(font,"Minas: "+(busy()?d.getInt("Mines"):mines),gx+22,y+155,0xFFFFFFFF,false);
    g.drawString(font,String.format(java.util.Locale.ROOT,"%.2fx",d.getDouble("Mult")),gx,y+30,0xFF90FF90,false);
    g.drawString(font,"Mais minas =",gx,y+60,0xFFAAAAAA,false);g.drawString(font,"mais prêmio",gx,y+71,0xFFAAAAAA,false);
   }
   case DICE->{
    int ax=x+14,ay=y+70,aw=210;g.fill(ax,ay,ax+aw,ay+12,0xFF303030);
    int cut=diceOver==0?diceChance:100-diceChance;
    g.fill(diceOver==0?ax:ax+aw*cut/100,ay,diceOver==0?ax+aw*cut/100:ax+aw,ay+12,0xFF30C060);
    int roll=d.getInt("Roll");boolean show=d.getInt("Rolls")>0;float shown=t<20?(float)(Math.random()*10000):roll;
    if(show){int px=ax+(int)(aw*shown/10000);g.fill(px-2,ay-6,px+3,ay+18,0xFFFFFFFF);
     String s=String.format(java.util.Locale.ROOT,"%.2f",shown/100);g.pose().pushPose();g.pose().translate(ax+aw/2F,y+110,0);g.pose().scale(2.5F,2.5F,1);g.drawString(font,s,-font.width(s)/2,0,0xFFFFFFFF,true);g.pose().popPose();}
    g.drawString(font,"Chance: "+diceChance+"%",gx,y+100,0xFFFFFFFF,false);g.drawString(font,String.format(java.util.Locale.ROOT,"Paga %.2fx",99.0/diceChance),gx,y+176,0xFF90FF90,false);
   }
   case LIMBO->{
    double res=d.getDouble("Result");boolean show=d.getInt("Rolls")>0;
    String s=show?(t<15?String.format(java.util.Locale.ROOT,"%.2fx",1+t*t*.05):String.format(java.util.Locale.ROOT,"%.2fx",res)):"?";
    g.pose().pushPose();g.pose().translate(x+118,y+80,0);g.pose().scale(3F,3F,1);g.drawString(font,s,-font.width(s)/2,0,show&&t>=15?(res>=limbo/100.0?0xFF40E070:0xFFE04040):0xFFFFFFFF,true);g.pose().popPose();
    g.drawString(font,"Alvo: "+limbo/100.0+"x",gx,y+90,0xFFFFFFFF,false);g.drawString(font,String.format(java.util.Locale.ROOT,"Chance: %.1f%%",99.0/(limbo/100.0)),gx,y+176,0xFF90FF90,false);
   }
   case KENO->{g.drawString(font,"Escolhidos: "+Long.bitCount(keno)+"/10",gx,y+30,0xFFFFFFFF,false);if(d.contains("Hits"))g.drawString(font,"Acertos: "+d.getInt("Hits"),gx,y+44,0xFF90FF90,false);
    g.drawString(font,"§eamarelo§7 = seu",gx,y+70,0xFFAAAAAA,false);g.drawString(font,"§bazul§7 = sorteado",gx,y+82,0xFFAAAAAA,false);g.drawString(font,"§averde§7 = acerto",gx,y+94,0xFFAAAAAA,false);}
   case BACCARAT->{
    g.fill(x+8,y+26,x+228,y+190,0xFF4A1018);
    if(d.contains("PC")){g.drawString(font,"Jogador",x+14,y+32,0xFFFFFFFF,false);cards(g,x+14,y+44,d.getIntArray("PC"),0);
     g.drawString(font,"Banca",x+14,y+110,0xFFFFFFFF,false);cards(g,x+14,y+122,d.getIntArray("BC"),0);}
    else g.drawString(font,"Aposte no Jogador, na Banca ou no Empate",x+14,y+90,0xFFDDDDDD,false);
   }
   case WHEEL->{
    var segs=Casino.Session.WHEEL[d.contains("Risk")?d.getInt("Risk"):risk];int cx=x+118,cy=y+104,r=66;
    int target=d.getInt("Seg");boolean spinning=d.getInt("Spin")>0&&t<50;
    float rot=spinning?(1-(float)Math.pow(1-t/50F,3))*(720+target*12):target*12;
    for(int i=0;i<30;i++){double a=Math.toRadians(i*12-rot-90);double m=segs[i];int col=m==0?0xFF404040:m>=10?0xFFE02020:m>=3?0xFFE08020:m>=2?0xFF40C060:m>=1.5?0xFF4080E0:0xFFB0B0B0;
     for(int k=40;k<r;k+=3){int px=cx+(int)(Math.cos(a)*k),py=cy+(int)(Math.sin(a)*k);g.fill(px-2,py-2,px+2,py+2,col);}}
    g.fill(cx-2,cy-r-10,cx+3,cy-r+2,0xFFFFFFFF);
    if(d.getInt("Spin")>0&&!spinning){String s=String.format(java.util.Locale.ROOT,"%.2fx",segs[target]);g.pose().pushPose();g.pose().translate(cx,cy-6,0);g.pose().scale(2F,2F,1);g.drawString(font,s,-font.width(s)/2,0,0xFFFFFFFF,true);g.pose().popPose();}
   }
   case HILO->{
    if(busy()||d.contains("Card")&&phase()==2){card(g,x+100,y+60,d.getInt("Card"),false);if(d.contains("Prev"))card(g,x+60,y+66,d.getInt("Prev"),false);}
    g.drawString(font,String.format(java.util.Locale.ROOT,"%.2fx",d.getDouble("Mult")),gx,y+30,0xFF90FF90,false);
    g.drawString(font,"Empate conta",gx,y+60,0xFFAAAAAA,false);g.drawString(font,"a seu favor",gx,y+71,0xFFAAAAAA,false);
   }
   case ANDAR_BAHAR->{
    g.fill(x+8,y+26,x+228,y+190,0xFF3A1A40);
    if(d.contains("Joker")){g.drawString(font,"Coringa",x+14,y+30,0xFFFFD050,false);card(g,x+60,y+28,d.getInt("Joker"),false);
     var a=d.getIntArray("A");var b=d.getIntArray("B");int show=(int)Math.min(Math.max(a.length,b.length),t/5);
     g.drawString(font,"Andar",x+14,y+80,0xFFFFFFFF,false);g.drawString(font,"Bahar",x+14,y+130,0xFFFFFFFF,false);
     for(int i=0;i<Math.min(show,a.length)&&i<7;i++)card(g,x+50+i*25,y+72,a[Math.max(0,a.length-Math.min(a.length,7))+i],false);
     for(int i=0;i<Math.min(show,b.length)&&i<7;i++)card(g,x+50+i*25,y+122,b[Math.max(0,b.length-Math.min(b.length,7))+i],false);}
   }
  }
  // message line
  String msg=d.getString("Msg");boolean waiting=(game==Game.ROLETA&&t<40)||(game==Game.WHEEL&&t<50)||(game==Game.PLINKO&&t<50)||(game==Game.DICE&&t<20)||(game==Game.LIMBO&&t<15);
  if(!waiting&&!msg.isEmpty())g.drawString(font,msg,x+W/2-font.width(msg)/2,y+H-40,0xFFFFFFFF,true);
  super.render(g,mx,my,pt);
 }
 @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){g.fill(0,0,width,height,0x80000000);}
}
