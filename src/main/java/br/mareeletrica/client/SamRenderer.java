package br.mareeletrica.client;

import br.mareeletrica.Missiles;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/** Anti-air launcher: the four-tube rack turns and elevates toward the locked target. */
public final class SamRenderer implements BlockEntityRenderer<Missiles.LauncherEntity> {
 public SamRenderer(BlockEntityRendererProvider.Context c){}
 @Override public void render(Missiles.LauncherEntity l,float pt,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  pose.pushPose();pose.translate(.5,0,.5);pose.scale(1.5F,1.5F,1.5F);pose.translate(-.5,0,-.5);   // big launcher
  DmtGearLayer.model("sam_base",pose,buffers,light);
  float yaw=l.yawO+Mth.wrapDegrees(l.yaw-l.yawO)*pt,pitch=Mth.lerp(pt,l.pitchO,l.pitch);
  pose.pushPose();
  pose.translate(.5,10F/16F,.5);
  pose.mulPose(Axis.YP.rotationDegrees(180F-yaw));
  pose.mulPose(Axis.XP.rotationDegrees(pitch));
  pose.translate(-.5,-10F/16F,-.5);
  DmtGearLayer.model("sam_rack",pose,buffers,light);
  pose.popPose();
  pose.popPose();
 }
 @Override public AABB getRenderBoundingBox(Missiles.LauncherEntity l){return new AABB(l.getBlockPos()).inflate(2);}
}
