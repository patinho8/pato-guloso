package br.mareeletrica.client;

import br.mareeletrica.Scoreboard;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;

/** LED digits on the scoreboard screen, always lit. */
public final class ScoreboardRenderer implements BlockEntityRenderer<Scoreboard.Board> {
 private final Font font;
 public ScoreboardRenderer(BlockEntityRendererProvider.Context c){font=c.getFont();}
 @Override public AABB getRenderBoundingBox(Scoreboard.Board b){return new AABB(b.getBlockPos()).inflate(1.6,1,1.6);}
 @Override public void render(Scoreboard.Board b,float pt,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  var f=b.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
  pose.pushPose();
  pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(180F-f.toYRot()));pose.translate(0,.72,11.85/16-.5);
  pose.mulPose(Axis.YP.rotationDegrees(180F));
  float s=1/64F;pose.scale(s,-s,s);
  int glow=LightTexture.FULL_BRIGHT;
  // labels
  text(pose,buffers,b.tennis?"TÊNIS":"PLACAR",0,-24,0xFFFFD040,1F,glow);
  text(pose,buffers,"CASA",-48,-12,0xFFFF5A3A,.9F,glow);
  text(pose,buffers,"VISITANTE",48,-12,0xFF4AD0FF,.9F,glow);
  // big point digits
  text(pose,buffers,b.pointText(0),-48,6,0xFFFF3020,3.2F,glow);
  text(pose,buffers,b.pointText(1),48,6,0xFF20C8FF,3.2F,glow);
  text(pose,buffers,"x",0,8,0xFFDDDDDD,1.6F,glow);
  if(b.tennis){
   text(pose,buffers,"SETS "+b.sets[0]+" - "+b.sets[1],0,24,0xFFB0FFB0,.9F,glow);
   text(pose,buffers,"GAMES "+b.games[0]+" - "+b.games[1],0,34,0xFFB0FFB0,.9F,glow);
  }
  pose.popPose();
 }
 /** Draws text centred on (x,y) in board pixels, scaled. */
 private void text(PoseStack pose,MultiBufferSource buffers,String t,float x,float y,int color,float scale,int light){
  pose.pushPose();pose.translate(x,y,0);pose.scale(scale,scale,1);
  font.drawInBatch(t,-font.width(t)/2F,-4F,color,false,pose.last().pose(),buffers,Font.DisplayMode.POLYGON_OFFSET,0,light);
  pose.popPose();
 }
}
