package br.mareeletrica.client;

import br.mareeletrica.GunPaint;
import br.mareeletrica.WeaponFamily;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** 0.70.0: the Paint Bench screen — pick a part, a colour (hue / saturation / brightness), a finish, a two-colour pattern
 *  and a sticker; the gun spins in the middle with the new paint before you confirm. */
public final class PaintScreen extends Screen {
 static final int W=384,H=232;
 final BlockPos pos;ItemStack gun,preview;CompoundTag work;
 int sel=-1;            // -1 = whole gun
 float h=0,s=.8F,b=.9F,h2=0,s2=0,b2=.1F;int fin=0,pat=0;
 int drag=-1;float spin=0,dragSpin=0;double lastMx;boolean rot;
 String toast="";int toastT;
 public static void open(BlockPos pos){Minecraft.getInstance().setScreen(new PaintScreen(pos));}
 PaintScreen(BlockPos pos){super(Component.literal("Bancada de Pintura"));this.pos=pos;var p=Minecraft.getInstance().player;
  gun=p.getMainHandItem().copy();work=GunPaint.paint(gun).copy();preview=gun.copy();
  boolean[] av=PaintClient.available(gun);for(int i=0;i<av.length;i++)if(work.contains("r"+i)){sel=-1;loadFrom(i);break;}}
 @Override public boolean isPauseScreen(){return false;}
 int x0(){return (width-W)/2;}int y0(){return (height-H)/2;}
 void loadFrom(int r){var c=work.getCompound("r"+r);float[] v=Color.RGBtoHSB(c.getInt("C")>>16&255,c.getInt("C")>>8&255,c.getInt("C")&255,null);h=v[0];s=v[1];b=v[2];
  float[] w=Color.RGBtoHSB(c.getInt("D")>>16&255,c.getInt("D")>>8&255,c.getInt("D")&255,null);h2=w[0];s2=w[1];b2=w[2];fin=c.getInt("F");pat=c.getInt("P");}
 int rgb(float hh,float ss,float bb){return Color.HSBtoRGB(hh,ss,bb)&0xFFFFFF;}
 /** writes the current settings into the selected part(s) */
 void paintSel(){
  var c=new CompoundTag();c.putInt("C",rgb(h,s,b));c.putInt("F",fin);c.putInt("P",pat);c.putInt("D",rgb(h2,s2,b2));
  boolean[] av=PaintClient.available(gun);
  for(int i=0;i<av.length;i++)if(av[i]&&(sel<0||sel==i))work.put("r"+i,c.copy());
  refresh();}
 void refresh(){preview=gun.copy();GunPaint.set(preview,work);}
 void beep(float p){Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK,p));}
 // ---------- layout ----------
 int[] partBtn(int i){return new int[]{x0()+6,y0()+24+(i+1)*16,74,14};}
 int[] clearBtn(){return new int[]{x0()+6,y0()+24+8*16+2,74,14};}
 int[] stickerBtn(int i){return new int[]{x0()+86+i*21,y0()+180,19,19};}
 int[] finBtn(int i){return new int[]{x0()+246+(i%2)*68,y0()+86+(i/2)*15,66,13};}
 int[] patBtn(int i){return new int[]{x0()+246+(i%2)*68,y0()+130+(i/2)*15,66,13};}
 int[] bar(int i){int y=i<3?y0()+30+i*12:y0()+174+(i-3)*11;return new int[]{x0()+246,y,134,i<3?8:7};}
 int[] applyBtn(){return new int[]{x0()+W-82,y0()+H-17,76,14};}
 int[] previewBox(){return new int[]{x0()+86,y0()+22,152,144};}
 static boolean in(int[] r,double mx,double my){return mx>=r[0]&&mx<r[0]+r[2]&&my>=r[1]&&my<r[1]+r[3];}
 void button(GuiGraphics g,int[] r,String t,boolean on,boolean enabled,int accent){
  boolean hov=in(r,lastMx,lastMy);
  g.fill(r[0],r[1],r[0]+r[2],r[1]+r[3],on?accent:hov&&enabled?0xFF3A4256:0xFF262B38);
  g.renderOutline(r[0],r[1],r[2],r[3],on?0xFFFFFFFF:0xFF4A5268);
  g.drawCenteredString(font,t,r[0]+r[2]/2,r[1]+(r[3]-8)/2,enabled?0xFFFFFFFF:0xFF6A7080);}
 double lastMy;
 @Override public void render(GuiGraphics g,int mx,int my,float pt){
  lastMx=mx;lastMy=my;
  super.render(g,mx,my,pt);
  int x=x0(),y=y0();
  g.fill(x,y,x+W,y+H,0xF0161A24);g.renderOutline(x,y,W,H,0xFF57C7FF);
  g.drawString(font,"🎨 Bancada de Pintura",x+8,y+7,0xFF7FE3FF);
  boolean[] av=PaintClient.available(gun);
  button(g,partBtn(-1),"Arma toda",sel==-1,true,0xFF2E7D4F);
  for(int i=0;i<GunPaint.PARTS.length;i++){boolean done=work.contains("r"+i);button(g,partBtn(i),GunPaint.PARTS[i]+(done?" •":""),sel==i,av[i],0xFF2E7D4F);}
  button(g,clearBtn(),"Limpar",false,true,0xFF8A2E2E);
  g.drawString(font,"§8(solvente)",x+20,y+24+9*16+2,0xFF888888);
  // preview
  int[] pb=previewBox();g.fill(pb[0],pb[1],pb[0]+pb[2],pb[1]+pb[3],0xFF0C0F16);g.renderOutline(pb[0],pb[1],pb[2],pb[3],0xFF2C3446);
  drawGun(g,pb,pt);
  g.drawString(font,"Adesivo",x+86,y+170,0xFFAAB4C8);
  int st=work.getInt("S");
  for(int i=0;i<GunPaint.STICKERS.length;i++){int[] r=stickerBtn(i);button(g,r,i==0?"✕":"",st==i,true,0xFF2E7D4F);
   if(i>0)g.blit(ResourceLocation.fromNamespaceAndPath("mareeletrica","textures/sticker/"+GunPaint.STICKER_TEX[i]+".png"),r[0]+2,r[1]+2,0,0,15,15,15,15);}
  // colour
  g.drawString(font,"Cor",x+246,y+20,0xFFAAB4C8);
  g.fill(x+356,y+18,x+380,y+28,0xFF000000|rgb(h,s,b));g.renderOutline(x+356,y+18,24,10,0xFFFFFFFF);
  slider(g,0,h,i->rgb(i,1,1));slider(g,1,s,i->rgb(h,i,Math.max(.3F,b)));slider(g,2,b,i->rgb(h,s,i));
  g.drawString(font,"Acabamento",x+246,y+76,0xFFAAB4C8);
  for(int i=0;i<4;i++)button(g,finBtn(i),GunPaint.FINISH[i],fin==i,true,new int[]{0xFF2E7D4F,0xFF6E7A8C,0xFFB89A2A,0xFF8A3FB5}[i]);
  g.drawString(font,"Padrão",x+246,y+120,0xFFAAB4C8);
  for(int i=0;i<4;i++)button(g,patBtn(i),GunPaint.PATTERN[i],pat==i,true,0xFF2E7D4F);
  int c2=pat>0?0xFFAAB4C8:0xFF555B66;g.drawString(font,"Cor 2 (do padrão)",x+246,y+164,c2);
  g.fill(x+356,y+162,x+380,y+171,0xFF000000|rgb(h2,s2,b2));
  slider(g,3,h2,i->rgb(i,1,1));slider(g,4,s2,i->rgb(h2,i,Math.max(.3F,b2)));slider(g,5,b2,i->rgb(h2,s2,i));
  // footer: cost and sprays
  var p=Minecraft.getInstance().player;int[] cost=GunPaint.cost(GunPaint.paint(gun),work),have=GunPaint.charges(p);
  boolean creative=p.getAbilities().instabuild,ok=true;
  String[] nm={"Tinta","Metal","Brilho","Arco","Solv."};var sb=new StringBuilder();
  for(int i=0;i<5;i++){boolean short_=!creative&&cost[i]>have[i];if(short_)ok=false;sb.append(short_?"§c":cost[i]>0?"§e":"§7").append(nm[i]).append(" ").append(cost[i]>0?cost[i]+"/":"").append(have[i]).append(i<4?"§8 · ":"");}
  g.drawString(font,sb.toString(),x+8,y+H-27,0xFFFFFFFF);
  int total=0;for(int c:cost)total+=c;
  g.drawString(font,creative?"§bCriativo: grátis":total==0?"§7Nada mudou":ok?"§aGasta "+total+" carga"+(total>1?"s":""):"§cFaltam sprays",x+8,y+H-14,0xFFFFFFFF);
  button(g,applyBtn(),"Pintar ✔",false,total>0&&(ok||creative),0xFF2E7D4F);
  if(toastT>0){toastT--;g.drawString(font,toast,x+200,y+7,0xFFFFE070);}
 }
 interface Grad{int at(float v);}
 void slider(GuiGraphics g,int i,float v,Grad grad){
  int[] r=bar(i);boolean dim=i>=3&&pat==0;
  for(int k=0;k<r[2];k++){int c=grad.at(k/(float)(r[2]-1));if(dim)c=(c>>1)&0x7F7F7F;g.fill(r[0]+k,r[1],r[0]+k+1,r[1]+r[3],0xFF000000|c);}
  g.renderOutline(r[0]-1,r[1]-1,r[2]+2,r[3]+2,0xFF3A4256);
  int px=r[0]+Math.round(v*(r[2]-1));g.fill(px-1,r[1]-2,px+2,r[1]+r[3]+2,0xFFFFFFFF);g.fill(px,r[1]-1,px+1,r[1]+r[3]+1,0xFF000000);}
 void drawGun(GuiGraphics g,int[] pb,float pt){
  var f=WeaponFamily.of(preview);if(f==null)return;
  float zMin=(float)ArmoryRenderer.muzzle(f)/16F,zMax=f.pistol()?.95F:preview.is(br.mareeletrica.MareEletrica.HUNTER.get())?2.3F:f==WeaponFamily.SNIPER?1.55F:1.45F;
  float len=zMax-zMin,sc=Math.min(pb[2]*.92F/len,pb[3]*.9F/1.2F);
  spin+=rot?0:.6F*Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
  float yaw=90+Mth(spin)+dragSpin;
  var pose=g.pose();pose.pushPose();
  pose.translate(pb[0]+pb[2]/2F,pb[1]+pb[3]/2F-4,220);
  pose.scale(sc,-sc,sc);
  pose.mulPose(Axis.XP.rotationDegrees(12));pose.mulPose(Axis.YP.rotationDegrees(yaw));
  pose.translate(-.5,-.55,-(zMin+zMax)/2);
  g.enableScissor(pb[0]+1,pb[1]+1,pb[0]+pb[2]-1,pb[1]+pb[3]-1);
  Lighting.setupForEntityInInventory();
  RifleRenderer.parts(preview,pose,g.bufferSource(),15728880,false);
  g.flush();Lighting.setupFor3DItems();
  g.disableScissor();
  pose.popPose();}
 static float Mth(float t){return (float)Math.sin(t*.02)*35;}
 @Override public boolean mouseClicked(double mx,double my,int btn){
  if(btn!=0)return super.mouseClicked(mx,my,btn);
  boolean[] av=PaintClient.available(gun);
  for(int i=-1;i<GunPaint.PARTS.length;i++)if(in(partBtn(i),mx,my)){if(i>=0&&!av[i])return true;sel=i;if(i>=0&&work.contains("r"+i))loadFrom(i);beep(1);return true;}
  if(in(clearBtn(),mx,my)){for(int i=0;i<av.length;i++)if(sel<0||sel==i)work.remove("r"+i);if(sel<0)work.remove("S");refresh();beep(.8F);return true;}
  for(int i=0;i<GunPaint.STICKERS.length;i++)if(in(stickerBtn(i),mx,my)){if(i==0)work.remove("S");else work.putInt("S",i);refresh();beep(1.2F);return true;}
  for(int i=0;i<4;i++)if(in(finBtn(i),mx,my)){fin=i;paintSel();beep(1.1F);return true;}
  for(int i=0;i<4;i++)if(in(patBtn(i),mx,my)){pat=i;paintSel();beep(1.1F);return true;}
  for(int i=0;i<6;i++)if(in(bar(i),mx,my)||in(new int[]{bar(i)[0]-2,bar(i)[1]-2,bar(i)[2]+4,bar(i)[3]+4},mx,my)){drag=i;setBar(i,mx);return true;}
  if(in(previewBox(),mx,my)){rot=true;return true;}
  if(in(applyBtn(),mx,my)){apply();return true;}
  return super.mouseClicked(mx,my,btn);}
 void setBar(int i,double mx){int[] r=bar(i);float v=(float)Math.clamp((mx-r[0])/(r[2]-1),0,1);
  switch(i){case 0->h=v;case 1->s=v;case 2->b=v;case 3->h2=v;case 4->s2=v;default->b2=v;}
  if(i>=3&&pat==0)pat=1;paintSel();}
 @Override public boolean mouseDragged(double mx,double my,int btn,double dx,double dy){
  if(drag>=0){setBar(drag,mx);return true;}
  if(rot){dragSpin+=(float)dx*1.6F;return true;}
  return super.mouseDragged(mx,my,btn,dx,dy);}
 @Override public boolean mouseReleased(double mx,double my,int btn){drag=-1;rot=false;return super.mouseReleased(mx,my,btn);}
 void apply(){
  var p=Minecraft.getInstance().player;int[] cost=GunPaint.cost(GunPaint.paint(gun),work),have=GunPaint.charges(p);int total=0;boolean ok=true;
  for(int i=0;i<5;i++){total+=cost[i];if(cost[i]>have[i])ok=false;}
  if(total==0)return;
  if(!ok&&!p.getAbilities().instabuild){toast="§cFaltam sprays — faça mais na bancada de trabalho";toastT=60;beep(.5F);return;}
  PacketDistributor.sendToServer(new GunPaint.Apply(pos,work.copy()));
  GunPaint.set(gun,work.copy());
  Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.FIRE_EXTINGUISH,1.7F,.5F));
  toast="§a🎨 Pintado!";toastT=40;}
 @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){renderTransparentBackground(g);}
}
