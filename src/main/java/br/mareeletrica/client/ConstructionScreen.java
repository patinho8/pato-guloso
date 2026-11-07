package br.mareeletrica.client;
import br.mareeletrica.*;
import java.util.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
/** Draws exposed voxel faces and allows orbit, zoom, slices, paint and axis scaling. */
public final class ConstructionScreen extends AbstractContainerScreen<ConstructionMenu>{
 private EditorPackets.Snapshot view;private final Map<Integer,int[]> voxels=new LinkedHashMap<>();private final List<Face> faces=new ArrayList<>();
 private float yaw=-.65F,pitch=.48F,zoom=1,panX,panY;private int chosen=16,slice=-1,axis=1,hover=-1,vx,vy,vw,vh;private boolean dirty=true,stretch;
 private final boolean[] usedColors=new boolean[17];
 private final Map<Integer,Integer> pendingPaint=new LinkedHashMap<>(),sentPaint=new LinkedHashMap<>();
 private boolean painting,awaitingPaint;private int brushTicks,dragAxis=-1,dragW,dragH,dragD,startW,startH,startD;private double dragX,dragY,dragStep,axisX,axisY;
 private record Handle(int axis,double x0,double y0,double x1,double y1,int color){}
 private EditBox sx,sy,sz,distance;private Button apply,project,layer;
 private static final int[][] NORMALS={{-1,0,0},{1,0,0},{0,-1,0},{0,1,0},{0,0,-1},{0,0,1}},CORNERS={{0,4,6,2},{1,3,7,5},{0,1,5,4},{2,6,7,3},{0,2,3,1},{4,5,7,6}};
 private record Face(int key,double[] x,double[] y,double depth,int color){}
 public ConstructionScreen(ConstructionMenu m,Inventory inv,Component title){super(m,inv,title);}
 private Button button(String s,int x,int y,int w,Runnable r){return addRenderableWidget(Button.builder(Component.literal(s),b->r.run()).bounds(leftPos+x,topPos+y,w,18).build());}
 private boolean brushBusy(){return awaitingPaint||!pendingPaint.isEmpty();}
 private void action(int id){if((id==0||id==1)&&brushBusy())return;if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 private void edit(int action,int a,int b,int c){if(action!=0&&brushBusy())return;PacketDistributor.sendToServer(new EditorPackets.Edit(menu.containerId,view==null?0:view.revision(),action,a,b,c));}
 private EditBox number(int x,int y,String name){var e=addRenderableWidget(new EditBox(font,leftPos+x,topPos+y,32,18,Component.literal(name)));e.setMaxLength(3);e.setFilter(s->s.matches("[0-9]*"));return e;}
 @Override protected void init(){imageWidth=Math.min(610,width-12);imageHeight=Math.min(350,height-12);super.init();vx=leftPos+8;vy=topPos+76;vw=imageWidth-86;vh=imageHeight-132;
  button("<",8,24,20,()->action(0));button(">",32,24,20,()->action(1));button("Esticar",58,24,54,()->{stretch=!stretch;sizeVisible();});
  sx=number(116,24,"Largura");sy=number(151,24,"Altura");sz=number(186,24,"Comprimento");apply=button("Aplicar",222,24,50,()->{try{edit(2,Integer.parseInt(sx.getValue()),Integer.parseInt(sy.getValue()),Integer.parseInt(sz.getValue()));}catch(NumberFormatException ignored){}});
  layer=button("Tudo",8,50,64,()->{slice=-1;dirty=true;});button("Eixo",76,50,38,()->{axis=(axis+1)%3;slice=-1;dirty=true;});button("−",118,50,20,()->{slice=Math.max(-1,slice-1);dirty=true;});button("+",142,50,20,()->{int max=view==null?1:axis==0?view.width():axis==1?view.height():view.depth();slice=Math.min(max-1,slice+1);dirty=true;});button("Repor",imageWidth-70,50,62,()->edit(3,0,0,0));
  int y=imageHeight-47;distance=number(8,y,"Distância");distance.setValue(""+Math.max(2,menu.data.get(18)));button("Distância",44,y,58,()->{try{PacketDistributor.sendToServer(new MachinePackets.Distance(menu.containerId,Integer.parseInt(distance.getValue())));}catch(NumberFormatException ignored){}});project=button("Projetar",106,y,62,()->action(3));button("Ligar",172,y,46,()->action(4));button("Área",222,y,42,()->action(2));sizeVisible();edit(0,0,0,0);dirty=true;
 }
 private void sizeVisible(){sx.visible=sy.visible=sz.visible=apply.visible=stretch;}
 public void receive(EditorPackets.Snapshot p){if(p.menu()!=menu.containerId)return;view=p;voxels.clear();Arrays.fill(usedColors,false);for(int i=2;i<p.cells().length;i+=3)usedColors[Math.floorMod(p.cells()[i],17)]=true;for(int i=0;i+2<p.cells().length;i+=3)voxels.put(p.cells()[i],new int[]{p.cells()[i+1],p.cells()[i+2]});sx.setValue(""+p.width());sy.setValue(""+p.height());sz.setValue(""+p.depth());
  for(var e:sentPaint.entrySet())if(voxels.containsKey(e.getKey())&&voxels.get(e.getKey())[1]!=e.getValue())pendingPaint.putIfAbsent(e.getKey(),e.getValue());for(var e:pendingPaint.entrySet())if(voxels.containsKey(e.getKey()))voxels.get(e.getKey())[1]=e.getValue();sentPaint.clear();awaitingPaint=false;dirty=true;}
 public void receivePaint(EditorPackets.Paint p){if(view==null||p.menu()!=menu.containerId)return;for(int key:p.keys())if(voxels.containsKey(key))voxels.get(key)[1]=pendingPaint.getOrDefault(key,p.color());view=new EditorPackets.Snapshot(view.menu(),p.revision(),view.width(),view.height(),view.depth(),view.cells());sentPaint.clear();awaitingPaint=false;Arrays.fill(usedColors,false);for(var v:voxels.values())usedColors[Math.floorMod(v[1],17)]=true;dirty=true;}
 @Override protected void containerTick(){super.containerTick();if(++brushTicks%4!=0||awaitingPaint||view==null||pendingPaint.isEmpty())return;int color=pendingPaint.values().iterator().next();var it=pendingPaint.entrySet().iterator();while(it.hasNext()&&sentPaint.size()<128){var e=it.next();if(e.getValue()==color){sentPaint.put(e.getKey(),color);it.remove();}}awaitingPaint=true;PacketDistributor.sendToServer(new EditorPackets.Stroke(menu.containerId,view.revision(),color,sentPaint.keySet().stream().mapToInt(Integer::intValue).toArray()));}
 private double pixelScale(){return view==null?1:Math.min(vw,vh)*.65/Math.max(view.width(),Math.max(view.height(),view.depth()))*zoom;}
 private List<Handle> handles(){List<Handle> out=new ArrayList<>();if(!stretch||view==null)return out;double cx=vx+vw/2.0+panX,cy=vy+vh/2.0+panY,len=Math.min(72,vh*.37);int[] colors={0xFFFF6262,0xFF71ED86,0xFF66B8FF};for(int i=0;i<3;i++){double[] a=rotate(i==0?1:0,i==1?1:0,i==2?1:0);double n=Math.hypot(a[0],a[1]);if(n<.1)continue;out.add(new Handle(i,cx,cy,cx+a[0]/n*len,cy-a[1]/n*len,colors[i]));}return out;}
 private static void line(GuiGraphics g,double x0,double y0,double x1,double y1,int color){int n=Math.max(1,(int)Math.hypot(x1-x0,y1-y0));for(int i=0;i<=n;i++){int x=(int)Math.round(x0+(x1-x0)*i/n),y=(int)Math.round(y0+(y1-y0)*i/n);g.fill(x-1,y-1,x+1,y+1,color);}}
 private void gizmos(GuiGraphics g){for(var h:handles()){line(g,h.x0,h.y0,h.x1,h.y1,h.color);double len=Math.hypot(h.x1-h.x0,h.y1-h.y0),ux=(h.x1-h.x0)/len,uy=(h.y1-h.y0)/len;line(g,h.x1,h.y1,h.x1-ux*9-uy*5,h.y1-uy*9+ux*5,h.color);line(g,h.x1,h.y1,h.x1-ux*9+uy*5,h.y1-uy*9-ux*5,h.color);g.drawString(font,"XYZ".substring(h.axis,h.axis+1),(int)h.x1+5,(int)h.y1-4,h.color,false);}}
 private int pick(double x,double y){int key=-1;if(inside(x,y))for(var f:faces)if(contains(f,x,y))key=f.key;return key;}
 private void brush(double x,double y){int key=pick(x,y);if(key<0)return;int[] v=voxels.get(key);if(v!=null&&v[1]!=chosen){pendingPaint.put(key,chosen);v[1]=chosen;dirty=true;}}
 public boolean hasSnapshot(){return view!=null&&!voxels.isEmpty();}
 private int coord(int k){return axis==0?VoxelRules.x(k):axis==1?VoxelRules.y(k):VoxelRules.z(k);}
 private double[] rotate(double x,double y,double z){double c=Math.cos(yaw),s=Math.sin(yaw),a=x*c+z*s,b=-x*s+z*c;return new double[]{a,y*Math.cos(pitch)-b*Math.sin(pitch),y*Math.sin(pitch)+b*Math.cos(pitch)};}
 private void rebuild(){dirty=false;faces.clear();if(view==null)return;double scale=pixelScale(),cx=vx+vw/2.0+panX,cy=vy+vh/2.0+panY;
  for(var e:voxels.entrySet()){int key=e.getKey(),x=VoxelRules.x(key),y=VoxelRules.y(key),z=VoxelRules.z(key);if(slice>=0&&coord(key)!=slice)continue;
   for(int f=0;f<6;f++){int[] n=NORMALS[f];if(rotate(n[0],n[1],n[2])[2]>=0)continue;int nx=x+n[0],ny=y+n[1],nz=z+n[2],next=VoxelRules.key(nx,ny,nz);if(nx>=0&&ny>=0&&nz>=0&&nx<128&&ny<128&&nz<128&&voxels.containsKey(next)&&(slice<0||coord(next)==slice))continue;
    double[] px=new double[4],py=new double[4];double depth=0;for(int i=0;i<4;i++){int corner=CORNERS[f][i];int w=dragAxis<0?view.width():dragW,h=dragAxis<0?view.height():dragH,d=dragAxis<0?view.depth():dragD;double[] r=rotate((x+(corner&1))*w/(double)view.width()-w/2.0,(y+(corner>>1&1))*h/(double)view.height()-h/2.0,(z+(corner>>2&1))*d/(double)view.depth()-d/2.0);px[i]=cx+r[0]*scale;py[i]=cy-r[1]*scale;depth+=r[2];}int rgb=ProjectionEditor.COLORS[Math.floorMod(e.getValue()[1],17)];double shade=f==3?1:f<2?.84:f==2?.60:.72;int color=0xFF000000|((int)((rgb>>16&255)*shade)<<16)|((int)((rgb>>8&255)*shade)<<8)|(int)((rgb&255)*shade);faces.add(new Face(key,px,py,depth,color));
   }
  }faces.sort(Comparator.comparingDouble(Face::depth).reversed());
 }
 private boolean inside(double x,double y){return x>=vx&&x<vx+vw&&y>=vy&&y<vy+vh;}
 private static boolean contains(Face f,double x,double y){boolean in=false;for(int i=0,j=3;i<4;j=i++)if((f.y[i]>y)!=(f.y[j]>y)&&x<(f.x[j]-f.x[i])*(y-f.y[i])/(f.y[j]-f.y[i])+f.x[i])in=!in;return in;}
 private void quad(BufferBuilder b,Face f,int color,double inset){double x=(f.x[0]+f.x[1]+f.x[2]+f.x[3])/4,y=(f.y[0]+f.y[1]+f.y[2]+f.y[3])/4;for(int i=0;i<4;i++)b.addVertex((float)(x+(f.x[i]-x)*inset),(float)(y+(f.y[i]-y)*inset),0).setColor(color);}
 @Override protected void renderBg(GuiGraphics g,float dt,int mx,int my){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF112633);g.fill(vx,vy,vx+vw,vy+vh,0xFF06141D);if(dirty)rebuild();hover=-1;if(inside(mx,my))for(var f:faces)if(contains(f,mx,my))hover=f.key;
  g.enableScissor(vx,vy,vx+vw,vy+vh);g.flush();if(!faces.isEmpty()){RenderSystem.setShader(GameRenderer::getPositionColorShader);RenderSystem.disableDepthTest();RenderSystem.disableCull();var b=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);for(var f:faces){quad(b,f,f.key==hover?0xFFE8FFFF:0xFF142630,1);quad(b,f,f.color,.93);}BufferUploader.drawWithShader(b.buildOrThrow());RenderSystem.enableCull();RenderSystem.enableDepthTest();}g.disableScissor();gizmos(g);
  for(int i=0;i<17;i++){int x=leftPos+imageWidth-67+i%3*20,y=vy+i/3*20;g.fill(x-1,y-1,x+17,y+17,i==chosen?0xFFFFFFFF:0xFF45626D);g.fill(x,y,x+16,y+16,0xFF000000|ProjectionEditor.COLORS[i]);if(usedColors[i])g.fill(x+11,y+11,x+15,y+15,0xFFEDF8FF);}
 }
 @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,Component.translatable("blueprint.mareeletrica."+menu.plan().id()),8,9,0xA9FFF0,false);if(view!=null&&!stretch)g.drawString(font,view.width()+" × "+view.height()+" × "+view.depth()+" | "+voxels.size()+" blocos",116,29,0xB2D5DA,false);layer.setMessage(Component.literal(slice<0?"Tudo "+"XYZ".charAt(axis):"XYZ".charAt(axis)+"="+slice));g.drawString(font,menu.data.get(0)+" FE",171,55,0x7EE3C3,false);project.setMessage(Component.literal(menu.data.get(24)==1?"Parar":"Projetar"));g.drawString(font,font.plainSubstrByWidth("Direito: girar · Meio: mover · Roda: zoom · Segure M1: pintar · Esticar: puxe X/Y/Z",imageWidth-16),8,imageHeight-22,0xA8C7D1,false);String status=hover<0?Component.translatable("status.mareeletrica."+menu.data.get(7)).getString():"Bloco "+VoxelRules.x(hover)+", "+VoxelRules.y(hover)+", "+VoxelRules.z(hover);g.drawString(font,font.plainSubstrByWidth(status,imageWidth-16),8,imageHeight-11,0x78DDDF,false);}
 @Override public boolean mouseClicked(double x,double y,int b){if(b==0){
  for(int i=0;i<17;i++){int px=leftPos+imageWidth-67+i%3*20,py=vy+i/3*20;if(x>=px&&x<px+16&&y>=py&&y<py+16){chosen=i;return true;}}
  if(!awaitingPaint&&pendingPaint.isEmpty())for(var h:handles())if(Math.hypot(x-h.x1,y-h.y1)<12){dragAxis=h.axis;startW=dragW=view.width();startH=dragH=view.height();startD=dragD=view.depth();dragX=x;dragY=y;double n=Math.hypot(h.x1-h.x0,h.y1-h.y0);axisX=(h.x1-h.x0)/n;axisY=(h.y1-h.y0)/n;dragStep=Math.max(1,pixelScale());return true;}
  if(inside(x,y)){int key=pick(x,y);if(key>=0&&hasShiftDown()&&!awaitingPaint&&pendingPaint.isEmpty())edit(1,key,chosen,1);else{painting=true;brush(x,y);}return true;}
 }return inside(x,y)||super.mouseClicked(x,y,b);}
 @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){
  if(b==0&&dragAxis>=0){int change=(int)Math.round(((x-dragX)*axisX+(y-dragY)*axisY)/dragStep);dragW=dragAxis==0?Math.max(1,Math.min(128,startW+change)):startW;dragH=dragAxis==1?Math.max(1,Math.min(128,startH+change)):startH;dragD=dragAxis==2?Math.max(1,Math.min(128,startD+change)):startD;sx.setValue(""+dragW);sy.setValue(""+dragH);sz.setValue(""+dragD);dirty=true;return true;}
  if(b==0&&painting){if(dirty)rebuild();int steps=Math.max(1,Math.min(64,(int)Math.ceil(Math.hypot(dx,dy)/3)));for(int i=1;i<=steps;i++)brush(x-dx+dx*i/steps,y-dy+dy*i/steps);return true;}
  if(inside(x,y)){if(b==1){yaw+=dx*.012F;pitch+=dy*.012F;dirty=true;}else if(b==2){panX+=dx;panY+=dy;dirty=true;}return true;}return super.mouseDragged(x,y,b,dx,dy);}
 @Override public boolean mouseReleased(double x,double y,int b){if(b==0){painting=false;if(dragAxis>=0){dragAxis=-1;edit(2,dragW,dragH,dragD);dirty=true;return true;}}return super.mouseReleased(x,y,b);}
 @Override public boolean mouseScrolled(double x,double y,double h,double v){if(inside(x,y)){zoom=Math.max(.25F,Math.min(18,zoom*(float)Math.pow(1.15,v)));dirty=true;return true;}return super.mouseScrolled(x,y,h,v);}
 @Override public void render(GuiGraphics g,int x,int y,float d){super.render(g,x,y,d);renderTooltip(g,x,y);}
}
