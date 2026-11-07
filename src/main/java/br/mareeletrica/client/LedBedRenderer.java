package br.mareeletrica.client;
import br.mareeletrica.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
/** Vanilla bed geometry and texture, with full-model color cycling and matching LED borders. */
public final class LedBedRenderer implements BlockEntityRenderer<LedBedEntity> {
 public static int color(double time){double t=(time%1260)/180;int i=(int)t;double f=t-i;int a=LedBedBlock.COLORS[i%7],b=LedBedBlock.COLORS[(i+1)%7];int red=(int)(((a>>16)&255)*(1-f)+((b>>16)&255)*f),green=(int)(((a>>8)&255)*(1-f)+((b>>8)&255)*f),blue=(int)((a&255)*(1-f)+(b&255)*f);return red<<16|green<<8|blue;}
 private final ModelPart head,foot;
 public LedBedRenderer(BlockEntityRendererProvider.Context c){head=c.bakeLayer(ModelLayers.BED_HEAD);foot=c.bakeLayer(ModelLayers.BED_FOOT);}
 @Override public void render(LedBedEntity bed,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  var s=bed.getBlockState();var facing=s.getValue(BedBlock.FACING);boolean h=s.getValue(BedBlock.PART)==BedPart.HEAD;
  pose.pushPose();pose.translate(0,.5625,0);pose.mulPose(Axis.XP.rotationDegrees(90));pose.translate(.5,.5,.5);pose.mulPose(Axis.ZP.rotationDegrees(180+facing.toYRot()));pose.translate(-.5,-.5,-.5);
  int rgb=color((bed.getLevel()==null?0:bed.getLevel().getGameTime())+partial);int bedColor=0xFF000000|rgb;
  (h?head:foot).render(pose,Sheets.BED_TEXTURES[DyeColor.WHITE.getId()].buffer(buffers,RenderType::entitySolid),0xF000F0,overlay,bedColor);pose.popPose();

  pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(-PotabilizerStructure.turn(facing)*90));pose.translate(-.5,0,-.5);
  var mc=net.minecraft.client.Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)),null,
   mc.getModelManager().getModel(ClientEvents.part(h?"led_bed_strip_head":"led_bed_strip_foot")),((rgb>>16)&255)/255F,((rgb>>8)&255)/255F,(rgb&255)/255F,0xF000F0,OverlayTexture.NO_OVERLAY);pose.popPose();

  if(h&&s.getValue(LedBedBlock.LIT))glow(pose,buffers,facing,rgb);
 }
 private static final net.minecraft.resources.ResourceLocation GLOW=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"textures/misc/led_glow.png");
 /** Coloured light: an additive glow pooled on the floor around the bed plus a soft halo in the air, in the bed's current colour. */
 private static void glow(PoseStack pose,MultiBufferSource buffers,net.minecraft.core.Direction facing,int rgb){
  var vc=buffers.getBuffer(RenderType.eyes(GLOW));
  float r=((rgb>>16)&255)/255F,g=((rgb>>8)&255)/255F,b=(rgb&255)/255F;
  var o=facing.getOpposite();float cx=.5F+o.getStepX()*.5F,cz=.5F+o.getStepZ()*.5F;
  var last=pose.last();
  float R=3.6F,y=.02F;   // floor pool
  quad(vc,last,new float[][]{{cx-R,y,cz-R},{cx-R,y,cz+R},{cx+R,y,cz+R},{cx+R,y,cz-R}},r,g,b,.55F);
  // halo facing the camera
  var cam=net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
  pose.pushPose();pose.translate(cx,.9F,cz);pose.mulPose(cam);
  float H=2.4F;quad(vc,pose.last(),new float[][]{{-H,-H,0},{H,-H,0},{H,H,0},{-H,H,0}},r,g,b,.22F);
  pose.popPose();
 }
 /** Both windings, so the glow shows from either side. */
 private static void quad(com.mojang.blaze3d.vertex.VertexConsumer vc,PoseStack.Pose last,float[][] p,float r,float g,float b,float a){
  float[][] uv={{0,0},{0,1},{1,1},{1,0}};
  for(int pass=0;pass<2;pass++)for(int k=0;k<4;k++){int i=pass==0?k:3-k;vc.addVertex(last,p[i][0],p[i][1],p[i][2]).setColor(r*a,g*a,b*a,a).setUv(uv[i][0],uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(last,0,1,0);}
 }
 @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(LedBedEntity bed){return new net.minecraft.world.phys.AABB(bed.getBlockPos()).inflate(4);}
 @Override public boolean shouldRenderOffScreen(LedBedEntity bed){return true;}
}
