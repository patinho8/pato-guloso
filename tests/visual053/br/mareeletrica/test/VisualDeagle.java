package br.mareeletrica.test;

import br.mareeletrica.*;
import br.mareeletrica.client.RifleRenderer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test only (-Dmareeletrica.deagle): the new Desert Eagle up close, in all 13 finishes and in the hand. */
@EventBusSubscriber(modid="mareeletrica",value=Dist.CLIENT)
public final class VisualDeagle {
 static int tick;static BlockPos o;
 static ItemStack de(int skin,int optic,int sup){var s=new ItemStack(WeaponFamily.DESERT_EAGLE.item());CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putBoolean("ArmoryFormat",true);t.putInt("Sticker",skin);if(optic>0){t.putBoolean("HasOptic",true);t.putInt("OpticVariant",optic);t.putInt("ArsenalZoom",optic);}t.putInt("Suppressor",sup);t.putInt("Rounds",7);});return s;}
 public static final class G extends Screen {
  final int mode;G(int m){super(Component.literal("d"));mode=m;}
  @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){g.fill(0,0,width,height,0xFF2A2F38);}
  void gun(GuiGraphics g,ItemStack s,float cx,float cy,float sc,float yaw,float pitch){
   var pose=g.pose();pose.pushPose();pose.translate(cx,cy,200);pose.scale(sc,-sc,sc);pose.mulPose(Axis.XP.rotationDegrees(pitch));pose.mulPose(Axis.YP.rotationDegrees(yaw));pose.translate(-.5,-.42,-.25);
   Lighting.setupForEntityInInventory();RifleRenderer.parts(s,pose,g.bufferSource(),15728880,true);g.flush();Lighting.setupFor3DItems();pose.popPose();}
  @Override public void render(GuiGraphics g,int mx,int my,float pt){
   renderBackground(g,mx,my,pt);
   if(mode==0){gun(g,de(0,0,0),width*.27F,height*.5F,300,90,0);gun(g,de(0,0,0),width*.75F,height*.5F,280,55,22);}
   else if(mode==1){gun(g,de(0,0,0),width*.27F,height*.5F,300,-90,0);gun(g,de(0,2,1),width*.75F,height*.5F,230,130,15);}
   else{int i=0;for(int sk=0;sk<=12;sk++){int c=i%5,r=i/5;gun(g,de(sk,0,0),width*(.1F+c*.2F),height*(.2F+r*.31F),120,90,0);g.drawString(font,""+sk,(int)(width*(.02F+c*.2F)),(int)(height*(.05F+r*.31F)),0xFFFFFF80);i++;}}
  }
  @Override public boolean isPauseScreen(){return false;}
 }
 static void shot(String n){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,"de_"+n+".png",mc.getMainRenderTarget(),c->{});}
 static void srv(Runnable r){Minecraft.getInstance().getSingleplayerServer().execute(r);}
 static net.minecraft.server.level.ServerPlayer sp(){return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  if(!Boolean.getBoolean("mareeletrica.deagle"))return;var mc=Minecraft.getInstance();if(mc.player==null||mc.getSingleplayerServer()==null)return;tick++;
  if(tick==60)srv(()->{var p=sp();var l=p.serverLevel();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);o=p.blockPosition().atY(150);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);
   for(int x=-8;x<=8;x++)for(int z=-30;z<=8;z++){l.setBlockAndUpdate(o.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState());for(int y=0;y<6;y++)l.setBlockAndUpdate(o.offset(x,y,z),Blocks.AIR.defaultBlockState());}
   for(int y=0;y<4;y++)l.setBlockAndUpdate(o.offset(0,y,-14),Blocks.TARGET.defaultBlockState());
   p.teleportTo(l,o.getX()+.5,o.getY(),o.getZ()+.5,180,3);});
  if(tick==70)mc.setScreen(new G(0));if(tick==80)shot("perto");
  if(tick==82)mc.setScreen(new G(1));if(tick==92)shot("perto2");
  if(tick==94)mc.setScreen(new G(2));if(tick==104)shot("skins");
  if(tick==106){mc.setScreen(null);srv(()->sp().setItemInHand(InteractionHand.MAIN_HAND,de(0,0,0)));}
  if(tick==122)shot("mao");
  if(tick==124)mc.options.keyUse.setDown(true);
  if(tick==142)shot("mira");
  if(tick==144){mc.options.keyUse.setDown(false);srv(()->sp().setItemInHand(InteractionHand.MAIN_HAND,de(1,1,1)));}
  if(tick==160)shot("mao_ouro");
  if(tick==162)mc.options.keyUse.setDown(true);
  if(tick==180)shot("mira_rmr");
  if(tick==182){mc.options.keyUse.setDown(false);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);}
  if(tick==196)shot("terceira");
  if(tick==198){mc.options.setCameraType(CameraType.FIRST_PERSON);System.out.println("[DEAGLE] DONE");}
 }
}
