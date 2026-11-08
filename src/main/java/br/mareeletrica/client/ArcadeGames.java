package br.mareeletrica.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.lwjgl.glfw.GLFW;

/** 0.69.0: three more arcade games — BLOCOS (falling blocks), FLAPPY PATO and ESTRADA (lane racer).
 *  Like Snake and the maze they run on the client; the final score goes to the machine's record board. */
final class ArcadeGames {
 private ArcadeGames(){}
 interface Sound{void beep(float pitch);}
 abstract static class Game {
  final RandomSource rnd=RandomSource.create();final Sound snd;final IntConsumer submit;
  int score;boolean over,started;int t;
  Game(Sound s,IntConsumer sub){snd=s;submit=sub;}
  abstract String title();abstract void reset();abstract void step();abstract void render(GuiGraphics g,Font f,int x0,int y0,int gw,int gh,float pt);
  abstract boolean key(int k,boolean down);
  void tick(){if(!started||over)return;t++;step();}
  void end(){if(over)return;over=true;snd.beep(.5F);snd.beep(.4F);submit.accept(score);}
  void start(){if(over)reset();started=true;}
  void overlay(GuiGraphics g,Font f,int x0,int y0,int gw,int gh,String help){
   if(started&&!over)return;g.fill(x0+14,y0+gh/2-20,x0+gw-14,y0+gh/2+20,0xD0000000);
   g.drawCenteredString(f,over?"§c§lFIM DE JOGO §r§7— "+score+" pontos":"§a§l"+title(),x0+gw/2,y0+gh/2-14,0xFFFFFF);
   g.drawCenteredString(f,"§7"+(over?"Espaço: jogar de novo":help),x0+gw/2,y0+gh/2+2,0xFFFFFF);}
 }
 static boolean left(int k){return k==GLFW.GLFW_KEY_LEFT||k==GLFW.GLFW_KEY_A;}
 static boolean right(int k){return k==GLFW.GLFW_KEY_RIGHT||k==GLFW.GLFW_KEY_D;}
 static boolean up(int k){return k==GLFW.GLFW_KEY_UP||k==GLFW.GLFW_KEY_W;}
 static boolean down(int k){return k==GLFW.GLFW_KEY_DOWN||k==GLFW.GLFW_KEY_S;}

 // ======================================================================= BLOCOS (falling blocks)
 static final class Blocks extends Game {
  static final int BW=10,BH=20;
  static final int[][][] SHAPES={{{0,1},{1,1},{2,1},{3,1}},{{0,0},{1,0},{0,1},{1,1}},{{1,0},{0,1},{1,1},{2,1}},{{0,0},{0,1},{1,1},{2,1}},{{2,0},{0,1},{1,1},{2,1}},{{1,0},{2,0},{0,1},{1,1}},{{0,0},{1,0},{1,1},{2,1}}};
  static final int[] COL={0xFF35E0FF,0xFFFFE040,0xFFB04CFF,0xFF3C6CFF,0xFFFF9A2E,0xFF4CFF6A,0xFFFF4848};
  final int[][] board=new int[BH][BW];int[][] cur;int kind,next,px,py,lines,level,fall,flash;final List<Integer> clearing=new ArrayList<>();
  Blocks(Sound s,IntConsumer sub){super(s,sub);reset();}
  @Override String title(){return "BLOCOS";}
  @Override void reset(){for(var r:board)java.util.Arrays.fill(r,0);score=0;lines=0;level=1;over=false;next=rnd.nextInt(7);spawn();}
  void spawn(){kind=next;next=rnd.nextInt(7);cur=new int[4][];for(int i=0;i<4;i++)cur[i]=SHAPES[kind][i].clone();px=3;py=-1;if(!fits(cur,px,py+1))end();}
  boolean fits(int[][] c,int x,int y){for(var b:c){int cx=b[0]+x,cy=b[1]+y;if(cx<0||cx>=BW||cy>=BH)return false;if(cy>=0&&board[cy][cx]!=0)return false;}return true;}
  void rotate(){if(kind==1)return;int n=kind==0?4:3;int[][] r=new int[4][];for(int i=0;i<4;i++)r[i]=new int[]{(n-1)-cur[i][1],cur[i][0]};
   for(int kick:new int[]{0,-1,1,-2,2})if(fits(r,px+kick,py)){cur=r;px+=kick;snd.beep(1.3F);return;}}
  void lock(){for(var b:cur){int cy=b[1]+py;if(cy<0){end();return;}board[cy][b[0]+px]=kind+1;}
   int n=0;for(int y=BH-1;y>=0;y--){boolean full=true;for(int x=0;x<BW;x++)if(board[y][x]==0){full=false;break;}
    if(full){n++;for(int yy=y;yy>0;yy--)board[yy]=board[yy-1].clone();board[0]=new int[BW];y++;}}
   if(n>0){lines+=n;score+=new int[]{0,100,300,500,800}[n]*level;level=1+lines/10;snd.beep(1.6F+n*.1F);flash=6;}else snd.beep(.9F);
   spawn();}
  @Override void step(){if(flash>0)flash--;int speed=Math.max(3,30-level*3);if(++fall>=speed){fall=0;if(fits(cur,px,py+1))py++;else lock();}}
  @Override boolean key(int k,boolean d){if(!d)return false;
   if(k==GLFW.GLFW_KEY_SPACE&&(!started||over)){start();return true;}if(!started||over)return false;
   if(left(k)&&fits(cur,px-1,py)){px--;return true;}if(right(k)&&fits(cur,px+1,py)){px++;return true;}
   if(up(k)){rotate();return true;}if(down(k)){if(fits(cur,px,py+1)){py++;score++;}return true;}
   if(k==GLFW.GLFW_KEY_SPACE){int n=0;while(fits(cur,px,py+1)){py++;n++;}score+=n*2;lock();return true;}
   return false;}
  void cell(GuiGraphics g,int x,int y,int c,int s){g.fill(x,y,x+s,y+s,c);g.fill(x,y,x+s,y+1,0x50FFFFFF);g.fill(x,y,x+1,y+s,0x30FFFFFF);g.fill(x,y+s-1,x+s,y+s,0x50000000);}
  @Override void render(GuiGraphics g,Font f,int x0,int y0,int gw,int gh,float pt){
   int s=gh/BH,bx=x0+gw/2-BW*s/2-30,by=y0+(gh-BH*s)/2;
   g.fill(bx-1,by-1,bx+BW*s+1,by+BH*s+1,0xFF00E5FF);g.fill(bx,by,bx+BW*s,by+BH*s,flash>0&&flash%2==0?0xFF30305A:0xFF0A0A18);
   for(int y=0;y<BH;y++)for(int x=0;x<BW;x++)if(board[y][x]!=0)cell(g,bx+x*s,by+y*s,COL[board[y][x]-1],s);
   if(started&&!over&&cur!=null){int gy=py;while(fits(cur,px,gy+1))gy++;
    for(var b:cur){if(b[1]+gy>=0)g.fill(bx+(b[0]+px)*s,by+(b[1]+gy)*s,bx+(b[0]+px)*s+s,by+(b[1]+gy)*s+s,0x40FFFFFF);}
    for(var b:cur)if(b[1]+py>=0)cell(g,bx+(b[0]+px)*s,by+(b[1]+py)*s,COL[kind],s);}
   int sx=bx+BW*s+10;g.drawString(f,"§l§bBLOCOS",sx,by,0xFFFFFF,true);g.drawString(f,"Pontos §e"+score,sx,by+14,0xFFFFFF,true);g.drawString(f,"Linhas §a"+lines,sx,by+26,0xFFFFFF,true);g.drawString(f,"Nível §d"+level,sx,by+38,0xFFFFFF,true);
   g.drawString(f,"§7Próxima:",sx,by+56,0xFFFFFF,true);for(var b:SHAPES[next])cell(g,sx+4+b[0]*7,by+68+b[1]*7,COL[next],7);
   g.drawString(f,"§8←→ move",sx,by+92,0xFFFFFF,false);g.drawString(f,"§8↑ gira  ↓ desce",sx,by+102,0xFFFFFF,false);g.drawString(f,"§8Espaço: cai",sx,by+112,0xFFFFFF,false);
   overlay(g,f,x0,y0,gw,gh,"Espaço começa • ←→ move • ↑ gira");}
 }

 // ======================================================================= FLAPPY PATO
 static final class Flappy extends Game {
  float y,vy,yo;final List<float[]> pipes=new ArrayList<>();int nextPipe;float scroll;
  Flappy(Sound s,IntConsumer sub){super(s,sub);reset();}
  @Override String title(){return "FLAPPY PATO";}
  @Override void reset(){y=60;vy=0;yo=y;pipes.clear();nextPipe=40;score=0;over=false;scroll=0;}
  @Override void step(){
   yo=y;vy=Math.min(5.5F,vy+.38F);y+=vy;scroll+=1.6F+Math.min(1.4F,score*.04F);float sp=1.6F+Math.min(1.4F,score*.04F);
   if(--nextPipe<=0){nextPipe=58;float gap=Math.max(36,50-score*.4F);pipes.add(new float[]{230,18+rnd.nextFloat()*(144-36-gap),gap,0});}
   for(var p:pipes){p[0]-=sp;if(p[3]==0&&p[0]+16<40){p[3]=1;score++;snd.beep(1.5F);}}
   pipes.removeIf(p->p[0]<-30);
   if(y<0||y>144-8){end();return;}
   for(var p:pipes)if(40+8>p[0]&&40<p[0]+16&&(y<p[1]||y+7>p[1]+p[2])){end();return;}}
  @Override boolean key(int k,boolean d){if(!d)return false;
   if(k==GLFW.GLFW_KEY_SPACE||up(k)||k==GLFW.GLFW_KEY_W){if(!started||over){start();}vy=-4.6F;snd.beep(1.9F);return true;}return false;}
  @Override void render(GuiGraphics g,Font f,int x0,int y0,int gw,int gh,float pt){
   g.fill(x0,y0,x0+gw,y0+gh,0xFF6EC8FF);
   for(int i=0;i<6;i++){int cx=(int)(x0+Math.floorMod((int)(i*50-scroll*.3F),gw+40)-20);g.fill(cx,y0+20+i%3*14,cx+22,y0+26+i%3*14,0xFFF4FAFF);g.fill(cx+4,y0+16+i%3*14,cx+16,y0+20+i%3*14,0xFFF4FAFF);}
   g.fill(x0,y0+gh-10,x0+gw,y0+gh,0xFF5DBB3A);for(int i=0;i<gw;i+=8){int xx=x0+Math.floorMod((int)(i-scroll),gw);g.fill(xx,y0+gh-10,Math.min(x0+gw,xx+4),y0+gh-8,0xFF3F9A25);}
   for(var p:pipes){int px=(int)(x0+p[0]);if(px>x0+gw||px+16<x0)continue;int l=Math.max(x0,px),r=Math.min(x0+gw,px+16);
    g.fill(l,y0,r,(int)(y0+p[1]),0xFF2FA040);g.fill(l,(int)(y0+p[1]+p[2]),r,y0+gh-10,0xFF2FA040);
    g.fill(Math.max(x0,px-2),(int)(y0+p[1])-6,Math.min(x0+gw,px+18),(int)(y0+p[1]),0xFF25852F);g.fill(Math.max(x0,px-2),(int)(y0+p[1]+p[2]),Math.min(x0+gw,px+18),(int)(y0+p[1]+p[2])+6,0xFF25852F);}
   float dy=Mth.lerp(pt,yo,y);int dx=x0+40,yy=(int)(y0+dy);boolean flapUp=vy<0;
   g.fill(dx,yy+1,dx+8,yy+7,0xFFFFE040);g.fill(dx+5,yy-2,dx+10,yy+3,0xFFFFE040);g.fill(dx+10,yy,dx+13,yy+2,0xFFFF8A20);g.fill(dx+8,yy-1,dx+9,yy,0xFF000000);
   g.fill(dx+1,yy+(flapUp?1:4),dx+5,yy+(flapUp?3:7),0xFFE6C230);
   g.drawString(f,"§l"+score,x0+gw/2-4,y0+6,0xFFFFFF,true);
   overlay(g,f,x0,y0,gw,gh,"Espaço/↑ bate as asas");}
 }

 // ======================================================================= ESTRADA (lane racer)
 static final class Road extends Game {
  static final int LANES=4;float lane=1.5F,target=1,speed,dist,offset;final List<float[]> cars=new ArrayList<>();int spawn,coins;
  Road(Sound s,IntConsumer sub){super(s,sub);reset();}
  @Override String title(){return "ESTRADA";}
  @Override void reset(){lane=1;target=1;speed=2;dist=0;cars.clear();spawn=30;coins=0;score=0;over=false;offset=0;}
  @Override void step(){
   speed=Math.min(6.5F,2+dist/900F);dist+=speed;offset+=speed;lane+=(target-lane)*.35F;
   if(--spawn<=0){spawn=Math.max(10,(int)(34-speed*3.5F))+rnd.nextInt(10);int l=rnd.nextInt(LANES);boolean coin=rnd.nextInt(4)==0;cars.add(new float[]{l,-20,coin?1:0,rnd.nextInt(5),.5F+rnd.nextFloat()*.4F});}
   for(var c:cars)c[1]+=c[2]==1?speed:speed*c[4];
   cars.removeIf(c->c[1]>170);
   for(var c:cars){if(c[1]>144-34&&c[1]<144-6&&Math.abs(c[0]-lane)<.6F){if(c[2]==1){coins++;c[1]=999;snd.beep(1.8F);}else{end();return;}}}
   score=(int)(dist/20)+coins*10;}
  @Override boolean key(int k,boolean d){if(!d)return false;
   if(k==GLFW.GLFW_KEY_SPACE&&(!started||over)){start();return true;}if(!started||over)return false;
   if(left(k)&&target>0){target--;snd.beep(1.1F);return true;}if(right(k)&&target<LANES-1){target++;snd.beep(1.1F);return true;}return false;}
  static final int[] CAR={0xFFFF4848,0xFF3C6CFF,0xFFFFE040,0xFF4CFF6A,0xFFFFFFFF};
  @Override void render(GuiGraphics g,Font f,int x0,int y0,int gw,int gh,float pt){
   int rw=LANES*30,rx=x0+gw/2-rw/2;g.fill(x0,y0,x0+gw,y0+gh,0xFF3A8A2A);g.fill(rx-4,y0,rx+rw+4,y0+gh,0xFFE0E0E0);g.fill(rx,y0,rx+rw,y0+gh,0xFF333338);
   for(int l=1;l<LANES;l++)for(int y=-20;y<gh;y+=20){int yy=(int)(y+offset%20);if(yy<0||yy>gh-8)continue;g.fill(rx+l*30-1,y0+yy,rx+l*30+1,y0+yy+10,0xFFF0F0F0);}
   for(int y=-24;y<gh;y+=24){int yy=(int)(y+offset*.6F%24);if(yy>=0&&yy<gh-10){g.fill(x0+8,y0+yy,x0+14,y0+yy+10,0xFF2C6A1E);g.fill(x0+gw-14,y0+yy,x0+gw-8,y0+yy+10,0xFF2C6A1E);}}
   for(var c:cars){int cx=rx+(int)(c[0]*30)+6,cy=(int)(y0+c[1]);if(cy<y0-20||cy>y0+gh)continue;
    if(c[2]==1){g.fill(cx+5,cy+5,cx+13,cy+13,0xFFFFD020);g.fill(cx+7,cy+7,cx+11,cy+11,0xFFFFF080);continue;}
    int top=Math.max(y0,cy),bot=Math.min(y0+gh,cy+24);g.fill(cx,top,cx+18,bot,CAR[(int)c[3]]);if(cy+16<y0+gh&&cy+16>y0)g.fill(cx+3,cy+16,cx+15,Math.min(y0+gh,cy+20),0xFF202030);}
   int px=rx+(int)(lane*30)+6,py=y0+gh-30;g.fill(px,py,px+18,py+24,0xFFFF8A20);g.fill(px+3,py+4,px+15,py+9,0xFF202030);g.fill(px+2,py+20,px+6,py+24,0xFF111111);g.fill(px+12,py+20,px+16,py+24,0xFF111111);
   g.drawString(f,"§l§bESTRADA",x0+4,y0+4,0xFFFFFF,true);g.drawString(f,"§e"+score,x0+4,y0+16,0xFFFFFF,true);g.drawString(f,"§6● "+coins,x0+4,y0+28,0xFFFFFF,true);
   g.drawString(f,"§7"+(int)(speed*30)+" km/h",x0+gw-56,y0+4,0xFFFFFF,true);
   overlay(g,f,x0,y0,gw,gh,"Espaço começa • ←→ troca de faixa • moedas +10");}
 }
}
