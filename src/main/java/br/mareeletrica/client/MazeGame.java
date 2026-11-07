package br.mareeletrica.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** 0.65.5: "Pato Guloso" — arcade maze game. A greedy duck eats corn in a hedge maze while three foxes hunt it.
 *  The bread loaves make the foxes run scared for a few seconds (then the duck can eat them).
 *  Runs entirely on the client (like Snake); the final score is sent to the machine for the high score. */
final class MazeGame {
 static final String[] MAP={
  "#####################",
  "#o........#........o#",
  "#.##.####.#.####.##.#",
  "#.#.......#.......#.#",
  "#.#.###.#####.###.#.#",
  "#.....#.......#.....#",
  "###.#.#.##-##.#.#.###",
  "T...#...#   #...#...T",
  "###.#.#.#####.#.#.###",
  "#.....#...P...#.....#",
  "#.#.###.#.#.#.###.#.#",
  "#.#.....#.#.#.....#.#",
  "#.##.####.#.####.##.#",
  "#o.................o#",
  "#####################"};
 static final int W=21,H=15,C=9,S=4;                       // S = sub-steps per tile
 static final int[] DX={1,0,-1,0},DY={0,1,0,-1};            // 0 right, 1 down, 2 left, 3 up
 static final int DOOR_X=10,DOOR_Y=6,OUT_Y=5,DEN_Y=7;
 final RandomSource rnd=RandomSource.create();
 final char[][] g=new char[H][W];
 int score,lives,level,corn,scared,tick,dying,ready,combo,startX,startY;boolean over,started;
 final Actor duck=new Actor();final Actor[] fox={new Actor(),new Actor(),new Actor()};
 static final class Actor{int x,y,px,py,dir=-1,want=-1,release;boolean home,leaving,dead,scared;}
 final Runnable beepEat,beepPower,beepDie,beepFox;final java.util.function.IntConsumer submit;

 MazeGame(Runnable eat,Runnable power,Runnable die,Runnable foxEaten,java.util.function.IntConsumer submit){
  beepEat=eat;beepPower=power;beepDie=die;beepFox=foxEaten;this.submit=submit;newGame();}
 void newGame(){score=0;lives=3;level=1;over=false;started=false;newLevel();}
 void newLevel(){
  corn=0;for(int y=0;y<H;y++)for(int x=0;x<W;x++){char c=MAP[y].charAt(x);if(c=='P'){startX=x;startY=y;c=' ';}g[y][x]=c;if(c=='.'||c=='o')corn++;}
  place();}
 void place(){
  duck.x=duck.px=startX*S;duck.y=duck.py=startY*S;duck.dir=2;duck.want=2;
  for(int i=0;i<3;i++){var f=fox[i];f.x=f.px=(9+i)*S;f.y=f.py=DEN_Y*S;f.dir=3;f.home=true;f.leaving=false;f.dead=false;f.scared=false;f.release=new int[]{30,130,230}[i];}
  scared=0;dying=0;ready=50;}
 // ---------------- rules ----------------
 boolean open(int tx,int ty,Actor a){
  tx=Math.floorMod(tx,W);if(ty<0||ty>=H)return false;char c=g[ty][tx];
  if(c=='#')return false;if(c=='-')return a!=duck&&(a.leaving||a.dead);
  if(a!=duck&&!a.leaving&&!a.dead&&ty==DEN_Y&&tx>=9&&tx<=11)return a.home;  // foxes don't wander back into the den
  return true;}
 void tick(){
  if(over||!started)return;tick++;
  if(ready>0){ready--;return;}
  if(dying>0){if(--dying==0){if(--lives<=0){over=true;submit.accept(score);}else place();}return;}
  move(duck);eat();
  int keep=Math.min(12,3+level);   // fox speed: 75% of the duck in level 1, faster each level
  for(var f:fox){
   if(f.home){if(--f.release<=0){f.home=false;f.leaving=true;}else{f.py=f.y;f.y=DEN_Y*S+((tick/6)%2==0?0:1)-0;continue;}}
   int steps=f.dead?2:f.scared?(tick%2):(tick%keep==0?0:1);
   for(int k=0;k<steps;k++)move(f);
   if(steps==0){f.px=f.x;f.py=f.y;}
  }
  if(scared>0&&--scared==0)for(var f:fox)f.scared=false;
  hits();
  if(corn<=0){level++;beepPower.run();newLevel();}
 }
 void move(Actor a){
  a.px=a.x;a.py=a.y;
  if(a.x%S==0&&a.y%S==0){
   int tx=a.x/S,ty=a.y/S;
   if(a==duck){if(a.want>=0&&open(tx+DX[a.want],ty+DY[a.want],a))a.dir=a.want;if(a.dir>=0&&!open(tx+DX[a.dir],ty+DY[a.dir],a))a.dir=-1;}
   else choose(a,tx,ty);
  }else if(a==duck&&a.want>=0&&a.dir>=0&&a.want==(a.dir+2)%4)a.dir=a.want;   // reverse at any moment
  if(a.dir<0)return;
  a.x+=DX[a.dir];a.y+=DY[a.dir];
  if(a.x<0){a.x+=W*S;a.px=a.x;}else if(a.x>=W*S){a.x-=W*S;a.px=a.x;}   // tunnel
 }
 void choose(Actor f,int tx,int ty){
  if(f.leaving&&tx==DOOR_X&&ty==OUT_Y){f.leaving=false;}
  if(f.dead&&tx==DOOR_X&&ty==DEN_Y){f.dead=false;f.scared=false;f.leaving=true;}
  int tgx,tgy;int dx=duck.x/S,dy=duck.y/S;
  if(f.dead){tgx=DOOR_X;tgy=DEN_Y;}
  else if(f.leaving){tgx=DOOR_X;tgy=OUT_Y;}
  else if(f==fox[0]){tgx=dx;tgy=dy;}                                                        // hunter: straight at the duck
  else if(f==fox[1]){int d=Math.max(duck.dir,0);tgx=dx+DX[d]*4;tgy=dy+DY[d]*4;}              // trapper: cuts in front of it
  else{int ddx=dx-tx,ddy=dy-ty;if(ddx*ddx+ddy*ddy>36){tgx=dx;tgy=dy;}else{tgx=1;tgy=H-2;}}   // sly: comes close, then backs off
  int best=-1;double bd=1e9;int back=f.dir<0?-1:(f.dir+2)%4;
  for(int pass=0;pass<2&&best<0;pass++)for(int d0=0;d0<4;d0++){
   int d=f.scared&&!f.dead&&!f.leaving?Math.floorMod(d0+rnd.nextInt(4),4):d0;
   if(pass==0&&d==back&&!f.leaving&&!f.dead)continue;
   if(!open(tx+DX[d],ty+DY[d],f))continue;
   if(f.scared&&!f.dead&&!f.leaving){best=d;break;}
   double ex=tx+DX[d]-tgx,ey=ty+DY[d]-tgy,dist=ex*ex+ey*ey;if(dist<bd){bd=dist;best=d;}
  }
  f.dir=best;
 }
 void eat(){
  if(duck.x%S!=0||duck.y%S!=0)return;int tx=Math.floorMod(duck.x/S,W),ty=duck.y/S;char c=g[ty][tx];
  if(c=='.'){g[ty][tx]=' ';corn--;score+=10;beepEat.run();}
  else if(c=='o'){g[ty][tx]=' ';corn--;score+=50;combo=0;scared=Math.max(60,170-level*15);beepPower.run();
   for(var f:fox)if(!f.dead&&!f.home&&!f.leaving){f.scared=true;if(f.dir>=0)f.dir=(f.dir+2)%4;}}
 }
 void hits(){
  for(var f:fox){
   if(f.home||f.dead)continue;
   int ex=Math.abs(f.x-duck.x),ey=Math.abs(f.y-duck.y);if(ex>W*S/2)ex=W*S-ex;
   if(ex+ey>=3)continue;
   if(f.scared){f.scared=false;f.dead=true;combo++;score+=100<<combo;beepFox.run();}
   else{dying=36;beepDie.run();return;}
  }
 }
 String debug(){var b=new StringBuilder("score="+score+" lives="+lives+" dying="+dying+" ready="+ready+" corn="+corn+" duck=("+duck.x/(float)S+","+duck.y/(float)S+") dir="+duck.dir);
  for(var f:fox)b.append(" | fox(").append(f.x/(float)S).append(",").append(f.y/(float)S).append(f.home?" home":"").append(f.leaving?" leaving":"").append(f.scared?" scared":"").append(f.dead?" dead":"").append(")");return b.toString();}
 // ---------------- keys ----------------
 boolean key(int dir){if(over)return false;started=true;duck.want=dir;return true;}
 void start(){if(over)newGame();started=true;}
 // ---------------- drawing ----------------
 void render(GuiGraphics gg,Font font,int ax,int ay,int aw,int ah,float pt,int best,String bestName){
  int x0=ax+(aw-W*C)/2,y0=ay+(ah-H*C)/2;
  for(int y=0;y<H;y++)for(int x=0;x<W;x++){
   int px=x0+x*C,py=y0+y*C;char c=g[y][x];
   if(c=='#'){gg.fill(px,py,px+C,py+C,0xFF1F6B2E);
    if(y>0&&g[y-1][x]!='#')gg.fill(px,py,px+C,py+1,0xFF4FBF5A);if(y<H-1&&g[y+1][x]!='#')gg.fill(px,py+C-1,px+C,py+C,0xFF0F3A18);
    if((x*7+y*3)%5==0)gg.fill(px+3,py+3,px+5,py+5,0xFF2E8A3E);}
   else if(c=='-')gg.fill(px,py+3,px+C,py+5,0xFFD9A35A);
   else if(c=='.')gg.fill(px+3,py+3,px+6,py+6,0xFFFFD23F);
   else if(c=='o'&&(tick/8)%2==0){gg.fill(px+1,py+2,px+8,py+7,0xFFB7792F);gg.fill(px+2,py+2,px+7,py+4,0xFFE0A85A);}
  }
  // foxes
  int[] col={0xFFE8742A,0xFFB5462A,0xFFC9C9D9};
  for(int i=0;i<3;i++){var f=fox[i];int[] p=at(f,pt,x0,y0);int fx=p[0],fy=p[1];
   if(f.dead){eyes(gg,fx,fy,f.dir);continue;}
   int body=f.scared?(scared<40&&(tick/5)%2==0?0xFFEFEFFF:0xFF2A3BD1):col[i];
   gg.fill(fx+1,fy+3,fx+8,fy+9,body);gg.fill(fx+1,fy+1,fx+3,fy+3,body);gg.fill(fx+6,fy+1,fx+8,fy+3,body);   // head + ears
   if(!f.scared){gg.fill(fx+3,fy+6,fx+6,fy+9,0xFFF5F0E6);gg.fill(fx+4,fy+5,fx+5,fy+6,0xFF000000);}else gg.fill(fx+2,fy+7,fx+7,fy+8,0xFFFFFFFF);
   eyes(gg,fx,fy,f.dir);}
  // duck
  if(dying==0||(dying/4)%2==0){int[] p=at(duck,pt,x0,y0);int dx=p[0],dy=p[1];int d=duck.dir<0?2:duck.dir;
   gg.fill(dx+1,dy+2,dx+8,dy+8,0xFFFFE070);gg.fill(dx+2,dy+1,dx+7,dy+2,0xFFFFE070);
   boolean open=((duck.x+duck.y)/2)%2==0;
   switch(d){
    case 0->{gg.fill(dx+8,dy+3,dx+10,dy+5,0xFFFF8A1E);if(open)gg.fill(dx+8,dy+5,dx+10,dy+6,0xFFE0701A);gg.fill(dx+5,dy+2,dx+6,dy+3,0xFF000000);}
    case 2->{gg.fill(dx-1,dy+3,dx+1,dy+5,0xFFFF8A1E);if(open)gg.fill(dx-1,dy+5,dx+1,dy+6,0xFFE0701A);gg.fill(dx+3,dy+2,dx+4,dy+3,0xFF000000);}
    case 1->{gg.fill(dx+3,dy+8,dx+6,dy+10,0xFFFF8A1E);gg.fill(dx+2,dy+4,dx+3,dy+5,0xFF000000);gg.fill(dx+6,dy+4,dx+7,dy+5,0xFF000000);}
    default->{gg.fill(dx+3,dy-1,dx+6,dy+1,0xFFFF8A1E);}
   }}
  // HUD
  gg.drawString(font,"§l§ePATO GULOSO",ax,ay-20,0xFFFFFF,true);
  String s="§e"+score+"  §c"+"❤".repeat(Math.max(0,lives))+"  §7fase "+level;gg.drawString(font,s,ax+aw-font.width(s),ay-20,0xFFFFFF,true);
  gg.drawCenteredString(font,best==0?"Recorde: —":"Recorde: §6"+best+" §7("+bestName+")",ax+aw/2,ay+ah+6,0xFFFFFF);
  String msg=over?"§c§lFIM DE JOGO §r§7— "+score+" pontos":!started?"§a§lPRONTO? §r§7setas/WASD jogam":ready>0?"§e§lPRONTO!":"";
  if(!msg.isEmpty()){int mw=font.width(msg)+16;gg.fill(ax+aw/2-mw/2,ay+ah/2-9,ax+aw/2+mw/2,ay+ah/2+9,0xCC000000);gg.drawCenteredString(font,msg,ax+aw/2,ay+ah/2-4,0xFFFFFF);}
  if(over||!started)gg.drawCenteredString(font,"§7Pão deixa as raposas com medo • Espaço joga • Backspace: menu",ax+aw/2,ay+ah/2+12,0xFFFFFF);
 }
 int[] at(Actor a,float pt,int x0,int y0){
  float x=Math.abs(a.x-a.px)>S?a.x:Mth.lerp(pt,a.px,a.x),y=Math.abs(a.y-a.py)>S?a.y:Mth.lerp(pt,a.py,a.y);
  return new int[]{x0+Math.round(x*C/S),y0+Math.round(y*C/S)};}
 static void eyes(GuiGraphics g,int x,int y,int dir){
  int ox=dir==0?1:dir==2?-1:0,oy=dir==1?1:dir==3?-1:0;
  g.fill(x+2,y+3,x+4,y+5,0xFFFFFFFF);g.fill(x+5,y+3,x+7,y+5,0xFFFFFFFF);
  g.fill(x+2+Math.max(0,ox),y+3+Math.max(0,oy),x+3+Math.max(0,ox),y+4+Math.max(0,oy),0xFF101040);
  g.fill(x+5+Math.max(0,ox),y+3+Math.max(0,oy),x+6+Math.max(0,ox),y+4+Math.max(0,oy),0xFF101040);}
}
