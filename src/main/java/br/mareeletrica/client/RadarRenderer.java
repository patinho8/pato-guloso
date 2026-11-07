package br.mareeletrica.client;

import br.mareeletrica.Radar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/** Spinning dish on the mast and live blips on the console screen (north up). */
public final class RadarRenderer implements BlockEntityRenderer<Radar.RadarEntity> {
 public RadarRenderer(BlockEntityRendererProvider.Context c){}
 @Override public AABB getRenderBoundingBox(Radar.RadarEntity r){return new AABB(r.getBlockPos()).inflate(2,3,2);}
 @Override public void render(Radar.RadarEntity r,float pt,PoseStack p,MultiBufferSource b,int light,int overlay){
  var mc=Minecraft.getInstance();if(mc.level==null)return;
  float t=(mc.level.getGameTime()%360)+pt;
  var f=r.getBlockState().getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING);
  // dish on the mast (back corner of the console, which turns with the block)
  p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(-f.toYRot()+180F));p.translate(-.5,0,-.5);
  p.translate(28/16F,32/16F,28/16F);p.mulPose(Axis.YP.rotationDegrees(t*6));p.translate(-.5,0,-.5);
  DmtGearLayer.model("radar_dish",p,b,light);p.popPose();
  int glow=LightTexture.FULL_BRIGHT;
  // sweep line
  p.pushPose();p.translate(.5,15.75/16,.5);p.mulPose(Axis.YP.rotationDegrees(-t*6));p.translate(-.5,-.5,-.5);DmtGearLayer.model("radar_sweep",p,b,glow);p.popPose();
  // blips
  var c=r.getBlockPos().getCenter();double scale=(14.5/16)/Radar.SCREEN_RANGE;int n=0;
  for(Entity e:mc.level.getEntitiesOfClass(Entity.class,new AABB(r.getBlockPos()).inflate(Radar.SCREEN_RANGE,Radar.SCREEN_RANGE,Radar.SCREEN_RANGE),e->Radar.kind(e)>0)){
   double dx=e.getX()-c.x,dz=e.getZ()-c.z;if(dx*dx+dz*dz>Radar.SCREEN_RANGE*Radar.SCREEN_RANGE)continue;
   int k=Radar.kind(e);if(++n>64)break;
   if(k==1&&(mc.level.getGameTime()/5)%2==1)continue;   // aircraft blink
   p.pushPose();p.translate(dx*scale,15.8/16-.5,dz*scale);
   float s=k==1?1.3F:1F;p.translate(.5,.5,.5);p.scale(s,s,s);p.translate(-.5,-.5,-.5);
   DmtGearLayer.model(k==1?"radar_blip_red":"radar_blip_yellow",p,b,glow);p.popPose();
  }
 }
}
