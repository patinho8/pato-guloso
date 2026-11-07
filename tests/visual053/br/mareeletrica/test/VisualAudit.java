package br.mareeletrica.test;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Asset audit (test only, -Dmareeletrica.audit): lists items with missing names/models and screenshots every item icon. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualAudit {
 static int tick=-1,page=0;static List<ItemStack> items=new ArrayList<>();
 static boolean on(){return Boolean.getBoolean("mareeletrica.audit");}
 static final int PER=8*12;
 static final class Grid extends Screen {
  Grid(){super(Component.literal("audit"));}
  @Override public void render(GuiGraphics g,int mx,int my,float pt){
   g.fill(0,0,width,height,0xFF6A6A70);
   int cols=System.getProperty("mareeletrica.auditonly","").isEmpty()?8:4,cw=width/cols;float sc=cols==8?1.4F:3F;int rh=cols==8?height/12:height/4;
   for(int i=0;i<PER;i++){int k=page*PER+i;if(k>=items.size())break;var st=items.get(k);int x=(i%cols)*cw+4,y=(i/cols)*rh+2;
    g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(sc,sc,1);g.renderItem(st,0,0);g.pose().popPose();
    String n=k+" "+BuiltInRegistries.ITEM.getKey(st.getItem()).getPath();if(font.width(n)>cw-30)n=font.plainSubstrByWidth(n,cw-32)+"…";g.drawString(font,n,x+(int)(sc*18),y+6,0xFFFFFFFF,true);}
  }
  @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){}
  @Override public boolean isPauseScreen(){return false;}
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!on())return;var mc=Minecraft.getInstance();if(mc.player==null)return;tick++;
  if(tick==20){
   var missing=mc.getModelManager().getMissingModel();
   for(var item:BuiltInRegistries.ITEM){var id=BuiltInRegistries.ITEM.getKey(item);if(!id.getNamespace().equals("mareeletrica"))continue;String only=System.getProperty("mareeletrica.auditonly","");if(!only.isEmpty()&&!java.util.Arrays.asList(only.split(",")).contains(id.getPath()))continue;var st=new ItemStack(item);System.out.println("[AUDIT] #"+items.size()+" "+id);items.add(st);
    if(!I18n.exists(item.getDescriptionId())&&st.getHoverName().getString().startsWith("item."))System.out.println("[AUDIT] sem nome: "+id);
    if(!I18n.exists(item.getDescriptionId())&&st.getHoverName().getString().startsWith("block."))System.out.println("[AUDIT] sem nome: "+id);
    if(mc.getItemRenderer().getModel(st,null,null,0)==missing)System.out.println("[AUDIT] sem modelo de item: "+id);}
   for(var b:BuiltInRegistries.BLOCK){var id=BuiltInRegistries.BLOCK.getKey(b);if(!id.getNamespace().equals("mareeletrica"))continue;
    for(var s:b.getStateDefinition().getPossibleStates())if(mc.getBlockRenderer().getBlockModel(s)==missing&&s.getRenderShape()==net.minecraft.world.level.block.RenderShape.MODEL){System.out.println("[AUDIT] sem modelo de bloco: "+s);break;}}
   for(var t:BuiltInRegistries.ENTITY_TYPE){var id=BuiltInRegistries.ENTITY_TYPE.getKey(t);if(id.getNamespace().equals("mareeletrica")&&!I18n.exists(t.getDescriptionId()))System.out.println("[AUDIT] entidade sem nome: "+id);}
   System.out.println("[AUDIT] itens: "+items.size());
   mc.options.hideGui=false;mc.setScreen(new Grid());
  }
  if(tick>40&&(tick-40)%10==0){int p=(tick-40)/10-1;if(p*PER<items.size()){page=p;}}
  if(tick>40&&(tick-40)%10==5){if(page*PER<items.size())Screenshot.grab(mc.gameDirectory,"audit_"+page+".png",mc.getMainRenderTarget(),c->{});}
  if(tick>40&&(tick-40)/10>items.size()/PER+2){System.out.println("[AUDIT] DONE");mc.stop();}
 }
}
