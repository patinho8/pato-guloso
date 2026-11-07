package br.mareeletrica.client;

import br.mareeletrica.DmtGrab;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Draws the torn-out block and, while it is held, the DMT cable from the player's hip to it. */
public final class GrabbedBlockRenderer extends EntityRenderer<DmtGrab.Grabbed> {
 public GrabbedBlockRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.5F;}
 @Override public void render(DmtGrab.Grabbed e,float yaw,float dt,PoseStack p,MultiBufferSource b,int light){
  var state=e.block();
  p.pushPose();
  p.translate(0,.5,0);
  if(!e.held()){float t=e.tickCount+dt;p.mulPose(Axis.XP.rotationDegrees(t*14));p.mulPose(Axis.ZP.rotationDegrees(t*9));}
  else{float t=e.tickCount+dt;p.mulPose(Axis.YP.rotationDegrees(Mth.sin(t*.2F)*12));}
  p.translate(-.5,-.5,-.5);
  Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state,p,b,light,OverlayTexture.NO_OVERLAY);
  p.popPose();
  if(e.held()&&e.level().getEntity(e.ownerId()) instanceof Player owner){
   Vec3 self=new Vec3(Mth.lerp(dt,e.xo,e.getX()),Mth.lerp(dt,e.yo,e.getY()),Mth.lerp(dt,e.zo,e.getZ()));
   // one cable from the right launcher to the top of the block
   Vec3 hand=br.mareeletrica.DmtGrab.Grabbed.hand(owner).subtract(owner.position()).add(owner.getPosition(dt));
   var lines=b.getBuffer(RenderType.lines());
   Vec3 a=hand.subtract(self),end=new Vec3(0,1,0);
   Vec3 prev=a;int n=16;double sag=Math.min(.35,a.length()*.02);
   for(int i=1;i<=n;i++){double t=i/(double)n;Vec3 next=a.add(end.subtract(a).scale(t)).add(0,-Math.sin(Math.PI*t)*sag,0);Vec3 nn=next.subtract(prev).normalize();
    lines.addVertex(p.last(),(float)prev.x,(float)prev.y,(float)prev.z).setColor(15,16,19,255).setNormal(p.last(),(float)nn.x,(float)nn.y,(float)nn.z);
    lines.addVertex(p.last(),(float)next.x,(float)next.y,(float)next.z).setColor(15,16,19,255).setNormal(p.last(),(float)nn.x,(float)nn.y,(float)nn.z);prev=next;}
   p.pushPose();p.translate(-.12,.94,-.12);p.scale(.24F,.24F,.24F);DmtGearLayer.model("dmt_hook",p,b,light);p.popPose();
  }
  super.render(e,yaw,dt,p,b,light);
 }
 @Override public ResourceLocation getTextureLocation(DmtGrab.Grabbed e){return TextureAtlas.LOCATION_BLOCKS;}
}
