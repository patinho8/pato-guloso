package br.mareeletrica.client;

import br.mareeletrica.Furniture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

/** 0.69.0: the TV picture — 6 little animated channels drawn as a 28×16 grid of glowing pixels. */
public final class TvRenderer implements BlockEntityRenderer<Furniture.TvEntity> {
 static final int GW=28,GH=16;
 public TvRenderer(BlockEntityRendererProvider.Context c){}
 @Override public void render(Furniture.TvEntity e,float pt,PoseStack p,MultiBufferSource b,int light,int overlay){
  var s=e.getBlockState();if(!s.hasProperty(Furniture.TvBlock.ON)||!s.getValue(Furniture.TvBlock.ON)||e.getLevel()==null)return;
  int ch=s.getValue(Furniture.TvBlock.CHANNEL);float t=(e.getLevel().getGameTime()+pt);
  p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(180-s.getValue(HorizontalDirectionalBlock.FACING).toYRot()));p.translate(-.5,0,-.5);
  VertexConsumer vc=b.getBuffer(RenderType.entityCutoutNoCull(PoolRenderer.WHITE));var ps=p.last();
  float x0=15.4F/16,x1=.6F/16,y0=13.4F/16,y1=3.6F/16,z=6.36F/16,cw=(x0-x1)/GW,chh=(y0-y1)/GH;
  long seed=e.getBlockPos().asLong();
  for(int i=0;i<GW;i++)for(int j=0;j<GH;j++){
   int c=pix(ch,i,j,t,seed);float ax=x0-i*cw,bx=ax-cw,ay=y0-j*chh,by=ay-chh;
   q(vc,ps,ax,ay,bx,by,z,c);}
  p.popPose();}
 static void q(VertexConsumer vc,PoseStack.Pose ps,float ax,float ay,float bx,float by,float z,int c){
  int col=0xFF000000|c;
  vc.addVertex(ps,ax,ay,z).setColor(col).setUv(.5F,.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(ps,0,0,-1);
  vc.addVertex(ps,ax,by,z).setColor(col).setUv(.5F,.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(ps,0,0,-1);
  vc.addVertex(ps,bx,by,z).setColor(col).setUv(.5F,.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(ps,0,0,-1);
  vc.addVertex(ps,bx,ay,z).setColor(col).setUv(.5F,.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(ps,0,0,-1);}
 static int hash(long a){a^=a>>>33;a*=0xff51afd7ed558ccdL;a^=a>>>33;a*=0xc4ceb9fe1a85ec53L;a^=a>>>33;return (int)a;}
 /** i = column (0 left), j = row (0 top) */
 static int pix(int ch,int i,int j,float t,long seed){
  switch(ch){
   case 0->{   // Desenho do Pato: a duck waddling across a sunny field
    int bg=j<11?(j<4?0x6EC8FF:0x8ED6FF):(j<12?0x3FA035:0x5DBB3A);
    if(Math.hypot(i-23,j-3)<2.2)bg=0xFFE45A;
    int dx=Math.floorMod((int)(t*.25F),GW+8)-6,dy=7+(((int)(t*.5F))%2);
    int x=i-dx,y=j-dy;
    if(x>=0&&x<=5&&y>=1&&y<=3)return 0xFFFFFF;if(x>=3&&x<=5&&y>=-1&&y<=1)return 0xFFFFFF;if(x==6&&y==0)return 0xFF9A20;if(x==4&&y==-1)return 0x111111;if((x==1||x==3)&&y==4)return 0xFF9A20;
    return bg;}
   case 1->{   // Surfe ao vivo: rolling waves and a tiny surfer
    float wave=7+2.2F*Mth.sin(i*.45F-t*.18F);int sx=Math.floorMod((int)(t*.15F),GW);
    if(j<wave)return j<3?0x9ED8FF:0xBEE6FF;
    if(j<wave+1.2F)return 0xF4FAFF;
    if(Math.abs(i-sx)<=1&&Math.abs(j-(int)(7+2.2F*Mth.sin(sx*.45F-t*.18F)))<=0&&i==sx)return 0xFF8A20;
    if(i==sx&&j<(int)wave&&j>=(int)wave-3)return j==(int)wave-3?0xE0B080:0x2050C0;
    return j<wave+4?0x2F8FD0:0x1A5FA0;}
   case 2->{   // Futebol: green field, ball and two players chasing it
    int bg=((i/4)%2==0)?0x3FA035:0x47B03C;if(i==GW/2||j==0||j==GH-1||i==0||i==GW-1)bg=0xE8F4E8;if(Math.hypot(i-GW/2,j-GH/2)>2.6&&Math.hypot(i-GW/2,j-GH/2)<3.4)bg=0xE8F4E8;
    float bx=GW/2+10*Mth.sin(t*.05F),by=GH/2+5*Mth.sin(t*.083F);if(Math.abs(i-bx)<.8&&Math.abs(j-by)<.8)return 0xFFFFFF;
    if(Math.abs(i-(bx-3+Mth.sin(t*.07F)))<.9&&Math.abs(j-by-1)<1.3)return 0xE03030;if(Math.abs(i-(bx+3))<.9&&Math.abs(j-by+1)<1.3)return 0x3050E0;
    return bg;}
   case 3->{   // Jornal da Maré: anchor at the desk, scrolling ticker
    if(j>=13){int k=Math.floorMod(i+(int)(t*.4F),12);return j==13?0xC81E28:(k<7&&k%2==0?0xFFFFFF:0xC81E28);}
    if(Math.hypot(i-9,j-5)<2.4)return 0xE0B080;if(j>=8&&j<13&&Math.abs(i-9)<4.5)return j==8&&Math.abs(i-9)<1?0xC0C0C0:0x203050;
    if(j>=11&&j<13)return 0x5A3A20;
    if(i>=17&&i<=25&&j>=2&&j<=9)return ((i+j+(int)(t*.1F))%5==0)?0x80D0FF:0x3070B0;
    return 0x14204A;}
   case 4->{   // Fliperama TV: a snake chasing an apple
    int bg=((i+j)%2==0)?0x0C0C1C:0x101028;int head=Math.floorMod((int)(t*.3F),GW*2);
    for(int k=0;k<6;k++){int pos=Math.floorMod(head-k,GW*2);int sx=pos<GW?pos:GW*2-1-pos,sy=pos<GW?6:9;if(i==sx&&j==sy)return k==0?0x7CFF4F:0x2FD14A;}
    if(i==(int)(GW*.7F)&&j==12)return 0xFF3B5C;return bg;}
   default->{int n=hash(seed*31+i*7919L+j*104729L+(long)(t*.5F)*15485863L)&255;return n<<16|n<<8|n;}   // chuvisco
  }
 }
}
