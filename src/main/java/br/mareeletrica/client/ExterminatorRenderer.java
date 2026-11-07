package br.mareeletrica.client;
import br.mareeletrica.ExterminatorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
public final class ExterminatorRenderer extends EntityRenderer<ExterminatorEntity>{
 public ExterminatorRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=1.3F;}
 private void part(String name,PoseStack p,MultiBufferSource b,int light){var mc=Minecraft.getInstance();mc.getBlockRenderer().getModelRenderer().renderModel(p.last(),b.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS)),null,mc.getModelManager().getModel(ClientEvents.part(name)),1,1,1,light,OverlayTexture.NO_OVERLAY);}
 private void box(String model,PoseStack p,MultiBufferSource b,int light,double x,double y,double z,float w,float h,float d){p.pushPose();p.translate(x,y,z);p.scale(w,h,d);part(model,p,b,light);p.popPose();}
 private void strut(PoseStack p,MultiBufferSource b,int light,double ax,double ay,double az,double bx,double by,double bz){
  float dx=(float)(bx-ax),dy=(float)(by-ay),dz=(float)(bz-az),length=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);
  p.pushPose();p.translate(ax,ay,az);p.mulPose(new org.joml.Quaternionf().rotationTo(0,1,0,dx/length,dy/length,dz/length));p.scale(2,length,2);p.translate(-.5,0,-.5);part("guardian_leg",p,b,light);p.popPose();
 }
 public void render(ExterminatorEntity e,float yaw,float dt,PoseStack p,MultiBufferSource b,int light){p.pushPose();p.mulPose(Axis.YP.rotationDegrees(180-yaw));
  if(e.giant()){
   box("devourer_body",p,b,light,-2.2,1.1,-1.8,4.4F,3.8F,3.6F);float walk=(float)Math.sin((e.tickCount+dt)*.35)*25*(e.getDeltaMovement().horizontalDistanceSqr()>.002?1:0);
   for(int side:new int[]{-1,1})for(int z:new int[]{-1,1}){p.pushPose();p.translate(side*1.85,1.6,z*1.35);p.mulPose(Axis.XP.rotationDegrees(walk*side*z));box("devourer_leg",p,b,light,-.45,-1.6,-.5,.9F,1.9F,1F);p.popPose();}
   p.pushPose();p.translate(0,2.0,-1.7);p.mulPose(Axis.XP.rotationDegrees(e.weapon()==4?15+(float)Math.sin((e.tickCount+dt)*.65)*22:5));box("devourer_jaw",p,b,light,-1.65,-.55,-.8,3.3F,.8F,1.9F);p.popPose();
  }else{
   box("guardian_body",p,b,light,-.34,1.2,-.20,.68F,.91F,.40F);
   box("guardian_head",p,b,light,-.26,2.17,-.27,.52F,.56F,.52F);
   for(int side:new int[]{-1,1}){p.pushPose();p.translate(side*.19,1.26,0);p.mulPose(Axis.XP.rotationDegrees((float)Math.sin((e.tickCount+dt)*.42)*28*side*(e.getDeltaMovement().horizontalDistanceSqr()>.002?1:0)));box("guardian_leg",p,b,light,-.10,-1.26,-.12,.20F,1.26F,.24F);p.popPose();}
   p.pushPose();p.translate(-.46,1.99,0);p.mulPose(Axis.XP.rotationDegrees(20+(e.weapon()==3?(float)Math.sin((e.tickCount+dt)*.7)*45:0)));box("guardian_arm",p,b,light,-.1,-.82,-.10,.20F,.85F,.22F);box("guardian_sword",p,b,light,-.17,-.83,-.58,.34F,1.42F,.34F);p.popPose();
   p.pushPose();p.translate(.45,1.98,0);p.mulPose(Axis.XP.rotationDegrees(52));box("guardian_arm",p,b,light,-.10,-.72,-.10,.20F,.75F,.22F);p.popPose();
   box("guardian_machinegun",p,b,light,.23,1.20,-1.02,.52F,.52F,1.18F);
  }p.popPose();super.render(e,yaw,dt,p,b,light);
 }
 public ResourceLocation getTextureLocation(ExterminatorEntity e){return TextureAtlas.LOCATION_BLOCKS;}
}
