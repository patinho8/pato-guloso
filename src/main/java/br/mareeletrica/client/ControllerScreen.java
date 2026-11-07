package br.mareeletrica.client;

import br.mareeletrica.ControlNet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Power Controller: global limit and on/off cycle, plus every device linked with a pendrive (switch, energy share, camera view, remove). */
public final class ControllerScreen extends Screen {
 private static final int[] LIMITS={10,25,50,75,100};
 private final BlockPos pos;private CompoundTag data;
 public ControllerScreen(BlockPos pos,CompoundTag data){super(Component.literal("Bloco de Controle"));this.pos=pos;this.data=data;}
 /** Called when the server answers an edit: refresh in place. */
 public void update(CompoundTag data){this.data=data;rebuildWidgets();}
 public BlockPos pos(){return pos;}
 private void send(int action,int index,int value){PacketDistributor.sendToServer(new ControlNet.Edit(pos,action,index,value));}
 private static String sec(int s){return s<=0?"sempre":s+" s";}
 private int top(){return Math.max(8,height/2-118);}
 @Override protected void init(){
  int cx=width/2,y=top()+22;
  int limit=data.getInt("Limit"),on=data.getInt("On"),off=data.getInt("Off");
  addRenderableWidget(Button.builder(Component.literal("Limite de energia: "+limit+"%"),b->{int idx=0;for(int i=0;i<LIMITS.length;i++)if(LIMITS[i]<=limit)idx=i;send(0,-1,LIMITS[(idx+1)%LIMITS.length]);}).bounds(cx-150,y,120,20).build());
  addRenderableWidget(Button.builder(Component.literal("-"),b->send(1,-1,Math.max(0,on-5))).bounds(cx-26,y,16,20).build());
  addRenderableWidget(Button.builder(Component.literal("Ligado "+sec(on)),b->{}).bounds(cx-10,y,72,20).build());
  addRenderableWidget(Button.builder(Component.literal("+"),b->send(1,-1,on+5)).bounds(cx+62,y,16,20).build());
  addRenderableWidget(Button.builder(Component.literal("-"),b->send(2,-1,Math.max(0,off-5))).bounds(cx+80,y,16,20).build());
  addRenderableWidget(Button.builder(Component.literal("Deslig. "+sec(off)),b->{}).bounds(cx+96,y,72,20).build());
  addRenderableWidget(Button.builder(Component.literal("+"),b->send(2,-1,off+5)).bounds(cx+168,y,16,20).build());
  var links=data.getList("Links",10);
  List<CameraView.Cam> cams=new ArrayList<>();List<Integer> camIndex=new ArrayList<>();
  for(int i=0;i<links.size();i++){var e=links.getCompound(i);if(e.getBoolean("Camera")){camIndex.add(i);cams.add(new CameraView.Cam(BlockPos.of(e.getLong("Pos")),Direction.from3DDataValue(e.getInt("Facing")),"Câmera "+(cams.size()+1)));}}
  for(int i=0;i<links.size();i++){
   var e=links.getCompound(i);final int idx=i;int ry=y+44+i*22;
   boolean camera=e.getBoolean("Camera"),missing=e.getBoolean("Missing"),machine=e.getBoolean("Machine");
   if(camera){final int ci=camIndex.indexOf(i);addRenderableWidget(Button.builder(Component.literal("Ver"),b->CameraView.start(cams,ci)).bounds(cx+20,ry,48,20).build());}
   else if(machine&&!missing){
    addRenderableWidget(Button.builder(Component.literal(e.getBoolean("On")?"§aLigado":"§cDesligado"),b->send(3,idx,0)).bounds(cx+20,ry,62,20).build());
    int share=e.getInt("Share");
    addRenderableWidget(Button.builder(Component.literal("-"),b->send(4,idx,Math.max(0,share-10))).bounds(cx+86,ry,16,20).build());
    addRenderableWidget(Button.builder(Component.literal(share+"%"),b->{}).bounds(cx+102,ry,40,20).build());
    addRenderableWidget(Button.builder(Component.literal("+"),b->send(4,idx,Math.min(100,share+10))).bounds(cx+142,ry,16,20).build());
   }
   addRenderableWidget(Button.builder(Component.literal("✕"),b->send(5,idx,0)).bounds(cx+162,ry,20,20).build());
  }
  addRenderableWidget(Button.builder(Component.literal("Fechar"),b->onClose()).bounds(cx-40,y+50+Math.max(1,links.size())*22,80,20).build());
 }
 @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
  super.render(g,mouseX,mouseY,partial);
  int cx=width/2,y=top();
  g.drawCenteredString(font,title,cx,y,0xFF5BC8E0);
  int cap=Math.max(1,data.getInt("Capacity"));
  g.drawCenteredString(font,Component.literal("Energia guardada: "+data.getInt("Energy")*100/cap+"%"),cx,y+10,0xFFAAAAAA);
  var links=data.getList("Links",10);
  g.drawString(font,"Aparelhos conectados ("+links.size()+"/8)  •  Energia: Ligado + parte de cada um",cx-150,y+56,0xFFFFFFFF,false);
  if(links.isEmpty())g.drawString(font,"Nenhum. Shift + clique direito num aparelho ou câmera com um Pen Drive, depois clique aqui com ele.",cx-150,y+70,0xFF999999,false);
  for(int i=0;i<links.size();i++){
   var e=links.getCompound(i);int ry=y+66+i*22;var p=BlockPos.of(e.getLong("Pos"));
   String name=e.getBoolean("Missing")?"§c(sumiu)":Component.translatable(e.getString("Name")).getString();
   g.drawString(font,name,cx-150,ry+2,0xFFFFFFFF,false);
   String info=p.getX()+", "+p.getY()+", "+p.getZ()+(e.contains("Charge")?"  •  carga "+e.getInt("Charge")+"%":"");
   g.drawString(font,info,cx-150,ry+12,0xFF888888,false);
  }
 }
 @Override public boolean isPauseScreen(){return false;}
}
