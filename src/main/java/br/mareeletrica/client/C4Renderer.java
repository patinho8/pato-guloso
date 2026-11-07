package br.mareeletrica.client;

import br.mareeletrica.C4;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Red countdown on the C4's little screen while it is armed. */
public final class C4Renderer implements BlockEntityRenderer<C4.C4Entity> {
 public C4Renderer(BlockEntityRendererProvider.Context c){}
 @Override public void render(C4.C4Entity c4,float pt,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  if(c4.at()<0||c4.getLevel()==null)return;
  long left=Math.max(0,c4.at()-c4.getLevel().getGameTime());
  String text=left>=24000*100?"999d":left>=72000*24?(left/24000)+"d":clock(left);
  var facing=c4.getBlockState().getValue(C4.C4Block.FACING);
  // same rotation as the blockstate (x then y)
  int x=switch(facing){case UP->0;case DOWN->180;default->270;},y=switch(facing){case NORTH->180;case EAST->270;case WEST->90;default->0;};
  pose.pushPose();
  pose.translate(.5,.5,.5);pose.mulPose(Axis.YP.rotationDegrees(-y));pose.mulPose(Axis.XP.rotationDegrees(-x));pose.translate(-.5,-.5,-.5);
  pose.translate(5F/16F,5.63F/16F,6.5F/16F);   // centre of the screen, just above it
  pose.mulPose(Axis.XP.rotationDegrees(90));
  var font=Minecraft.getInstance().font;
  float s=Math.min(.0105F,4.6F/16F/Math.max(1,font.width(text)));
  pose.scale(s,s,-s);
  boolean blink=left<200&&(c4.getLevel().getGameTime()/4)%2==0;
  font.drawInBatch(text,-font.width(text)/2F,-4F,blink?0xFFFFA0A0:0xFFFF2020,false,pose.last().pose(),buffers,Font.DisplayMode.POLYGON_OFFSET,0,0xF000F0);
  pose.popPose();
 }
 private static String clock(long ticks){long s=(ticks+19)/20;return s>=3600?String.format("%d:%02d:%02d",s/3600,(s/60)%60,s%60):String.format("%02d:%02d",s/60,s%60);}
}
