package br.mareeletrica.client;

import br.mareeletrica.Fun;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Arcade cabinet: a menu, Snake (single player, high score kept in the machine), Pong (2 players on the same machine,
 *  or against the computer) and Pato Guloso (maze game, see {@link MazeGame}). Pong runs on the server; this screen just sends the paddle keys and draws what it receives. */
public final class ArcadeScreen extends Screen {
 enum Mode{MENU,SNAKE,PONG,MAZE,BLOCKS,FLAPPY,ROAD}
 static final int W=24,H=16,CELL=9;
 final BlockPos pos;final RandomSource rnd=RandomSource.create();Mode mode=Mode.MENU;int menuSel;
 final Deque<int[]> snake=new ArrayDeque<>();int dx=1,dy=0,ndx=1,ndy=0,fx,fy,score,step,tick;boolean running,over;
 static Fun.PongState pong;int pongDir;boolean upHeld,downHeld;
 final MazeGame maze;
 final ArcadeGames.Game[] extra=new ArcadeGames.Game[3];
 ArcadeGames.Game extra(){return mode==Mode.BLOCKS?extra[0]:mode==Mode.FLAPPY?extra[1]:mode==Mode.ROAD?extra[2]:null;}
 MazeGame newMaze(){return new MazeGame(()->beep(1.5F+(float)Math.random()*.1F),()->beep(.7F),()->{beep(.5F);beep(.4F);},()->beep(2F),sc->PacketDistributor.sendToServer(new Fun.Score(this.pos,sc,1)));}
 public ArcadeScreen(BlockPos pos){super(Component.literal("Fliperama"));this.pos=pos;maze=newMaze();reset();
  extra[0]=new ArcadeGames.Blocks(this::beep,sc->PacketDistributor.sendToServer(new Fun.Score(pos,sc,2)));extra[1]=new ArcadeGames.Flappy(this::beep,sc->PacketDistributor.sendToServer(new Fun.Score(pos,sc,3)));extra[2]=new ArcadeGames.Road(this::beep,sc->PacketDistributor.sendToServer(new Fun.Score(pos,sc,4)));}
 public static void open(BlockPos pos){Minecraft.getInstance().setScreen(new ArcadeScreen(pos));}
 public String mazeDebug(){return maze.debug();}public int mazeScore(){return maze.score;}public boolean mazeOver(){return maze.over;}
 public static String debugState(){return pong==null?"null":"phase="+pong.phase()+" bx="+pong.bx();}
 public static void state(Fun.PongState s){Minecraft.getInstance().execute(()->pong=s);}
 void send(int kind,int dir){PacketDistributor.sendToServer(new Fun.PongCmd(pos,kind,dir));}
 // ---------------- snake ----------------
 void reset(){snake.clear();for(int i=0;i<4;i++)snake.addLast(new int[]{8-i,H/2});dx=ndx=1;dy=ndy=0;score=0;step=6;over=false;running=false;food();}
 void food(){do{fx=rnd.nextInt(W);fy=rnd.nextInt(H);}while(on(fx,fy));}
 boolean on(int x,int y){for(var c:snake)if(c[0]==x&&c[1]==y)return true;return false;}
 void beep(float pitch){Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BIT.value(),pitch,.5F));}
 static final Mode[] MENU_MODES={Mode.SNAKE,Mode.PONG,Mode.MAZE,Mode.BLOCKS,Mode.FLAPPY,Mode.ROAD};
 @Override public void tick(){
  if(mode==Mode.MAZE){maze.tick();return;}
  if(extra()!=null){extra().tick();return;}
  if(mode!=Mode.SNAKE||!running||over)return;
  if(++tick<step)return;tick=0;
  dx=ndx;dy=ndy;var h=snake.peekFirst();int nx=h[0]+dx,ny=h[1]+dy;
  if(nx<0||ny<0||nx>=W||ny>=H||on(nx,ny)){over=true;running=false;beep(.5F);PacketDistributor.sendToServer(new Fun.Score(pos,score));return;}
  snake.addFirst(new int[]{nx,ny});
  if(nx==fx&&ny==fy){score++;beep(1.2F+Math.min(.8F,score*.03F));food();if(score%4==0&&step>2)step--;}
  else snake.removeLast();
 }
 // ---------------- keys ----------------
 static boolean up(int k){return k==GLFW.GLFW_KEY_UP||k==GLFW.GLFW_KEY_W;}
 static boolean down(int k){return k==GLFW.GLFW_KEY_DOWN||k==GLFW.GLFW_KEY_S;}
 void startMode(Mode m){mode=m;beep(1.4F);if(m==Mode.PONG){pong=null;send(0,0);}else if(m==Mode.MAZE)maze.newGame();else if(extra()!=null){extra().reset();extra().started=false;}else reset();}
 @Override public boolean keyPressed(int key,int scan,int mods){
  if(mode==Mode.MENU){
   if(up(key)||down(key)){menuSel=Math.floorMod(menuSel+(up(key)?-1:1),MENU_MODES.length);beep(1F);return true;}
   if(key>=GLFW.GLFW_KEY_1&&key<GLFW.GLFW_KEY_1+MENU_MODES.length){startMode(MENU_MODES[key-GLFW.GLFW_KEY_1]);return true;}
   if(key==GLFW.GLFW_KEY_ENTER||key==GLFW.GLFW_KEY_SPACE){startMode(MENU_MODES[menuSel]);return true;}
   return super.keyPressed(key,scan,mods);
  }
  if(key==GLFW.GLFW_KEY_BACKSPACE){if(mode==Mode.PONG)send(2,0);mode=Mode.MENU;return true;}
  if(extra()!=null){if(extra().key(key,true))return true;return super.keyPressed(key,scan,mods);}
  if(mode==Mode.MAZE){
   if(key==GLFW.GLFW_KEY_SPACE||key==GLFW.GLFW_KEY_ENTER){maze.start();return true;}
   int d=key==GLFW.GLFW_KEY_RIGHT||key==GLFW.GLFW_KEY_D?0:down(key)?1:key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_A?2:up(key)?3:-1;
   if(d>=0&&maze.key(d))return true;
   return super.keyPressed(key,scan,mods);
  }
  if(mode==Mode.PONG){
   if(up(key)){upHeld=true;paddle();return true;}if(down(key)){downHeld=true;paddle();return true;}
   if(key==GLFW.GLFW_KEY_SPACE){send(3,0);return true;}
   return super.keyPressed(key,scan,mods);
  }
  if(key==GLFW.GLFW_KEY_SPACE&&!running){if(over)reset();running=true;return true;}
  int x=0,y=0;
  if(up(key))y=-1;else if(down(key))y=1;else if(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_A)x=-1;else if(key==GLFW.GLFW_KEY_RIGHT||key==GLFW.GLFW_KEY_D)x=1;
  if(x!=0||y!=0){if(x!=-dx||y!=-dy){ndx=x;ndy=y;}if(!running&&!over)running=true;return true;}
  return super.keyPressed(key,scan,mods);
 }
 @Override public boolean keyReleased(int key,int scan,int mods){
  if(mode==Mode.PONG){if(up(key)){upHeld=false;paddle();return true;}if(down(key)){downHeld=false;paddle();return true;}}
  return super.keyReleased(key,scan,mods);
 }
 void paddle(){int d=(downHeld?1:0)-(upHeld?1:0);if(d!=pongDir){pongDir=d;send(1,d);}}
 @Override public void removed(){if(mode==Mode.PONG)send(2,0);super.removed();}
 @Override public boolean isPauseScreen(){return false;}
 // ---------------- drawing ----------------
 @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){g.fill(0,0,width,height,0x90000000);}
 @Override public void render(GuiGraphics g,int mx,int my,float pt){
  super.render(g,mx,my,pt);
  int gw=W*CELL,gh=H*CELL,x0=(width-gw)/2,y0=(height-gh)/2+6;
  g.fill(x0-10,y0-30,x0+gw+10,y0+gh+22,0xFF1A1440);g.fill(x0-6,y0-26,x0+gw+6,y0+gh+18,0xFF2E2470);
  g.fill(x0-2,y0-2,x0+gw+2,y0+gh+2,0xFF00E5FF);g.fill(x0,y0,x0+gw,y0+gh,0xFF070712);
  switch(mode){case MENU->menu(g,x0,y0,gw,gh);case SNAKE->snake(g,x0,y0,gw,gh);case PONG->pong(g,x0,y0,gw,gh);
   case BLOCKS,FLAPPY,ROAD->{var ga=extra();g.enableScissor(x0,y0,x0+gw,y0+gh);ga.render(g,font,x0,y0,gw,gh,pt);g.disableScissor();var be=Minecraft.getInstance().level!=null&&Minecraft.getInstance().level.getBlockEntity(pos) instanceof Fun.ArcadeEntity a?a:null;int id=mode==Mode.BLOCKS?2:mode==Mode.FLAPPY?3:4;
    g.drawCenteredString(font,(be==null||be.best(id)==0?"Recorde: —":"Recorde: §6"+be.best(id)+" §7("+be.bestName(id)+")")+" §8• Backspace: menu",x0+gw/2,y0+gh+6,0xFFFFFF);}
   case MAZE->{var be=Minecraft.getInstance().level!=null&&Minecraft.getInstance().level.getBlockEntity(pos) instanceof Fun.ArcadeEntity a?a:null;
    maze.render(g,font,x0,y0,gw,gh,pt,be==null?0:be.mazeBest,be==null?"":be.mazeBestName);}}
 }
 void menu(GuiGraphics g,int x0,int y0,int gw,int gh){
  g.drawCenteredString(font,"§l§bFLIPERAMA",x0+gw/2,y0-20,0xFFFFFF);
  String[] items={"1  COBRINHA §7(1 jogador)","2  PONG §7(2 jogadores ou vs computador)","3  PATO GULOSO §7(labirinto)","4  BLOCOS §7(encaixe as peças)","5  FLAPPY PATO §7(voe entre os canos)","6  ESTRADA §7(desvie dos carros)"};
  for(int i=0;i<items.length;i++){int y=y0+gh/2-52+i*18;if(i==menuSel)g.fill(x0+24,y-5,x0+gw-24,y+13,0xFF2E2470);g.drawCenteredString(font,(i==menuSel?"§e▶ ":"")+items[i],x0+gw/2,y,0xFFFFFF);}
  g.drawCenteredString(font,"§7↑↓ escolhe • Enter joga • Esc sai",x0+gw/2,y0+gh+6,0xFFFFFF);
 }
 void snake(GuiGraphics g,int x0,int y0,int gw,int gh){
  for(int i=0;i<W;i++)for(int j=0;j<H;j++)if((i+j)%2==0)g.fill(x0+i*CELL,y0+j*CELL,x0+i*CELL+CELL,y0+j*CELL+CELL,0xFF0C0C1C);
  g.fill(x0+fx*CELL+1,y0+fy*CELL+1,x0+fx*CELL+CELL-1,y0+fy*CELL+CELL-1,0xFFFF3B5C);g.fill(x0+fx*CELL+3,y0+fy*CELL+1,x0+fx*CELL+5,y0+fy*CELL+3,0xFFFFB0C0);
  int i=0;for(var c:snake){int col=i==0?0xFF7CFF4F:(i%2==0?0xFF2FD14A:0xFF27B53F);g.fill(x0+c[0]*CELL+1,y0+c[1]*CELL+1,x0+c[0]*CELL+CELL-1,y0+c[1]*CELL+CELL-1,col);i++;}
  var head=snake.peekFirst();if(head!=null){int hx=x0+head[0]*CELL,hy=y0+head[1]*CELL;g.fill(hx+2,hy+2,hx+4,hy+4,0xFF000000);g.fill(hx+5,hy+2,hx+7,hy+4,0xFF000000);}
  var be=Minecraft.getInstance().level!=null&&Minecraft.getInstance().level.getBlockEntity(pos) instanceof Fun.ArcadeEntity a?a:null;
  g.drawString(font,"§l§bCOBRINHA",x0,y0-20,0xFFFFFF,true);g.drawString(font,"Pontos: §e"+score,x0+gw-70,y0-20,0xFFFFFF,true);
  g.drawCenteredString(font,be==null||be.best==0?"Recorde: —":"Recorde: §6"+be.best+" §7("+be.bestName+")",x0+gw/2,y0+gh+6,0xFFFFFF);
  if(!running){g.fill(x0+20,y0+gh/2-18,x0+gw-20,y0+gh/2+18,0xCC000000);
   g.drawCenteredString(font,over?"§c§lFIM DE JOGO §r§7— "+score+" pontos":"§a§lPRONTO?",x0+gw/2,y0+gh/2-12,0xFFFFFF);
   g.drawCenteredString(font,"§7Espaço/setas jogam • Backspace volta ao menu",x0+gw/2,y0+gh/2+4,0xFFFFFF);}
 }
 void pong(GuiGraphics g,int x0,int y0,int gw,int gh){
  var s=pong!=null&&pong.pos().equals(pos)?pong:null;
  float sx=gw/Fun.Pong.W,sy=gh/Fun.Pong.H;
  for(int y=0;y<gh;y+=8)g.fill(x0+gw/2-1,y0+y,x0+gw/2+1,y0+y+4,0xFF2A2A44);   // net
  if(s==null){g.drawCenteredString(font,"§7Conectando...",x0+gw/2,y0+gh/2,0xFFFFFF);return;}
  int ph=(int)(Fun.Pong.PH*sy);
  g.fill(x0+(int)(6*sx),y0+(int)(s.ly()*sy)-ph/2,x0+(int)(10*sx),y0+(int)(s.ly()*sy)+ph/2,s.side()==0?0xFF7CFF4F:0xFFE0E0E0);
  g.fill(x0+(int)((Fun.Pong.W-10)*sx),y0+(int)(s.ry()*sy)-ph/2,x0+(int)((Fun.Pong.W-6)*sx),y0+(int)(s.ry()*sy)+ph/2,s.side()==1?0xFF7CFF4F:0xFFE0E0E0);
  if(s.phase()==2){int bs=(int)(Fun.Pong.BS*sx);g.fill(x0+(int)(s.bx()*sx)-bs/2,y0+(int)(s.by()*sy)-bs/2,x0+(int)(s.bx()*sx)+bs/2,y0+(int)(s.by()*sy)+bs/2,0xFFFFE040);}
  g.drawString(font,"§l"+s.ls(),x0+gw/2-24,y0+6,0xFFFFFF,true);g.drawString(font,"§l"+s.rs(),x0+gw/2+18,y0+6,0xFFFFFF,true);
  g.drawString(font,"§b"+(s.left().isEmpty()?"—":s.left()),x0,y0-20,0xFFFFFF,true);
  String r=s.right().isEmpty()?"§7esperando...":"§d"+s.right();g.drawString(font,r,x0+gw-font.width(r),y0-20,0xFFFFFF,true);
  String msg=switch(s.phase()){
   case 0->s.right().isEmpty()?"§eEsperando o 2º jogador abrir este fliperama... §7(Espaço: jogar contra o computador)":"";
   case 1->"§a"+(s.timer()/20+1)+"...";
   case 3->"§6§l"+(s.ls()>s.rs()?s.left():s.right())+" venceu!";
   default->"";};
  if(!msg.isEmpty()){g.fill(x0+8,y0+gh/2-12,x0+gw-8,y0+gh/2+14,0xCC000000);g.drawCenteredString(font,msg,x0+gw/2,y0+gh/2-4,0xFFFFFF);}
  g.drawCenteredString(font,"§7"+(s.side()==0?"Você é a raquete da ESQUERDA":s.side()==1?"Você é a raquete da DIREITA":"Assistindo")+" • W/S ou ↑/↓ • primeiro a 7 • Backspace: menu",x0+gw/2,y0+gh+6,0xFFFFFF);
 }
}
