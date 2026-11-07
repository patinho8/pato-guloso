package br.mareeletrica.client;

import br.mareeletrica.Games;
import br.mareeletrica.RareEgg;
import br.mareeletrica.Support;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** 0.65.4: renderers for the rare egg, the supply crate, golf / bowling balls and bowling pins (all drawn from block-model parts). */
public final class PlayRenderers {
 private PlayRenderers(){}
 public static final String[] PARTS={"rare_egg","supply_crate","supply_chute","golf_ball_part","bowling_ball_part","bowling_pin"};
 abstract static class Base<T extends Entity> extends EntityRenderer<T> {
  Base(EntityRendererProvider.Context c,float shadow){super(c);shadowRadius=shadow;}
  @Override public ResourceLocation getTextureLocation(T e){return TextureAtlas.LOCATION_BLOCKS;}
  static void part(String n,PoseStack p,MultiBufferSource b,int light){FrogRenderer.part(n,p,b,light,OverlayTexture.NO_OVERLAY);}
  /** rotate around a pivot given in model pixels */
  static void around(PoseStack p,float x,float y,float z,com.mojang.math.Axis axis,float deg){p.translate(x/16F,y/16F,z/16F);p.mulPose(axis.rotationDegrees(deg));p.translate(-x/16F,-y/16F,-z/16F);}
 }
 public static final class Egg extends Base<RareEgg> {
  public Egg(EntityRendererProvider.Context c){super(c,.25F);}
  @Override public void render(RareEgg e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   p.pushPose();p.translate(-.5,0,-.5);float w=Mth.lerp(pt,e.wobbleO,e.wobble);
   around(p,8,0,8,Axis.ZP,w*25F);around(p,8,0,8,Axis.XP,w*12F);
   part("rare_egg",p,b,light);p.popPose();super.render(e,yaw,pt,p,b,light);
  }
 }
 public static final class Crate extends Base<Support.Crate> {
  public Crate(EntityRendererProvider.Context c){super(c,.6F);}
  @Override public void render(Support.Crate e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   p.pushPose();p.translate(-.5,0,-.5);float s=Mth.lerp(pt,e.swayO,e.sway);
   around(p,8,30,8,Axis.ZP,s);part("supply_crate",p,b,light);
   p.translate(0,1,0);part("supply_chute",p,b,light);p.popPose();super.render(e,yaw,pt,p,b,light);
  }
 }
 public static final class Golf extends Base<Games.GolfBall> {
  public Golf(EntityRendererProvider.Context c){super(c,.1F);}
  @Override public void render(Games.GolfBall e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   p.pushPose();p.translate(-.5,0,-.5);around(p,8,1.6F,8,Axis.XP,Mth.lerp(pt,e.spinO,e.spin));part("golf_ball_part",p,b,light);p.popPose();
   super.render(e,yaw,pt,p,b,light);
  }
 }
 public static final class Bowling extends Base<Games.BowlingBall> {
  public Bowling(EntityRendererProvider.Context c){super(c,.25F);}
  @Override public void render(Games.BowlingBall e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   var v=e.getDeltaMovement();float dir=v.horizontalDistanceSqr()>1e-6?(float)Math.toDegrees(Math.atan2(-v.x,v.z)):0;
   p.pushPose();p.mulPose(Axis.YP.rotationDegrees(-dir));p.translate(-.5,0,-.5);around(p,8,3.6F,8,Axis.XP,Mth.lerp(pt,e.spinO,e.spin));
   int c=e.color();var mc=net.minecraft.client.Minecraft.getInstance();
   mc.getBlockRenderer().getModelRenderer().renderModel(p.last(),b.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)),null,
    mc.getModelManager().getModel(ClientEvents.part("bowling_ball_part")),(c>>16&255)/255F,(c>>8&255)/255F,(c&255)/255F,light,OverlayTexture.NO_OVERLAY);
   p.popPose();super.render(e,yaw,pt,p,b,light);
  }
 }
 public static final class Pin extends Base<Games.Pin> {
  public Pin(EntityRendererProvider.Context c){super(c,.15F);}
  @Override public void render(Games.Pin e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   p.pushPose();p.mulPose(Axis.YP.rotationDegrees(-e.fallDir()));p.translate(-.5,0,-.5);
   around(p,8,0,8,Axis.XP,Mth.lerp(pt,e.tipO,e.tip));part("bowling_pin",p,b,light);p.popPose();super.render(e,yaw,pt,p,b,light);
  }
 }
}
