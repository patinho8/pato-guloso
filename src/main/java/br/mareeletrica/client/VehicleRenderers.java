package br.mareeletrica.client;

import br.mareeletrica.Vehicles;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Helicopter and tank renderers. Models are drawn at scale 4 (1 model pixel = 1/4 block); noses point -z. */
public final class VehicleRenderers {
 private VehicleRenderers(){}
 /** Painted variant of a vehicle model (0 = olive drab, the plain model). */
 static String paint(String model,Vehicles.Base v){int p=v.paint();return p==0?model:model+"_p"+p;}
 /** The local player rides this vehicle and looks through their own eyes. */
 static boolean fromInside(net.minecraft.world.entity.Entity e){var mc=net.minecraft.client.Minecraft.getInstance();return mc.player!=null&&e.hasPassenger(mc.player)&&mc.options.getCameraType().isFirstPerson();}
 public static final class Heli extends EntityRenderer<Vehicles.Helicopter> {
  public Heli(EntityRendererProvider.Context c){super(c);shadowRadius=3.2F;}
  @Override public ResourceLocation getTextureLocation(Vehicles.Helicopter e){return TextureAtlas.LOCATION_BLOCKS;}
  @Override public void render(Vehicles.Helicopter e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   float y=Mth.rotLerp(pt,e.yRotO,e.getYRot());
   float tilt=Mth.clamp((float)e.moved*18F,0,14F);
   p.pushPose();
   p.mulPose(Axis.YP.rotationDegrees(180F-y));
   p.translate(0,1.9,0);p.mulPose(Axis.XP.rotationDegrees(-tilt));p.translate(0,-1.9,0);   // nose dips when flying forward
   p.scale(4,4,4);p.translate(-.5,0,-.5);
   // inside the cockpit in first person the body would cover the whole screen: only the rotors are drawn then
   // inside, in first person: the cabin as a pilot sees it (dashboard, posts, nose bubble, skids and pods out of the windows)
   if(!fromInside(e))DmtGearLayer.model(paint("heli_body",e),p,b,light);
   else{DmtGearLayer.model("heli_fp",p,b,light);DmtGearLayer.model("heli_fp_glow",p,b,0xF000F0);}
   float a=Mth.lerp(pt,e.rotorAngleO,e.rotorAngle);
   p.pushPose();p.translate(.5,16.6/16,.5);p.mulPose(Axis.YP.rotationDegrees(a));p.translate(-.5,0,-.5);
   // from the cockpit a fast rotor is only a blur: the blades are drawn when it turns slowly
   if(!fromInside(e)||e.rotor<.35F)for(int i=0;i<4;i++){p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(i*90));p.translate(-.5,0,-.5);DmtGearLayer.model("heli_blade",p,b,light);p.popPose();}
   p.popPose();
   if(!fromInside(e)){p.pushPose();p.translate(9.6/16F,14/16F,30.5/16F);p.mulPose(Axis.XP.rotationDegrees(a*1.8F));p.translate(-.5,-.5,-.5);DmtGearLayer.model("heli_tail_rotor",p,b,light);p.popPose();}
   p.popPose();
   super.render(e,yaw,pt,p,b,light);
  }
 }
 public static final class Tank extends EntityRenderer<Vehicles.Tank> {
  public Tank(EntityRendererProvider.Context c){super(c);shadowRadius=3.4F;}
  @Override public ResourceLocation getTextureLocation(Vehicles.Tank e){return TextureAtlas.LOCATION_BLOCKS;}
  @Override public void render(Vehicles.Tank e,float yaw,float pt,PoseStack p,MultiBufferSource b,int light){
   float y=Mth.rotLerp(pt,e.yRotO,e.getYRot()),ty=Mth.rotLerp(pt,e.turretO,e.turretR),bp=Mth.lerp(pt,e.barrelO,e.barrelR);
   float age=e.tickCount+pt-e.firedTick(),kick=age>=0&&age<8?1-age/8F:0;
   p.pushPose();
   p.pushPose();p.mulPose(Axis.YP.rotationDegrees(180F-y));p.scale(4,4,4);p.translate(-.5,0,-.5);DmtGearLayer.model(paint("tank_hull",e),p,b,light);p.popPose();
   p.mulPose(Axis.YP.rotationDegrees(180F-ty));p.scale(4,4,4);p.translate(-.5,0,-.5);
   if(!fromInside(e))DmtGearLayer.model(paint("tank_turret",e),p,b,light);
   p.translate(.5,10.5/16,1/16F);p.mulPose(Axis.XP.rotationDegrees(-bp));p.translate(-.5,-10.5/16,-1/16F+kick*.12F);
   DmtGearLayer.model(paint("tank_barrel",e),p,b,light);
   p.popPose();
   super.render(e,yaw,pt,p,b,light);
  }
 }
}
