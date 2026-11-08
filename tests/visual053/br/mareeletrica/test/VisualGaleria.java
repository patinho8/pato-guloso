package br.mareeletrica.test;

import br.mareeletrica.*;
import br.mareeletrica.client.ArmoryRenderer;
import br.mareeletrica.client.RifleRenderer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.galeria): every gun in a grid, side and angled views, with and without attachments. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualGaleria {
 static int tick;
 static final String[] MODES={"lado","angulo","mira1","mira2","mira3","cima"};
 public static final class G extends Screen {
  final int mode;G(int m){super(Component.literal("g"));mode=m;}
  @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){g.fill(0,0,width,height,0xFF20242C);}
  @Override public void render(GuiGraphics g,int mx,int my,float pt){
   renderBackground(g,mx,my,pt);
   var fams=WeaponFamily.values();int cols=5,rows=(fams.length+1+cols-1)/cols;int cw=width/cols,ch=height/rows;
   for(int i=0;i<=fams.length;i++){
    ItemStack s;String label;
    if(i==fams.length){s=new ItemStack(MareEletrica.HUNTER.get());label="hunter";}else{var f=fams[i];s=new ItemStack(f.item());label=f.id;}
    var f=WeaponFamily.of(s);if(f==null)continue;
    if(mode>=1&&mode!=5||mode==5){CustomData.update(DataComponents.CUSTOM_DATA,s,t->{if(mode>=2&&mode<=4||mode==1||mode==5){t.putBoolean("ArmoryFormat",true);t.putBoolean("HasOptic",true);t.putInt("OpticVariant",mode>=2&&mode<=4?mode-1:3);}if(mode==1||mode==5){t.putInt("Suppressor",1);t.putBoolean("Bipod",true);}});}
    int cx=(i%cols)*cw,cy=(i/cols)*ch;
    float zMin=(float)ArmoryRenderer.muzzle(f)/16F-.15F,zMax=f.pistol()?.95F:s.is(MareEletrica.HUNTER.get())?2.3F:f==WeaponFamily.SNIPER?1.55F:1.5F;
    if(mode==1||mode==5)zMin-=.5F;
    float len=zMax-zMin,sc=Math.min(cw*.9F/len,ch*.8F/1.0F);
    var pose=g.pose();pose.pushPose();pose.translate(cx+cw/2F,cy+ch/2F+4,200);pose.scale(sc,-sc,sc);
    if(mode==1){pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(60));}
    else if(mode==5){pose.mulPose(Axis.XP.rotationDegrees(80));pose.mulPose(Axis.YP.rotationDegrees(90));}
    else pose.mulPose(Axis.YP.rotationDegrees(90));
    pose.translate(-.5,-.6,-(zMin+zMax)/2);
    g.enableScissor(cx+1,cy+1,cx+cw-1,cy+ch-1);
    Lighting.setupForEntityInInventory();RifleRenderer.parts(s,pose,g.bufferSource(),15728880,true);g.flush();Lighting.setupFor3DItems();
    g.disableScissor();pose.popPose();
    g.drawString(font,label,cx+3,cy+2,0xFFFFFF80);g.renderOutline(cx,cy,cw,ch,0xFF3A4050);
   }
  }
  @Override public boolean isPauseScreen(){return false;}
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.galeria"))return;var mc=Minecraft.getInstance();if(mc.player==null)return;tick++;
  int k=tick-60;if(k<0)return;int m=k/20;
  if(m<MODES.length){if(k%20==0)mc.setScreen(new G(m));if(k%20==15)Screenshot.grab(mc.gameDirectory,"gal_"+MODES[m]+".png",mc.getMainRenderTarget(),c->{});}
  else if(k==MODES.length*20){mc.setScreen(null);System.out.println("[GALERIA] DONE");}
 }
}
